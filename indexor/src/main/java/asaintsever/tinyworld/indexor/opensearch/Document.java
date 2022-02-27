package asaintsever.tinyworld.indexor.opensearch;

import java.io.IOException;

import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._global.IndexRequest;
import org.opensearch.client.opensearch._global.IndexResponse;
import org.opensearch.client.opensearch._global.SearchResponse;

public class Document<T> {
    
    private OpenSearchClient osClient;
    private String index;

    public void setClient(OpenSearchClient client) {
        this.osClient = client;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public String add(T document) throws IOException {
        IndexRequest<T> indexRequest = new IndexRequest.Builder<T>().index(this.index).value(document).build();
        IndexResponse indexResponse = this.osClient.index(indexRequest);
        return indexResponse.id();
    }

    public SearchResponse<T> search() {
        // TODO Auto-generated method stub
        return null;
    }
}
