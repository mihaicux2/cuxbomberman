package com.cux.bomberman.world.items;

/**
 *
 * @author mihaicux
 */
public class RandomItem extends AbstractItem{

    /**
     * Public constructor
     * @param posX The X coordinate of the item
     * @param posY The Y coordinate of the item
     */
    public RandomItem(int posX, int posY) {
        super("random");
        this.texture = "item-random.PNG";
        this.scale = 1;
        this.setTimed(false);
        this.setPosX(posX);
        this.setPosY(posY);
    }
    
}
