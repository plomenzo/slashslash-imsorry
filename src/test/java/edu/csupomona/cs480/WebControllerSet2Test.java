package edu.csupomona.cs480;

import static org.junit.Assert.*;

import java.net.UnknownHostException;

import org.junit.Before;
import org.junit.Test;

import com.mongodb.DBObject;

import edu.csupomona.cs480.controller.WebController;


public class WebControllerSet2Test 
{
    
	private WebController web;
	
	@Before
	public void setup() throws Exception
	{
		web = WebController.getInstance();
	}
	/*
	@Test
	public void testDeleteList()
	{   
		boolean result = web.deleteList("54d6631c0fdcf8e36aa174d8");
		assertTrue(result);
	}
	*/
	@Test
	public void testUserExists() throws UnknownHostException
	{
		web.createUser("test123","a");
		boolean result = web.userExists("test123");
		assertTrue(result);
	}
}