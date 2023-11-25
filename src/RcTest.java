

import java.util.ArrayList;


import java.util.*;

import algorithms.Clustering;
import lib.GORead;
import lib.Graph;
import lib.SparkDB;

public class RcTest {
    static Graph G = new GORead("../data/GO:0040007/interactions.tsv", "node1", "node2", "combined_score").G;
    public static void main(String[] args) {
        /* CliXO */
        SparkDB db = new SparkDB();
        try {
            db.readFromFile("../data/GO:0040007/clixo.csv");
        } catch (Exception e) {
            // Shhhh....
        }

        ArrayList<String> clustersun = db.getColumn("CD_Labeled");
        ArrayList<ArrayList<String>> Cs = new ArrayList<>();
        for (String st : clustersun) {
            String[] cluster = st.split("\\s+");
            ArrayList<String> c = new ArrayList<>();
            Collections.addAll(c, cluster);
            Cs.add(c);
        }

        ArrayList<Graph> GraphClusters = new ArrayList<>();
        for(ArrayList<String> nodeList : Cs) {
            GraphClusters.add(G.subcluster(nodeList));
        }
        analyze(GraphClusters, "CliXO");

        /* TrSC */
        Clustering obj = new Clustering(G);
        ArrayList<Graph> results = obj.cluster(0.45, 0.1, 1.0, 0.85);
        analyze(results, "TrSC");
    }

    public static void analyze(ArrayList<Graph> al, String alg) {
        /* Measure avg old GCR */
        double avgOGCR = 0;
        double Rc = 0;
        for(Graph inG : al) {
            int nNodes = inG.G.keySet().size();
            if(nNodes < 2) continue;
            double oneAvg = 0;
            ArrayList<HashSet<String>> proccessedEdges = new ArrayList<>();
        for(String node1 : inG.G.keySet()) {
            

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
                    oneAvg += inG.getWeight(node2, node1);
                    proccessedEdges.add(pair); // Consider the edge processed
                }
            }
        }
        avgOGCR += oneAvg / ((nNodes * (nNodes - 1))/2);
        Rc += inG.Ne / ((nNodes * (nNodes - 1))/2);
        }

        avgOGCR /= al.size();
        Rc /= al.size();
        /* Print */
        System.out.println(alg + " has avgOldGCR: " + avgOGCR + ", and avgRc: " + Rc);
    }
}
