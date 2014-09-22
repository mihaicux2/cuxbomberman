/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.cux.bomberman.world;

import com.cux.bomberman.BombermanWSEndpoint;
import static com.cux.bomberman.BombermanWSEndpoint.map;
import com.cux.bomberman.world.items.AbstractItem;
import com.cux.bomberman.world.walls.AbstractWall;
import java.util.Random;
import javax.websocket.EndpointConfig;
//import javax.websocket.Session;

/**
 *
 * @author mihaicux
 */
public abstract class BBaseBot extends BCharacter implements Runnable, BBaseBotI{
    
    protected boolean markedBlock[][];    
    protected boolean running = false;
    protected int searchRange = 3;
    
    public BBaseBot(String id, String name, int roomIndex, EndpointConfig config) {
        super(id, name, roomIndex, config);
        this.markedBlock = new boolean[map.get(this.roomIndex).getWidth()/World.wallDim][map.get(this.roomIndex).getHeight()/World.wallDim];
        this.running = true;
    }
    
    public void setRunning(boolean running){
        this.running = running;
    }
    
    public boolean getRunning(){
        return this.running;
    }
    
    @Override
    public void moveUp(){
        this.setDirection("Up");
        if (!this.isWalking() && !BombermanWSEndpoint.map.get(this.roomIndex).HasMapCollision(this)) {
            super.moveUp();
        }
        BombermanWSEndpoint.charsChanged.put(this.roomIndex, true);
    }
    
    @Override
    public void moveLeft(){
        this.setDirection("Left");
        if (!this.isWalking() && !BombermanWSEndpoint.map.get(this.roomIndex).HasMapCollision(this)) {
            super.moveLeft();
        }
        BombermanWSEndpoint.charsChanged.put(this.roomIndex, true);
    }
    
    @Override
    public void moveDown(){
        this.setDirection("Down");
        if (!this.isWalking() && !BombermanWSEndpoint.map.get(this.roomIndex).HasMapCollision(this)) {
            super.moveDown();
        }
        BombermanWSEndpoint.charsChanged.put(this.roomIndex, true);
    }
    
    @Override
    public void moveRight(){
        this.setDirection("Right");
        if (!this.isWalking() && !BombermanWSEndpoint.map.get(this.roomIndex).HasMapCollision(this)) {
            super.moveRight();
        }
        BombermanWSEndpoint.charsChanged.put(this.roomIndex, true);
    }
    
    public void dropBomb(){
        this.addOrDropBomb(); // change character state
        boolean isAllowed = this.getPlantedBombs() < this.getMaxBombs();
        //boolean isAllowed = true;
//        System.out.println("BBaseBot.drop : trying to plant bomb..." + (isAllowed ? "can do" : "can't do"));
//        System.out.println("BBaseBot.drop : pot pune "+this.getMaxBombs()+" bombe, am pus "+this.getPlantedBombs());
        if (isAllowed && this.getState().equals("Normal")) { // if he dropped the bomb, add the bomb to the screen
            final BBomb b = new BBomb(this);
//            if (BombermanWSEndpoint.bombExists(BombermanWSEndpoint.map.get(this.roomIndex).blockMatrix, b.getPosX() / World.wallDim, b.getPosY() / World.wallDim)) {
//                return;
//            }
            BombermanWSEndpoint.bombs.get(this.roomIndex).add(b);
            BombermanWSEndpoint.map.get(this.roomIndex).blockMatrix[b.getPosX() / World.wallDim][b.getPosY() / World.wallDim] = b;
            this.incPlantedBombs();
            this.avoidBomb("left", this.posX /  World.wallDim, this.posY / World.wallDim);
        } else if (!isAllowed) {
            this.addOrDropBomb();
        }
        BombermanWSEndpoint.charsChanged.put(this.roomIndex, true);
        BombermanWSEndpoint.bombsChanged.put(this.roomIndex, true);
    }
    
    @Override
    public int saveToDB(){
         return 1;
    }
    
    @Override
    public int logIn(){
        return 1;
    }
    
    @Override
    public void restoreFromDB(){
        
    }
    
