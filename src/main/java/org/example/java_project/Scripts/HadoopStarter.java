package org.example.java_project.Scripts;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class HadoopStarter {

    public static void main(String[] args) {
        try {
            // Define the script file
            String scriptPath = "/home/amine/scripts/start_hadoop.sh"; // Replace with the actual path to your script

            // Create a process builder
            ProcessBuilder processBuilder = new ProcessBuilder(scriptPath);

            // Set the working directory if needed (e.g., where the script resides)
            processBuilder.directory(new File("/path/to/")); // Replace with the actual directory

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

