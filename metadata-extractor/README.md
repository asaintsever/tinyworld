# TinyWorld Metadata Extractor

## Extracted Metadata

- **Taken Date (TZ)** *Exif:SubIFD:DateTimeOriginal (Exif:SubIFD:TimeZoneOffset)*
- **Width x Height** *Exif:SubIFD:ExifImageWidth x Exif:SubIFD:ExifImageHeight*
- **Country Code** *Iptc:IPTC:Country-PrimaryLocationCode*
- **Country** *Iptc:IPTC:Country-PrimaryLocationName*
- **State/Province** *Iptc:IPTC:Province-State*
- **City** *Iptc:IPTC:City*
- **Sublocation** *Iptc:IPTC:Sub-location*
- **Caption** *Iptc:IPTC:Caption-Abstract*
- **Title** *Iptc:IPTC:Title*
- **Headline** *Iptc:IPTC:Headline*
- GPS
  - **Lat** *Exif:GPS:LatitudeRef Exif:GPS:Latitude*
  - **Long** *Exif:GPS:LongitudeRef Exif:GPS:Longitude*
  - **Datum** *Exif:GPS:GPSMapDatum*
- **File Size**
- **File Name**
- **Absolute Path** *filesystem or URL*
- **Camera Model (Manufacturer)** *Exif:IFD0:Model (Exif:IFD0:Make)*
- **Thumbnail** *Exif:IFD1:ThumbnailImage*

## Format and Schema

- Format: JSON
- Schema
  
  ```json
  {"path":"<photo url>","fileName":"<photo filename>","sizeMb":<photo size in mb>,"takenDate":"<taken date>","timeZoneOffset":"<timezone offset>","thumbnail":"<base64-encoded photo thumbnail>","camModelMake":"<camera model and maker>","pixelRes":"<pixel resolution>","countryCode":"<country code>","country":"<country name>","stateOrProvince":"<state/province>","city":"<city>","sublocation":"<sub-location>","caption":"<caption>","title":"<title>","headline":"<headline>","gpsDatum":"<gps map datum>","gpsLat":"<gps latitude>","gpsLong":"<gps longitude>"}
  ```
