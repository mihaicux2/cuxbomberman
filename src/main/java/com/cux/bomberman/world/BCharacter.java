package com.cux.bomberman.world;

import com.cux.bomberman.BombermanWSEndpoint;
import com.cux.bomberman.util.BLogger;
import com.cux.bomberman.world.generator.ItemGenerator;
import com.cux.bomberman.world.items.AbstractItem;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;
import javax.websocket.EndpointConfig;
import javax.websocket.Session;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;

/**
 * This class is used to represent the players of the game (bots and real users)
 * and it provides basic functionality for their actions
 *
 * @version 1.0
 * @author Mihail Cuculici (mihai.cuculici@gmail.com)
 * @author http://www.
 */
public class BCharacter extends AbstractBlock {

    /**
     * The player name
     */
    protected String name;

    /**
     * A list of all textures a player can hage
     */
    protected static final HashMap<String, Integer> textures = new HashMap<>(); // direction+state, texture = int(.gif)

    /**
     * Associations for the player textures
     */
    public int crtTexture = 2; // can also be {1, 3, 4, 14, 15, 16, 17, 20, 22, 23, 24}

    /**
     * A player state can be as follows: Normal, Bomb, Blow, Win and Trapped
     */
    protected String state = "Normal"; // can also be "Bomb", "Blow", "Win" and "Trapped"

    /**
     * A player direction can be as follows: right, up, left, down
     */
    protected String direction = "right"; // can also be "up", "down" and "left"

    /**
     * The id of the player (associated peed ID)
     */
    protected String id;

    /**
     * The maximum range an exploding bomb can reach
     */
    protected int bombRange = 1;

    /**
     * The moving speed of the player
     */
    protected int speed = 1; // first gear :)

    /**
     * The number of simultaneous bombs a player cand drop
     */
    protected int maxBombs = 1;

    /**
     * Check to see if the player is moving or stopped
     */
    public boolean walking = false;

    /**
     * Check to see if the player can detonate bombs with a trigger
     */
    protected boolean triggered = false; // checks if the character has a trigger for the "planted" bombs

    /**
     * Stores the room to which the player is connected
     */
    public int roomIndex;

    /**
     * Number of kills the player made
     */
    protected int kills = 0;

    /**
     * Numbers of player deaths
     */
    protected int deaths = 0;

    /**
     * Stores the number of seconds that passed since the player entered the
     * game
     */
    public long connectionTime = 0; // in seconds

    /**
     * Stores the date when the player entered the game
     */
    public Date creationTime;

    /**
     * If true, this property will make the player drop bombs unwillingly
     */
    protected boolean dropBombs = false;

    /**
     * Checks to see if the player is ready to play the game
     */
    public boolean ready = false;

    /**
     * The number of planted, unexploded bombs of the player
     */
    protected int plantedBombs = 0;

    /**
     * The database id of the player (from the table `characters`)
     */
    protected int dbId = 0;

    /**
     * The information about the player, stored in the cookies
     */
    protected EndpointConfig config = null;

    /**
     * The database id of the player (from the table `user`)
     */
    protected int userId = 0;

    /**
     * Set to true if the current player has admin privileges
     */
    protected boolean isAdmin = false;

    /**
     * Store the user's/bot's last valid move
     */
    protected String previousMove = null;

    /**
     * Store the user ip address
     */
    protected String ip = null;

    /**
     * The player session connection
     */
    protected Session peer = null;

    /**
     * Set to true if the current player can blow any type of walls
     */
    protected boolean gold = false;

    /**
     * Static initialization of the player textures
     */
    static {
        // walk in normal state
        textures.put("walkupNormal", 1);
        textures.put("walkrightNormal", 2);
        textures.put("walkdownNormal", 3);
        textures.put("walkleftNormal", 4);

        // walk with a bomb
        textures.put("walkupBomb", 14);
        textures.put("walkrightBomb", 16);
        textures.put("walkdownBomb", 17);
        textures.put("walkleftBomb", 15);

        // walk while trapped
        textures.put("walkupTrapped", 20);
        textures.put("walkrightTrapped", 24);
        textures.put("walkdownTrapped", 22);
        textures.put("walkleftTrapped", 23);

        // walk while blow
        textures.put("walkupBlow", 18);
        textures.put("walkrightBlow", 18);
        textures.put("walkdownBlow", 18);
        textures.put("walkleftBlow", 18);

        // walk while win
        textures.put("walkupWin", 10);
        textures.put("walkrightWin", 10);
        textures.put("walkdownWin", 10);
        textures.put("walkleftWin", 10);
    }

