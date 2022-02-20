package asaintsever.tinyworld.indexor.metadata;

import java.io.IOException;

import asaintsever.tinyworld.metadata.extractor.PhotoMetadata;

public interface IPhoto {

    void add(PhotoMetadata photo) throws IOException;
}
