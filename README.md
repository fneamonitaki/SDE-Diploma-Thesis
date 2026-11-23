# And Synopses for All: A Synopses Data Engine for Extreme Scale Analytics-as-a-Service

## SDEaaS: Synopses Data Engine-as-a-Service

This repository contains an extension of the **Synopses Data Engine (SDE)** developed by Antonios Kontaxakis, Nikos Giatrakos, Dimitris Sacharidis, and Antonios Deligiannakis.

The original SDE provides a scalable framework for processing streaming data and answering analytical queries using different data synopses.

This fork contains additional implementation work developed as part of a diploma thesis, including the Relative Error Quantile Sketch implementation, performance measurement instrumentation, and modifications to selected components of the original SDE.

---

## Original Publication

Please cite the original SDE publication when using the underlying framework:

```bibtex
@article{sdeaas,
  author    = {Antonios Kontaxakis and
               Nikos Giatrakos and
               Dimitris Sacharidis and
               Antonios Deligiannakis},
  title     = {{And Synopses for All: a Synopses Data Engine for Extreme Scale Analytics-as-a-Service}},
  journal   = {Inf. Syst.},
  volume    = {116},
  year      = {2023}
}
```

---

# 1. Original SDE Architecture and Functionality

The SDE reads data and requests from separate Kafka topics.

Based on each request's key-value parameters, the engine performs operations such as adding, updating, deleting, and querying data synopses.

## 1.1 Input Main Class

The main entry point of the application is the `Run` class.

The original SDE accepts the following configuration arguments:

| Argument | Description | Original default |
|---|---|---|
| `args[0]` | Kafka data input topic | `Forex` |
| `args[1]` | Kafka request input topic | `Requests` |
| `args[2]` | Kafka output topic | `OUT` |
| `args[3]` | Kafka broker list | `localhost:9092` |
| `args[4]` | Job parallelism | `4` |


---

## 1.2 Original Available Synopses

The original SDE supports several synopsis algorithms for different streaming analytics tasks.

| Synopsis ID | Synopsis | Estimation parameters | Estimates | Main application |
|---|---|---|---|---|
| 1 | CountMin | KEY | Count | Frequent Itemsets |
| 2 | BloomFilter | KEY | Membership | Membership queries |
| 3 | AMS | KEY | L2 norm, inner product, count | Frequent Itemsets |
| 4 | DFT | Similarity score | Fourier coefficients | Correlation |
| 5 | LSH | None | Bucket ID, projected features | Correlation |
| 6 | Coresets | Number of clusters K | Coresets for k-means | Clustering |
| 7 | FM Sketch | None | Cardinality | Cardinality estimation |
| 8 | HyperLogLog | None | Cardinality | Cardinality estimation |
| 9 | StickySampling | KEY | Frequent items, frequency, count | Frequent Itemsets |
| 10 | LossyCounting | KEY | Count, frequent items | Frequent Itemsets |
| 11 | ChainSampler | None | Sample of the data | Sampling |
| 12 | GKQuantiles | KEY | Quantiles | Quantile estimation |
| 13 | MaritimeSketch | None | Ship positions (sample) | Sampling |
| 14 | TopK | None | Top-K elements | Top-K queries |
| 15 | OptimalDistributedWindowSampling | None | Sample of the data | Sampling |
| 16 | OptimalDistributedSampling | None | Sample of the data | Sampling |
| 17 | WindowedQuantiles | KEY | Quantiles | Quantile estimation |
| 18 | Radius Sketch Family | Similarity score | List of streams/windows | Similarity and distance |

The original synopsis implementations and their parameters are maintained as part of the upstream SDE project.

---

## 1.3 Available Request Types

The original SDE supports the following request types:

| Request ID | Operation | Description |
|---|---|---|
| 1 | ADD | Add a synopsis using keyed partitioning |
| 2 | DELETE | Delete a synopsis |
| 3 | ESTIMATE | Request an estimation from a queryable synopsis |
| 4 | ADD | Add a synopsis using random partitioning |
| 5 | ADD | Add a continuous synopsis |
| 6 | ESTIMATE | Request an advanced estimation |
| 7 | UPDATE | Update a synopsis state |

---

## 1.4 Message Examples

### Input Data Example

```json
{
  "values": "{\"time\":\"02/19/2019 06:07:14\",\"StockID\":\"ForexALLNoExpiry\",\"price\":\"110.11\"}",
  "streamID": "ForexALLNoExpiry",
  "dataSetkey": "Forex"
}
```

### Request Example

```json
{
  "streamID": "INTEL",
  "synopsisID": 4,
  "requestID": 1,
  "dataSetkey": "Forex",
  "param": [
    "StockID",
    "price",
    "Queryable",
    "1",
    "720",
    "3600",
    "8"
  ],
  "noOfP": 4,
  "uid": 1110
}
```