    /**
     * Public constructor of the current class
     *
     * @param id - the id of the connected peer
     * @param name - the name of the connected peer
     * @param roomIndex - the room associated with the player
     * @param config - the config information (restored from cookies)
     */
    public BCharacter(String id, String name, int roomIndex, EndpointConfig config) {
        this.id = id;
        this.name = name;
        this.roomIndex = roomIndex;
        this.creationTime = new Date();
        this.config = config;
    }

    /**
     * Increase the number of bombs a player can drop simultaneously
     *
     * @return The current object
     */
    public synchronized BCharacter incPlantedBombs() {
        if (this.plantedBombs < this.maxBombs) {
            this.plantedBombs++;
//            System.out.println("BCharacter.inc - " + this.getId()+" : am pus " + this.getPlantedBombs());
        }
        return this;
    }

    /**
     * Decrease the number of bombs a player can drop simultaneously
     *
     * @return The current object
     */
    public synchronized BCharacter decPlantedBombs() {
        if (this.plantedBombs > 0) {
            this.plantedBombs--;
//            System.out.println("BCharacter.dec - " + this.getId()+" : am pus " + this.getPlantedBombs());
        }
        return this;
    }

    /**
     * Check if the current player has admin privileges
     *
     * @return true if the user has admin privileges
     */
    public boolean getIsAdmin() {
        return this.isAdmin;
    }

    /**
     * Gives/revokes admin privileges on the current player
     *
     * @param isAdmin - the new value for the <b>isAdmin</b> property
     * @return The current object
     */
    public synchronized BCharacter setIsAdmin(boolean isAdmin) {
        this.isAdmin = isAdmin;
        return this;
    }

    /**
     * Get the number of bombs a player can drop simultaneously
     *
     * @return the number of bombs a player can drop
     */
    public synchronized int getPlantedBombs() {
        return this.plantedBombs;
    }

    /**
     * Set the number of bombs a player can drop simultaneously
     *
     * @param plantedBombs - the new number of bombs that can be dropped in the
     * same time
     * @return The current object
     */
    public synchronized BCharacter setPlantedBombs(int plantedBombs) {
        this.plantedBombs = plantedBombs;
        return this;
    }

    /**
     * Public getter for the id property
     *
     * @return The requested property
     */
    public String getId() {
        return id;
    }

    /**
     * Public setter for the id property
     *
     * @param id The new value
     * @return The current object
     */
    public BCharacter setId(String id) {
        this.id = id;
        this.saveToDB();
        return this;
    }

    /**
     * Public getter for the userId property
     *
     * @return The requested property
     */
    public int getUserId() {
        return this.userId;
    }

    /**
     * Public setter for the usedId property
     *
     * @param userId The new value
     * @return The current object
     */
    public BCharacter setUserId(int userId) {
        this.userId = userId;
        if (this.dbId == 0) {
            try {
                String query = "SELECT id FROM `characters` WHERE `user_id`=?";
                PreparedStatement st = (PreparedStatement) BombermanWSEndpoint.con.prepareStatement(query);
                st.setInt(1, this.userId);
                ResultSet rs = st.executeQuery();
                if (rs.next()) {
                    this.dbId = rs.getInt("id");
                } else {
                    this.saveToDB();
                }
            } catch (SQLException ex) {
                BLogger.getInstance().logException2(ex);
            }
        }
        return this;
    }

    /**
     * Public getter for the dbId property
     *
     * @return The requested property
     */
    public int getDbId() {
        return dbId;
    }

