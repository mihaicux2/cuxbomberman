package com.cux.bomberman.world;

/**
 * This class represents the "building blocks" for all of the game objects
 * (characters/bots, walls, bombs, explosions)
 * 
 * @version 1.0
 * @author  Mihail Cuculici (mihai.cuculici@gmail.com)
 * @author  http://www.
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
    public synchronized int getPosX() {
        return posX;
    }

    /**
     * Getter for the Y coordinate of the block
     * @return The Y coordinate
     */
    public synchronized int getPosY() {
        return posY;
    }

    /**
     * Get the world matrix block column
     * @return The block column
     */
    public synchronized int getBlockPosX(){
        return (int) Math.ceil(posX / width);
    }
    
    /**
     * Get the world matrix block row
     * @return The block row
     */
    public synchronized int getBlockPosY(){
        return (int) Math.ceil(posY / height);
    }
    
    /**
     * Getter for the width of the block
     * @return The width of the block
     */
    public synchronized int getWidth() {
        return width;
    }

    /**
     * Getter for the height of the block
     * @return The height of the block
     */
    public synchronized int getHeight() {
        return height;
    }
    
    /**
     * Setter for the X coordinate of the block
     * @param posX The new position
     * @return The current object
     */
    public synchronized AbstractBlock setPosX(int posX) {
        this.posX = posX;
        return this;
    }

    /**
     * Setter for the Y coordinate of the block
     * @param posY The new position
     * @return The current object
     */
    public synchronized AbstractBlock setPosY(int posY) {
        this.posY = posY;
        return this;
    }

    /**
     * Setter for the width of the block
     * @param width The new size
     * @return The current object
     */
    public synchronized AbstractBlock setWidth(int width) {
        this.width = width;
        return this;
    }
    
    /**
     * Setter for the height of the block
     * @param height The new size
     * @return The current object
     */
    public synchronized AbstractBlock setHeight(int height) {
        this.height = height;
        return this;
    }
    
    /**
     * Public method used to calculate the (linear) distance between the current block and another block
     * @param o1 The block for the measurement
     * @return The required (linear) distance
     */
    public double distance(AbstractBlock o1){
        return Math.sqrt(Math.pow(Math.abs(this.posX - o1.posX), 2) + Math.pow(Math.abs(this.posY - o1.posY), 2));
    }
    
    public int boundNumber(int input, int min, int max){
        if (input < min) input = min;
        if (input > max) input = max;
        return input;
    }
    
}
