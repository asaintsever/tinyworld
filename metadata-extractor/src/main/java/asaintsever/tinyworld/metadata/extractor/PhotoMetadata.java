package asaintsever.tinyworld.metadata.extractor;

import java.net.URL;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonInclude(Include.NON_NULL)
public class PhotoMetadata {
	
	protected static String EXIF_DATE_PATTERN; // Pattern for dates encoded in EXIF metadata
	protected static String JSON_DATE_PATTERN; // Pattern for dates in PhotoMetadata objects
	
	public enum HoursFormat {
		// HH for (0-23) hours format (hh is for (1-12) am/pm format)
		_24HOURS("HH"), _12HOURS("hh");
		
		private String value;
		
		private HoursFormat(String value) { this.value = value; }
		
		public String getValue() { return this.value; }
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
	public String gpsLat;
	public String gpsLong;
}
