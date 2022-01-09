package asaintsever.tinyworld;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class CustomFloatSerializer extends JsonSerializer<Float> {

	@Override
	public void serialize(Float value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
		if (value != null) {
			// Only keep 2 digits
			Float roundedValue = Float.valueOf(String.format("%.2f", value));
			gen.writeNumber(roundedValue);
		} else {
			gen.writeNull();
		}
	}

}
