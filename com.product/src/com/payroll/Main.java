package com.payroll;

class Employee {

    String employeeName;
    double monthlySalary;
    boolean isHandicaped;
    int hoursWorked;
    int monthNumber;

   
    Employee(String employeeName, double monthlySalary,
             boolean isHandicaped, int hoursWorked, int monthNumber) {

        this.employeeName = employeeName;
        this.monthlySalary = monthlySalary;
        this.isHandicaped = isHandicaped;
        this.hoursWorked = hoursWorked;
        this.monthNumber = monthNumber;
    }

  
    void calculateSalary() {

      
        double hourlySalary = monthlySalary / 160;    // Assumption: 4 weeks and 40 regular hours per week

        double payment;

        if (hoursWorked > 40) {

            int overtimeHours = hoursWorked - 40;

            double regularPayment = 40 * hourlySalary;

            double overtimePayment =
                    overtimeHours * hourlySalary * 1.5;

            payment = regularPayment + overtimePayment;

            
            if (isHandicaped && hoursWorked > 50) {

                double bonus = overtimePayment * 0.10;

                payment = payment + bonus;
            }

        } else {

            payment = hoursWorked * hourlySalary;
        }

        System.out.println("Employee Name: " + employeeName);
        System.out.println("Monthly Salary: Rs. " + monthlySalary);
        System.out.println("Handicapped: " + isHandicaped);
        System.out.println("Hours Worked: " + hoursWorked);
        System.out.println("Month Number: " + monthNumber);
        System.out.println("Weekly Payment: Rs. " + payment);
    }
}


public class Main {

    public static void main(String[] args) {

        Employee employee = new Employee(
                "Akshita",
                80000,
                false,
                40,
                9
        );

        employee.calculateSalary();
    }
}
