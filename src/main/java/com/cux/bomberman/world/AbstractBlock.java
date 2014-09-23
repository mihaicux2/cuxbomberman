package com.cux.bomberman.world;

/**
 *
 * @author mihaicux
 */
public abstract class AbstractBlock {
    
    /**
     * THe X coordinate of the block
     */
    public int posX = 0;
    
    /**
     * The Y coordinate of the block
     */
    public int posY = 0;
    
    /**
     * The default block width
     */
    public int width = 30;
    
    /**
     * The default block height
     */
    public int height = 30;
    
    /**
     * Basic constructor to call the constructor tor the default Object class
     */
    public AbstractBlock(){
         super();
    }
    
    /**
     * Getter for the X coordinate of the block
     * @return The X coordinate
     */
    public int getPosX() {
        return posX;
    }

    /**
     * Getter for the Y coordinate of the block
     * @return The Y coordinate
     */
    public int getPosY() {
        return posY;
    }

    /**
     * Getter for the width of the block
     * @return The width of the block
     */
    public int getWidth() {
        return width;
    }

    /**
     * Getter for the height of the block
     * @return The height of the block
     */
    public int getHeight() {
        return height;
    }
    
    /**
     * Setter for the X coordinate of the block
     * @param posX The new position
     */
    public void setPosX(int posX) {
        this.posX = posX;
    }

    /**
     * Setter for the Y coordinate of the block
     * @param posY The new position
     */
    public void setPosY(int posY) {
        this.posY = posY;
    }

    /**
     * Setter for the width of the block
     * @param width The new size
     */
    public void setWidth(int width) {
        this.width = width;
    }
    
    /**
     * Setter for the height of the block
     * @param height The new size
     */
    public void setHeight(int height) {
        this.height = height;
    }
    
    /**
     * Public method used to calculate the (linear) distance between the current block and another block
     * @param o1 The block for the measurement
     * @return The required (linear) distance
     */
    public double distance(AbstractBlock o1){
        return Math.sqrt(Math.pow(Math.abs(this.posX - o1.posX), 2) + Math.pow(Math.abs(this.posY - o1.posY), 2));
    }
    
}
