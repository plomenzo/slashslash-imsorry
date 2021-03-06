package edu.csupomona.cs480.controller;

//Project imports
import edu.csupomona.cs480.App;
import edu.csupomona.cs480.data.User;
import edu.csupomona.cs480.data.provider.UserManager;






//Java imports
import java.util.Arrays;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static java.util.concurrent.TimeUnit.SECONDS;






//Apache imports
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;

//Joda Library
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

//Jsoup Library
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

//Bson Object import
import org.bson.types.ObjectId;






//Google? import
import static com.google.common.base.Preconditions.*;

//Object Mapper? 
import com.fasterxml.jackson.databind.ObjectMapper;






//Spring Framework
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;






//MongoDB imports
import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBList;
import com.mongodb.BulkWriteOperation;
import com.mongodb.BulkWriteResult;
import com.mongodb.Cursor;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.ParallelScanOptions;
import com.mongodb.QueryBuilder;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.WriteResult;
import com.mongodb.BasicDBList;




/**
 * This is the controller used by Spring framework.
 * <p>
 * The basic function of this controller is to map
 * each HTTP API Path to the correspondent method.
 *
 */
@RestController
public class WebController {
	
	private static WebController instance = null;
	
	   public static WebController getInstance() throws Exception 
	   {
	        if (instance == null) 
	        {
	            instance = new WebController();
	        }
	 
	        return instance;
	    }
    

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
    private WebController() throws UnknownHostException {
    	//Initialize connection to MongoDB
    	//Do this once on the WebController constructor to prevent wasted connections
    	Boolean useLocal = false;
    	
    	if(useLocal)
    	{
    		mongoClient = new MongoClient( "localhost" , 27017 );
    		checkNotNull(mongoClient);
        	db = mongoClient.getDB( "test" );
    	}
    	else
    	{
    		//Uses mongolab mongodb in AWS
        	mongoClient = new MongoClient(new MongoClientURI("mongodb://dev:devpassword@ds029811.mongolab.com:29811/list_manager_1"));
        	db = mongoClient.getDB( "list_manager_1" );
    	}
        	    	
    	//Initialize references to collections
		usersColl = db.getCollection("users");
		listsColl = db.getCollection("lists");
		
		//Initialize VoiceRecognitionCorrector
		//May take time depending on how large the corrections file is
		//Uses singularity
		VoiceRecognitionCorrector vrc = VoiceRecognitionCorrector.getVoiceRecognitionCorrector();
    }    
    
