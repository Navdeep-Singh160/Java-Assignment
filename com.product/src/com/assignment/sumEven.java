package com.assignment;

public class sumEven {
	void sumEven(int number1,int number2) {
		
		if(number1%2==0 && number2%2==0) {
			int sum= number1+number2 ;
			System.out.println("Sum of two even numbers:"+sum);
			}
		else {
			System.out.println("Both numbers are not even");
		}
		
	}
	public static void main(String[] args) {
		sumEven s= new sumEven();
		s.sumEven(10,30);
	}

}
