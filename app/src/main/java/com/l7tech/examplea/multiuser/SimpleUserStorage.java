/*
 * Copyright (c) 2013 CA Technologies. All rights reserved.
 */
package com.l7tech.examplea.multiuser;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * The SimpleUserStorage stores set of username as String to {@link android.content.SharedPreferences} with attribute name
 * {@link com.l7tech.examplea.multiuser.SimpleUserStorage#PREFS_NAME}.
 */
public class SimpleUserStorage implements UserStorage<String> {

    private static final String PREFS_NAME = "multi_user_demo";
    private static final String USERS = "users";
    private final SharedPreferences prefs;

    public SimpleUserStorage(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    @Override
    public void add(String username) {
        if (username != null) {
            Set<String> users = prefs.getStringSet(USERS, new HashSet<String>());
            users.add(username);
            SharedPreferences.Editor editor = prefs.edit();
            editor.clear();
            editor.putStringSet(USERS, users);
            editor.commit();
        }
    }

    @Override
    public void remove(String username) {
        if (username != null) {
            Set<String> users = prefs.getStringSet(USERS, new HashSet<String>());
            users.remove(username);
            prefs.edit().putStringSet(USERS, users).apply();
        }
    }

    @Override
    public String[] getAll() {
        Set<String> users = prefs.getStringSet(USERS, new HashSet<String>());
        String[] a = new String[users.size()];
        users.toArray(a);
        Arrays.sort(a);
        return  a;
    }

    @Override
    public void clear() {
        prefs.edit().clear().apply();
    }
}
