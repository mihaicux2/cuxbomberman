package com.cux.bomberman.world.walls;

/**
 * This class represents the "code" for the BRICK wall
 * 
 * @version 1.0
 * @author  Mihail Cuculici (mihai.cuculici@gmail.com)
 * @author  http://www.
 */
public class BrickWall extends AbstractWall{

    /**
     * Public constructor
     * @param posX The X coordinate of the item
     * @param posY The Y coordinate of the item
     */
    public BrickWall(int posX, int posY) {
        super("brick");
        this.texture = "brick.gif";
        this.setPosX(posX);
        this.setPosY(posY);
        this.makeBlowable();
    }
    
}
