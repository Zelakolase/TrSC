package algorithms;

import java.util.ArrayList;

import lib.Graph;

/**
 * G.P.I : General Performance Indicator
 * Used to quantitatively represent clustering performance of a certain algorithm based on its clusters.
 * The indicator has interval [0.0,1.0], where 1.0 represents perfect clustering
 * @author Morad A.
 */
public class GPI {
    
    /**
     * Calculates G.P.I. 
     * @param clusters List of detected clusters by the clustering algorithm
     * @param mainGraph The original (big) graph
     * @param idealClusterSize The ideal number of nodes per cluster
     * @return The GPI value of the algorithm performance based on the supplied information
     */
    public static double calculate(ArrayList<Graph> clusters, Graph mainGraph, double idealClusterSize) {
        // a (-stdev(GCR)) + a(mean(GCR)) - b(stdev(RC)) + b(mean(RC)), then all over 2

        ArrayList<Double> GCRs = new ArrayList<>(); // All GCR Values for all clusters
        ArrayList<Double> RCs = new ArrayList<>(); // All RC Values for all clusters
        double mCS = 0.0; // Mean cluster size

        for(Graph cluster : clusters) {
            if(cluster.G.keySet().size() > 2) { // If the number of nodes is higher than 2
                GCRs.add(Stats.GCR(cluster));
                RCs.add(Stats.RC(cluster));
                mCS = mCS + cluster.G.keySet().size();
            }else continue;
        }

        mCS = mCS / clusters.size();

        double meanGCRs = Stats.mean(GCRs); double stdevGCRs = Stats.stdev(GCRs, meanGCRs);
        double meanRCs = Stats.mean(RCs); double stdevRCs = Stats.stdev(RCs,meanRCs);

        // Most Important (0.7)
        double GPI = ((-stdevGCRs) * 0.7);
        GPI = GPI + (meanGCRs * 0.7);
        // Important (0.3)
        GPI = GPI + ((-stdevRCs) * 0.3);
        GPI = GPI + (meanRCs * 0.3);

        GPI = GPI / 2.0;

        return GPI;
    }
}
