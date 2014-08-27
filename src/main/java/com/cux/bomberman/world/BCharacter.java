/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.cux.bomberman.world;

import com.cux.bomberman.BombermanWSEndpoint;
import static com.cux.bomberman.BombermanWSEndpoint.peers;
import com.cux.bomberman.util.BLogger;
import com.cux.bomberman.world.generator.ItemGenerator;
import com.cux.bomberman.world.items.AbstractItem;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.websocket.EndpointConfig;
import javax.websocket.Session;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;

/**
 *
 * @author mihaicux
 */
public class BCharacter extends AbstractBlock{
    
    protected String name;
    private static final HashMap<String, Integer> textures = new HashMap<>(); // direction+state, texture = int(.gif)
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
    public boolean ready = false;
    private int plantedBombs = 0;
    private int dbId = 0;
    private EndpointConfig config = null;
    private int userId = 0;
    
    static {
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
    
    public BCharacter(String id, String name, int roomIndex, EndpointConfig config){
        this.id = id;
        this.name = name;
        this.roomIndex = roomIndex;
        this.creationTime = new Date();
        this.config = config;
    }

    public void incPlantedBombs(){
        if (this.plantedBombs < this.maxBombs){
            this.plantedBombs++;
        }
    }
    
    public void decPlantedBombs(){
        if (this.plantedBombs > 0){
            this.plantedBombs--;
        }
    }
    
    public int getPlantedBombs(){
        return this.plantedBombs;
    }
    
    public void setPlantedBombs(int plantedBombs){
        this.plantedBombs = plantedBombs;
    }
    
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
        this.saveToDB();
    }
    
    public int getUserId() {
        return this.userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
        if (this.dbId == 0){
            try {
                String query = "SELECT id FROM `characters` WHERE `user_id`=?";
                PreparedStatement st = (PreparedStatement)BombermanWSEndpoint.con.prepareStatement(query);
                st.setInt(1, this.userId);
                ResultSet rs = st.executeQuery();
                if(rs.next())
                {
                    this.dbId = rs.getInt("id");
                }
                else{
                    this.saveToDB();
                }
            } catch (SQLException ex) {
                BLogger.getInstance().logException2(ex);
            }
        }
    }
    
    public int getDbId() {
        return dbId;
    }

    public void setDbId(int dbId) {
        this.dbId = dbId;
        this.saveToDB();
    }
    
    public boolean getReady(){
        return this.ready;
    }
    
    public void setReady(boolean ready){
        this.ready = ready;
    }
    
    public int getMaxBombs() {
        return maxBombs;
    }

    public void setMaxBombs(int maxBombs) {
        this.maxBombs = maxBombs;
        this.saveToDB();
    }
    
    public boolean isTriggered() {
        return triggered;
    }

    public void setTriggered(boolean triggered) {
        this.triggered = triggered;
        this.saveToDB();
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
        this.saveToDB();
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
        this.saveToDB();
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
        this.saveToDB();
    }
    
    public int getDeaths(){
        return this.deaths;
    }
    
    public void setDeaths(int deaths){
        this.deaths = deaths;
        this.saveToDB();
    }
    
    public void resetScore(){
        this.kills = 0;
        this.deaths = 0;
    }
    
    public void incDeaths(){
        this.deaths++;
        this.saveToDB();
    }
    
    public void incKills(){
        this.kills++;
        this.saveToDB();
    }
    
