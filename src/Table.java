import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import algorithms.clusteringCoefficient;
import algorithms.TfSCA;
import lib.Graph;
import lib.IO;
import lib.SparkDB;

/**
 * Generate CSV Table for excel/libreoffice-calc/google-sheets software
 * NOTE: 'cz' is cluster size
 * @author Morad A.
 */
public class Table {
    public static void main(String[] args) throws Exception {
        ArrayList<String> GO_IDs = new ArrayList<>();
        /* GeneOntology IDs */
        GO_IDs.add("GO:0016032"); GO_IDs.add("GO:0022414"); GO_IDs.add("GO:0040007"); GO_IDs.add("GO:0000003"); 
        GO_IDs.add("GO:0040011"); GO_IDs.add("GO:0042592"); GO_IDs.add("GO:0043473"); GO_IDs.add("GO:0098754");

        for(String ID : GO_IDs) run(ID);
    }

    public static void run(String ID) throws Exception {
        /* K: Mean/Stdev/etc.. | V: A set of 2-tuples */
        /* valTable is used for output ranking and structured printing */
        HashMap<String, ArrayList<Tuple>> valTable = new HashMap<>();
        valTable.put("mean", new ArrayList<>());
        valTable.put("stdev", new ArrayList<>());
        valTable.put("n", new ArrayList<>());
        valTable.put("cz", new ArrayList<>());

        String folderPath = "../data/" + ID;

        /* Algorithms and files */
        ArrayList<String> fileNames = new ArrayList<>();
        fileNames.add("louvain.csv"); fileNames.add("mcl.csv");
        fileNames.add("clixo.csv"); fileNames.add("infomap.csv");
        fileNames.add("TfSCA");

        /* PPI Graph Import */
        Graph G = new Graph();
        G.importTSV(new String(IO.read(folderPath + "/" + "interactions.tsv")), "node1", "node2", "combined_score");

        for(String name : fileNames) {
            // Multi-threaded processing
            new Thread(() -> {
            if(name.equals("TfSCA")) AlgPrint(G, valTable);

            else if(name.equals("mcl.csv")) {
                /* MCL Parsing */
                /* Each line is a protein, we insert proteins according to their cluster number in __mclCluster */
                SparkDB MCL = new SparkDB();
                try {
                    MCL.readFromFile(folderPath+"/"+name);
                } catch (Exception e) {
                    // Shhhh....
                }
                ArrayList<ArrayList<String>> Clusters = new ArrayList<>();

                for(int i = 0; i < MCL.num_queries; i++) Clusters.add(new ArrayList<>());

                for(int index = 0; index < MCL.num_queries; index++) {
                    HashMap<String, String> row = MCL.get(index);
                    try {
                        Clusters.get(Integer.parseInt(row.get("__mclCluster"))).add(row.get("name"));
                    } catch (Exception e) {
                        // Drop lines with malformed structure
                    } 
                }

                insertClusterData(Clusters, G, valTable, "MCL");
            }

            else {
                /* Parsing for any Community Detection file */
                /* Each line is a cluster, we insert proteins in CD_Labeled column for each line */
                SparkDB db = new SparkDB();
                try {
                    db.readFromFile(folderPath+"/"+name);
                } catch (Exception e) {
                    // Shhhh....
                }

                ArrayList<String> clustersun = db.getColumn("CD_Labeled");
                ArrayList<ArrayList<String>> Cs = new ArrayList<>();
                for(String st : clustersun) {
                    String[] cluster = st.split("\\s+");
                    ArrayList<String> c = new ArrayList<>();
                    Collections.addAll(c, cluster);
                    Cs.add(c);
                }

                insertClusterData(Cs, G, valTable, name.split("\\.")[0].toUpperCase());
            }
        }).start();
        }

        /* Wait until all threads finish */
        while(Thread.activeCount() > 1) {}

        /* The sorted (ranked) data */
        HashMap<String, ArrayList<Tuple>> sorteds = new HashMap<>();

        /* Sort that shit */
        for(Map.Entry<String, ArrayList<Tuple>> e : valTable.entrySet()) {
            ArrayList<Tuple> sorted = new ArrayList<>(e.getValue());

            Collections.sort(sorted, new Comparator<Tuple>() {
                @Override
                public int compare(Tuple t1, Tuple t2) {
                    if (e.getKey().equals("cz")) {
                        // Sort by value ascending if the key is "cz"
                        /* Lower cluster size means more modular and analyzable clusters */
                        return Double.compare(t1.value, t2.value);
                    } else {
                        // Sort by value descending for other keys
                        return Double.compare(t2.value, t1.value);
                    }
                }
            });

            sorteds.put(e.getKey(), sorted);
        }

        /* Print 5 algorithms (TfSCA,MCL,LOUVAIN,INFOMAP,CLIXO) */
        // Should print File, Algorithm, Mean, Mean-Stdev
        System.out.println(ID);
        System.out.println("MeanCC,,MeanCC-StdevCC,,NumberOfClusters,,MeanClusterSize");
        System.out.println("Algorithm,Value,Algorithm,Value,Algorithm,Value,Algorithm,Value");
        for(int i = 0;i < 5; i++) {
            HashMap<String, String> vals = new HashMap<>();
            for(Map.Entry<String, ArrayList<Tuple>> s : sorteds.entrySet()) {
                vals.put(s.getKey(), s.getValue().get(i).Algorithm + "," + s.getValue().get(i).value);
            }

            System.out.println(vals.get("mean") + "," + vals.get("stdev") + "," + vals.get("n") + "," + vals.get("cz"));
        }
    }


    /**
     * TfSCA Algorithm
     */
    public static void AlgPrint(Graph G, HashMap<String, ArrayList<Tuple>> in) {
        Graph modified = G.subcluster(new ArrayList<>(G.nodes.keySet()));
        /* 35% head, 10% transition proteins, 5% Triangle multiplier */
        TfSCA A = new TfSCA(modified, 0.35, 0.1, 0.05, true, false);
        ArrayList<ArrayList<String>> clustersc = A.run();
        insertClusterData(clustersc, G, in, "TfSCA");

    }

    /* synchronized for single-threaded access */
    public synchronized static void insertClusterData(ArrayList<ArrayList<String>> clusters, Graph G, HashMap<String, ArrayList<Tuple>> i, String alg) {
        ArrayList<Double> CCs = new ArrayList<>();

        double MeanClusterSize = 0;
        double N = 0;

        for(ArrayList<String> cluster : clusters) {
            double CC = new clusteringCoefficient(G.subcluster(cluster)).run(false);
            /* Consider clusters with 4 proteins or more */
            if(cluster.size() >= 4 && CC == CC) {
                CCs.add(CC);
                MeanClusterSize += cluster.size();
                N++;
            }
        }

        MeanClusterSize /= N;

        double sum = 0.0;
        for (double num : CCs) sum += num;
        double mean = sum / CCs.size();

        double variance = 0.0;
        for (double num : CCs) variance += Math.pow(num - mean, 2);
        double stddev = Math.sqrt(variance / CCs.size());

        i.get("mean").add(new Tuple(alg, mean));
        i.get("stdev").add(new Tuple(alg, mean-stddev));
        i.get("n").add(new Tuple(alg, N));
        i.get("cz").add(new Tuple(alg, MeanClusterSize));
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
