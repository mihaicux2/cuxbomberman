package com.cux.bomberman.world.walls;

/**
 * This class represents the "code" for the STEEL wall
 * 
 * @version 1.0
 * @author  Mihail Cuculici (mihai.cuculici@gmail.com)
 * @author  http://www.
 */
public class SteelWall extends AbstractWall{

    /**
     * Public constructor
     * @param posX The X coordinate of the item
     * @param posY The Y coordinate of the item
     */
    public SteelWall(int posX, int posY) {
        super("steel");
        this.texture = "steel.png";
        this.setPosX(posX);
        this.setPosY(posY);
        this.makeUnblowable();
    }
    
}
