package algorithms;

import java.util.ArrayList;


import lib.Graph;

public class clusteringCoefficient {
    private Graph G;

    public clusteringCoefficient(Graph tempG) {
        G = tempG;
    }

    public double run(boolean reciprocal) {
        double result = 0.0;

        /* 1. Calculate all nodes clustering coefficients */
        double cGross = 0.0, cMax = 0.0;
        for(String node : G.nodes.keySet()) {
            ArrayList<String> indegree = G.getIndegree(node);
            ArrayList<String> outdegree = G.getOutdegree(node);
            int degree = indegree.size() + outdegree.size();
            double sumWeights = 0;
            
            for(String otherNode : indegree) sumWeights += G.getWeight(otherNode, node);
            for(String otherNode : outdegree) sumWeights += G.getWeight(node, otherNode);

            /* 2. sum them to cGross */
            cGross += (sumWeights * degree);
            /* 3. Calculate cMax */
            int maxDegree;
            if(! reciprocal) maxDegree = (G.nodes.size() - 1);
            else maxDegree = (G.nodes.size() - 1);
            double maxWeight = maxDegree * 1.0;
            cMax += maxWeight * maxDegree;
        }
        /* result = cGross / cMax */
        result = cGross / cMax;

        return result;
    }
}
