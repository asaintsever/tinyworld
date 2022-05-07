package asaintsever.tinyworld.indexor;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Random;

import org.apache.commons.codec.digest.DigestUtils;
import org.jeasy.random.api.Randomizer;

public class UrlGenerator implements Randomizer<URL> {
    private Random random = new Random();

    @Override
    public URL getRandomValue() {
        // return random, but valid URL. Default Easy Random URL Randomizer is too limited (use short list of URLs): 
        // https://github.com/j-easy/easy-random/blob/master/easy-random-core/src/main/resources/easy-random-data.properties#L1
        byte[] rndBytes = new byte[20];
        this.random.nextBytes(rndBytes);
        
        try {
            return new URL("file:///" + DigestUtils.sha256Hex(rndBytes));
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
    }
}
