package com.example.helloworld;

public class MyEventClass extends java.util.EventObject {
    //here's the constructor
    public MyEventClass(Object source) {
        super(source);
    }
}