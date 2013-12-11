/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.cux.bomberman;

import com.cux.bomberman.world.BBomb;
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
import java.util.ConcurrentModificationException;
import java.util.Date;

/**
 *
 * @author mihaicux
 */
@ServerEndpoint("/bombermanendpoint")
public class BombermanWSEndpoint {

    private static Set<Session> peers = Collections.synchronizedSet(new HashSet<Session>());
    
    private static Set<BBomb> bombs = Collections.synchronizedSet(new HashSet<BBomb>());
    
    private static boolean isFirst = true;
    
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
        
        switch (message){
            case "up":
                myChar.setDirection("Up");
                if (!map.HasMapCollision(myChar))
                    myChar.moveUp();
                //else myChar.moveDown();
                break;
            case "down":
                myChar.setDirection("Down");
                if (!map.HasMapCollision(myChar))
                    myChar.moveDown();
                //else myChar.moveUp();
                break;
            case "left":
                myChar.setDirection("Left");
                if (!map.HasMapCollision(myChar))
                    myChar.moveLeft();
                //else myChar.moveRight();
                break;
            case "right":
                myChar.setDirection("Right");
                if (!map.HasMapCollision(myChar))
                    myChar.moveRight();
                //else myChar.moveLeft();
                break;
            case "bomb":
                myChar.addOrDropBomb(); // change character state
                if (myChar.getState() == "Normal"){ // if he dropped the bomb, add the bomb to the screen
                    this.bombs.add(new BBomb(myChar));
                }
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
        
        //exportEnvironment();
        if (isFirst){
            watchPeers();
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
        
        if (myChar == null){
            myChar = new BCharacter(peer.getId());
            myChar.setPosX(0);
            myChar.setPosY(0);
            myChar.setWidth(20);
            myChar.setHeight(28);
        }
        
        if (isFirst){
            watchPeers();
        }
    }

    @OnError
    public void onError(Throwable t) {
    }
    
    public synchronized void watchPeers(){
        System.out.println("da");
        new Thread(new Runnable(){
            @Override
            public void run() {
                isFirst = false;
                while (true){
                    try {
                        exportEnvironment();
                        Thread.sleep(30); // limiteaza la 100FPS comunicarea cu serverul
                    } catch (InterruptedException ex) {
                        Logger.getLogger(BombermanWSEndpoint.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }).start();
    }
    
    protected synchronized void exportEnvironment(){
        for (Session peer2 : peers) {
            try {
                peer2.getBasicRemote().sendText("char:[" + this.exportChars()); // something...
                peer2.getBasicRemote().sendText("map:[" + this.exportMap());
                peer2.getBasicRemote().sendText("bomb:[" + this.exportBombs());
            } catch (IOException ex) {
                Logger.getLogger(BombermanWSEndpoint.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    protected synchronized String exportChars(){
        return myChar.toString();
    }
    
    protected synchronized String exportMap(){
        return map.toString();
    }
    
    protected synchronized void markForRemove(final BBomb bomb){
        new Thread(new Runnable(){
            @Override
            public void run() {
                try {
                    Thread.sleep(1000); // wait for a one second before actual removing
                    bombs.remove(bomb);
                } catch (InterruptedException ex) {
                    Logger.getLogger(BombermanWSEndpoint.class.getName()).log(Level.SEVERE, null, ex);
                } 
            }
        }).start();
    }
    
    static String ret = "";
    
    protected synchronized String exportBombs(){
        
        new Thread(new Runnable(){
            @Override
            public void run() {
                ret = "";
                 for (BBomb bomb : bombs){
                    try{
                        if (bomb.isVolatileB() && (new Date().getTime() - bomb.getCreationTime().getTime())/1000 >= bomb.getLifeTime()){
                            markForRemove(bomb);
                            continue;
                        }
                        else{
                            ret += bomb.toString()+"[#bombSep#]";
                        }
                    }
                    catch (ConcurrentModificationException ex){
                        Logger.getLogger(BombermanWSEndpoint.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } 
            }
        }).start();
       
        return ret;
    }
    
}
