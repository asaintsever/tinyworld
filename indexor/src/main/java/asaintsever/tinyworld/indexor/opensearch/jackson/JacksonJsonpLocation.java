package asaintsever.tinyworld.indexor.opensearch.jackson;

import jakarta.json.stream.JsonLocation;

/**
 * Translate a Jackson location to a JSONP location.
 */
public class JacksonJsonpLocation implements JsonLocation {

    private final com.fasterxml.jackson.core.JsonLocation location;

    JacksonJsonpLocation(com.fasterxml.jackson.core.JsonLocation location) {
        this.location = location;
    }

    JacksonJsonpLocation(com.fasterxml.jackson.core.JsonParser parser) {
        this(parser.getTokenLocation());
    }

    @Override
    public long getLineNumber() {
        return location.getLineNr();
    }

    @Override
    public long getColumnNumber() {
        return location.getColumnNr();
    }

    @Override
    public long getStreamOffset() {
        long charOffset = location.getCharOffset();
        return charOffset == -1 ? location.getByteOffset() : charOffset;
    }
}
