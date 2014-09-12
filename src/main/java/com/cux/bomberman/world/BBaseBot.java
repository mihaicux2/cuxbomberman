/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.cux.bomberman.world;

import com.cux.bomberman.BombermanWSEndpoint;
//import static com.cux.bomberman.BombermanWSEndpoint.bombExists;
//import static com.cux.bomberman.BombermanWSEndpoint.bombsChanged;
//import static com.cux.bomberman.BombermanWSEndpoint.charsChanged;
//import static com.cux.bomberman.BombermanWSEndpoint.map;
//import static com.cux.bomberman.BombermanWSEndpoint.peers;
import com.cux.bomberman.util.BLogger;
import es.usc.citius.hipster.algorithm.Hipster;
import es.usc.citius.hipster.model.problem.SearchProblem;
import es.usc.citius.hipster.util.graph.GraphBuilder;
import es.usc.citius.hipster.util.graph.GraphSearchProblem;
import es.usc.citius.hipster.util.graph.HipsterDirectedGraph;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.Random;
import javax.websocket.EndpointConfig;
import javax.websocket.Session;

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
        this.markedBlock = new boolean[World.getWidth()/World.wallDim][World.getHeight()/World.wallDim];
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
        if (!this.isWalking() && !BombermanWSEndpoint.getInstance().map.get(this.roomIndex).HasMapCollision(this)) {
            super.moveUp();
        }
        BombermanWSEndpoint.getInstance().charsChanged.put(this.roomIndex, true);
    }
    
    @Override
    public void moveLeft(){
        this.setDirection("Left");
        if (!this.isWalking() && !BombermanWSEndpoint.getInstance().map.get(this.roomIndex).HasMapCollision(this)) {
            super.moveLeft();
        }
        BombermanWSEndpoint.getInstance().charsChanged.put(this.roomIndex, true);
    }
    
    @Override
    public void moveDown(){
        this.setDirection("Down");
        if (!this.isWalking() && !BombermanWSEndpoint.getInstance().map.get(this.roomIndex).HasMapCollision(this)) {
            super.moveDown();
        }
        BombermanWSEndpoint.getInstance().charsChanged.put(this.roomIndex, true);
    }
    
    @Override
    public void moveRight(){
        this.setDirection("Right");
        if (!this.isWalking() && !BombermanWSEndpoint.getInstance().map.get(this.roomIndex).HasMapCollision(this)) {
            super.moveRight();
        }
        BombermanWSEndpoint.getInstance().charsChanged.put(this.roomIndex, true);
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
            BombermanWSEndpoint.getInstance().bombs.get(this.roomIndex).add(b);
            BombermanWSEndpoint.getInstance().map.get(this.roomIndex).blockMatrix[b.getPosX() / World.wallDim][b.getPosY() / World.wallDim] = b;
            this.incPlantedBombs();
            this.avoidBomb("left", this.posX /  World.wallDim, this.posY / World.wallDim);
        } else if (!isAllowed) {
            this.addOrDropBomb();
        }
        BombermanWSEndpoint.getInstance().charsChanged.put(this.roomIndex, true);
        BombermanWSEndpoint.getInstance().bombsChanged.put(this.roomIndex, true);
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
    
}
