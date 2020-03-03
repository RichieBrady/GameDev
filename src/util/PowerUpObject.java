package util;

import java.awt.*;
/* Richard Brady
 * 16726839
 * */
/*power up class inherits GameObject. power ups use one image with no animation so this class
* allows for power up object creation with extra class variables and a single image*/

public class PowerUpObject extends GameObject{
    private String textureLocation;
    private int imageIndex;

    public PowerUpObject(String texture, float x, int imageIndex) {
        super(new String[] {""}, 50, 50, new Point3f(x, 0, 0),
                new Rectangle((int)x + 5, 5, 50 - 10, 50 - 10));

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
