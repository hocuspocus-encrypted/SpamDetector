package com.spamdetector.util;

import com.spamdetector.domain.TestFile;

import java.io.*;
import java.net.URL;
import java.util.*;
import java.net.URISyntaxException;


/**
 * TODO: This class will be implemented by you
 * You may create more methods to help you organize you strategy and make you code more readable
 */
public class SpamDetector {

    public List<TestFile> trainAndTest(File mainDirectory) throws FileNotFoundException {
        //TODO: main method of loading the directories and files, training and testing the model

        //Loading directories for training
        URL trainSpam = this.getClass().getClassLoader().getResource("data/train/spam");
        URL trainHam = this.getClass().getClassLoader().getResource("data/train/ham");
        URL trainHam1 = this.getClass().getClassLoader().getResource("data/train/ham2");

        //Loading directories for testing
        URL testSpam = this.getClass().getClassLoader().getResource("data/test/spam");
        URL testHam = this.getClass().getClassLoader().getResource("data/test/ham");

        File trainSpamDir = null;
        File trainHamDir = null;
        File trainHam1Dir = null;
        File testSpamDir = null;
        File testHamDir = null;

        try {
            //Converting directories to files for train
            trainSpamDir = new File(trainSpam.toURI());
            trainHamDir = new File(trainHam.toURI());
            trainHam1Dir = new File(trainHam1.toURI());

            //Converting directories to files for test
            testSpamDir = new File(testSpam.toURI());
            testHamDir = new File(testHam.toURI());

        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        File[] spamFiles = testSpamDir.listFiles();
        File[] hamFiles = testHamDir.listFiles();

        List<TestFile> dataFile = new ArrayList<>();
        //Obtain map from training files

        Map<String, Double> checkMap = getTrainProb(trainSpamDir,trainHamDir,trainHam1Dir);
        //Process spam and ham files
        for (File file : spamFiles)
        {
            String fileName = file.getName();
            double spamProbability = test(file, checkMap);
            String className = "spam";

            TestFile spamFile = new TestFile(fileName, spamProbability, className);
            dataFile.add(spamFile);
        }

        for (File file : hamFiles)
        {
            String fileName = file.getName();
            double spamProbability = test(file, checkMap);
            String className = "ham";

            TestFile hamFile = new TestFile(fileName, spamProbability, className);
            dataFile.add(hamFile);
        }
        return dataFile;
    }
    //Test method which returns probability of individual files being spam in spam and ham folders
    private Double test(File file, Map<String, Double> wordProbabilityMap) throws FileNotFoundException {
        Scanner scanner = new Scanner(file);
        double eta = 0.0;
        while (scanner.hasNext()) {
            String word = scanner.next();

            if (wordProbabilityMap.containsKey(word))
            {
                double probability = wordProbabilityMap.get(word); //Get the probability of a word being spam
                double value1 = Math.log((1.0 - probability)); //Compute logarithms of the probability
                double value2 = Math.log(probability);

                //Prevents calculating log(0), which returns an undefined value
                //So if probability is zero the default value for value2 is also zero
                if(probability == 0.0)
                {
                    value2 = 0.0;
                }
                eta +=  value1 - value2; //Calculate eta
            }
        }
        //After calculating the eta value calculate the probability that the file is spam
        double pr_SF = 1.0 / (1.0 + Math.pow(Math.E, eta));
        scanner.close(); // Make sure to close the scanner manually
        return pr_SF;
    }
//This method returns a map of probabilities based on word frequencies
    private Map<String, Double> getTrainProb(File spamFiles, File hamFiles, File hamFiles2)
    {
        //Create our frequency maps for both spam and ham
        Map<String, Integer> spamFreqMap = getFrequency(spamFiles);
        Map<String, Integer> hamFreqMap = getFrequency(hamFiles);

        //Loop through frequencies obtained from the second set of hamfiles
        for (Map.Entry<String, Integer> entry : getFrequency(hamFiles2).entrySet())
        {
            if (!hamFreqMap.containsKey(entry.getKey())) //If word is not present in map
            {
                hamFreqMap.put(entry.getKey(), entry.getValue()); //Add the word's value and frequency to map
            } else
            {
                int totalFreq = entry.getValue() + hamFreqMap.get(entry.getKey()); //Calculate total frequency
                hamFreqMap.put(entry.getKey(), totalFreq);
            }
        }
        return calcProb(spamFreqMap, hamFreqMap); //Calculate the probability of the updated maps
    }

    /*This method calculates the probabilities of each word being spam or ham,
     and stores and returns a map of these probabilities
     */
    private Map<String, Double> calcProb (Map < String, Integer > spamMap, Map < String, Integer > hamMap)
    {
        //Create a new Treemap type for our training probabilities map
        Map<String, Double> trainProbMap = new TreeMap<>();

        //Obtain counts for spam and ham
        double spamCount = spamMap.size();
        double hamCount = hamMap.size();

        //Equations given by assignment
        //Create a map for the probabilities that a word appears in spam
        Map<String, Double> pr_WiS_Map = new TreeMap<>();
        for (Map.Entry<String, Integer> entry : spamMap.entrySet()) //Loop through map
        {
            //This value is the probability that the word appears in the spam file
            double pr_WiS = entry.getValue() / spamCount;
            pr_WiS_Map.put(entry.getKey(), pr_WiS); //Put it into the map
        }
        //Create a map for the probabilities that a word appears in ham
        Map<String, Double> pr_WiH_Map = new TreeMap<>();
        for (Map.Entry<String, Integer> entry : hamMap.entrySet()) {
            double pr_WiH = entry.getValue() / hamCount; //Probability the word appears in ham
            pr_WiH_Map.put(entry.getKey(), pr_WiH);
        }

        //Calculating probability that word is spam using given formula
        // Iterate over keys of map1
        for (String key : pr_WiS_Map.keySet()) {
            double value1 = pr_WiS_Map.get(key); // Get value from map1 or default to 0
            double value2 = pr_WiH_Map.getOrDefault(key, 0.0); // Get value from map2

            double pr_SWi = value1 / (value1 + value2);

            trainProbMap.put(key, pr_SWi);
        }

        // Iterate over keys of map2 to handle keys not present in map1
        for (String key : pr_WiH_Map.keySet()) {
            if (!pr_WiS_Map.containsKey(key)) {
                double value1 = pr_WiS_Map.getOrDefault(key, 0.0); // Get value from map1 or default to 0
                double value2 = pr_WiH_Map.get(key); // Get value from map2

                double pr_SWi = value1 / (value1 + value2); //Probability a file is spam

                trainProbMap.put(key, pr_SWi); //Put value into map
            }
        }
        return trainProbMap;
    }

//This method calculates the frequency of each word in spam and ham files
    private Map<String, Integer> getFrequency (File trainData)
    {   //Create a new treemap for word frequencies
        Map<String, Integer> FrequencyMap = new TreeMap<>();
        File[] dataFiles = trainData.listFiles(); //Obtain list of files from training data

        for (File dataFile : dataFiles) { //Calls the function below and calculates frequency of words in file
            Map<String, Integer> EmailFrequencyMap = calculateFrequency(dataFile);
            Set<String> words = EmailFrequencyMap.keySet(); //Set word frequencies
            Iterator<String> spamIterator = words.iterator(); //We use this to iterate through the words in the set

            while (spamIterator.hasNext()) {
                String word = spamIterator.next(); //Obtain word
                int wordCount = EmailFrequencyMap.get(word); //Get frequency of word
                if (!FrequencyMap.containsKey(word)) {
                    //Update map with the word and it's count if the word is not already present in the map
                    FrequencyMap.put(word, wordCount);
                }
                else
                {
                    int oldCount = FrequencyMap.get(word);
                    FrequencyMap.put(word, oldCount + wordCount);
                }
            }
        }
        return FrequencyMap; //Return word frequencies of the data set
    }

    //This method calculates the frequency of the words with respect to the files it appears in
    private Map<String, Integer> calculateFrequency (File spamFile){
        Map<String, Integer> FrequencyMap = new TreeMap<>();
        try {
            Scanner scanner = new Scanner(spamFile); //Parse the spam file
            while (scanner.hasNext()) {
                String word = scanner.next();
                word = word.toLowerCase(); //Turn all words to lowercase

                if (isWord(word)) //Add to the frequency map if the word is not already present
                {
                    if (!FrequencyMap.containsKey(word))
                    {
                        FrequencyMap.put(word, 1);
                    }
                }
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        return FrequencyMap; //Return the word frequencies of the individual file
    }

    //Simple method which checks the validity of a word
    private boolean isWord(String word)
    {
        if (word == null || word.isEmpty()) {
            return false;
        } //If it matches this regex format, the word is valid
        String wordPattern = "\\b[A-Za-z]+\\b"; //If it is uppercase or lowercase in the alphabet

        if (word.matches(wordPattern)) {
            return true;
        }
        return false;
    }

    //Function to calculate accuracy (percentage of correct guesses)
    public double getAccuracy(List<TestFile> testFiles)
    {   //Set our default values which are used in the formula
        double truePositives = 0.0;
        double trueNegatives = 0.0;
        double falsePositives = 0.0;
        double falseNegatives = 0.0;

        for(TestFile testfile : testFiles)
        {
            //If the probability a file is spam is >= 0.5 and belongs in the spam files, it is a true spam file
            //Counting truePositives, if it is a true spam
            if(testfile.getSpamProbability() >= 0.5 && testfile.getActualClass().equals("spam"))
            {
                truePositives++;
            }
            //If the probability a file is spam is < 0.5 and belongs in ham files, it is a true ham file
            else if(testfile.getSpamProbability() < 0.5 && testfile.getActualClass().equals("ham"))
            {
                trueNegatives++;
            }//If the probability a file is spam is >= 0.5 but it is in the ham files, it is falsely flagged spam
            else if (testfile.getSpamProbability() >= 0.5 && testfile.getActualClass().equals("ham"))
            {
                falsePositives++;
            }//If the probability a file is spam is < 0.5 but it is in spam files, it is a falseNegative spam
            else if (testfile.getSpamProbability() < 0.5 && testfile.getActualClass().equals("spam"))
            {
                falseNegatives++;
            }
        }
        //Add the number of true positives/negatives and false positives/negatives from above into this total
        double numFiles = truePositives + trueNegatives + falseNegatives + falsePositives;
        //Plug the number of true positives and negatives then divide by total to obtain accuracy
        double formula = (truePositives + trueNegatives) / numFiles;
        return formula;
    }

    //Function to calculate precision (ratio of true positives to guesses)
    public double getPrecision(List<TestFile> testFiles)
    {   //Default values
        double truePositives = 0.0;
        double falsePositives = 0.0;

        for(TestFile testfile : testFiles)
        {   //If the probability is >= 0.5 and it correctly belongs in spam then it is true positive
            if(testfile.getSpamProbability() >= 0.5 && testfile.getActualClass().equals("spam"))
            {
                truePositives++;
            }
            //If the probability is >= 0.5 but it is in ham it is false positive
            else if (testfile.getSpamProbability() >= 0.5 && testfile.getActualClass().equals("ham"))
            {
                falsePositives++;
            }
        }
        //Formula for precision given by assignment
        double formula = truePositives / (falsePositives + truePositives);
        return formula;
    }
}