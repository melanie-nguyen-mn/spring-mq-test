package test.rabbitmq;

import java.io.IOException;
import java.util.Calendar;

import com.rabbitmq.client.Address;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

/**
 * Use rabbitmq client API to send message to a queue
 * @author mnguyen
 *
 */
public class Send {
	public final static String QUEUE_NAME = "test_queue_1";
	
	public static void main(String[] args) throws IOException {
		ConnectionFactory factory = new ConnectionFactory();
	    factory.setHost("local-rabbitmq03");
	    factory.setUsername("admin");
	    factory.setPassword("admin");
		Address[] addrAry = new Address[] { 
				new Address("local-rabbitmq03"), 
				new Address("local-rabbitmq04")
				};
		Connection connection = factory.newConnection(addrAry);		
	    Channel channel = connection.createChannel();

	    channel.queueDeclare(QUEUE_NAME, false, false, false, null);
	    
	    String message = "Hello World! " + Calendar.getInstance().getTime().toString();
	    channel.basicPublish("", QUEUE_NAME, null, message.getBytes());
	    System.out.println(" [x] Sent '" + message + "'");
	    channel.close();
	    connection.close();
	}
}
