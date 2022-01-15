package asaintsever.tinyworld.metadata.extractor;

import java.net.URI;

import com.drew.imaging.FileType;
import com.drew.metadata.Metadata;

public interface IPhotoProcess {
    void task(URI uri, FileType fileType, Metadata metadata);
}
