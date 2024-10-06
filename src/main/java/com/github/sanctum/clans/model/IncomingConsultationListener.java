package com.github.sanctum.clans.model;

import org.jetbrains.annotations.NotNull;

/**
 * An in coming message listener for use in tandem with {@link Consultant}
 *
 * @see Consultant
 */
@FunctionalInterface
public interface IncomingConsultationListener {

	@NotNull Ticket onReceiveMessage(@NotNull Object object);

}
