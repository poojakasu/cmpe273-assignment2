package edu.sjsu.cmpe.library.dto;

import java.util.List;
import java.util.ArrayList;

public class InputFrom {
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
	public void print()
	{
		System.out.println("They are from post: ");
		for(int i=0;i<order_book_isbns.size();++i){
			System.out.println(order_book_isbns.get(i));
		}
	}
	
}
