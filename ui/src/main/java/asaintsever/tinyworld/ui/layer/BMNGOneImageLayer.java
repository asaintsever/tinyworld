package asaintsever.tinyworld.ui.layer;

import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.SurfaceImage;
import gov.nasa.worldwind.util.Logging;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BMNGOneImageLayer extends RenderableLayer {

    protected static Logger logger = LoggerFactory.getLogger(BMNGOneImageLayer.class);

    // Allows to set file from config (not possible in original WW BMNGOneImage class)
    protected static final String IMAGE_PATH = Configuration
            .getStringValue("gov.nasa.worldwind.avkey.BMNGOneImagePath");

    public BMNGOneImageLayer() {
        logger.debug("Loading " + IMAGE_PATH);

        this.setName(Logging.getMessage("layers.Earth.BlueMarbleOneImageLayer.Name"));
        this.addRenderable(new SurfaceImage(IMAGE_PATH, Sector.FULL_SPHERE));

        // Disable picking for the layer because it covers the full sphere and will override a terrain pick.
        this.setPickEnabled(false);
    }

    @Override
    public String toString() {
        return Logging.getMessage("layers.Earth.BlueMarbleOneImageLayer.Name");
    }
}
