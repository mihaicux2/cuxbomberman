/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.cux.bomberman;

import java.io.IOException;
/*import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;*/
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

//import com.cux.bomberman.world.walls.*;
import com.cux.bomberman.world.BCharacter;
import com.cux.bomberman.world.World;
import com.cux.bomberman.world.generator.WallGenerator;
import com.cux.bomberman.world.walls.AbstractWall;

/**
 *
 * @author mihaicux
 */
@ServerEndpoint("/bombermanendpoint")
public class BombermanWSEndpoint {

    private static Set<Session> peers = Collections.synchronizedSet(new HashSet<Session>());
    
    private static boolean isFirst = false;
    
    private static BCharacter myChar = null;
    
    private static World map = null;
    
    @OnMessage
    public String onMessage(String message, Session peer) {
//        String wall = WallGenerator.getInstance().generateRandomWall().toString();
//        for (Session peer2 : peers){
//            try {
//                //if (!peer.equals(peer2))
//                peer2.getBasicRemote().sendText(wall); // something...
//            } catch (IOException ex) {
//                Logger.getLogger(BombermanWSEndpoint.class.getName()).log(Level.SEVERE, null, ex);
//            }
//        }
        if (myChar == null){
            myChar = new BCharacter(peer.getId());
            myChar.setPosX(0);
            myChar.setPosY(0);
            myChar.setWidth(20);
            myChar.setHeight(30);
        }
        
        switch (message){
            case "up":
                myChar.setDirection("Up");
                if (!this.MapCollision(myChar, map))
                    myChar.moveUp();
                break;
            case "down":
                myChar.setDirection("Down");
                if (!this.MapCollision(myChar, map))
                    myChar.moveDown();
                //else myChar.moveUp();
                break;
            case "left":
                myChar.setDirection("Left");
                if (!this.MapCollision(myChar, map))
                    myChar.moveLeft();
                //else myChar.moveRight();
                break;
            case "right":
                myChar.setDirection("Right");
                if (!this.MapCollision(myChar, map))
                    myChar.moveRight();
                //else myChar.moveLeft();
                break;
            case "bomb":
                myChar.addOrDropBomb();
                break;
            case "trap":
                myChar.makeTrapped();
                break;
            case "free":
                myChar.makeFree();
                break;
            case "blow":
                myChar.setState("Blow");
                break;
            case "win":
                myChar.setState("Win");
                break;
            default:
                break;
        }
        for (Session peer2 : peers){
            try {
                peer2.getBasicRemote().sendText("char:["+myChar.toString()); // something...
            } catch (IOException ex) {
                Logger.getLogger(BombermanWSEndpoint.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return null; // any string will be send to the requesting peer
    }

    @OnClose
    public void onClose(Session peer) {
        peers.remove(peer);
    }

    @OnOpen
    public void onOpen(Session peer) {
        peers.add(peer);
        
        if (map == null){
            map = new World("/home/mihaicux/bomberman_java/src/main/java/com/maps/firstmap.txt");
        }
        try { 
            peer.getBasicRemote().sendText("map:["+map.toString());
        } catch (IOException ex) {
            Logger.getLogger(BombermanWSEndpoint.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (!isFirst){
            isFirst = true;
            watchPeers();
        }
    }

    @OnError
    public void onError(Throwable t) {
    }
    
    public synchronized void watchPeers(){
        new Thread(new Runnable(){

            @Override
            public void run() {
                while (true){
                    try {
                        // monitor :-?? bombs, bonuses, events in general 
                        for (Session peer : peers){
                            //peer.getBasicRemote().sendText("asd");
                            //peer.getBasicRemote().sendBinary(ByteBuffer.wrap(message.getBytes()));
                        }
                        Thread.sleep(10); // limiteaza la 100FPS comunicarea cu serverul
                    } catch (InterruptedException ex) {
                        Logger.getLogger(BombermanWSEndpoint.class.getName()).log(Level.SEVERE, null, ex);
                    }/* catch (IOException ex) {
                        Logger.getLogger(BombermanWSEndpoint.class.getName()).log(Level.SEVERE, null, ex);
                    }*/
                }
            }
        }).start();
    }
    
    public synchronized boolean MapCollision(BCharacter myChar, World map){
        boolean ret = false;
        if (map.bricks.size() > 0){
            for (AbstractWall brick : map.bricks){
                if (myChar.hits(brick) && myChar.walksTo(brick)){
                    //myChar.stepBack(brick);
                    ret = true;
                    break;
                }
            }
        }
        return ret;
    }
    
}
