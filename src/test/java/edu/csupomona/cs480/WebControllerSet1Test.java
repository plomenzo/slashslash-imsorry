package edu.csupomona.cs480;

import static org.junit.Assert.*;

import java.net.UnknownHostException;

import org.junit.Before;
import org.junit.Test;

import com.mongodb.DBObject;

import edu.csupomona.cs480.controller.WebController;


public class WebControllerSet1Test 
{
    
	private WebController web;
	
	@Before
	public void setup() throws UnknownHostException
	{
		web = new WebController();
	}
	
	@Test
	public void testDeleteItem()
	{   //Note that right now it does not matter if the user
		//Tries to delete an item that is not on the list
		boolean result = web.removeItem("54d6625a0fdcf0e2679092ad", "apple");
		assertTrue(result);
	}
	
	@Test
	public void testGetList()
	{ //Should be non null since list is present
		DBObject result = web.getList("54d6625a0fdcf0e2679092ad");
		assertNotNull(result);
		//Should be null as this is an invalid list ID
		result = web.getList("ase");
		assertNull(result);
	}
	
	@Test
	public void testAuthenticate()
	{ 
		//Authenticate an existing user  
		String result = web.authenticate("testUser","testPassword");
		
		assertNotNull(result);
		assertEquals(result, "54cebe2117ef75cddfb06a36");
		
		//Authenticate a non-existing user  
		result = web.authenticate("gibberish","testPassword");
				
		assertNotNull(result);
		assertEquals(result, "Login Failed");
			
	}
	
	
	
}
