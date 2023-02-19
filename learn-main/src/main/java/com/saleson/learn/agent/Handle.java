package com.saleson.learn.agent;

public class Handle {
    public void invoke() {
        System.out.println("com.saleson.learn.java.agent.Handle.invoke....");
    }

    public void call() {
        System.out.println("com.saleson.learn.java.agent.Handle.call....");
    }

    public String hello(){
        return "hello ";
    }
}
