package de.dhbw.emotion_classifier;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * The Importer can load any CSV file and extract its fields.
 */
public class Importer {

    /**
     * Loads any CSV file containing numerical (double) values in the specified columns range.
     * @param filename The filename (and path) of the file that will be loaded.
     * @param seperator The separator splits each column of the CSV file.
     * @param columnsBegin The column ID (beginning at 1) that marks the first column that will be loaded.
     * @param columnsEnd The column ID that marks the last column that will be loaded.
     * @param skipHeader If set, the first line will not be interpreted as data but as a header line.
     * @return
     */
    public static ArrayList<double[]> loadDoubleCSV(String filename, String seperator, int columnsBegin, int columnsEnd, boolean skipHeader) {
        try(BufferedReader br = new BufferedReader(new FileReader(filename))) {
            // Read each line and seperate it by the global seperator
            String line = "";
            boolean headerFound = false;
            ArrayList<double[]> rows = new ArrayList<>();

            while((line = br.readLine()) != null) {
                String row[] = line.split(seperator);

                if(skipHeader && !headerFound) {
                    headerFound = true;
                    continue;
                }

                double[] parsedRow = new double[columnsEnd - columnsBegin + 1];
                int j = 0;
                for(int i = columnsBegin - 1; i < columnsEnd; i++) {
                    parsedRow[j] = Double.parseDouble(row[i]);
                    j++;
                }
                rows.add(parsedRow);
            }

            return rows;

        } catch (IOException e) {
            e.printStackTrace();

            return null;
        }
    }

    /**
     * Loads any CSV file containing numerical (double) values in the specified columns range.
     * @param filename The filename (and path) of the file that will be loaded.
     * @param seperator The separator splits each column of the CSV file.
     * @param columnsBegin The column ID (beginning at 1) that marks the first column that will be loaded.
     * @param columnsEnd The column ID that marks the last column that will be loaded.
     * @param skipHeader If set, the first line will not be interpreted as data but as a header line.
     * @return
     */
    public static ArrayList<String[]> loadStringCSV(String filename, String seperator, int columnsBegin, int columnsEnd, boolean skipHeader) {
        try(BufferedReader br = new BufferedReader(new FileReader(filename))) {
            // Read each line and seperate it by the global seperator
            String line = "";
            boolean headerFound = false;
            ArrayList<String[]> rows = new ArrayList<>();

            while((line = br.readLine()) != null) {
                String row[] = line.split(seperator);

                String[] parsedRow = new String[columnsEnd - columnsBegin + 1];
                int j = 0;
                for(int i = columnsBegin - 1; i < columnsEnd; i++) {
                    if(row.length - 1 < i) {
                        // Add empty cell if key does not exist
                        parsedRow[j] = "";
                    } else {
                        // Add cell as normal
                        parsedRow[j] = row[i];
                    }
                    j++;
                }

                rows.add(parsedRow);

                if(skipHeader && !headerFound) {
                    headerFound = true;
                    continue;
                }
            }

            return rows;

        } catch (IOException e) {
            e.printStackTrace();

            return null;
        }
    }

    public static HashMap<String, ArrayList<Integer>> loadCategories(String filename, String separator, int columnId) {
        // We will store the categories in a HashMap containing the category name as the key which each hold an List of
        // ints that represent the row number (beginning at 0) of the category in question. E.g. category "sadness"
        // exists in rows [0, 4, 5] of the input data.
        HashMap<String, ArrayList<Integer>> categories = new HashMap<>();

        ArrayList<String[]> raw = Importer.loadStringCSV(filename, separator, columnId, columnId, true);

        // Fill HashMap
        for(String[] row: raw) {
            if(!row[0].isEmpty()) {
                categories = Importer.insertCategory(categories, row[0], raw.indexOf(row));
            }
        }

        return categories;
    }

    private static HashMap<String, ArrayList<Integer>> insertCategory(HashMap<String, ArrayList<Integer>> categories, String category, int rowId) {
        if(!categories.containsKey(category)) {
            categories.put(category, new ArrayList<Integer>());
        }

        categories.get(category).add(rowId);
        return categories;
    }

    /**
     * Prints each row and column of the provided table.
     * @param table The table to be printed.
     */
    public static void printTable(ArrayList<double[]> table) {
        table.forEach(x -> System.out.println(Arrays.toString(x)));
    }

    public static void printCategories(HashMap<String, ArrayList<Integer>> categories) {
        for(String category: categories.keySet()){
            ArrayList<Integer> value = categories.get(category);
            System.out.println(category + " " + value.toString());
        }
    }
}
