package com.cux.bomberman.world.items;

/**
 * This class represents the "code" for the RANDOM item
 * 
 * @version 1.0
 * @author  Mihail Cuculici (mihai.cuculici@gmail.com)
 * @author  http://www.
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
