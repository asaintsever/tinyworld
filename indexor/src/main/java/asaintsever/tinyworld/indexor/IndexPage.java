package asaintsever.tinyworld.indexor;

import java.util.Collections;
import java.util.List;

public class IndexPage<T> {
    
    public static final IndexPage EMPTY = new IndexPage<>(Collections.emptyList(), null, 0, 0, 0);

    private final List<T> documents;
    private final String query;
    private final long total;
    private final int from;
    private final int size;

    public IndexPage(List<T> products, String query, long total, int from, int size) {
        this.documents = products;
        this.query = query;
        this.total = total;
        this.from = from;
        this.size = size;
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
