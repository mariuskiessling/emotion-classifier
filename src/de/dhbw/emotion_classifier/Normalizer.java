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

    /**
     * I can't remeber why the hell I introduced this method. @todo Figure that out.
     */
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

    /**
     * Set the normalizer's boundaries based on one column of the raw data.
     * @param data A list of all raw data points
     * @param column The column that will be evaluated while setting the normalizer's boundaries
     */
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

    /**
     * Normalize the provided raw data point value into one of three categories. The normalizer's boundaries have to be
     * set before using this method!
     * @param value The raw data point value that should be normalized
     * @return The normalized category value of the provided data point
     */
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

    /**
     * Normalize each row inside the provided raw data points. The normalization is limited to the provided column ID
     * (starting at 0).
     * @param table The raw data point table
     * @param columnId The column inside the data point table whose data points will be normalized
     * @return The provided data point table in normalized form
     */
    public ArrayList<double[]> normalizeColumn(ArrayList<double[]> table, int columnId) {
        table.forEach(row -> row[columnId] = normalize(row[columnId]));
        return table;
    }

    /**
     * Get the certainty that can be assigned to the provided value.
     * @param value A value (part of the raw data point set) whose certainty will be determined
     * @return The certainty for the provided data point value
     */
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

    /**
     * Calculate the normalization evidence for a data point whose value is smaller than the center of the normalization
     * category it is assigned to.
     * @param value The raw data point value
     * @param offset The offset of the data point value from the center of the normalization category
     * @param center The center of the normalization category the value is part of
     * @param normalizationWidth The width of the normalization category the value is part of
     * @return The evidence that the provided value is part of the normalization category
     */
    public double getLeftNormalizationEvidence(double value, double offset, double center, double normalizationWidth) {
        double n = (0.5 / -(normalizationWidth / 2)) * center + 1;
        double y = (0.5 / (normalizationWidth / 2)) * value + n;

        // Cap the result at 0.5
        return capNormalizationEvidence(y);
    }

    /**
     * Calculate the normalization evidence for a data point whose value is bigger than the center of the normalization
     * category it is assigned to.
     * @param value The raw data point value
     * @param offset The offset of the data point value from the center of the normalization category
     * @param center The center of the normalization category the value is part of
     * @param normalizationWidth The width of the normalization category the value is part of
     * @return The evidence that the provided value is part of the normalization category
     */
    public double getRightNormalizationEvidence(double value, double offset, double center, double normalizationWidth) {
        double n = (0.5 / (normalizationWidth / 2)) * center + 1;
        double y = -(0.5 / (normalizationWidth / 2)) * value + n;

        // Cap the result at 0.5
        return capNormalizationEvidence(y);
    }

    /**
     * Cap the normalization evidence at 1 and 0.5 as smaller/ bigger values don't matter in this project.
     * @param evidence The evidence value that should be capped
     * @return The capped evidence
     */
    private double capNormalizationEvidence(double evidence) {
        if(evidence < 0.5) {
            return 0.5;
        } else if(evidence > 1) {
            return 1;
        } else {
            return evidence;
        }
    }

    /**
     * Get the categorie's width of the provided raw input vector point.
     * @param value The raw input vector point
     * @return The width of the category in which the the value falls
     */
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

    /**
     * Get the offset from the categorie's center of the provided raw data point. If e.g. minSmall is 100 and minMedium
     * is 200 and the provided raw value is 160, the offset is 10.
     * @param value The raw input vector's point value whose center offset should be determined
     * @return The offset from the categorie's center of the provided value
     */
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

    /**
     * Get the value that functions as the normalization categorie's center. If e.g. the minSmall and minMedium values
     * are set to 100 and 200, then the normalization center is 150.
     * @param value The raw input vector data point whose normalization center should be determined
     * @return The numerical value of the normalization center of the provided raw data point
     */
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
     * Print each row and column of the provided normalized data table.
     * @param table The table that will be printed
     */
    public void printTable(ArrayList<double[]> table) {
        table.forEach(x -> System.out.println(Arrays.toString(x)));
    }
}
