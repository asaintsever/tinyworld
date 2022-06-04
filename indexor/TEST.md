# TinyWorld Indexor

## Indexing test photos

A test program is provided to test extraction of photo metadata: [IndexorCmd](src/main/java/asaintsever/tinyworld/indexor/IndexorCmd.java). This program instantiates and exposes a local OpenSearch cluster at `http://localhost:9200`.

```sh
# Set indexor.cmd.clearIndex to false if you want to keep previously indexed data
# Set indexor.cmd.allowUpdate to true if you allow updates of existing photo metadata in index
mvn package -Dmaven.test.skip=true -Dindexor.cmd.path=<full path to root directory to index> -Dindexor.cmd.clearIndex=true -Dindexor.cmd.allowUpdate=false -P indexorCmd
```

Once metadata extraction has been done, the program will pause to give you the opportunity to inspect the index (see procedure in next section). Press `Q + <Enter>` to exit the program.

## Testing and Monitoring OpenSearch/Elasticsearch Cluster

To explore content and configuration of TinyWorld index, one of the easiest method is to use [Elasticvue](https://elasticvue.com/).

To connect on OpenSearch/Elasticsearch cluster using Elasticvue's docker image:

1) 
    - Using IndexorCmd

        Just start it (comes with an embbeded, exposed, cluster) :-)

    - Using TinyWorld's embedded OpenSearch cluster
  
        - Make sure you also expose the cluster. Check configuration file:

            ```yaml
            indexor:
              cluster:
                embedded:
                  expose: true
            ```

            *Or* set env var `TW_IDX_CLUSTER_EMBEDDED_EXPOSE=true`

        - (Re)Start TinyWorld

2) Start Elasticvue

    ```sh
    docker run -p 8080:8080 -d cars10/elasticvue
    ```

3) Open browser at `http://<your IP>:8080`, then enter cluster address and port and click on "Connect" button.

    > *Note*
    >
    > - *CORS settings are already properly configured on the embedded cluster instance*
    > - *If you use your own OpenSearch/Elasticsearch cluster instance, you should provide the necessary configuration to enable access to it*
