package de.dhbw.emotion_classifier;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;

public class Main {
    public static void main(String[] args) {
        String filename = "/Users/marius/Downloads/E2/E_2_B.csv";

        ArrayList<double[]> data = Importer.loadDoubleCSV(filename, ";", 2, 4, true);

        System.out.println("Loaded raw data:");
        Importer.printTable(data);

        // Normalize loaded values into three categories
        ArrayList<double[]> normalized;

//        Normalizer n = new EyeNormalizer();
//        normalized = n.normalizeColumn(raw, 0);
//
//        n = new MouthNormalizer();
//        normalized = n.normalizeColumn(normalized, 1);
//
//        n = new ForeheadNormalizer();
//        normalized = n.normalizeColumn(normalized, 2);
//
//        System.out.println("Normalized the raw data:");
//        n.printTable(normalized);

        HashMap<String, ArrayList<Integer>> categories = Importer.loadCategories(filename, ";", 5);
        System.out.println("Loaded these categories in [columns]:");
        Importer.printCategories(categories);

        // Create a Normalizer for each feature and emotion
//        for(String category: categories.keySet()) {
//            for(int i = 0; i < raw.get(0).length; i++) {
//                Normalizer n = new Normalizer(0, 0);
//                n.setBoundaries(raw, categories.get(category), i);
//                System.out.println("Created normalizer for " + category + String.valueOf(i) + ":: Min: " + n.minMedium + " Max: " + n.maxMedium);
//            }
//        }

        // Create a Normalizer for each feature and normalize each feature column
        ArrayList<Normalizer> normalizers = new ArrayList<>();
        for(int i = 0; i < data.get(0).length; i++) {
            Normalizer n = new Normalizer(0, 0);
            n.setBoundaries(data, i);
            System.out.println("Created normalizer for feature " + i + ":: Min: " + n.minMedium + " Max: " + n.maxMedium);

            n.normalizeColumn(data, i);
            normalizers.add(n);
        }

        HashMap<String, ArrayList<double[]>> table = Classifier.createTable(data, categories);
        Classifier.printTable(table);

        // The following code will evaluate each row that is not part of the pre-classified data set and determine a classification for each one
        for(double[] row: data) {
            // Skip pre-classified rows
            if(Classifier.dataRowPreClassified(data.indexOf(row), categories)) {
                continue;
            }

            // Create a hit list of all features inside the current data row
            ArrayList<ArrayList> hitLists = new ArrayList<>();
            for(int i = 0; i < data.get(0).length; i++) {
                hitLists.add(Classifier.createFeatureHitList(row, table, i));
            }

            System.out.println("HI");
        }
    }
}
