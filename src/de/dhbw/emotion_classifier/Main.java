package de.dhbw.emotion_classifier;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class Main {
    public static void main(String[] args) {
        String filename = "/Users/marius/Downloads/E2/E_2_B.csv";

        ArrayList<double[]> rawData = Importer.loadDoubleCSV(filename, ";", 2, 4, true);

        System.out.println("Loaded raw data:");
        Importer.printTable(rawData);

        HashMap<String, ArrayList<Integer>> categories = Importer.loadCategories(filename, ";", 5);
        System.out.println("Loaded these categories in [columns]:");
        Importer.printCategories(categories);

        // Deep copy the raw data into a normalized data structure in order to preserve the original data
        ArrayList<double[]> normalizedData = new ArrayList<>();
        for(double[] row: rawData) {
            normalizedData.add(row.clone());
        }

        // Create a Normalizer for each feature and normalize each feature column
        ArrayList<Normalizer> normalizers = new ArrayList<>();

        for(int i = 0; i < normalizedData.get(0).length; i++) {
            Normalizer n = new Normalizer(0, 0);
            n.setBoundaries(normalizedData, i);
            System.out.println("Created normalizer for feature " + i + ":: Min: " + n.minMedium + " Max: " + n.maxMedium);

            n.normalizeColumn(normalizedData, i);
            normalizers.add(n);
        }

        HashMap<String, ArrayList<double[]>> table = Classifier.createTable(normalizedData, categories);
        Classifier.printTable(table);

        ArrayList<ArrayList> hitLists = new ArrayList<>();
        for(double[] row: normalizedData) {
            // Create a hit list of all features inside the current data row and store it in a hit list collection
            ArrayList<ArrayList> hitListCollection = new ArrayList<>();
            for(int i = 0; i < normalizedData.get(0).length; i++) {
                hitListCollection.add(Classifier.createFeatureHitList(row, table, i));
            }
            hitLists.add(hitListCollection);
        }

        double[] row = normalizedData.get(0);
        for(int i = 0; i < normalizedData.get(0).length; i++) {
            Classifier.calculateEvidence(rawData, normalizedData, hitLists.get(0), table, categories, i, row);
        }
        System.out.println("\n\n");

//        row = normalizedData.get(23);
//        for(int i = 0; i < normalizedData.get(0).length; i++) {
//            Classifier.calculateEvidence(rawData, normalizedData, hitLists.get(23), table, categories, i, row);
//        }

//    Normalizer normalizer = new Normalizer(250, 300);
//    normalizer.minSmall = 150;
//    normalizer.maxLarge = 400;
//        System.out.println(normalizer.normalize(300));
//        System.out.println(normalizer.getNormalizationEvidence(275));


    }
}
