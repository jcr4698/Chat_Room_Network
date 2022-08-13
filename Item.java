/*
 * FinalProject_Server Item.java
 * EE422C Final Project submission by
 * Replace <...> with your actual data.
 * Jan Rubio
 * jcr4698
 * 17125
 * Slip days used: <0>
 * Spring 2021
 */

package server;

public class Item {
	String description;
	Double price;
	Double limit;
	// date and time requirements later
	
	public Item(String description, Double price, Double limit) {
		this.description = description;
		this.price = price;
		this.limit = limit;
	}
}
