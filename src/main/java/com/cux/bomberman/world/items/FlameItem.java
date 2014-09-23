package com.cux.bomberman.world.items;

/**
 *
 * @author mihaicux
 */
public class FlameItem extends AbstractItem{

    /**
     * Public constructor
     * @param posX The X coordinate of the item
     * @param posY The Y coordinate of the item
     */
    public FlameItem(int posX, int posY) {
        super("flame");
        this.texture = "item-flame.PNG";
        this.scale = 1;
        this.setTimed(false);
        this.setPosX(posX);
        this.setPosY(posY);
    }
    
}
