package test.springrabbitmq.fanout;

import java.util.Calendar;

import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JsonMessageConverter;

import test.springrabbitmq.AccountUser;

public class StandaloneSender {
	public static String EXCHANGE_NAME = "StandAloneExchange";
	public static String RABBITMQ_HOST = "local-rabbitmq03";
	public static String RABBITMQ_HOST_ADDRESSES = "local-rabbitmq03,local-rabbitmq04";

	public static String RABBITMQ_USERNAME = "admin";
	public static String RABBITMQ_PASSWORD = "admin";

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
        
		RabbitTemplate template = new RabbitTemplate(cf);
		template.setMessageConverter(new JsonMessageConverter());
		template.setExchange(StandaloneSender.EXCHANGE_NAME);
		AccountUser myAccount = new AccountUser("id_"
				+ Calendar.getInstance().getTime(), "First", "Last");

		template.convertAndSend(StandaloneSender.EXCHANGE_NAME, "", myAccount);
		System.out.println("[x] Message [" + myAccount.toString() + "] sent");
		cf.destroy();
	}
}
