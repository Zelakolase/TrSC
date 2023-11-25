import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import algorithms.Clustering;
import lib.GORead;
import lib.Graph;
import lib.SparkDB;

public class App {
    public static void main(String[] args) {
        /* 0. Constants */
        double headCutoff = 0.35;
        double transitionCutoff = 0.1;
        double selectPercent = 1.0;
        /* 1. GO IDs */
        ArrayList<String> GOIDs = new ArrayList<>();
        GOIDs.add("GO:0016032"); GOIDs.add("GO:0022414"); GOIDs.add("GO:0040007"); GOIDs.add("GO:0000003"); 
        GOIDs.add("GO:0040011"); GOIDs.add("GO:0042592"); GOIDs.add("GO:0043473"); GOIDs.add("GO:0098754");
        /* 2. Run function */
        for(String ID : GOIDs) run(ID, headCutoff, transitionCutoff, selectPercent);
    }

    /**
     * Process a single GO ID
     */
    public static void run(String GOID, double headCutoff, double transitionCutoff, double selectPercent) {
        /* 0. Constants */
        String GOFolderPath = "../data/" + GOID;
        // Key: Mean/ClusterSize/etc.. | Value: List of 2-tuples (algorithm, value)
        HashMap<String, ArrayList<Tuple>> resultTable = new HashMap<>();
        resultTable.put("mean", new ArrayList<>());
        resultTable.put("mean-stdev", new ArrayList<>());
        resultTable.put("n", new ArrayList<>());
        resultTable.put("clustersize", new ArrayList<>());
        resultTable.put("meanRc", new ArrayList<>());
        resultTable.put("mean-stdev Rc", new ArrayList<>());
        // Files of algorithm outputs
        ArrayList<String> fileNames = new ArrayList<>();
        fileNames.add("louvain.csv"); fileNames.add("mcl.csv");
        fileNames.add("clixo.csv"); fileNames.add("infomap.csv");
        fileNames.add("TrSC");
        /* 1. Import Interactions */
        Graph G = new GORead(GOFolderPath + "/interactions.tsv", "node1", "node2", "combined_score").G;
        /* 2. Make a thread for each algorithm */
        for(String name : fileNames) {
                if(name.equals("TrSC")) {
                    /* Cluster using TrSC */
                    Clustering obj = new Clustering(G);
                    ArrayList<Graph> results = obj.cluster(headCutoff, transitionCutoff, 1.0, selectPercent);
                    insertClusterData(results, resultTable, "TrSC");
                }

                else if(name.equals("mcl.csv")) {
                    /* MCL Parsing */
                    SparkDB MCL = new SparkDB();
                    try {
                        MCL.readFromFile(GOFolderPath + "/" + name);
                    } catch (Exception e) {
                        // Shhhh....
                    }
                    ArrayList<ArrayList<String>> Clusters = new ArrayList<>();

                    for (int i = 0; i < MCL.num_queries; i++) Clusters.add(new ArrayList<>());

                    for (int index = 0; index < MCL.num_queries; index++) {
                        HashMap<String, String> row = MCL.get(index);
                        try {
                            Clusters.get(Integer.parseInt(row.get("__mclCluster"))).add(row.get("name"));
                        } catch (Exception e) {
                            // Drop lines with malformed structure
                        }
                    }

                    ArrayList<Graph> GraphClusters = new ArrayList<>();
                    for(ArrayList<String> nodeList : Clusters) {
                        GraphClusters.add(G.subcluster(nodeList));
                    }

                    insertClusterData(GraphClusters, resultTable, "MCL");                    
                }

                else {
                    /* Louvain/CliXO/Infomap */
                    SparkDB db = new SparkDB();
                    try {
                        db.readFromFile(GOFolderPath + "/" + name);
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

                    insertClusterData(GraphClusters, resultTable, name.split("\\.")[0].toUpperCase()); 
                }
        }
        /* 3. Wait till all tasks are finished */
        while(Thread.activeCount() > 1) {}
        /* 4. Sort the results */
        HashMap<String, ArrayList<Tuple>> sortedResults = new HashMap<>();
        for(Map.Entry<String, ArrayList<Tuple>> e : resultTable.entrySet()) {
            ArrayList<Tuple> sorted = new ArrayList<>(e.getValue());

            Collections.sort(sorted, new Comparator<Tuple>() {
                @Override
                public int compare(Tuple t1, Tuple t2) {
                    if (e.getKey().equals("clustersize")) {
                        // Sort by value ascending if the key is "clustersize"
                        /* Lower cluster size means more modular and analyzable clusters */
                        return Double.compare(t1.value, t2.value);
                    } else {
                        // Sort by value descending for other keys
                        return Double.compare(t2.value, t1.value);
                    }
                }
            });

            sortedResults.put(e.getKey(), sorted);
        }
        /* 5. Print the results */
        System.out.println(GOID);
        System.out.println("MeanGCR,,MeanGCR-StdevGCR,,NumberOfClusters,,MeanClusterSize,,MeanRc,,MeanRc-StdevRc");
        System.out.println("Algorithm,Value,Algorithm,Value,Algorithm,Value,Algorithm,Value,Algorithm,Value,Algorithm,Value");
        for(int i = 0;i < 5; i++) {
            HashMap<String, String> vals = new HashMap<>();
            for(Map.Entry<String, ArrayList<Tuple>> s : sortedResults.entrySet()) {
                vals.put(s.getKey(), s.getValue().get(i).Algorithm + "," + s.getValue().get(i).value);
            }

            System.out.println(vals.get("mean") + "," + vals.get("mean-stdev") + "," + vals.get("n") + "," + vals.get("clustersize") + "," + vals.get("meanRc") + "," + vals.get("mean-stdev Rc"));
        }
    }

    /**
     * Make statistical calculation for a list of clusters, then insert to results table
     * @param clusters Clusters to be considered
     * @param G Main graph object
     * @param resultsTable Target results table
     * @param algorithm Algorithm name
     */
    public  static void insertClusterData(ArrayList<Graph> clusters, HashMap<String, ArrayList<Tuple>> resultsTable, String algorithm) {
        ArrayList<Double> GCRs = new ArrayList<>(); // Value for each cluster
        ArrayList<Double> Rcs = new ArrayList<>(); // Value for each cluster
        
        
        double meanClusterSize = 0;
        double n = 0; // Number of clusters

        for(Graph subcluster : clusters) {
            double GCR = algorithms.ClusterStats.GCR(subcluster);
            /* Consider clusters with 4 proteins or more + null check */
            if(subcluster.G.size() >= 4 && GCR == GCR) {
                double Rc = algorithms.ClusterStats.Rc(subcluster);
                GCRs.add(GCR);
                Rcs.add(Rc);
                meanClusterSize += subcluster.G.size();
                n++;
            }
        }

        meanClusterSize /= n;

        double sum = 0.0;
        for (double num : GCRs) sum += num;
        double meanGCR = sum / GCRs.size();

        double variance = 0.0;
        for (double num : GCRs) variance += Math.pow(num - meanGCR, 2);
        double stddevGCR = Math.sqrt(variance / GCRs.size());

        variance = 0.0; sum = 0.0;
        for (double num : Rcs) sum += num;
        double meanRC = sum / Rcs.size();

        for (double num : Rcs) variance += Math.pow(num - meanRC, 2);
        double stddevRc = Math.sqrt(variance / Rcs.size());

        resultsTable.get("mean").add(new Tuple(algorithm, meanGCR));
        resultsTable.get("mean-stdev").add(new Tuple(algorithm, meanGCR-stddevGCR));
        resultsTable.get("n").add(new Tuple(algorithm, n));
        resultsTable.get("clustersize").add(new Tuple(algorithm, meanClusterSize));
        resultsTable.get("meanRc").add(new Tuple(algorithm, meanRC));
        resultsTable.get("mean-stdev Rc").add(new Tuple(algorithm, meanRC-stddevRc));
    }

    public static class Tuple {
        public String Algorithm;
        public double value;

        public Tuple(String a, double v) {
            Algorithm = a;
            value = Double.parseDouble(new DecimalFormat("#.###").format(v));
        }
    }
}