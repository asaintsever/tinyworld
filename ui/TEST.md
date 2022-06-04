# TinyWorld UI

## Testing

Using default configuration (with embedded OpenSearch cluster):

```sh
mvn package -Dmaven.test.skip=true -P UI
```

Using environment variables to override default config and use an external local cluster at "localhost:9200":

```sh
TW_IDX_CLUSTER_EMBEDDED=false mvn package -Dmaven.test.skip=true -P UI
```

> Refer to Configuration's [README](../cfg/README.md) for list of available environment variables
