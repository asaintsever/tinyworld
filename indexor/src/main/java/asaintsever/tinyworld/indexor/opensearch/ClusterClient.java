/*
 * Copyright 2021-2025 A. Saint-Sever
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 * More information about this project is available at:
 *
 *    https://github.com/asaintsever/tinyworld
 */
package asaintsever.tinyworld.indexor.opensearch;

import java.io.Closeable;
import java.io.IOException;
import java.io.StringReader;
import java.net.URISyntaxException;

import lombok.Getter;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.util.EntityUtils;
import org.opensearch.client.*;
import org.opensearch.client.base.BooleanResponse;
import org.opensearch.client.base.RestClientTransport;
import org.opensearch.client.base.Transport;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._global.DeleteScriptRequest;
import org.opensearch.client.opensearch._global.DeleteScriptResponse;
import org.opensearch.client.opensearch._global.GetScriptRequest;
import org.opensearch.client.opensearch._global.GetScriptResponse;
import org.opensearch.client.opensearch._global.PutScriptRequest;
import org.opensearch.client.opensearch._global.PutScriptResponse;
import org.opensearch.client.opensearch._types.StoredScript;
import org.opensearch.client.opensearch.indices.CreateRequest;
import org.opensearch.client.opensearch.indices.CreateResponse;
import org.opensearch.client.opensearch.indices.DeleteRequest;
import org.opensearch.client.opensearch.indices.DeleteResponse;
import org.opensearch.client.opensearch.indices.ExistsRequest;
import org.opensearch.rest.RestStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.json.Json;
import jakarta.json.JsonReader;
import jakarta.json.JsonValue;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.MappingJsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsonp.JSONPModule;

import asaintsever.tinyworld.cfg.utils.Utils;
import asaintsever.tinyworld.indexor.opensearch.jackson.JacksonJsonpMapper; // Cherry pick https://github.com/opensearch-project/opensearch-java/pull/61

public class ClusterClient implements Closeable {

    protected static Logger logger = LoggerFactory.getLogger(ClusterClient.class);

    private RestClient restClient;
    private final RestClientBuilder restClientBuilder;
    private OpenSearchClient osClient;

    @Getter
    private final ObjectMapper mapper;

    public ClusterClient(String host, int port) {
        this.restClientBuilder = RestClient.builder(new HttpHost(host, port));
        this.restClient = this.restClientBuilder.build();

        // See https://github.com/opensearch-project/opensearch-java/issues/60 and
        // https://github.com/opensearch-project/opensearch-java/pull/61
        this.mapper = JsonMapper.builder().addModule(new JSONPModule()).build();
        this.mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        JsonFactory jsonFactory = new MappingJsonFactory(this.mapper);

        Transport transport = new RestClientTransport(this.restClient,
                new JacksonJsonpMapper(this.mapper, jsonFactory));

        this.osClient = new OpenSearchClient(transport);
    }

    public boolean isConnected() {
        if (this.restClient != null && this.restClient.isRunning() && this.osClient != null) {
            try {
                BooleanResponse pingResponse = this.osClient.ping();
                return pingResponse.value();
            } catch (IOException e) {
                logger.warn("Fail to ping cluster: {}", e.getMessage());
            }
        }

        return false;
    }

    /**
     * Checks if the OpenSearch cluster is ready to process requests.
     * <p>
     * "Ready" means the cluster health status is either "yellow" or "green", indicating that the
     * cluster is operational and able to handle requests (not "red").
     * </p>
     *
     * @param wait if true, waits (up to a timeout) for the cluster to reach at least "yellow" status
     *             before returning; if false, checks the current status without waiting.
     * @return true if the cluster is ready (status is "yellow" or "green"), false otherwise.
     */
    public boolean isReady(boolean wait) {
        if (this.isConnected()) {
            try {
                // Check that cluster is ready to process requests
                // See https://opensearch.org/docs/latest/opensearch/rest-api/cluster-health/
                String endpoint = "/_cluster/health";
                if (wait) {
                    endpoint += "?wait_for_status=yellow&timeout=50s";
                }

                Response resp = this.restClient.performRequest(new Request("GET", endpoint));
                HttpEntity entity = resp.getEntity();
                String responseContent = EntityUtils.toString(entity);
                logger.debug("Cluster health response: {}", responseContent);

                try (JsonReader reader = Json.createReader(new StringReader(responseContent))) {
                    var obj = reader.readObject();
                    String status = obj.getString("status"); // e.g. "yellow", "green", "red"

                    if (Cluster.READY_STATUSES.contains(status)) {
                        return true;
                    } else {
                        logger.warn("Cluster is not ready: {}", status);
                    }
                }
            } catch (IOException e) {
                logger.warn("Fail to get cluster health: {}", e.getMessage());
            }
        }

        return false;
    }

