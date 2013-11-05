package edu.sjsu.cmpe.procurement;


import java.util.HashMap;
import java.util.List;

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.fusesource.stomp.jms.StompJmsConnectionFactory;
import org.fusesource.stomp.jms.StompJmsDestination;
import org.fusesource.stomp.jms.message.StompJmsMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jersey.api.client.Client;
import com.yammer.dropwizard.Service;
import com.yammer.dropwizard.client.JerseyClientBuilder;
import com.yammer.dropwizard.config.Bootstrap;
import com.yammer.dropwizard.config.Environment;

import edu.sjsu.cmpe.procurement.api.resources.HttpConnectorPublisher;
import edu.sjsu.cmpe.procurement.api.resources.ProcurementResource;
import edu.sjsu.cmpe.procurement.config.ProcurementServiceConfiguration;
import edu.sjsu.cmpe.procurement.domain.Book;
import edu.sjsu.cmpe.procurement.jobs.JobBundle;
import edu.sjsu.cmpe.procurement.parser.CustomUtil;

public class ProcurementService extends Service<ProcurementServiceConfiguration> {

    private final Logger log = LoggerFactory.getLogger(getClass());
    
    private static Connection connection;
    private static MessageConsumer consumer;
    private static HashMap<Integer,String> orderList = new HashMap<Integer,String>();
    private static String user = env("APOLLO_USER", "admin");
    private static String password = env("APOLLO_PASSWORD", "password");
    private static String host = env("APOLLO_HOST", "54.215.210.214");
    private static int port = Integer.parseInt(env("APOLLO_PORT", "61613"));
    
    public static Client jerseyClient;     
    
    public static void main(String[] args) throws Exception {
	new ProcurementService().run(args);
    }
/*
 Publish to the queue called /topic/05829.books.cat 3 books send 3 times for each book in the topic queue
 */
    @Override
    public void initialize(Bootstrap<ProcurementServiceConfiguration> bootstrap) {
	bootstrap.setName("procurement-service");
	bootstrap.addBundle(new JobBundle("edu.sjsu.cmpe.procurement.jobs"));
    }
    
    public static void retrieveFromtheQueue() throws JMSException {    	
    	
    	orderList = new HashMap<Integer,String>();
    	String queue = "/queue/05829.book.orders";
    	Integer counter =0;
    	try{
    	StompJmsConnectionFactory factory = new StompJmsConnectionFactory();
        factory.setBrokerURI("tcp://" + host + ":" + port);
        connection = factory.createConnection(user, password);             
        connection.start();
        
    	System.out.println("****************************************************");   	
    	System.out.println("Connected to Apollo Broker :" + queue);   	
    	
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Destination dest = new StompJmsDestination(queue);

        consumer = session.createConsumer(dest);
        System.out.println("Waiting for messages from " + queue + "...");
        
        while(true) {
         Message msg = consumer.receive(500);
         if(msg == null)
        	 break;
         if( msg instanceof TextMessage ) {
                String body = ((TextMessage) msg).getText();
                if( "SHUTDOWN".equals(body)) {                	
                	continue;
                }
                System.out.println("Received Text message = " + body);
                orderList.put(counter,body);
                counter+=1;

         } else if (msg instanceof StompJmsMessage) {
                StompJmsMessage smsg = ((StompJmsMessage) msg);
                String body = smsg.getFrame().contentAsString();
                if ("SHUTDOWN".equals(body)) {                	
                	continue;                
                }
                System.out.println("Received Stomp jms message = " + body);

         } else {
                System.out.println("Unexpected message type: "+ msg.getClass());
                break;
         }
        }
        if(orderList.size() > 0)
        	HttpConnectorPublisher.prepareDataPublish(orderList);
        connection.close();
        System.out.println("*********Closed Connection to Apollo Broker*********");
    	}
    	catch(Exception e){
    		connection.close();
    		System.out.println("Exception in ProcurementService when Listening to Apollo Broker, Exception: "+e.getMessage());
    	}
        
    }

    @Override    
    public void run(ProcurementServiceConfiguration configuration,
	    Environment environment) throws Exception { 
    	
    	jerseyClient = new JerseyClientBuilder().using(configuration.getJerseyClientConfiguration()).using(environment).build();
    	
    	 /** Root API */
     	environment.addResource(ProcurementResource.class);
     	
        String queueName = configuration.getStompQueueName();
        String topicName = configuration.getStompTopicName();
        
        log.debug("Queue name is {}. Topic is {}", queueName, topicName);
     	
    }
    
    public static void PublisherToTopic(List<Book> response) throws JMSException{
    	
    	 System.out.println("Publishing the Books to Library through Topic");
         StompJmsConnectionFactory factory = new StompJmsConnectionFactory();
         factory.setBrokerURI("tcp://" + host + ":" + port);

         connection = factory.createConnection(user, password);
         connection.start();
         
         Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
         String topic="";
         for(int i=0;i<response.size();i++){
        	 if(response.get(i).getCategory() != null && response.get(i).getCategory() != "")
        		 topic = "/topic/05829.book."+response.get(i).getCategory();
        	 else
        		 topic = "/topic/05829.book.computer";
        	 
	         /*if(response.get(i).getCategory().equalsIgnoreCase("computer"))
	        	 topic = "/topic/05829.book.computer";
	         else if(response.get(i).getCategory().equalsIgnoreCase("management"))
	        	 topic = "/topic/05829.book.management";
	         else if(response.get(i).getCategory().equalsIgnoreCase("comics"))
	        	 topic = "/topic/05829.book.comics";
	         else if(response.get(i).getCategory().equalsIgnoreCase("selfimprovement"))
	        	 topic = "/topic/05829.book.selfimprovement";*/
	         
	         Destination dest = new StompJmsDestination(topic);
	         
	         MessageProducer producer = session.createProducer(dest);
	         producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
	         
	         String data = CustomUtil.ConvertToFormat(response.get(i));
	         System.out.println("Sending : " + data);
	         TextMessage msg = session.createTextMessage(data);
	         
	         producer.send(msg);
	         producer.send(session.createTextMessage("SHUTDOWN"));
         }
         
         connection.close();
    	
    }
    
    private static String env(String key, String defaultValue) {
        String rc = System.getenv(key);
        if( rc== null ) {
         return defaultValue;
        }
        return rc;
    }
}
