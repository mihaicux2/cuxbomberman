package com.cux.bomberman.world.walls;

/**
 * This class represents the "code" for the GRASS wall
 * 
 * @version 1.0
 * @author  Mihail Cuculici (mihai.cuculici@gmail.com)
 * @author  http://www.
 */
public class GrassWall extends AbstractWall{

    /**
     * Public constructor
     * @param posX The X coordinate of the item
     * @param posY The Y coordinate of the item
     */
    public GrassWall(int posX, int posY) {
        super("grass");
        this.texture = "grass.gif";
        this.setPosX(posX);
        this.setPosY(posY);
        this.makeBlowable();
    }
    
}
