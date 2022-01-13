package asaintsever.tinyworld.metadata.extractor;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class CustomDateSerializer extends JsonSerializer<Date> {
	
	@Override
	public void serialize(Date value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
		if (value != null) {
			SimpleDateFormat formatter = new SimpleDateFormat(PhotoMetadata.JSON_DATE_PATTERN);
			formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
			formatter.setLenient(false);
			
			gen.writeString(formatter.format(value));
		} else {
			gen.writeNull();
		}
	}

}
