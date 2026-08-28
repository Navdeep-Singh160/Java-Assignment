package com.conststatic;

class BankAccount {

    String accountNumber;
    String accountHolder;
    double balance;

    static String bankName;

   
    BankAccount(String accountNumber, String accountHolder, double balance) {
        this.accountNumber = accountNumber;
        this.accountHolder = accountHolder;
        this.balance = balance;
    }

    
    BankAccount() {
        accountNumber = "Unknown";
        accountHolder = "Unknown";
        balance = 0.0;
    }

  
    void deposit(double amount) {
        balance = balance + amount;
    }

   
    void withdraw(double amount) {
        if (amount <= balance) {
            balance = balance - amount;
        } else {
            System.out.println("Insufficient balance");
        }
    }

   
    void printAccountDetails() {
        System.out.println("Bank: " + bankName);
        System.out.println("Account Number: " + accountNumber);
        System.out.println("Account Holder: " + accountHolder);
        System.out.println("Balance: " + balance);
    }

  
    static void setBankName(String name) {
        bankName = name;
    }
}


class Employee {

    String employeeId;
    String employeeName;
    double salary;

    static String companyName;
    static int employeeCount;

    
    Employee(String employeeId, String employeeName, double salary) {
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.salary = salary;
        employeeCount++;
    }

    
    Employee() {
        employeeId = "Unknown";
        employeeName = "Unknown";
        salary = 0.0;
        employeeCount++;
    }

  
    void raiseSalary(double amount) {
        salary = salary + amount;
    }

    
    void printEmployeeDetails() {
        System.out.println("Company: " + companyName);
        System.out.println("Employee ID: " + employeeId);
        System.out.println("Employee Name: " + employeeName);
        System.out.println("Salary: " + salary);
    }

    
    static void setCompanyName(String name) {
        companyName = name;
    }

    
    static void printEmployeeCount() {
        System.out.println("Total Employees: " + employeeCount);
    }

 
    static void printAllEmployees(Employee[] employees) {
        employees[0].printEmployeeDetails();
        employees[1].printEmployeeDetails();
        employees[2].printEmployeeDetails();
    }
}


class Product {

    String productId;
    String productName;
    double price;
    int quantity;

    static String storeName;
    static int totalProducts;

    
    Product(String productId, String productName, double price, int quantity) {
        this.productId = productId;
        this.productName = productName;
        this.price = price;
        this.quantity = quantity;
        totalProducts++;
    }

    
    Product() {
        productId = "Unknown";
        productName = "Unknown";
        price = 0.0;
        quantity = 0;
        totalProducts++;
    }

    
    void addStock(int amount) {
        quantity = quantity + amount;
    }

 
    void sellProduct(int amount) {
        if (amount <= quantity) {
            quantity = quantity - amount;
        } else {
            System.out.println("Not enough stock");
        }
    }

   
    void printProductDetails() {
        System.out.println("Store: " + storeName);
        System.out.println("Product ID: " + productId);
        System.out.println("Product Name: " + productName);
        System.out.println("Price: " + price);
        System.out.println("Quantity: " + quantity);
    }

  
    static void setStoreName(String name) {
        storeName = name;
    }

    
    static void printTotalProducts() {
        System.out.println("Total Products: " + totalProducts);
    }

   
    static void printAllProducts(Product[] products) {
        products[0].printProductDetails();
        products[1].printProductDetails();
        products[2].printProductDetails();
    }
}


public class Main {

    public static void main(String[] args) {

        

        BankAccount.setBankName("HDFC Bank");

        BankAccount account1 =
                new BankAccount("ACC101", "Aman Sharma", 10000);

        BankAccount account2 = new BankAccount();

        account1.deposit(2000);
        account1.withdraw(1500);

        account1.printAccountDetails();

        System.out.println();

        account2.printAccountDetails();


        System.out.println();


     

        Employee.setCompanyName("Infosys");

        Employee employee1 =
                new Employee("E01", "Rahul Mehta", 45000);

        Employee employee2 =
                new Employee("E02", "Priya Kapoor", 55000);

        Employee employee3 = new Employee();

        employee1.raiseSalary(5000);

        Employee[] employees = {employee1, employee2, employee3};

        Employee.printAllEmployees(employees);

        Employee.printEmployeeCount();


        System.out.println();


    

        Product.setStoreName("Fresh Mart");

        Product product1 =
                new Product("P01", "Laptop", 65000, 10);

        Product product2 =
                new Product("P02", "Headphones", 2500, 25);

        Product product3 = new Product();

        product1.addStock(5);
        product2.sellProduct(3);

        Product[] products = {product1, product2, product3};

        Product.printAllProducts(products);

        Product.printTotalProducts();
    }
}

