/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.cux.bomberman.world;

import com.cux.bomberman.BombermanWSEndpoint;
import com.cux.bomberman.util.BLogger;
import com.cux.bomberman.world.generator.ItemGenerator;
import com.cux.bomberman.world.items.AbstractItem;
//import com.cux.bomberman.world.walls.AbstractWall;
//import com.sun.org.apache.bcel.internal.util.BCELifier;
import java.io.IOException;
import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.HashMap;
//import java.util.logging.Level;
//import java.util.logging.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;

/**
 *
 * @author mihaicux
 */
public class BCharacter extends AbstractBlock{
    
    protected String name;
    private HashMap<String, Integer> textures = new HashMap<>(); // direction+state, texture = int(.gif)
    public int crtTexture = 2; // can also be {1, 3, 4, 14, 15, 16, 17, 20, 22, 23, 24}
    private String state = "Normal"; // can also be "Bomb", "Blow", "Win" and "Trapped"
    private String direction = "Right"; // can also be "Up", "Down" and "Left"
    private String id;
    protected int bombRange = 1;
    protected int speed = 1; // first gear :)
    protected int maxBombs = 1; 
    protected boolean walking = false;
    protected boolean triggered = false; // checks if the character has a trigger for the "planted" bombs
    private World map;
    
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
    
    public BCharacter(String id, World map){
        this.id = id;
        this.name = id;
        this.map = map;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getMaxBombs() {
        return maxBombs;
    }

    public void setMaxBombs(int maxBombs) {
        this.maxBombs = maxBombs;
    }
    
    public boolean isTriggered() {
        return triggered;
    }

    public void setTriggered(boolean triggered) {
        this.triggered = triggered;
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
        if (speed > 9) speed = 9;
        this.speed = speed;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
    
    // change occupied block in the world matrix mapping
    private synchronized void IAmWalking(final BCharacter myChar, final String direction){
        new Thread(new Runnable(){
            @Override
            public void run() {
                map.chars[myChar.posX / World.wallDim][myChar.posY / World.wallDim].remove(myChar.name);
                for (int i = 0; i < World.wallDim; i++){
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
                        //Thread.sleep(10);
                        Thread.sleep(10-speed);
                    } catch (InterruptedException ex) {
                        BLogger.getInstance().logException2(ex);
                    }
                }
                map.chars[myChar.posX / World.wallDim][myChar.posY / World.wallDim].put(myChar.name, myChar);
                myChar.walking = false;
            }
        }).start();
    }
    
    public double getDistance(int x1, int y1, int x2, int y2){
        return Math.sqrt((x2-x1) * (x2-x1) + (y2-y1)*(y2-y1) );
    }
    
    public boolean hits(AbstractBlock block){ 
        
       boolean ret = false; 
        
       int x1 = this.posX;
       int x2 = x1 + this.width;
       int y1 = this.posY;
       int y2 = y1 + this.height;
       
       int x11 = block.getPosX();
       int x12 = x11 + block.getWidth();
       int y11 = block.getPosY();
       int y12 = y11 + block.getHeight();
       if (this.direction == "Right" && x2 >= x11 && x2 < x12 && ((y1 >= y11 && y1 < y12) || (y2 > y11 && y2 <= y12))) ret = true;
       if (this.direction == "Left" && x1 > x11 && x1 <= x12 && ((y1 >= y11 && y1 < y12) || (y2 > y11 && y2 <= y12))) ret = true;
       if (this.direction == "Up" && y1 > y11 && y1 <= y12 && ((x1 >= x11 && x1 < x12) || (x2 > x11 && x2 <= x12))) ret = true;
       if (this.direction == "Down" && y2 >= y11 && y2 < y12 && ((x1 >= x11 && x1 < x12) || (x2 > x11 && x2 <= x12))) ret = true;
       
       if (ret == true && AbstractItem.class.isAssignableFrom(block.getClass())){
           this.attachEvent((AbstractItem)block);
           map.blockMatrix[(block.getPosX() / World.wallDim)][block.getPosY() / World.wallDim] = null;
           try{
               BombermanWSEndpoint.items.remove((AbstractItem)block);
           } catch (ConcurrentModificationException ex) {
               BLogger.getInstance().logException2(ex);
           }
           ret = false;
       }
       
       return ret;
    }
    
    public void attachEvent(AbstractItem item){
        item.setCreationTime(new Date());
        switch (item.getName()) {
            case "trigger":
                this.setTriggered(true);
                break;
            case "skate":
                this.setSpeed(this.getSpeed() + item.getScale());
                break;
            case "slow":
                this.setSpeed(this.getSpeed() - item.getScale());
                break;
            case "flame":
                this.setBombRange(this.getBombRange() + item.getScale());
                break;
            case "spoog":
                this.setMaxBombs(this.getMaxBombs() + item.getScale());
                break;
            case "random":
                this.attachEvent(ItemGenerator.getInstance().generateRandomItem());
                break;
        }
        if (item.isTimed()){
            cycleEvent(this, item);
        }
    }
    
    private synchronized void cycleEvent(final BCharacter myChar, final AbstractItem item){
        new Thread(new Runnable(){
            @Override
            public void run() {
                try {
                    Thread.sleep(1000*item.getLifeTime());
                    switch(item.getName()){
                        case "trigger":
                            myChar.setTriggered(false);
                            break;
                        case "skate":
                            myChar.setSpeed(myChar.getSpeed()-item.getScale());
                            break;
                        case "slow":
                            myChar.setSpeed(myChar.getSpeed()+item.getScale());
                            break;
                        case "flame":
                            myChar.setBombRange(myChar.getBombRange()-item.getScale());
                            break;
                        case "spoog":
                            myChar.setMaxBombs(myChar.getMaxBombs() - item.getScale());
                            break;
                    }
                } catch (InterruptedException ex) {
                    BLogger.getInstance().logException2(ex);
                }
            }
        }).start();
    }
    
    public void stepBack(AbstractBlock block){
        
        if (this.direction == "Right") this.posX--;
        if (this.direction == "Left") this.posX++;
        if (this.direction == "Down") this.posY--;
        if (this.direction == "Up") this.posY++;
        
//        if (this.posX + this.width > block.getPosX()) this.posX++;
//        else this.posX--;
//        
//        if (this.posY + this.height > block.getPosY()) this.posY++;
//        else this.posY--;
    }
    
    public boolean walksTo(AbstractBlock block){
        if (this.direction == "Right" && this.posX + this.width <= block.getPosX()) return true;
        if (this.direction == "Left" && this.posX >= block.getPosX() + block.getWidth()) return true;
        if (this.direction == "Down" && this.posY +this.height <= block.getPosY()) return true;
        if (this.direction == "Up" && this.posY >= block.getPosY() + block.getHeight()) return true;
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
    
    @Override
    public BCharacter clone(){
        BCharacter ret = new BCharacter(this.id, this.map);
        ret.posX = this.posX;
        ret.posY = this.posY;
        ret.width = this.width;
        ret.height = this.height;
        ret.direction = this.direction;
        ret.bombRange = this.bombRange;
        ret.triggered = this.triggered;
        ret.id = this.id;
        ret.name = this.name;
        ret.state = this.state;
        ret.crtTexture = this.crtTexture;
        ret.speed = this.speed;
        ret.maxBombs = this.maxBombs;
        ret.walking = this.walking;
        
        return ret;
    }
    
}
