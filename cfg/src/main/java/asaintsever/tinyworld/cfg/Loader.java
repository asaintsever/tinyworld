/*
 * Copyright 2021-2025 A. Saint-Sever
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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.text.StringSubstitutor;
import org.apache.commons.text.lookup.StringLookupFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import asaintsever.tinyworld.cfg.utils.Utils;
import asaintsever.tinyworld.cfg.utils.Utils.IResourceProcessing;

public class Loader {
    protected static Logger logger = LoggerFactory.getLogger(Loader.class);

    private static Path TINYWORLD_CONFIG_FILE_PATH = Paths.get(Env.TINYWORLD_CONFIG_HOME_PATH.toString(),
            Env.TINYWORLD_CONFIG_FILE);
    private static final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
    private static final StringSubstitutor stringSubstitutor = new StringSubstitutor(
            StringLookupFactory.INSTANCE.environmentVariableStringLookup());

    static void setPathToConfigFile(String pathCfg) {
        TINYWORLD_CONFIG_FILE_PATH = Paths.get(pathCfg);
    }

    public static Configuration getConfig() {
        return getConfig(true);
    }

    public static Configuration getConfig(boolean writeDefaultCfgIfNotFound) {
        Configuration cfg = null;
        boolean internalCfg = false;

        // Try reading config from external file first (in current user's home directory)
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("Try reading config from: " + TINYWORLD_CONFIG_FILE_PATH);
            }

            cfg = unserialize(TINYWORLD_CONFIG_FILE_PATH);
        } catch (Exception e) {
            logger.warn("Fail to load configuration from external file [" + e.getMessage()
                    + "]. Default to internal configuration.");
            internalCfg = true;
        }

        // As a fallback: get default config from file embedded in jar
        if (internalCfg) {
            try {
                cfg = Utils.getInternalResource(Env.TINYWORLD_CONFIG_HOME + "/" + Env.TINYWORLD_CONFIG_FILE,
                        new IResourceProcessing<Configuration>() {

                            @Override
                            public Configuration process(Path resourcePath) throws IOException {
                                Configuration defaultCfg = unserialize(resourcePath);

                                if (writeDefaultCfgIfNotFound) {
                                    // Write config in current user's home directory
                                    try {
                                        Files.createDirectories(Env.TINYWORLD_CONFIG_HOME_PATH);
                                        Files.copy(resourcePath, TINYWORLD_CONFIG_FILE_PATH);
                                    } catch (Exception e) {
                                        logger.error("Fail to write default configuration", e);
                                    }
                                }

                                return defaultCfg;
                            }
                        });
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
