param(
    [switch]$NoBrowser
)

$ErrorActionPreference = 'Stop'

$projectRoot = $PSScriptRoot
$toolsRoot = Join-Path (Split-Path $projectRoot -Parent) '.tools'
$runtimeDir = if ($env:LOCALAPPDATA) {
    Join-Path $env:LOCALAPPDATA 'mall-test-system'
}
else {
    Join-Path $env:TEMP 'mall-test-system'
}
$frontendDir = Join-Path $projectRoot 'frontend'
$backendDir = Join-Path $projectRoot 'backend'
$appUrl = 'http://127.0.0.1:5173/products'

New-Item -ItemType Directory -Path $runtimeDir -Force | Out-Null

function Get-PortListener {
    param([int]$Port)

    Get-NetTCPConnection -LocalPort $Port -State Listen -ErrorAction SilentlyContinue |
        Select-Object -First 1
}

function Get-PortOwnerText {
    param([int]$Port)

    $listener = Get-PortListener -Port $Port
    if (-not $listener) {
        return 'no listener'
    }

    $process = Get-Process -Id $listener.OwningProcess -ErrorAction SilentlyContinue
    if ($process) {
        return "PID $($listener.OwningProcess) ($($process.ProcessName))"
    }

    return "PID $($listener.OwningProcess)"
}

function Wait-ForPort {
    param(
        [int]$Port,
        [int]$TimeoutSeconds
    )

    $deadline = (Get-Date).AddSeconds($TimeoutSeconds)
    while ((Get-Date) -lt $deadline) {
        if (Get-PortListener -Port $Port) {
            return $true
        }
        Start-Sleep -Milliseconds 500
    }

    return $false
}

function Test-HttpOk {
    param([string]$Uri)

    try {
        $response = Invoke-WebRequest -UseBasicParsing -Uri $Uri -TimeoutSec 3
        return $response.StatusCode -eq 200
    }
    catch {
        return $false
    }
}

function Wait-ForHttp {
    param(
        [string]$Uri,
        [int]$TimeoutSeconds
    )

    $deadline = (Get-Date).AddSeconds($TimeoutSeconds)
    while ((Get-Date) -lt $deadline) {
        if (Test-HttpOk -Uri $Uri) {
            return $true
        }
        Start-Sleep -Seconds 1
    }

    return $false
}

function Show-LogTail {
    param([string]$Path)

    if (Test-Path -LiteralPath $Path) {
        Write-Host "`nLast log lines from $Path" -ForegroundColor Yellow
        Get-Content -LiteralPath $Path -Tail 35 -ErrorAction SilentlyContinue
    }
}

function Resolve-Maven {
    foreach ($name in @('mvn.cmd', 'mvn')) {
        $command = Get-Command $name -ErrorAction SilentlyContinue
        if ($command) {
            return $command.Source
        }
    }

    $candidate = Get-ChildItem -LiteralPath $toolsRoot -Directory -Filter 'apache-maven-*' -ErrorAction SilentlyContinue |
        Sort-Object Name -Descending |
        ForEach-Object { Join-Path $_.FullName 'bin\mvn.cmd' } |
        Where-Object { Test-Path -LiteralPath $_ } |
        Select-Object -First 1

    if ($candidate) {
        return $candidate
    }

    throw 'Maven was not found. Install Maven or place apache-maven-* under the sibling .tools directory.'
}

function Resolve-Pnpm {
    param([string]$ProjectDirectory)

    foreach ($name in @('pnpm.cmd', 'pnpm')) {
        $command = Get-Command $name -ErrorAction SilentlyContinue
        if ($command) {
            return [PSCustomObject]@{
                FilePath = $command.Source
                PrefixArguments = @()
            }
        }
    }

    foreach ($name in @('corepack.cmd', 'corepack')) {
        $command = Get-Command $name -ErrorAction SilentlyContinue
        if ($command) {
            # Older Corepack releases fail while looking up the latest pnpm
            # signing key. The project pins pnpm, so latest-version lookup is
            # unnecessary and can be disabled safely.
            $env:COREPACK_DEFAULT_TO_LATEST = '0'

            Push-Location $ProjectDirectory
            try {
                & $command.Source pnpm --version 2>&1 | Out-Null
                if ($LASTEXITCODE -ne 0) {
                    throw 'Corepack could not start the pnpm version configured by the frontend project.'
                }
            }
            finally {
                Pop-Location
            }

            return [PSCustomObject]@{
                FilePath = $command.Source
                PrefixArguments = @('pnpm')
            }
        }
    }

    throw 'Neither pnpm nor Corepack was found. Reinstall Node.js with Corepack included.'
}

