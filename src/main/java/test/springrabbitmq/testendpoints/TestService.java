package test.springrabbitmq.testendpoints;

import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JsonMessageConverter;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import test.springrabbitmq.AccountUser;
import test.springrabbitmq.fanout.StandaloneSender;


public class TestService {
	private ObjectMapper objectMapper = new ObjectMapper();
	
	@RabbitListener(queues = "StandaloneQueue")
	public void process(AccountUser au) {
        System.out.println("### [threadId-" + Thread.currentThread().getId() + "] TestService.processMessage(AccountUser accountUser) :  " + au.toString());
		
	}
	
	public void processMessage(byte[] message) {
        System.out.println("### [threadId-" + Thread.currentThread().getId() + "] TestService.processMessage(String msg):  " + message);
        try {
        	AccountUser au = objectMapper.readValue(message, AccountUser.class);
            System.out.println("### [threadId-" + Thread.currentThread().getId() + "] TestService.processMessage(AccountUser accountUser) :  " + au.toString());
        } catch (Exception e) {
        	System.out.println("Cannot convert");
        	e.printStackTrace();
        }
	}
	
	
	@Configuration
	@EnableRabbit
	public static class AppConfig {
		@Bean
		public JsonMessageConverter jsonMessageConverter() {
			return new JsonMessageConverter();			
		}
		
		@Bean
		public RabbitTemplate amqpTemplate() {
			return new RabbitTemplate(connectionFactory());
		}
		
	    @Bean
	    public ConnectionFactory connectionFactory() {
	        CachingConnectionFactory connectionFactory = new CachingConnectionFactory(StandaloneSender.RABBITMQ_HOST);
	        connectionFactory.setUsername(StandaloneSender.RABBITMQ_USERNAME);
	        connectionFactory.setPassword(StandaloneSender.RABBITMQ_USERNAME);
	        connectionFactory.setChannelCacheSize(10);

	        return connectionFactory;
	    }
	    
	    @Bean
	    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory() {
	        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
	        factory.setConnectionFactory(connectionFactory());
	        factory.setConcurrentConsumers(3);
	        factory.setMaxConcurrentConsumers(10);
	        factory.setMessageConverter(jsonMessageConverter());
	        return factory;
	    }
	    
	    @Bean
	    public TestService testService() {
	    	return new TestService();
	    }
	    
	}
    
    public static void main(String[] args) {
        System.out.println("### TestService: START");
        ApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
        System.out.println("### TestService: here");
       
    }

}
