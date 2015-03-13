package test.springrabbitmq.fanout;

import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.amqp.support.converter.JsonMessageConverter;

import test.springrabbitmq.AccountUser;

public class StandaloneListener {
	public static void main(String[] args) throws Exception {

		CachingConnectionFactory cf = new CachingConnectionFactory(
				StandaloneSender.RABBITMQ_HOST);
		cf.setUsername(StandaloneSender.RABBITMQ_USERNAME);
		cf.setPassword(StandaloneSender.RABBITMQ_PASSWORD);
		RabbitAdmin rabbitAdmin = new RabbitAdmin(cf);
		Queue thisQueue = new Queue(StandaloneReceiver.QUEUE_NAME);
		FanoutExchange thisExchange = new FanoutExchange(
				StandaloneSender.EXCHANGE_NAME, true, false);
		rabbitAdmin.declareQueue(thisQueue);
		rabbitAdmin.declareExchange(thisExchange);
		rabbitAdmin.declareBinding(BindingBuilder.bind(thisQueue).to(
				thisExchange));

		// setup listener & container
		final SimpleMessageListenerContainer container = new SimpleMessageListenerContainer(cf);
		Object listener = new Object() {
			public void handleMessage(AccountUser accountUser) {
				System.out.println("[Received] " + accountUser.toString());
			}
		};
		MessageListenerAdapter adapter = new MessageListenerAdapter(listener);
		adapter.setMessageConverter(new JsonMessageConverter());
		adapter.setDelegate(listener);
		container.setMessageListener(adapter);
		container.setQueueNames(StandaloneReceiver.QUEUE_NAME);

		container.start();
		Thread.sleep(1000);
		container.stop();
		cf.destroy();
		System.out.println("END");
	}

}
