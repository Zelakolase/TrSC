package lib;

import java.util.ArrayList;
import java.util.Collections;

public class Statistics {
    /**
     * Calculates and prints statistics
     * @param data Input datapoints
     */
    public static void printStats(ArrayList<Double> data, double nNodes) {
        // Compute max value
        double max = Collections.max(data);
        System.out.print(max+",");

        // Compute min value
        double min = Collections.min(data);
        System.out.print(min+",");

        // Compute mean value
        double sum = 0.0;
        for (double num : data) sum += num;
        double mean = sum / data.size();
        System.out.print(mean+",");

        // Compute standard deviation
        double variance = 0.0;
        for (double num : data) variance += Math.pow(num - mean, 2);
        double stddev = Math.sqrt(variance / data.size());
        System.out.print(stddev+",");

        // Compute coefficient of variation
        double cv = (stddev / mean);
        System.out.print(cv * 100 + "%"+",");

        // Number of elements
        System.out.print(data.size()+",");

        // Percentage of CCs higher than 0.25 (usually significantly enriched)
        double e = 0;
        for(double elem : data) if(elem >= 0.25) e++;
        System.out.print(e/data.size()+",");

        // Score
        double score = 0;
        score += (data.size() / nNodes) * 0.2;
        score += cv * 0.8;
        score += mean * 1;
        score += max * 0.6;
        score += min * 0.4;
        System.out.println(score);
    }
}