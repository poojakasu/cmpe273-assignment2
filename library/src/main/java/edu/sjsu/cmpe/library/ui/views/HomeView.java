package edu.sjsu.cmpe.library.ui.views;

import java.util.List;

import com.yammer.dropwizard.views.View;

import edu.sjsu.cmpe.library.domain.Book;

public class HomeView extends View {
	private final List<Book> books;
	private final String instance;
	
    public HomeView(List<Book> books,String instances) {
              super("home.mustache");
              this.books = books;
              this.instance = instances;
    }

    public List<Book> getBooks() {
              return books;
    }
    public String getInstance() {
        return instance;
}
}
