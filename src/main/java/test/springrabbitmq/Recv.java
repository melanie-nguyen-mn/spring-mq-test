package test.springrabbitmq;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.GenericXmlApplicationContext;

public class Recv {

	public static void main(String[] args) throws Exception {
		System.out.println("Recv starts...");
		AbstractApplicationContext context =
			    new GenericXmlApplicationContext("classpath:/rabbit-send-context.xml");
	    RabbitTemplate template = context.getBean(RabbitTemplate.class);
	    String message = (String) template.receiveAndConvert("myQueue");
	    System.out.println(" [x] Received '" + message + "'");
		Thread.sleep(1000);
	    context.close();
	}

}
