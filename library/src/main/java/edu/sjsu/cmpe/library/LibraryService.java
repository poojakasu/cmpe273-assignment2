package edu.sjsu.cmpe.library;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yammer.dropwizard.Service;
import com.yammer.dropwizard.config.Bootstrap;
import com.yammer.dropwizard.config.Environment;
import com.yammer.dropwizard.views.ViewBundle;

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

import edu.sjsu.cmpe.library.api.resources.BookResource;
import edu.sjsu.cmpe.library.api.resources.RootResource;
import edu.sjsu.cmpe.library.config.LibraryServiceConfiguration;
import edu.sjsu.cmpe.library.repository.BookRepository;
import edu.sjsu.cmpe.library.repository.BookRepositoryInterface;
import edu.sjsu.cmpe.library.ui.resources.HomeResource;

public class LibraryService extends Service<LibraryServiceConfiguration> {

    private final Logger log = LoggerFactory.getLogger(getClass());
    private static Connection connection;
    private static MessageProducer producer;
    private static String instanceName = "";
   
    private static String user = env("APOLLO_USER", "admin");
    private static String password = env("APOLLO_PASSWORD", "password");
    private static String host = env("APOLLO_HOST", "54.215.210.214");
    private static int port = Integer.parseInt(env("APOLLO_PORT", "61613"));
    
    
    public static void main(String[] args) throws Exception {
	 
     new LibraryService().run(args);	
	 int numThreads = 1;
	 ExecutorService executor = Executors.newFixedThreadPool(numThreads);
	  
	 Runnable backgroundTask = new Runnable() {
	  
	 @Override
	 public void run() {
	 try{
		ListenToApolloTopic();
	 }
	 catch(JMSException e){
		 System.out.println("Exception thrown in the background thread " + e.getMessage());
	 }
	 }
	 };
	  
	 executor.execute(backgroundTask);
	 executor.shutdown();
		 
    }
    /* 
     * This is called from the Service abstract class.
     * @see com.yammer.dropwizard.Service#initialize(com.yammer.dropwizard.config.Bootstrap)
     * Use Async listener or Stomp Listener based on the topic. 
     * and update the book based  and if it doesnt exists then create a new book.
     */
    
    @Override
    public void initialize(Bootstrap<LibraryServiceConfiguration> bootstrap) {
	bootstrap.setName("library-service");
	bootstrap.addBundle(new ViewBundle());
    }
    
    public static void OrderForNewBook(Long lostisbn)throws JMSException{
    	
    	String queue = "/queue/05829.book.orders";
    	System.out.println("**********************************************************************");   	
	 	System.out.println("Library: "+ instanceName +" is ordering for following lost books through: " + queue); 
	 	
    	StompJmsConnectionFactory factory = new StompJmsConnectionFactory();
    	factory.setBrokerURI("tcp://" + host + ":" + port);
    	connection = factory.createConnection(user, password);
    	connection.start();
    	
    	Destination dest = new StompJmsDestination(queue);
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
       
        producer = session.createProducer(dest);
        producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
        
        String data = instanceName+":"+lostisbn.toString();
        TextMessage msg = session.createTextMessage(data);
        System.out.println("Ordering book: " + data);
        System.out.println("**********************************************************************");   	        
        producer.send(msg);
        
        producer.send(session.createTextMessage("SHUTDOWN"));
        connection.close();
    }
    
    @Override
    public void run(LibraryServiceConfiguration configuration,
	    Environment environment) throws Exception {
    
	// This is how you pull the configurations from library_x_config.yml
	String queueName = configuration.getStompQueueName();
	String topicName = configuration.getStompTopicName();
	instanceName = configuration.getDefaultName();
	
	log.debug("Queue name is {}. Topic name is {}", queueName,
		topicName);
    
    /** Root API */
	environment.addResource(RootResource.class);
	/** Books APIs */
	BookRepositoryInterface bookRepository = new BookRepository();
	environment.addResource(new BookResource(bookRepository));

	/** UI Resources */
	environment.addResource(new HomeResource(bookRepository));
    }
    
    public static void ListenToApolloTopic() throws JMSException{
    	
    	 String destination = "";
    	 if(instanceName != "" && instanceName.equalsIgnoreCase("library-a"))
    		 destination = "/topic/05829.book.*";
    	 else 
    		 destination = "/topic/05829.book.computer";
    	 
    	 System.out.println("**********************************************************************");   	
     	 System.out.println("Library: "+ instanceName +" connected to: " + destination);  
         StompJmsConnectionFactory factory = new StompJmsConnectionFactory();
         factory.setBrokerURI("tcp://" + host + ":" + port);

         Connection connection = factory.createConnection(user, password);
         connection.start();
         
         Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
         Destination dest = new StompJmsDestination(destination);

         MessageConsumer consumer = session.createConsumer(dest);
         System.out.println("Waiting for messages from " + destination + "...");
         
         while(true) {
          Message msg = consumer.receive(500);
          if(msg == null)
        	  continue;
          if( msg instanceof TextMessage ) {
                 String body = ((TextMessage) msg).getText();                 
                 if( "SHUTDOWN".equals(body)) {
                  continue;
                 }
                 BookRepository.UpdateBookDetails(body);
                 System.out.println("Received message = " + body);

          } else if (msg instanceof StompJmsMessage) {
                 StompJmsMessage smsg = ((StompJmsMessage) msg);
                 String body = smsg.getFrame().contentAsString();                 
                 if ("SHUTDOWN".equals(body)) {
                	 continue;
                 }
                 BookRepository.UpdateBookDetails(body);
                 System.out.println("Received message = " + body);

          } else {
                 System.out.println("Unexpected message type: "+ msg.getClass());
                 continue;
          }
         }
         //connection.close();
    }
    private static String env(String key, String defaultValue) {
        String rc = System.getenv(key);
        if( rc== null ) {
         return defaultValue;
        }
        return rc;
    }
}
