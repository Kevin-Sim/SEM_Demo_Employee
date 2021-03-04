package com.napier.sem;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.web.bind.annotation.*;

@SpringBootApplication
@RestController
public class App {
    /**
     * Connection to MySQL database.
     */
    private static Connection con = null;

    private static int databaseDelay = 30000;//30000 30 secs

    public static void main(String[] args) throws InterruptedException, IOException {
        connect();
        ConfigurableApplicationContext ctx = SpringApplication.run(App.class, args);
        System.out.println("http://localhost:8080/allemployees");
        System.out.println("http://localhost:8080/getEmployeesByDept?department=Development");
        System.out.println("http://localhost/employees.html");
        /*
         * Travis Deploy Call all the reports and close the app so that the build finishes
         */
        String command = "curl http://app:8080/getEmployeesByDept?department=Development";// inside docker use app locally use localhost
        //probably best to remove inheritIO to minimise travis log
        ProcessBuilder processBuilder = new ProcessBuilder(command.split(" "));//.inheritIO();
        processBuilder.start();
        //let process run then close spring app so that travis exits build
        Thread.sleep(30000);
        ctx.close();
        System.out.println("app closed");
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

    @RequestMapping("getEmployeesByDept")
    public ArrayList<Employee> getEmployeesByDept(@RequestParam(value = "department") String department) {
        try {
            System.out.println("Request for all employees in " + department);
            String query = "select employees.emp_no, first_name, last_name, t.title, s.salary, d.dept_name from employees\n" +
                    "    join titles t on employees.emp_no = t.emp_no\n" +
                    "    join salaries s on employees.emp_no = s.emp_no\n" +
                    "    join dept_emp de on employees.emp_no = de.emp_no\n" +
                    "    join departments d on d.dept_no = de.dept_no\n" +
                    "where t.to_date = '9999-1-1' AND s.to_date = '9999-1-1' and d.dept_name = '" + department + "' " +
                    "order by s.salary desc limit 500";
            PreparedStatement stmt = con.prepareStatement(query);
//            stmt.setString(1, department);

            // Execute SQL statement
            ResultSet rset = stmt.executeQuery();
            // Return new employee if valid.
            // Check one is returned
            ArrayList<Employee> employees = new ArrayList<>();
            while (rset.next()) {
                Employee emp = new Employee();
                emp.setEmp_no(rset.getInt("employees.emp_no"));
                emp.setFirst_name(rset.getString("first_name"));
                emp.setLast_name(rset.getString("last_name"));
                emp.setTitle(rset.getString("t.title"));
//                emp.setSalary(rset.getInt("s.salary"));
                emp.setDept_name(rset.getString("d.dept_name"));
//                emp.setManager(rset.getString("manager_firstname") + " " + rset.getString("manager_lastname"));
                employees.add(emp);

            }
            System.out.println("returned " + employees.size() + " details");
            printEmployeeReport(employees, "All Employees in Department " + department, "employees" + department + ".md");
            return employees;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.out.println("Failed to get employee details");
            return null;
        }


    }

    @RequestMapping("allemployees")
    public ArrayList<Employee> getEmployees() {
        System.out.println("Request for all employees");
        try {
            // Create an SQL statement
            //Statement stmt = con.createStatement();
            // Create string for SQL statement

            //could break this down into two SQL statements to retrieve employee details using joins
            // then another query to get the manager

            String strSelect = "SELECT * from employees " +
                    "join titles on employees.emp_no = titles.emp_no limit 500;";

            PreparedStatement stmt = con.prepareStatement(strSelect);
//            stmt.setInt(1, Integer.parseInt(ID));

            // Execute SQL statement
            ResultSet rset = stmt.executeQuery();
            // Return new employee if valid.
            // Check one is returned
            ArrayList<Employee> employees = new ArrayList<>();
            while (rset.next()) {
                Employee emp = new Employee();
                emp.setEmp_no(rset.getInt("emp_no"));
                emp.setFirst_name(rset.getString("first_name"));
                emp.setLast_name(rset.getString("last_name"));
                emp.setTitle(rset.getString("titles.title"));
//                emp.setSalary(rset.getInt("s.salary"));
//                emp.setDept_name(rset.getString("dep.dept_name"));
//                emp.setManager(rset.getString("manager_firstname") + " " + rset.getString("manager_lastname"));

                employees.add(emp);
            }
            System.out.println("returned " + employees.size() + " details");
            printEmployeeReport(employees, "All Employees (Truncated)", "employees.md");
            return employees;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.out.println("Failed to get employees details");
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
                Thread.sleep(databaseDelay);

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


    public static void printEmployeeReport(ArrayList<Employee> employees, String heading, String filename) {
        StringBuilder sb = new StringBuilder();
        sb.append("# " + heading + "\r\n\r\n");
        sb.append("| emp_no | First Name | Surname | Title | Salary | Department |\r\n");
        sb.append("| :--- | :--- | :--- | :--- | :--- | :--- |\r\n");
        for (Employee employee : employees) {
            sb.append(employee.toMarkdown() + "\r\n");
        }
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(new File(filename)));
            writer.write(sb.toString());
            writer.close();
            System.out.println("Successfully output " + employees.size() + " results to " + filename);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}