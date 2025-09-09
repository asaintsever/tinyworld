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
package asaintsever.tinyworld.indexor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.text.ParseException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.drew.imaging.FileType;
import com.drew.metadata.Metadata;

import asaintsever.tinyworld.metadata.extractor.Extract;
import asaintsever.tinyworld.metadata.extractor.Extract.Result;
import asaintsever.tinyworld.metadata.extractor.IPhotoProcess;
import asaintsever.tinyworld.metadata.extractor.PhotoMetadata;
import asaintsever.tinyworld.metadata.extractor.PhotoObject;
import asaintsever.tinyworld.metadata.extractor.PhotoProcessException;

public class IndexorCmd {

    protected static Logger logger = LoggerFactory.getLogger(IndexorCmd.class);
    private static boolean clearIndex = true;
    private static boolean allowUpdate = false;

    private static void usage() {
        System.out.println("Usage: " + IndexorCmd.class.getCanonicalName()
                + " <full path to ingest> [<boolean to clear index if already exists, default is 'true'>] [<boolean to allow updates in index, default is 'false'>]\n");
        System.exit(1);
    }

    public static void main(String[] args) throws Exception {
        // Check args
        if (args.length == 0 || args.length > 3)
            usage();

        String ingestionPath = args[0];

        if (args.length >= 2)
            clearIndex = Boolean.valueOf(args[1]);
        if (args.length >= 3)
            allowUpdate = Boolean.valueOf(args[2]);

        System.out.println("--> current working directory: " + System.getProperty("user.dir"));
        System.out.println("--> clearIndex: " + clearIndex);
        System.out.println("--> allowUpdate: " + allowUpdate);

        // Create an indexor instance spawning an embedded cluster with expose set to 'true' to be able to
        // connect to it with Elasticvue tool
        try (Indexor indexor = new Indexor(Indexor.DEFAULT_HOST, Indexor.DEFAULT_PORT, "indexor.cmd", true, true)) {
            logger.info("Indexor Cmd started and ready to ingest photos from " + ingestionPath);

            PhotoMetadata defaultMetadata = new PhotoMetadata();
            defaultMetadata.setTakenYear((short) 1970).setTakenMonth((short) 1).setCountry("_Unknown_")
                    .setCountryCode("XXX").setGpsLatLong("25.0,-71.0");

            if (clearIndex) {
                // Clear index (may already exists): ie delete then create with default mapping for photo metadata
                indexor.metadataIndex().clear();
            }

            Result res = Extract.exploreFS(ingestionPath, Integer.MAX_VALUE, new IPhotoProcess() {

                @Override
                public void task(URI uri, FileType fileType, Metadata metadata) throws PhotoProcessException {
                    try {
                        PhotoObject photo = new PhotoObject(defaultMetadata); // Provide default metadata to be used if
                                                                              // not found in photos

                        // Extract then insert photo metadata
                        PhotoMetadata mtd = photo.extractMetadata(uri, fileType, metadata).getMetadata();
                        indexor.photos().add(mtd, allowUpdate);
                    } catch (IOException | ParseException e) {
                        throw new PhotoProcessException(e);
                    }
                }
            });

            logger.info("Number of ingested photos: " + res.getProcessed_ok());
            logger.info("Number of skipped files: " + res.getSkipped());
            logger.info("Number of errors: " + res.getProcessed_nok());

            if (res.getProcessed_nok() > 0) {
                System.out.println("\n----- ERRORS ----");
                for (String msg : res.getErrorMsg())
                    System.out.println("Error msg: " + msg);
            }

            System.out.println("\n>>>>>> Press Q + <Enter> to exit <<<<<<\n");
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
            String msg;

            while (true) {
                try {
                    msg = in.readLine();
                    if (msg.equals("Q") || msg.equals("q"))
                        break;
                } catch (IOException e) {
                }

                Thread.sleep(2000);
            }
        }
    }
}
