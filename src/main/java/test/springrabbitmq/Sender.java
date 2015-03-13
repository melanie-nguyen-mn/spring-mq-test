package test.springrabbitmq;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.GenericXmlApplicationContext;

public class Sender {
	public static void main(String[] args) throws Exception {
		AbstractApplicationContext context =
			    new GenericXmlApplicationContext("classpath:/rabbit-send-context.xml");
	    RabbitTemplate template = context.getBean(RabbitTemplate.class);
	    String message = args.length < 1 ? "Hello World!" : args[0];
	    template.convertAndSend(message);
	    System.out.println(" [x] Sent '" + message + "'");
	    Thread.sleep(1000);
	    context.close();
	}
}
