package algorithms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import lib.Graph;

/**
 * Graph Completeness Ratio Calculation
 * @author Morad
 */
@SuppressWarnings("unchecked")
public class Stats {
    /* SumWeights over SumMaxWeights */
    public static double GCR(Graph G) {
        double GCR = 0;
        // A. Calculate Denominator [max. num. possible edges] n(n-1) / 2
        int nNodes = G.G.keySet().size();
        double denominator = (nNodes * (nNodes - 1)) / 2;
        // B. Calculate Numerator [Sum of all edge weights]
        double numerator = 0;
        // B.0. Keep track of processed edges so that we don't duplicate
        ArrayList<HashSet<String>> proccessedEdges = new ArrayList<>();
        // B.1. Iterate over all nodes
        for(String node1 : G.G.keySet()) {
            // B.1.1. Get all edges for node1
            /* Key: Other Node name end | Value: Edge weight */
            HashMap<String, Double> nodeEdges = G.G.get(node1).get(1);
            // B.1.2. Iterate over all these edges
            inner: for(String node2 : nodeEdges.keySet()) {
                // B.1.2.1. Make an unordered pair of node1 and node2
                HashSet<String> pair = new HashSet<>();
                pair.add(node1); pair.add(node2);
                // B.1.2.2. Check if this pair of nodes (ie. edge) has been processed before
                if(proccessedEdges.contains(pair)) continue inner;
                else {
                    numerator += G.getWeight(node2, node1);
                    proccessedEdges.add(pair); // Consider the edge processed
                }
            }
        }
        
        // C. Calculate GCR
        GCR = numerator / denominator;

        return GCR;
    }

    /* N.Edges over N.Max.Edges */
    public static double RC(Graph G) {
        int nNodes = G.G.keySet().size();
        if(nNodes < 2) return 0;
        return (double) G.Ne / (double) ((nNodes * (nNodes - 1)) / 2);
    }

    /* Calculates the mean value */
    public static double mean(ArrayList<Double> values) {
        double mean = 0.0;
        for(double value : values) mean += value;
        mean = mean / (double) values.size();

        return mean;
    }

    /* Calculates standard deviation */
    public static double stdev(ArrayList<Double> values, double mean) {
        double variance = 0.0;
        for (double num : values) variance += Math.pow(num - mean, 2);
        double stdev = Math.sqrt(variance / values.size());

        return stdev;
    }
}
