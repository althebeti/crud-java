package org.example;

import com.sun.net.httpserver.HttpServer;
import org.example.config.DataSource;
import org.example.controller.PersonController;

import java.io.IOException;
import java.net.InetSocketAddress;


public class Main {
    public static void main(String[] args) {
        DataSource.initialize();
        HttpServer server;
        try {
            server = HttpServer.create(new InetSocketAddress(8080), 0);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        server.createContext("/persons", new PersonController(DataSource.getConnection()));
        server.start();

        System.out.println("Server started on port 8080...");
    }
}