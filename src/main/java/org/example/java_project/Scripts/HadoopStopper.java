package org.example.java_project.Scripts;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class HadoopStopper {

    public static void main(String[] args) {
        try {
            // Define the script file path in WSL format
            String scriptPath = "/home/amine/scripts/stop_hadoop.sh";

            // Create a process builder to run the script with bash in WSL
            ProcessBuilder processBuilder = new ProcessBuilder("wsl", "bash", scriptPath);

            // Start the process
            Process process = processBuilder.start();

            // Read the output from the script
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }
            }

            // Read the error output from the script
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.err.println(line);
                }
            }

            // Wait for the process to complete
            int exitCode = process.waitFor();
            System.out.println("Script exited with code: " + exitCode);

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
