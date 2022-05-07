package asaintsever.tinyworld.indexor.opensearch;

import java.io.IOException;

public class DocumentAlreadyExistsException extends IOException {
    
    private final String docId;
    
    public DocumentAlreadyExistsException(String id, IOException ex) {
        super(ex);
        this.docId = id;
    }
    
    public String getDocId() {
        return this.docId;
    }
}
