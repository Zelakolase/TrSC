# TrSC
Top-ranked Selective Clustering, Protein-Protein Interaction Network Usecase

- [TrSC](#trsc)
  - [Class list (in src/)](#class-list-in-src)
  - [Build and Run](#build-and-run)
  - [License](#license)
  - [Available datasets](#available-datasets)


## Class list (in src/)
- algorithms/
  - **Clustering** : TrSC clustering algorithm (inc. ranking eq.)
  - **GCR** : Calculation of Graph Completeness Ratio for a Graph object
- lib/
  - **GORead** : Reads STRING-DB short TSV file to Graph object
  - **Graph** : Weighted undirected graph object (data structure + CRUD functions)
  - **IO** : Disk IO r/w
  - **SparkDB** : Check NaDeSys/SparkDB repository
- **App** : Make a ranked table comparing the mean GCR, mean-stdev GCR, num. clusters, and mean cluster size for MCL, Louvain, CliXO, infomap, and TrSC for all of the available dataset.
- **SingleGO** : Makes a report on TrSC clustering for a specified GO term in dataset.

## Build and Run
The current runnable classes are **App** and **SingleGO**
`$ cd src/; chmod +x buildAndRun.sh; ./buildAndRun.sh [classname]`

## License
Top-ranked Selective Clustering Algorithm © 2023 by Morad A.; Nourhan A.K. is licensed under Attribution-ShareAlike 4.0 International. To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/4.0/

## Available datasets
All GO terms under GO:0008150 (Biological Process) under 2,000 homosapien proteins:
- GO:0000003 (Reproduction)
- GO:0016032 (Viral Process)
- GO:0022414 (Reproductive Process)
- GO:0040007 (Growth)
- GO:0040011 (Locomotion)
- GO:0042592 (Homeostatic Process)
- GO:0043473 (Pigmentation)
- GO:0098754 (Detoxification)

Note: ALS.tsv is related to a test case.