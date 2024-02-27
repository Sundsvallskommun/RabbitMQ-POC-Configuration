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
class CancelOrderConfiguration {

	/**
	 * Queues
	 */
	private static final String CANCEL_ORDER_QUEUE = "cancelOrderQueue";
	private static final String CANCEL_ORDER_RETRY_QUEUE = CANCEL_ORDER_QUEUE + ".retry";
	private static final String CANCEL_ORDER_DEAD_LETTER_QUEUE = CANCEL_ORDER_QUEUE + ".dlq";

	/**
	 * Routing keys
	 */
	private static final String CANCEL_ORDER_ROUTING_KEY = "RK_" + CANCEL_ORDER_QUEUE;
	private static final String CANCEL_ORDER_RETRY_ROUTING_KEY = CANCEL_ORDER_ROUTING_KEY + ".retry";
	private static final String CANCEL_ORDER_DEAD_LETTER_ROUTING_KEY = "RK_" + CANCEL_ORDER_QUEUE + ".dlq";

	private final SharedConfiguration sharedConfiguration;

	CancelOrderConfiguration(SharedConfiguration sharedConfiguration) {
		this.sharedConfiguration = sharedConfiguration;
	}

	@Bean
	public Queue cancelOrderQueue() {
		return QueueBuilder.durable(CANCEL_ORDER_QUEUE)
			.deadLetterExchange(POC_DLX)
			.deadLetterRoutingKey(CANCEL_ORDER_DEAD_LETTER_QUEUE)
			.quorum()
			.build();
	}

	@Bean
	public Queue cancelOrderRetryQueue() {
		return QueueBuilder.durable(CANCEL_ORDER_RETRY_QUEUE)
			.deadLetterExchange(POC_EXCHANGE)
			.deadLetterRoutingKey(CANCEL_ORDER_ROUTING_KEY)
			.ttl(5000)
			.quorum()
			.build();
	}

	@Bean
	public Queue cancelOrderDeadLetterQueue() {
		return QueueBuilder.durable(CANCEL_ORDER_DEAD_LETTER_QUEUE)
			.quorum()
			.build();
	}

	@Bean
	Binding cancelOrderBinding() {
		return BindingBuilder.bind(cancelOrderQueue())
			.to(sharedConfiguration.mainExchange())
			.with(CANCEL_ORDER_ROUTING_KEY);
	}

	@Bean
	Binding cancelOrderRetryBinding() {
		return BindingBuilder.bind(cancelOrderRetryQueue())
			.to(sharedConfiguration.mainExchange())
			.with(CANCEL_ORDER_RETRY_ROUTING_KEY);
	}

	@Bean
	Binding cancelOrderDeadLetterBinding() {
		return BindingBuilder.bind(cancelOrderDeadLetterQueue())
			.to(sharedConfiguration.deadLetterExchange())
			.with(CANCEL_ORDER_DEAD_LETTER_ROUTING_KEY);
	}

}
