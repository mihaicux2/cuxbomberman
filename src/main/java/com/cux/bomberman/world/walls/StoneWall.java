package com.cux.bomberman.world.walls;

/**
 * This class represents the "code" for the STONE wall
 * 
 * @version 1.0
 * @author  Mihail Cuculici (mihai.cuculici@gmail.com)
 * @author  http://www.
 */
public class StoneWall extends AbstractWall{

    /**
     * Public constructor
     * @param posX The X coordinate of the item
     * @param posY The Y coordinate of the item
     */
    public StoneWall(int posX, int posY) {
        super("stone");
        this.texture = "stone.gif";
        this.setPosX(posX);
        this.setPosY(posY);
        this.makeUnblowable();
    }
    
}
