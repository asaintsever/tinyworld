package asaintsever.tinyworld.indexor;

import java.io.IOException;

import asaintsever.tinyworld.metadata.extractor.PhotoMetadata;

public interface IPhoto {

    String add(PhotoMetadata photo) throws IOException;
}