    @Override
    public void avoidBomb(String bombLocation, int x, int y){
//        System.out.println("bomb detected "+bombLocation);
        boolean move = true;
        switch(bombLocation){
            case "up":
                if (x > 0 && !this.markedBlock[x-1][y]){ // try to go left
                    String checkLeft = BombermanWSEndpoint.checkWorldMatrix(this.roomIndex, x-1, y);
                    if (checkLeft.equals("char") || checkLeft.equals("empty")){
                        this.moveLeft();
                        move = false;
                        this.markedBlock[x-1][y] = true;
                    }
                }
                else if (x > 0){
                    this.markedBlock[x-1][y] = false;
                }
                
                if (move && x+1 < map.get(this.roomIndex).getWidth() / World.wallDim && !this.markedBlock[x+1][y]){ // try to go right
                    String checkRight = BombermanWSEndpoint.checkWorldMatrix(this.roomIndex, x+1, y);
                    if (checkRight.equals("char") || checkRight.equals("empty")){
                        this.moveRight();
                        move = false;
                        this.markedBlock[x+1][y] = true;
                    }
                }
                else if (x+1 < map.get(this.roomIndex).getWidth() / World.wallDim){
                    this.markedBlock[x+1][y] = false;
                }
                
                if (move && y+1 < map.get(this.roomIndex).getHeight() / World.wallDim && !this.markedBlock[x][y+1]){ // try to go down
                    String checkDown = BombermanWSEndpoint.checkWorldMatrix(this.roomIndex, x, y+1);
                    if (checkDown.equals("char") || checkDown.equals("empty")){
                        this.moveDown();
                        this.markedBlock[x][y+1] = true;
                    }
                }
                else if (y+1 < map.get(this.roomIndex).getHeight() / World.wallDim){
                    this.markedBlock[x][y+1] = false;
                }
                break;
            case "down":
                if (x > 0 && !this.markedBlock[x-1][y]){ // try to go left
                    String checkLeft = BombermanWSEndpoint.checkWorldMatrix(this.roomIndex, x-1, y);
                    if (checkLeft.equals("char") || checkLeft.equals("empty")){
                        this.moveLeft();
                        move = false;
                        this.markedBlock[x-1][y] = true;
                    }
                }
                else if (x > 0){
                    this.markedBlock[x-1][y] = false;
                }
                
                if (move && x+1 < map.get(this.roomIndex).getWidth() / World.wallDim && !this.markedBlock[x+1][y]){ // try to go right
                    String checkRight = BombermanWSEndpoint.checkWorldMatrix(this.roomIndex, x+1, y);
                    if (checkRight.equals("char") || checkRight.equals("empty")){
                        this.moveRight();
                        move = false;
                        this.markedBlock[x+1][y] = true;
                    }
                }
                else if (x+1 < map.get(this.roomIndex).getWidth() / World.wallDim){
                    this.markedBlock[x+1][y] = false;
                }
                
                if (move && y > 0 && !this.markedBlock[x][y-1]){ // try to go up
                    String checkUp = BombermanWSEndpoint.checkWorldMatrix(this.roomIndex, x, y-1);
                    if (checkUp.equals("char") || checkUp.equals("empty")){
                        this.moveUp();
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
                        this.moveUp();
                        move = false;
                        this.markedBlock[x][y-1] = true;
                    }
                }
                else if (y > 0){
                    this.markedBlock[x][y-1] = true;
                }
                
                if (move && y+1 < map.get(this.roomIndex).getHeight() / World.wallDim && !this.markedBlock[x][y+1]){ // try to go down
                    String checkDown = BombermanWSEndpoint.checkWorldMatrix(this.roomIndex, x, y+1);
                    if (checkDown.equals("char") || checkDown.equals("empty")){
                        this.moveDown();
                        move = false;
                        this.markedBlock[x][y+1] = true;
                    }
                }
                else if (y+1 < map.get(this.roomIndex).getHeight() / World.wallDim){
                    this.markedBlock[x][y+1] = false;
                }
                
                if (move && x+1 < map.get(this.roomIndex).getWidth() / World.wallDim && !this.markedBlock[x+1][y]){ // try to go right
                    String checkRight = BombermanWSEndpoint.checkWorldMatrix(this.roomIndex, x+1, y);
                    if (checkRight.equals("char") || checkRight.equals("empty")){
                        this.moveRight();
                        this.markedBlock[x+1][y] = true;
                    }
                }
                else if (x+1 < map.get(this.roomIndex).getWidth() / World.wallDim){
                    this.markedBlock[x+1][y] = false;
                }
                break;
            case "right":
                if (move && y > 0 && !this.markedBlock[x][y-1]){ // try to go up
                    String checkUp = BombermanWSEndpoint.checkWorldMatrix(this.roomIndex, x, y-1);
                    if (checkUp.equals("char") || checkUp.equals("empty")){
                        this.moveUp();
                        move = false;
                        this.markedBlock[x][y-1] = true;
                    }
                }
                else if (y > 0){
                    this.markedBlock[x][y-1] = true;
                }
                
                if (move && y+1 < map.get(this.roomIndex).getHeight() / World.wallDim && !this.markedBlock[x][y+1]){ // try to go down
                    String checkDown = BombermanWSEndpoint.checkWorldMatrix(this.roomIndex, x, y+1);
                    if (checkDown.equals("char") || checkDown.equals("empty")){
                        this.moveDown();
                        this.markedBlock[x][y+1] = true;
                    }
                }
                else if (y+1 < map.get(this.roomIndex).getHeight() / World.wallDim){
                    this.markedBlock[x][y+1] = false;
                }
                
                if (x > 0 && !this.markedBlock[x-1][y]){ // try to go left
                    String checkLeft = BombermanWSEndpoint.checkWorldMatrix(this.roomIndex, x-1, y);
                    if (checkLeft.equals("char") || checkLeft.equals("empty")){
                        this.moveLeft();
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
        int x = this.posX / World.wallDim;
        int y = this.posY / World.wallDim;
        
        // if you sit on a bomb, ruuuunn!!!
        if (BombermanWSEndpoint.bombExists(BombermanWSEndpoint.map.get(this.roomIndex).blockMatrix, x, y)){
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
    
    public String nearbyBombs(int x, int y){
        
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
                if (BombermanWSEndpoint.bombExists(BombermanWSEndpoint.map.get(this.roomIndex).blockMatrix, xLeft, y)){
                    expandLeft = false;
                    // if the bomb range raches the bot, must avoid explosion
                    if (BombermanWSEndpoint.getInstance().bombReaches((BBomb) BombermanWSEndpoint.map.get(this.roomIndex).blockMatrix[xLeft][y], x - xLeft)) {
                        return "left";
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
            if (expandRight && xRight < map.get(this.roomIndex).getWidth() / World.wallDim ){
                String checkRight = BombermanWSEndpoint.checkWorldMatrix(this.roomIndex, xRight, y);
                if (BombermanWSEndpoint.bombExists(BombermanWSEndpoint.map.get(this.roomIndex).blockMatrix, xRight, y)){
                    expandRight = false;
                    // if the bomb range raches the bot, must avoid explosion
                    if (BombermanWSEndpoint.getInstance().bombReaches((BBomb)BombermanWSEndpoint.map.get(this.roomIndex).blockMatrix[xRight][y], xRight - x)){
                        return "right";
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
                if (BombermanWSEndpoint.bombExists(BombermanWSEndpoint.map.get(this.roomIndex).blockMatrix, x, yUp)){
                    expandUp = false;
                    // if the bomb range raches the bot, must avoid explosion
                    if (BombermanWSEndpoint.getInstance().bombReaches((BBomb)BombermanWSEndpoint.map.get(this.roomIndex).blockMatrix[x][yUp], y - yUp)){
                        return "up";
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
            if (expandDown && yDown < map.get(this.roomIndex).getHeight() / World.wallDim ){
                String checkDown = BombermanWSEndpoint.checkWorldMatrix(this.roomIndex, x, yDown);
                if (BombermanWSEndpoint.bombExists(BombermanWSEndpoint.map.get(this.roomIndex).blockMatrix, x, yDown)){
                    expandDown = false;
                    // if the bomb range raches the bot, must avoid explosion
                    if (BombermanWSEndpoint.getInstance().bombReaches((BBomb) BombermanWSEndpoint.map.get(this.roomIndex).blockMatrix[x][yDown], yDown - y)) {
                        return "down";
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
        
        return "";
    }
    
    /**
     * This method checks if a bomb is capable of killing the BOT.<br />
     * It also tries to avoid the bomb's explosion if TRUE should be returned
     * @return True if the BOT can be killed by an existing bomb
     */
    public boolean IHaveBombsNearby(){
        
        // current bot position
        int x = this.posX / World.wallDim;
        int y = this.posY / World.wallDim;
        
        String direction = this.nearbyBombs(x, y);
        if (!direction.equals("")){
            this.avoidBomb(direction, x, y);
            return true;
        }
        
        return false;
        
    }
    
    /**
     * This method checks if the given neighbour is the best next move.
     * It also goes to the given neighbour if TRUE should be returned
     * @param x - the neighbour X coordinate
     * @param y - the neighbour Y coordinate
     * @param direction - the direction twoards the neighbour
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
     * This method checks if any of the existing neighbours can be choosen as the next best move
     * @return TRUE if such a neighbour is found
     */
    public boolean CheckNeighbours(){
        
        // current bot position
        int x = this.posX / World.wallDim;
        int y = this.posY / World.wallDim;
        
        boolean ret = false;
        
        if (x > 0){ // check left
            ret = checkNextBlock(x-1, y, "left");
        }

        if (x < map.get(this.roomIndex).getWidth() / World.wallDim){ // check right
            ret = checkNextBlock(x+1, y, "right");
        }

        if (y > 0){ // check up
            ret = checkNextBlock(x, y-1, "up");
        }

        if (y < map.get(this.roomIndex).getHeight() / World.wallDim){ // check down
            ret = checkNextBlock(x, y+1, "down");
        }
            
        return ret;
    }
    
}
