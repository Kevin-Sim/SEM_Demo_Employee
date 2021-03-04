package com.napier.sem;

/**
 * Represents an employee
 */
public class Employee
{
    /**
     * Employee number
     */
    private int emp_no;

    /**
     * Employee's first name
     */
    private String first_name;

    /**
     * Employee's last name
     */
    private String last_name;

    /**
     * Employee's job title
     */
    private String title;

    /**
     * Employee's salary
     */
    private int salary;

    /**
     * Employee's current department
     */
    private String dept_name;

    /**
     * Employee's manager
     */
    private String manager;


    public Employee() {

    }

    public int getEmp_no() {
        return emp_no;
    }

    public void setEmp_no(int emp_no) {
        this.emp_no = emp_no;
    }

    public String getFirst_name() {
        return first_name;
    }

    public void setFirst_name(String first_name) {
        this.first_name = first_name;
    }

    public String getLast_name() {
        return last_name;
    }

    public void setLast_name(String last_name) {
        this.last_name = last_name;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getSalary() {
        return salary;
    }

    public void setSalary(int salary) {
        this.salary = salary;
    }

    public String getDept_name() {
        return dept_name;
    }

    public void setDept_name(String dept_name) {
        this.dept_name = dept_name;
    }

    public String getManager() {
        return manager;
    }

    public void setManager(String manager) {
        this.manager = manager;
    }

    @Override
    public String toString() {
        return "Employee{" +
                "emp_no=" + emp_no +
                ", first_name='" + first_name + '\'' +
                ", last_name='" + last_name + '\'' +
                ", title='" + title + '\'' +
                ", salary=" + salary +
                ", dept_name='" + dept_name + '\'' +
                ", manager='" + manager + '\'' +
                '}';
    }

    public String toMarkdown() {
        String str = "";
        str += "|" + emp_no + "|" + first_name + "|" + last_name + "|" +  title + "|" +
        salary + "|" +  dept_name + "|";
        return  str;
    }
}