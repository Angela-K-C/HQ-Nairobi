package com.example.drinksproject.rmi.shared;

public class RMIConfig {
    public static final String HOST = "10.195.77.192"; // Replace with your server IP
    public static final int PORT = 1099;

    public static String getURL(String serviceName) {
        return String.format("rmi://%s:%d/%s", HOST, PORT, serviceName);
    }
}
