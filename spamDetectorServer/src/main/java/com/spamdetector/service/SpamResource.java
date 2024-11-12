package com.spamdetector.service;

import com.spamdetector.domain.TestFile;
import com.spamdetector.util.SpamDetector;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import jakarta.ws.rs.core.Response;

@Path("/spam")
public class SpamResource
{
    //your SpamDetector Class responsible for all the SpamDetecting logic
    //Default variables
    private SpamDetector detector;
    private File dataDir;
    private final ObjectMapper objectMapper;
    private final List<TestFile> testResults;

    public SpamResource() throws FileNotFoundException
    {

//        TODO: load resources, train and test to improve performance on the endpoint calls

        // Initialize the detector
        this.detector = new SpamDetector();
        // Initialize the ObjectMapper
        this.objectMapper = new ObjectMapper();

        // Load resources, train, and test the model
        System.out.println("Training and testing the model, please wait...");
        URL data = this.getClass().getClassLoader().getResource("data/");
        try {
            File dataDir = new File(data.toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

//        TODO: call  this.trainAndTest();
        this.testResults = trainAndTest();
    }
    //Obtain the results of testing our model and return the response object of the results
    @GET
    @Produces("application/json")
    @Path("data")
    public Response getSpamResults() { //Obtain testing results
        return buildResponse(testResults);
    }
    //Obtain our accuracy measurement of our results and return the response object
    @GET
    @Path("/accuracy")
    @Produces("application/json")
    public Response getAccuracy() { //Obtain accuracy measurement
        Double accuracy = detector.getAccuracy(testResults);
        return buildResponse(accuracy); //Build response object for accuracy
    }
    //Obtain our precision measurement of our testing results and return the response object
    @GET
    @Path("/precision")
    @Produces("application/json")
    public Response getPrecision() { //Obtain precision measurements
        Double precision = detector.getPrecision(testResults);
        return buildResponse(precision); //Build response object for precision
    }
    /*Attempt to build a response object.
    All methods above use this code to build their corresponding responses to the server.
    Here we set our data type and CORS headers and build the object.
     */
    private Response buildResponse(Object entity) {
        try {
            return Response.status(200)
                    .header("Access-Control-Allow-Origin", "http://localhost:63342")
                    .header("Content-Type", "application/json")
                    .entity(objectMapper.writeValueAsString(entity))
                    .build();
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize response entity", e);
        }
    }
    //Train and test our model
    private List<TestFile> trainAndTest() throws FileNotFoundException
    {
        if (this.detector==null)
        {
            this.detector = new SpamDetector();
        }
//        TODO: load the main directory "data" here from the Resources folder
        File mainDirectory = dataDir; //Set main directory for training and testing the model
        return this.detector.trainAndTest(mainDirectory); //Train and test the directory
    }
}