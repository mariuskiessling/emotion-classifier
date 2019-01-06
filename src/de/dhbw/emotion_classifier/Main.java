package de.dhbw.emotion_classifier;

import dempster.*;

import java.awt.*;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;

public class Main {
    public static void main(String[] args) throws Exception {
        String filename = "a.csv";

        if(args.length < 1) {
            // Try to load default file
            try {
                FileReader fr = new FileReader(filename);
            } catch (FileNotFoundException e) {
                System.out.println("Could not load default file 'a.csv'. Please provide a filename (and path) as the first argument. The path can be provided as an absolute or relative value from the programs execution directory.");
                System.exit(1);
            }
        } else {
            // Try to load a custom input file
            filename = args[0];
            try {
                FileReader fr = new FileReader(filename);
            } catch (FileNotFoundException e) {
                System.out.println("Could not load custom file " + filename + ". Please provide a filename (and path) as the first argument. The path can be provided as an absolute or relative value from the programs execution directory.");
                System.exit(1);
            }
        }

        // Import the required data columns of the input file
        ArrayList<double[]> rawData = Importer.loadDoubleCSV(filename, ";", 2, 4, true);
        System.out.println("Successfully loaded " + rawData.size() + " raw data vectors.\n");

        // Import the category labels from the input file
        HashMap<String, ArrayList<Integer>> categories = Importer.loadCategories(filename, ";", 5);
        System.out.println("Successfully loaded the following category labels in [rows]:");
        Importer.printCategories(categories);
        System.out.println("\n");

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

            n.normalizeColumn(normalizedData, i);
            normalizers.add(n);

            System.out.println("Created a normalizer for feature "+ i + ": [" + n.minSmall + " --- " + n.minMedium + "][" + n.minMedium + " --- " + n.maxMedium + "][" + n.maxMedium + " --- " + n.maxLarge + "]");
        }
        System.out.println("\n");

        // Create classification table
        HashMap<String, ArrayList<double[]>> table = Classifier.createTable(normalizedData, categories);
        System.out.println("Successfully created the classification table with the following categories:");
        Classifier.printTable(table);
        System.out.println("\n");

        // We can now process each input vector and
        ArrayList<ArrayList> hitLists = new ArrayList<>();
        for(double[] row: normalizedData) {
            // Create a hit list of all features inside the current data row and store it in a hit list collection
            ArrayList<ArrayList> hitListCollection = new ArrayList<>();
            for(int i = 0; i < normalizedData.get(0).length; i++) {
                hitListCollection.add(Classifier.createFeatureHitList(row, table, i));
            }
            hitLists.add(hitListCollection);

            // Gather evidences for all categories an input vector can be classified as
            ArrayList<Double> evidences = new ArrayList<>();
            for(int i = 0; i < normalizedData.get(0).length; i++) {
                evidences.add(Classifier.calculateEvidence(rawData, normalizedData, hitLists.get(0), table, categories, i, row, normalizers));
            }

            // Create a measure for each input vector feature and accumulate them using the Dempster-Schafer rule
            dempster.DempsterHandler dH = new DempsterHandler(hitListCollection.get(0).size());
            for(int i = 0; i < normalizedData.get(0).length; i++) {
                Measure m = dH.addMeasure();
                m.addEntry(hitListCollection.get(i), evidences.get(i));
                dH.accumulateAllMeasures();
            }

            // Calculate all the classification categorie's plausabilities and determine the highest one.
            // The highest one will be chosen as this input vectors correct classification.
            double highestPlausability = 0;
            int highestPlausabilityIndex = 0;
            for(int i = 0; i < hitListCollection.get(0).size(); i++) {
                double plausability = dH.getFirstMeasure().calculatePlausability(i);
                System.out.println("Plausibility of " + Classifier.getTableRowName(table, i) + ": " + plausability);

                if(highestPlausability < plausability) {
                    highestPlausability = plausability;
                    highestPlausabilityIndex = i;
                }
            }
            System.out.println("=> Detected category for input vector " + normalizedData.indexOf(row) + ": " + Classifier.getTableRowName(table, highestPlausabilityIndex));
            System.out.println("\n");
        }
    }
}
