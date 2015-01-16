package com.cux.bomberman.world.items;

/**
 * This class represents the "code" for the TRIGGER item
 * 
 * @version 1.0
 * @author  Mihail Cuculici (mihai.cuculici@gmail.com)
 * @author  http://www.
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
