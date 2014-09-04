/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.cux.bomberman.world;

import com.cux.bomberman.BombermanWSEndpoint;
import com.cux.bomberman.util.BLogger;
import java.util.Random;
import javax.websocket.EndpointConfig;

/**
 * @author mihaicux
 */
public class BMediumBot extends BBaseBot{

    private static final String[] messages = {"Bear is love. Bear is life",
                                              "Bad luck Brian",
                                              "Scumbag Stacy",
                                              "Goog guy Greg",
                                              "Dr. Evil Air Quotes",
                                              "Doge",
                                              "Confession Kid",
                                              "That's the Evilest Thing I Can Imagine",
                                              "Bitches Be Like",
                                              "Surprised Patrick",
                                              "Fuck Logic",
                                              "Me Gusta"};
    
    public BMediumBot(String id, String name, int roomIndex, EndpointConfig config) {
        super(id, name, roomIndex, config);
    }
    
    @Override
    public void searchAndDestroy(){
        
        //System.out.println("search & destroy");
        
        // random bomb add...
        Random r = new Random();
        int rand = r.nextInt(100000);
        if (rand % 10 == 0){
            this.dropBomb();
        }
        
        if (rand % 666 == 0){
            BombermanWSEndpoint.getInstance().sendMessageAll(this.roomIndex, "<b>" + this.getName() + " : </b>" + messages[rand % messages.length]);
        }
        
        int x = this.posX / World.wallDim;
        int y = this.posY / World.wallDim;
        
        int xLeft, xRight, yUp, yDown;
        xLeft  = x;
        xRight = x;
        yUp    = y;
        yDown  = y;
        boolean expandUp, expandDown, expandLeft, expandRight, checkFurther;
        expandUp = expandDown = expandLeft = expandRight = checkFurther = true;
        // check and try to avoid bombs
        while (checkFurther){
            
            if (!expandLeft && !expandRight && !expandUp && !expandDown){
                checkFurther = false;
                break;
            }
            
            // check for bombs in the left
            if (expandLeft && xLeft > 0 ){
                String checkLeft = BombermanWSEndpoint.checkWorldMatrix(this.roomIndex, xLeft, y);
                switch(checkLeft){
                    case "bomb":
                        // must avoid explosion...
                        this.avoidBomb("left", x, y);
                        checkFurther = false;
                        expandLeft = false;
                        break;
                    case "wall":
                        expandLeft = false;
                        break;
                    default:
                        xLeft--;
                }
            }
            else{
                expandLeft = false;
            }
            
            // check for bombs in the right
            if (checkFurther && expandRight && xRight < World.getWidth() / World.wallDim ){
                String checkRight = BombermanWSEndpoint.checkWorldMatrix(this.roomIndex, xRight, y);
                switch(checkRight){
                    case "bomb":
                        // must avoid explosion...
                        this.avoidBomb("right", x, y);
                        checkFurther = false;
                        expandRight = false;
                        break;
                    case "wall":
                        expandRight = false;
                        break;
                    default:
                        xRight++;
                }
            }
            else{
                expandRight = false;
            }
            
            // check for bombs up
            if (checkFurther && expandUp && yUp > 0 ){
                String checkUp = BombermanWSEndpoint.checkWorldMatrix(this.roomIndex, x, yUp);
                switch(checkUp){
                    case "bomb":
                        // must avoid explosion...
                        this.avoidBomb("up", x, y);
                        checkFurther = false;
                        expandUp = false;
                        break;
                    case "wall":
                        expandUp = false;
                        break;
                    default:
                        yUp--;
                }
            }
            else{
                expandUp = false;
            }
            
            // check for bombs down
            if (checkFurther && expandDown && yDown < World.getHeight() / World.wallDim ){
                String checkDown = BombermanWSEndpoint.checkWorldMatrix(this.roomIndex, x, yDown);
                switch(checkDown){
                    case "bomb":
                        // must avoid explosion...
                        this.avoidBomb("down", x, y);
                        checkFurther = false;
                        expandDown = false;
                        break;
                    case "wall":
                        expandDown = false;
                        break;
                    default:
                        yDown++;
                }
            }
            else{
                expandDown = false;
            }
        }
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
                
                if (move && x+1 < World.getWidth() / World.wallDim && !this.markedBlock[x+1][y]){ // try to go right
                    String checkRight = BombermanWSEndpoint.checkWorldMatrix(this.roomIndex, x+1, y);
                    if (checkRight.equals("char") || checkRight.equals("empty")){
                        this.moveRight();
                        move = false;
                        this.markedBlock[x+1][y] = true;
                    }
                }
                else if (x+1 < World.getWidth() / World.wallDim){
                    this.markedBlock[x+1][y] = false;
                }
                
                if (move && y+1 < World.getHeight() / World.wallDim && !this.markedBlock[x][y+1]){ // try to go down
                    String checkDown = BombermanWSEndpoint.checkWorldMatrix(this.roomIndex, x, y+1);
                    if (checkDown.equals("char") || checkDown.equals("empty")){
                        this.moveDown();
                        move = false;
                        this.markedBlock[x][y+1] = true;
                    }
                }
                else if (y+1 < World.getHeight() / World.wallDim){
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
                
                if (move && x+1 < World.getWidth() / World.wallDim && !this.markedBlock[x+1][y]){ // try to go right
                    String checkRight = BombermanWSEndpoint.checkWorldMatrix(this.roomIndex, x+1, y);
                    if (checkRight.equals("char") || checkRight.equals("empty")){
                        this.moveRight();
                        move = false;
                        this.markedBlock[x+1][y] = true;
                    }
                }
                else if (x+1 < World.getWidth() / World.wallDim){
                    this.markedBlock[x+1][y] = false;
                }
                
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
                
                if (move && y+1 < World.getHeight() / World.wallDim && !this.markedBlock[x][y+1]){ // try to go down
                    String checkDown = BombermanWSEndpoint.checkWorldMatrix(this.roomIndex, x, y+1);
                    if (checkDown.equals("char") || checkDown.equals("empty")){
                        this.moveDown();
                        move = false;
                        this.markedBlock[x][y+1] = true;
                    }
                }
                else if (y+1 < World.getHeight() / World.wallDim){
                    this.markedBlock[x][y+1] = false;
                }
                
                if (move && x+1 < World.getWidth() / World.wallDim && !this.markedBlock[x+1][y]){ // try to go right
                    String checkRight = BombermanWSEndpoint.checkWorldMatrix(this.roomIndex, x+1, y);
                    if (checkRight.equals("char") || checkRight.equals("empty")){
                        this.moveRight();
                        move = false;
                        this.markedBlock[x+1][y] = true;
                    }
                }
                else if (x+1 < World.getWidth() / World.wallDim){
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
                
                if (move && y+1 < World.getHeight() / World.wallDim && !this.markedBlock[x][y+1]){ // try to go down
                    String checkDown = BombermanWSEndpoint.checkWorldMatrix(this.roomIndex, x, y+1);
                    if (checkDown.equals("char") || checkDown.equals("empty")){
                        this.moveDown();
                        move = false;
                        this.markedBlock[x][y+1] = true;
                    }
                }
                else if (y+1 < World.getHeight() / World.wallDim){
                    this.markedBlock[x][y+1] = false;
                }
                
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
                break;
        }
    }
    
    @Override
    public void run() {
        while (this.running) {
            try {
                this.searchAndDestroy();
                Thread.sleep(100); // limit bot action to 10 FPS
            } catch (InterruptedException ex) {
                BLogger.getInstance().logException2(ex);
            }
        }
    }
    
}
