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

import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

import org.jeasy.random.EasyRandom;
import org.jeasy.random.EasyRandomParameters;
import org.jeasy.random.FieldPredicates;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opensearch.client.ResponseException;
import org.opensearch.rest.RestStatus;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import asaintsever.tinyworld.indexor.LatLongGenerator;
import asaintsever.tinyworld.indexor.opensearch.Cluster.ClusterNodeException;
import asaintsever.tinyworld.indexor.search.results.TermsAggregation;
import asaintsever.tinyworld.indexor.search.results.IndexPage;
import asaintsever.tinyworld.metadata.extractor.CustomDateSerializer;
import lombok.EqualsAndHashCode;
import lombok.ToString;

public class ClusterClientTest {

    private final static String TEST_SEARCH_TEMPLATE_ID = "my_test_template";
    private final static Map<String, String> TEST_SEARCH_TEMPLATES = Map.of(TEST_SEARCH_TEMPLATE_ID,
            "search_templates/my_test_template.json");

    private static Cluster cluster;
    private static ClusterClient client;
    private static long seed;

    @ToString
    @EqualsAndHashCode
    static class DocObject {
        public String attr1;
        public String attr2;
        public Float attr3;
        public URL attr4;

        @JsonSerialize(using = CustomDateSerializer.class)
        public Date creationDate;

        public String latlong;
    }

    @SuppressWarnings("resource")
    @BeforeAll
    public static void setup() throws ClusterNodeException {
        System.setProperty("jna.debug_load", "true");
        System.setProperty("jna.debug_load.jna", "true");

        // Create single node cluster on special port. Cluster is exposed to be able to connect to it from
        // any external tool.
        cluster = new Cluster().setHttpPort(9299).setPathHome("target/index").create(true);

        // Create client, connect on local cluster
        client = new ClusterClient("localhost", 9299);
    }

    @AfterAll
    public static void teardown() throws IOException {
        if (client.isSearchTemplateExists(TEST_SEARCH_TEMPLATE_ID)) {
            client.deleteSearchTemplate(TEST_SEARCH_TEMPLATE_ID);
        }

        client.close();
        cluster.close();
    }

    @BeforeEach
    void setupTest() {
        // Get new random seed to init our EasyRandom instance
        seed = new Random().nextLong();
    }

    @AfterEach
    void teardownTest() throws IOException {
        client.deleteIndex("*");
    }

    @Test
    void createThenDeleteIndex() throws IOException {
        assertTrue(client.createIndex("test.index", null));
        assertTrue(client.isIndexExists("test.index"));

        assertTrue(client.deleteIndex("test.index"));
        assertFalse(client.isIndexExists("test.index"));
    }

    @Test
    void createIndexTwice() throws IOException {
        assertTrue(client.createIndex("test.index", null));
        assertTrue(client.isIndexExists("test.index"));

        assertEquals(assertThrows(ResponseException.class, () -> {
            client.createIndex("test.index", null);
        }).getResponse().getStatusLine().getStatusCode(), RestStatus.BAD_REQUEST.getStatus());
    }

    @Test
    void deleteUnknownIndex() {
        assertEquals(assertThrows(ResponseException.class, () -> {
            client.deleteIndex("test2");
        }).getResponse().getStatusLine().getStatusCode(), RestStatus.NOT_FOUND.getStatus());
    }

    @Test
    void insertCountAndGetDocument() throws IOException, InterruptedException {
        assertFalse(client.isIndexExists("test.index"));

        try (Document<DocObject> doc = new Document<>(client)) {
            doc.setIndex("test.index").getMapper().setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));

            // No mapping provided: creationDate and latlong fields will be stored using text type
            String id = doc.add(new EasyRandom(new EasyRandomParameters().seed(seed)).nextObject(DocObject.class));
            assertTrue((id != null) && !id.isEmpty());

            // Index is created automatically if not exist when inserting documents
            assertTrue(client.isIndexExists("test.index"));

            // Pause before asking # of doc in index
            Thread.sleep(2000);
            assertEquals(doc.count(), 1);

