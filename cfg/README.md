# TinyWorld Configuration

## Configuration file

TinyWorld will look after a file named `tinyworld.yml` under `.tinyworld/config` directory in current user's home directory. You can edit the provided configuration under this location to set your own custom values (or use env vars instead, see next section).

If no configuration file can be loaded from this location, TinyWorld will fallback to an internal default configuration that will be saved into `.tinyworld/config` directory in current user's home directory. In DEBUG level, the logs display the loaded configuration at startup.

In case your config file ends up being corrupted or you want to reset, just delete your `tinyworld.yml` file to let TinyWorld creates a new default one at next start.

**Default configuration**: [tinyworld.yml](src/main/resources/config/tinyworld.yml)

## Environment Variables

Following env vars allow to override values in TinyWorld's config file (`tinyworld.yml`) in case you don't want to modify the file itself:

| Env | Supported value | Description |
|-----|-----------------|-------------|
| TW_UI_LOGS_WWJ | `on` (default), `off` | Enable logs for NASA WorldWind |
| TW_UI_LOGS_FLATLAF | `on` (default), `off` | Enable logs for FlatLaf framework |
| TW_UI_PHOTOTREE_FILTER_TMPL | Any valid search template name. Default is `year_month` | Search template to use in photo tree |
| TW_IDX_CLUSTER_EMBEDDED | `true` (default), `false` | Use TinyWorld's embedded cluster (OpenSearch). Set to `false` to use your own ElasticSearch / OpenSearch cluster |
| TW_IDX_CLUSTER_EMBEDDED_EXPOSE | `true`, `false` (default) | Expose embedded cluster to external machines (with CORS enabled as well) |
| TW_IDX_CLUSTER_ADDRESS | Any address. Default is `localhost` | Address of OpenSearch / Elasticsearch cluster |
| TW_IDX_CLUSTER_PORT | A valid port number. Default is `9200` | Cluster listening port |
| TW_IDX_CLUSTER_INDEX | Any valid index name. Default is `photos` | Cluster index name |
