package com.ontariotechu.sofe3980U;

import java.io.FileReader;
import java.util.List;
import com.opencsv.*;

public class App 
{
    // Helper class to store results for one model
    static class ModelMetrics {
        String modelName;
        double mse;
        double mae;
        double mare;

        ModelMetrics(String modelName, double mse, double mae, double mare) {
            this.modelName = modelName;
            this.mse = mse;
            this.mae = mae;
            this.mare = mare;
        }
    }

    public static void main(String[] args)
    {
        ModelMetrics model1 = evaluateModel("model_1.csv");
        ModelMetrics model2 = evaluateModel("model_2.csv");
        ModelMetrics model3 = evaluateModel("model_3.csv");

        if (model1 == null || model2 == null || model3 == null) {
            System.out.println("Error: could not evaluate one or more model files.");
            return;
        }

        // Print results
        printMetrics(model1);
        printMetrics(model2);
        printMetrics(model3);

        // Find best model for each metric
        ModelMetrics bestMSE = model1;
        if (model2.mse < bestMSE.mse) bestMSE = model2;
        if (model3.mse < bestMSE.mse) bestMSE = model3;

        ModelMetrics bestMAE = model1;
        if (model2.mae < bestMAE.mae) bestMAE = model2;
        if (model3.mae < bestMAE.mae) bestMAE = model3;

        ModelMetrics bestMARE = model1;
        if (model2.mare < bestMARE.mare) bestMARE = model2;
        if (model3.mare < bestMARE.mare) bestMARE = model3;

        System.out.println("\nBest model based on MSE: " + bestMSE.modelName);
        System.out.println("Best model based on MAE: " + bestMAE.modelName);
        System.out.println("Best model based on MARE: " + bestMARE.modelName);
    }

    public static ModelMetrics evaluateModel(String filePath) {
        FileReader filereader;
        List<String[]> allData;

        try {
            filereader = new FileReader(filePath);
            CSVReader csvReader = new CSVReaderBuilder(filereader).withSkipLines(1).build();
            allData = csvReader.readAll();
        } catch (Exception e) {
            System.out.println("Error reading the CSV file: " + filePath);
            return null;
        }

        double mseSum = 0.0;
        double maeSum = 0.0;
        double mareSum = 0.0;
        double eps = 1e-10; // small value to avoid division by zero
        int n = 0;

        for (String[] row : allData) {
            double y_true = Double.parseDouble(row[0]);
            double y_predicted = Double.parseDouble(row[1]);

            double error = y_true - y_predicted; // difference between actual and predicted

            mseSum += error * error;             // squared error for MSE
            maeSum += Math.abs(error);           // absolute error for MAE
            mareSum += Math.abs(error) / (Math.abs(y_true) + eps); // relative error for MARE

            n++;
        }

        double mse = mseSum / n;   // average squared error
        double mae = maeSum / n;   // average absolute error
        double mare = mareSum / n; // average relative error

        // Compare models to find the best (lowest error)
        return new ModelMetrics(filePath, mse, mae, mare);
    }

    public static void printMetrics(ModelMetrics model) {
        System.out.println("\nResults for " + model.modelName + ":");
        System.out.println("MSE  = " + model.mse);
        System.out.println("MAE  = " + model.mae);
        System.out.println("MARE = " + model.mare);
    }
}
