/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

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
     
    public void avoidBomb(String bombLocation, int x, int y);
    
}
