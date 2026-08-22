package com.assignment;

public class checkNumber {
	void checkConditionr(int number) {
		if(number>0) {
			System.out.println("Positive Number");
		}
		else if(number ==0) {
			System.out.println("Number is Zero");
		}
		
		else {
			System.out.println("Negative Number");
		}
		
	}
	public static void main(String[] args) {
		checkNumber c=new checkNumber();
		c.checkConditionr(20);
	}

}
