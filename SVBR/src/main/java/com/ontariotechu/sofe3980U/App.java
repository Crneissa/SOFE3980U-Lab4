package com.ontariotechu.sofe3980U;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import com.opencsv.*;

public class App
{
    // Store results for each model
    static class ModelMetrics {
        String modelName;
        double bce;
        int tp;
        int tn;
        int fp;
        int fn;
        double accuracy;
        double precision;
        double recall;
        double f1;
        double auc;

        ModelMetrics(String modelName, double bce, int tp, int tn, int fp, int fn,
                     double accuracy, double precision, double recall, double f1, double auc) {
            this.modelName = modelName;
            this.bce = bce;
            this.tp = tp;
            this.tn = tn;
            this.fp = fp;
            this.fn = fn;
            this.accuracy = accuracy;
            this.precision = precision;
            this.recall = recall;
            this.f1 = f1;
            this.auc = auc;
        }
    }

    static class DataPoint {
        int yTrue;
        double yPred;

        DataPoint(int yTrue, double yPred) {
            this.yTrue = yTrue;
            this.yPred = yPred;
        }
    }

    static class ROCPoint {
        double fpr;
        double tpr;

        ROCPoint(double fpr, double tpr) {
            this.fpr = fpr;
            this.tpr = tpr;
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

        printMetrics(model1);
        printMetrics(model2);
        printMetrics(model3);

        ModelMetrics bestBCE = model1;
        if (model2.bce < bestBCE.bce) bestBCE = model2;
        if (model3.bce < bestBCE.bce) bestBCE = model3;

        ModelMetrics bestAccuracy = model1;
        if (model2.accuracy > bestAccuracy.accuracy) bestAccuracy = model2;
        if (model3.accuracy > bestAccuracy.accuracy) bestAccuracy = model3;

        ModelMetrics bestPrecision = model1;
        if (model2.precision > bestPrecision.precision) bestPrecision = model2;
        if (model3.precision > bestPrecision.precision) bestPrecision = model3;

        ModelMetrics bestRecall = model1;
        if (model2.recall > bestRecall.recall) bestRecall = model2;
        if (model3.recall > bestRecall.recall) bestRecall = model3;

        ModelMetrics bestF1 = model1;
        if (model2.f1 > bestF1.f1) bestF1 = model2;
        if (model3.f1 > bestF1.f1) bestF1 = model3;

        ModelMetrics bestAUC = model1;
        if (model2.auc > bestAUC.auc) bestAUC = model2;
        if (model3.auc > bestAUC.auc) bestAUC = model3;

        System.out.println("\nBest model based on BCE: " + bestBCE.modelName);
        System.out.println("Best model based on Accuracy: " + bestAccuracy.modelName);
        System.out.println("Best model based on Precision: " + bestPrecision.modelName);
        System.out.println("Best model based on Recall: " + bestRecall.modelName);
        System.out.println("Best model based on F1 Score: " + bestF1.modelName);
        System.out.println("Best model based on AUC-ROC: " + bestAUC.modelName);
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

        List<DataPoint> dataPoints = new ArrayList<>();

        double bceSum = 0.0;
        double eps = 1e-15; // prevents log(0)

        int tp = 0, tn = 0, fp = 0, fn = 0;

        for (String[] row : allData) {
            int yTrue = Integer.parseInt(row[0]);
            double yPred = Double.parseDouble(row[1]);

            dataPoints.add(new DataPoint(yTrue, yPred));

            double p = Math.max(eps, Math.min(1.0 - eps, yPred));
            bceSum += yTrue * Math.log(p) + (1 - yTrue) * Math.log(1 - p);
            // Binary Cross Entropy calculation
            
            int predictedLabel = (yPred >= 0.5) ? 1 : 0;
            // Convert probability to class using threshold 0.5

            // Build confusion matrix
            if (yTrue == 1 && predictedLabel == 1) tp++;
            else if (yTrue == 0 && predictedLabel == 0) tn++;
            else if (yTrue == 0 && predictedLabel == 1) fp++;
            else if (yTrue == 1 && predictedLabel == 0) fn++;
        }

        int n = allData.size();
        double bce = -bceSum / n;

    double accuracy = (tp + tn) / (double)(tp + tn + fp + fn); // overall correctness
    double precision = tp / (double)(tp + fp); // correct positive predictions
    double recall = tp / (double)(tp + fn);    // how many actual positives were found
    double f1 = 2 * precision * recall / (precision + recall); // balance of precision & recall
        
        double auc = computeAUC(dataPoints);

        return new ModelMetrics(filePath, bce, tp, tn, fp, fn, accuracy, precision, recall, f1, auc);
    }

    public static double computeAUC(List<DataPoint> dataPoints) {
        List<ROCPoint> rocPoints = new ArrayList<>();

        int totalPositives = 0;
        int totalNegatives = 0;

        for (DataPoint point : dataPoints) {
            if (point.yTrue == 1) totalPositives++;
            else totalNegatives++;
        }

        for (int i = 0; i <= 100; i++) {
            double threshold = i / 100.0;
            // test different thresholds from 0 to 1
            
            int tp = 0, fp = 0, tn = 0, fn = 0;

            for (DataPoint point : dataPoints) {
                int predictedLabel = (point.yPred >= threshold) ? 1 : 0;
                // recompute classification at each threshold

                if (point.yTrue == 1 && predictedLabel == 1) tp++;
                else if (point.yTrue == 0 && predictedLabel == 0) tn++;
                else if (point.yTrue == 0 && predictedLabel == 1) fp++;
                else if (point.yTrue == 1 && predictedLabel == 0) fn++;
            }
            // true positive rate
            double tpr = (totalPositives == 0) ? 0.0 : tp / (double) totalPositives;
            // false positive rate
            double fpr = (totalNegatives == 0) ? 0.0 : fp / (double) totalNegatives;

            rocPoints.add(new ROCPoint(fpr, tpr));
        }

        rocPoints.sort((a, b) -> Double.compare(a.fpr, b.fpr));

        double auc = 0.0;
        for (int i = 1; i < rocPoints.size(); i++) {
            double x1 = rocPoints.get(i - 1).fpr;
            double y1 = rocPoints.get(i - 1).tpr;
            double x2 = rocPoints.get(i).fpr;
            double y2 = rocPoints.get(i).tpr;

            auc += (x2 - x1) * (y1 + y2) / 2.0;
            // trapezoidal rule to approximate ROC area
        }

        return auc;
    }

    public static void printMetrics(ModelMetrics model) {
        System.out.println("\nResults for " + model.modelName + ":");
        System.out.println("BCE = " + model.bce);
        System.out.println("Confusion Matrix:");
        System.out.println("TP = " + model.tp + ", TN = " + model.tn + ", FP = " + model.fp + ", FN = " + model.fn);
        System.out.println("Accuracy  = " + model.accuracy);
        System.out.println("Precision = " + model.precision);
        System.out.println("Recall    = " + model.recall);
        System.out.println("F1 Score  = " + model.f1);
        System.out.println("AUC-ROC   = " + model.auc);
    }
}