    /**
     * Public setter for the dbId property
     *
     * @param dbId he new value
     * @return The current object
     */
    public BCharacter setDbId(int dbId) {
        this.dbId = dbId;
        this.saveToDB();
        return this;
    }

    /**
     * Public getter for the ready property
     *
     * @return The requested property
     */
    public boolean getReady() {
        return this.ready;
    }

    /**
     * Public setter for the ready property
     *
     * @param ready The new value
     * @return The current object
     */
    public BCharacter setReady(boolean ready) {
        this.ready = ready;
        return this;
    }

    /**
     * Public getter for the maxBombs property
     *
     * @return The requested property
     */
    public synchronized int getMaxBombs() {
        return maxBombs;
    }

    /**
     * Public setter for the maxBombs property
     *
     * @param maxBombs The new value
     * @return The current object
     */
    public synchronized BCharacter setMaxBombs(int maxBombs) {
        this.maxBombs = maxBombs;
        this.saveToDB();
        return this;
    }

    /**
     * Public method used to tell if a bomb is detonated by a trigger or
     * explodes after<br />
     * a given amount of time (it's lifetime)
     *
     * @return The requester property
     */
    public synchronized boolean isTriggered() {
        return triggered;
    }

    /**
     * Public setter for the triggered property
     *
     * @param triggered The new value
     * @return The current object
     */
    public synchronized BCharacter setTriggered(boolean triggered) {
        this.triggered = triggered;
        this.saveToDB();
        return this;
    }

    /**
     * Public getter for the walking property
     *
     * @return The requester property
     */
    public synchronized boolean isWalking() {
        return walking;
    }

    /**
     * Public setter for the walking property
     *
     * @param walking The new value
     * @return The current object
     */
    public synchronized BCharacter setWalking(boolean walking) {
        this.walking = walking;
        return this;
    }

    /**
     * Public getter for the bombRange property
     *
     * @return The requested property
     */
    public synchronized int getBombRange() {
        return bombRange;
    }

    /**
     * Public setter for the bombRange property
     *
     * @param bombRange The new value
     * @return The current object
     */
    public synchronized BCharacter setBombRange(int bombRange) {
        this.bombRange = bombRange;
        this.saveToDB();
        return this;
    }

    /**
     * Public getter for the speed property
     *
     * @return The requested property
     */
    public synchronized int getSpeed() {
        return speed;
    }

    /**
     * Public setter for the speed property
     *
     * @param speed The new value
     * @return The current object
     */
    public synchronized BCharacter setSpeed(int speed) {
        if (speed > 9) {
            speed = 9;
        }
        this.speed = speed;
        return this;
    }

    /**
     * Public getter for the name property
     *
     * @return The requested property
     */
    public String getName() {
        return name;
    }

    /**
     * Public setter for the name property
     *
     * @param name The new value
     * @return The current object
     */
    public BCharacter setName(String name) {
        this.name = name;
        this.saveToDB();
        return this;
    }

    /**
     * Public getter for the ip property
     *
     * @return The requested property
     */
    public String getIp() {
        return ip;
    }

    /**
     * Public setter for the ip property
     *
     * @param ip The new value
     * @return The current object
     */
    public BCharacter setIp(String ip) {
        this.ip = ip;
        return this;
    }

    /**
     * Get the session of the current player
     *
     * @return the player connection session
     */
    public synchronized Session getPeer() {
        return this.peer;
    }

    /**
     * Set the session for the current player
     *
     * @param peer - the session for the current player connection
     * @return The current object
     */
    public synchronized BCharacter setPeer(Session peer) {
        this.peer = peer;
        return this;
    }

    /**
     * Public getter for the config property
     *
     * @return The requested property
     */
    public EndpointConfig getConfig() {
        return config;
    }

    /**
     * Public setter for the config property
     *
     * @param config The new value
     * @return The current object
     */
    public BCharacter setConfig(EndpointConfig config) {
        this.config = config;
        return this;
    }

    /**
     * Public getter for the gold property
     *
     * @return The requested property
     */
    public synchronized boolean getGold() {
        return this.gold;
    }

