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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Random;

import org.apache.commons.codec.digest.DigestUtils;
import org.jeasy.random.api.Randomizer;

public class UrlGenerator implements Randomizer<URL> {
    private final Random random = new Random();

    @Override
    public URL getRandomValue() {
        // return random, but valid URL. Default Easy Random URL Randomizer is too limited (use short list
        // of URLs):
        // https://github.com/j-easy/easy-random/blob/master/easy-random-core/src/main/resources/easy-random-data.properties#L1
        byte[] rndBytes = new byte[20];
        this.random.nextBytes(rndBytes);

        try {
            return new URL("file:///" + DigestUtils.sha256Hex(rndBytes));
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
    }
}
