package com.cux.bomberman.world.walls;

/**
 *
 * @author mihaicux
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
