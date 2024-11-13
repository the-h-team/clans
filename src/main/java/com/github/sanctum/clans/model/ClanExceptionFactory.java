package com.github.sanctum.clans.model;

import com.github.sanctum.panther.annotation.Note;
import java.lang.reflect.InvocationTargetException;
import java.util.function.Function;

public class ClanExceptionFactory<T extends ClanError> {

	private Object value;
	private final Class<? extends ClanError> errorClass;

	protected ClanExceptionFactory(Class<T> value) {
		this.errorClass = value;
	}

	protected ClanExceptionFactory(Function<String, T> rSupplier) {
		this.errorClass = rSupplier.apply("").getClass();
	}

	public static <T extends ClanError> ClanExceptionFactory<T> call(Class<T> value) {
		return new ClanExceptionFactory<>(value);
	}

	public static <T extends ClanError> ClanExceptionFactory<T> call(Function<String, T> rSupplier) {
		return new ClanExceptionFactory<>(rSupplier);
	}

	public <R> ClanExceptionFactory<T> check(R value) {
		this.value = value;
		return this;
	}

	@Note("Reverse the check if you need the value to be not null")
	public void run(String message, boolean reverse) {
		try {
			Error exception = errorClass.getDeclaredConstructor(String.class).newInstance(message);
			if (reverse) {
				if (value != null) {
					throw exception;
				}
			} else {
				if (value == null) {
					throw exception;
				}
			}
		} catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException ignored) {
		}
	}

	public void run(String message) {
		try {
			Error exception = errorClass.getDeclaredConstructor(String.class).newInstance(message);
			if (value == null) {
				throw exception;
			}
		} catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException ignored) {
		}
	}


}
