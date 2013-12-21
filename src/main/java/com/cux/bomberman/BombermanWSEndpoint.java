/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.cux.bomberman;

import com.cux.bomberman.world.AbstractBlock;
import com.cux.bomberman.world.BBomb;
import java.io.IOException;
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

import com.cux.bomberman.world.BCharacter;
import com.cux.bomberman.world.Explosion;
import com.cux.bomberman.world.World;
import com.cux.bomberman.world.generator.ItemGenerator;
import com.cux.bomberman.world.items.AbstractItem;
import com.cux.bomberman.world.walls.AbstractWall;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 *
 * @author mihaicux
 */
@ServerEndpoint("/bombermanendpoint")
public class BombermanWSEndpoint {

    private static final ArrayList<Session> peers = new ArrayList<>();
    
    private static final ArrayList<BBomb> bombs = new ArrayList<>();
    
    private static final ArrayList<BBomb> markedBombs = new ArrayList<>();
    
    private static final HashMap<String, BCharacter> chars = new HashMap<>();
    
    private static final ArrayList<String> workingThreads =new ArrayList<>();
    
    private static final ArrayList<Explosion> explosions = new ArrayList<>();
    
    public static ArrayList<AbstractItem> items = new ArrayList<>();
    
    private static boolean isFirst = true;
    
