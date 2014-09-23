package com.cux.bomberman.world.walls;

/**
 *
 * @author mihaicux
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
