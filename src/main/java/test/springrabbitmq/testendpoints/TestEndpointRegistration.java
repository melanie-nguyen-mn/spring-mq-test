package test.springrabbitmq.testendpoints;

import java.util.Calendar;
import java.util.Random;

import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.AmqpIOException;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.annotation.RabbitListenerConfigurer;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerEndpoint;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistrar;
import org.springframework.amqp.support.converter.JsonMessageConverter;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.RecoveryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.support.RetryTemplate;

import test.springrabbitmq.AccountUser;
import test.springrabbitmq.fanout.StandaloneReceiver;
import test.springrabbitmq.fanout.StandaloneSender;


public class TestEndpointRegistration {
	static final String THIS_EXCHANGE = StandaloneSender.EXCHANGE_NAME;
	static final String THIS_QUEUE = StandaloneReceiver.QUEUE_NAME;
	static final String THIS_HOST = StandaloneSender.RABBITMQ_HOST;
	static final String THIS_USERNAME = StandaloneSender.RABBITMQ_USERNAME;
	static final String THIS_PASSWORD = StandaloneSender.RABBITMQ_PASSWORD;
	
	public static class MyService {
		private ObjectMapper objectMapper = new ObjectMapper();
		
		public void processMessage(byte[] message) {
	        try {
	        	AccountUser au = objectMapper.readValue(message, AccountUser.class);
	            System.out.println("### [threadId-" + Thread.currentThread().getId() + "] MyService.processMessage(AccountUser accountUser) :  " + au.toString());
	        } catch (Exception e) {
	        	System.out.println("### ERROR [threadId-" + Thread.currentThread().getId() + "] Cannot convert");
	        	e.printStackTrace();
	        }
		}
		
		public void processAccountUser(AccountUser au) {
            System.out.println("### [threadId-" + Thread.currentThread().getId() + "] MyService.processAccountUser(AccountUser accountUser) :  " + au.toString());
		}
		
	}
	
	@Configuration
	@EnableRabbit
	public static class AppConfig implements RabbitListenerConfigurer {
	    @Bean
	    public ConnectionFactory connectionFactory() {
	        CachingConnectionFactory connectionFactory = new CachingConnectionFactory(THIS_HOST);
	        connectionFactory.setUsername(THIS_USERNAME);
	        connectionFactory.setPassword(THIS_PASSWORD);
	        connectionFactory.setChannelCacheSize(10);
	        connectionFactory.setAddresses(THIS_HOST);

	        return connectionFactory;
	    }
	    
	    @Bean 
	    public JsonMessageConverter jsonMessageConverter() {
	    	return new JsonMessageConverter();
	    }

