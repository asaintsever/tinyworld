package asaintsever.tinyworld.indexor.opensearch;

import java.io.Closeable;
import java.io.IOException;

import org.apache.http.HttpHost;
import org.opensearch.client.RestClient;
import org.opensearch.client.base.BooleanResponse;
import org.opensearch.client.base.RestClientTransport;
import org.opensearch.client.base.Transport;
import org.opensearch.client.json.jackson.JacksonJsonpMapper;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch.indices.CreateRequest;
import org.opensearch.client.opensearch.indices.CreateResponse;
import org.opensearch.client.opensearch.indices.DeleteRequest;
import org.opensearch.client.opensearch.indices.DeleteResponse;
import org.opensearch.client.opensearch.indices.ExistsRequest;


public class ClusterClient implements Closeable {
    
    private RestClient restClient;
    private OpenSearchClient osClient;
    

    public ClusterClient(String host, int port) {
        this.restClient = RestClient.builder(new HttpHost(host, port)).build();
        Transport transport = new RestClientTransport(this.restClient, new JacksonJsonpMapper());
        this.osClient = new OpenSearchClient(transport);
    }
    

    public Boolean isStarted() {
        return (this.restClient != null && this.restClient.isRunning() && this.osClient != null);
    }
    
    public OpenSearchClient getClient() {
        return this.osClient;
    }

    public Boolean createIndex(String name) throws IOException {
        CreateRequest createIndexRequest = new CreateRequest.Builder().index(name).build();
        CreateResponse createIndexResponse = this.osClient.indices().create(createIndexRequest);
        return createIndexResponse.acknowledged();
    }
    
    public Boolean isIndexExists(String name) throws IOException {
        ExistsRequest existsIndexRequest = new ExistsRequest.Builder().addIndex(name).build();
        BooleanResponse boolResponse = this.osClient.indices().exists(existsIndexRequest);
        return boolResponse.value();
    }
    
    public Boolean deleteIndex(String name) throws IOException {
        DeleteRequest deleteRequest = new DeleteRequest.Builder().index(name).build();
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
