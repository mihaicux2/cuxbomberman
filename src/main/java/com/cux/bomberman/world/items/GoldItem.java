package com.cux.bomberman.world.items;

/**
 * This class represents the "code" for the FLAME item
 * 
 * @version 1.0
 * @author  Mihail Cuculici (mihai.cuculici@gmail.com)
 * @author  http://www.
 */
public class GoldItem extends AbstractItem{

    /**
     * Public constructor
     * @param posX The X coordinate of the item
     * @param posY The Y coordinate of the item
     */
    public GoldItem(int posX, int posY) {
        super("gold");
        this.texture = "item-gold.PNG";
        this.scale = 1;
        this.setLifeTime(10);
        this.setPosX(posX);
        this.setPosY(posY);
    }
    
}