    /**
     * Public setter for the gold property
     *
     * @param gold The new value
     * @return The current object
     */
    public synchronized BCharacter setGold(boolean gold) {
        this.gold = gold;
        return this;
    }

    /**
     * Public method used to change the state of the character
     * <br />(uncomment to make the original drop bomb movement (2 buttons ;))))
     *
     * @return 0
     */
    public synchronized int addOrDropBomb() {
//        if (state == "Normal") state = "Bomb";
//        else if (state == "Bomb") state = "Normal";
        return 0;
    }

    /**
     * Public method used to change the state of the character
     *
     * @return 0
     */
    public synchronized int makeTrapped() {
        state = "Trapped";
        return 0;
    }

    /**
     * Public method used to change the state of the character
     *
     * @return 0
     */
    public synchronized int makeFree() {
        state = "Normal";
        return 0;
    }

    /**
     * Public getter for the crtTexture property
     *
     * @return The requested property
     */
    public synchronized int getCrtTexture() {
        return crtTexture;
    }

    /**
     * Public setter for the crtTexture property
     *
     * @param crtTexture The new value
     * @return The current object
     */
    public synchronized BCharacter setCrtTexture(int crtTexture) {
        this.crtTexture = crtTexture;
        return this;
    }

    /**
     * Public getter for the state property
     *
     * @return The requested property
     */
    public synchronized String getState() {
        return state;
    }

    /**
     * Public setter for the state property
     *
     * @param state The new value
     * @return The current object
     */
    public synchronized BCharacter setState(String state) {
        this.state = state;
        return this;
    }

    /**
     * Public getter for the direction property
     *
     * @return The requested property
     */
    public synchronized String getDirection() {
        return direction;
    }

    /**
     * Public setter for the direction property
     *
     * @param direction The new value
     * @return The current object
     */
    public synchronized BCharacter setDirection(String direction) {
        this.direction = direction;
        return this;
    }

    /**
     * Public getter for the kills property
     *
     * @return The requested property
     */
    public synchronized int getKills() {
        return this.kills;
    }

    /**
     * Public setter for the kills property
     *
     * @param kills The new value
     * @return The current object
     */
    public synchronized BCharacter setKills(int kills) {
        this.kills = kills;
        this.saveToDB();
        return this;
    }

    /**
     * Public getter for the deaths property
     *
     * @return The requested property
     */
    public synchronized int getDeaths() {
        return this.deaths;
    }

    /**
     * Public setter for the deaths property
     *
     * @param deaths The new value
     * @return The current object
     */
    public synchronized BCharacter setDeaths(int deaths) {
        this.deaths = deaths;
        this.saveToDB();
        return this;
    }

    /**
     * Public method used to reset the character's score
     *
     * @return The current object
     */
    public BCharacter resetScore() {
        this.kills = 0;
        this.deaths = 0;
        return this;
    }

    /**
     * Public method used to increase the number of deaths for the character
     *
     * @return The current object
     */
    public synchronized BCharacter incDeaths() {
        this.deaths++;
        this.saveToDB();
        return this;
    }

    /**
     * Public method used to increase the number of kills for the character
     *
     * @return The current object
     */
    public synchronized BCharacter incKills() {
        this.kills++;
        this.saveToDB();
        return this;
    }

    /**
     * Public method used to decrease the number of kills for the character
     *
     * @return The current object
     */
    public synchronized BCharacter decKills() {
        this.kills--;
        this.saveToDB();
        return this;
    }

    /**
     * Public method used to for the character to take a random move
     */
    public void moveRandom() {
        //if (previousMove == null){
        Random r = new Random();
        int rand = r.nextInt(100000);
        if (rand % 4 == 0) {
            previousMove = "left";
        } else if (rand % 3 == 0) {
            previousMove = "down";
        } else if (rand % 2 == 0) {
            previousMove = "up";
        } else {
            previousMove = "right";
        }
        //}
        move(previousMove);
    }

    /**
     * Public method used to make the character walk to a given direction
     *
     * @param direction The movement direction
     */
    public synchronized void move(String direction) {
        this.setDirection(direction);
        if (this.isWalking()) {
            return;
        }
        this.IAmWalking(direction);
    }

