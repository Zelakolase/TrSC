package algorithms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import lib.Graph;

@SuppressWarnings("unchecked")
public class Clustering {
    public Graph G;
    public ArrayList<String> headNodes = new ArrayList<>();
    public ArrayList<String> transitionNodes = new ArrayList<>();

    public Clustering(Graph inG) {
        G = inG;
    }

    /**
     * Makes the clustering process
     * @param headCutoff The percentage of nodes to be heads, based on the highest ranking. [0.1:10%,..]
     * @param transitionCutoff The percentage of nodes to be considered transitionary, based on the lowest ranking. [0.1:10%,..]
     * @param selectPercent Percentage of edges for head to be included in cluster [1.0:100%]
     * @return An array list of sub-graphs of clusters
     */
    public  ArrayList<Graph> cluster(double headCutoff, double transitionCutoff, double selectPercent) {
        ArrayList<Graph> subGraphs = new ArrayList<>();
        Set<String> leftoverNodes = new HashSet<>(G.G.keySet());
        leftoverNodes.removeAll(headNodes);
        leftoverNodes.removeAll(transitionNodes);

        // 1. Calculate ranking equation for nodes
        rankEqCalc();
        // 2. Classify heads and transition, based on headCutoff and transitionCutoff
        rank(headCutoff, transitionCutoff);
        // 3. Iterate over heads
        for(String headNodeName : headNodes) {
            /* Key: Connected Node Name | Value: The edge weight between the key and headNodeName */
            HashMap<String, Double> connectedNodes = G.G.get(headNodeName).get(1);
            // 3.1. Find top selectPercent% highest edges
            List<String> topKeys = connectedNodes.entrySet().stream()
                    .filter(e -> e.getValue() >= connectedNodes.values().stream().sorted()
                            .skip((int) (Math.ceil(connectedNodes.size() * selectPercent))).findFirst().orElse(-1.0))
                    .map(Map.Entry::getKey).collect(Collectors.toList());
            topKeys.add(headNodeName); // Add the head node to the cluster

            leftoverNodes.removeAll(topKeys);
            subGraphs.add(G.subcluster(topKeys));
        }
        // 4. Iterate over Leftover (Unprocessed nodes)
        while(leftoverNodes.size() > 0) {
            /* Key: Connected Node Name | Value: The edge weight between the key and headNodeName */
            HashMap<String, Double> connectedNodes = G.G.get(leftoverNodes.iterator().next()).get(1);
            // 3.1. Find top 50% highest edges
            List<String> topKeys = connectedNodes.entrySet().stream()
                    .filter(e -> e.getValue() >= connectedNodes.values().stream().sorted()
                            .skip((int) (Math.ceil(connectedNodes.size() * 0.5))).findFirst().orElse(-1.0))
                    .map(Map.Entry::getKey).collect(Collectors.toList());
            topKeys.add(leftoverNodes.iterator().next()); // Add the head node to the cluster

            leftoverNodes.removeAll(topKeys);
            subGraphs.add(G.subcluster(topKeys));
        }

        return subGraphs;
    }

    /**
     * Ranks the nodes based on an equation
     */
    private void rankEqCalc() {
        for(String node : G.G.keySet()) {
            /* Equation: 2*mean(weights) - 1.5*stdev(weights) */
            
            // 0. Remove rankValue property if it exists
            G.deleteProperty(node, "rankValue");
            // 1. Get all connected nodes and their weights
            ArrayList<Double> weights = new ArrayList<>();
            HashSet<String> connectedNodeNames = new HashSet<>(G.getConnectedNodes(node));
            for(String endNodeName : connectedNodeNames) weights.add(G.getWeight(endNodeName, node));
            // 2. Calculate meanWeights
            double meanWeights = Stats.mean(weights);
            // 3. Calculate the stdev
            double stdevWeights = Stats.stdev(weights, meanWeights);
            // 4. Insert property

            /**
             * meanWeights - stdevWeights + [nodeDegree / maxNodeDegree]
             */
            G.addProperty(node, "rankValue", String.valueOf(meanWeights - stdevWeights + (weights.size() / Double.parseDouble("" + (G.G.keySet().size() - 1)))));
        }
    }

    /**
     * Ranks nodes based on rankValue property
     * @param headCutoff The percentage of nodes to be heads, based on the highest ranking. [0.1:10%,..]
     * @param transitionCutoff The percentage of nodes to be considered transitionary, based on the lowest ranking. [0.1:10%,..]
     */
    public  void rank(double headCutoff, double transitionCutoff) {
        // 0. New headNodes and transitionNodes objects
        headNodes = new ArrayList<>();
        transitionNodes = new ArrayList<>();
        // 1. Node key set to array
        String[] nodes = G.G.keySet().toArray(new String[G.G.size()]);
        // 2. Sort array based on rankValue
        Arrays.sort(nodes, (node1, node2) -> {
            double rankVal1 = Double.parseDouble(G.getProperty((String) node1, "rankValue"));
            double rankVal2 = Double.parseDouble(G.getProperty((String) node2, "rankValue"));
            return Double.compare(rankVal2, rankVal1); // Descending order
        });
        // 3. Get Heads
        int topIndex = (int) Math.ceil(headCutoff * nodes.length);
        headNodes.addAll(Arrays.asList(Arrays.copyOfRange(nodes, 0, topIndex)));
        // 4. Get Transitions
        int bottomIndex = (int) Math.ceil(transitionCutoff * nodes.length);
        transitionNodes.addAll(Arrays.asList(Arrays.copyOfRange(nodes, nodes.length - bottomIndex, nodes.length)));
    }
}
