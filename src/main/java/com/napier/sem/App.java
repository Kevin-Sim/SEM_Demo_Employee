package com.napier.sem;

import java.sql.*;
import java.util.ArrayList;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;

@SpringBootApplication
@RestController
public class App {
    /**
     * Connection to MySQL database.
     */
    private static Connection con = null;


    public static void main(String[] args) {
        connect();
        SpringApplication.run(App.class, args);
        System.out.println("http://localhost:8080/employee?id=10021");
        System.out.println("http://localhost/employees?id=10021");
    }

    @RequestMapping("employee")
    public ArrayList<Employee> getEmployee(@RequestParam(value = "id") String ID) {
        System.out.println("Request for " + ID);
        try {
            // Create an SQL statement
            //Statement stmt = con.createStatement();
            // Create string for SQL statement

            //could break this down into two SQL statements to retrieve employee details using joins
            // then another query to get the manager

            String strSelect = "SELECT * from employees " +
                    "join titles on employees.emp_no = titles.emp_no " +
                    "WHERE employees.emp_no = ?;";

            PreparedStatement stmt = con.prepareStatement(strSelect);
            stmt.setInt(1, Integer.parseInt(ID));

            // Execute SQL statement
            ResultSet rset = stmt.executeQuery();
            // Return new employee if valid.
            // Check one is returned
            if (rset.next()) {
                Employee emp = new Employee();
                emp.setEmp_no(rset.getInt("emp_no"));
                emp.setFirst_name(rset.getString("first_name"));
                emp.setLast_name(rset.getString("last_name"));
                emp.setTitle(rset.getString("titles.title"));
//                emp.setSalary(rset.getInt("s.salary"));
//                emp.setDept_name(rset.getString("dep.dept_name"));
//                emp.setManager(rset.getString("manager_firstname") + " " + rset.getString("manager_lastname"));
                ArrayList<Employee> employees = new ArrayList<>();
                employees.add(emp);
                return employees;
            } else
                return null;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.out.println("Failed to get employee details");
            return null;
        }
    }

    /**
     * Connect to the MySQL database.
     */
    public static void connect() {
        try {
            // Load Database driver
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println("Could not load SQL driver");
            System.exit(-1);
        }

        int retries = 10;
        for (int i = 0; i < retries; ++i) {
            System.out.println("Connecting to database...");
            try {
                // Wait a bit for db to start needed for travis but can be removed locally if db running
                Thread.sleep(1000);

//                Connect to database locally
//                con = DriverManager.getConnection("jdbc:mysql://localhost:33060/employees?useSSL=true", "root", "example");

//              Connect to database inside docker
                con = DriverManager.getConnection("jdbc:mysql://db:3306/employees?useSSL=false", "root", "example");

                System.out.println("Successfully connected");
                break;
            } catch (SQLException sqle) {
                System.out.println("Failed to connect to database attempt " + Integer.toString(i));
                System.out.println(sqle.getMessage());
            } catch (InterruptedException ie) {
                System.out.println("Thread interrupted? Should not happen.");
            }
        }
    }

    /**
     * Disconnect from the MySQL database.
     */
    public static void disconnect() {
        if (con != null) {
            try {
                // Close connection
                con.close();
            } catch (Exception e) {
                System.out.println("Error closing connection to database");
            }
        }
    }


//    public void displayEmployee(Employee emp) {
//        if (emp != null) {
//            System.out.println(
//                    emp.emp_no + " "
//                            + emp.first_name + " "
//                            + emp.last_name + "\n"
//                            + emp.title + "\n"
//                            + "Salary:" + emp.salary + "\n"
//                            + emp.dept_name + "\n"
//                            + "Manager: " + emp.manager + "\n");
//        }
//    }
}