            DocObject docObj = doc.get(id, DocObject.class);
            System.out.println("Document=" + docObj.toString());
        }
    }

    @Test
    void insertDocumentWithSameId() throws IOException, InterruptedException {
        assertFalse(client.isIndexExists("test.index"));

        try (Document<DocObject> doc = new Document<>(client)) {
            doc.setIndex("test.index").getMapper().setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));

            EasyRandomParameters parameters = new EasyRandomParameters().seed(seed);
            EasyRandom easyRandom = new EasyRandom(parameters);

            final String id = doc.add(easyRandom.nextObject(DocObject.class));
            assertTrue((id != null) && !id.isEmpty());

            // Index is created automatically if not exist when inserting documents
            assertTrue(client.isIndexExists("test.index"));

            DocObject docObj = doc.get(id, DocObject.class);
            System.out.println("Document[id:" + id + "]=" + docObj.toString());

            // Pause before adding another doc
            Thread.sleep(2000);

            // Try to update previously inserted doc => use same id and set boolean flag to 'true'
            String id2 = doc.add(id, easyRandom.nextObject(DocObject.class), true);
            assertTrue((id2 != null) && !id2.isEmpty() && id2.equals(id));

            DocObject docObj2 = doc.get(id2, DocObject.class);
            System.out.println("Document[id:" + id2 + "]=" + docObj2.toString());

            Thread.sleep(2000);
            // We still should have 1 doc as we did an update
            assertEquals(doc.count(), 1);

            // Pause before adding another doc
            Thread.sleep(2000);

            // Try to add another doc still using same Id but this time with boolean flag to 'false' => must
            // fail with error
            assertThrows(DocumentAlreadyExistsException.class, () -> {
                doc.add(id, easyRandom.nextObject(DocObject.class), false);
            });
        }
    }

    @Test
    void insertThenSearchDocuments() throws IOException, InterruptedException {
        assertFalse(client.isIndexExists("test.index"));

        // Set explicit mapping so that dates and coordinates are handled the way we want
        String mapping = "{\"properties\": {\"attr1\": {\"type\": \"text\"}, \"attr2\": {\"type\": \"text\"},"
                + "\"attr3\": {\"type\": \"float\"}, \"attr4\": {\"type\": \"text\"},"
                + "\"creationDate\": {\"type\": \"date\", \"format\": \"yyyy-MM-dd HH:mm:ss\"},"
                + "\"latlong\": {\"type\": \"geo_point\"}}}";

        assertTrue(client.createIndex("test.index", mapping));

        try (Document<DocObject> doc = new Document<>(client)) {
            // Set date format for our Document mapper to match defined format for DocObject
            doc.setIndex("test.index").getMapper().setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));

            EasyRandomParameters parameters = new EasyRandomParameters().seed(seed)
                    .dateRange(LocalDate.of(2021, 12, 1), LocalDate.of(2022, 1, 10))
                    .randomize(FieldPredicates.named("latlong"), new LatLongGenerator());

            EasyRandom easyRandom = new EasyRandom(parameters);

            for (int i = 0; i < 15; i++) {
                assertTrue(() -> {
                    try {
                        // Insert documents
                        String id = doc.add(easyRandom.nextObject(DocObject.class));
                        return ((id != null) && !id.isEmpty());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            }

            // Pause before asking # of doc in index
            Thread.sleep(2000);
            assertEquals(doc.count(), 15);

            // Search all documents
            IndexPage<DocObject> docObjList = doc.search("{\"simple_query_string\": {\"query\": \"*\"}}", 0, 10,
                    DocObject.class);
            System.out.println(docObjList);
            assertFalse(docObjList.isLastPage());

            docObjList = doc.next(docObjList, DocObject.class); // Get remaining documents
            System.out.println(docObjList);
            assertTrue(docObjList.isLastPage());

            // Search for documents with creationDate date before 2022
            String queryDSL = "{\"range\": {\"creationDate\": {\"lt\": \"2022-01-01\", \"format\": \"yyyy-MM-dd\"}}}";

            docObjList = doc.search(queryDSL, 0, 5, DocObject.class);
            System.out.println(docObjList);

            // Search for documents in south hemisphere
            queryDSL = "{\"geo_bounding_box\": {\"latlong\": {\"top_left\": \"0,-180\", \"bottom_right\": \"-90,180\"}}}";

            docObjList = doc.search(queryDSL, 0, 5, DocObject.class);
            System.out.println(docObjList);

            // Search for documents within given distance (5000km radius from Paris, France)
            queryDSL = "{\"geo_distance\": {\"distance\": \"5000km\", \"latlong\": \"48.85,2.35\"}}";

            docObjList = doc.search(queryDSL, 0, 5, DocObject.class);
            System.out.println(docObjList);
        }
    }

    @Test
    void loadThenDeleteSearchTemplate() throws IOException {
        assertFalse(client.isSearchTemplateExists(TEST_SEARCH_TEMPLATE_ID));
        assertTrue(
                client.loadSearchTemplate(TEST_SEARCH_TEMPLATE_ID, TEST_SEARCH_TEMPLATES.get(TEST_SEARCH_TEMPLATE_ID)));
        assertTrue(client.isSearchTemplateExists(TEST_SEARCH_TEMPLATE_ID));
        assertTrue(client.deleteSearchTemplate(TEST_SEARCH_TEMPLATE_ID));
    }

    @Test
    void runSearchTemplate() throws IOException, InterruptedException {
        // Make sure our template has been loaded
        if (!client.isSearchTemplateExists(TEST_SEARCH_TEMPLATE_ID)) {
            assertTrue(client.loadSearchTemplate(TEST_SEARCH_TEMPLATE_ID,
                    TEST_SEARCH_TEMPLATES.get(TEST_SEARCH_TEMPLATE_ID)));
        }

        assertFalse(client.isIndexExists("test.index"));

        // Set explicit mapping with keyword type for fields we want to aggregate (attr4 & attr1 in our test
        // template)
        String mapping = "{\"properties\": {\"attr1\": {\"type\": \"keyword\"}, \"attr4\": {\"type\": \"keyword\"}}}";

        assertTrue(client.createIndex("test.index", mapping));

        // Insert documents before running our template
        try (Document<DocObject> doc = new Document<>(client)) {
            // Set date format for our Document mapper to match defined format for DocObject
            doc.setIndex("test.index").getMapper().setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));

            EasyRandomParameters parameters = new EasyRandomParameters().seed(seed);
            EasyRandom easyRandom = new EasyRandom(parameters);

            for (int i = 0; i < 30; i++) {
                assertTrue(() -> {
                    try {
                        // Insert documents
                        String id = doc.add(easyRandom.nextObject(DocObject.class));
                        return ((id != null) && !id.isEmpty());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            }

            // Pause before asking # of doc in index
            Thread.sleep(2000);
            assertEquals(doc.count(), 30);

            // Now, run template
            List<TermsAggregation> aggr = doc.getAggregations(TEST_SEARCH_TEMPLATE_ID);
            System.out.println(aggr);

            assertTrue(aggr.size() > 0);
        }

        assertTrue(client.deleteSearchTemplate(TEST_SEARCH_TEMPLATE_ID));
    }
}
