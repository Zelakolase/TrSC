package algorithms;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.stream.Collectors;

import lib.Graph;

/**
 * Classification for undirected weighted graph
 * 
 * @author Morad A.
 *         NOTE: COV is a deprecated term
 */
public class TfSCA {
    public Graph G;
    public double headCutoff, unclusteredCutoff, loopMultiplier;
    public boolean moreClusters = false;
    public ArrayList<String> unclustered;
    public ArrayList<String> heads;
    public boolean moreClusterss = false;

    public TfSCA(Graph tG, double tHC, double tUC, double tLM, boolean moreClusters, boolean moreClusterss) {
        G = tG;
        headCutoff = tHC;
        unclusteredCutoff = tUC;
        tLM = loopMultiplier;
        this.moreClusters = moreClusters;
        this.moreClusterss = moreClusterss;
    }

    public ArrayList<ArrayList<String>> run() {
        ArrayList<ArrayList<String>> clusters = new ArrayList<>();
        /* Triangle Amplification */
        ArrayList<String> tempNodes = new ArrayList<>();
        tempNodes.addAll(G.nodes.keySet()); // All nodes are either 'n', 'nOD', or 'Cx'
        while (tempNodes.size() > 0) {
            String n = tempNodes.get(0);
            // Get All outdegrees for looping
            ArrayList<String> nOutDegrees = G.getOutdegree(n);
            // Get All Indegrees
            ArrayList<String> nInDegrees = G.getIndegree(n);

            // nOD: nOutDegree
            for (String nOD : nOutDegrees) {
                // Define S2 as indeg/outdeg of each node n
                ArrayList<String> S2OutDegrees = G.getOutdegree(nOD);
                ArrayList<String> S2InDegrees = G.getIndegree(nOD);
                /*
                 * Find common element(s) [Cx] in one of 4 cases while n -> nOD:
                 * 1. n -> Cx -> nOD , set denoted by Cx1
                 * 2. n -> Cx <- nOD , set denoted by Cx2
                 * 3. n <- Cx -> nOD , set denoted by Cx3
                 * 4. n <- Cx <- nOD , set denoted by Cx4
                 * If Cx appears in more than one case, one case is considered
                 */

                ArrayList<String> excludedCx = new ArrayList<>();
                // 1
                ArrayList<String> Cx1 = new ArrayList<>(
                        nOutDegrees.stream().filter(S2InDegrees::contains).collect(Collectors.toList()));
                Cx1.removeIf(excludedCx::contains);
                for (String Cx1E : Cx1) {
                    G.multiplyWeight(n, Cx1E, loopMultiplier);
                    G.multiplyWeight(Cx1E, nOD, loopMultiplier);
                    excludedCx.add(Cx1E);
                }
                // 2. n -> Cx <- nOD , set denoted by Cx2
                ArrayList<String> Cx2 = new ArrayList<>(
                        nOutDegrees.stream().filter(S2OutDegrees::contains).collect(Collectors.toList()));
                Cx2.removeIf(excludedCx::contains);
                for (String Cx2E : Cx2) {
                    G.multiplyWeight(n, Cx2E, loopMultiplier);
                    G.multiplyWeight(nOD, Cx2E, loopMultiplier);
                    excludedCx.add(Cx2E);
                }
                // 4. n <- Cx <- nOD , set denoted by Cx4
                ArrayList<String> Cx4 = new ArrayList<>(
                        nInDegrees.stream().filter(S2OutDegrees::contains).collect(Collectors.toList()));
                Cx4.removeIf(excludedCx::contains);
                for (String Cx4E : Cx4) {
                    G.multiplyWeight(Cx4E, n, loopMultiplier);
                    G.multiplyWeight(nOD, Cx4E, loopMultiplier);
                    excludedCx.add(Cx4E);
                }
                // 3. n <- Cx -> nOD , set denoted by Cx3
                ArrayList<String> Cx3 = new ArrayList<>(
                        nInDegrees.stream().filter(S2InDegrees::contains).collect(Collectors.toList()));
                Cx3.removeIf(excludedCx::contains);
                for (String Cx3E : Cx3) {
                    G.multiplyWeight(Cx3E, n, loopMultiplier);
                    G.multiplyWeight(Cx3E, nOD, loopMultiplier);
                    excludedCx.add(Cx3E);
                }

                tempNodes.removeAll(excludedCx);
                tempNodes.remove(nOD);
            }

            tempNodes.remove(n); // Remove n
        }
        /* 1. Node property for COV calculation */
        COVCalculation();
        /* 2. Classify Unclustered and head node names */
        heads = headClassify(G.nodes.keySet());
        unclustered = unclusteredClassify();
        ArrayList<String> clusteredNodes = new ArrayList<>();
        /* 3. Iterate over head */
        for (String head : heads) {
            HashMap<String, Double> DegreeWeights = new HashMap<>();
            ArrayList<String> headOutDegrees = G.getOutdegree(head);
            ArrayList<String> headInDegrees = G.getIndegree(head);

            for (String out : headOutDegrees)
                DegreeWeights.put(out, G.getWeight(head, out));
            for (String in : headInDegrees)
                DegreeWeights.put(in, G.getWeight(in, head));

            List<String> topKeys = DegreeWeights.entrySet().stream()
                    .filter(e -> e.getValue() >= DegreeWeights.values().stream().sorted()
                            .skip((int) (Math.ceil(DegreeWeights.size() * 0.5))).findFirst().orElse(-1.0))
                    .map(Map.Entry::getKey).collect(Collectors.toList());

            topKeys.add(head);

            clusters.add(new ArrayList<>(topKeys));
            clusteredNodes.addAll(topKeys);
        }

        if (!moreClusters)
            return clusters;

        /* If we want more clusters */
        ArrayList<String> leftOverNodes = new ArrayList<>(G.nodes.keySet());
        leftOverNodes.removeAll(heads);
        leftOverNodes.removeAll(clusteredNodes);

        while (leftOverNodes.size() > 0) {
            ArrayList<String> headss = headClassify(new HashSet<String>(leftOverNodes));
            for (String loN : headss) {
                /* Find outdegrees and their weight */
                ArrayList<String> headOutDegrees = G.getOutdegree(loN);
                ArrayList<String> headInDegrees = G.getIndegree(loN);
                // headInDegrees.removeAll(unclustered);
                // headOutDegrees.removeAll(unclustered);
                HashMap<String, Double> headOutDegreeWeights = new HashMap<>();
                for (String outDegree : headOutDegrees)
                    headOutDegreeWeights.put(outDegree, G.getWeight(loN, outDegree));
                for (String outDegree : headInDegrees)
                    headOutDegreeWeights.put(outDegree, G.getWeight(outDegree, loN));
                /* Find highest 50% nodes with weights */
                List<String> topKeys = headOutDegreeWeights.entrySet().stream()
                        .filter(e -> e.getValue() >= headOutDegreeWeights.values().stream().sorted()
                                .skip((int) (Math.ceil(headOutDegreeWeights.size() * 0.5))).findFirst().orElse(0.0))
                        .map(Map.Entry::getKey).collect(Collectors.toList());
                /* Cluster them */
                topKeys.add(loN);
                clusters.add(new ArrayList<>(topKeys));
                clusteredNodes.addAll(topKeys);
            }

            leftOverNodes.removeAll(clusteredNodes);
        }

        return clusters;
    }

