/*
 * Copyright 2021-2022 A. Saint-Sever
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
import java.util.ArrayList;
import java.util.List;

import org.opensearch.action.search.SearchRequest;
import org.opensearch.action.search.SearchResponse;
import org.opensearch.client.RequestOptions;
import org.opensearch.client.ResponseException;
import org.opensearch.client.RestHighLevelClient;
import org.opensearch.client.opensearch._global.CountRequest;
import org.opensearch.client.opensearch._global.CountResponse;
import org.opensearch.client.opensearch._global.GetRequest;
import org.opensearch.client.opensearch._global.GetResponse;
import org.opensearch.client.opensearch._global.IndexRequest;
import org.opensearch.client.opensearch._global.IndexResponse;
import org.opensearch.index.query.QueryBuilder;
import org.opensearch.index.query.QueryBuilders;
import org.opensearch.rest.RestStatus;
import org.opensearch.search.SearchHit;
import org.opensearch.search.SearchHits;
import org.opensearch.search.builder.SearchSourceBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;

import asaintsever.tinyworld.indexor.IndexPage;


public class Document<T> implements Closeable {
    private ObjectMapper mapper;
    private ClusterClient client;
    private String index;
    
    // Used for search only (new Java client search capabilities not on par compared to high-level rest client)
    // To be removed once new Java client fully supports all search expressions
    private RestHighLevelClient restHlClient;
    
    
    public Document(ClusterClient client) {
        this.setClient(client);
    }
    
    // Getter to allow customization of mapper
    public ObjectMapper getMapper() {
        return this.mapper;
    }

    public Document<T> setClient(ClusterClient client) {
        this.client = client;
        this.mapper = client.getMapper();
        
        // Create internal high-level rest client in addition (for advanced search only)
        this.restHlClient = new RestHighLevelClient(client.getClientBuilder());
        return this;
    }
    
    public Document<T> setIndex(String index) {
        this.index = index;
        return this;
    }
    
    public String getIndex() {
        return this.index;
    }

    public String add(T document) throws IOException {
        return this.add(null, document, false);
    }
    
    public String add(String id, T document, boolean allowUpdate) throws DocumentAlreadyExistsException, IOException {
        // Use "_doc" type to allow update if document with same id already exists in index. Note: id can be null when using "_doc".
        // Use "_create" type to fail if we try to add a document with same id as existing document in index. Note: id must not be null when using "_create".
        IndexRequest<T> indexRequest = new IndexRequest.Builder<T>().index(this.index).type((allowUpdate || id == null) ? "_doc" : "_create").id(id).value(document).build();
        
        try {
            IndexResponse indexResponse = this.client.getClient().index(indexRequest);
            return indexResponse.id();
        } catch(ResponseException e) {
            if (e.getResponse().getStatusLine().getStatusCode() == RestStatus.CONFLICT.getStatus()) {
                throw new DocumentAlreadyExistsException(id, e);
            }
            
            throw e;
        }
    }
    
    public T get(String id, Class<T> docClass) throws IOException {
        GetRequest getRequest = new GetRequest.Builder().index(this.index).id(id).build();
        GetResponse<T> getResponse = this.client.getClient().get(getRequest, docClass);
        return getResponse.source();
    }
    
    public long count() throws IOException {
        CountRequest countRequest = new CountRequest.Builder().index(this.index).build();
        CountResponse countResponse = this.client.getClient().count(countRequest);
        return countResponse.count().longValue();
    }
    
    public IndexPage<T> search(String queryDSL, Class<T> docClass) throws IOException {
        return this.search(queryDSL, 0, 10, docClass);
    }
    
    @SuppressWarnings("unchecked")
	public IndexPage<T> search(String queryDSL, int from, int size, Class<T> docClass) throws IOException {
        QueryBuilder searchQuery = QueryBuilders.wrapperQuery(queryDSL);
        
        SearchRequest searchRequest = new SearchRequest(this.index);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.trackTotalHits(true).from(from).size(size).query(searchQuery);
        searchRequest.source(searchSourceBuilder);

        SearchResponse searchResponse = this.restHlClient.search(searchRequest, RequestOptions.DEFAULT);
        SearchHits hits = searchResponse.getHits();
        
        if (hits.getTotalHits().value == 0 || hits.getHits().length == 0) {
            return IndexPage.EMPTY;
        }
        
        SearchHit[] searchHits = hits.getHits();
        List<T> resultList = new ArrayList<T>();
        
        for (SearchHit hit : searchHits) {
            // Try to unserialize source to object using provided class
            T result = mapper.readValue(hit.getSourceAsString(), docClass);
            resultList.add(result);
        }

        return new IndexPage<T>(resultList, queryDSL, hits.getTotalHits().value, searchSourceBuilder.from(), searchSourceBuilder.size());
    }
    
    
    /**
     * By default, you cannot use from and size to page through more than 10,000 hits. This limit is a safeguard set by the index.max_result_window index setting.
     * If you need to page through more than 10,000 hits, use the search_after parameter instead.
     * See https://www.elastic.co/guide/en/elasticsearch/reference/current/paginate-search-results.html#search-after
     */
    public IndexPage<T> next(IndexPage<T> page, Class<T> docClass) throws IOException {
        return this.search(page.query(), page.from() + page.size(), page.size(), docClass);
    }

    @Override
    public void close() throws IOException {
        if(this.restHlClient != null) this.restHlClient.close();  // do not forget to free the internal high-level rest client
    }
}
