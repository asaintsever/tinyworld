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
package asaintsever.tinyworld.metadata.extractor;

import java.net.URL;
import java.util.Calendar;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@ToString
@Setter
@Getter
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
    private URL path;

    private String fileName;

    @JsonSerialize(using = CustomFloatSerializer.class)
    private Float sizeMb;

    @JsonSerialize(using = CustomDateSerializer.class)
    private Date takenDate;

    private Short takenYear;
    private Short takenMonth;

    private String timeZoneOffset;

    /**
     * Base64-encoded thumbnail
     */
    private String thumbnail;

    /**
     * Model (Manufacturer)
     */
    private String camModelMake;

    /**
     * Width x Height
     */
    private String pixelRes;

    private String countryCode;
    private String country;
    private String stateOrProvince;
    private String city;
    private String sublocation;
    private String caption;
    private String title;
    private String headline;

    private String gpsDatum;

    // "lat,lon" format to comply with geo_point string format
    // (https://www.elastic.co/guide/en/elasticsearch/reference/current/geo-point.html)
    private String gpsLatLong;

    // IPTC keywords from photo but also custom ones (added by object/face detection algorithms later
    // on)
    private String[] tags;

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
            this.setCountryCode(defaultMetadata.countryCode);
            this.setGpsLatLong(defaultMetadata.gpsLatLong);
        }

        return this;
    }

    /**
     * Setters with data extraction
     */
    public PhotoMetadata setTakenDate(Date takenDate) {
        this.takenDate = takenDate;

        // Extract year and month
        if (this.takenDate != null) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(this.takenDate);
            return this.setTakenYear((short) calendar.get(Calendar.YEAR))
                    .setTakenMonth((short) (calendar.get(Calendar.MONTH) + 1));
        } else {
            return this;
        }
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

    public PhotoMetadata setCountryCode(String countryCode) {
        if (countryCode != null && !countryCode.isBlank()) {
            this.countryCode = countryCode;
        }

        return this;
    }

    public PhotoMetadata setGpsLatLong(String latlong) {
        if (latlong != null && !latlong.isBlank()) {
            this.gpsLatLong = latlong;
        }

        return this;
    }

    /**
     * Private setters (internal call only)
     */
    private PhotoMetadata setTakenYear(Short takenYear) {
        this.takenYear = takenYear;
        return this;
    }

    private PhotoMetadata setTakenMonth(Short takenMonth) {
        this.takenMonth = takenMonth;
        return this;
    }
}
