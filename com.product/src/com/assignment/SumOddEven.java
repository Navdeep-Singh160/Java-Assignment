package com.assignment;

public class SumOddEven {
	void findSum(int number1,int number2) {
		int sum= number1+number2 ;
		
		System.out.println("Sum of two Numbers:"+sum);
		
		if(sum%2 ==0) {
			System.out.println("Sum is EVEN");
		}
		else {
			System.out.println("Sum is ODD");
			
		}
		
	}
	public static void main(String[] args) {
		SumOddEven s= new SumOddEven();
		s.findSum(10,50 );
		
	}

}
