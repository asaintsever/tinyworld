package asaintsever.tinyworld.cfg;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.SetEnvironmentVariable;

public class LoaderTest {
    
    @AfterEach
    void teardown() {
        Loader.setPathToConfigFile(Loader.DEFAULT_TINYWORL_CONFIG_FILE);
    }

    @Test
    void loadDefaultInternalConfig() {
        Configuration cfg = Loader.getConfig();
        assertNotNull(cfg);
        
        System.out.println(cfg.toString());
        
        assertEquals(cfg.indexor.cluster.embedded.enabled, true);
        assertEquals(cfg.indexor.cluster.embedded.expose, false);
        assertEquals(cfg.indexor.cluster.address, "localhost");
        assertEquals(cfg.indexor.cluster.port, 9200);
    }
    
    @Test
    // Override some defaults using env vars
    // If error, see https://junit-pioneer.org/docs/environment-variables/#warnings-for-reflective-access
    // Must add JVM args: --add-opens java.base/java.lang=ALL-UNNAMED --add-opens java.base/java.util=ALL-UNNAMED
    @SetEnvironmentVariable(key="TW_IDX_CLUSTER_EMBEDDED", value="false")
    @SetEnvironmentVariable(key="TW_IDX_CLUSTER_EMBEDDED_EXPOSE", value="true")
    @SetEnvironmentVariable(key="TW_IDX_CLUSTER_ADDRESS", value="127.0.0.1")
    @SetEnvironmentVariable(key="TW_IDX_CLUSTER_PORT", value="9210")
    void loadDefaultInternalConfigOverrideWithEnv() {
        Configuration cfg = Loader.getConfig();
        assertNotNull(cfg);
        
        System.out.println(cfg.toString());
        
        assertEquals(cfg.indexor.cluster.embedded.enabled, false);
        assertEquals(cfg.indexor.cluster.embedded.expose, true);
        assertEquals(cfg.indexor.cluster.address, "127.0.0.1");
        assertEquals(cfg.indexor.cluster.port, 9210);
    }
    
    @Test
    void loadCustomConfig() {
        Loader.setPathToConfigFile("src/test/resources/cfg_test1.yml");
        
        Configuration cfg = Loader.getConfig();
        assertNotNull(cfg);
        
        System.out.println(cfg.toString());
        
        assertEquals(cfg.indexor.cluster.embedded.enabled, true);
        assertEquals(cfg.indexor.cluster.embedded.expose, true);
        assertEquals(cfg.indexor.cluster.address, "localhost");
        assertEquals(cfg.indexor.cluster.port, 9200);
    }
}
