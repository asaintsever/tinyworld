package asaintsever.tinyworld.cfg;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.text.StringSubstitutor;
import org.apache.commons.text.lookup.StringLookupFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

public class Loader {
    public static final String DEFAULT_TINYWORL_CONFIG_FILE = "config/tinyworld.yml";
    
    protected static Logger logger = LoggerFactory.getLogger(Loader.class);
    
    private static String TINYWORLD_CONFIG_FILE = DEFAULT_TINYWORL_CONFIG_FILE;
    private static ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
    private static StringSubstitutor stringSubstitutor = new StringSubstitutor(StringLookupFactory.INSTANCE.environmentVariableStringLookup());

    public static void setPathToConfigFile(String pathCfg) {
        TINYWORLD_CONFIG_FILE = pathCfg;
    }
    
    public static Configuration getConfig() {
        Configuration cfg = null;
        boolean internalCfg = false;
        
        // Try reading config from external file first
        try {
            if (logger.isDebugEnabled()) {
                String cwd = Paths.get("").toAbsolutePath().toString();
                logger.debug("Current working directory: " + cwd);
            }
            
            Path cfgFile = Path.of(TINYWORLD_CONFIG_FILE);
            cfg = unserialize(cfgFile);
        } catch (Exception e) {
            logger.warn("Fail to load configuration from external file [" + e.getMessage() + "]. Default to internal configuration.");
            internalCfg = true;
        }
        
        // As a fallback: get default config from file embedded in jar
        if (internalCfg) {
            try {
                ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
                URL resource = classLoader.getResource(TINYWORLD_CONFIG_FILE);
                Path cfgFile = Path.of(resource.toURI());
                cfg = unserialize(cfgFile);
            } catch (Exception e) {
                logger.error("Fail to load internal configuration", e);
            }
        }
        
        return cfg;
    }
    
    private static Configuration unserialize(Path cfgFilePath) throws IOException {       
        String cfgFileContent = stringSubstitutor.replace(new String(Files.readAllBytes(cfgFilePath)));
        Configuration cfg = mapper.readValue(cfgFileContent, Configuration.class);
        return cfg;
    }
}