    /**
     * userExists()
     * Returns a boolean describing if there is a user in the userColl with the same name.
     * @param userName
     */
    @RequestMapping(value = "/cs480/userExists/{userName}", method = RequestMethod.GET)
    public Boolean userExists(@PathVariable("userName") String userName){

    	DBObject query = new BasicDBObject("userName", userName); 
    	DBCursor cursor = usersColl.find(query);
    	DBObject result = cursor.one();
    	
    	System.out.println("Call to userExists() :" + result);
    	Boolean userExists = !(result == null);
    	
        return userExists;
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
    public Boolean createUser(
    		@PathVariable("userName") String userName ,
    		@RequestParam("password") String pw) {

    	DBObject user = new BasicDBObject("userName", userName)
    	                    .append("password", pw)
    						.append("userAccessibleLists", new BasicDBList());
    	
    	//Check to see if username is being used
    	DBObject query = new BasicDBObject("userName", userName); 
    	DBCursor cursor = usersColl.find(query);
    	DBObject result = cursor.one();
    	
        //If username exists
    	if(result != null)
        {
        	System.out.println("User Name Already Exists :" + user.toString());
        	return false;
        }
        else
        {   //Username does not exist
            usersColl.insert(user);
        	
        	//Creates a default list for user
        	createList(	"Default List" , user.get("_id").toString());
        	
        	System.out.println("Call to createUser() :" + user.toString());
        	
        	return true;	
        }
    }
    
    /**
     * createList()
     * Creates a new list in the database
     * Note: Does not check for duplicates
     * List JSON Object format
    	  List
    	  {
    	  	listname: name
    	  	items:
    	  	[
    	  		item
    	  		item
    	  	]
    	  	userAccess:
    	  	[
    	  		userID
    	  		userID
    	  	]
    	  	itemHistory:
    	  	{
    	  		item
    	  		item
    	  	}
    	  	createdBy: userID
    	  	lastModified: (time_in_millis)
    	  }
    	  
     * @param listName
     * @param userAccess
     * @return
     */    
	@RequestMapping(value = "/cs480/createList/{listName}", method = RequestMethod.POST)
    Boolean createList(
    		@PathVariable("listName") String listName ,
    		@RequestParam("creatorUserID") String creatorUserID) {
		
		DBObject list = createListInDatabase( creatorUserID, listName );
		
    	//Get the list oid to add to the user 
		String listId = extractListID(list);
    	
		addNewListToUser(listId, creatorUserID);
    	
    	System.out.println("Call to createList() :" + list.toString());
    	
    	return true;
    } 
	
	private DBObject createListInDatabase(String creatorUserID, String listName ){
		BasicDBList userAccess = new BasicDBList();
    	userAccess.add(creatorUserID);
    	DBObject list = new BasicDBObject("listName", listName)
    					.append("items", new BasicDBList())
    					.append("userAccess", userAccess)
    					.append("itemHistory", new BasicDBList())
    					.append("createdBy", creatorUserID)
    					.append("lastModified", System.currentTimeMillis());
    	
    	listsColl.insert(list);
    	
    	return list;
		
	}
	
	private String extractListID(DBObject list){
	 	String listId = (list.get("_id")).toString();
	 	return listId;	
	}
	
	private void addNewListToUser(String listId, String creatorUserID){
		//Get user object
    	BasicDBObject queryUser = new BasicDBObject("_id", new ObjectId(creatorUserID));
    	DBCursor cursorUser = usersColl.find(queryUser);
    	//Use the first user found from search
    	DBObject oldUserObject = cursorUser.one();

    	DBObject newUserObject = appendNewListToUserObject(oldUserObject, listId, cursorUser);
    	
    	updateUserObjectInDatabase(cursorUser,newUserObject);    			
	}
	
	private void updateUserObjectInDatabase(DBCursor cursorToUser,DBObject newUserObject){
		//Get original user
    	DBObject origUser = cursorToUser.one();
    	//Update user object
    	usersColl.update(origUser, newUserObject);
    	//Close cursor to user
    	cursorToUser.close();
	}
	
	private DBObject appendNewListToUserObject(DBObject userObject, String listId, DBCursor cursorUser){
		//Get the lists that the user has access to
    	BasicDBList usersLists = (BasicDBList) userObject.get("userAccessibleLists");

    	//Add new list id
    	usersLists.add(listId);
    	//Put back the user accessible lists list
    	userObject.put("userAccessibleLists", usersLists);
    	
    	return userObject;
	}
	
    
    /**
     * getList()
     * Returns a list object matching the specified $oid of the list from the database.
     * @param id
     */   	
    @RequestMapping(value = "/cs480/getList/{listId}", method = RequestMethod.GET)
	public
    DBObject getList(
    		@PathVariable("listId") String listId) {
    	
    	try {
	    	BasicDBObject query = new BasicDBObject("_id", new ObjectId(listId));
	    	DBCursor cursor = listsColl.find(query);
	    	
	    	//Uses the first list found from search
			DBObject listObject = cursor.one();
	    	cursor.close();  	
	    	System.out.println("Call to getList() : " + listObject.toString());
	
	    	return listObject;
    	}
    	catch(Exception e)
    	{
    		//TEMP FIX: Return null object as the list is not present
    		//ObjectId(id) throws error if the id given is invalid
    		return null;
    	}
    }
    
    /**
     * removeItem()
     * Removes an item from a list specified from the $oid of the list in the database
     * @param id $oid of list
     * @param itemName name of item to remove
     * @return the boolean value of true if item was removed, false if item failed to remove
     */    
    @RequestMapping(value = "/cs480/removeItem/{listId}/{itemName}", method = RequestMethod.POST)
	public
    boolean removeItem(
    		@PathVariable("listId") String listId,
    		@PathVariable("itemName") String itemName) {
    	
    	//List that we want to find
    	BasicDBObject listObject = new BasicDBObject("_id",new ObjectId(listId));
    	//Item that we want to remove
    	DBObject item =  new BasicDBObject("name", itemName);
    	DBObject itemRemove = new BasicDBObject("items",item);
    	
    	//Removes item from item list
    	listsColl.update(listObject,new BasicDBObject("$pull", itemRemove), false, false);
    	//Item we want to add to history
    	DBObject itemHistory = new BasicDBObject("itemHistory", item);
    	
    	//Adds item to history
    	listsColl.update(listObject, new BasicDBObject("$push", itemHistory));
    	DBObject lastUpdated = new BasicDBObject("lastModified", System.currentTimeMillis());
        listsColl.update(listObject, new BasicDBObject("$set", lastUpdated), false, false);
        
        System.out.println("Call to removeItem: " + itemName);
        return true;
    }
    
    /**
     * Adds item to list
     * @param id List ID
     * @param name Item name
     * @param quantity item quantity
     * @param price item price
     * @param isChecked is item "checked"
     * @return 
     */   
    @RequestMapping(value = "/cs480/addItem/{listId}/{userId}", method = RequestMethod.POST)
    Boolean addItemToList(
    		@PathVariable("listId") String listId,
    		@PathVariable("userId") String userId,
    		@RequestParam("itemName") String name,
    		@RequestParam("price") double price,
    		@RequestParam("quantity") int quantity,
    		@RequestParam("isChecked") boolean isChecked) {
    	long start = System.currentTimeMillis();
    	//Verify is the user is allowed to add to the list
    	if(!verifyListAccess(listId,userId))
    	{
    		System.out.println("user doesnt have access");
    		return false;
    	}
    	long end = System.currentTimeMillis();
    	System.out.println("Finsihed user verification " + (end - start) + "ms");
    	BasicDBObject query = new BasicDBObject("_id",new ObjectId(listId));
    	DBCursor cursor = listsColl.find(query);
    	DBObject listObject = cursor.one();
    	
		//Get the items part of the list DBObject and cast as a list
		BasicDBList items = (BasicDBList) listObject.get("items");
		
		//Voice Correct item name
		String correction = VoiceRecognitionCorrector.getVoiceRecognitionCorrector().correct(name);
		if(!name.equals(correction))
		{
			System.out.println("During addItem correction made to itemName: \"" + name + "\" corrected to \"" + correction + "\"");
			name = correction;
		}

		//Create new item
		BasicDBObject item = new BasicDBObject("name", name)
								.append("quantity", quantity)
								.append("price", price)
								.append("isChecked", isChecked);
								//.append("wantedBy", new BasicDBObject("userId", userId));
		
		//Checks to see if itemName is already present in list
		BasicDBObject[] itemList = items.toArray(new BasicDBObject[0]);
		for(BasicDBObject i : itemList)
		{
			if(i.get("name").equals(( name)))
			{
				return false;
			}
		}
		end = System.currentTimeMillis();
    	System.out.println("Finsihed checking list " + (end - start) + "ms");
		//Add item to list
		items.add(item);
		//Removes item in new list
        listObject.put("items", items);    
        
        //Copy constructor does not work for some reason
        //Thus orig = listObject will give us effectively
        //2 listObjects
    	//DBObject orig = cursor.one();
    	//Replace old list with new list
        listsColl.update(query,listObject);
        //Updates lastUpdated to current time
        DBObject lastUpdated = new BasicDBObject("lastModified", System.currentTimeMillis());
        listsColl.update(listObject, new BasicDBObject("$set", lastUpdated), false, false);
        end = System.currentTimeMillis();
    	System.out.println("Finsihed adding item " + (end - start) + "ms");
        //Print to console
    	System.out.println("Call to addItem() : "  +  listObject.toString());
        
        return true;	
    }
    
    /**
=======
>>>>>>> Commented out testGetList()
     * Edits item in list
     * @param id List ID
     * @param oldName old Item(before edit) name
     * @param name new name of item
     * @param user user who's is editing
     * @param quantity item quantity
     * @param price item price
     * @param isChecked is item "checked"
     * @return 
     */    
    @RequestMapping(value = "/cs480/editItem/{listId}/{oldName}", method = RequestMethod.POST)
    Boolean editItem(
    		@PathVariable("listId") String listId,
    		@PathVariable("oldName") String oldName,
    		@RequestParam("name") String newName,
    		@RequestParam("userId") String userId,
    		@RequestParam("quantity") int quantity,
    		@RequestParam("price") double price,
    		@RequestParam("isChecked") boolean isChecked) {
    	
    	//Verify is the user is allowed to edit list
    	if(!verifyListAccess(listId,userId))
    	{
    		System.out.println("user doesnt have access");
    		return false;
    	}
    	
    	BasicDBObject query = new BasicDBObject("_id",new ObjectId(listId));
    	DBCursor cursor = listsColl.find(query);
    	DBObject listObject = cursor.one();

		//Get the items part of the list DBObject and cast as a list
		BasicDBList items = (BasicDBList) listObject.get("items");
		BasicDBList temp = new BasicDBList();
		BasicDBObject[] itemList = items.toArray(new BasicDBObject[0]);		
		for(BasicDBObject i : itemList)
		{
			if(i.get("name").equals((oldName)))
			{
				i.append("name",newName).append("quantity", quantity).append("price",price).append("isChecked", isChecked);
			}
			temp.add(i);
		}
		//Update the list with the edited item
		items = temp;
		listObject.put("items", items);
		listsColl.update(cursor.one(),listObject);
		
    	return true;
    }	
    
    /**
     * gets the user oid from userName
     * @param userName
     * @return oid or "" if userName is not present
     */   
    String getUserOIDFromName(String userName)
    {
    	DBObject query = new BasicDBObject("userName", userName); 
    	DBCursor cursor = usersColl.find(query);
    	DBObject result = cursor.one();
    	if(result == null)
    	{
    		return "";
    	}
    	//return the oid
    	return result.get("_id").toString();
    }
    
    /**
     * Adds user to userAccess
     * @param id List ID
     * @param userId The user oid you are adding
     * @return 
     */  
    @RequestMapping(value = "/cs480/inviteUser/{listId}", method = RequestMethod.POST)
    Boolean inviteUser(
    		@PathVariable("listId") String listId,
    		@RequestParam("userName") String userName) {
    	if(!userExists(userName))
    	{
    		return false;
    	}

    	//Get The id using the username
    	String userId = getUserOIDFromName(userName);

    	BasicDBObject query = new BasicDBObject("_id",new ObjectId(listId));
    	DBCursor cursor = listsColl.find(query);
    	DBObject listObject = cursor.one();
    	System.out.println(listObject);
    	//Check to see if they are already added to the list *not implemented yet
    	
		//Get the UserAccess list
    	BasicDBList userList = (BasicDBList) listObject.get("userAccess");
   
    	if(userList.contains(userId))
    	{
    		return true;
    	}
    	//Add user to userId
		userList.add(userId);
		
		//Add the user to userAccess
        listObject.put("userAccess", userList);
        
        //orig = listObject will give us 2 list objects
    	DBObject origList = cursor.one();
    	
    	//Update the list collection with new user object
    	listsColl.update(origList,listObject);
    	
    	//Get user object
    	BasicDBObject queryUser = new BasicDBObject("_id", new ObjectId(userId));
    	DBCursor cursorUser = usersColl.find(queryUser);
    	//Use the first user found from search
    	DBObject userObject = cursorUser.one();
    	//Get the lists that the user has access to
    	BasicDBList usersLists = (BasicDBList) userObject.get("userAccessibleLists");
    	
    	System.out.println(usersLists);
    	System.out.println(listId);
    	
    	//Add new list id
    	usersLists.add(listId);
    	//Put back the user accessible lists list
    	userObject.put("userAccessibleLists", usersLists);
    	//Get original user
    	DBObject origUser = cursorUser.one();
    	
    	//Update user object
    	usersColl.update(origUser, userObject);
    	//Close cursor to user
    	cursorUser.close();  	
    	
    	System.out.println("Call to inviteUser() : " + userObject.toString());
    	
    	return true;
    }
    
    /**
     * Deletes an entire list
     * @param id List ID
     * @return 
     */   
    @RequestMapping(value = "/cs480/deleteList/{listId}", method = RequestMethod.POST)
    public boolean deleteList(
    		@PathVariable("listId") String listId) {

	   	//List that we want to find
    	BasicDBObject listObject = new BasicDBObject("_id",new ObjectId(listId));
		
    	//TEMP SOLUTION
    	//We find the list in the db
    	DBCursor listCursor = listsColl.find(listObject);
		DBObject listObj = listCursor.one();
		
        //We remove the list from each of the user's userAccessibleLists
		BasicDBList userList =  (BasicDBList)(listObj.get("userAccess"));
        for(int i = 0; i < userList.size(); i++)
        {
        	removeListFromUser((String)userList.get(i),listObject);
        }
		//Remove list
	    listsColl.remove(listObject);
	    
	    System.out.println("Call to removeList: " + listId);
	    
	    return true;
    }
    
    /**
     * 
     * @param listId
     * @param userId
     * @return
     */   
    public boolean verifyListAccess(String listId, String userId)
    {
    	BasicDBList userList = getUserListId(userId);
		String[] userArray = userList.toArray(new String[0]);
		for(String oid : userArray)
		{  
			if(oid.equals(listId))
			{
				//User has access to list
				return true;
			}
		}
		//No access
    	return false; 
    }
    
    /**
     * Authenticates a user/password
     * @param userName user name
     * @param password password
     * @return true if password is correct
     * @return false if password is incorrect
     * @return false if user doesn't not exist
     */ 
    @RequestMapping(value = "/cs480/authenticate/{userName}", method = RequestMethod.POST)
    public String authenticate(
    		@PathVariable("userName") String userName,
    		@RequestParam("password") String pw) {
    	
    	//Find the user given the user name
    	DBObject query = new BasicDBObject("userName", userName); 
    	DBCursor cursor = usersColl.find(query);
    	
    	try
    	{
	    	DBObject result = cursor.one();
	    	
	    	BasicDBObject userObject = new BasicDBObject("userName",userName).append("password", pw);
	    	//Check to see if the passwords match
	    	if(userObject.get("password").equals(result.get("password")))
	    	{   	
	    		//Password is correct
	    		System.out.println(userName + "authenticated");
	    		System.out.println(result.get("_id").toString());
	    		return result.get("_id").toString();
	    	}	
	    	else
	    	{
	    		//Password is incorrect
	    		System.out.println("password is incorrect");
	    		return "Login failed";
	    	}
    	}
    	catch (Exception e)
    	{
    		//User name does not exist
    		System.out.println(userName + " doesn't exist");
    		return "Login Failed";
    	}
    }
    
    /**
     * getUserLists()
     * Returns a list with objects containing both the oids  and names
     * of all the lists the user has access to.
     * @param userId
     */    
    @RequestMapping(value = "/cs480/getUserLists/{userId}", method = RequestMethod.GET)
    BasicDBList getUserLists(
    		@PathVariable("userId") String userId) {
    	BasicDBObject userQuery = new BasicDBObject("_id", new ObjectId(userId));
    	
    	DBCursor userCursor = usersColl.find(userQuery);
    	//Uses the first User found from search
    	DBObject userObject = userCursor.one();
    	BasicDBList usersListsOIDs = (BasicDBList)userObject.get("userAccessibleLists");
    	BasicDBList lists = new BasicDBList();
    	
    	for(int i = 0; i < usersListsOIDs.size(); i++)
    	{
    		BasicDBObject listQuery = new BasicDBObject("_id", new ObjectId(usersListsOIDs.get(i).toString()));
    		DBCursor listCursor = listsColl.find(listQuery);
    		DBObject listObj = listCursor.one();
    		
    		String listName = (String)(listObj.get("listName"));
    		BasicDBObject currentList = new BasicDBObject("oid", usersListsOIDs.get(i))
    											.append("name", listName);
    		listCursor.close();
    		lists.add(currentList);

    	}
    	userCursor.close();  
    	System.out.println("Call to getUserLists() of user: " + userObject.toString());
    	
    	return lists;
    }
    
    BasicDBList getUserListId(String userId)
    {
    	BasicDBObject userQuery = new BasicDBObject("_id", new ObjectId(userId));
    	
    	DBCursor userCursor = usersColl.find(userQuery);
    	//Uses the first User found from search
    	DBObject userObject = userCursor.one();
    	BasicDBList usersListsOIDs = (BasicDBList)userObject.get("userAccessibleLists");
    	return usersListsOIDs;
    }
  
    /**
     * Returns the history object from given list
     * @param listOID
     * @return
     */
    @RequestMapping(value = "/cs480/getHistory/{listId}", method = RequestMethod.GET)
    public DBObject getHistory(
    		@PathVariable("listId") String listId) {
    	
    			BasicDBObject query = new BasicDBObject("_id", new ObjectId(listId));
    	    	//We find the list in the db
    	    	DBCursor listCursor = listsColl.find(query);
    			DBObject listObj = listCursor.one();
    			BasicDBList history = (BasicDBList)listObj.get("itemHistory");
    			
    			System.out.println("Call to get history() : " + listId);
    			
    			return history;
    		}
    
    /**
     * Removes a list from a user
     * @param userQuery User that we want to remove a list from
     * @param listObject List that we want to remove from the user
     */
    private void removeListFromUser(String userID, DBObject listObject)
    {
    	//We find the user
    	BasicDBObject userQuery = new BasicDBObject("_id", new ObjectId(userID));
    	DBCursor userCursor = usersColl.find(userQuery);
    	DBObject user = userCursor.one();
        System.out.println(user);
        
    	//We get the userAccessibleLists
    	BasicDBList list = (BasicDBList) user.get("userAccessibleLists");
    	//We remove the list from the userAccessibleLists
    	list.remove(listObject.get("_id"));
    	//We update the user object
        user.put("userAccessibleLists", list);
        DBObject userOrig = userCursor.one();
        
        //We update the database user
        usersColl.update(userOrig,user);
    }
  
    //Temp debug due to remove list glitch leaving in case we need function
    /**
     * Public check to see if a list exists
     * @param id ID of the list
     * @return
     */
    @RequestMapping(value = "/cs480/manualRemoveList/{listId}", method = RequestMethod.POST)
    public void manualRemoveList(
    		@PathVariable("listId") String listId,
            @RequestParam("userID") String userID) {
    	
	   	//List that we want to find
    	BasicDBObject userQuery= new BasicDBObject("_id",new ObjectId(userID));
    	//BasicDBObject listQuery = new BasicDBObject("_id",new ObjectId(id));
    	DBCursor userCursor = usersColl.find(userQuery);
    	DBObject user = userCursor.one();
    	
        //We get the userAccessibleLists
    	BasicDBList list = (BasicDBList) user.get("userAccessibleLists");
        System.out.println(list);
        
    	//We remove the list from the userAccessibleLists
    	list.remove(listId);
        System.out.println(list);
        
    	//We update the user object
        user.put("userAccessibleLists", list);
        DBObject userOrig = userCursor.one();
        
        //We update the database user
        usersColl.update(userOrig,user);
    	userCursor.close();
    }  
    
    /**
     * Changes checked state of item in list
     * @param listId
     * @param itemName
     * @param isChecked
     * @return
     */
    @RequestMapping(value = "/cs480/changeCheckState/{listId}", method = RequestMethod.POST)
    boolean changeCheckedState(
        	@PathVariable("listId") String listId,
    		@RequestParam("itemName") String itemName,
    		@RequestParam("isChecked") boolean isChecked) {
    	
    	BasicDBObject query = new BasicDBObject("_id",new ObjectId(listId));
    	DBCursor cursor = listsColl.find(query);
    	DBObject listObject = cursor.one();

		//Get the items part of the list DBObject and cast as a list
		BasicDBList items = (BasicDBList) listObject.get("items");
		BasicDBList temp = new BasicDBList();
		BasicDBObject[] itemList = items.toArray(new BasicDBObject[0]);
		for(BasicDBObject i : itemList)
		{
			if(i.get("name").equals((itemName)))
			{
				i.append("isChecked", isChecked);
			}
			temp.add(i);
		}
		items = temp;
		listObject.put("items", items);
		//Update the list with the new checked state
		listsColl.update(cursor.one(),listObject);
		
		System.out.println("Call to changeChekedState() : list: " + listId + " " + " item : " + itemName + " checked : " + isChecked);
		
    	return true;
    }
    
    /**
     * Cost splits for a list
     * @param id List ID
     * @return 
     */
    @RequestMapping(value = "/cs480/splitCostOfList/{listId}", method = RequestMethod.GET)
    public double splitCostOfList(
    		@PathVariable("listId") String listId,
    		@RequestParam("userId") String userId) {

	   	//List that we want to find
    	BasicDBObject listObject = new BasicDBObject("_id",new ObjectId(listId));
    	DBCursor listCursor = listsColl.find(listObject);
		DBObject listObj = listCursor.one();
		
		//First sum up prices
		double totalPrice = 0;
		BasicDBList itemsList = (BasicDBList)(listObj.get("items"));
		for(int i = 0; i < itemsList.size(); i++)
		{
			BasicDBObject item  = (BasicDBObject)(itemsList.get(i));
			double itemPrice = item.getDouble("price");
			totalPrice += itemPrice;
		}
		System.out.println("totalPrice after adding is: " + totalPrice);
		
		//Short circuits if totalPrice is 0 then no need to calc numUsers
		if(totalPrice == 0)
		{
			return 0.0;
		}
		
		//second sum up number of users that use this list
		BasicDBList userList =  (BasicDBList)(listObj.get("userAccess"));
		int numUsers = userList.size();
		
		//Short circuits divide if numUsers is 0 or 1
		System.out.println("Number of users in list is: " + numUsers);
		if((numUsers == 0) || (numUsers == 1))
		{
			return totalPrice;
		}
		
		//Third divide the summed price by number of users
		double result = totalPrice / numUsers;
		
		System.out.println("Result of cost splitting:" + result);
		
		return result;
    }

//////////////////////////////////////////////////Assignment 5//////////////////////////////////
//////////////////////////////////////////////////Assignment 5//////////////////////////////////
//////////////////////////////////////////////////Assignment 5//////////////////////////////////
//////////////////////////////////////////////////Assignment 5//////////////////////////////////
//////////////////////////////////////////////////Assignment 5//////////////////////////////////
    /**
     * Prints out the items listed in the store's online advertisement
     * @return
     * @throws IOException
     */
    @RequestMapping(value = "/cs480/storeprice/", method = RequestMethod.GET)
    String getStorePrice() throws IOException {
    	
    	String result = "";
    	//Make the url's dynamic
    	String albertsonsURL = "http://albertsons.mywebgrocer.com/Circular/San-Dimas/8C4073634/Weekly/2/";
    	String ralphsURL = "http://ralphs.mywebgrocer.com/Circular/RALPHS-626/000592641/Weekly/1/";
    	//Arraylist?
    	String[] stores = new String[2];
    	stores[0] = albertsonsURL;
    	stores[1] = ralphsURL;
    	String[]storeName = new String[2];
    	storeName[0] = "Albertsons";
    	storeName[1] = "Ralphs";
    	for(int j = 0; j < stores.length; j++)
    	{
    		result+="***********************\n";
    		result+="***********************\n";
    		result+="Store " + storeName[j] +"\n";
    		result+="***********************\n";
    		result+="***********************\n";

        	try
        	{
            	int page = 1;
        		while(true)
        		{
            		String pageURL = stores[j] + page;
            		System.out.println(pageURL);
                	Document store = Jsoup.connect(pageURL).get();
                	if((store.location().contains("2/1") || store.location().contains("1/1")) & page >1)
                	{
                		throw new NullPointerException();
                	}
                	Elements saleItems = store.select("p.itemTitle");
                	Elements salePrice = store.select("p.itemPrice");
                	for(int i = 0; i < saleItems.size(); i++)
                	{
                		String item = String.format("Item       %s \n", saleItems.get(i).text());
                		System.out.print(item);
                		result+=item;
                		//May want to add a condition, i.e. membership card, buy 4, etc
                		//Instead of having it included in the sale price
                		String price = String.format("Sale Price %s \n", salePrice.get(i).text());
                		System.out.print(price);
                		result+=price;
                		result+="-----------------------\n";

                	}
                	page++;
        		}

        	}
        	catch(Exception e)
        	{
        		continue;
        	}
    	}
    	
    	return result;

    }

    
    /**
     * Tests out the Apache Commons Math for Assignment 5
     * @return
     */
    @RequestMapping(value = "/cs480/commonsMathExample/", method = RequestMethod.GET)
	public
    String commonsMathExample() {
    	
    	//Create a real matrix with two rows and three columns, using a factory
    	//method that selects the implementation class for us.
    	double[][] matrixData = { {1d,2d,3d}, {2d,5d,3d}};
    	RealMatrix m = MatrixUtils.createRealMatrix(matrixData);

    	//One more with three rows, two columns, this time instantiating the
    	//RealMatrix implementation class directly.
    	double[][] matrixData2 = { {1d,2d}, {2d,5d}, {1d, 7d}};
    	RealMatrix n = new Array2DRowRealMatrix(matrixData2);

    	//Note: The constructor copies  the input double[][] array in both cases.

    	//Now multiply m by n
    	RealMatrix p = m.multiply(n);
    	System.out.println(p.getRowDimension());    // 2
    	System.out.println(p.getColumnDimension()); // 2

    	//Invert p, using LU decomposition
    	RealMatrix pInverse = new LUDecomposition(p).getSolver().getInverse();
    	
    	return pInverse.toString();
    }
    
    /**
     * ASSIGNMENT 5
     * Prints the date in LA
     * @return
     */
    @RequestMapping(value = "/cs480/getDate", method = RequestMethod.GET)
    boolean getDate() {
    	
    	//Set up Data for LA
    	DateTime utc = new DateTime(DateTimeZone.UTC);
    	DateTimeZone tz = DateTimeZone.forID("America/Los_Angeles");
    	DateTime losAngelesDateTime = utc.toDateTime(tz);
    	
    	//Format the time to day/month/year
    	DateTimeFormatter fmt = DateTimeFormat.forPattern("dd-MMM-yy");
    	
    	//Print the time
    	System.out.println(fmt.print(losAngelesDateTime));
    	
    	return true;
    }
 
//////////////////////////////////////OLD CODE/////////////////////////////////////////////    
//////////////////////////////////////OLD CODE/////////////////////////////////////////////    
//////////////////////////////////////OLD CODE/////////////////////////////////////////////    
//////////////////////////////////////OLD CODE/////////////////////////////////////////////    

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