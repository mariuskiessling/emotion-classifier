package de.dhbw.emotion_classifier;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class Classifier {

    /**
     * Create a classification table based on the rows that are pre-classified.
     * @param data The input vectors (data HAS to be normalized!)
     * @param categories
     * @return The classification table. Each classification category is stored under its own classification name.
     * Multiple rows for a category are stored inside a list under the categorie's name.
     */
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

    /**
     * Add a classification row to the classification table. Rows are only inserted if needed (e.g. if it does not exist
     * yet and conflicts are handled automatically.
     * @param row Normalized input vector row
     * @param category Name of the category the row should be inserted in
     * @param table The classification category table the row will be added to
     * @return The classification category table with the new row added (if necessary)
     */
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

    /**
     * Print the classification category table and all its entries.
     * @param table The classification category table that should be printed
     */
    public static void printTable(HashMap<String, ArrayList<double[]>> table) {
        for(String category: table.keySet()) {
            System.out.println("== Category: " + category + " ==");

            for(double[] row: table.get(category)) {
                System.out.println("  " + category + "_" + table.get(category).indexOf(row) + " " + Arrays.toString(row));
            }
        }
    }

    /**
     * Generate a row name based on the classification category table. The name will always follow the pattern
     * "CategoryName_rowIndexInsideCategory" (e.g. Anger_2). Row row index will start at 0.
     * @param table The classification category table the row is stored in
     * @param rowId The row ID that need naming. This ID identifies the row globally inside the table, meaning that e.g.
     *              category 0 has 2 entries and category 1 has 3 entries, the row ID 3 would be the second row inside
     *              the second category. The ID starts at 0.
     * @return The generated table row name
     */
    public static String getTableRowName(HashMap<String, ArrayList<double[]>> table, int rowId) {
        int i = 0;

        for(String category: table.keySet()) {
            for(double[] row: table.get(category)) {
                if(i == rowId) {
                    return category + "_" + table.get(category).indexOf(row);
                }
                i++;
            }
        }

        return "This classification does not exist";
    }

    /**
     * Create a feature hit list (e.g. [1,0,1,1,0,1] based on the input vector row and classification category table.
     * @param normalizedRow The input vector in normalized form
     * @param table The classification category table that will function as the hit list's template.
     * @param column The column number (also referred to as the feature index) inside input vector and classification
     *               category table. The column number starts at 0.
     * @return The feature list in the form of e.g. [1,0,1,1,0,1] for a classification category table that stores 6
     * categories.
     */
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

    /**
     * Checks whether the input vector row is part of the pre-classified dataset.
     * @param dataRowId The input vectors row ID. The ID starts at 0.
     * @param categories A list of all categories and their input data rows.
     * @return True if the row is already pre-classified, false if not.
     */
    public static boolean dataRowPreClassified(int dataRowId, HashMap<String, ArrayList<Integer>> categories) {
        for(String category: categories.keySet()) {
            if(categories.get(category).contains(dataRowId)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Calculate the overall evidence that the rows inside the classification category table selected by the hit list
     * are "correct". We will do this by accumulating the evidences of all the pre-classified rows that are selected
     * inside the hit list.
     * @param rawData The raw input vector list
     * @param normalizedData The normalized input vector list
     * @param hitLists All hit lists that were generated for the current input vector
     * @param table The classification category table
     * @param categories The list of all categories and their row IDs inside the raw data
     * @param column The column (also referred to as feature index) inside the input vector and hit lists.
     * @param row The input vector in normalized form
     * @param normalizers All normalizers that were generated globally for each feature
     * @return The evidence that the data selected by the hit list is "representable".
     */
    public static double calculateEvidence(ArrayList<double[]> rawData, ArrayList<double[]> normalizedData, ArrayList<ArrayList> hitLists, HashMap<String, ArrayList<double[]>> table, HashMap<String, ArrayList<Integer>> categories, int column, double[] row, ArrayList<Normalizer> normalizers) {
        int i = 0;
        //System.out.println("\nWorking on row: " + Arrays.toString(row) + " in column " + column + " with hit list " + hitLists.get(column));

        double evidences = 0;
        int evidencesCount = 0;

        for(String category: table.keySet()) {
            ArrayList<double[]> categoryRows = table.get(category);
            for(double[] categoryRow: categoryRows) {
                ArrayList<Integer> columnHitList = hitLists.get(column);
                if(columnHitList.get(i) == 1) {
                    //System.out.println("Row " + i + " in category " + category + " is relevant");

                    // Search for rows in pre-classified category rows in the normalized data that have the same normalized column like the current category row
                    for(Integer rowId: categories.get(category)) {
                        if(categoryRow[column] == normalizedData.get(rowId)[column]) {
                            //System.out.println("Found a match!" + categoryRow[column] +  ";" + normalizedData.get(rowId)[column]);

                            double evidence = normalizers.get(column).getNormalizationEvidence(normalizedData.get(rowId)[column]);
                            evidences += evidence;
                            evidencesCount++;
                        }
                    }
                }
                i++;
            }
        }

       return evidences / evidencesCount;
    }
}
