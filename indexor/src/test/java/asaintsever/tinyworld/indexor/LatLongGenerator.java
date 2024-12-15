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
package asaintsever.tinyworld.indexor;

import java.util.Random;

import org.jeasy.random.api.Randomizer;

public class LatLongGenerator implements Randomizer<String> {
    private final Random random = new Random();

    @Override
    public String getRandomValue() {
        // return random, but valid, "latitude,longitude" as per geo_point string format
        return random.nextDouble(-90.0, 90.0) + "," + random.nextDouble(-180.0, 180.0);
    }
}
