import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import algorithms.Clustering;
import algorithms.GPI;
import lib.GORead;
import lib.Graph;

/**
 * Sorted single GO clustering with statistical output
 * @author Morad
 */
public class SingleGO {
    public static void main(String[] args) {
        long AllF = System.nanoTime();
        double headCutoff = 0.4;
        double transitionCutoff = 0.1;
        double selectPercent = 0.95;

        Graph G = new GORead("../data/GO:0042592/interactions.tsv", "node1", "node2", "combined_score").G;
        //Graph G = new GORead("../data/ALS.tsv", "node1", "node2", "combined_score").G;
        Clustering obj = new Clustering(G);
        long TimeF = System.nanoTime();
        ArrayList<Graph> results = obj.cluster(headCutoff, transitionCutoff, selectPercent);
        TimeF = System.nanoTime() - TimeF;

        /* This table has ArrayList of GCR and RC, then a Set of node names for each cluster */
        HashMap<ArrayList<Double>, Set<String>> table = new HashMap<>();

        for(Graph sub : results) {
            double GCR = algorithms.Stats.GCR(sub);
            double RC = algorithms.Stats.RC(sub);
            if(sub.G.keySet().size() > 2) table.put(new ArrayList<>() {{add(RC); add(GCR);}}, sub.G.keySet());
        }

        sort(table, G);

        System.out.println("\nTransition proteins: " + obj.transitionNodes);

        System.out.println("\nAll proteins: " + G.G.keySet());

        System.out.println("Number of unique proteins: " + G.G.keySet().size() + ", GPI: " + GPI.calculate(results, G, 7) * 100 + "%");

        System.out.println("\nApplication took: " + (System.nanoTime()-AllF)/1_000_000.0d + " ms");
        System.out.println("\nClustering took: " + TimeF/1_000_000.0d + " ms");
    }

    /**
     * Sort a hashmap ascendingly, then print output
     * @param in Input HashMap
     */
    public static void sort(HashMap<ArrayList<Double>, Set<String>> in, Graph G) {
        // Create a list of the HashMap's entries.
        List<Map.Entry<ArrayList<Double>, Set<String>>> entries = new ArrayList<>(in.entrySet());

        // Sort the list of entries using a custom comparator that compares the Double keys of the entries.
        Collections.sort(entries, new Comparator<Map.Entry<ArrayList<Double>, Set<String>>>() {
            @Override
            public int compare(Map.Entry<ArrayList<Double>, Set<String>> o1, Map.Entry<ArrayList<Double>, Set<String>> o2) {
                /* o1.getKey.get(0) is GCR */
                /* o1.getKey.get(1) is RC */
                /* Last term is ratio of nodes in cluster to the whole cluster, 10% means that cluster has 10% of all nodes in network */
                /* 2.5 coefficient means that for (f/s)V value to increase, GCR needs to be 0.1 higher or RC to be 0.25 higher */
                double fV = 2.5 * o1.getKey().get(0) + o1.getKey().get(1) + (o1.getValue().size() / G.G.keySet().size());
                double sV = 2.5 * o2.getKey().get(0) + o2.getKey().get(1) + (o2.getValue().size() / G.G.keySet().size());

                if(fV == sV) return 0;
                if(fV < sV) return -1;
                return 1;
            }
        });

        for (Map.Entry<ArrayList<Double>, Set<String>> entry : entries) System.out.println(entry.getValue());
    }
}
