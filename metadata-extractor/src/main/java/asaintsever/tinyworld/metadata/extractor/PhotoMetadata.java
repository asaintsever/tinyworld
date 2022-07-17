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
package asaintsever.tinyworld.metadata.extractor;

import java.net.URL;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@ToString
@Setter
@Accessors(chain = true)
@JsonInclude(Include.NON_NULL)
public class PhotoMetadata {

    protected static String EXIF_DATE_PATTERN; // Pattern for dates encoded in EXIF metadata
    protected static String JSON_DATE_PATTERN; // Pattern for dates in PhotoMetadata objects

    public enum HoursFormat {
        // HH for (0-23) hours format (hh is for (1-12) am/pm format)
        _24HOURS("HH"), _12HOURS("hh");

        private String value;

        private HoursFormat(String value) {
            this.value = value;
        }

        public String getValue() {
            return this.value;
        }
    }

    public static void setHoursFormat(HoursFormat format) {
        EXIF_DATE_PATTERN = "yyyy:MM:dd " + format.getValue() + ":mm:ss";
        JSON_DATE_PATTERN = "yyyy-MM-dd " + format.getValue() + ":mm:ss";
    }

    static {
        // Default to 24h format
        setHoursFormat(HoursFormat._24HOURS);
    }

    /**
     * File protocol (eg "file:///<path>"), HTTP (eg "http://<path>") ...
     *
     * Used to compute Id/Primary Key during ingestion
     */
    public URL path;

    public String fileName;

    @JsonSerialize(using = CustomFloatSerializer.class)
    public Float sizeMb;

    @JsonSerialize(using = CustomDateSerializer.class)
    public Date takenDate;

    public String timeZoneOffset;

    /**
     * Base64-encoded thumbnail
     */
    public String thumbnail;

    /**
     * Model (Manufacturer)
     */
    public String camModelMake;

    /**
     * Width x Height
     */
    public String pixelRes;

    public String countryCode;
    public String country;
    public String stateOrProvince;
    public String city;
    public String sublocation;
    public String caption;
    public String title;
    public String headline;

    public String gpsDatum;

    // "lat,lon" format to comply with geo_point string format
    // (https://www.elastic.co/guide/en/elasticsearch/reference/current/geo-point.html)
    public String gpsLatLong;

    /**
     * Initialize from default metadata
     */
    public PhotoMetadata from(PhotoMetadata defaultMetadata) {
        if (defaultMetadata != null) {
            // Only handle default country & GPS coordinates to be able to easily filter & display non-geotagged
            // photos on the globe
            // Handling other fields not relevant unless we want to add extended metadata ingestion capabilities
            // to TinyWorld (not the intent now)
            this.setCountry(defaultMetadata.country);
            this.setGpsLatLong(defaultMetadata.gpsLatLong);
        }

        return this;
    }

    /**
     * Setters with validation (to not overwrite defaults)
     */

    public PhotoMetadata setCountry(String country) {
        if (country != null && !country.isBlank()) {
            this.country = country;
        }

        return this;
    }

    public PhotoMetadata setGpsLatLong(String latlong) {
        if (latlong != null && !latlong.isBlank()) {
            this.gpsLatLong = latlong;
        }

        return this;
    }
}
