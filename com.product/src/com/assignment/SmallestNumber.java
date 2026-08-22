package com.assignment;

public class SmallestNumber {
	void findSmallest(int number1,int number2) {
		if(number1>number2) {
			System.out.println("Smallest Number:"+number2);
			
		}
		else {
			System.out.println("Smallest Number:"+number1);
		}
		
	}
	public static void main(String[] args) {
		
		SmallestNumber s=new SmallestNumber();
		s.findSmallest(3,10);
		
				
	}

}
