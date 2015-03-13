package test.rabbitmq;

import java.io.IOException;

import com.rabbitmq.client.Address;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;

/**
 * Use rabbitmq client api to receive simple message
 * @author mnguyen
 *
 */
public class Recv {
	public static void main(String[] args) 
			throws IOException, InterruptedException{
		
		int maxConsumeCount = 1;
		if (args.length > 0) {
			maxConsumeCount = Integer.parseInt(args[0]);
		}
		System.out.println("maxConsumeCount = " + maxConsumeCount);
		
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost("local-rabbitmq04");
	    factory.setUsername("admin");
	    factory.setPassword("admin");

		Address[] addrAry = new Address[] { 
				new Address("local-rabbitmq03"), 
				new Address("local-rabbitmq04")
				};
		Connection connection = factory.newConnection(addrAry);		
	    Channel channel = connection.createChannel();

	    channel.queueDeclare(Send.QUEUE_NAME, false, false, false, null);
	    System.out.println(" [*] Waiting for messages. To exit press CTRL+C");		
	    QueueingConsumer consumer = new QueueingConsumer(channel);
	    channel.basicConsume(Send.QUEUE_NAME, true, consumer);

	    int remainingCount = maxConsumeCount;
	    while (remainingCount > 0) {
	      QueueingConsumer.Delivery delivery = consumer.nextDelivery();
	      String message = new String(delivery.getBody());
	      System.out.println(" [x] Received '" + message + "'");
	      remainingCount--;
	    }
	    
	    channel.close();
	    connection.close();
	}

}
