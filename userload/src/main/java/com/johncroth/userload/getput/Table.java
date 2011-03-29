package com.johncroth.userload.getput;

/**
 * Trivial interface for things that store key/value pairs.
 */
public interface Table {

	public abstract String get(String key);

	public abstract void put(String key, String value);

}