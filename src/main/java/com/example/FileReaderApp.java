package com.example;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class FileReaderApp {
    public static void main(String[] args) {
        String filePath = System.getProperty("user.dir") + "/project.txt";
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
            }
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
    }
}