package asaintsever.tinyworld.indexor.opensearch.jackson;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParseException;

import jakarta.json.JsonException;
import jakarta.json.stream.JsonGenerationException;
import jakarta.json.stream.JsonParsingException;

class JacksonUtils {
    public static JsonException convertException(IOException ioe) {
        if (ioe instanceof com.fasterxml.jackson.core.JsonGenerationException) {
            return new JsonGenerationException(ioe.getMessage(), ioe);

        } else if (ioe instanceof com.fasterxml.jackson.core.JsonParseException) {
            JsonParseException jpe = (JsonParseException) ioe;
            return new JsonParsingException(ioe.getMessage(), jpe, new JacksonJsonpLocation(jpe.getLocation()));

        } else {
            return new JsonException("Jackson exception", ioe);
        }
    }
}
