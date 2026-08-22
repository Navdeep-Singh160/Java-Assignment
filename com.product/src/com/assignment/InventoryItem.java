package com.assignment;

public class InventoryItem {
	
	static int totalItems =0;
	String itemName;
	int quantity;
	
	InventoryItem(String itemName,int quantity){
		this.itemName=itemName;
		this.quantity=quantity;
		totalItems += quantity;
		
		
		
	}
	 void addToInventory(int amount) {
	        quantity += amount;
	        totalItems += amount;
	    }

	    
	    void removeFromInventory(int amount) {
	        quantity -= amount;
	        totalItems -= amount;
	    }

	    
	    static int getTotalItems() {
	        return totalItems;
	    }
	

}
