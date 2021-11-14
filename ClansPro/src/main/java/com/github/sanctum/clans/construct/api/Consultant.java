package com.github.sanctum.clans.construct.api;

import com.github.sanctum.clans.construct.impl.ServerAssociate;
import java.util.function.Supplier;
import org.jetbrains.annotations.NotNull;

/**
 * An interface describing an object that has a form of ai, something that can reply to an inquiry.
 *
 * @see ServerAssociate
 */
public interface Consultant {

	/**
	 * Send an object of any type to this entity, the result of doing so afterwards depends on
	 * if listeners are waiting or not, you can setup up automatic reactions using {@link IncomingConsultationListener}'s & {@link OutgoingConsultationListener}'s.
	 * An {@link InvasiveEntity.Tag} is a string supplier used as a way to dedicate a memory space for a listener.
	 *
	 * Regardless of no outgoing listeners this method will return any possible responses known as {@link Ticket}'s in conjunction to relative data.
	 *
	 * @see Ticket
	 * @apiNote This method simply skips channel selection and traverses every registered listener.
	 * @param supplier The object supplier to use to provide any given object type.
	 * @return An array of message responses, empty if no valid processing is provided.
	 */
	@NotNull Ticket[] sendMessage(@NotNull Supplier<Object> supplier);

	/**
	 * Send an object of any type to this entity, the result of doing so afterwards depends on
	 * if listeners are waiting or not, you can setup up automatic reactions using {@link IncomingConsultationListener}'s & {@link OutgoingConsultationListener}'s.
	 * An {@link InvasiveEntity.Tag} is a string supplier used as a way to dedicate a memory space for a listener.
	 *
	 * Regardless of no outgoing listeners this method will return any possible response known as a {@link Ticket} in conjunction to relative data.
	 *
	 * @see Ticket
	 * @param channel The channel to send the message to.
	 * @param supplier The object supplier to use to provide any given object type.
	 * @return A message response, empty if no valid processing is provided.
	 */
	@NotNull Ticket sendMessage(@NotNull InvasiveEntity.Tag channel, @NotNull Supplier<Object> supplier);

	/**
	 * Setup a custom listener for when this entity receives messages.
	 *
	 * @param listener The listener to use for incoming information to this entity.
	 */
	void registerIncomingListener(@NotNull InvasiveEntity.Tag holder, @NotNull IncomingConsultationListener listener);

	/**
	 * Setup a custom listener for when this entity receives a ticket (message response).
	 *
	 * @param listener The listener to use for outgoing responses to information for this entity.
	 */
	void registerOutgoingListener(@NotNull InvasiveEntity.Tag holder, @NotNull OutgoingConsultationListener listener);

	boolean hasIncomingListener(@NotNull InvasiveEntity.Tag holder);

	boolean hasOutgoingListener(@NotNull InvasiveEntity.Tag holder);

}
