package com.github.sanctum.clans.model;

import org.jetbrains.annotations.NotNull;

/**
 * An out going ticket (message response) listener in conjunction to information received from a {@link Consultant}
 *
 * @see Consultant
 */
@FunctionalInterface
public interface OutgoingConsultationListener {

	void onReceiveResponse(@NotNull Ticket response);

}
