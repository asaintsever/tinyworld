package asaintsever.tinyworld.cfg;

import java.nio.file.Path;
import java.nio.file.Paths;

public class Env {
    public static final String TINYWORLD_USER_HOME = ".tinyworld";
    public static final String TINYWORLD_CONFIG_HOME = "config";
    public static final String TINYWORLD_CONFIG_FILE = "tinyworld.yml";
    
    public static final Path TINYWORLD_USER_HOME_PATH;
    public static final Path TINYWORLD_CONFIG_HOME_PATH;
    
    static {
        String homedir = System.getProperty("user.home") != null ? System.getProperty("user.home") : ".";
        
        TINYWORLD_USER_HOME_PATH = Paths.get(homedir, TINYWORLD_USER_HOME);
        TINYWORLD_CONFIG_HOME_PATH = Paths.get(TINYWORLD_USER_HOME_PATH.toString(), TINYWORLD_CONFIG_HOME);
    }
}
