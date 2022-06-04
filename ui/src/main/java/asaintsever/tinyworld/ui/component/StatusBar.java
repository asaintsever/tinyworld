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
package asaintsever.tinyworld.ui.component;

import asaintsever.tinyworld.cfg.Configuration;


public class StatusBar extends gov.nasa.worldwind.util.StatusBar {

    protected IndexorStatusPanel indexorStatusPanel;

    
    public StatusBar(Configuration cfg) {
        super();
        
        // Remove elevation info in status bar (no need since we disabled elevation retrieval)
        this.remove(this.eleDisplay);
        
        indexorStatusPanel = new IndexorStatusPanel(cfg.indexor);
        this.add(indexorStatusPanel);
    }
    
    
    public IndexorStatusPanel getIndexorStatusPanel() {
        return this.indexorStatusPanel;
    }
}
