# load-tests
Load tests for ALA services and infrastructure

## Solr Load Tests

The Solr Load Tests are designed to be run against a text file with each line containing the URL query parameters representing the Select query. These can be extracted from an existing Solr instance using a query logger.

The way to run the Solr Load Tests is the following, where the parameters are customisable based on your requirements:

```
cd "/path/to/load-tests"
mvn gatling:execute \
    -Dgatling.simulationClass=au.org.ala.loadtester.SolrStressTests \
    -Dau.org.ala.loadtester.solr.logfile=/path/to/querylog/solr.q.all.log \
    -Dau.org.ala.loadtester.solr.servers="http://solr-node-1.example:8983 http://solr-node-2.example:8983 http://solr-node-3.example:8983" \
    -Dau.org.ala.loadtester.solr.constantuserspersecond="100" \
    -Dau.org.ala.loadtester.solr.peakrequestspersecond="100" \
    -Dau.org.ala.loadtester.solr.latterrequestspersecond="50" \
    -Dau.org.ala.loadtester.solr.maxfacetcount="1000"
```

## Biocache Service Load Tests

The Biocache Service Load Tests are designed to be run against a text file with each line containing the GET paths, with query parameters attached. These can be extracted from an existing Biocache Service install using the Apache/Nginx access.log file.

The way to run the Biocache Service Load Tests is the following, where the parameters are customisable based on your requirements:

```
cd "/path/to/load-tests"
mvn gatling:execute \
    -Dgatling.simulationClass=au.org.ala.loadtester.BiocacheServiceStressTests \
    -Dau.org.ala.loadtester.biocacheservice.logfile=/path/to/querylog/biocacheservice.log \
    -Dau.org.ala.loadtester.biocacheservice.servers="https://biocache-a.example https://biocache-b.example https://biocache-c.example" \
    -Dau.org.ala.loadtester.biocacheservice.constantuserspersecond="100" \
    -Dau.org.ala.loadtester.biocacheservice.peakrequestspersecond="100" \
    -Dau.org.ala.loadtester.biocacheservice.latterrequestspersecond="50" \
    -Dau.org.ala.loadtester.biocacheservice.maxfacetcount="1000"
```

## Results

The results of the load test are created in the Maven ``target`` directory (and hence will be removed automatically the next time you run ``mvn clean``)
