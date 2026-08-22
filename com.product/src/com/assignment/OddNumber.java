package com.assignment;

public class OddNumber {
	void checkOdd(int number){
		if(number%2 !=0) {
			System.out.println("Number is Odd");
			}
		else {
			System.out.println("Number is Even"); // or Number is not Odd
		}
	}
	public static void main (String[]args) {
		
		OddNumber o=new OddNumber();
		o.checkOdd(1);
	}

}
