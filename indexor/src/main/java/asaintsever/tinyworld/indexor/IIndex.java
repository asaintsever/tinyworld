package asaintsever.tinyworld.indexor;

import java.io.IOException;

public interface IIndex {

    Boolean create() throws IOException;
    
    Boolean delete() throws IOException;
    
    Boolean clear() throws IOException;
}
