package util;

import java.awt.*;

public class PowerUpObject extends GameObject{
    private String textureLocation;
    private int imageIndex;

    public PowerUpObject(String texture, float x, int imageIndex) {
        super(new String[] {""}, 50, 50, new Point3f(x, 0, 0),
                new Rectangle((int)x, 0, 50, 50));

        this.textureLocation = texture;
        this.imageIndex = imageIndex;
    }

    public String getTextureLocation() {
        return textureLocation;
    }

    public int getImageIndex() {
        return imageIndex;
    }
}
