package com.cux.bomberman.world;

/**
 *
 * @author mihaicux
 */
public interface BBaseBotI {
    
    /**
     * Public method that will be implemented different for different bot types
     */
    public void searchAndDestroy();
    
    /**
     * Public method that will be implemented by each bot type
     * @param bombLocation The location of the bomb to be avoided
     * @param x The X coordinate of the BOT
     * @param y The Y coordinate of the BOT
     */
    public void avoidBomb(String bombLocation, int x, int y);
    
    /**
     * Public method used to get the description for each bot type
     * @return The requested description
     */
    public String getDescription();
    
}
