package edu.sjsu.cmpe.procurement.domain;

import java.util.ArrayList;
import java.util.List;

public class InputToPublisher {
	private String id;
	private List<Integer> order_book_isbns = new ArrayList<Integer>();
	
	public List<Integer> getOrder_book_isbns() {
		return order_book_isbns;
	}
	public void setOrder_book_isbns(List<Integer> order_book_isbns) {
		this.order_book_isbns = order_book_isbns;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	} 
	
}
