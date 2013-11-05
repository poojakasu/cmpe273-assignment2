package edu.sjsu.cmpe.library.repository;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import edu.sjsu.cmpe.library.domain.Book;
import edu.sjsu.cmpe.library.domain.Book.Status;

public class BookRepository implements BookRepositoryInterface {
    /** In-memory map to store books. (Key, Value) -> (ISBN, Book) */
    private static ConcurrentHashMap<Long, Book> bookInMemoryMap;

    /** Never access this key directly; instead use generateISBNKey() */
    private long isbnKey;

    public BookRepository() 
    {
    	bookInMemoryMap = seedData();
    	isbnKey = 0;
    }
    
    public static void UpdateBookDetails(String input){
    	try{
    		String[] tempBookDetails = input.split(":");
    		Book tempBook;
    		Long isbn = Long.parseLong("0");
    		if(tempBookDetails.length >0){
    			if(tempBookDetails[0] != null)
    				isbn = Long.parseLong(tempBookDetails[0]);
    			if(bookInMemoryMap.containsKey(isbn)){
    				tempBook = bookInMemoryMap.get(isbn);
    				if(tempBook != null){
    					tempBook.setStatus(Status.available);
    				}
    			}
    			else{//its a new book
    				String title = (tempBookDetails[1] != null)?tempBookDetails[1]:"";
    				String category = (tempBookDetails[2] != null)?tempBookDetails[2]:"";
    				String uri = (tempBookDetails[3] != null && tempBookDetails[4] != null)?tempBookDetails[3]+":"+tempBookDetails[4]:"";
    				URI tempUri = new URI(uri);
    				URL tempUrl = tempUri.toURL();
    				tempBook = new Book();
    				tempBook.setIsbn(isbn);
    				tempBook.setTitle(title);
    				tempBook.setCategory(category);
    				tempBook.setCoverimage(tempUrl);
    				tempBook.setStatus(Status.available);
    				bookInMemoryMap.putIfAbsent(isbn, tempBook);
    				System.out.println("A new book has been added.");
    			}
    				
    		}
    	}
    	catch(Exception e){
    		System.out.println("Exception when book was updated in the library, Exception: " + e.getMessage());
    	}
    }

    private ConcurrentHashMap<Long, Book> seedData()
    {	
    	ConcurrentHashMap<Long, Book> bookMap = new ConcurrentHashMap<Long, Book>();
		Book book = new Book();
		book.setIsbn(1);
		book.setCategory("computer");
		book.setTitle("Java Concurrency in Practice");
		try {
		    book.setCoverimage(new URL("http://goo.gl/N96GJN"));
		} catch (MalformedURLException e) {
		    // eat the exception
		}
		bookMap.put(book.getIsbn(), book);
	
		book = new Book();
		book.setIsbn(2);
		book.setCategory("computer");
		book.setTitle("Restful Web Services");
		try {
		    book.setCoverimage(new URL("http://goo.gl/ZGmzoJ"));
		} catch (MalformedURLException e) {
		    // eat the exception
		}
		bookMap.put(book.getIsbn(), book);
		
		/*book = new Book();
		book.setIsbn(3);
		book.setCategory("comic");
		book.setTitle("Iron Man");
		try {
		    book.setCoverimage(new URL("http://goo.gl/N96GJN"));
		} catch (MalformedURLException e) {
		    // eat the exception
		}
		bookMap.put(book.getIsbn(), book);
		
		book = new Book();
		book.setIsbn(4);
		book.setCategory("management");
		book.setTitle("SQA");
		try {
		    book.setCoverimage(new URL("http://goo.gl/N96GJN"));
		} catch (MalformedURLException e) {
		    // eat the exception
		}
		bookMap.put(book.getIsbn(), book);
		
		book = new Book();
		book.setIsbn(5);
		book.setCategory("selfimprovement");
		book.setTitle("Robin Cook");
		try {
		    book.setCoverimage(new URL("http://goo.gl/N96GJN"));
		} catch (MalformedURLException e) {
		    // eat the exception
		}
		bookMap.put(book.getIsbn(), book);*/
	
		return bookMap;
    }

    /**
     * This should be called if and only if you are adding new books to the
     * repository.
     * 
     * @return a new incremental ISBN number
     */
    private final Long generateISBNKey() {
	// increment existing isbnKey and return the new value
	return Long.valueOf(++isbnKey);
    }

    /**
     * This will auto-generate unique ISBN for new books.
     */
    @Override
    public Book saveBook(Book newBook) {
	checkNotNull(newBook, "newBook instance must not be null");
	// Generate new ISBN
	Long isbn = generateISBNKey();
	newBook.setIsbn(isbn);
	// TODO: create and associate other fields such as author

	// Finally, save the new book into the map
	bookInMemoryMap.putIfAbsent(isbn, newBook);

	return newBook;
    }

    /**
     * @see edu.sjsu.cmpe.library.repository.BookRepositoryInterface#getBookByISBN(java.lang.Long)
     */
    @Override
    public Book getBookByISBN(Long isbn) {
	checkArgument(isbn > 0,
		"ISBN was %s but expected greater than zero value", isbn);
	return bookInMemoryMap.get(isbn);
    }

    @Override
    public List<Book> getAllBooks() {
	return new ArrayList<Book>(bookInMemoryMap.values());
    }

    /*
     * Delete a book from the map by the isbn. If the given ISBN was invalid, do
     * nothing.
     * 
     * @see
     * edu.sjsu.cmpe.library.repository.BookRepositoryInterface#delete(java.
     * lang.Long)
     */
    @Override
    public void delete(Long isbn) {
	bookInMemoryMap.remove(isbn);
    }

}
