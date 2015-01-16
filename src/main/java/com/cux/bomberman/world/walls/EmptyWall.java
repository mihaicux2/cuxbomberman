package com.cux.bomberman.world.walls;

/**
 * This class represents the "code" for the EMPTY wall
 * (blank space)
 * 
 * @version 1.0
 * @author  Mihail Cuculici (mihai.cuculici@gmail.com)
 * @author  http://www.
 */
public class EmptyWall extends AbstractWall{

    /**
     * Public constructor
     * @param posX The X coordinate of the item
     * @param posY The Y coordinate of the item
     */
    public EmptyWall(int posX, int posY) {
        super("empty");
        this.texture = "";
        this.setPosX(posX);
        this.setPosY(posY);
    }
    
}