---

# 2. Diploma Thesis Contributions

This fork extends the original SDE with additional functionality and experimental modifications.

The main contributions are described below.

## 2.1 ReqSketch Implementation

A new ReqSketch implementation was developed and integrated into the SDE.

The implementation introduces the following components:

- `ReqSketchSynopsis.java`: integrates the sketch into the SDE synopsis architecture.
- `ReqSketchReduce.java`: provides the corresponding reduction functionality.
- `RelativeErrorSketch.java`: implements the relative-error sketch functionality.
- `RelativeCompactor.java`: implements the compaction mechanism.
- `CDFItem.java`, `Constants.java`, `ItemWeight.java`, and `RankItem.java`: supporting classes for the sketch implementation.
- `StreamMaker.java`: provides stream-generation functionality.

The implementation is written in Java and is based on the ReqSketch research approach.

## 2.2 Performance Measurement Instrumentation

Additional instrumentation was introduced to monitor the performance of the streaming application.

The modifications include:

- Query throughput measurement.
- Data ingestion time
- Measuring elapsed time over measurement intervals.


## 2.3 Data Routing Modifications

The `dataRouterCoFlatMap` transformation was modified to improve the handling of incoming datapoints.

The change include:

- Direct forwarding of datapoints when no keyed or random partitioning configuration is active and the dataset has no grid mapping.

---

# 3. Project Structure

The main source-code components introduced or modified in this fork include:

```text
src/main/java/
├── infore/SDE/
│   ├── Run.java
│   ├── reduceFunctions/
│   │   └── ReqSketchReduce.java
│   ├── synopses/Sketches/
│   │   └── ReqSketchSynopsis.java
│   └── transformations/
│       ├── GReduceFlatMap.java
│       ├── ReduceFlatMap.java
│       ├── SDEcoFlatMap.java
│       └── dataRouterCoFlatMap.java
│
└── lib/
    └── ReqSketchLib/
       ├── HelperClasses/
       │   ├── CDFItem.java
       │   ├── Constants.java
       │   ├── ItemWeight.java
       │   └── RankItem.java
       └── relativeSketchPackage/
           ├── RelativeCompactor.java
           ├── RelativeErrorSketch.java
           └── StreamMaker.java
    
    
```

---

# 4. Requirements

The project uses Maven for dependency management and building.

The environment used for building this fork includes:

- Java Development Kit (JDK) 8
- Apache Maven
- Apache Kafka
- Apache Flink

The project was successfully compiled and packaged using JDK 8.

---

# 5. Getting Started

## 5.1 Clone the Repository

Clone this fork:

```bash
git clone https://github.com/fneamonitaki/SDE-Diploma-Thesis.git
cd SDE-Diploma-Thesis
```

## 5.2 Build the Project

Make sure JDK 8 is configured as the active Java version.

Then execute:

```bash
mvn clean package
```

After a successful build, the generated Fat JAR can be found in the `target` directory.


## 5.3 Example Messages For ReqSketch

### Input Data Example


```json
{
  "values": "{"temperature":28.49}",
  "streamID": "1",
  "dataSetKey": "example_dataset"
}
```
### Build Synopsis Example

```json
{
  "dataSetKey": "example_dataset",
  "requestID": 4,
  "synopsisID": 19,
  "UID": 10111,
  "streamID": "stream1",
  "param": [
    "temperature",
    "null",
    "Queryable",
    "50"
  ],
  "noOfP": 4
}
```

## 5.4 Running the Application

The SDE uses Kafka for input data and requests.

Before running the application:

1. Make sure Kafka is running and accessible.
2. Configure the appropriate Kafka broker address.
3. Create or select the required input and output topics.
4. Configure the dataset and request parameters.
5. Start the application using the `Run` entry point.
---

# 6. Acknowledgements and References

This repository builds upon the original Synopses Data Engine developed by:

- Antonios Kontaxakis
- Nikos Giatrakos
- Dimitris Sacharidis
- Antonios Deligiannakis

The original publication is:

*And Synopses for All: a Synopses Data Engine for Extreme Scale Analytics-as-a-Service*, Information Systems, Volume 116, 2023.

The `ReqSketch` code also includes an adaptation of the original code from:

[edoliberty/streaming-quantiles](https://github.com/edoliberty/streaming-quantiles)

The relevant source attribution is retained in the Java source file.

---

# 7. Contact

For questions regarding the original SDE publication and framework, please refer to the original authors:

- Antonios Kontaxakis
- Nikos Giatrakos
- Dimitris Sacharidis
- Antonios Deligiannakis

For questions concerning the extensions and experimental modifications in this fork, please refer to the repository maintainer.

---

**This repository is an extension of the original SDE project. The original framework and publication remain attributed to their respective authors.**
