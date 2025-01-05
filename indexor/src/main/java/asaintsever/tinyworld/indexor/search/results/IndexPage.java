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
package asaintsever.tinyworld.indexor.search.results;

import java.util.Collections;
import java.util.List;

import lombok.Getter;
import lombok.ToString;

@ToString
public class IndexPage<T> {

    @SuppressWarnings("rawtypes")
    public static final IndexPage EMPTY = new IndexPage<>(Collections.emptyList(), null, 0, 0, 0);

    private final long total;
    private final int from;
    private final int size;

    @Getter
    private final boolean lastPage;

    private final String query;
    private final List<T> documents;

    public IndexPage(List<T> products, String query, long total, int from, int size) {
        this.documents = products;
        this.query = query;
        this.total = total;
        this.from = from;
        this.size = size;
        this.lastPage = (from + size) >= total;
    }

    public List<T> get() {
        return Collections.unmodifiableList(this.documents);
    }

    public String query() {
        return this.query;
    }

    public long total() {
        return this.total;
    }

    public int from() {
        return this.from;
    }

    public int size() {
        return this.size;
    }
}
