package com.example.demo;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.http.HttpHeaders;
import java.util.Collections;

@SpringBootApplication
public class Main implements CommandLineRunner {

    // You need to fill in your personal information here
    private static final String YOUR_NAME = "Nishant Jhade";
    private static final String YOUR_REG_NO = "0536CS221032";
    private static final String YOUR_EMAIL = "jhadenishant@gmail.com";

    // URLs for the API endpoints
    private static final String GENERATE_WEBHOOK_URL = "https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook/JAVA";
    private static final String SUBMIT_SOLUTION_URL = "https://bfhldevapigw.healthrx.co.in/hiring/testWebhook/JAVA";

    // Question: Write a query to find the department with the highest average salary.
    private static final String FINAL_SQL_QUERY = "SELECT T1.EMP_ID, T1.FIRST_NAME, T1.LAST_NAME, D.DEPARTMENT_NAME, COUNT(T2.EMP_ID) AS YOUNGER_EMPLOYEES_COUNT FROM EMPLOYEE AS T1 JOIN EMPLOYEE AS T2 ON T1.DEPARTMENT = T2.DEPARTMENT JOIN DEPARTMENT AS D ON T1.DEPARTMENT = D.DEPARTMENT_ID WHERE T1.DOB > T2.DOB GROUP BY T1.EMP_ID, T1.FIRST_NAME, T1.LAST_NAME, D.DEPARTMENT_NAME ORDER BY T1.EMP_ID DESC;";
    
    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    @Override
    public void run(String... args) {
        RestTemplate restTemplate = new RestTemplate();
        ObjectMapper objectMapper = new ObjectMapper();

        System.out.println("Step 1: Generating webhook and access token...");

        try {
            // Create the request body for the first API call
            GenerateWebhookRequest requestBody = new GenerateWebhookRequest(YOUR_NAME, YOUR_REG_NO, YOUR_EMAIL);
            
            // Set up headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            // Create the HTTP entity
            HttpEntity<GenerateWebhookRequest> entity = new HttpEntity<>(requestBody, headers);

            // Make the POST request
            ResponseEntity<GenerateWebhookResponse> response = restTemplate.postForEntity(
                GENERATE_WEBHOOK_URL, entity, GenerateWebhookResponse.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                GenerateWebhookResponse responseBody = response.getBody();
                if (responseBody != null) {
                    System.out.println("Webhook and Access Token received successfully.");
                    System.out.println("Webhook URL: " + responseBody.getWebhookUrl());
                    System.out.println("Access Token: " + responseBody.getAccessToken());

                    // Proceed to step 2 & 3: Submit the SQL solution
                    submitSolution(restTemplate, responseBody.getWebhookUrl(), responseBody.getAccessToken());
                } else {
                    System.err.println("Received a successful status code but the response body was empty.");
                }
            } else {
                System.err.println("Failed to generate webhook. Status code: " + response.getStatusCode());
            }

        } catch (Exception e) {
            System.err.println("An error occurred during API communication: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void submitSolution(RestTemplate restTemplate, String webhookUrl, String accessToken) {
        System.out.println("\nStep 2 & 3: Submitting the SQL solution...");
        try {
            // Create the request body for submitting the solution
            SubmitSolutionRequest requestBody = new SubmitSolutionRequest(FINAL_SQL_QUERY);

            // Set up headers with the JWT token
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            headers.setBearerAuth(accessToken);

            // Create the HTTP entity
            HttpEntity<SubmitSolutionRequest> entity = new HttpEntity<>(requestBody, headers);

            // --- Debugging logs added here ---
            System.out.println("Submitting request to: " + SUBMIT_SOLUTION_URL);
            System.out.println("Headers: " + headers);
            System.out.println("Body: " + requestBody.getFinalQuery());
            // ---------------------------------

            // Make the POST request to the webhook URL
            ResponseEntity<String> response = restTemplate.postForEntity(
                SUBMIT_SOLUTION_URL, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                System.out.println("SQL solution submitted successfully!");
                System.out.println("Response from server: " + response.getBody());
            } else {
                System.err.println("Failed to submit solution. Status code: " + response.getStatusCode());
                System.err.println("Response body: " + response.getBody());
            }

        } catch (Exception e) {
            System.err.println("An error occurred while submitting the solution: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Class for the generateWebhook request body
    private static class GenerateWebhookRequest {
        private final String name;
        private final String regNo;
        private final String email;

        public GenerateWebhookRequest(String name, String regNo, String email) {
            this.name = name;
            this.regNo = regNo;
            this.email = email;
        }

        public String getName() {
            return name;
        }

        public String getRegNo() {
            return regNo;
        }

        public String getEmail() {
            return email;
        }
    }

    // Class for the generateWebhook response body
    private static class GenerateWebhookResponse {
        @JsonProperty("webhookURL")
        private String webhookUrl;
        @JsonProperty("accessToken")
        private String accessToken;

        public String getWebhookUrl() {
            return webhookUrl;
        }

        public String getAccessToken() {
            return accessToken;
        }
    }

    // Class for the submitSolution request body
    private static class SubmitSolutionRequest {
        private final String finalQuery;

        public SubmitSolutionRequest(String finalQuery) {
            this.finalQuery = finalQuery;
        }

        public String getFinalQuery() {
            return finalQuery;
        }
    }
}
