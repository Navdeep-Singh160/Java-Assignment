package com.assignment;

public class sumOdd {
	void sumOdd(int number1,int number2) {
		
		if(number1%2 !=0 && number2%2 !=0) {
			int sum=number1+number2;
			System.out.println("Sum of two odd numbers:"+sum);
			
		}
		else {
			System.out.println("Both numbers are not odd");
			
		}
		
	}
	public static void main(String [] args) {
		sumOdd s =new sumOdd();
		s.sumOdd(3, 3);
		
	}

}
