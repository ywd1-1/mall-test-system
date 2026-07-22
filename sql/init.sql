CREATE DATABASE IF NOT EXISTS mall_test_system
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE mall_test_system;

DROP TABLE IF EXISTS order_status_log;
DROP TABLE IF EXISTS order_item;
DROP TABLE IF EXISTS orders;
DROP TABLE IF EXISTS cart;
DROP TABLE IF EXISTS user_address;
DROP TABLE IF EXISTS product;
DROP TABLE IF EXISTS `user`;

CREATE TABLE `user` (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
  password VARCHAR(100) NOT NULL COMMENT 'BCrypt 密码哈希',
  role VARCHAR(20) NOT NULL COMMENT 'USER / ADMIN',
  status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE / DISABLED',
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  KEY idx_user_role_status (role, status),
  CONSTRAINT chk_user_role CHECK (role IN ('USER', 'ADMIN')),
  CONSTRAINT chk_user_status CHECK (status IN ('ACTIVE', 'DISABLED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

CREATE TABLE user_address (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  recipient_name VARCHAR(50) NOT NULL COMMENT '收货人',
  phone VARCHAR(20) NOT NULL COMMENT '手机号',
  province VARCHAR(50) NOT NULL,
  city VARCHAR(50) NOT NULL,
  district VARCHAR(50) NOT NULL,
  detail_address VARCHAR(200) NOT NULL,
  is_default TINYINT(1) NOT NULL DEFAULT 0,
  default_user_id BIGINT GENERATED ALWAYS AS (
    CASE WHEN is_default = 1 THEN user_id ELSE NULL END
  ) STORED,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  KEY idx_address_user_id (user_id),
  UNIQUE KEY uk_address_one_default (default_user_id),
  CONSTRAINT fk_address_user FOREIGN KEY (user_id) REFERENCES `user` (id),
  CONSTRAINT chk_address_default CHECK (is_default IN (0, 1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户收货地址表';

CREATE TABLE product (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(100) NOT NULL COMMENT '商品名称',
  price DECIMAL(10,2) NOT NULL COMMENT '商品价格',
  stock INT NOT NULL COMMENT '库存',
  category VARCHAR(50) NOT NULL COMMENT '商品分类',
  status VARCHAR(20) NOT NULL COMMENT 'ON_SALE / OFF_SHELF',
  image_url VARCHAR(500) COMMENT '商品图片地址',
  description VARCHAR(1000) COMMENT '商品描述',
  deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '软删除标记',
  deleted_at DATETIME NULL,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  KEY idx_product_category (category),
  KEY idx_product_status_deleted (status, deleted),
  CONSTRAINT chk_product_price CHECK (price > 0),
  CONSTRAINT chk_product_stock CHECK (stock >= 0),
  CONSTRAINT chk_product_status CHECK (status IN ('ON_SALE', 'OFF_SHELF')),
  CONSTRAINT chk_product_deleted CHECK (deleted IN (0, 1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品表';

CREATE TABLE cart (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  product_id BIGINT NOT NULL,
  quantity INT NOT NULL,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  UNIQUE KEY uk_cart_user_product (user_id, product_id),
  KEY idx_cart_user_id (user_id),
  KEY idx_cart_product_id (product_id),
  CONSTRAINT fk_cart_user FOREIGN KEY (user_id) REFERENCES `user` (id),
  CONSTRAINT fk_cart_product FOREIGN KEY (product_id) REFERENCES product (id),
  CONSTRAINT chk_cart_quantity CHECK (quantity > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='购物车表';

CREATE TABLE orders (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  order_no VARCHAR(50) NOT NULL UNIQUE COMMENT '订单编号',
  user_id BIGINT NOT NULL,
  status VARCHAR(20) NOT NULL COMMENT 'CREATED / PAID / SHIPPED / COMPLETED / CANCELLED',
  total_amount DECIMAL(10,2) NOT NULL,
  address_id BIGINT NULL COMMENT '下单时使用的地址 ID，不设置外键以允许删除地址',
  recipient_name VARCHAR(50) NOT NULL COMMENT '收货人快照',
  recipient_phone VARCHAR(20) NOT NULL COMMENT '手机号快照',
  province VARCHAR(50) NOT NULL COMMENT '省份快照',
  city VARCHAR(50) NOT NULL COMMENT '城市快照',
  district VARCHAR(50) NOT NULL COMMENT '区县快照',
  detail_address VARCHAR(200) NOT NULL COMMENT '详细地址快照',
  shipping_company VARCHAR(100) NULL,
  tracking_number VARCHAR(100) NULL,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  KEY idx_orders_user_id (user_id),
  KEY idx_orders_status_created_at (status, created_at),
  KEY idx_orders_created_at (created_at),
  CONSTRAINT fk_orders_user FOREIGN KEY (user_id) REFERENCES `user` (id),
  CONSTRAINT chk_orders_status CHECK (status IN ('CREATED', 'PAID', 'SHIPPED', 'COMPLETED', 'CANCELLED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表，包含收货地址快照';

CREATE TABLE order_item (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  order_id BIGINT NOT NULL,
  product_id BIGINT NOT NULL COMMENT '商品 ID 快照',
  product_name VARCHAR(100) NOT NULL COMMENT '商品名称快照',
  product_image_url VARCHAR(500) COMMENT '商品图片快照',
  price DECIMAL(10,2) NOT NULL COMMENT '下单单价',
  quantity INT NOT NULL,
  subtotal DECIMAL(10,2) NOT NULL,
  KEY idx_order_item_order_id (order_id),
  KEY idx_order_item_product_id (product_id),
  CONSTRAINT fk_order_item_order FOREIGN KEY (order_id) REFERENCES orders (id),
  CONSTRAINT fk_order_item_product FOREIGN KEY (product_id) REFERENCES product (id),
  CONSTRAINT chk_order_item_quantity CHECK (quantity > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单明细表';

CREATE TABLE order_status_log (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  order_id BIGINT NOT NULL,
  old_status VARCHAR(20) NULL,
  new_status VARCHAR(20) NOT NULL,
  operator_id BIGINT NULL,
  operator_name VARCHAR(50) NOT NULL,
  operator_role VARCHAR(20) NOT NULL,
  operated_at DATETIME NOT NULL,
  remark VARCHAR(255) NULL,
  KEY idx_order_log_order_time (order_id, operated_at),
  CONSTRAINT fk_order_log_order FOREIGN KEY (order_id) REFERENCES orders (id),
  CONSTRAINT chk_order_log_new_status CHECK (new_status IN ('CREATED', 'PAID', 'SHIPPED', 'COMPLETED', 'CANCELLED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单状态流转记录';

INSERT INTO `user` (id, username, password, role, status, created_at, updated_at) VALUES
(1, 'user', '$2b$10$ypU/7h/lM.8jNyziy8xTeOlo5r3K8flGI3TOvAigEPn6oobF9oZmy', 'USER', 'ACTIVE', NOW(), NOW()),
(2, 'admin', '$2b$10$1kr3Fau2xAEoLMLc7fcJ4eL7XvVpdVfPCnuzZxdNiOz5RckH0XXLe', 'ADMIN', 'ACTIVE', NOW(), NOW()),
(3, 'disabled_user', '$2b$10$UIeqDBkivE/YTz2W.NSKWeJAiofRWRGDC0Z8vHVwL5sWDUyjoEEhy', 'USER', 'DISABLED', NOW(), NOW());

INSERT INTO user_address
(id, user_id, recipient_name, phone, province, city, district, detail_address, is_default, created_at, updated_at) VALUES
(1, 1, '测试用户', '13800000001', '广东省', '深圳市', '南山区', '科技园测试路 100 号 A 座 801', 1, NOW(), NOW()),
(2, 1, '备用收货人', '13900000002', '广东省', '广州市', '天河区', '软件园验证街 20 号 3 楼', 0, NOW(), NOW());

INSERT INTO product
(id, name, price, stock, category, status, image_url, description, deleted, deleted_at, created_at, updated_at) VALUES
(1, '智能手机 X1', 2999.00, 35, '手机', 'ON_SALE', 'https://picsum.photos/seed/mall-phone-x1/480/320', '正常库存商品，用于常规下单和支付流程。', 0, NULL, NOW(), NOW()),
(2, '轻薄笔记本 Pro', 6299.00, 5, '电脑', 'ON_SALE', 'https://picsum.photos/seed/mall-laptop-pro/480/320', '低库存样本，用于库存边界和并发下单测试。', 0, NULL, NOW(), NOW()),
(3, '无线蓝牙耳机', 199.00, 80, '配件', 'ON_SALE', 'https://picsum.photos/seed/mall-earphone/480/320', '正常库存配件。', 0, NULL, NOW(), NOW()),
(4, '机械键盘 K87', 299.00, 0, '配件', 'ON_SALE', 'https://picsum.photos/seed/mall-keyboard/480/320', '零库存样本，用于库存不足测试。', 0, NULL, NOW(), NOW()),
(5, '27 英寸显示器', 1299.00, 8, '电脑', 'ON_SALE', 'https://picsum.photos/seed/mall-monitor/480/320', '低库存显示器。', 0, NULL, NOW(), NOW()),
(6, 'USB-C 扩展坞', 189.00, 55, '配件', 'ON_SALE', 'https://picsum.photos/seed/mall-dock/480/320', '正常库存扩展坞。', 0, NULL, NOW(), NOW()),
(7, '手机保护壳', 39.00, 2, '手机', 'ON_SALE', 'https://picsum.photos/seed/mall-phone-case/480/320', '极低库存边界样本。', 0, NULL, NOW(), NOW()),
(8, '办公人体工学椅', 899.00, 20, '生活用品', 'ON_SALE', 'https://picsum.photos/seed/mall-chair/480/320', '正常上架商品。', 0, NULL, NOW(), NOW()),
(9, '不锈钢保温杯', 79.00, 6, '生活用品', 'ON_SALE', 'https://picsum.photos/seed/mall-cup/480/320', '低库存生活用品。', 0, NULL, NOW(), NOW()),
(10, '移动固态硬盘 1TB', 499.00, 60, '电脑', 'ON_SALE', 'https://picsum.photos/seed/mall-ssd/480/320', '正常库存电脑配件。', 0, NULL, NOW(), NOW()),
(11, '智能手环', 159.00, 100, '手机', 'ON_SALE', 'https://picsum.photos/seed/mall-band/480/320', '高库存商品。', 0, NULL, NOW(), NOW()),
(12, '下架测试配件', 59.00, 15, '配件', 'OFF_SHELF', 'https://picsum.photos/seed/mall-off-shelf/480/320', '管理员可见、普通用户不可见的下架商品。', 0, NULL, NOW(), NOW()),
(13, '软删除测试商品', 99.00, 10, '配件', 'OFF_SHELF', 'https://picsum.photos/seed/mall-deleted/480/320', '默认前后台列表均不可见，且不能再次上架。', 1, NOW(), NOW(), NOW());

INSERT INTO orders
(id, order_no, user_id, status, total_amount, address_id, recipient_name, recipient_phone,
 province, city, district, detail_address, shipping_company, tracking_number, created_at, updated_at) VALUES
(1, 'V2-DEMO-CREATED', 1, 'CREATED', 2999.00, 1, '测试用户', '13800000001', '广东省', '深圳市', '南山区', '科技园测试路 100 号 A 座 801', NULL, NULL, DATE_SUB(NOW(), INTERVAL 5 DAY), DATE_SUB(NOW(), INTERVAL 5 DAY)),
(2, 'V2-DEMO-PAID', 1, 'PAID', 199.00, 1, '测试用户', '13800000001', '广东省', '深圳市', '南山区', '科技园测试路 100 号 A 座 801', NULL, NULL, DATE_SUB(NOW(), INTERVAL 4 DAY), DATE_SUB(NOW(), INTERVAL 3 DAY)),
(3, 'V2-DEMO-SHIPPED', 1, 'SHIPPED', 189.00, 1, '测试用户', '13800000001', '广东省', '深圳市', '南山区', '科技园测试路 100 号 A 座 801', '顺丰速运', 'SF1000000003', DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY)),
(4, 'V2-DEMO-COMPLETED', 1, 'COMPLETED', 79.00, 2, '备用收货人', '13900000002', '广东省', '广州市', '天河区', '软件园验证街 20 号 3 楼', '京东物流', 'JD1000000004', DATE_SUB(NOW(), INTERVAL 10 DAY), DATE_SUB(NOW(), INTERVAL 6 DAY)),
(5, 'V2-DEMO-CANCELLED', 1, 'CANCELLED', 39.00, 1, '测试用户', '13800000001', '广东省', '深圳市', '南山区', '科技园测试路 100 号 A 座 801', NULL, NULL, DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY));

INSERT INTO order_item
(id, order_id, product_id, product_name, product_image_url, price, quantity, subtotal) VALUES
(1, 1, 1, '智能手机 X1', 'https://picsum.photos/seed/mall-phone-x1/480/320', 2999.00, 1, 2999.00),
(2, 2, 3, '无线蓝牙耳机', 'https://picsum.photos/seed/mall-earphone/480/320', 199.00, 1, 199.00),
(3, 3, 6, 'USB-C 扩展坞', 'https://picsum.photos/seed/mall-dock/480/320', 189.00, 1, 189.00),
(4, 4, 9, '不锈钢保温杯', 'https://picsum.photos/seed/mall-cup/480/320', 79.00, 1, 79.00),
(5, 5, 7, '手机保护壳', 'https://picsum.photos/seed/mall-phone-case/480/320', 39.00, 1, 39.00);

INSERT INTO order_status_log
(order_id, old_status, new_status, operator_id, operator_name, operator_role, operated_at, remark) VALUES
(1, NULL, 'CREATED', 1, 'user', 'USER', DATE_SUB(NOW(), INTERVAL 5 DAY), '创建订单'),
(2, NULL, 'CREATED', 1, 'user', 'USER', DATE_SUB(NOW(), INTERVAL 4 DAY), '创建订单'),
(2, 'CREATED', 'PAID', 1, 'user', 'USER', DATE_SUB(NOW(), INTERVAL 3 DAY), '用户完成支付'),
(3, NULL, 'CREATED', 1, 'user', 'USER', DATE_SUB(NOW(), INTERVAL 3 DAY), '创建订单'),
(3, 'CREATED', 'PAID', 1, 'user', 'USER', DATE_SUB(NOW(), INTERVAL 2 DAY), '用户完成支付'),
(3, 'PAID', 'SHIPPED', 2, 'admin', 'ADMIN', DATE_SUB(NOW(), INTERVAL 1 DAY), '管理员发货：顺丰速运 / SF1000000003'),
(4, NULL, 'CREATED', 1, 'user', 'USER', DATE_SUB(NOW(), INTERVAL 10 DAY), '创建订单'),
(4, 'CREATED', 'PAID', 1, 'user', 'USER', DATE_SUB(NOW(), INTERVAL 9 DAY), '用户完成支付'),
(4, 'PAID', 'SHIPPED', 2, 'admin', 'ADMIN', DATE_SUB(NOW(), INTERVAL 8 DAY), '管理员发货：京东物流 / JD1000000004'),
(4, 'SHIPPED', 'COMPLETED', 1, 'user', 'USER', DATE_SUB(NOW(), INTERVAL 6 DAY), '用户确认收货'),
(5, NULL, 'CREATED', 1, 'user', 'USER', DATE_SUB(NOW(), INTERVAL 2 DAY), '创建订单'),
(5, 'CREATED', 'CANCELLED', 1, 'user', 'USER', DATE_SUB(NOW(), INTERVAL 2 DAY), '用户取消订单并恢复库存');
