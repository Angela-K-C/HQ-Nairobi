package com.example.drinksproject.rmi.server;

import com.example.drinksproject.rmi.shared.*;

import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;

public class RMIServer {
    public static void main(String[] args) {
        try {
            // Start RMI registry on the specified port
            LocateRegistry.createRegistry(RMIConfig.PORT);
            System.out.println("🔌 RMI Registry started on port " + RMIConfig.PORT);

            // Bind services using consistent config
            Naming.rebind(RMIConfig.getURL("CustomerService"), new CustomerServiceImpl());
            System.out.println("✅ CustomerService bound.");

            Naming.rebind(RMIConfig.getURL("OrderService"), new OrderServiceImpl());
            System.out.println("✅ OrderService bound.");

            Naming.rebind(RMIConfig.getURL("DrinkService"), new DrinkServiceImpl());
            System.out.println("✅ DrinkService bound.");

            Naming.rebind(RMIConfig.getURL("LoginService"), new LoginServiceImpl());
            System.out.println("✅ LoginService bound.");

            Naming.rebind(RMIConfig.getURL("StockService"), new StockServiceImpl());
            System.out.println("✅ StockService bound to RMI registry.");

            Naming.rebind(RMIConfig.getURL("BranchService"), new BranchServiceImpl());
            System.out.println("✅ StockService bound to RMI registry.");

            System.out.println("🚀 RMI Server is up at: " + RMIConfig.getURL("AnyService"));
        } catch (Exception e) {
            System.err.println("❌ Failed to start RMI server:");
            e.printStackTrace();
        }
    }
}














//package com.example.drinksproject.rmi.server;
//
//import java.rmi.Naming;
//import java.rmi.registry.LocateRegistry;
//
//import com.example.drinksproject.rmi.shared.CustomerService;
//import com.example.drinksproject.rmi.shared.DrinkService;
//import com.example.drinksproject.rmi.shared.LoginService;
//import com.example.drinksproject.rmi.shared.OrderService;
//
//public class RMIServer {
//    public static void main(String[] args) {
//        try {
//            // Start RMI registry on port 1099 (only if not already started)
//            LocateRegistry.createRegistry(1099);
//
//            // Bind CustomerService
//            CustomerService customerService = new CustomerServiceImpl();
//            Naming.rebind("rmi://localhost/CustomerService", customerService);
//            System.out.println("✅ CustomerService bound.");
//
//            // Bind OrderService
//            OrderService orderService = new OrderServiceImpl();
//            Naming.rebind("rmi://localhost/OrderService", orderService);
//            System.out.println("✅ OrderService bound.");
//
//            DrinkService drinkService = new DrinkServiceImpl();
//            Naming.rebind("rmi://localhost/DrinkService", drinkService);
//            System.out.println("✅ DrinkService bound.");
//
//            LoginService loginService = new LoginServiceImpl();
//            Naming.rebind("rmi://localhost/LoginService", loginService);
//            System.out.println("✅ LoginService bound.");
//
//            System.out.println("🚀 RMI Server running successfully.");
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//}
