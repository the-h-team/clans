package com.github.sanctum.clans.construct.api;

import com.github.sanctum.clans.construct.util.ClanError;
import com.github.sanctum.panther.annotation.Note;
import java.lang.reflect.InvocationTargetException;
import java.util.function.Function;

public class ClanException<T extends ClanError> {

	private Object value;
	private final Class<? extends ClanError> errorClass;

	protected ClanException(Class<T> value) {
		this.errorClass = value;
	}

	protected ClanException(Function<String, T> rSupplier) {
		this.errorClass = rSupplier.apply("").getClass();
	}

	public static <T extends ClanError> ClanException<T> call(Class<T> value) {
		return new ClanException<>(value);
	}

	public static <T extends ClanError> ClanException<T> call(Function<String, T> rSupplier) {
		return new ClanException<>(rSupplier);
	}

	public <R> ClanException<T> check(R value) {
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
