package asaintsever.tinyworld.indexor;

import java.io.IOException;

import asaintsever.tinyworld.metadata.extractor.PhotoMetadata;

public interface IPhoto {

    String add(PhotoMetadata photo, boolean allowUpdate) throws IOException;
    
    PhotoMetadata get(String id, Class<PhotoMetadata> docClass) throws IOException;
    
    long count() throws IOException;
    
    IndexPage<PhotoMetadata> search(String query, int from, int size, Class<PhotoMetadata> docClass) throws IOException;
    
    IndexPage<PhotoMetadata> next(IndexPage<PhotoMetadata> page, Class<PhotoMetadata> docClass) throws IOException;
}
