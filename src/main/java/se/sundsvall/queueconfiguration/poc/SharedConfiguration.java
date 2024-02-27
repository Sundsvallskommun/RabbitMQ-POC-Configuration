package se.sundsvall.queueconfiguration.poc;

import org.springframework.amqp.core.DirectExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class SharedConfiguration {

	/**
	 * Exchanges
	 */
	public static final String POC_EXCHANGE = "poc-exchange";
	public static final String POC_DLX = "poc-dlx";

	@Bean
	DirectExchange mainExchange() {
		return new DirectExchange(POC_EXCHANGE);
	}

	@Bean
	DirectExchange deadLetterExchange() {
		return new DirectExchange(POC_DLX);
	}

}
