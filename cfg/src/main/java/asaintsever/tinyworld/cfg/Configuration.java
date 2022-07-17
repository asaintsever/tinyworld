/*
 * Copyright 2021-2022 A. Saint-Sever
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

import java.util.Map;

import asaintsever.tinyworld.metadata.extractor.PhotoMetadata;
import lombok.ToString;

@ToString
public class Configuration {
    public UI ui;
    public INDEXOR indexor;

    @ToString
    public class UI {
        public Deps deps;

        @ToString
        public class Deps {
            public Map<String, String> logging;
        }
    }

    @ToString
    public class INDEXOR {
        public Cluster cluster;
        public Photo photo;

        @ToString
        public class Cluster {
            public Embedded embedded;
            public String address;
            public int port;
            public String index;

            @ToString
            public class Embedded {
                public boolean enabled;
                public boolean expose;
            }
        }

        @ToString
        public class Photo {
            public PhotoMetadata defaultMetadata;
        }
    }
}
