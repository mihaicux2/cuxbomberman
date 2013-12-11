/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.cux.bomberman.world;

import java.io.IOException;
import java.util.Date;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;

/**
 *
 * @author root
 * @todo check if owner has remote for bomb => will set isVolatile : false & lifeTime = -1
 */
public class BBomb {

    private int posX = 0;
    protected int posY = 0;
    protected int width = 18;
    protected int height = 18;
    //protected String charId = "";
    protected BCharacter owner;
    protected Date creationTime = null;
    protected int lifeTime = 3;
    protected boolean volatileB = true;
    
    public BBomb(BCharacter owner){
        this.owner = owner;
        this.posX = owner.getPosX();
        this.posY = owner.getPosY();
        this.creationTime = new Date();
        
    }
    
    public int getPosX() {
        return posX;
    }

    public void setPosX(int posX) {
        this.posX = posX;
    }

    public int getPosY() {
        return posY;
    }

    public void setPosY(int posY) {
        this.posY = posY;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public String getCharId() {
        return this.owner.getName();
    }

    public Date getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Date creationTime) {
        this.creationTime = creationTime;
    }

    public int getLifeTime() {
        return lifeTime;
    }

    public void setLifeTime(int lifeTime) {
        this.lifeTime = lifeTime;
    }

    public boolean isVolatileB() {
        return volatileB;
    }

    public void setVolatileB(boolean volatileB) {
        this.volatileB = volatileB;
    }   
    
    @Override
    public String toString(){
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        try {
            return ow.writeValueAsString(this);
        } catch (IOException ex) {
           // Logger.getLogger(AbstractWall.class.getName()).log(Level.SEVERE, null, ex);
            return ex.getMessage();
           // return "";
        }
    }
    
}
