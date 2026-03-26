package com.ontariotechu.sofe3980U;

import java.io.FileReader;
import java.util.List;
import com.opencsv.*;

public class App
{
    public static void main(String[] args)
    {
        String filePath = "model.csv";
        FileReader filereader;
        List<String[]> allData;

        try {
            filereader = new FileReader(filePath);
            CSVReader csvReader = new CSVReaderBuilder(filereader).withSkipLines(1).build();
            allData = csvReader.readAll();
        }
        catch (Exception e) {
            System.out.println("Error reading the CSV file");
            return;
        }

        double ceSum = 0.0;
        double eps = 1e-15;
        int n = 0;

        int[][] confusionMatrix = new int[5][5];

        for (String[] row : allData) {
            int yTrue = Integer.parseInt(row[0]);

            double[] yPredicted = new double[5];
            for (int i = 0; i < 5; i++) {
                yPredicted[i] = Double.parseDouble(row[i + 1]);
            }

            // Find predicted class using argmax
            int predictedClass = 1;
            double maxProb = yPredicted[0];

            for (int i = 1; i < 5; i++) {
                if (yPredicted[i] > maxProb) {
                    maxProb = yPredicted[i];
                    predictedClass = i + 1;
                }
            }

            // Cross Entropy: use probability of the true class
            double pTrue = yPredicted[yTrue - 1];
            pTrue = Math.max(eps, Math.min(1.0 - eps, pTrue));
            ceSum += -Math.log(pTrue);

            // Update confusion matrix
            confusionMatrix[yTrue - 1][predictedClass - 1]++;

            n++;
        }

        double crossEntropy = ceSum / n;

        System.out.println("Cross Entropy (CE) = " + crossEntropy);
        System.out.println("\nConfusion Matrix:");
        System.out.println("Rows = Actual class, Columns = Predicted class\n");

        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                System.out.print(confusionMatrix[i][j] + "\t");
            }
            System.out.println();
        }
    }
}