package de.dhbw.emotion_classifier;

import java.util.ArrayList;
import java.util.Arrays;

public class Normalizer {
    public double minMedium;
    public double maxMedium;

    public Normalizer(int minMedium, int maxMedium) {
        this.minMedium = minMedium;
        this.maxMedium = maxMedium;
    }

    public void setBoundariesBasedOnClassifications(ArrayList<double[]> data, ArrayList<Integer> classifiedRowIds, int column) {
        double min = Double.MAX_VALUE, max = Double.MIN_VALUE, sum = 0, average = 0;

        // Find the min and max value of the rows that are classified inside the classifiedRowIds
        for(Integer rowId: classifiedRowIds) {
            double fieldValue = data.get(rowId)[column];

            min = Double.min(min, fieldValue);
            max = Double.max(max, fieldValue);

            sum += fieldValue;
        }

        // Find the average value of the rows that are classified inside the classifiedRowIds
        average = sum / classifiedRowIds.size();

        // Set the lower boundary for medium and upper border of medium accordingly
        this.minMedium = average - average * 1/3;
        this.maxMedium = average + (max - average) * 1/3;
    }

    public void setBoundaries(ArrayList<double[]> data, int column) {
        double min = Double.MAX_VALUE, max = Double.MIN_VALUE, sum = 0, average = 0;

        // Find the min and max value of the rows that are classified inside the classifiedRowIds
        for(double[] row: data) {
            double fieldValue = row[column];

            min = Double.min(min, fieldValue);
            max = Double.max(max, fieldValue);

            sum += fieldValue;
        }

        // Find the average value of the rows that are classified inside the classifiedRowIds
        average = sum / data.size();

        // Set the lower boundary for medium and upper border of medium accordingly
        this.minMedium = average - average * 1/3;
        this.maxMedium = average + (max - average) * 1/3;
    }

    public int normalize(double value) {
        if (value < minMedium) {
            return 1;
        }
        if ((minMedium <= value) && (value <= maxMedium)) {
            return 2;
        }
        if (maxMedium < value) {
            return 3;
        }
        return 0;
    }

    public ArrayList<double[]> normalizeColumn(ArrayList<double[]> table, int columnId) {
        table.forEach(row -> row[columnId] = normalize(row[columnId]));
        return table;
    }

    /**
     * Prints each row and column of the provided normalized table.
     * @param table The table to be printed.
     */
    public void printTable(ArrayList<double[]> table) {
        table.forEach(x -> System.out.println(Arrays.toString(x)));
    }
}
