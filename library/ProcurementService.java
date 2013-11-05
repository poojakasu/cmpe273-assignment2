package edu.sjsu.cmpe.procurement;

import java.awt.List;
import java.util.HashMap;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.fusesource.stomp.jms.StompJmsConnectionFactory;
import org.fusesource.stomp.jms.StompJmsDestination;
import org.fusesource.stomp.jms.message.StompJmsMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yammer.dropwizard.Service;
import com.yammer.dropwizard.config.Bootstrap;
import com.yammer.dropwizard.config.Environment;
//import com.yammer.dropwizard.j








import edu.sjsu.cmpe.procurement.api.resources.HttpConnectorPublisher;
import edu.sjsu.cmpe.procurement.api.resources.ProcurementResource;
import edu.sjsu.cmpe.procurement.config.ProcurementServiceConfiguration;
import edu.sjsu.cmpe.procurement.jobs.JobBundle;

public class ProcurementService extends Service<ProcurementServiceConfiguration> {

    private final Logger log = LoggerFactory.getLogger(getClass());
    private static Connection connection;
    private static MessageConsumer consumer;
    private static HashMap<Integer,String> orderList = new HashMap<Integer,String>();
    
    public static void main(String[] args) throws Exception {
	new ProcurementService().run(args);
    }

    @Override
    public void initialize(Bootstrap<ProcurementServiceConfiguration> bootstrap) {
	bootstrap.setName("procurement-service");
	bootstrap.addBundle(new JobBundle("edu.sjsu.cmpe.procurement.jobs"));
    }
    
    public static void retrieveFromtheQueue() throws JMSException
    {
    	String queue = "/queue/05829.book.orders";
    	System.out.println("Connected to retrieveFromtheQueue");   	
        connection.start();
        Integer counter =0;
        System.out.println("Connected to queue and listening...");
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
        HttpConnectorPublisher.prepareDataPublish(orderList);
        connection.close();
        System.out.println("DisConnected to queue and listening...");
    }

    @Override    
    public void run(ProcurementServiceConfiguration configuration,
	    Environment environment) throws Exception {
    	
    	 String user = env("APOLLO_USER", "admin");
         String password = env("APOLLO_PASSWORD", "password");
         String host = env("APOLLO_HOST", "54.215.210.214");
         int port = Integer.parseInt(env("APOLLO_PORT", "61613"));
                  

         StompJmsConnectionFactory factory = new StompJmsConnectionFactory();
         factory.setBrokerURI("tcp://" + host + ":" + port);
         connection = factory.createConnection(user, password);  
         
         /** Root API */
     	environment.addResource(ProcurementResource.class);
     	
    }
    
    private static String env(String key, String defaultValue) {
        String rc = System.getenv(key);
        if( rc== null ) {
         return defaultValue;
        }
        return rc;
    }
}
