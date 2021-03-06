package com.cux.bomberman.world;

import com.cux.bomberman.BombermanWSEndpoint;
import com.cux.bomberman.world.items.AbstractItem;
import com.cux.bomberman.world.walls.AbstractWall;
import java.util.AbstractMap;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.websocket.EndpointConfig;

/**
 * This class is used as a base wrapper for the game bots.
 * 
 * @version 1.0
 * @author  Mihail Cuculici (mihai.cuculici@gmail.com)
 * @author  http://www.
 */
public abstract class BBaseBot extends BCharacter implements Runnable, BBaseBotI{
    
    /**
     * Matrix to store the blocks already followed
     */
    protected boolean markedBlock[][];    
    
    /**
     * Boolean telling if the bot is in Search&Destroy directive
     */
    protected boolean running = false;
    
    /**
     * The default range for best move search
     */
    protected int searchRange = 3;
    
    /**
     * The default rate for the Search&Destroy directive
     */
    protected int FPS = 5;
    
    /**
     * Public constructor used for every bot
     * @param id The BOT's id
     * @param name The BOT's name
     * @param roomIndex The room in which it will be added
     * @param config The server endpoint configuration object
     */
    public BBaseBot(String id, String name, int roomIndex, EndpointConfig config) {
        super(id, name, roomIndex, config);
        this.markedBlock = new boolean[BombermanWSEndpoint.map.get(this.roomIndex).getWidth()/World.wallDim][BombermanWSEndpoint.map.get(this.roomIndex).getHeight()/World.wallDim];
        this.running = true;
    }
    
    /**
     * Public setter for the running property
     * @param running The new value
     */
    public void setRunning(boolean running){
        this.running = running;
    }
    
    /**
     * Public getter for the running property
     * @return The value of the running property
     */
    public boolean getRunning(){
        return this.running;
    }
    
    public String canMove(){
        
        int x = this.getBlockPosX();
        int y = this.getBlockPosY();
        
        // check to see if we can move to left
        if (x > 0){
            String check = BombermanWSEndpoint.checkWorldMatrix(this.roomIndex, x-1, y);
            if (check.equals("empty") || check.equals("char") || check.equals("item")){
                return "left";
            }
        }
        
        // check to see if we can move to right
        if (x < BombermanWSEndpoint.map.get(this.roomIndex).getWidth() / World.wallDim - 2){
            String check = BombermanWSEndpoint.checkWorldMatrix(this.roomIndex, x+1, y);
            if (check.equals("empty") || check.equals("char") || check.equals("item")){
                return "right";
            }
        }
        
        // check to see if we can move up
        if (y > 0){
            String check = BombermanWSEndpoint.checkWorldMatrix(this.roomIndex, x, y-1);
            if (check.equals("empty") || check.equals("char") || check.equals("item")){
                return "up";
            }
        }
        
        // check to see if we can move down
        if (y < BombermanWSEndpoint.map.get(this.roomIndex).getHeight() / World.wallDim - 2){
            String check = BombermanWSEndpoint.checkWorldMatrix(this.roomIndex, x, y+1);
            if (check.equals("empty") || check.equals("char") || check.equals("item")){
                return "right";
            }
        }
        
        return "";
    }
    
    /**
     * Public method used by the bot to drop a bomb
     */
    public void dropBomb(){
        this.addOrDropBomb(); // change character state
        boolean isAllowed = this.getPlantedBombs() < this.getMaxBombs();
        if (isAllowed && this.getState().equals("Normal")) { // if he can drop bombs, add the bomb to the screen
            String move = this.canMove(); // 
            if (move.equals("")) return;
            final BBomb b = new BBomb(this);
            if (BombermanWSEndpoint.map.get(this.roomIndex).bombExists(b.getBlockPosX(), b.getBlockPosY())) {
                return;
            }
            BombermanWSEndpoint.bombs.get(this.roomIndex).put(b.objId,b);
            BombermanWSEndpoint.map.get(this.roomIndex).blockMatrix[b.getBlockPosX()][b.getBlockPosY()] = b;
            this.incPlantedBombs();
            this.move(move);
        } else if (!isAllowed) {
            this.addOrDropBomb();
        }
        BombermanWSEndpoint.charsChanged.put(this.roomIndex, true);
        BombermanWSEndpoint.bombsChanged.put(this.roomIndex, true);
    }
    