    public void COVCalculation() {
        /* 1. Iterate over all nodes */
        for (String node : G.nodes.keySet()) {
            ArrayList<Double> weights = new ArrayList<>();

            ArrayList<String> Indegrees = G.getIndegree(node);
            ArrayList<String> Outdegrees = G.getOutdegree(node);

            for (String indegree : Indegrees)
                weights.add(G.getWeight(indegree, node));
            for (String outdegree : Outdegrees)
                weights.add(G.getWeight(node, outdegree));

            double sum = 0.0;
            for (double num : weights)
                sum += num;
            double mean = sum / weights.size();

            double variance = 0.0;
            for (double num : weights)
                variance += Math.pow(num - mean, 2);

            /*
             * You can change the formula to see if accuracy is better
             * If you have an equation that can result in a better accuracy, please contact
             * me at Zelakolase@tuta.io
             * weights.size is the number of edges (indeg/outdeg) for a specific node
             */
            double stddev = mean - 1.25*Math.sqrt(variance);

            // COV is a deprecated term, too lazy to change it tho
            G.nodes.get(node).put("COV", String.valueOf(stddev));
        }
    }

    public ArrayList<String> headClassify(Set<String> nodes) {
        ArrayList<String> out = new ArrayList<String>();
        /* 1. Find the N highest COV nodes */
        PriorityQueue<String> pq = new PriorityQueue<>(G.nodes.size(), new Comparator<String>() {
            public int compare(String node1, String node2) {
                double COV1 = Double.parseDouble(G.nodes.get(node1).get("COV"));
                double COV2 = Double.parseDouble(G.nodes.get(node2).get("COV"));
                return (int) Math.round(COV2 - COV1);
            }
        });

        /* Adds nodes to the custom comparator */
        for (String node : nodes) {
            pq.offer(node);
        }

        /* Draws N nodes from custom comparator */
        for (int i = 0; i < (headCutoff * nodes.size()) && !pq.isEmpty(); i++) {
            out.add(pq.poll());
        }

        return out;
    }

    // Unclassified is deprecated term, it is transition proteins
    public ArrayList<String> unclusteredClassify() {
        ArrayList<String> out = new ArrayList<String>();
        /* 1. Find the N highest COV nodes */
        PriorityQueue<String> pq = new PriorityQueue<>(G.nodes.size(), new Comparator<String>() {
            public int compare(String node1, String node2) {
                double COV1 = Double.parseDouble(G.nodes.get(node1).get("COV"));
                double COV2 = Double.parseDouble(G.nodes.get(node2).get("COV"));
                return (int) Math.round(COV1 - COV2);
            }
        });

        /* Adds nodes to the custom comparator */
        for (String node : G.nodes.keySet()) {
            if (G.getIndegree(node).size() + G.getOutdegree(node).size() > 1)
                pq.offer(node);
        }

        /* Draws N nodes from custom comparator */
        for (int i = 0; i < (unclusteredCutoff * G.nodes.size()) && !pq.isEmpty(); i++) {
            out.add(pq.poll());
        }

        return out;
    }
}