package com.github.sanctum.clans.construct.impl;

import java.util.Map;

public class SimpleEntry<K, V> implements Map.Entry<K, V> {

	private final K key;
	private V value;

	public SimpleEntry(K k, V v) {
		this.key = k;
		this.value = v;
	}

	@Override
	public K getKey() {
		return this.key;
	}

	@Override
	public V getValue() {
		return this.value;
	}

	@Override
	public V setValue(V value) {
		this.value = value;
		return value;
	}
}
