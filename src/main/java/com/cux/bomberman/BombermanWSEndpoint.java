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
                myChar.moveUp();
                break;
            case "down":
                myChar.setDirection("Down");
                myChar.moveDown();
                break;
            case "left":
                myChar.setDirection("Left");
                myChar.moveLeft();
                break;
            case "right":
                myChar.setDirection("Right");
                myChar.moveRight();
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
                peer2.getBasicRemote().sendText(myChar.toString()); // something...
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
    
}
