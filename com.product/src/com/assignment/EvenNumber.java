package com.assignment;

public class EvenNumber {
	void checkNumber(int number){
		
		if(number%2 ==0) {
			System.out.println("Even Number");
			}
		else {
			System.out.println("Odd Number");
		}
		
}
	public static void main(String[] args) {
		
		EvenNumber e =new EvenNumber();
		e.checkNumber(5);
				
	}
}
