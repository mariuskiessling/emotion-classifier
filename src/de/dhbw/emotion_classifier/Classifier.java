package de.dhbw.emotion_classifier;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class Classifier {

    public static HashMap<String, ArrayList<double[]>> createTable(ArrayList<double[]> data, HashMap<String, ArrayList<Integer>> categories) {
        // Create table that will hold an ArrayList for each category
        HashMap<String, ArrayList<double[]>> table = new HashMap<>();

        // Loop through each category and add it to the table
        for(String category: categories.keySet()) {
            ArrayList<Integer> rowIds = categories.get(category);

            for(Integer rowId: rowIds) {
                table = addTableRow(data.get(rowId), category, table);
            }
        }

        return table;
    }

    private static HashMap<String, ArrayList<double[]>> addTableRow(double[] row, String category, HashMap<String, ArrayList<double[]>> table) {
        // Check if category already exists in table
        if(table.containsKey(category)) {
            // Add row to existing category by checking if a new category payload has to be created
            ArrayList<double[]> categoryPayload = table.get(category);

            // A clash is falsified if a row is found inside the categorie's payload that is identical to the new row
            boolean clash = true;

            for(double[] payloadRow: categoryPayload) {
                if(Arrays.equals(row, payloadRow)) {
                    clash = false;
                }
            }

            if(clash) {
                // Add a new row to the categorie's payload
                categoryPayload.add(row);
                table.put(category, categoryPayload);
            }
        } else {
            // Create new category and add row to new ArrayList
            ArrayList<double[]> categoryPayload = new ArrayList<>();
            categoryPayload.add(row);
            table.put(category, categoryPayload);
        }

        return table;
    }

    public static void printTable(HashMap<String, ArrayList<double[]>> table) {
        for(String category: table.keySet()) {
            System.out.println("== Category: " + category + " ==");

            for(double[] row: table.get(category)) {
                System.out.println("  " + category + "_" + table.get(category).indexOf(row) + " " + Arrays.toString(row));
            }
        }
    }

    public static ArrayList<Integer> createFeatureHitList(double[] normalizedRow, HashMap<String, ArrayList<double[]>> table, int column) {
        ArrayList<Integer> hitList = new ArrayList<>();

        for(String category: table.keySet()) {
            for(double[] row: table.get(category)) {
                if(normalizedRow[column] == row[column]) {
                    hitList.add(1);
                } else {
                    hitList.add(0);
                }
            }
        }

        return hitList;
    }

    public static boolean dataRowPreClassified(int dataRowId, HashMap<String, ArrayList<Integer>> categories) {
        for(String category: categories.keySet()) {
            if(categories.get(category).contains(dataRowId)) {
                return true;
            }
        }

        return false;
    }
}