    /**
     * This method is used to make a character walk towards a given direction
     * for exactly a block distance
     *
     * @param direction The walking direction
     */
    protected synchronized void IAmWalking(final String direction) {
        this.setWalking(true);
        new Thread(new Runnable() {
            @Override
            public synchronized void run() {
                int x = BCharacter.this.getBlockPosX();
                int y = BCharacter.this.getBlockPosY();
                int x2 = x;
                int y2 = y;
                switch (direction) {
                    case "up":
                        y2--;
                        break;
                    case "down":
                        y2++;
                        break;
                    case "left":
                        x2--;
                        break;
                    default: // right
                        x2++;
                        break;
                }

                x = BCharacter.this.boundNumber(x, 0, BombermanWSEndpoint.map.get(BCharacter.this.roomIndex).getWorldWidth() - 1);
                y = BCharacter.this.boundNumber(y, 0, BombermanWSEndpoint.map.get(BCharacter.this.roomIndex).getWorldHeight() - 1);
                x2 = BCharacter.this.boundNumber(x2, 0, BombermanWSEndpoint.map.get(BCharacter.this.roomIndex).getWorldWidth() - 1);
                y2 = BCharacter.this.boundNumber(y2, 0, BombermanWSEndpoint.map.get(BCharacter.this.roomIndex).getWorldHeight() - 1);

                BombermanWSEndpoint.charsChanged.put(BCharacter.this.roomIndex, true);

                synchronized (BombermanWSEndpoint.map) {
                    BombermanWSEndpoint.map.get(BCharacter.this.roomIndex).chars[x][y].remove(BCharacter.this.id);
                    BombermanWSEndpoint.map.get(BCharacter.this.roomIndex).chars[x2][y2].add(BCharacter.this.id);
                }

                for (int i = 0; i < World.wallDim; i++) {
                    if (!BCharacter.this.isWalking()) {
                        BCharacter.this.posX = (BCharacter.this.getBlockPosX()) * World.wallDim;
                        BCharacter.this.posY = (BCharacter.this.getBlockPosY()) * World.wallDim;
                        break;
                    }
                    switch (direction) {
                        case "up":
                            BCharacter.this.posY--;
                            break;
                        case "down":
                            BCharacter.this.posY++;
                            break;
                        case "left":
                            BCharacter.this.posX--;
                            break;
                        case "right":
                            BCharacter.this.posX++;
                            break;
                    }
                    try {
                        Thread.sleep(10 - BCharacter.this.speed);
                    } catch (InterruptedException ex) {
                        BLogger.getInstance().logException2(ex);
                    }
                    BombermanWSEndpoint.charsChanged.put(BCharacter.this.roomIndex, true);
                }

                BCharacter.this.posX = (BCharacter.this.getBlockPosX()) * World.wallDim;
                BCharacter.this.posY = (BCharacter.this.getBlockPosY()) * World.wallDim;

                BCharacter.this.setWalking(false);
                try {
                    Thread.sleep(50);
                } catch (InterruptedException ex) {
                    BLogger.getInstance().logException2(ex);
                }
                BombermanWSEndpoint.charsChanged.put(BCharacter.this.roomIndex, true);
            }
        }).start();
    }

