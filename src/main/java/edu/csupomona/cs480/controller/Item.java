/* Item Class: an object class designed to hold the instance variables for an item
 * Author: Vincent Koeten
 * Last Modified: 1/24/15
 * 		By: Vincent Koeten
 */

package edu.csupomona.cs480.controller;

public class Item 
{
	private String name;
	private int quantity;
	private double price;
	private String createdByUser;
	
	// Basic constructor using all the variables
	public Item(String name, int quantity, double price, String createdByUser)
	{
		this.name = name;
		this.quantity = quantity;
		this.price = price;
		this.createdByUser = createdByUser;
	}
	// Basic constructor excluding the price; price is set to -1
	public Item(String name, int quantity, String createdByUser)
	{
		this.name = name;
		this.quantity = quantity;
		this.price = -1.0; // negative price indicates that user hasn't set a value for it yet
		this.createdByUser = createdByUser;
	}

	//Basic getters for variables
	public String getName()
	{
		return name;
	}
	public int getQuantity()
	{
		return quantity;
	}
	public double getPrice()
	{
		return price;
	}
	public String getCreatedByUser()
	{
		return createdByUser;
	}
	
	// Basic setters for variables
	public void setName(String name)
	{
		this.name = name;
	}
	public void setQuantity(int quantity)
	{
		this.quantity = quantity;
	}
	public void setPrice(double price)
	{
		this.price = price;
	}
	public void setCreatedByUser(String createdByUser)
	{
		this.createdByUser = createdByUser;
	}
	
	// Equals method to test for equality
	// returns true if names of both items are the same
	public boolean equals(Object o)
	{
		if((o instanceof Item) && (this.name.equals(((Item)o).name)))
		{
			return true;
		}
		return false;
	}
	/* needed for sorting
	public boolean equals(Object o) 
	{
		  if (!(o instanceof Item)) {
		    return false;
		  }
		  Item other = (Item) o;
		  return name.equalsIgnoreCase(other.getName());
	}
	public int hashCode() 
	{
		  return name.hashCode();
	}
	*/
}
