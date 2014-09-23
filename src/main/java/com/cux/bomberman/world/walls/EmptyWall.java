package com.cux.bomberman.world.walls;

/**
 *
 * @author mihaicux
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
