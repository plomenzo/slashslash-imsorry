/* List Class: an object class designed to hold the array of items for the list
 * Author: Vincent Koeten
 * Last Modified: 1/23/15
 * 		By: Vincent Koeten
 */

package edu.csupomona.cs480.controller;

import java.util.ArrayList;

public class List 
{
	private ArrayList<Item> list;

	// Basic constructor using an array of Items
	public List(ArrayList<Item> list)
	{
		this.list = list;
	}
	// Constructor to create an empty list
	public List()
	{
		this.list = new ArrayList<Item>();
	}
	
	// add: adds an Item to the list
	// if the array is full creates a new one with more space
	// also keeps alphabetical order
	// returns if successful
	public boolean add(Item item)
	{
		// list is empty add item to beginning
		if(list.isEmpty())
		{
			list.add(item);
			return true;
		}
		
		// list already contains item do not add
		// note: does not compare cases!
		if(list.contains(item))
		{
			return false;
		}
		
		// list does not contain item and is not empty
		// add item to list at index where 
		// alphabetically appropriate
		int index = 0;
		while(((item.getName()).toUpperCase()).compareTo(((list.get(index)).getName()).toUpperCase()) >= 0)
		{
			index++;
		}
		list.add(index, item);
		return true;
	}
	
	// remove: removes an Item from the list
	// keeps alphabetical order
	// if item is not in list do nothing
	// returns if successful
	public boolean remove(Item item)
	{
		return list.remove(item);
	}
	
	// returns an Item[] of the list in alphabetical order
	public Item[] getList()
	{
		Item[] i = new Item[list.size()];
		i = list.toArray(i);
		return i;
	}
	
	
	
	
}