    /**
     * Public method used to calculate the linear distance between 2 given
     * points
     *
     * @param x1 The X coordinate of the first point
     * @param y1 The Y coordinate of the first point
     * @param x2 The X coordinate of the second point
     * @param y2 The Y coordinate of the second point
     * @return The calculated distance
     */
    public double getDistance(int x1, int y1, int x2, int y2) {
        return Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));
    }

    /**
     * Public method used to tell if the character hits a given block
     *
     * @param block The block to be checked
     * @return TRUE if the character hits the given block
     */
    public synchronized boolean hits(AbstractBlock block) {
        
        boolean ret = false;
        
        int blockBlockX = block.getBlockPosX();
        int blockBlockY = block.getBlockPosY();
        int charBlockX  = this.getBlockPosX();
        int charBlockY  = this.getBlockPosY();
        
        if ("right".equals(this.direction) && (blockBlockY == charBlockY) && (charBlockX+1 == blockBlockX)) ret=true;
        if ("left".equals(this.direction) && (blockBlockY == charBlockY) && (charBlockX-1 == blockBlockX)) ret=true;
        if ("up".equals(this.direction) && (blockBlockX == charBlockX) && (charBlockY-1 == blockBlockY)) ret=true;
        if ("down".equals(this.direction) && (blockBlockX == charBlockX) && (charBlockY+1 == blockBlockY)) ret=true;
        
        if (ret == true && AbstractItem.class.isAssignableFrom(block.getClass())) {
            this.attachEvent((AbstractItem) block);
            BombermanWSEndpoint.items.get(this.roomIndex).remove((AbstractItem) block);
            BombermanWSEndpoint.map.get(this.roomIndex).blockMatrix[(block.getBlockPosX())][block.getBlockPosY()] = null;
            while (true) {
                try {
                    BombermanWSEndpoint.items.get(this.roomIndex).remove(((AbstractItem) block).itemId); // eliminate the item
                    break;
                } catch (ConcurrentModificationException ex) {
                    BLogger.getInstance().logException2(ex);
                }
            }
            BombermanWSEndpoint.itemsChanged.put(this.roomIndex, true);
            ret=false;
        }
        
        return ret;
    }
    
    /**
     * Public method used to attach an event to the character (in item is picked
     * up)
     *
     * @param item The picked up item
     */
    public synchronized void attachEvent(AbstractItem item) {
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
            case "gold":
                this.setGold(true);
                break;
            case "random":
                this.attachEvent(ItemGenerator.getInstance().generateRandomItem(BombermanWSEndpoint.map.get(this.roomIndex).getWidth(), BombermanWSEndpoint.map.get(this.roomIndex).getHeight()));
                break;
        }
        if (item.isTimed()) {
            cycleEvent(this, item);
        }
    }

    /**
     * Public getter for the dropBombs property
     *
     * @return The requested property
     */
    public synchronized boolean dropsBombs() {
        return this.dropBombs;
    }

    /**
     * This method is used to revert the character properties back to their
     * original state
     *
     * @param myChar The current character
     * @param item The picked up item
     */
    protected synchronized void cycleEvent(final BCharacter myChar, final AbstractItem item) {
        new Thread(new Runnable() {
            @Override
            public synchronized void run() {
                try {
                    Thread.sleep(1000 * item.getLifeTime());
                    switch (item.getName()) {
                        case "trigger":
                            myChar.setTriggered(false);
                            break;
                        case "skate":
                            myChar.setSpeed(myChar.getSpeed() - item.getScale());
                            break;
                        case "slow":
                            myChar.setSpeed(myChar.getSpeed() + item.getScale());
                            break;
                        case "flame":
                            myChar.setBombRange(myChar.getBombRange() - item.getScale());
                            break;
                        case "spoog":
                            myChar.setMaxBombs(myChar.getMaxBombs() - item.getScale());
                            break;
                        case "ebola":
                            myChar.dropBombs = false;
                            myChar.setSpeed(myChar.getSpeed() + item.getScale());
                            myChar.setMaxBombs(myChar.getMaxBombs() - item.getScale());
                            break;
                        case "gold":
                            myChar.setGold(false);
                            break;
                    }
                } catch (InterruptedException ex) {
                    BLogger.getInstance().logException2(ex);
                }
            }
        }).start();
    }

    /**
     * This method is used to force the character to drop bombs and walk very
     * slow
     *
     * @param myChar The infected character
     */
    public synchronized void cycleEbola(final BCharacter myChar) {
        new Thread(new Runnable() {
            @Override
            public synchronized void run() {
                while (myChar.dropsBombs()) {
                    try {
//                        Iterator<Map.Entry<String, BCharacter>> it = BombermanWSEndpoint.chars.get(myChar.roomIndex).entrySet().iterator();
//                        while (it.hasNext()){
//                            Map.Entry<String, BCharacter> pair = it.next();
//                            BCharacter crtChar = pair.getValue();
//                            Session peer = crtChar.getPeer();
//                            if (peer == null){
//                                ((BBaseBot)crtChar).dropBomb();
//                            }
//                            else{
//                                BombermanWSEndpoint.getInstance().onMessage("bomb", peer, crtChar.config);
//                            }
//                        }
                        Session peer = myChar.getPeer();
                        if (peer == null) {
                            ((BBaseBot) myChar).dropBomb();
                        } else {
                            BombermanWSEndpoint.getInstance().onMessage("bomb", peer, myChar.config);
                        }
                        Thread.sleep(800); // almost 1 bomb per second
                    } catch (InterruptedException ex) {
                        BLogger.getInstance().logException2(ex);
                    }
                }
            }
        }).start();
    }

    /**
     * This method is used to make the character to enlarge the distance to a
     * given block
     *
     * @param block The block to avoid
     */
    public synchronized void stepBack(AbstractBlock block) {

        if ("right".equals(this.direction)) {
            this.posX--;
        }
        if ("left".equals(this.direction)) {
            this.posX++;
        }
        if ("down".equals(this.direction)) {
            this.posY--;
        }
        if ("up".equals(this.direction)) {
            this.posY++;
        }

//        if (this.posX + this.width > block.getPosX()) this.posX++;
//        else this.posX--;
//        
//        if (this.posY + this.height > block.getPosY()) this.posY++;
//        else this.posY--;
    }

    /**
     * Check if the character walks towards a given block
     *
     * @param block The block to be tested
     * @return TRUE if the character is getting closer to the given block
     */
    public synchronized boolean walksTo(AbstractBlock block) {
        
        int blockBlockX = block.getBlockPosX();
        int blockBlockY = block.getBlockPosY();
        int charBlockX  = this.getBlockPosX();
        int charBlockY  = this.getBlockPosY();
        
        if ("right".equals(this.direction) && charBlockX <= blockBlockX) {
            return true;
        }
        if ("left".equals(this.direction) && blockBlockX >= blockBlockX) {
            return true;
        }
        if ("down".equals(this.direction) && blockBlockY <=blockBlockY) {
            return true;
        }
        return "up".equals(this.direction) && blockBlockY >= blockBlockY;
    }

    abstract class MixIn {

        @JsonIgnore
        abstract Session getPeer();

        @JsonIgnore
        abstract int getBombRange();

        @JsonIgnore
        abstract int getSpeed();

        @JsonIgnore
        abstract int getMaxBombs();

        @JsonIgnore
        abstract boolean isTriggered();

        @JsonIgnore
        abstract EndpointConfig getConfig();
    }

    /**
     * Public method used to convert the character to JSON, to be sent to a
     * client
     *
     * @return The JSON representation of the character
     */
    @Override
    public String toString() {
        this.crtTexture = textures.get("walk" + this.direction + this.state);
        Date now = new Date();
        this.connectionTime = (now.getTime() - this.creationTime.getTime()) / 1000;
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.getSerializationConfig().addMixInAnnotations(BCharacter.class, BCharacter.MixIn.class);
        ObjectWriter ow = objectMapper.writer().withDefaultPrettyPrinter();
        try {
            return ow.writeValueAsString(this);
        } catch (IOException ex) {
            // Logger.getLogger(AbstractWall.class.getName()).log(Level.SEVERE, null, ex);
            return ex.getMessage();
            // return "";
        }
    }

    /**
     * Overwritten method used to create a copy of the current character
     *
     * @return a clone of the current object
     */
    @Override
    public synchronized BCharacter clone() {
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

    /**
     * Public method used to restore the character properties from the database
     */
    public synchronized void restoreFromDB() {
        try {
            if (this.dbId == 0) {
                this.saveToDB();
            }
            String query = "SELECT * FROM `characters` WHERE `user_id`=?";
            PreparedStatement st = (PreparedStatement) BombermanWSEndpoint.con.prepareStatement(query);
            st.setInt(1, userId);
            ResultSet ret = st.executeQuery();
            if (ret.next()) {
                this.setDbId(ret.getInt("id"));
                this.setName(ret.getString("name"));
                this.setSpeed(ret.getInt("speed"));
                this.setBombRange(ret.getInt("bomb_range"));
                this.setMaxBombs(ret.getInt("max_bombs"));
                this.setTriggered((ret.getInt("triggered") == 1));
                this.setKills(ret.getInt("kills"));
                this.setDeaths(ret.getInt("deaths"));
            }
        } catch (SQLException ex) {
            BLogger.getInstance().logException2(ex);
        }
    }

    /**
     * Public method used to update the login date for the character
     *
     * @return 1 if the query run without errors
     */
    public int storeLogIn() {
        try {

            String currentTime = BombermanWSEndpoint.getInstance().getMySQLDateTime();

            String insQuery = "INSERT INTO `login_history` SET user_id=?, ip=?, login_date=?";
            PreparedStatement insSt = (PreparedStatement) BombermanWSEndpoint.con.prepareStatement(insQuery);
            insSt.setInt(1, this.userId);
            insSt.setString(2, this.ip);
            insSt.setString(3, currentTime);
            int insAffectedRows = insSt.executeUpdate();
            if (insAffectedRows == 0) {
                throw new SQLException("Cannot save character. UserId : " + this.userId);
            }

            String upQuery = "UPDATE `user` SET last_login=? WHERE `id`=?";
            PreparedStatement upSt = (PreparedStatement) BombermanWSEndpoint.con.prepareStatement(upQuery);
            upSt.setString(1, currentTime);
            upSt.setInt(2, this.userId);
            int upAffectedRows = upSt.executeUpdate();
            if (upAffectedRows == 0) {
                throw new SQLException("Cannot save character. UserId : " + this.userId);
            }
            return 1;
        } catch (SQLException ex) {
            BLogger.getInstance().logException2(ex);
            return 0;
        }
    }

    public synchronized int resetEvolution() {
        this.setBombRange(1);
        this.setDeaths(0);
        this.setKills(0);
        this.setMaxBombs(1);
        this.setSpeed(1);
        this.setTriggered(false);
        return this.saveToDB();
    }

    /**
     * Public method used to store the character properties in the database
     *
     * @return 1 if the query run without errors
     */
    public int saveToDB() {
        try {
            String query;
            if (this.dbId == 0) {
                query = "INSERT INTO `characters` SET "
                        + "`name`=?,"
                        + "`speed`=?,"
                        + "`bomb_range`=?,"
                        + "`max_bombs`=?,"
                        + "`triggered`=?,"
                        + "`kills`=?,"
                        + "`deaths`=?,"
                        + "`user_id`=?,"
                        + "`creation_time`='" + BombermanWSEndpoint.getInstance().getMySQLDateTime() + "';";
            } else {
                query = "UPDATE `characters` SET "
                        + "`name`=?,"
                        + "`speed`=?,"
                        + "`bomb_range`=?,"
                        + "`max_bombs`=?,"
                        + "`triggered`=?,"
                        + "`kills`=?,"
                        + "`deaths`=?,"
                        + "`user_id`=?,"
                        + "`modification_time`='" + BombermanWSEndpoint.getInstance().getMySQLDateTime() + "'"
                        + "WHERE `id`=?";
            }
            PreparedStatement st = (PreparedStatement) BombermanWSEndpoint.con.prepareStatement(query);
            st.setString(1, this.name);
//            st.setInt(2, this.speed);
            st.setInt(2, 1);
            st.setInt(3, this.bombRange);
            st.setInt(4, this.maxBombs);
            st.setInt(5, this.triggered ? 1 : 0);
            st.setInt(6, this.kills);
            st.setInt(7, this.deaths);
            st.setInt(8, this.userId);
            if (this.dbId != 0) {
                st.setInt(9, this.dbId);
            }
            int affectedRows = st.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Cannot save character");
            } else {
                ResultSet rs = st.getGeneratedKeys();
                if (rs.next() && this.dbId == 0) {
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
