/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.cux.bomberman.world;

import com.cux.bomberman.world.walls.AbstractWall;
import com.sun.org.apache.bcel.internal.util.BCELifier;
import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;

/**
 *
 * @author root
 */
public class BCharacter {
    
    private int posX = 0;
    protected int posY = 0;
    protected int width = 20;
    protected int height = 30;
    protected String name;
    private HashMap<String, Integer> textures = new HashMap<String, Integer>(); // direction+state, texture = int(.gif)
    public int crtTexture = 2; // can also be {1, 3, 4, 14, 15, 16, 17, 20, 22, 23, 24}
    private String state = "Normal"; // can also be "Bomb", "Blow", "Win" and "Trapped"
    private String direction = "Right"; // can also be "Up", "Down" and "Left"
    private String id;
    protected int bombRange = 2;
    protected int speed = 1;
    protected boolean walking = false;
    
    {
        // walk in normal state
        textures.put("walkUpNormal", 1);
        textures.put("walkRightNormal", 2);
        textures.put("walkDownNormal", 3);
        textures.put("walkLeftNormal", 4);
        
        // walk with bomb
        textures.put("walkUpBomb", 14);
        textures.put("walkRightBomb", 16);
        textures.put("walkDownBomb", 17);
        textures.put("walkLeftBomb", 15);
        
        // walk while trapped
        textures.put("walkUpTrapped", 20);
        textures.put("walkRightTrapped", 24);
        textures.put("walkDownTrapped", 22);
        textures.put("walkLeftTrapped", 23);
        
         // walk while blow
        textures.put("walkUpBlow", 18);
        textures.put("walkRightBlow", 18);
        textures.put("walkDownBlow", 18);
        textures.put("walkLeftBlow", 18);
        
        // walk while win
        textures.put("walkUpWin", 10);
        textures.put("walkRightWin", 10);
        textures.put("walkDownWin", 10);
        textures.put("walkLeftWin", 10);
    }
    
    public BCharacter(String id){
        this.id = id;
        this.name = id;
    }

    public boolean isWalking() {
        return walking;
    }

    public void setWalking(boolean walking) {
        this.walking = walking;
    }

    public int getBombRange() {
        return bombRange;
    }

    public void setBombRange(int bombRange) {
        this.bombRange = bombRange;
    }

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
    
    public int addOrDropBomb(){
        if (state == "Normal") state = "Bomb";
        else if (state == "Bomb") state = "Normal";
        return 0;
    }
    
    public int makeTrapped(){
        state = "Trapped";
        return 0;
    }
    
    public int makeFree(){
        state = "Normal";
        return 0;
    }

    public int getCrtTexture() {
        return crtTexture;
    }

    public void setCrtTexture(int crtTexture) {
        this.crtTexture = crtTexture;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }
    
    public void moveUp(){
        //this.posY-=speed;
        //this.posY-=speed;
        walking = true;
        this.IAmWalking(this, "up");
    }
    
    public void moveDown(){
        //this.posY+=speed;
        //this.posY+=speed;
        walking = true;
        this.IAmWalking(this, "down");
    }
    
    public void moveLeft(){
        //this.posX-=speed;
        //this.posX-=speed;
        walking = true;
        this.IAmWalking(this, "left");
    }
    
    public void moveRight(){
        //this.posX+=speed;
        //this.posX+=speed;
        walking = true;
        this.IAmWalking(this, "right");
    }
    
    private synchronized void IAmWalking(final BCharacter myChar, final String direction){
        new Thread(new Runnable(){
            @Override
            public void run() {
                for (int i = 0; i < 5; i++){
                    switch(direction){
                        case "up":
                            myChar.posY--;
                            break;
                        case "down":
                            myChar.posY++;
                            break;
                        case "left":
                            myChar.posX--;
                            break;
                        case "right":
                            myChar.posX++;
                            break;
                    }
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(BCharacter.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                myChar.walking = false;
            }
        }).start();
    }
    
    public double getDistance(int x1, int y1, int x2, int y2){
        return Math.sqrt((x2-x1) * (x2-x1) + (y2-y1)*(y2-y1) );
    }
    
    public boolean hits(AbstractWall brick){
       int x1 = this.posX;
       int x2 = x1 + this.width;
       int y1 = this.posY;
       int y2 = y1 + this.height;
       
       int x11 = brick.getPosX();
       int x12 = x11 + brick.getWidth();
       int y11 = brick.getPosY();
       int y12 = y11 + brick.getHeight();
       if (this.direction == "Right" && x2 >= x11 && x2 < x12 && ((y1 >= y11 && y1 < y12) || (y2 > y11 && y2 <= y12))) return true;
       if (this.direction == "Left" && x1 > x11 && x1 <= x12 && ((y1 >= y11 && y1 < y12) || (y2 > y11 && y2 <= y12))) return true;
       if (this.direction == "Up" && y1 > y11 && y1 <= y12 && ((x1 >= x11 && x1 < x12) || (x2 > x11 && x2 <= x12))) return true;
       if (this.direction == "Down" && y2 >= y11 && y2 < y12 && ((x1 >= x11 && x1 < x12) || (x2 > x11 && x2 <= x12))) return true;
       return false;
    }
    
    public void stepBack(AbstractWall brick){
        
        if (this.direction == "Right") this.posX--;
        if (this.direction == "Left") this.posX++;
        if (this.direction == "Down") this.posY--;
        if (this.direction == "Up") this.posY++;
        
//        if (this.posX + this.width > brick.getPosX()) this.posX++;
//        else this.posX--;
//        
//        if (this.posY + this.height > brick.getPosY()) this.posY++;
//        else this.posY--;
    }
    
    public boolean walksTo(AbstractWall brick){
        if (this.direction == "Right" && this.posX + this.width <= brick.getPosX()) return true;
        if (this.direction == "Left" && this.posX >= brick.getPosX() + brick.getWidth()) return true;
        if (this.direction == "Down" && this.posY +this.height <= brick.getPosY()) return true;
        if (this.direction == "Up" && this.posY >= brick.getPosY() + brick.getHeight()) return true;
        return false;
    }
    
    @Override
    public String toString(){
        this.crtTexture = textures.get("walk"+this.direction+this.state);
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
