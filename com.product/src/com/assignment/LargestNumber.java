package com.assignment;

public class LargestNumber {
	void findLargest(int number1,int number2) {
		if(number1>number2) {
			System.out.println("Largest Number:"+number1);
			}
		
		else {
			System.out.println("Largest Number:"+number2);
		}
		
	}
	public static void main(String[] args) {
		LargestNumber l= new LargestNumber();
		l.findLargest(10,20);
		
	}

}