    public OpenSearchClient getClient() {
        return this.osClient;
    }

    public RestClientBuilder getClientBuilder() {
        return this.restClientBuilder;
    }

    public Boolean createIndex(String index, String mapping) throws IOException {
        JsonValue mappingJson = null;

        if (mapping != null && !mapping.isEmpty()) {
            try (JsonReader jsonreader = Json.createReader(new StringReader(mapping))) {
                mappingJson = jsonreader.readValue();
            }
        }

        CreateRequest createIndexRequest = new CreateRequest.Builder().index(index).mappings(mappingJson).build();
        CreateResponse createIndexResponse = this.osClient.indices().create(createIndexRequest);
        return createIndexResponse.acknowledged();
    }

    public boolean isIndexExists(String index) throws IOException {
        ExistsRequest existsIndexRequest = new ExistsRequest.Builder().addIndex(index).build();
        BooleanResponse boolResponse = this.osClient.indices().exists(existsIndexRequest);
        return boolResponse.value();
    }

    public Boolean deleteIndex(String index) throws IOException {
        DeleteRequest deleteRequest = new DeleteRequest.Builder().index(index).build();
        DeleteResponse deleteResponse = this.osClient.indices().delete(deleteRequest);
        return deleteResponse.acknowledged();
    }

    public Boolean loadSearchTemplate(String templateId, String templatePath) throws IOException {
        // Read JSON Search Template file and load it in cluster
        // See https://www.elastic.co/guide/en/elasticsearch/reference/current/search-template.html,
        // https://opensearch.org/docs/1.2/opensearch/search-template/
        // See
        // https://www.elastic.co/guide/en/elasticsearch/client/java-api-client/current/searching.html#_templated_search
        JsonValue scriptLang = null;
        StringReader scriptLangStr = new StringReader("\"mustache\""); // lang must be 'mustache'
        try (JsonReader jsonreader = Json.createReader(scriptLangStr)) {
            scriptLang = jsonreader.readValue();
        }

        String searchTemplate;
        try {
            searchTemplate = new String(Utils.getInternalResource(templatePath));
        } catch (IOException | URISyntaxException e) {
            logger.error("Fail to load search template ({})", templatePath, e);
            throw new IOException(e);
        }

        StoredScript storedScript = new StoredScript.Builder().lang(scriptLang).source(searchTemplate).build();
        PutScriptRequest putScriptRequest = new PutScriptRequest.Builder().id(templateId).script(storedScript).build();
        PutScriptResponse putScriptResponse = this.osClient.putScript(putScriptRequest);
        return putScriptResponse.acknowledged();
    }

    public Boolean isSearchTemplateExists(String templateId) throws IOException {
        GetScriptRequest getScriptRequest = new GetScriptRequest.Builder().id(templateId).build();
        try {
            GetScriptResponse getScriptResponse = this.osClient.getScript(getScriptRequest);
            return getScriptResponse.found();
        } catch (ResponseException e) {
            if (e.getResponse().getStatusLine().getStatusCode() == RestStatus.NOT_FOUND.getStatus()) {
                return false;
            }

            throw e;
        }
    }

    public Boolean deleteSearchTemplate(String templateId) throws IOException {
        DeleteScriptRequest delScriptRequest = new DeleteScriptRequest.Builder().id(templateId).build();
        DeleteScriptResponse delScriptResponse = this.osClient.deleteScript(delScriptRequest);
        return delScriptResponse.acknowledged();
    }

    @Override
    public void close() throws IOException {
        if (this.restClient != null)
            this.restClient.close();
        this.restClient = null;
        this.osClient = null;
    }
}
