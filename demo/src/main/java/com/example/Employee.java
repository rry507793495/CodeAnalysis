package com.example;
import java.io.IOException;
import java.io.Serializable;

public class Employee implements Serializable {

    private String employeeName;

    public void foo() {

    }

    public void testFunc() {
        foo();
    }

    public final Object readObject() throws IOException, ClassNotFoundException {
        if (employeeName == "3") {
            testFunc();
        } else {
            foo();
        }
        testFunc();
        User rry = new User();
        rry.getName();
        System.out.println(employeeName + " is working ！");
        return 1;
    }
}
