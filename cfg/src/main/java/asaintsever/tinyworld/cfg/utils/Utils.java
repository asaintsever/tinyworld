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
package asaintsever.tinyworld.cfg.utils;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Utils {
    protected static Logger logger = LoggerFactory.getLogger(Utils.class);

    public interface IResourceProcessing<T> {
        T process(Path resourcePath) throws IOException;
    }

    public static byte[] getInternalResource(String resourceName) throws IOException, URISyntaxException {
        return getInternalResource(resourceName, null);
    }

    @SuppressWarnings("unchecked")
    public static <T> T getInternalResource(String resourceName, IResourceProcessing<T> resProcessing)
            throws IOException, URISyntaxException {
        T ret = null;
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        URL resource = classLoader.getResource(resourceName);

        if (logger.isDebugEnabled()) {
            logger.debug("Loading internal resource from: " + resource.toString());
        }

        try (FileSystem fs = initFileSystem(resource.toURI())) {
            Path resPath = Paths.get(resource.toURI());

            if (resProcessing != null)
                ret = resProcessing.process(resPath);
            else
                ret = (T) Files.readAllBytes(resPath);
        } catch (UnsupportedOperationException e) {
        }

        return ret;
    }

    // Create filesystem, required to read resources in zip/jar archives (see doc
    // https://docs.oracle.com/javase/7/docs/technotes/guides/io/fsp/zipfilesystemprovider.html)
    // See
    // https://stackoverflow.com/questions/25032716/getting-filesystemnotfoundexception-from-zipfilesystemprovider-when-creating-a-p
    private static FileSystem initFileSystem(URI uri) throws IOException {
        try {
            return FileSystems.newFileSystem(uri, Collections.emptyMap());
        } catch (IllegalArgumentException e) {
            return FileSystems.getDefault();
        }
    }
}
