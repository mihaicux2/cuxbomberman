package com.cux.bomberman.world.items;

/**
 *
 * @author mihaicux
 */
public class SlowItem extends AbstractItem{

    /**
     * Public constructor
     * @param posX The X coordinate of the item
     * @param posY The Y coordinate of the item
     */
    public SlowItem(int posX, int posY) {
        super("slow");
        this.setLifeTime(10);
        this.setScale(3);
        this.texture = "item-slow.PNG";
        this.setPosX(posX);
        this.setPosY(posY);
    }
    
}
