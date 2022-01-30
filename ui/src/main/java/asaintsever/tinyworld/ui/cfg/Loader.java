package asaintsever.tinyworld.ui.cfg;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

public class Loader {
    public static String TINYWORLD_CONFIG_FILE = "config/tinyworld.yml";
    
    protected static Logger logger = LoggerFactory.getLogger(Loader.class);
    private static ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

    public static Configuration getConfig() {
        Configuration cfg = null;
        boolean internalCfg = false;
        
        // Try reading config from external file first
        try {
            if (logger.isDebugEnabled()) {
                String cwd = Paths.get("").toAbsolutePath().toString();
                logger.debug("Current working directory: " + cwd);
            }
            
            File cfgFile = Path.of(TINYWORLD_CONFIG_FILE).toFile();
            cfg = mapper.readValue(cfgFile, Configuration.class);
        } catch (Exception e) {
            logger.error("Fail to load configuration from external file [" + e.getMessage() + "]. Default to internal configuration.");
            internalCfg = true;
        }
        
        // As a fallback: get a default config from file embedded in jar
        if (internalCfg) {
            try {
                ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
                URL resource = classLoader.getResource(TINYWORLD_CONFIG_FILE);
                cfg = mapper.readValue(resource, Configuration.class);
            } catch (Exception e) {
                logger.error("Fail to load internal configuration: " + e.getMessage());
            }
        }
        
        return cfg;
    }
    
}