    /**
     * Public method overridden to avoid storing bot information in the database
     * @return 1
     */
    @Override
    public int saveToDB(){
         return 1;
    }
    
    /**
     * Public method overridden to avoid getting bot information from the database
     * @return 1
     */
    @Override
    public int storeLogIn(){
        return 1;
    }
    
    /**
     * Public method overridden to avoid getting bot information from the database.
     */
    @Override
    public void restoreFromDB(){}
    
    /**
     * Public method used by the bots to try to avoid an explosion
     * @param bombLocation String telling the bot where is the bomb placed
     * @param x The X coordinate of bot
     * @param y The Y coordinate of the bot
     */
    @Override
    public void avoidBomb(String bombLocation, int x, int y){
        boolean move = true;
        switch(bombLocation){
            case "up":
                if (x > 0 && !this.markedBlock[x-1][y]){ // try to go left
                    String checkLeft = BombermanWSEndpoint.checkWorldMatrix(this.roomIndex, x-1, y);
                    if (checkLeft.equals("char") || checkLeft.equals("empty")){
                        this.move("left");
                        move = false;
                        this.markedBlock[x-1][y] = true;
                    }
                }
                else if (x > 0){
                    this.markedBlock[x-1][y] = false;
                }
                
                if (move && x+1 < BombermanWSEndpoint.map.get(this.roomIndex).getWidth() / World.wallDim && !this.markedBlock[x+1][y]){ // try to go right
                    String checkRight = BombermanWSEndpoint.checkWorldMatrix(this.roomIndex, x+1, y);
                    if (checkRight.equals("char") || checkRight.equals("empty")){
                        this.move("right");
                        move = false;
                        this.markedBlock[x+1][y] = true;
                    }
                }
                else if (x+1 < BombermanWSEndpoint.map.get(this.roomIndex).getWidth() / World.wallDim){
                    this.markedBlock[x+1][y] = false;
                }
                
                if (move && y+1 < BombermanWSEndpoint.map.get(this.roomIndex).getHeight() / World.wallDim && !this.markedBlock[x][y+1]){ // try to go down
                    String checkDown = BombermanWSEndpoint.checkWorldMatrix(this.roomIndex, x, y+1);
                    if (checkDown.equals("char") || checkDown.equals("empty")){
                        this.move("down");
                        this.markedBlock[x][y+1] = true;
                    }
                }
                else if (y+1 < BombermanWSEndpoint.map.get(this.roomIndex).getHeight() / World.wallDim){
                    this.markedBlock[x][y+1] = false;
                }
                break;
            case "down":
                if (x > 0 && !this.markedBlock[x-1][y]){ // try to go left
                    String checkLeft = BombermanWSEndpoint.checkWorldMatrix(this.roomIndex, x-1, y);
                    if (checkLeft.equals("char") || checkLeft.equals("empty")){
                        this.move("left");
                        move = false;
                        this.markedBlock[x-1][y] = true;
                    }
                }
                else if (x > 0){
                    this.markedBlock[x-1][y] = false;
                }
                
                if (move && x+1 < BombermanWSEndpoint.map.get(this.roomIndex).getWidth() / World.wallDim && !this.markedBlock[x+1][y]){ // try to go right
                    String checkRight = BombermanWSEndpoint.checkWorldMatrix(this.roomIndex, x+1, y);
                    if (checkRight.equals("char") || checkRight.equals("empty")){
                        this.move("right");
                        move = false;
                        this.markedBlock[x+1][y] = true;
                    }
                }
                else if (x+1 < BombermanWSEndpoint.map.get(this.roomIndex).getWidth() / World.wallDim){
                    this.markedBlock[x+1][y] = false;
                }
                
                if (move && y > 0 && !this.markedBlock[x][y-1]){ // try to go up
                    String checkUp = BombermanWSEndpoint.checkWorldMatrix(this.roomIndex, x, y-1);
                    if (checkUp.equals("char") || checkUp.equals("empty")){
                        this.move("up");
                        this.markedBlock[x][y-1] = true;
                    }
                }
                else if (y > 0){
                    this.markedBlock[x][y-1] = true;
                }
                break;
            case "left":
                if (move && y > 0 && !this.markedBlock[x][y-1]){ // try to go up
                    String checkUp = BombermanWSEndpoint.checkWorldMatrix(this.roomIndex, x, y-1);
                    if (checkUp.equals("char") || checkUp.equals("empty")){
                        this.move("up");
                        move = false;
                        this.markedBlock[x][y-1] = true;
                    }
                }
                else if (y > 0){
                    this.markedBlock[x][y-1] = true;
                }
                
                if (move && y+1 < BombermanWSEndpoint.map.get(this.roomIndex).getHeight() / World.wallDim && !this.markedBlock[x][y+1]){ // try to go down
                    String checkDown = BombermanWSEndpoint.checkWorldMatrix(this.roomIndex, x, y+1);
                    if (checkDown.equals("char") || checkDown.equals("empty")){
                        this.move("down");
                        move = false;
                        this.markedBlock[x][y+1] = true;
                    }
                }
                else if (y+1 < BombermanWSEndpoint.map.get(this.roomIndex).getHeight() / World.wallDim){
                    this.markedBlock[x][y+1] = false;
                }
                
                if (move && x+1 < BombermanWSEndpoint.map.get(this.roomIndex).getWidth() / World.wallDim && !this.markedBlock[x+1][y]){ // try to go right
                    String checkRight = BombermanWSEndpoint.checkWorldMatrix(this.roomIndex, x+1, y);
                    if (checkRight.equals("char") || checkRight.equals("empty")){
                        this.move("right");
                        this.markedBlock[x+1][y] = true;
                    }
                }
                else if (x+1 < BombermanWSEndpoint.map.get(this.roomIndex).getWidth() / World.wallDim){
                    this.markedBlock[x+1][y] = false;
                }
                break;
            case "right":
                if (move && y > 0 && !this.markedBlock[x][y-1]){ // try to go up
                    String checkUp = BombermanWSEndpoint.checkWorldMatrix(this.roomIndex, x, y-1);
                    if (checkUp.equals("char") || checkUp.equals("empty")){
                        this.move("up");
                        move = false;
                        this.markedBlock[x][y-1] = true;
                    }
                }
                else if (y > 0){
                    this.markedBlock[x][y-1] = true;
                }
                
                if (move && y+1 < BombermanWSEndpoint.map.get(this.roomIndex).getHeight() / World.wallDim && !this.markedBlock[x][y+1]){ // try to go down
                    String checkDown = BombermanWSEndpoint.checkWorldMatrix(this.roomIndex, x, y+1);
                    if (checkDown.equals("char") || checkDown.equals("empty")){
                        this.move("down");
                        this.markedBlock[x][y+1] = true;
                    }
                }
                else if (y+1 < BombermanWSEndpoint.map.get(this.roomIndex).getHeight() / World.wallDim){
                    this.markedBlock[x][y+1] = false;
                }
                
                if (x > 0 && !this.markedBlock[x-1][y]){ // try to go left
                    String checkLeft = BombermanWSEndpoint.checkWorldMatrix(this.roomIndex, x-1, y);
                    if (checkLeft.equals("char") || checkLeft.equals("empty")){
                        this.move("left");
                        this.markedBlock[x-1][y] = true;
                    }
                }
                else if (x > 0){
                    this.markedBlock[x-1][y] = false;
                }
                break;
        }
    }
    
