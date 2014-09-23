package com.cux.bomberman.world.items;

/**
 *
 * @author mihaicux
 */
public class EbolaItem extends AbstractItem{

    /**
     * Public constructor
     * @param posX The X coordinate of the item
     * @param posY The Y coordinate of the item
     */
    public EbolaItem(int posX, int posY) {
        super("ebola");
        this.setLifeTime(10);
        this.setScale(10);
        this.texture = "item-ebola.PNG";
        this.setPosX(posX);
        this.setPosY(posY);
    }
    
}
