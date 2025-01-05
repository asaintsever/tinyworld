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