    /**
     * This method checks if a bomb is in the same position as the BOT.<br />
     * It also tries to avoid the bomb's explosion if TRUE should be returned
     * @return True if the BOT is in the same position as a bomb
     */
    public boolean ISitOnABomb(){
        // current bot position
        int x = this.getBlockPosX();
        int y = this.getBlockPosY();
        
        // if you sit on a bomb, ruuuunn!!!
        if (BombermanWSEndpoint.map.get(this.roomIndex).bombExists(x, y)){
            Random r = new Random();
            // random new position...
            int rand = r.nextInt(100000);
            if (rand % 4 == 0){
                avoidBomb("left", x, y);
            }
            else if (rand % 3 == 3){
                avoidBomb("down", x, y);
            }
            else if (rand % 2 == 3){
                avoidBomb("right", x, y);
            }
            else {
                avoidBomb("up", x, y);
            }
            return true;
        }
        
        return false;
    }
    
    /**
     * This method checks if there are bombs that could reach a given position
     * @param x The X coordinate of the given position
     * @param y The Y coordinate of the given position
     * @return A string representing the direction from the bomb reaches.<br />
     * An empty string if no such bomb is found.
     */
    public AbstractMap.SimpleEntry<BBomb, String> nearbyBombs(int x, int y){
        
        boolean expandUp=true,
                expandDown=true,
                expandLeft=true,
                expandRight=true,
                checkFurther = true;
        
        int xLeft = x,
            xRight = x,
            yUp = y,
            yDown = y;
        
        while (true){
            
            if (!expandLeft && !expandRight && !expandUp && !expandDown){
                break;
            }
            
            // check for bombs in the left
            if (expandLeft && xLeft > 0 ){
                String checkLeft = BombermanWSEndpoint.checkWorldMatrix(this.roomIndex, xLeft, y);
                if (BombermanWSEndpoint.map.get(this.roomIndex).bombExists(xLeft, y)){
                    expandLeft = false;
                    // if the bomb range raches the bot, must avoid explosion
                    BBomb bomb = (BBomb)BombermanWSEndpoint.map.get(this.roomIndex).blockMatrix[xLeft][y];
                    if (BombermanWSEndpoint.getInstance().bombReaches(bomb, x - xLeft)) {
                        return new AbstractMap.SimpleEntry(bomb, "left");
                    }
                }
                else if (checkLeft.equals("wall")){
                    expandLeft = false;
                }
                else{
                    xLeft--;
                }
            }
            else{
                expandLeft = false;
            }
            
            // check for bombs in the right
            if (expandRight && xRight < BombermanWSEndpoint.map.get(this.roomIndex).getWidth() / World.wallDim ){
                String checkRight = BombermanWSEndpoint.checkWorldMatrix(this.roomIndex, xRight, y);
                if (BombermanWSEndpoint.map.get(this.roomIndex).bombExists(xRight, y)){
                    expandRight = false;
                    // if the bomb range raches the bot, must avoid explosion
                    BBomb bomb = (BBomb)BombermanWSEndpoint.map.get(this.roomIndex).blockMatrix[xRight][y];
                    if (BombermanWSEndpoint.getInstance().bombReaches(bomb, xRight - x)){
                        return new AbstractMap.SimpleEntry(bomb, "right");
                    }
                }
                else if (checkRight.equals("wall")){
                    expandRight = false;
                }
                else{
                    xRight++;
                }
            }
            else{
                expandRight = false;
            }
            
            // check for bombs up
            if (expandUp && yUp > 0 ){
                String checkUp = BombermanWSEndpoint.checkWorldMatrix(this.roomIndex, x, yUp);
                if (BombermanWSEndpoint.map.get(this.roomIndex).bombExists(x, yUp)){
                    expandUp = false;
                    // if the bomb range raches the bot, must avoid explosion
                    BBomb bomb = (BBomb)BombermanWSEndpoint.map.get(this.roomIndex).blockMatrix[x][yUp];
                    if (BombermanWSEndpoint.getInstance().bombReaches(bomb, y - yUp)){
                        return new AbstractMap.SimpleEntry(bomb, "up");
                    }
                }
                else if (checkUp.equals("wall")){
                    expandUp = false;
                }
                else{
                    yUp--;
                }
            }
            else{
                expandUp = false;
            }
            
            // check for bombs down
            if (expandDown && yDown < BombermanWSEndpoint.map.get(this.roomIndex).getHeight() / World.wallDim ){
                String checkDown = BombermanWSEndpoint.checkWorldMatrix(this.roomIndex, x, yDown);
                if (BombermanWSEndpoint.map.get(this.roomIndex).bombExists(x, yDown)){
                    expandDown = false;
                    // if the bomb range raches the bot, must avoid explosion
                    BBomb bomb = (BBomb)BombermanWSEndpoint.map.get(this.roomIndex).blockMatrix[x][yDown];
                    if (BombermanWSEndpoint.getInstance().bombReaches(bomb, yDown - y)) {
                        return new AbstractMap.SimpleEntry(bomb, "down");
                    }
                }
                else if (checkDown.equals("wall")){
                    expandDown = false;
                }
                else{
                    yDown++;
                }
            }
            else{
                expandDown = false;
            }
        }
        
        return null;
    }
    
