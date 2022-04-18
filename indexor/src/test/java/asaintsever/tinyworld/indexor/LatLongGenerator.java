package asaintsever.tinyworld.indexor;

import java.util.Random;

import org.jeasy.random.api.Randomizer;

public class LatLongGenerator implements Randomizer<String> {
    private Random random = new Random();

    @Override
    public String getRandomValue() {
        // return random, but valid, "latitude,longitude" as per geo_point string format
        return random.nextDouble(-90.0, 90.0) + "," + random.nextDouble(-180.0, 180.0);
    }
}
