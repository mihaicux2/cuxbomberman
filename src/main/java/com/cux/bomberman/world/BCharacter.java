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
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.HashMap;
import javax.websocket.Session;
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
    public int roomIndex;
    protected int kills = 0;
    protected int deaths = 0;
    public long connectionTime = 0; // in seconds
    public Date creationTime;
    private boolean dropBombs = false;
    
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
    
    public BCharacter(String id, int roomIndex){
        this.id = id;
        this.name = id;
        this.roomIndex = roomIndex;
        this.creationTime = new Date();
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
    
    // uncomment to make the original drop bomb movement (2 buttons ;)))
    public int addOrDropBomb(){
        //if (state == "Normal") state = "Bomb";
        //else if (state == "Bomb") state = "Normal";
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
    
    public int getKills(){
        return this.kills;
    }
    
    public void setKills(int kills){
        this.kills = kills;
    }
    
    public int getDeaths(){
        return this.deaths;
    }
    
    public void setDeaths(int deaths){
        this.deaths = deaths;
    }
    
    public void resetScore(){
        this.kills = 0;
        this.deaths = 0;
    }
    
    public void incDeaths(){
        this.deaths++;
    }
    
    public void incKills(){
        this.kills++;
    }
    
    public void decKills(){
        this.kills--;
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
            public synchronized void run() {
                
                int x = myChar.posX / World.wallDim;
                int y = myChar.posY / World.wallDim;
                int x2 = x;
                int y2 = y;
                //BombermanWSEndpoint.map.get(myChar.roomIndex).chars[x][y].remove(myChar.name);
                if (direction.equals("up")){
                    y2--;
                }
                else if (direction.equals("down")){
                    y2++;
                }
                else if (direction.equals("left")){
                    x2--;
                }
                else{
                    x2++;
                }
                
                BombermanWSEndpoint.map.get(myChar.roomIndex).chars[x][y].remove(myChar.name);
                BombermanWSEndpoint.map.get(myChar.roomIndex).chars[x2][y2].put(myChar.name, myChar);
                
                BombermanWSEndpoint.charsChanged.put(myChar.roomIndex, true);
                
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
                    BombermanWSEndpoint.charsChanged.put(myChar.roomIndex, true);
                }
                //BombermanWSEndpoint.map.get(myChar.roomIndex).chars[x][y].remove(myChar.name);
                //BombermanWSEndpoint.map.get(myChar.roomIndex).chars[x2][y2].put(myChar.name, myChar);
                myChar.walking = false;
                BombermanWSEndpoint.charsChanged.put(myChar.roomIndex, true);
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
           BombermanWSEndpoint.items.get(this.roomIndex).remove((AbstractItem)block);
           BombermanWSEndpoint.map.get(this.roomIndex).blockMatrix[(block.getPosX() / World.wallDim)][block.getPosY() / World.wallDim] = null;
           try{
               BombermanWSEndpoint.items.remove((AbstractItem)block); // eliminate the item
           } catch (ConcurrentModificationException ex) {
               BLogger.getInstance().logException2(ex);
           }
           BombermanWSEndpoint.itemsChanged.put(this.roomIndex, true);
           ret = false; // return false ;)
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
            case "ebola":
                this.dropBombs = true;
                this.setSpeed(this.getSpeed() - item.getScale());
                this.setMaxBombs(this.getMaxBombs() + item.getScale());
                this.cycleEbola(this);
                break;
            case "random":
                this.attachEvent(ItemGenerator.getInstance().generateRandomItem());
                break;
        }
        if (item.isTimed()){
            cycleEvent(this, item);
        }
    }
    
    public boolean dropsBombs(){
        return this.dropBombs;
    }
    
    private synchronized void cycleEvent(final BCharacter myChar, final AbstractItem item){
        new Thread(new Runnable(){
            @Override
            public synchronized void run() {
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
                        case "ebola":
                            myChar.dropBombs = false;
                            myChar.setSpeed(myChar.getSpeed() + item.getScale());
                            myChar.setMaxBombs(myChar.getMaxBombs() - item.getScale());
                            break;
                    }
                } catch (InterruptedException ex) {
                    BLogger.getInstance().logException2(ex);
                }
            }
        }).start();
    }
    
    public void cycleEbola(final BCharacter myChar){
        new Thread(new Runnable(){
            @Override
            public synchronized void run() {
                while (myChar.dropsBombs()){
                    try {
                        for (Session peer : BombermanWSEndpoint.peers){
                            if (peer.getId() == myChar.getId()){
                                BombermanWSEndpoint.getInstance().onMessage("bomb", peer);
                                break;
                            }
                        }
                        Thread.sleep(1000); // 1 bomb per second
                    } catch (InterruptedException ex) {
                        BLogger.getInstance().logException2(ex);
                    }
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
        Date now = new Date();
        this.connectionTime = (now.getTime() - this.creationTime.getTime()) / 1000;
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
        BCharacter ret = new BCharacter(this.id, this.roomIndex);
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
        ret.roomIndex = this.roomIndex;
        
        return ret;
    }
    
}