    /**
     * This method checks if a bomb is capable of killing the BOT.<br />
     * It also tries to avoid the bomb's explosion if TRUE should be returned
     * @return True if the BOT can be killed by an existing bomb
     */
    public boolean IHaveBombsNearby(){
        
        // current bot position
        int x = this.getBlockPosX();
        int y = this.getBlockPosY();
        
        AbstractMap.SimpleEntry<BBomb, String> nearby = this.nearbyBombs(x, y);
        
        if (nearby == null) return false;
        
        String _direction = nearby.getValue();
        if (!_direction.equals("")){
            this.avoidBomb(_direction, x, y);
            return true;
        }
        
        return false;
        
    }
    
    /**
     * This method checks if the given neighbour is the best next move.
     * It also goes to the given neighbour if TRUE should be returned
     * @param x the neighbour X coordinate
     * @param y the neighbour Y coordinate
     * @param direction the direction towards the neighbour
     * @return TRUE if the given neighbour is a character or a positive item 
     */
    public boolean checkNextBlock(int x, int y, String direction){
        
        if (BombermanWSEndpoint.characterExists(this.roomIndex, x, y)){
            dropBomb();
            return true;
        }
        
        AbstractBlock block = BombermanWSEndpoint.map.get(this.roomIndex).blockMatrix[x][y];
        String type = BombermanWSEndpoint.checkWorldMatrix(this.roomIndex, x, y);
        boolean ret = false;
        switch (type){
            case "item": // try to grab the item, if it is positive
                String itemType = ((AbstractItem) block).getName();
                switch (itemType) {
                    case "ebola":
                    case "slow":
                        break;
                    default: // positive item, try to grab it
                        ret = true;
                        move(direction);
                        break;
                }
                break;
            case "wall": // try to blow it, if it is blowable
                if (((AbstractWall)block).isBlowable()){
                    ret = true;
                    dropBomb();
                }
                break;
        }
        return ret;
    }
    
