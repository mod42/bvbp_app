/*
 * Copyright (c) 2013 CA Technologies. All rights reserved.
 */
package com.l7tech.examplea.multiuser;

/**
 * Define a User storage interface to store user profile.
 * @param <T> The user profile type
 */
public interface UserStorage<T> {

    /**
     * Add the provided user to the user storage.
     *
     * @param user The user to be added to the user storage.
     */
    public void add(T user);

    /**
     * Remove the provide username from the user storage.
     *
     * @param user The user to be removed from the user storage.
     */
    public void remove(T user);

    /**
     * Retrieve all the user from the user storage.
     *
     * @return All the user from the user storage as an Array
     */
    public T[] getAll();

    /**
     * Clear the user storage, all user will be removed from the user storage.
     */
    public void clear();
}
