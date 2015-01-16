package com.cux.bomberman.world.items;

/**
 * This class represents the "code" for the SKATE item
 * 
 * @version 1.0
 * @author  Mihail Cuculici (mihai.cuculici@gmail.com)
 * @author  http://www.
 */
public class SkateItem extends AbstractItem{

    /**
     * Public constructor
     * @param posX The X coordinate of the item
     * @param posY The Y coordinate of the item
     */
    public SkateItem(int posX, int posY) {
        super("skate");
        this.setLifeTime(10);
        this.setScale(3);
        this.texture = "item-skate.PNG";
        this.setPosX(posX);
        this.setPosY(posY);
    }
    
}
