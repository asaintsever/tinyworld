package asaintsever.tinyworld.indexor.opensearch.jackson;

import java.io.IOException;
import java.util.EnumSet;

import org.opensearch.client.json.JsonpDeserializer;
import org.opensearch.client.json.JsonpMapper;
import org.opensearch.client.json.jackson.JacksonJsonpGenerator;
import org.opensearch.client.json.jackson.JacksonJsonpParser;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.json.spi.JsonProvider;
import jakarta.json.stream.JsonGenerator;
import jakarta.json.stream.JsonParser;

public class JacksonJsonpMapper implements JsonpMapper {
    
    private final JacksonJsonProvider provider;
    private final ObjectMapper objectMapper;

    public JacksonJsonpMapper(ObjectMapper objectMapper) {
        this(objectMapper, new JsonFactory());
    }
    
    public JacksonJsonpMapper(ObjectMapper objectMapper, JsonFactory jsonFactory) {
        this.provider = new JacksonJsonProvider(jsonFactory);
        this.objectMapper = objectMapper;
    }
    
    public JacksonJsonpMapper() {
        this(new ObjectMapper());
    }
    
    /**
     * Returns the underlying Jackson mapper.
     */
    public ObjectMapper objectMapper() {
        return this.objectMapper;
    }

    @Override
    public JsonProvider jsonpProvider() {
        return provider;
    }

    @Override
    public <T> JsonpDeserializer<T> getDeserializer(Class<T> clazz) {
        return new JacksonValueParser<>(clazz);
    }

    @Override
    public <T> void serialize(T value, JsonGenerator generator) {
        if (!(generator instanceof JacksonJsonpGenerator)) {
            throw new IllegalArgumentException("Jackson' ObjectMapper can only be used with the JacksonJsonpProvider");
        }

        com.fasterxml.jackson.core.JsonGenerator jkGenerator = ((JacksonJsonpGenerator)generator).jacksonGenerator();
        try {
            objectMapper.writeValue(jkGenerator, value);
        } catch (IOException ioe) {
            throw JacksonUtils.convertException(ioe);
        }
    }
    
    private class JacksonValueParser<T> extends JsonpDeserializer<T> {

        private final Class<T> clazz;

        protected JacksonValueParser(Class<T> clazz) {
            super(EnumSet.allOf(JsonParser.Event.class));
            this.clazz = clazz;
        }

        @Override
        public T deserialize(JsonParser parser, JsonpMapper mapper, JsonParser.Event event) {

            if (!(parser instanceof JacksonJsonpParser)) {
                throw new IllegalArgumentException("Jackson' ObjectMapper can only be used with the JacksonJsonpProvider");
            }

            com.fasterxml.jackson.core.JsonParser jkParser = ((JacksonJsonpParser)parser).jacksonParser();

            try {
                return objectMapper.readValue(jkParser, clazz);
            } catch(IOException ioe) {
                throw JacksonUtils.convertException(ioe);
            }
        }
    }
}
