package test.springrabbitmq.fanout;

import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JsonMessageConverter;

import test.springrabbitmq.AccountUser;

public class StandaloneReceiver {
	public static String QUEUE_NAME = "StandaloneQueue";

	public static void main(String[] args) throws Exception {
		/*
		CachingConnectionFactory cf = new CachingConnectionFactory(
				StandaloneSender.RABBITMQ_HOST);
		cf.setUsername(StandaloneSender.RABBITMQ_USERNAME);
		cf.setPassword(StandaloneSender.RABBITMQ_PASSWORD);
		*/
		
        CachingConnectionFactory cf = new CachingConnectionFactory(StandaloneSender.RABBITMQ_HOST);
        cf.setUsername(StandaloneSender.RABBITMQ_USERNAME);
        cf.setPassword(StandaloneSender.RABBITMQ_PASSWORD);
        cf.setAddresses(StandaloneSender.RABBITMQ_HOST_ADDRESSES);
		
		RabbitAdmin rabbitAdmin = new RabbitAdmin(cf);
		Queue thisQueue = new Queue(QUEUE_NAME);
		FanoutExchange thisExchange = new FanoutExchange(
				StandaloneSender.EXCHANGE_NAME, true, false);
		rabbitAdmin.declareQueue(thisQueue);
		rabbitAdmin.declareExchange(thisExchange);
		rabbitAdmin.declareBinding(BindingBuilder.bind(thisQueue).to(
				thisExchange));

		RabbitTemplate template = rabbitAdmin.getRabbitTemplate();
		template.setMessageConverter(new JsonMessageConverter());
		AccountUser myAccount = (AccountUser) template
				.receiveAndConvert(QUEUE_NAME);
		System.out.println("[x] Received => " + myAccount);
		cf.destroy();
	}

}
