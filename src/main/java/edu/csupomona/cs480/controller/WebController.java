package edu.csupomona.cs480.controller;

import java.util.Arrays;
//import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;

import edu.csupomona.cs480.App;
import edu.csupomona.cs480.data.User;
import edu.csupomona.cs480.data.provider.UserManager;



import com.mongodb.BasicDBList;
//MongoDB imports
import com.mongodb.BasicDBObject;
import com.mongodb.BulkWriteOperation;
import com.mongodb.BulkWriteResult;
import com.mongodb.Cursor;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.ParallelScanOptions;

import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static java.util.concurrent.TimeUnit.SECONDS;


/**
 * This is the controller used by Spring framework.
 * <p>
 * The basic function of this controller is to map
 * each HTTP API Path to the correspondent method.
 *
 */

@RestController
public class WebController {

	/**
	 * When the class instance is annotated with
	 * {@link Autowired}, it will be looking for the actual
	 * instance from the defined beans.
	 * <p>
	 * In our project, all the beans are defined in
	 * the {@link App} class.
	 */
    @Autowired
    private UserManager userManager;
    
    
    
    //MongoDB Global Objects
    MongoClient mongoClient;
	DB db; 
	DBCollection usersColl;
	DBCollection listsColl;

	//Constructor for WebController to handle 1 time MongoDB initializations
    public WebController() throws UnknownHostException{
    	
    	//Initialize connection to MongoDB
    	//Do this once on the WebController constructor to prevent wasted connections
    	mongoClient = new MongoClient( "localhost" , 27017 );
    	
    	//Initialize a reference to the specific db
    	db = mongoClient.getDB( "test" );
    	    	
    	//Initialize references to collections
		usersColl = db.getCollection("users");
		listsColl = db.getCollection("lists");
    }
    
    

    /**
     * This is a simple example of how the HTTP API works.
     * It returns a String "OK" in the HTTP response.
     * To try it, run the web application locally,
     * in your web browser, type the link:
     * 	http://localhost:8080/cs480/ping
     */
    @RequestMapping(value = "/cs480/ping", method = RequestMethod.GET)
    String healthCheck() {
    	// You can replace this with other string,
    	// and run the application locally to check your changes
    	// with the URL: http://localhost:8080/
        return "OK";
    }

    @RequestMapping(value = "/cs480/isaac", method = RequestMethod.GET)
    String isaac() throws UnknownHostException {
    	System.out.println("THe start of the end ==========");
		
		
		List<Integer> books = Arrays.asList(27464, 747854);
		DBObject person = new BasicDBObject("_id", "jo")
		                            .append("name", "Jo Bloggs")
		                            .append("address", new BasicDBObject("street", "123 Fake St")
		                                                         .append("city", "Faketon")
		                                                         .append("state", "MA")
		                                                         .append("zip", 12345))
		                            .append("books", books);
		//coll.insert(person);
		System.out.println(person.get("_id").toString());
		DBObject myDoc = usersColl.findOne();
		System.out.println(myDoc);
    	// You can replace this with other string,
    	// and run the application locally to check your changes
    	// with the URL: http://localhost:8080/
        return "is awesome";
    }
    
    
    /**
     * userExists()
     * Returns a boolean describing if there is a user in the userColl with the same name.
     * @param userName
     */
    @RequestMapping(value = "/cs480/userExists/{userName}", method = RequestMethod.GET)
    Boolean userExists(@PathVariable("userName") String userName) throws UnknownHostException {
    	DBObject query = new BasicDBObject("userName", userName); 
    	DBCursor cursor = usersColl.find(query);
    	DBObject result = cursor.one();
    	System.out.println("Call to userExists() :" + result);
    	Boolean userExists = !(result == null);
    	
        return userExists;
    }
    
    @RequestMapping(value = "/cs480/vincent_test", method = RequestMethod.GET)
    String vincent() {
    	// You can replace this with other string,
    	// and run the application locally to check your changes
    	// with the URL: http://localhost:8080/
        return "I am testing the functionality for Assignment 3!";
    }
    
    @RequestMapping(value = "/cs480/test_e", method = RequestMethod.GET)
    String ethan() {
    	//URL: http://localhost:8080/
        return "WALL-E & Space";
    }
    
