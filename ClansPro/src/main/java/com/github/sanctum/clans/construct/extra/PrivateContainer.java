package com.github.sanctum.clans.construct.extra;

import java.util.Map;
import java.util.Set;

public interface PrivateContainer {

	<T> T get(Class<T> clazz, String key);

	void set(String key, Object o);

	Set<Map.Entry<String, Object>> entrySet();

}
