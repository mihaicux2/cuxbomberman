/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.cux.bomberman;

import com.cux.bomberman.world.AbstractBlock;
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
import com.cux.bomberman.world.Explosion;
import com.cux.bomberman.world.World;
import com.cux.bomberman.world.walls.AbstractWall;
import com.cux.bomberman.world.walls.BrickWall;
import java.util.Collection;
//import com.cux.bomberman.world.generator.WallGenerator;
//import com.cux.bomberman.world.walls.AbstractWall;
import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 *
 * @author mihaicux
 */
@ServerEndpoint("/bombermanendpoint")
public class BombermanWSEndpoint {

    private static Set<Session> peers = Collections.synchronizedSet(new HashSet<Session>());
    
    private static Set<BBomb> bombs = Collections.synchronizedSet(new HashSet<BBomb>());
    
    private static ConcurrentMap<String, BCharacter> chars = new ConcurrentHashMap<String, BCharacter>();
    
    private static Set<String> workingThreads = Collections.synchronizedSet(new HashSet<String>());
    
    private static Set<Explosion> explosions = Collections.synchronizedSet(new HashSet<Explosion>());
    
    private static boolean isFirst = true;
    
    //private static BCharacter myChar = null;
    
    private static World map = null;
    
    @OnMessage
    public synchronized String onMessage(String message, Session peer) {
        
        BCharacter crtChar = chars.get(peer.getId());
        
        switch (message){
            case "up":
                crtChar.setDirection("Up");
                if (!crtChar.isWalking() && !map.HasMapCollision(crtChar)){
                    crtChar.moveUp();
                }
                //else crtChar.moveDown();
                break;
            case "down":
                crtChar.setDirection("Down");
                if (!crtChar.isWalking() && !map.HasMapCollision(crtChar)){
                    crtChar.moveDown();
                }
                //else crtChar.moveUp();
                break;
            case "left":
                crtChar.setDirection("Left");
                if (!crtChar.isWalking() && !map.HasMapCollision(crtChar)){
                    crtChar.moveLeft();
                }
                //else crtChar.moveRight();
                break;
            case "right":
                crtChar.setDirection("Right");
                if (!crtChar.isWalking() && !map.HasMapCollision(crtChar)){
                     crtChar.moveRight();
                }
                //else crtChar.moveLeft();
                break;
            case "bomb":
                crtChar.addOrDropBomb(); // change character state
                if (crtChar.getState() == "Normal"){ // if he dropped the bomb, add the bomb to the screen
                    BBomb b = new BBomb(crtChar);
                    this.bombs.add(b);
                    map.blockMatrix[b.getPosX()/World.wallDim][b.getPosY()/World.wallDim] = b;
                }
                break;
            case "trap":
                crtChar.makeTrapped();
                break;
            case "free":
                crtChar.makeFree();
                break;
            case "blow":
                crtChar.setState("Blow");
                break;
            case "win":
                crtChar.setState("Win");
                break;
            case "QUIT":
                chars.remove(peer.getId());
                if (peer.isOpen()){
                    try {
                        peer.close();
                    } catch (IOException ex) {
                        Logger.getLogger(BombermanWSEndpoint.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            default:
                break;
        }
        
        return null; // any string will be send to the requesting peer
    }

    @OnClose
    public synchronized void onClose(Session peer) {
        this.delayedRemove(peer.getId());
        this.stopThread(peer.getId());
        peers.remove(peer);
        System.out.println("out...");
    }

    @OnOpen
    public synchronized void onOpen(Session peer) {
        peers.add(peer);
        
        workingThreads.add(peer.getId());
        
        BCharacter newChar = new BCharacter(peer.getId());
        newChar = new BCharacter(peer.getId());
        newChar.setPosX(0);
        newChar.setPosY(0);
        newChar.setWidth(World.wallDim);
        newChar.setHeight(World.wallDim);
        
        chars.put(peer.getId(), newChar);
        
        if (map == null){
            map = new World("/home/mihaicux/bomberman_java/src/main/java/com/maps/firstmap.txt");
        }
        
        watchPeer(peer);
    }

    @OnError
    public synchronized void onError(Throwable t) {
    }
    
    public synchronized void stopThread(String threadId){
        workingThreads.remove(threadId);
    }
    
    public synchronized void watchPeer(final Session peer){
        final BombermanWSEndpoint environment = this;
        new Thread(new Runnable(){
            @Override
            public synchronized void run() {
                isFirst = false;
                String precCharStr = "";
                String precBombStr = "";
                String precExplStr = "";
                String precWallStr = "";
                while (peer.isOpen() && workingThreads.contains(peer.getId())){
                    try {
                        String exportCharStr = environment.exportChars();
                        if (!exportCharStr.equals(precCharStr)){
                            peer.getBasicRemote().sendText("chars:[" + exportCharStr);
                            precCharStr = exportCharStr;
                        }
                        
                        String exportWallStr = environment.exportMap();
                        if (!exportWallStr.equals(precWallStr)){
                            peer.getBasicRemote().sendText("map:[" + exportWallStr);
                            precWallStr = exportWallStr;
                        }
                        
                        String exportBombStr = environment.exportBombs();
                        if (!exportBombStr.equals(precBombStr)){
                            peer.getBasicRemote().sendText("bombs:[" + exportBombStr);
                            precBombStr = exportBombStr;
                            //System.out.println(exportBombStr);
                        }
                        String exportExplStr = environment.exportExplosions();
                        if (!exportExplStr.equals(precExplStr)){
                            peer.getBasicRemote().sendText("explosions:[" + exportExplStr);
                            precExplStr = exportExplStr;
                        }
                        Thread.sleep(10); // limiteaza la 100FPS comunicarea cu clientul
                    } catch (InterruptedException ex) {
                        Logger.getLogger(BombermanWSEndpoint.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (IOException ex) {
                        Logger.getLogger(BombermanWSEndpoint.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }).start();
    }
    
    protected synchronized void delayedRemove(final String peerId){
        new Thread(new Runnable(){
            @Override
            public void run() {
                try {
                    Thread.sleep(1000); // wait for a one second before actual removing
                    chars.remove(peerId);
                    System.out.println("final");
                } catch (InterruptedException ex) {
                    Logger.getLogger(BombermanWSEndpoint.class.getName()).log(Level.SEVERE, null, ex);
                } 
            }
        }).start();
    }
    
    public static boolean elementExists(AbstractBlock[][] data, int i, int j){
        try{
          AbstractBlock x = data[i][j];
          if (data[i][j] == null) return false;
          if (!AbstractWall.class.isAssignableFrom(data[i][j].getClass())) return false;
          return true;
        } catch(ArrayIndexOutOfBoundsException e){
          return false;
        }
    }
    
    protected synchronized void markForRemove(final BBomb bomb){
        new Thread(new Runnable(){
            @Override
            public synchronized void run() {
                try {
                    Explosion exp = new Explosion(bomb.getOwner());
                    map.blockMatrix[bomb.getPosX()/World.wallDim][bomb.getPosY()/World.wallDim] = null;
                    int charRange = bomb.getOwner().getBombRange();
                    for (int i = 1; i <= charRange; i++){
                        if (BombermanWSEndpoint.elementExists(map.blockMatrix, (bomb.getPosX()/World.wallDim)+i, bomb.getPosY()/World.wallDim) && ((AbstractWall)map.blockMatrix[(bomb.getPosX()/World.wallDim)+i][bomb.getPosY()/World.wallDim]).isBlowable()){
                            map.walls.remove(map.blockMatrix[(bomb.getPosX()/World.wallDim)+i][bomb.getPosY()/World.wallDim]);
                            map.blockMatrix[(bomb.getPosX()/World.wallDim)+i][bomb.getPosY()/World.wallDim] = null;
                            exp.directions.add("right");
                        }
                        else if (!BombermanWSEndpoint.elementExists(map.blockMatrix, (bomb.getPosX()/World.wallDim)+i, bomb.getPosY()/World.wallDim)){
                            exp.directions.add("right");
                        }
                        if (BombermanWSEndpoint.elementExists(map.blockMatrix, (bomb.getPosX()/World.wallDim)-i, bomb.getPosY()/World.wallDim) && ((AbstractWall)map.blockMatrix[(bomb.getPosX()/World.wallDim)-i][bomb.getPosY()/World.wallDim]).isBlowable()){
                            map.walls.remove(map.blockMatrix[(bomb.getPosX()/World.wallDim)-i][bomb.getPosY()/World.wallDim]);
                            map.blockMatrix[(bomb.getPosX()/World.wallDim)-i][bomb.getPosY()/World.wallDim] = null;
                            exp.directions.add("left");
                        }
                        else  if (!BombermanWSEndpoint.elementExists(map.blockMatrix, (bomb.getPosX()/World.wallDim)-i, bomb.getPosY()/World.wallDim)){
                            exp.directions.add("left");
                        }
                        if (BombermanWSEndpoint.elementExists(map.blockMatrix, (bomb.getPosX()/World.wallDim), bomb.getPosY()/World.wallDim+i) && ((AbstractWall)map.blockMatrix[(bomb.getPosX()/World.wallDim)][bomb.getPosY()/World.wallDim+i]).isBlowable()){
                            map.walls.remove(map.blockMatrix[(bomb.getPosX()/World.wallDim)][bomb.getPosY()/World.wallDim+i]);
                            map.blockMatrix[(bomb.getPosX()/World.wallDim)][bomb.getPosY()/World.wallDim+i] = null;
                            exp.directions.add("down");
                        }
                        else if (!BombermanWSEndpoint.elementExists(map.blockMatrix, (bomb.getPosX()/World.wallDim), bomb.getPosY()/World.wallDim+i)){
                            exp.directions.add("down");
                        }
                        if (BombermanWSEndpoint.elementExists(map.blockMatrix, (bomb.getPosX()/World.wallDim), bomb.getPosY()/World.wallDim-i) && ((AbstractWall)map.blockMatrix[(bomb.getPosX()/World.wallDim)][bomb.getPosY()/World.wallDim-i]).isBlowable()){
                            map.walls.remove(map.blockMatrix[(bomb.getPosX()/World.wallDim)][bomb.getPosY()/World.wallDim-i]);
                            map.blockMatrix[(bomb.getPosX()/World.wallDim)][bomb.getPosY()/World.wallDim-i] = null;
                            exp.directions.add("up");
                        }
                        else if (!BombermanWSEndpoint.elementExists(map.blockMatrix, (bomb.getPosX()/World.wallDim), bomb.getPosY()/World.wallDim-i)){
                            exp.directions.add("up");
                        }
                    }
                    explosions.add(exp);
                    Thread.sleep(1000); // wait one second before actual removing
                    explosions.remove(exp);
                    bombs.remove(bomb);
                } catch (InterruptedException ex) {
                    Logger.getLogger(BombermanWSEndpoint.class.getName()).log(Level.SEVERE, null, ex);
                } 
            }
        }).start();
    }
    
    protected synchronized String exportChars(){
        //return myChar.toString();
        String ret = "";
        try{
            for (Session peer : peers) {
                ret += chars.get(peer.getId()).toString()+"[#charSep#]";
            }
        }
        catch (ConcurrentModificationException ex){
            Logger.getLogger(BombermanWSEndpoint.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return ret;
    }
    
    protected synchronized String exportMap(){
        return map.toString();
    }
    
    //static String ret = "";
    
    protected synchronized String exportBombs(){
        
//        new Thread(new Runnable(){
//            @Override
//            public void run() {
//                ret = "";
//                 for (BBomb bomb : bombs){
//                    try{
//                        if (bomb.isVolatileB() && (new Date().getTime() - bomb.getCreationTime().getTime())/1000 >= bomb.getLifeTime()){
//                            markForRemove(bomb);
//                            continue;
//                        }
//                        else{
//                            ret += bomb.toString()+"[#bombSep#]";
//                        }
//                    }
//                    catch (ConcurrentModificationException ex){
//                        Logger.getLogger(BombermanWSEndpoint.class.getName()).log(Level.SEVERE, null, ex);
//                    }
//                } 
//            }
//        }).start();
        String ret = "";
        try{
            for (BBomb bomb : bombs){
                if (bomb.isVolatileB() && (new Date().getTime() - bomb.getCreationTime().getTime())/1000 >= bomb.getLifeTime()){
                   markForRemove(bomb);
                   continue;
               }
               else{
                   ret += bomb.toString()+"[#bombSep#]";
               }
           }
        }
        catch (ConcurrentModificationException ex){
            Logger.getLogger(BombermanWSEndpoint.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ret;
    }
    
    protected synchronized String exportExplosions(){
        
//        new Thread(new Runnable(){
//            @Override
//            public void run() {
//                ret = "";
//                 for (BBomb bomb : explosions){
//                    ret += bomb.toString()+"[#explosionSep#]";
//                } 
//            }
//        }).start();
       
        String ret = "";
        try{
            for (BBomb bomb : explosions){
               ret += bomb.toString()+"[#explosionSep#]";
            }
        }
        catch (ConcurrentModificationException ex){
            Logger.getLogger(BombermanWSEndpoint.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return ret;
    }
    
}
