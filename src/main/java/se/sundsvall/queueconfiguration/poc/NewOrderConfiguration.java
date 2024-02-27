package se.sundsvall.queueconfiguration.poc;

import static se.sundsvall.queueconfiguration.poc.SharedConfiguration.POC_DLX;
import static se.sundsvall.queueconfiguration.poc.SharedConfiguration.POC_EXCHANGE;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class NewOrderConfiguration {

	/**
	 * Queues
	 */
	private static final String NEW_ORDER_QUEUE = "newOrderQueue";
	private static final String NEW_ORDER_RETRY_QUEUE = NEW_ORDER_QUEUE + ".retry";
	private static final String NEW_ORDER_DEAD_LETTER_QUEUE = NEW_ORDER_QUEUE + ".dlq";

	/**
	 * Routing keys
	 */
	private static final String NEW_ORDER_ROUTING_KEY = "RK_" + NEW_ORDER_QUEUE;
	private static final String NEW_ORDER_RETRY_ROUTING_KEY = NEW_ORDER_ROUTING_KEY + ".retry";
	private static final String NEW_ORDER_DEAD_LETTER_ROUTING_KEY = NEW_ORDER_QUEUE + ".dlq";

	private final SharedConfiguration sharedConfiguration;

	NewOrderConfiguration(SharedConfiguration sharedConfiguration) {
		this.sharedConfiguration = sharedConfiguration;
	}

	/**
	 * Main queue, where the messages are first sent.
	 */
	@Bean
	public Queue newOrderQueue() {
		return QueueBuilder.durable(NEW_ORDER_QUEUE)
			.deadLetterExchange(POC_DLX)
			.deadLetterRoutingKey(NEW_ORDER_DEAD_LETTER_QUEUE)
			.quorum()
			.build();
	}

	/**
	 * Retry queue, where the messages are sent if they fail to be processed.
	 * Uses a Time-To-Live (TTL) of 5000ms. If the messages is not processed in that time
	 * the message will be sent to the configured dead-letter-exchange using the
	 * dead-letter-routing-key. In this case, the main exchange and the main routing key is used.
	 * ---------------------------------------------------------------------------------------------
	 * The message will be sent to the main queue again, and the process will start over.
	 * The consumer have the ability to set the number of retries before sending the message to
	 * the dead letter queue.
	 */
	@Bean
	public Queue newOrderRetryQueue() {
		return QueueBuilder.durable(NEW_ORDER_RETRY_QUEUE)
			.deadLetterExchange(POC_EXCHANGE)
			.deadLetterRoutingKey(NEW_ORDER_ROUTING_KEY)
			.ttl(5000)
			.quorum()
			.build();
	}

	/**
	 * Dead-letter queue, where the messages are sent if they fail to be processed after the
	 * configured number of retries. This allows us to act on the messages that failed to process
	 * with a seperate consumer if we so please.
	 */
	@Bean
	public Queue newOrderDeadLetterQueue() {
		return QueueBuilder.durable(NEW_ORDER_DEAD_LETTER_QUEUE)
			.quorum()
			.build();
	}

	/**
	 * Binding the main queue bean to the mian exchange with a routing key.
	 */
	@Bean
	Binding newOrderBinding() {
		return BindingBuilder.bind(newOrderQueue())
			.to(sharedConfiguration.mainExchange())
			.with(NEW_ORDER_ROUTING_KEY);
	}

	/**
	 * Binding the retry queue bean to the main exchange with a routing key.
	 */
	@Bean
	Binding newOrderRetryBinding() {
		return BindingBuilder.bind(newOrderRetryQueue())
			.to(sharedConfiguration.mainExchange())
			.with(NEW_ORDER_RETRY_ROUTING_KEY);
	}

	/**
	 * Binding the dead-letter queue bean to the dead-letter exchange. No need to use a routing key
	 * as the fanout exchange will send the message to all queues bound to it. If we had multiple
	 * queues in the exchange, we might need to use a routing key to specify which queue to send the
	 * message to.
	 */
	@Bean
	Binding newOrderDeadLetterBinding() {
		return BindingBuilder.bind(newOrderDeadLetterQueue())
			.to(sharedConfiguration.deadLetterExchange())
			.with(NEW_ORDER_DEAD_LETTER_ROUTING_KEY);
	}
}
