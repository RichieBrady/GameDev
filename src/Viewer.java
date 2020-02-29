import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import javax.imageio.ImageIO;
import javax.swing.JPanel;


/*
 * Created by Abraham Campbell on 15/01/2020.
 *   Copyright (c) 2020  Abraham Campbell

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
   
   (MIT LICENSE ) e.g do what you want with this :-) 
 */
public class Viewer extends JPanel {
    private long CurrentAnimationTime = 0;
    private boolean wallTilesCollected = false;
    Model gameworld;

    public Viewer(Model World) {
        this.gameworld = World;
        // TODO Auto-generated constructor stub
    }

    public Viewer(LayoutManager layout) {
        super(layout);
        // TODO Auto-generated constructor stub
    }

    public Viewer(boolean isDoubleBuffered) {
        super(isDoubleBuffered);
        // TODO Auto-generated constructor stub
    }

    public Viewer(LayoutManager layout, boolean isDoubleBuffered) {
        super(layout, isDoubleBuffered);
        // TODO Auto-generated constructor stub
    }

    public void updateview() {
        this.repaint();
        // TODO Auto-generated method stub
    }

    public void paintComponent(Graphics g) {

        super.paintComponent(g);
        CurrentAnimationTime++; // runs animation time step
        //Draw background
        drawBackground(g);

        gameworld.update();

        //Draw player Game Object
        int x = (int) gameworld.getPlayer().getCentre().getX();
        int y = (int) gameworld.getPlayer().getCentre().getY();
        int width = (int) gameworld.getPlayer().getWidth();
        int height = (int) gameworld.getPlayer().getHeight();
        String[] texture = gameworld.getPlayer().getTexture();

        //Draw player

        drawPlayer(x, y, width, height, texture, g);

        // //Draw Bullets
        // // change back
        // gameworld.getBullets().forEach((temp) ->
        // {
        //     drawBullet((int) temp.getCentre().getX(), (int) temp.getCentre().getY(), (int) temp.getWidth(), (int) temp.getHeight(), temp.getTexture(), g);
        // });

        // //Draw Enemies
        gameworld.getEnemies().forEach((temp) ->
                drawEnemies((int) temp.getCentre().getX(), (int) temp.getCentre().getY(), (int) temp.getWidth(), (int) temp.getHeight(), temp.getTexture(), g));

        // Draw power ups
        gameworld.getPowerUpList().forEach((temp) -> drawPowerUps((int) temp.getCentre().getX(), (int) temp.getCentre().getY(), (int) temp.getWidth(), (int) temp.getHeight(), temp.getTextureLocation(), temp.getImageIndex(), g));

        int xAxis = 30;
        int yAxis = 720;
        for (int i : gameworld.getLivesList()) {
            drawLives(xAxis, yAxis, g);
            xAxis += 10;
        }

        if (gameworld.isHasPower()) {
            gameworld.getPowerUpCollectedList().forEach((temp) -> drawHasPowerUp(temp.getTextureLocation(), temp.getImageIndex(), g));
        }
    }