    /**
     * This method checks if any of the existing neighbours can be chosen as the next best move
     * @return TRUE if such a neighbour is found
     */
    public boolean CheckNeighbours(){
        
        // current bot position
        int x = this.getBlockPosX();
        int y = this.getBlockPosY();
        
        boolean ret = false;
        
        if (x > 0){ // check left
            ret = checkNextBlock(x-1, y, "left");
        }

        if (x < BombermanWSEndpoint.map.get(this.roomIndex).getWidth() / World.wallDim){ // check right
            ret = checkNextBlock(x+1, y, "right");
        }

        if (y > 0){ // check up
            ret = checkNextBlock(x, y-1, "up");
        }

        if (y < BombermanWSEndpoint.map.get(this.roomIndex).getHeight() / World.wallDim){ // check down
            ret = checkNextBlock(x, y+1, "down");
        }
            
        return ret;
    }
    
     /**
     * Public method used to loop the Search&Destroy directive
     */
    @Override
    public void run() {
        while (true){
            try {
                if (this.running && !this.walking) {
                    this.searchAndDestroy();
                }
                Thread.sleep(1000 / this.FPS);
            } catch (InterruptedException ex) {
                Logger.getLogger(BMediumBot.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    /**
     * Public method used to make the bot "sleep" (it's running thread will wait:> )
     * @param ms The time in milliseconds to "sleep"
     */
    public void sleep(int ms){
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ex) {
            Logger.getLogger(BMediumBot.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
