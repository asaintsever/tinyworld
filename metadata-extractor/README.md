# TinyWorld Metadata Extractor

## Extracted Metadata

| Attribute | Origin |
|-----------|--------|
| **Taken Date** | *Exif:SubIFD:DateTimeOriginal* |
| **Timezone Offset** | *Exif:SubIFD:TimeZoneOffset* |
| **Width x Height** | *Exif:SubIFD:ExifImageWidth, Exif:SubIFD:ExifImageHeight* |
| **Country Code** | *Iptc:IPTC:Country-PrimaryLocationCode* |
| **Country** | *Iptc:IPTC:Country-PrimaryLocationName* |
| **State/Province** | *Iptc:IPTC:Province-State* |
| **City** | *Iptc:IPTC:City* |
| **Sublocation** | *Iptc:IPTC:Sub-location* |
| **Caption** | *Iptc:IPTC:Caption-Abstract* |
| **Title** | *Iptc:IPTC:Title* |
| **Headline** | *Iptc:IPTC:Headline* |
| **Lat** | *Exif:GPS:LatitudeRef Exif:GPS:Latitude* |
| **Long** | *Exif:GPS:LongitudeRef Exif:GPS:Longitude* |
| **Datum** | *Exif:GPS:GPSMapDatum* |
| **File Size** | Photo file size |
| **File Name** | Photo file name |
| **Absolute Path** | URL to photo |
| **Camera Model (Manufacturer)** | *Exif:IFD0:Model (Exif:IFD0:Make)* |
| **Thumbnail** | *Exif:IFD1:ThumbnailImage* or generated from photo if not found |
| **Keywords** | *Iptc:IPTC:Keywords* |

## Format and Schema

- Format: JSON
- Schema
  
  ```json
  {
    "path":"<photo url>",
    "fileName":"<photo filename>",
    "sizeMb":<photo size in mb>,
    "takenDate":"<taken date>",
    "takenYear":<taken year>,
    "takenMonth":<taken month>,
    "timeZoneOffset":"<timezone offset>",
    "thumbnail":"<base64-encoded photo thumbnail>",
    "camModelMake":"<camera model and maker>",
    "pixelRes":"<pixel resolution>",
    "countryCode":"<country code>",
    "country":"<country name>",
    "stateOrProvince":"<state/province>",
    "city":"<city>",
    "sublocation":"<sub-location>",
    "caption":"<caption>",
    "title":"<title>",
    "headline":"<headline>",
    "gpsDatum":"<gps map datum>",
    "gpsLat":"<gps latitude>",
    "gpsLong":"<gps longitude>",
    "tags":["<keyword>", ...]
  }
  ```