    private void drawEnemies(int x, int y, int width, int height, String[] texture, Graphics g) {
        File TextureToLoad;  //should work okay on OSX and Linux but check if you have issues depending your eclipse install or if your running this without an IDE
        int imageWidth = 0;
        int imageHeight = 0;

        if (texture[0].contains("Grumpy_bee")) {
            imageWidth = 80;
            imageHeight = 61;
        } else if (texture[0].contains("rocket")) {
            imageWidth = 80;
            imageHeight = 52;
        } else if (texture[0].contains("spider")) {
            imageWidth = 100;
            imageHeight = 83;
        } else if (texture[0].contains("ufo_alien")) {
            imageWidth = 100;
            imageHeight = 47;
        } else if (texture[0].contains("skull_ufo_boss")) {
            imageWidth = 150;
            imageHeight = 101;
        }

        try {
            int currentPositionInAnimation = ((int) (((CurrentAnimationTime) * 6 % 100) / 10)); //slows down animation so every 10 frames we get another frame so every 100ms
            if (texture.length > 1) {
                if (currentPositionInAnimation % 2 == 0) {
                    TextureToLoad = new File(texture[0]);
                } else {
                    TextureToLoad = new File(texture[1]);
                }
            } else {
                TextureToLoad = new File(texture[0]);
            }
            Image myImage = ImageIO.read(TextureToLoad);
            //The spirte is 32x32 pixel wide and 4 of them are placed together so we need to grab a different one each time
            //remember your training :-) computer science everything starts at 0 so 32 pixels gets us to 31
            // TODO redraw enemy rectangles
            g.drawImage(myImage, x, y, x + imageWidth, y + imageHeight, 0, 0, imageWidth, imageHeight, null);
            g.setColor(Color.red);
            g.drawRect(x + 20, y + 20, 40, 40);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void drawBackground(Graphics g) {
        //should work okay on OSX and Linux but check if you have issues depending your eclipse install or if your running this without an IDE

        File TextureToLoad = new File("res/Full-Background.png");
        try {
            Image myImage = ImageIO.read(TextureToLoad);
            g.drawImage(myImage, 0, 0, 1536, 768, 0, 0, 3072, 1536, null);
            g.setColor(Color.red);
            g.drawRect(0, 665, 1535, 39);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void drawBullet(int x, int y, int width, int height, String texture, Graphics g) {
        File TextureToLoad = new File(texture);  //should work okay on OSX and Linux but check if you have issues depending your eclipse install or if your running this without an IDE
        try {
            Image myImage = ImageIO.read(TextureToLoad);
            //64 by 128
            g.drawImage(myImage, x, y, x + width, y + height, 0, 0, 63, 127, null);

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void drawLives(int x, int y, Graphics g) {
        File TextureToLoad = new File("res/life/life.png"); //should work okay on OSX and Linux but check if you have issues depending your eclipse install or if your running this without an IDE
        try {
            Image myImage = ImageIO.read(TextureToLoad);
            g.drawImage(myImage, x, y, x + 30, y + 30, 0, 0, 260, 207, null);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void drawHasPowerUp(String texture, int imageIndex, Graphics g) {
        File TextureToLoad = new File(texture); //should work okay on OSX and Linux but check if you have issues depending your eclipse install or if your running this without an IDE
        // TODO add image index here set size
        int imageWidth = 0;
        int imageHeight = 0;

        if (imageIndex == 0) {
            imageWidth = 320;
            imageHeight = 319;
        } else if (imageIndex == 1) {
            imageWidth = 696;
            imageHeight = 558;
        } else if (imageIndex == 2) {
            imageWidth = 522;
            imageHeight = 529;
        } else if (imageIndex == 3) {
            imageWidth = 459;
            imageHeight = 556;
        }

        try {
            Image myImage = ImageIO.read(TextureToLoad);

            g.drawImage(myImage, 25, 20, 55, 50, 0, 0, imageWidth, imageHeight, null);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void drawPowerUps(int x, int y, int width, int height, String texture, int imageIndex, Graphics g) {
        File TextureToLoad = new File(texture);;  //should work okay on OSX and Linux but check if you have issues depending your eclipse install or if your running this without an IDE
        int imageWidth = 0;
        int imageHeight = 0;

        if (imageIndex == 0) {
            imageWidth = 320;
            imageHeight = 319;
        } else if (imageIndex == 1) {
            imageWidth = 696;
            imageHeight = 558;
        } else if (imageIndex == 2) {
            imageWidth = 522;
            imageHeight = 529;
        } else if (imageIndex == 3) {
            imageWidth = 459;
            imageHeight = 556;
        }

        try {
            Image myImage = ImageIO.read(TextureToLoad);

            g.drawImage(myImage, x, y, x + width, y + height, 0, 0, imageWidth, imageHeight, null);
            g.setColor(Color.red);
            g.drawRect(x, y, width, height);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void drawPlayer(int x, int y, int width, int height, String[] texture, Graphics g) {
        //should work okay on OSX and Linux but check if you have issues depending your eclipse install or if your running this without an IDE
        File TextureToLoad;
        try {
            int currentPositionInAnimation = ((int) (((CurrentAnimationTime) * 6 % 100) / 10)); //slows down animation so every 10 frames we get another frame so every 100ms
            if (currentPositionInAnimation % 2 == 0) {
                TextureToLoad = new File(texture[0]);
            } else {
                TextureToLoad = new File(texture[1]);
            }
            Image myImage = ImageIO.read(TextureToLoad);
            g.drawImage(myImage, x, y, x + width, y + height, 0, 0, 80, 67, null);
            g.setColor(Color.red);
            g.drawRect(
                    (int)gameworld.getPlayer().getCollider().getX(),
                    (int)gameworld.getPlayer().getCollider().getY(),
                    (int)gameworld.getPlayer().getCollider().getWidth(),
                    (int)gameworld.getPlayer().getCollider().getHeight()
            );
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        //g.drawImage(img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, observer));
        //Lighnting Png from https://opengameart.org/content/animated-spaceships  its 32x32 thats why I know to increament by 32 each time
        // Bullets from https://opengameart.org/forumtopic/tatermands-art
        // background image from https://www.needpix.com/photo/download/677346/space-stars-nebula-background-galaxy-universe-free-pictures-free-photos-free-images
    }
}
/*
 * 
 * 
 *              VIEWER HMD into the world                                                             
                                                                                
                                      .                                         
                                         .                                      
                                             .  ..                              
                               .........~++++.. .  .                            
                 .   . ....,++??+++?+??+++?++?7ZZ7..   .                        
         .   . . .+?+???++++???D7I????Z8Z8N8MD7I?=+O$..                         
      .. ........ZOZZ$7ZZNZZDNODDOMMMMND8$$77I??I?+?+=O .     .                 
      .. ...7$OZZ?788DDNDDDDD8ZZ7$$$7I7III7??I?????+++=+~.                      
       ...8OZII?III7II77777I$I7II???7I??+?I?I?+?+IDNN8??++=...                  
     ....OOIIIII????II?I??II?I????I?????=?+Z88O77ZZO8888OO?++,......            
      ..OZI7III??II??I??I?7ODM8NN8O8OZO8DDDDDDDDD8DDDDDDDDNNNOZ= ......   ..    
     ..OZI?II7I?????+????+IIO8O8DDDDD8DNMMNNNNNDDNNDDDNDDNNNNNNDD$,.........    
      ,ZII77II?III??????DO8DDD8DNNNNNDDMDDDDDNNDDDNNNDNNNNDNNNNDDNDD+.......   .
      7Z??II7??II??I??IOMDDNMNNNNNDDDDDMDDDDNDDNNNNNDNNNNDNNDMNNNNNDDD,......   
 .  ..IZ??IIIII777?I?8NNNNNNNNNDDDDDDDDNDDDDDNNMMMDNDMMNNDNNDMNNNNNNDDDD.....   
      .$???I7IIIIIIINNNNNNNNNNNDDNDDDDDD8DDDDNM888888888DNNNNNNDNNNNNNDDO.....  
       $+??IIII?II?NNNNNMMMMMDN8DNNNDDDDZDDNN?D88I==INNDDDNNDNMNNMNNNNND8:..... 
   ....$+??III??I+NNNNNMMM88D88D88888DDDZDDMND88==+=NNNNMDDNNNNNNMMNNNNND8......
.......8=+????III8NNNNMMMDD8I=~+ONN8D8NDODNMN8DNDNNNNNNNM8DNNNNNNMNNNNDDD8..... 
. ......O=??IIIIIMNNNMMMDDD?+=?ONNNN888NMDDM88MNNNNNNNNNMDDNNNMNNNMMNDNND8......
........,+++???IINNNNNMMDDMDNMNDNMNNM8ONMDDM88NNNNNN+==ND8NNNDMNMNNNNNDDD8......
......,,,:++??I?ONNNNNMDDDMNNNNNNNNMM88NMDDNN88MNDN==~MD8DNNNNNMNMNNNDND8O......
....,,,,:::+??IIONNNNNNNDDMNNNNNO+?MN88DN8DDD888DNMMM888DNDNNNNMMMNNDDDD8,.... .
...,,,,::::~+?+?NNNNNNNMD8DNNN++++MNO8D88NNMODD8O88888DDDDDDNNMMMNNNDDD8........
..,,,,:::~~~=+??MNNNNNNNND88MNMMMD888NNNNNNNMODDDDDDDDND8DDDNNNNNNDDD8,.........
..,,,,:::~~~=++?NMNNNNNNND8888888O8DNNNNNNMMMNDDDDDDNMMNDDDOO+~~::,,,.......... 
..,,,:::~~~~==+?NNNDDNDNDDNDDDDDDDDNNND88OOZZ$8DDMNDZNZDZ7I?++~::,,,............
..,,,::::~~~~==7DDNNDDD8DDDDDDDD8DD888OOOZZ$$$7777OOZZZ$7I?++=~~:,,,.........   
..,,,,::::~~~~=+8NNNNNDDDMMMNNNNNDOOOOZZZ$$$77777777777II?++==~::,,,......  . ..
...,,,,::::~~~~=I8DNNN8DDNZOM$ZDOOZZZZ$$$7777IIIIIIIII???++==~~::,,........  .  
....,,,,:::::~~~~+=++?I$$ZZOZZZZZ$$$$$777IIII?????????+++==~~:::,,,...... ..    
.....,,,,:::::~~~~~==+?II777$$$$77777IIII????+++++++=====~~~:::,,,........      
......,,,,,:::::~~~~==++??IIIIIIIII?????++++=======~~~~~~:::,,,,,,.......       
.......,,,,,,,::::~~~~==+++???????+++++=====~~~~~~::::::::,,,,,..........       
.........,,,,,,,,::::~~~======+======~~~~~~:::::::::,,,,,,,,............        
  .........,.,,,,,,,,::::~~~~~~~~~~:::::::::,,,,,,,,,,,...............          
   ..........,..,,,,,,,,,,::::::::::,,,,,,,,,.,....................             
     .................,,,,,,,,,,,,,,,,.......................                   
       .................................................                        
           ....................................                                 
               ....................   .                                         
                                                                                
                                                                                
                                                                 GlassGiant.com
                                                                 */
