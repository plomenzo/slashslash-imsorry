/* List Class: an object class designed to hold the array of items for the list
 * 				as well as the history for the list
 * Author: Vincent Koeten
 * Last Modified: 1/24/15
 * 		By: Vincent Koeten
 */

package edu.csupomona.cs480.controller;

import java.util.ArrayList;

public class List 
{
	private ArrayList<Item> list;
	private ArrayList<String> history;

	// Basic constructor using an array of Items
	public List(ArrayList<Item> list)
	{
		this.list = list;
		history = new ArrayList<String>();
	}
	// Constructor to create an empty list
	public List()
	{
		this.list = new ArrayList<Item>();
		history = new ArrayList<String>();
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
	
	// remove: removes an Item from the list by Item object
	// adds the removed item's name to history list
	// keeps alphabetical order
	// if item is not in list do nothing
	// returns if successful
	public boolean remove(Item item)
	{
		if((list.contains(item)))
		{
			addToHistory(item.getName());
			return list.remove(item);
		}
		return false;
	}
	
	// remove: removes an Item from the list by item name
	// adds the removed item's name to history list
	// keeps alphabetical order
	// if item is not in list do nothing
	// returns if successful
	public boolean remove(String itemName)
	{
		boolean contains = false;
		int index = -1;
		for(int i=0; i<list.size(); i++)
		{
			if(((list.get(i)).getName()).equals(itemName))
			{
				contains = true;
				index = i;
				break;
			}
		}
		if(contains)
		{
			addToHistory(itemName);
			list.remove(index);
			return true;
		}
		return false;
	}
	
	// returns an Item[] of the list in alphabetical order
	public Item[] getList()
	{
		Item[] i = new Item[list.size()];
		i = list.toArray(i);
		return i;
	}
	// returns a String[] of the history in alphabetical order
	public String[] getHistory()
	{
		String[] s = new String[history.size()];
		s = history.toArray(s);
		return s;
	}
	
	// adds an item's name to the history list keeping alphabetical order
	private void addToHistory(String name)
	{
		// history is empty add name to beginning
		if(history.isEmpty())
		{
			history.add(name);
		}
		// history does not contain name and is not empty
		// add name to history at index where alphabetically appropriate
		else if(!(history.contains(name)))
		{
			int index = 0;
			while((name.toUpperCase()).compareTo((history.get(index)).toUpperCase()) >= 0)
			{
				index++;
			}
			history.add(index, name);
		}
	}
}
