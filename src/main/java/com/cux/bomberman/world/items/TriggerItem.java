package com.cux.bomberman.world.items;

/**
 *
 * @author mihaicux
 */
public class TriggerItem extends AbstractItem{

    /**
     * Public constructor
     * @param posX The X coordinate of the item
     * @param posY The Y coordinate of the item
     */
    public TriggerItem(int posX, int posY) {
        super("trigger");
        this.texture = "item-trig.PNG";
        this.setTimed(false);
        this.setPosX(posX);
        this.setPosY(posY);
    }
    
}
