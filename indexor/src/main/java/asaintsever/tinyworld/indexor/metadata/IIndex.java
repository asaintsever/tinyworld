package asaintsever.tinyworld.indexor.metadata;

import java.io.IOException;

public interface IIndex {

    Boolean create() throws IOException;
    
    Boolean clear() throws IOException;
}
