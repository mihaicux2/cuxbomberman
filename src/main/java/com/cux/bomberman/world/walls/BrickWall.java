package com.cux.bomberman.world.walls;

/**
 *
 * @author mihaicux
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
