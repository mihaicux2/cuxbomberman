/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.cux.bomberman.world;

import com.cux.bomberman.util.BLogger;
import java.util.Random;
import javax.websocket.EndpointConfig;

/**
 * It simply works based on total randomness
 * @author mihaicux
 */
public class BDumbBot extends BBaseBot{

    public BDumbBot(String id, String name, int roomIndex, EndpointConfig config) {
        super(id, name, roomIndex, config);
    }
    
    @Override
    public void searchAndDestroy(){
        Random r = new Random();
        // random moves
        int rand = r.nextInt(1000000);
        if (rand % 5 == 0) {
            this.moveUp();
        } else if (rand % 4 == 0) {
            this.moveLeft();
        } else if (rand % 3 == 0) {
            this.moveDown();
        } else if (rand % 2 == 0) {
            this.moveRight();
        } 
        
        // a new bomb, maby? :D
        rand = r.nextInt(100);
        if (rand % 5 == 0){
            this.dropBomb();
        }
    }

    public void avoidBomb(String bombLocation, int x, int y){
//        System.out.println("bomb detected "+bombLocation);
    }
    
    @Override
    public void run() {
        while (true) {
            try {
                this.searchAndDestroy();
                Thread.sleep(500); // limit dumb bot action to 2 FPS
            } catch (InterruptedException ex) {
                BLogger.getInstance().logException2(ex);
            }
        }
    }
    
}