try {
    Write-Host 'Mall Test System - one-click startup' -ForegroundColor Cyan
    Write-Host "Project: $projectRoot"

    if (-not (Get-Command node.exe -ErrorAction SilentlyContinue)) {
        throw 'node.exe was not found. Install Node.js before starting the project.'
    }

    $mavenCmd = Resolve-Maven
    $pnpm = Resolve-Pnpm -ProjectDirectory $frontendDir

    Write-Host "`n[1/3] MySQL (127.0.0.1:3308)"
    $mysqlBase = 'C:\Program Files\MySQL\MySQL Server 8.0'
    $mysqlExe = Join-Path $mysqlBase 'bin\mysql.exe'
    $mysqldExe = Join-Path $mysqlBase 'bin\mysqld.exe'
    $mysqlIni = Join-Path $toolsRoot 'mysql-v2.ini'

    if (-not (Get-PortListener -Port 3308)) {
        foreach ($requiredFile in @($mysqldExe, $mysqlIni)) {
            if (-not (Test-Path -LiteralPath $requiredFile)) {
                throw "Required MySQL runtime file was not found: $requiredFile"
            }
        }

        $mysqlProcess = Start-Process -FilePath $mysqldExe `
            -ArgumentList "--defaults-file=$mysqlIni" `
            -WindowStyle Hidden `
            -PassThru

        Write-Host "Starting MySQL (PID $($mysqlProcess.Id))..."
        if (-not (Wait-ForPort -Port 3308 -TimeoutSeconds 45)) {
            throw 'MySQL did not open port 3308 within 45 seconds.'
        }
    }

    if (Test-Path -LiteralPath $mysqlExe) {
        $dbPassword = if ($env:LOCAL_DB_PASSWORD) { $env:LOCAL_DB_PASSWORD } else { '123456' }
        $env:MYSQL_PWD = $dbPassword
        try {
            & $mysqlExe --protocol=tcp --host=127.0.0.1 --port=3308 --user=root `
                --skip-column-names --execute='SELECT COUNT(*) FROM mall_test_system.product;' | Out-Null
            if ($LASTEXITCODE -ne 0) {
                throw "Port 3308 is open, but the project database check failed. Owner: $(Get-PortOwnerText -Port 3308)"
            }
        }
        finally {
            Remove-Item Env:MYSQL_PWD -ErrorAction SilentlyContinue
        }
    }
    Write-Host "MySQL ready - $(Get-PortOwnerText -Port 3308)" -ForegroundColor Green

    Write-Host "`n[2/3] Spring Boot (127.0.0.1:8080)"
    $backendHealth = 'http://127.0.0.1:8080/api/products?page=1&size=1'
    if (Get-PortListener -Port 8080) {
        if (-not (Test-HttpOk -Uri $backendHealth)) {
            throw "Port 8080 is occupied by another service: $(Get-PortOwnerText -Port 8080)"
        }
    }
    else {
        $env:DB_URL = 'jdbc:mysql://127.0.0.1:3308/mall_test_system?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true'
        $env:DB_USERNAME = 'root'
        $env:DB_PASSWORD = if ($env:LOCAL_DB_PASSWORD) { $env:LOCAL_DB_PASSWORD } else { '123456' }
        $env:SERVER_PORT = '8080'

        $backendOut = Join-Path $runtimeDir 'backend.out.log'
        $backendErr = Join-Path $runtimeDir 'backend.err.log'
        $backendProcess = Start-Process -FilePath $mavenCmd `
            -ArgumentList 'spring-boot:run' `
            -WorkingDirectory $backendDir `
            -RedirectStandardOutput $backendOut `
            -RedirectStandardError $backendErr `
            -WindowStyle Hidden `
            -PassThru

        Write-Host "Starting backend (PID $($backendProcess.Id))..."
        if (-not (Wait-ForHttp -Uri $backendHealth -TimeoutSeconds 90)) {
            Show-LogTail -Path $backendOut
            Show-LogTail -Path $backendErr
            throw 'Backend health check failed on port 8080.'
        }
    }
    Write-Host "Backend ready - $(Get-PortOwnerText -Port 8080)" -ForegroundColor Green

    Write-Host "`n[3/3] Vue/Vite (127.0.0.1:5173)"
    if (-not (Test-Path -LiteralPath (Join-Path $frontendDir 'node_modules'))) {
        Write-Host 'Installing frontend dependencies...'
        $installArguments = @($pnpm.PrefixArguments) + @('install', '--frozen-lockfile')
        & $pnpm.FilePath @installArguments
        if ($LASTEXITCODE -ne 0) {
            throw 'pnpm install failed.'
        }
    }

    $frontendHealth = 'http://127.0.0.1:5173/api/products?page=1&size=1'
    if (Get-PortListener -Port 5173) {
        if (-not (Test-HttpOk -Uri $frontendHealth)) {
            throw "Port 5173 is occupied by another service: $(Get-PortOwnerText -Port 5173)"
        }
    }
    else {
        $frontendOut = Join-Path $runtimeDir 'frontend.out.log'
        $frontendErr = Join-Path $runtimeDir 'frontend.err.log'
        $devArguments = @($pnpm.PrefixArguments) + @('dev')
        $frontendProcess = Start-Process -FilePath $pnpm.FilePath `
            -ArgumentList $devArguments `
            -WorkingDirectory $frontendDir `
            -RedirectStandardOutput $frontendOut `
            -RedirectStandardError $frontendErr `
            -WindowStyle Hidden `
            -PassThru

        Write-Host "Starting frontend (PID $($frontendProcess.Id))..."
        if (-not (Wait-ForHttp -Uri $frontendHealth -TimeoutSeconds 45)) {
            Show-LogTail -Path $frontendOut
            Show-LogTail -Path $frontendErr
            throw 'Frontend health check failed on port 5173.'
        }
    }
    Write-Host "Frontend ready - $(Get-PortOwnerText -Port 5173)" -ForegroundColor Green

    Write-Host "`nAll services are ready: $appUrl" -ForegroundColor Cyan
    if (-not $NoBrowser) {
        Start-Process $appUrl
    }

    exit 0
}
catch {
    Write-Host "`nStartup failed: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}