    public void decKills(){
        this.kills--;
        this.saveToDB();
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
                //BombermanWSEndpoint.map.get(myChar.roomIndex).chars[x][y].remove(myChar.id);
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
                
                BombermanWSEndpoint.charsChanged.put(myChar.roomIndex, true);
                BombermanWSEndpoint.map.get(myChar.roomIndex).chars[x][y].remove(myChar.id);
                BombermanWSEndpoint.map.get(myChar.roomIndex).chars[x2][y2].put(myChar.id, myChar);
                
                for (int i = 0; i < World.wallDim; i++){
                    if (!myChar.isWalking()){
                        myChar.posX = (myChar.posX / World.wallDim) * World.wallDim;
                        myChar.posY = (myChar.posY / World.wallDim) * World.wallDim;
                        break;
                    }
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
                
                myChar.posX = (myChar.posX / World.wallDim) * World.wallDim;
                myChar.posY = (myChar.posY / World.wallDim) * World.wallDim;
                
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
                        Iterator it = peers.entrySet().iterator();
                        while (it.hasNext()) {
                            Map.Entry pairs = (Map.Entry) it.next();
                            Session peer = (Session) pairs.getValue();
                            if (peer.getId() == myChar.getId()){
                                BombermanWSEndpoint.getInstance().onMessage("bomb", peer, myChar.config);
                                break;
                            }
                        }
                        Thread.sleep(800); // almost 1 bomb per second
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
        BCharacter ret = new BCharacter(this.id, this.name, this.roomIndex, this.config);
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
    
    public void restoreFromDB(){
        try {
            if (this.dbId == 0){
                this.saveToDB();
            }
            String query = "SELECT * FROM `characters` WHERE `user_id`=?";
            PreparedStatement st = (PreparedStatement) BombermanWSEndpoint.con.prepareStatement(query);
            st.setInt(1, userId);
            ResultSet ret = st.executeQuery();
            if (ret.next()){
                this.setDbId(ret.getInt("id"));
                this.setName(ret.getString("name"));
                this.setSpeed(ret.getInt("speed"));
                this.setBombRange(ret.getInt("bomb_range"));
                this.setMaxBombs(ret.getInt("max_bombs"));
                this.setTriggered((ret.getInt("triggered") == 1) ? true : false);
                this.setKills(ret.getInt("kills"));
                this.setDeaths(ret.getInt("deaths"));
            }
        } catch (SQLException ex) {
            BLogger.getInstance().logException2(ex);
        }
    }
    
    public int logIn(){
        try {
            String query = "UPDATE `user` SET last_login=NOW() WHERE `id`=?";
            PreparedStatement st = (PreparedStatement)BombermanWSEndpoint.con.prepareStatement(query);
            st.setInt(1, this.userId);
            int affectedRows = st.executeUpdate();
            if (affectedRows == 0){
                throw new SQLException("Cannot save character. UserId : "+this.userId);
            }
            return 1;
        } catch (SQLException ex) {
            BLogger.getInstance().logException2(ex);
            return 0;
        }
    }
    
    public int saveToDB(){
        try {
            String query = "";
            if (this.dbId == 0){
                query = "INSERT INTO `characters` SET "
                        + "`name`=?,"
                        + "`speed`=?,"
                        + "`bomb_range`=?,"
                        + "`max_bombs`=?,"
                        + "`triggered`=?,"
                        + "`kills`=?,"
                        + "`deaths`=?,"
                        + "`user_id`=?,"
                        + "`creation_time`=NOW();";
            }
            else{
                query = "UPDATE `characters` SET "
                        + "`name`=?,"
                        + "`speed`=?,"
                        + "`bomb_range`=?,"
                        + "`max_bombs`=?,"
                        + "`triggered`=?,"
                        + "`kills`=?,"
                        + "`deaths`=?,"
                        + "`user_id`=?,"
                        + "`modification_time`=NOW()"
                        + "WHERE `id`=?";
            }
            PreparedStatement st = (PreparedStatement)BombermanWSEndpoint.con.prepareStatement(query);
            st.setString(1, this.name);
            st.setInt(2, this.speed);
            st.setInt(3, this.bombRange);
            st.setInt(4, this.maxBombs);
            st.setInt(5, this.triggered ? 1 : 0);
            st.setInt(6, this.kills);
            st.setInt(7, this.deaths);
            st.setInt(8, this.userId);
            if (this.dbId != 0){
                st.setInt(9, this.dbId);
            }
            int affectedRows = st.executeUpdate();
            if (affectedRows == 0){
                throw new SQLException("Cannot save character");
            }
            else{
                ResultSet rs = st.getGeneratedKeys();
                if(rs.next())
                {
                    this.dbId = rs.getInt(1);
                }
            }
            return 1;
        } catch (SQLException ex) {
            BLogger.getInstance().logException2(ex);
            return 0;
        }
    }
    
}
