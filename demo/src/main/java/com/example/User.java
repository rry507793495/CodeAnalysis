package com.example;

import java.io.IOException;
import java.io.Serializable;

public class User implements Serializable {
    public String name = "rry";
    public String getName() {
        return name;
    }
    // public final Object readObject() throws IOException, ClassNotFoundException {
        
    //     return name;
    // }
}
