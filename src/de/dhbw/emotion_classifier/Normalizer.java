package de.dhbw.emotion_classifier;

import java.util.ArrayList;
import java.util.Arrays;

public class Normalizer {
    public double minMedium;
    public double maxMedium;
    public double minSmall;
    public double maxLarge;

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

        this.minSmall = min;
        this.maxLarge = max;

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

    public double getNormalizationEvidence(double value) {
        double offset = this.getNormalizationOffset(value);
        double center = this.getNormalizationCenter(value);
        double normalizationWidth = this.getNormalizationWidth(value);

        if(value < center) {
            return this.getLeftNormalizationEvidence(value, offset, center, normalizationWidth);
        } else {
            return this.getRightNormalizationEvidence(value, offset, center, normalizationWidth);
        }
    }

    public double getLeftNormalizationEvidence(double value, double offset, double center, double normalizationWidth) {
        double n = (0.5 / -(normalizationWidth / 2)) * center + 1;
        double y = (0.5 / (normalizationWidth / 2)) * value + n;

        // Cap the result at 0.5
        return capNormalizationEvidence(y);
    }

    public double getRightNormalizationEvidence(double value, double offset, double center, double normalizationWidth) {
        double n = (0.5 / (normalizationWidth / 2)) * center + 1;
        double y = -(0.5 / (normalizationWidth / 2)) * value + n;

        // Cap the result at 0.5
        return capNormalizationEvidence(y);
    }

    private double capNormalizationEvidence(double evidence) {
        if(evidence < 0.5) {
            return 0.5;
        } else if(evidence > 1) {
            return 1;
        } else {
            return evidence;
        }
    }

    public double getNormalizationWidth(double value) {
        double normalized = this.normalize(value);
        if(normalized == 1) {
            return minMedium - minSmall;
        } else if(normalized == 2) {
            return maxMedium - minMedium;
        } else if(normalized == 3) {
            return maxLarge - maxMedium;
        }
        return -1;
    }

    public double getNormalizationOffset(double value) {
        // Determine the normalized value
        double normalized = this.normalize(value);

        double center = this.getNormalizationCenter(value);

        double offset = 0;
        if(value < center) {
            offset = center - value;
        } else {
            offset = value - center;
        }

        return offset;
    }

    private double getNormalizationCenter(double value) {
        // Determine the normalized value
        double normalized = this.normalize(value);

        // Determine the center of that normalized values range
        // |-------|x|-------||-------|x|-------||-------|x|-------|
        double center = 0;
        if(normalized == 1) {
            center = minMedium - (minMedium - minSmall) / 2;
        } else if(normalized == 2) {
            center = maxMedium - (maxMedium - minMedium) / 2;
        } else if(normalized == 3) {
            center = maxLarge - (maxLarge - maxMedium) / 2;
        }
        return center;
    }

    /**
     * Prints each row and column of the provided normalized table.
     * @param table The table to be printed.
     */
    public void printTable(ArrayList<double[]> table) {
        table.forEach(x -> System.out.println(Arrays.toString(x)));
    }
}
