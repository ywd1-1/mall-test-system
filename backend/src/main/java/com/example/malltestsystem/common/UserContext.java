package com.example.malltestsystem.common;

import com.example.malltestsystem.entity.User;

public final class UserContext {
    private static final ThreadLocal<User> CURRENT = new ThreadLocal<User>();

    private UserContext() {
    }

    public static void set(User user) {
        CURRENT.set(user);
    }

    public static User get() {
        return CURRENT.get();
    }

    public static Long getUserId() {
        User user = get();
        return user == null ? null : user.getId();
    }

    public static void clear() {
        CURRENT.remove();
    }
}
