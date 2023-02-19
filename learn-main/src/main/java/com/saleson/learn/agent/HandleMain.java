package com.saleson.learn.agent;

import java.util.Objects;
import java.util.Scanner;

public class HandleMain {
    public static void main(String[] args) throws Exception {
        Handle handle = new Handle();
        Scanner scanner = new Scanner(System.in);
        while (true) {
            String txt = scanner.next();
            if (Objects.equals("exit", txt)) {
                break;
            }
            handle.invoke();
            handle.call();
            System.out.println("com.saleson.learn.java.agent.Handle.hello():" + handle.hello());
        }
    }
}
