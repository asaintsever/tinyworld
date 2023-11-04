/*
 * Copyright 2021-2024 A. Saint-Sever
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 * More information about this project is available at:
 *
 *    https://github.com/asaintsever/tinyworld
 */
package asaintsever.tinyworld.cfg;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.SetEnvironmentVariable;

import static org.junit.jupiter.api.Assertions.*;

public class LoaderTest {

    @BeforeEach
    void setupTest() {
        Loader.setPathToConfigFile("target/newconfig.yml");
    }

    @Test
    void loadDefaultInternalConfig() {
        Configuration cfg = Loader.getConfig(false);
        assertNotNull(cfg);

        System.out.println(cfg.toString());

        assertTrue(cfg.indexor.cluster.embedded.enabled);
        assertFalse(cfg.indexor.cluster.embedded.expose);
        assertEquals(cfg.indexor.cluster.address, "localhost");
        assertEquals(cfg.indexor.cluster.port, 9200);
        assertEquals(cfg.indexor.cluster.index, "photos");
        assertEquals(cfg.ui.photoTree.filter.template, "year_month");
    }

    @Test
    // Override some defaults using env vars
    // If error, see
    // https://junit-pioneer.org/docs/environment-variables/#warnings-for-reflective-access
    // Must add JVM args: --add-opens java.base/java.lang=ALL-UNNAMED --add-opens
    // java.base/java.util=ALL-UNNAMED
    @SetEnvironmentVariable(key = "TW_IDX_CLUSTER_EMBEDDED", value = "false")
    @SetEnvironmentVariable(key = "TW_IDX_CLUSTER_EMBEDDED_EXPOSE", value = "true")
    @SetEnvironmentVariable(key = "TW_IDX_CLUSTER_ADDRESS", value = "127.0.0.1")
    @SetEnvironmentVariable(key = "TW_IDX_CLUSTER_PORT", value = "9210")
    @SetEnvironmentVariable(key = "TW_IDX_CLUSTER_INDEX", value = "test")
    @SetEnvironmentVariable(key = "TW_UI_PHOTOTREE_FILTER_TMPL", value = "country_year_month")
    void loadDefaultInternalConfigOverrideWithEnv() {
        Configuration cfg = Loader.getConfig(false);
        assertNotNull(cfg);

        System.out.println(cfg.toString());

        assertFalse(cfg.indexor.cluster.embedded.enabled);
        assertTrue(cfg.indexor.cluster.embedded.expose);
        assertEquals(cfg.indexor.cluster.address, "127.0.0.1");
        assertEquals(cfg.indexor.cluster.port, 9210);
        assertEquals(cfg.indexor.cluster.index, "test");
        assertEquals(cfg.ui.photoTree.filter.template, "country_year_month");
    }

    @Test
    void loadCustomConfig() {
        Loader.setPathToConfigFile("src/test/resources/cfg_test1.yml");

        Configuration cfg = Loader.getConfig(false);
        assertNotNull(cfg);

        System.out.println(cfg.toString());

        assertTrue(cfg.indexor.cluster.embedded.enabled);
        assertTrue(cfg.indexor.cluster.embedded.expose);
        assertEquals(cfg.indexor.cluster.address, "localhost");
        assertEquals(cfg.indexor.cluster.port, 9200);
        assertEquals(cfg.indexor.cluster.index, "my_index");
        assertEquals(cfg.ui.photoTree.filter.template, "year_country_month");
    }
}
