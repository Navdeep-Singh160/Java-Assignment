package com.assignment;

public class EvenOdd {
	void checkEvenOdd(int number ) {
		if(number%2 ==0) {
			System.out.println("Number is even");
			}
		else {
			System.out.println("Number is Odd");
		}
		
	}
		
		
	
	public static void main(String[]args) {
		EvenOdd eo=new EvenOdd();
		eo.checkEvenOdd(5);
		}
}



