/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.cux.bomberman.world;

import com.cux.bomberman.BombermanWSEndpoint;
import static com.cux.bomberman.BombermanWSEndpoint.bombExists;
import static com.cux.bomberman.BombermanWSEndpoint.bombsChanged;
import static com.cux.bomberman.BombermanWSEndpoint.charsChanged;
import static com.cux.bomberman.BombermanWSEndpoint.map;
import static com.cux.bomberman.BombermanWSEndpoint.peers;
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
    
    public BBaseBot(String id, String name, int roomIndex, EndpointConfig config) {
        super(id, name, roomIndex, config);
        this.markedBlock = new boolean[World.getWidth()/World.wallDim][World.getHeight()/World.wallDim];
    }
    
    @Override
    public void moveUp(){
        this.setDirection("Up");
        if (!this.isWalking() && !map.get(this.roomIndex).HasMapCollision(this)) {
            super.moveUp();
        }
        BombermanWSEndpoint.charsChanged.put(this.roomIndex, true);
    }
    
    @Override
    public void moveLeft(){
        this.setDirection("Left");
        if (!this.isWalking() && !map.get(this.roomIndex).HasMapCollision(this)) {
            super.moveLeft();
        }
        BombermanWSEndpoint.charsChanged.put(this.roomIndex, true);
    }
    
    @Override
    public void moveDown(){
        this.setDirection("Down");
        if (!this.isWalking() && !map.get(this.roomIndex).HasMapCollision(this)) {
            super.moveDown();
        }
        BombermanWSEndpoint.charsChanged.put(this.roomIndex, true);
    }
    
    @Override
    public void moveRight(){
        this.setDirection("Right");
        if (!this.isWalking() && !map.get(this.roomIndex).HasMapCollision(this)) {
            super.moveRight();
        }
        BombermanWSEndpoint.charsChanged.put(this.roomIndex, true);
    }
    
    public void dropBomb(){
        this.addOrDropBomb(); // change character state
        boolean isAllowed = this.getPlantedBombs() < this.getMaxBombs();
        if (isAllowed && this.getState().equals("Normal")) { // if he dropped the bomb, add the bomb to the screen
            final BBomb b = new BBomb(this);
            if (BombermanWSEndpoint.bombExists(BombermanWSEndpoint.map.get(this.roomIndex).blockMatrix, b.getPosX() / World.wallDim, b.getPosY() / World.wallDim)) {
                return;
            }
            BombermanWSEndpoint.bombs.get(this.roomIndex).add(b);
            map.get(this.roomIndex).blockMatrix[b.getPosX() / World.wallDim][b.getPosY() / World.wallDim] = b;
            this.incPlantedBombs();
        } else if (!isAllowed) {
            this.addOrDropBomb();
        }
        BombermanWSEndpoint.charsChanged.put(this.roomIndex, true);
        BombermanWSEndpoint.bombsChanged.put(this.roomIndex, true);
    }
    
}
