package edu.sjsu.cmpe.procurement.api.resources;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import edu.sjsu.cmpe.procurement.ProcurementService;
import edu.sjsu.cmpe.procurement.domain.Book;
import edu.sjsu.cmpe.procurement.domain.InputToPublisher;
import edu.sjsu.cmpe.procurement.domain.shipped_books;
import edu.sjsu.cmpe.procurement.parser.CustomUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

 
public class HttpConnectorPublisher{
 
	public static void prepareDataPublish(HashMap<Integer,String> orderList){
		
		try{
			
		InputToPublisher input = new InputToPublisher();
		input.setId("05829");
		List<Integer> lst = new ArrayList<Integer>();
		for(int i=0;i<orderList.size();++i){
			String st = orderList.get(i);
			if(st!=null && st.contains(":"))
				st = st.split(":")[1];
			else
				st="0";
			lst.add(Integer.parseInt(st));
		}
		input.setOrder_book_isbns(lst);
		
		ObjectMapper jacktojson = new ObjectMapper();
		String s = jacktojson.writeValueAsString(input);
		
		Client client =  Client.create();
		WebResource webResource = client.resource("http://54.219.156.168:9000/orders");
		ClientResponse response = webResource.type("application/json").post(ClientResponse.class, s);
		
		if (response.getStatus() != 200) {
			throw new RuntimeException
			("Couldnt Post the order to http://54.219.156.168:9000/orders" + response.getStatus());
		}
		else
			System.out.println(response.toString());
		
		ProcurementService.PublisherToTopic(ReceiveGetFromPubliser()); 
		}
		catch(Exception e)
		{
		System.out.println("Exception at sending POST: "+e.getMessage());
		}
	}
  
	// HTTP GET request
	private static List<Book> ReceiveGetFromPubliser() throws Exception {

		String url = "http://54.219.156.168:9000/orders/05829";
		shipped_books book = null;
		try{
		Client client = Client.create();
		WebResource webResource = client.resource(url);
 
		ClientResponse response = webResource.accept("application/json").get(ClientResponse.class);
 
		if (response.getStatus() != 200) {
		   throw new RuntimeException("No Books received from Publisher as got status: "+response.getStatus());
		}
			
 		String output = response.getEntity(String.class);
 		try {
			ObjectMapper jsontojackson = new ObjectMapper();
			book = jsontojackson.readValue(output,shipped_books.class);
		}
		catch(Exception e)	{
			System.out.println("Error While parsing the jsontojackson in receiveget" + e.getMessage());
		}
		}
		catch(Exception e){
			System.out.println("Exception in ReceiveGetFromPubliser");
		}
		return book.getShipped_books();
		
		
	}
 
}

