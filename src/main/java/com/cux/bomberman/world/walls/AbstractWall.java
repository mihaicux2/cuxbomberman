/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.cux.bomberman.world.walls;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;



/**
 *
 * @author mihaicux
 */
public abstract class AbstractWall {

    protected int posX = 0;
    protected int posY = 0;
    protected int width = 20;
    protected int height = 20;
    protected String texture;
    protected String name;
    protected boolean blowable = false;
    
    protected AbstractWall(String name){
        this.name = name;
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

    public void setTexture(String texture) {
        this.texture = texture;
    }
    
    public void makeBlowable(){
        this.blowable = true;
    }
    
    public void makeUnblowable(){
        this.blowable = false;
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

    public String getName() {
        return name;
    }
    
    public String getTexture() {
        return texture;
    }
    
    public boolean isBlowable(){
        return this.blowable;
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