    @RequestMapping(value = "/cs480/mitchell", method = RequestMethod.GET)
    String mitchell() {
    	// You can replace this with other string,
    	// and run the application locally to check your changes
    	// with the URL: http://localhost:8080/
        return "potato";
    }
    
    /**
     * This is a simple example of how to use a data manager
     * to retrieve the data and return it as an HTTP response.
     * <p>
     * Note, when it returns from the Spring, it will be
     * automatically converted to JSON format.
     * <p>
     * Try it in your web browser:
     * 	http://localhost:8080/cs480/user/user101
     */
    @RequestMapping(value = "/cs480/user/{userId}", method = RequestMethod.GET)
    User getUser(@PathVariable("userId") String userId) {
    	User user = userManager.getUser(userId);
        return user;
    }
    
    /**
     * createUser()
     * Creates a new user in the database
     * Note: Does not check for duplicates
     * @param userName
     * @param password
     * @return
     */
    @RequestMapping(value = "/cs480/createUser/{userName}", method = RequestMethod.POST)
    Boolean createUser(
    		@PathVariable("userName") String userName ,
    		@RequestParam("password") String pw){
    	DBObject user = new BasicDBObject("userName", userName);
    	
    	usersColl.insert(user);
    	
    	System.out.println("Call to createUser() :" + user.toString());
    	
    	return true;
    }
    
    //Basic API to retrieve list
    @RequestMapping(value = "/cs480/list/{listName}", method = RequestMethod.GET)
    String getList(
    		@PathVariable("listName") String listName){
    	BasicDBObject query = new BasicDBObject("listName", listName);
    	
    	DBCursor cursor = listsColl.find(query);
    	//Uses the first list found from search
    	DBObject listObject = cursor.one();
    	String result = "";
    	if(listObject != null)
    	{
			//Get the items part of the list DBObject and cast as a list
			BasicDBList items = (BasicDBList)listObject.get("item");
			System.out.println(items.toString());
			
			BasicDBObject [] itemArray = items.toArray(new BasicDBObject[0]);
			for(int i = 0 ; i < itemArray.length; i++)
			{
				result+= itemArray[i].toString() + " ";
			}
    	}
    	else
    	{
    		cursor.close();  	
    		return "no_list";
    	}
    	cursor.close();  	
    	return result;
    }
    

    /**
     * This is an example of sending an HTTP POST request to
     * update a user's information (or create the user if not
     * exists before).
     *
     * You can test this with a HTTP client by sending
     *  http://localhost:8080/cs480/user/user101
     *  	name=John major=CS
     *
     * Note, the URL will not work directly in browser, because
     * it is not a GET request. You need to use a tool such as
     * curl.
     *
     * @param id
     * @param name
     * @param major
     * @return
     */
    @RequestMapping(value = "/cs480/user/{userId}", method = RequestMethod.POST)
    User updateUser(
    		@PathVariable("userId") String id,
    		@RequestParam("name") String name,
    		@RequestParam(value = "major", required = false) String major) {
    	User user = new User();
    	user.setId(id);
    	user.setMajor(major);
    	user.setName(name);
    	userManager.updateUser(user);
    	return user;
    }

    /**
     * This API deletes the user. It uses HTTP DELETE method.
     *
     * @param userId
     */
    @RequestMapping(value = "/cs480/user/{userId}", method = RequestMethod.DELETE)
    void deleteUser(
    		@PathVariable("userId") String userId) {
    	userManager.deleteUser(userId);
    }

    /**
     * This API lists all the users in the current database.
     *
     * @return
     */
    @RequestMapping(value = "/cs480/users/list", method = RequestMethod.GET)
    List<User> listAllUsers() {
    	return userManager.listAllUsers();
    }

    /*********** Web UI Test Utility **********/
    /**
     * This method provide a simple web UI for you to test the different
     * functionalities used in this web service.
     */
    @RequestMapping(value = "/cs480/home", method = RequestMethod.GET)
    ModelAndView getUserHomepage() {
        ModelAndView modelAndView = new ModelAndView("home");
        modelAndView.addObject("users", listAllUsers());
        return modelAndView;
    }

}