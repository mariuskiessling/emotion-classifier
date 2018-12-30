package dempster;

import java.util.ArrayList;
import java.util.List;

/**
 * Class to hold a collection of {@links Measure} and allow dempster-shafer-operations.
 * Allows for adding {@link Measure}s after initialisation with a certain size and accumulation of those measures.
 * Important: there is pretty much no safety-net for misuse! If you screw up and add a measure with the wrong size or something like that you might run into errors
 * @author Ben F�rnrohr
 */
public class DempsterHandler{

    /** size of the {@link Measure}s and their entrys. Should match the number of alternatives */
    private int size;

    /** {@link Measure}s held by the handler */
    private List<Measure> measures = new ArrayList<Measure>();

    public DempsterHandler (int size) {
        this.size = size;
    }

    /**
     * adds a {@link Measure} of the standard size and returns it for further editing
     * @return the added {@link Measure}
     */
    public Measure addMeasure() {
        Measure newMeasure = new Measure(size);
        this.measures.add(newMeasure);
        return newMeasure;
    }

    /**
     * Recursively accumulates all {@link Measure}s held by the handler
     * After calling this you should be left with 1 final {@link Measure}
     */
    public void accumulateAllMeasures() {
        if (measures.size() < 2) {
            //no measures or just 1 left. Abort
            return;
        }
        else {
            //take 2 measures, remove them from list
            Measure measure1 = measures.get(0);
            Measure measure2 = measures.get(1);
            this.measures.remove(measure1);
            this.measures.remove(measure2);
            //accumulate measures, add to list
            this.measures.add(this.accumulateMeasures(measure1, measure2));
            this.accumulateAllMeasures();
        }

    }

    /**
     * Returns the first {@link Measure} of the list
     * Useful after accumulating all measures to get access to the last remaining {@link Measure}
     * @return
     */
    public Measure getFirstMeasure() {
        return this.measures.get(0);
    }

    /**
     * Accumulates 2 {@link Measure}s, taking conflicts into account
     * @param measure1 first {@link Measure}
     * @param measure2 second {@link Measure}
     * @return resulting {@link Measure}
     */
    private Measure accumulateMeasures(Measure measure1, Measure measure2) {

        Measure retMeasure = new Measure(this.size);
        double conflict = getConflict(measure1,measure2);

        if (conflict <= 0.99d)
        {
            double correction = 1.0d/(1.0d-conflict);
            List<MeasureEntry> entries1 = measure1.getMeasureEntrys();
            List<MeasureEntry> entries2 = measure2.getMeasureEntrys();

            for (MeasureEntry entry1 : entries1){
                for (MeasureEntry entry2 : entries2){
                    List<Integer> intersection = getIntersection(entry1, entry2);
                    double value = entry1.getProbability() * entry2.getProbability() * correction;
                    if (value > 0.0d && !entryIsEmpty(intersection) &&!isOmegaEntry(intersection)) {
                        retMeasure.addEntry(intersection, value);
                    }
                }
            }
        }
        return retMeasure;
    }

    /**
     * Checks if a List contains only 1s, identifying the omega-entry
     * @param entryValues the list to be checked
     * @return {@link true} if the List contains only 1s
     */
    private boolean isOmegaEntry(List<Integer> entryValues) {
        for (Integer entry : entryValues) {
            if (entry != 1) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if a List contains only 0s, identifying it as an empty List as a result of a conflict
     * @param entryValues the list to be checked
     * @return {@link true} if the List contains only 0s
     */
    private boolean entryIsEmpty(List<Integer> entryValues) {
        for (Integer entry : entryValues) {
            if (entry != 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * Creates an intersection of 2 {@link MeasureEntry}s
     * @param entry1 first {@MeasureEntry}
     * @param entry2 second {@MeasureEntry}
     * @return a List representing the intersection. Can be used to create a new {@link MeasureEntry}
     */
    private List<Integer> getIntersection(MeasureEntry entry1, MeasureEntry entry2) {
        List<Integer> retList = new ArrayList<Integer>();
        for(int i = 0; i<this.size; i++) {
            int i1 = entry1.getValues().get(i);
            int i2 = entry2.getValues().get(i);
            if ((i1 + i2) == 2) {
                retList.add(1);
            }
            else {
                retList.add(0);
            }
        }
        return retList;
    }

    /**
     * Calculates the conflict-value of an accumulation of two {@link Measure}s
     * @param measure1 first {@link Measure}
     * @param measure2 second {@link Measure}
     * @return the value of the conflict
     */
    private double getConflict(Measure measure1, Measure measure2) {
        double conflict = 0.0d;
        for (MeasureEntry me1: measure1.getMeasureEntrys())
        {
            for (MeasureEntry me2 : measure2.getMeasureEntrys()) {
                if (this.entryIsEmpty(this.getIntersection(me1, me2))) {
                    conflict = conflict + (me1.getProbability() * me2.getProbability());
                }
            }
        }
        return conflict;
    }
}
