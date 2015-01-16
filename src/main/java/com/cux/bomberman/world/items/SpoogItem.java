package com.cux.bomberman.world.items;

/**
 * This class represents the "code" for the SPOOG item
 * 
 * @version 1.0
 * @author  Mihail Cuculici (mihai.cuculici@gmail.com)
 * @author  http://www.
 */
public class SpoogItem extends AbstractItem{

    /**
     * Public constructor
     * @param posX The X coordinate of the item
     * @param posY The Y coordinate of the item
     */
    public SpoogItem(int posX, int posY) {
        super("spoog");
        this.texture = "item-spoog.PNG";
        this.scale = 1;
        this.setTimed(false);
        this.setPosX(posX);
        this.setPosY(posY);
    }
    
}
