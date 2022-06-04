package asaintsever.tinyworld.indexor.opensearch;

import java.io.Closeable;
import java.io.IOException;
import java.io.StringReader;

import org.apache.http.HttpHost;
import org.opensearch.client.RestClient;
import org.opensearch.client.RestClientBuilder;
import org.opensearch.client.base.BooleanResponse;
import org.opensearch.client.base.RestClientTransport;
import org.opensearch.client.base.Transport;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch.indices.CreateRequest;
import org.opensearch.client.opensearch.indices.CreateResponse;
import org.opensearch.client.opensearch.indices.DeleteRequest;
import org.opensearch.client.opensearch.indices.DeleteResponse;
import org.opensearch.client.opensearch.indices.ExistsRequest;
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

import asaintsever.tinyworld.indexor.opensearch.jackson.JacksonJsonpMapper; // Cherry pick https://github.com/opensearch-project/opensearch-java/pull/61


public class ClusterClient implements Closeable {
    
    protected static Logger logger = LoggerFactory.getLogger(ClusterClient.class);
    
    private RestClient restClient;
    private RestClientBuilder restClientBuilder;
    private OpenSearchClient osClient;
    private ObjectMapper mapper;
    

    public ClusterClient(String host, int port) {
        this.restClientBuilder = RestClient.builder(new HttpHost(host, port));
        this.restClient = this.restClientBuilder.build();
        
        // See https://github.com/opensearch-project/opensearch-java/issues/60 and https://github.com/opensearch-project/opensearch-java/pull/61
        this.mapper = JsonMapper.builder()
                                .addModule(new JSONPModule())
                                .build();
        this.mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        
        JsonFactory jsonFactory = new MappingJsonFactory(this.mapper);
        
        Transport transport = new RestClientTransport(this.restClient, new JacksonJsonpMapper(this.mapper, jsonFactory));
        
        this.osClient = new OpenSearchClient(transport);
    }
    

    public Boolean isConnected() {
        if (this.restClient != null && this.restClient.isRunning() && this.osClient != null) {
            try {
                BooleanResponse pingResponse = this.osClient.ping();
                return pingResponse.value();
            } catch (IOException e) {
                logger.warn("Fail to ping cluster: " + e.getMessage());
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
    
    public ObjectMapper getMapper() {
        return this.mapper;
    }
    

    public Boolean createIndex(String index, String mapping) throws IOException {
        JsonValue mappingJson = null;
        
        if(mapping != null && !mapping.isEmpty()) {
            StringReader mappingStr = new StringReader(mapping);
            try(JsonReader jsonreader = Json.createReader(mappingStr)) {
                mappingJson = jsonreader.readValue();
            }
        }
        
        CreateRequest createIndexRequest = new CreateRequest.Builder().index(index).mappings(mappingJson).build();
        CreateResponse createIndexResponse = this.osClient.indices().create(createIndexRequest);
        return createIndexResponse.acknowledged();
    }
    
    public Boolean isIndexExists(String index) throws IOException {       
        ExistsRequest existsIndexRequest = new ExistsRequest.Builder().addIndex(index).build();
        BooleanResponse boolResponse = this.osClient.indices().exists(existsIndexRequest);
        return boolResponse.value();
    }
    
    public Boolean deleteIndex(String index) throws IOException {       
        DeleteRequest deleteRequest = new DeleteRequest.Builder().index(index).build();
        DeleteResponse deleteResponse = this.osClient.indices().delete(deleteRequest);
        return deleteResponse.acknowledged();
    }
    
    @Override
    public void close() throws IOException {
        if (this.restClient != null) this.restClient.close();
        this.restClient = null;
        this.osClient = null;
    }
}
