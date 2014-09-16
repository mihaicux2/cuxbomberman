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
public abstract class AbstractBlock {
    
    public int posX = 0;
    public int posY = 0;
    public int width = 30;
    public int height = 30;
    
    public AbstractBlock(){
         super();
    }
    
    public int getPosX() {
        return posX;
    }

    public int getPosY() {
        return posY;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
    
    public void setPosX(int posX) {
        this.posX = posX;
    }

    public void setPosY(int posY) {
        this.posY = posY;
    }

    public void setWidth(int width) {
        this.width = width;
    }
    
    public void setHeight(int height) {
        this.height = height;
    }
    
    public double distance(AbstractBlock o1){
        return Math.sqrt(Math.pow(Math.abs(this.posX - o1.posX), 2) + Math.pow(Math.abs(this.posY - o1.posY), 2));
    }
    
}