    private static String precCharStr = "";
    private static String precBombStr = "";
    private static String precExplStr = "";
    private static String precWallStr = "";
    private static String precItemStr = "";
    
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
                boolean isAllowed = canPlantNewBomb(crtChar);
                if (crtChar.getState() == "Normal" && isAllowed){ // if he dropped the bomb, add the bomb to the screen
                    BBomb b = new BBomb(crtChar);
                    this.bombs.add(b);
                    map.blockMatrix[b.getPosX()/World.wallDim][b.getPosY()/World.wallDim] = b;
                }
                else if (!isAllowed){
                    crtChar.addOrDropBomb();
                }
                break;
            case "detonate":
                if(crtChar.isTriggered()){
                    detonateBomb(crtChar);
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
            case "reset":
                resetMap();
                break;
            case "getEnvironment":
                exportEnvironment(peer);
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
        if (peer.isOpen()){
            try {
                peer.close();
            } catch (IOException ex) {
                Logger.getLogger(BombermanWSEndpoint.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
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
            //map = new World("/home/mihaicux/bomberman_java/src/main/java/com/maps/firstmap.txt");
            map = new World("D:\\Programe\\hobby\\bomberman_java\\bomberman_java\\src\\main\\java\\com\\maps\\firstmap.txt");
        }
        
        map.chars[0][0].put(newChar.getName(), newChar);
        
        watchPeer(peer);
    }

    @OnError
    public synchronized void onError(Throwable t) {
    }
    
    public synchronized void exportEnvironment(Session peer){
        try {
            peer.getBasicRemote().sendText("chars:[" + exportChars());
            peer.getBasicRemote().sendText("map:[" + exportMap());
            peer.getBasicRemote().sendText("bombs:[" + exportBombs());
            peer.getBasicRemote().sendText("explosions:[" + exportExplosions());
            peer.getBasicRemote().sendText("items:[" + exportItems());
        } catch (IOException ex) {
            Logger.getLogger(BombermanWSEndpoint.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalStateException ex){
            Logger.getLogger(BombermanWSEndpoint.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ConcurrentModificationException ex) {
            Logger.getLogger(BombermanWSEndpoint.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public synchronized void resetMap(){
        map.walls = new ArrayList<>();
        map.blockMatrix = new AbstractBlock[100][100];
        //map = new World("/home/mihaicux/bomberman_java/src/main/java/com/maps/firstmap.txt");
        map = new World("D:\\Programe\\hobby\\bomberman_java\\bomberman_java\\src\\main\\java\\com\\maps\\firstmap.txt");
        items = new ArrayList<>();
        //precCharStr = "";
        //precBombStr = "";
        //precExplStr = "";
        precWallStr = "";
        //precItemStr = "";
    }
    
    public synchronized void stopThread(String threadId){
        workingThreads.remove(threadId);
    }
    
    public synchronized boolean isTrapped(BCharacter crtChar){
        int x     = crtChar.getPosX();
        int y     = crtChar.getPosY();
        int w     = crtChar.getWidth();
        int h     = crtChar.getHeight();
        int left  = (x/World.wallDim -1);
        int right = (x/World.wallDim +1);
        int up    = (y/World.wallDim -1);
        int down  = (y/World.wallDim +1);
        return ((x<=0 || wallExists(map.blockMatrix, left, y/World.wallDim) || bombExists(map.blockMatrix, left, y/World.wallDim)) &&
                (x+w >= World.getWidth() || wallExists(map.blockMatrix, right, y/World.wallDim) || bombExists(map.blockMatrix, right, y/World.wallDim)) &&
                (y <= 0 || wallExists(map.blockMatrix, x/World.wallDim, up) || bombExists(map.blockMatrix, x/World.wallDim, up)) &&
                (y+h >= World.getHeight() || wallExists(map.blockMatrix, x/World.wallDim, down) || bombExists(map.blockMatrix, x/World.wallDim, down)));
    }
    
    public synchronized void watchPeer(final Session peer){
        final BombermanWSEndpoint environment = this;
        new Thread(new Runnable(){
            @Override
            public synchronized void run() {
                while (peer.isOpen() && workingThreads.contains(peer.getId())){
                    isFirst = false;
                    BCharacter crtChar = chars.get(peer.getId());
                    if (isTrapped(crtChar)){
                        crtChar.setState("Trapped"); // will be automated reverted when a bomb kills him >:)
                    }   
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
                        String exportItemStr = environment.exportItems();
                        if (!exportExplStr.equals(precItemStr)){
                            peer.getBasicRemote().sendText("items:[" + exportItemStr);
                            precItemStr = exportItemStr;
                        }
                        Thread.sleep(10); // limiteaza la 100FPS comunicarea cu clientul
                    } catch (InterruptedException ex) {
                        Logger.getLogger(BombermanWSEndpoint.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (IOException ex) {
                        Logger.getLogger(BombermanWSEndpoint.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (IllegalStateException ex){
                        Logger.getLogger(BombermanWSEndpoint.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (ConcurrentModificationException ex) {
                        Logger.getLogger(BombermanWSEndpoint.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }).start();
    }
    
    protected void detonateBomb(BCharacter myChar){
        try{
            for (BBomb bomb : bombs){
                if (bomb.getCharId().equals(myChar.getName())){
                    bomb.setVolatileB(true);
                    markForRemove(bomb);
                    break;
                }
            }
        } catch (ConcurrentModificationException ex){
            Logger.getLogger(BombermanWSEndpoint.class.getName()).log(Level.SEVERE, null, ex);
        }
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
    
    public static boolean wallExists(AbstractBlock[][] data, int i, int j){
        try{
          AbstractBlock x = data[i][j];
          if (data[i][j] == null) return false;
          if (!AbstractWall.class.isAssignableFrom(data[i][j].getClass())) return false;
          return true;
        } catch(ArrayIndexOutOfBoundsException e){
          return false;
        }
    }
    
    public static boolean bombExists(AbstractBlock[][] data, int i, int j){
        try{
          AbstractBlock x = data[i][j];
          if (data[i][j] == null) return false;
          if (!BBomb.class.isAssignableFrom(data[i][j].getClass())) return false;
          return true;
        } catch(ArrayIndexOutOfBoundsException e){
          return false;
        }
        
    }
    
    public static boolean characterExists(int i, int j){
        return !map.chars[i][j].isEmpty();
    }
    
    protected synchronized void triggerBlewCharacter(final int x, final int y){
        new Thread(new Runnable(){
            @Override
            public synchronized void run() {
                Iterator it = map.chars[x][y].entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry pairs = (Map.Entry)it.next();
                    revertState((BCharacter)pairs.getValue());
                    it.remove(); // avoids a ConcurrentModificationException
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(BombermanWSEndpoint.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }).start();
    }
    
    protected synchronized void revertState(final BCharacter myChar){
        new Thread(new Runnable(){
            @Override
            public synchronized void run() {
                myChar.setState("Blow");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(BombermanWSEndpoint.class.getName()).log(Level.SEVERE, null, ex);
                }
                myChar.setState("Normal");
            }
        }).start();
    }
    
    protected synchronized void markForRemove(final BBomb bomb){
        new Thread(new Runnable(){
            @Override
            public synchronized void run() {
                try {
                    Explosion exp = new Explosion(bomb.getOwner());
                    Set<String> objectHits = Collections.synchronizedSet(new HashSet<String>());
                    map.blockMatrix[bomb.getPosX()/World.wallDim][bomb.getPosY()/World.wallDim] = null;
                    int charRange = bomb.getOwner().getBombRange();
                    
                    for (int i = 1; i <= charRange; i++){
                        
                        // right
                        if (bomb.getPosX() + bomb.getWidth()*(i+1) <= World.getWidth() && BombermanWSEndpoint.bombExists(map.blockMatrix, (bomb.getPosX()/World.wallDim)+i, bomb.getPosY()/World.wallDim) && !objectHits.contains("right")){
                             markForRemove((BBomb)map.blockMatrix[(bomb.getPosX()/World.wallDim)+i][bomb.getPosY()/World.wallDim]);
                             objectHits.add("right");
                             //System.out.println("hit bomb right");
                        }
                        else if (bomb.getPosX() + bomb.getWidth()*(i+1) <= World.getWidth() && BombermanWSEndpoint.characterExists((bomb.getPosX()/World.wallDim)+i, bomb.getPosY()/World.wallDim) && !objectHits.contains("right")){
                            triggerBlewCharacter((bomb.getPosX()/World.wallDim)+i, bomb.getPosY()/World.wallDim);
                            objectHits.add("right");
                            //System.out.println("hit character right");
                        }
                        else if (bomb.getPosX() + bomb.getWidth()*(i+1) <= World.getWidth() && BombermanWSEndpoint.wallExists(map.blockMatrix, (bomb.getPosX()/World.wallDim)+i, bomb.getPosY()/World.wallDim) && !objectHits.contains("right")){
                            exp.directions.add("right");
                            //System.out.println("hit wall right");
                            if (((AbstractWall)map.blockMatrix[(bomb.getPosX()/World.wallDim)+i][bomb.getPosY()/World.wallDim]).isBlowable()){
                                map.walls.remove(map.blockMatrix[(bomb.getPosX()/World.wallDim)+i][bomb.getPosY()/World.wallDim]);
                                exp.ranges.put("right", exp.ranges.get("right")+1);
                                //map.blockMatrix[(bomb.getPosX()/World.wallDim)+i][bomb.getPosY()/World.wallDim] = null;
                                flipForItems((bomb.getPosX()/World.wallDim)+i, bomb.getPosY()/World.wallDim);
                            }
                            objectHits.add("right");
                        }
                        else if (bomb.getPosX() + bomb.getWidth()*(i+1) <= World.getWidth() && !BombermanWSEndpoint.wallExists(map.blockMatrix, (bomb.getPosX()/World.wallDim)+i, bomb.getPosY()/World.wallDim) && !objectHits.contains("right")){
                            exp.directions.add("right");
                            exp.ranges.put("right", exp.ranges.get("right")+1);
                            //System.out.println("empty right");
                        }
                        
                        // left
                        if (bomb.getPosX() - bomb.getWidth()*i >= 0 && BombermanWSEndpoint.bombExists(map.blockMatrix, (bomb.getPosX()/World.wallDim)-i, bomb.getPosY()/World.wallDim) && !objectHits.contains("left")){
                            markForRemove((BBomb)map.blockMatrix[(bomb.getPosX()/World.wallDim)-i][bomb.getPosY()/World.wallDim]);
                            objectHits.add("left");
                            //System.out.println("hit bomb left");
                        }
                        else if (bomb.getPosX() - bomb.getWidth()*i >= 0 && BombermanWSEndpoint.characterExists((bomb.getPosX()/World.wallDim)-i, bomb.getPosY()/World.wallDim) && !objectHits.contains("left")){
                            triggerBlewCharacter((bomb.getPosX()/World.wallDim)-i, bomb.getPosY()/World.wallDim);
                            objectHits.add("left");
                            //System.out.println("hit character left");
                        }
                        else if (bomb.getPosX() - bomb.getWidth()*i >= 0 && BombermanWSEndpoint.wallExists(map.blockMatrix, (bomb.getPosX()/World.wallDim)-i, bomb.getPosY()/World.wallDim) && !objectHits.contains("left")){
                            exp.directions.add("left");
                            //System.out.println("hit wall left");
                            if (((AbstractWall)map.blockMatrix[(bomb.getPosX()/World.wallDim)-i][bomb.getPosY()/World.wallDim]).isBlowable()){
                                map.walls.remove(map.blockMatrix[(bomb.getPosX()/World.wallDim)-i][bomb.getPosY()/World.wallDim]);
                                exp.ranges.put("left", exp.ranges.get("left")+1);
                                //map.blockMatrix[(bomb.getPosX()/World.wallDim)-i][bomb.getPosY()/World.wallDim] = null;
                                flipForItems((bomb.getPosX()/World.wallDim)-i, bomb.getPosY()/World.wallDim);
                            }
                            objectHits.add("left");
                        }
                        else  if (bomb.getPosX() - bomb.getWidth()*i >= 0 && !BombermanWSEndpoint.wallExists(map.blockMatrix, (bomb.getPosX()/World.wallDim)-i, bomb.getPosY()/World.wallDim) && !objectHits.contains("left")){
                            exp.directions.add("left");
                            exp.ranges.put("left", exp.ranges.get("left")+1);
                            //System.out.println("empty left");
                        }
                        
                        // down
                        if (bomb.getPosY() + bomb.getHeight()*(i+1) <= World.getHeight() && BombermanWSEndpoint.bombExists(map.blockMatrix, (bomb.getPosX()/World.wallDim), bomb.getPosY()/World.wallDim+i) && !objectHits.contains("down")){
                            markForRemove((BBomb)map.blockMatrix[(bomb.getPosX()/World.wallDim)][bomb.getPosY()/World.wallDim+i]);
                            objectHits.add("down");
                            //System.out.println("hit bomb down");
                        }
                        else if (bomb.getPosY() + bomb.getHeight()*(i+1) <= World.getHeight() && BombermanWSEndpoint.characterExists((bomb.getPosX()/World.wallDim), bomb.getPosY()/World.wallDim+i) && !objectHits.contains("down")){
                            triggerBlewCharacter((bomb.getPosX()/World.wallDim), bomb.getPosY()/World.wallDim+i);
                            objectHits.add("down");
                            //System.out.println("hit character down");
                        }
                        else if (bomb.getPosY() + bomb.getHeight()*(i+1) <= World.getHeight() && BombermanWSEndpoint.wallExists(map.blockMatrix, (bomb.getPosX()/World.wallDim), bomb.getPosY()/World.wallDim+i) && !objectHits.contains("down")){
                            exp.directions.add("down");
                            //System.out.println("hit wall down");
                            if (((AbstractWall)map.blockMatrix[(bomb.getPosX()/World.wallDim)][bomb.getPosY()/World.wallDim+i]).isBlowable()){
                                map.walls.remove(map.blockMatrix[(bomb.getPosX()/World.wallDim)][bomb.getPosY()/World.wallDim+i]);
                                exp.ranges.put("down", exp.ranges.get("down")+1);
                                //map.blockMatrix[(bomb.getPosX()/World.wallDim)][bomb.getPosY()/World.wallDim+i] = null;
                                flipForItems((bomb.getPosX()/World.wallDim), bomb.getPosY()/World.wallDim+i);
                            }
                            objectHits.add("down");
                        }
                        else if (bomb.getPosY() + bomb.getHeight()*(i+1) <= World.getHeight() && !BombermanWSEndpoint.wallExists(map.blockMatrix, (bomb.getPosX()/World.wallDim), bomb.getPosY()/World.wallDim+i) && !objectHits.contains("down")){
                            exp.directions.add("down");
                            exp.ranges.put("down", exp.ranges.get("down")+1);
                            //System.out.println("empty down");
                        }                       
                        
                        // up
                        if (bomb.getPosY() - bomb.getHeight()*i >= 0 && BombermanWSEndpoint.bombExists(map.blockMatrix, (bomb.getPosX()/World.wallDim), bomb.getPosY()/World.wallDim-i) && !objectHits.contains("up")){
                            markForRemove((BBomb)map.blockMatrix[(bomb.getPosX()/World.wallDim)][bomb.getPosY()/World.wallDim-i]);
                            objectHits.add("up");
                            //System.out.println("hit bomb up");
                        }
                        else if (bomb.getPosY() - bomb.getHeight()*i >= 0 && BombermanWSEndpoint.characterExists((bomb.getPosX()/World.wallDim), bomb.getPosY()/World.wallDim-i) && !objectHits.contains("up")){
                            triggerBlewCharacter((bomb.getPosX()/World.wallDim), bomb.getPosY()/World.wallDim-i);
                            objectHits.add("up");
                            //System.out.println("hit character up");
                        }
                        else if (bomb.getPosY() - bomb.getHeight()*i >= 0 && BombermanWSEndpoint.wallExists(map.blockMatrix, (bomb.getPosX()/World.wallDim), bomb.getPosY()/World.wallDim-i) && !objectHits.contains("up")){
                            exp.directions.add("up");
                            //System.out.println("hit wall up");
                            if(((AbstractWall)map.blockMatrix[(bomb.getPosX()/World.wallDim)][bomb.getPosY()/World.wallDim-i]).isBlowable()){
                                map.walls.remove(map.blockMatrix[(bomb.getPosX() / World.wallDim)][bomb.getPosY() / World.wallDim - i]);
                                exp.ranges.put("up", exp.ranges.get("up")+1);
                                //map.blockMatrix[(bomb.getPosX()/World.wallDim)][bomb.getPosY()/World.wallDim-i] = null;
                                flipForItems((bomb.getPosX()/World.wallDim), bomb.getPosY()/World.wallDim-i);
                            }
                            objectHits.add("up");
                        }
                        else if (bomb.getPosY() - bomb.getHeight()*i >= 0 && !BombermanWSEndpoint.wallExists(map.blockMatrix, (bomb.getPosX()/World.wallDim), bomb.getPosY()/World.wallDim-i) && !objectHits.contains("up")){
                            exp.directions.add("up");
                            exp.ranges.put("up", exp.ranges.get("up")+1);
                            //System.out.println("empty up");
                        }
                    }
                    
                    markedBombs.add(bomb);
                    explosions.add(exp);
                    Thread.sleep(100); // wait one second before actual removing
                    explosions.remove(exp);
                    markedBombs.remove(bomb);
                    bombs.remove(bomb);
                } catch (InterruptedException ex) {
                    Logger.getLogger(BombermanWSEndpoint.class.getName()).log(Level.SEVERE, null, ex);
                } 
            }
        }).start();
    }
    
    protected synchronized void flipForItems(int x, int y){
        
        int rand = (int) (Math.random()*1000000);
        if (rand % 5 == 0){
            AbstractItem item = ItemGenerator.getInstance().generateRandomItem();
            item.setPosX(map.blockMatrix[x][y].getPosX());
            item.setPosY(map.blockMatrix[x][y].getPosY());
            map.blockMatrix[x][y] = null;
            map.blockMatrix[x][y] = item;
            items.add(item);
        }
        else{
            map.blockMatrix[x][y] = null; // remove the block
        }
    }
    
    protected boolean alreadyMarked(BBomb bomb){
        return markedBombs.contains(bomb);
    }
    
    protected synchronized String exportChars(){
        
        String ret = "";
        ArrayList<Session> peers2 = (ArrayList<Session>)peers.clone();
        
        for (Session peer : peers2) {
            ret += chars.get(peer.getId()).toString()+"[#charSep#]";
        }
        
        return ret;
    }
    
    protected synchronized String exportMap(){
        return map.toString();
    }
    
    protected synchronized String exportBombs(){
        
        String ret = "";
        ArrayList<BBomb> bombs2 = (ArrayList<BBomb>)bombs.clone();
        
        for (BBomb bomb : bombs2){
            if (bomb.isVolatileB() && (new Date().getTime() - bomb.getCreationTime().getTime())/1000 >= bomb.getLifeTime() && !alreadyMarked(bomb)){
               markForRemove(bomb);
               continue;
            }
            else{
                ret += bomb.toString()+"[#bombSep#]";
            }
        }
        
        return ret;
    }
    
    protected synchronized String exportExplosions(){
       
        String ret = "";
        ArrayList<Explosion> explosions2 = (ArrayList<Explosion>)explosions.clone();
        
        for (Explosion exp : explosions2){
            ret += exp.toString()+"[#explosionSep#]";
        }
        
        return ret;
    }
    
    protected synchronized String exportItems(){
        
        String ret = "";
        ArrayList<AbstractItem> items2 = (ArrayList<AbstractItem>)items.clone();
     
        for (AbstractItem item : items2){
            ret += item.toString()+"[#itemSep#]";
        }
        
        return ret;
    }
    
    public synchronized boolean canPlantNewBomb(BCharacter crtChar){
        int maxBombs = crtChar.getMaxBombs();
        int plantedBombs = 0;
        ArrayList<BBomb> bombs2 = (ArrayList<BBomb>)bombs.clone();
        
        for (BBomb bomb : bombs2){
            if (bomb.getOwner().getId() == crtChar.getId())
                plantedBombs++;
        }
        
        return (plantedBombs < maxBombs);
    }
    
    public static String checkWorldMatrix(AbstractBlock[][] data, int i, int j){
        String ret = "";
        try{
            AbstractBlock x = data[i][j];
            if (data[i][j] == null) return "blank";
            else if (AbstractWall.class.isAssignableFrom(data[i][j].getClass())) return "wall";
            else if (BBomb.class.isAssignableFrom(data[i][j].getClass())) return "bomb";
            else if (BCharacter.class.isAssignableFrom(data[i][j].getClass())) return "char";
            return "else";
        } catch(ArrayIndexOutOfBoundsException e){
          return "out of bounds";
        }
    }
    
}
