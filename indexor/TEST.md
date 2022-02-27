# Testing and Monitoring OpenSearch/Elasticsearch Cluster

To explore content and configuration of TinyWorld index, one of the easiest method is to use [Elasticvue](https://elasticvue.com/).

To connect on TinyWorld's embedded OpenSearch cluster using Elasticvue's docker image:

1) Edit TinyWorld configuration file to expose the embedded cluster

    ```yaml
    indexor:
      cluster:
        embedded:
          expose: true
    ```

2) (Re)Start TinyWorld

3) Start Elasticvue

    ```sh
    docker run -p 8080:8080 -d cars10/elasticvue
    ```

4) open browser at `http://<your IP>:8080`, then enter cluster address and port and click on "Connect" button.

    > *Note: CORS settings are already properly configured on the embedded cluster instance.*
