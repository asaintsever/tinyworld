# TinyWorld UI

## Testing

Using default configuration (with embedded OpenSearch cluster):

```sh
make run-ui
```

Using environment variables to override default config and use an external local cluster at "localhost:9200":

```sh
TW_IDX_CLUSTER_EMBEDDED=false make run-ui
```

> Refer to Configuration's [README](../cfg/README.md) for list of available environment variables