	    @Bean
	    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory() {
	        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
	        factory.setConnectionFactory(connectionFactory());
	        factory.setConcurrentConsumers(3);
	        factory.setMaxConcurrentConsumers(10);
	        factory.setMessageConverter(jsonMessageConverter());
	        //factory.setAutoStartup(false);
	        return factory;
	    }

	    
	    public void configureRabbitListeners(RabbitListenerEndpointRegistrar registrar) {
	        SimpleRabbitListenerEndpoint endpoint = new SimpleRabbitListenerEndpoint();
	        endpoint.setId(MyService.class.getSimpleName());
	        endpoint.setQueueNames(THIS_QUEUE);
	        endpoint.setMessageListener(new MessageListener() {
		    	final MyService myService = new MyService();
		    	final JsonMessageConverter messageConverter = jsonMessageConverter();

				public void onMessage(Message arg0) {
					AccountUser au = (AccountUser) messageConverter.fromMessage(arg0);
					myService.processAccountUser(au);
				}
	        	
	        });
	        registrar.registerEndpoint(endpoint);
	        
	    }		

	}
    /*
    public void configureRabbitListeners_v1(RabbitListenerEndpointRegistrar registrar) {
        SimpleRabbitListenerEndpoint endpoint = new SimpleRabbitListenerEndpoint();
        endpoint.setId(MyService.class.getSimpleName());
        endpoint.setQueueNames(THIS_QUEUE);
        endpoint.setMessageListener(new MessageListener() {
	    	final MyService myService = new MyService();

			public void onMessage(Message arg0) {
				// TODO Auto-generated method stub
				myService.processMessage(arg0.getBody());
			}
        	
        });
        registrar.registerEndpoint(endpoint);
    }
    */
	
	
    public static void main(String[] args) throws Exception {
        System.out.println("### TestEndpointRegistration: BEGIN");
        
        System.out.println("### TestEndpointRegistration[threadId-" + Thread.currentThread().getId() + "]: publishing...");
        long startTime = Calendar.getInstance().getTimeInMillis();
        try {
	        CachingConnectionFactory connectionFactory = new CachingConnectionFactory(THIS_HOST);
	        connectionFactory.setUsername(THIS_USERNAME);
	        connectionFactory.setPassword(THIS_PASSWORD);
	        connectionFactory.setChannelCacheSize(10);
	        //connectionFactory.setAddresses("local-rabbitmq03,local-rabbitmq04");
	        
	        // binding
			RabbitAdmin rabbitAdmin = new RabbitAdmin(connectionFactory);
			Queue thisQueue = new Queue(THIS_QUEUE);
			FanoutExchange thisExchange = new FanoutExchange(THIS_EXCHANGE, true, false);
			rabbitAdmin.declareQueue(thisQueue);
			rabbitAdmin.declareExchange(thisExchange);
			rabbitAdmin.declareBinding(BindingBuilder.bind(thisQueue).to(thisExchange));
	        
			
			// Test publishing with RetryTemplate
	        System.out.println("### TestEndpointRegistration: test publishing with RetryTemplate");
	 	    RabbitTemplate template = new RabbitTemplate( connectionFactory);
	 	    RetryTemplate retryTemplate = new RetryTemplate();
	 	    ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
	 	    backOffPolicy.setMultiplier(2);
	 	    backOffPolicy.setMaxInterval(5000);
	 	    backOffPolicy.setInitialInterval(500);
	 	    retryTemplate.setBackOffPolicy(backOffPolicy);
	 	    template.setRetryTemplate(retryTemplate);
	 	    template.setRecoveryCallback(new RecoveryCallback<Object>() {

				public Object recover(RetryContext arg0) throws Exception {
					System.out.println("### my RecoveryCallback... ");
					return null;
				}
	 	    	
	 	    });
	 	    template.setMessageConverter(new JsonMessageConverter());
	 	    template.setExchange(THIS_EXCHANGE);
	 	    
	 	    // publishing 
	 	    for (int i=0; i < 6; i++) {
	 		    Random r = new Random();
	 		    int id = r.nextInt(100);
	 		    AccountUser myAccount = new AccountUser("id_" + id, "First-"+id, "Last");
	 		    template.convertAndSend(myAccount);
	 		    System.out.println("### [x] Sent '" + myAccount.toString() + "'");
	 	    }
       	
        } catch (AmqpIOException  amqpException) {
            long totalTime = Calendar.getInstance().getTimeInMillis() - startTime;           
       	System.out.println("### Amqp IO Exception  in " + totalTime + " ms");
        	amqpException.printStackTrace();
        } catch (AmqpException amqpIOException) {
            long totalTime = Calendar.getInstance().getTimeInMillis() - startTime;           
        	System.out.println("### AmqpException in " + totalTime + " ms");
        	amqpIOException.printStackTrace();
        	
        } catch (Exception e) {
            long totalTime = Calendar.getInstance().getTimeInMillis() - startTime;           
       	System.out.println("### AmqpIOException  in " + totalTime + " ms");
        	e.printStackTrace();
        }

	    
        System.out.println("### TestEndpointRegistration: rest for 500 ms and then start listening...");
 	    Thread.sleep(500);
 	    
        ApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
        System.out.println("### TestEndpointRegistration: here");
       
       
    }

    
}
