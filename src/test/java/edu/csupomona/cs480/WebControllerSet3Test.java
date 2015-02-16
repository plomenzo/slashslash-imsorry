package edu.csupomona.cs480;

import static org.junit.Assert.*;

import java.net.UnknownHostException;

import org.junit.Before;
import org.junit.Test;

import com.mongodb.DBObject;

import edu.csupomona.cs480.controller.WebController;


public class WebControllerSet3Test 
{
    
	private WebController web;
	
	@Before
	public void setup() throws UnknownHostException
	{
		web = new WebController();
	}
	
	@Test
	public void testCommonsMathExample()
	{   //Authenticate an existing user  
		String result = web.commonsMathExample();
		
		assertNotNull(result);
		assertEquals(result, "Array2DRowRealMatrix{{-0.5263157895,0.3473684211},{0.1578947368,-0.0842105263}}");
	}
}
