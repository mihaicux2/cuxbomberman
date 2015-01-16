package com.cux.bomberman.world.walls;

/**
 * This class represents the "code" for the WATER wall
 * 
 * @version 1.0
 * @author  Mihail Cuculici (mihai.cuculici@gmail.com)
 * @author  http://www.
 */
public class WaterWall extends AbstractWall{

    /**
     * Public constructor
     * @param posX The X coordinate of the item
     * @param posY The Y coordinate of the item
     */
    public WaterWall(int posX, int posY) {
        super("water");
        this.texture = "water.jpg";
        this.setPosX(posX);
        this.setPosY(posY);
        this.makeUnblowable();
    }
    
}
