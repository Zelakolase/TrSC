import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import algorithms.Clustering;
import lib.GORead;
import lib.Graph;

/**
 * Sorted single GO clustering with statistical output
 * @author Morad
 */
public class SingleGO {
    public static void main(String[] args) {
        long AllF = System.nanoTime();
        double headCutoff = 0.35;
        double transitionCutoff = 0.15;
        double selectPercent = 0.75;

        Graph G = new GORead("../data/GO:0000003/interactions.tsv", "node1", "node2", "combined_score").G;
        //Graph G = new GORead("../data/ALS.tsv", "node1", "node2", "combined_score").G;
        Clustering obj = new Clustering(G);
        long TimeF = System.nanoTime();
        ArrayList<Graph> results = obj.cluster(headCutoff, transitionCutoff, 1.0, selectPercent);
        TimeF = System.nanoTime() - TimeF;

        double meanClusterSize = 0;
        double n = 0; // Number of clusters
        ArrayList<Double> GCRs = new ArrayList<>(); // Value for each cluster
        HashMap<Double, Set<String>> table = new HashMap<>();
        double nOverHalf = 0; // Number of clusters over or equal GCR:0.5

        for(Graph sub : results) {
            double GCR = algorithms.ClusterStats.GCR(sub);
            if(sub.G.keySet().size() > 1) {
                table.put(GCR, sub.G.keySet());
                GCRs.add(GCR);
                meanClusterSize += sub.G.size();
                n++;
            }
            
            if(GCR >= 0.5) nOverHalf ++;
        }
        meanClusterSize /= n;

        double sum = 0.0;
        for (double num : GCRs) sum += num;
        double mean = sum / GCRs.size();

        double variance = 0.0;
        for (double num : GCRs) variance += Math.pow(num - mean, 2);
        double stddev = Math.sqrt(variance / GCRs.size());

        sort(table);

        System.out.println("\nGCR-STDEV: " + stddev + " , GCR-MEAN: " + mean + " , NUM_CLUSTERS: " + n + " , MEAN_CLUSTER_SIZE: " + meanClusterSize + " , GCR-PERCENT_OVER_0.5: " + (nOverHalf / n) + " , NUM_UNIQUE_PROTEINS: " + G.G.keySet().size());

        System.out.println("\nTransition proteins: " + obj.transitionNodes);

        System.out.println("\nAll proteins: " + G.G.keySet());

        System.out.println("\nApplication took: " + (System.nanoTime()-AllF)/1_000_000.0d + " ms");
        System.out.println("\nClustering took: " + TimeF/1_000_000.0d + " ms");
    }

    /**
     * Sort a hashmap ascendingly, then print output
     * @param in Input HashMap
     */
    public static void sort(HashMap<Double, Set<String>> in) {
        // Create a list of the HashMap's entries.
        List<Map.Entry<Double, Set<String>>> entries = new ArrayList<>(in.entrySet());

        // Sort the list of entries using a custom comparator that compares the Double keys of the entries.
        Collections.sort(entries, new Comparator<Map.Entry<Double, Set<String>>>() {
            @Override
            public int compare(Map.Entry<Double, Set<String>> o1, Map.Entry<Double, Set<String>> o2) {
                return o1.getKey().compareTo(o2.getKey());
            }
        });

        for (Map.Entry<Double, Set<String>> entry : entries) {
            /* Use the commented line to get GCR values alongside each cluster */
            //System.out.println(entry.getValue() + " GCR: " + entry.getKey());
            System.out.println(entry.getValue());
        }
    }
}
