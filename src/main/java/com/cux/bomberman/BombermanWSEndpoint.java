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
//import java.util.logging.Level;
//import java.util.logging.Logger;
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
//import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.concurrent.ConcurrentMap;
//import java.util.logging.FileHandler;
//import java.util.logging.SimpleFormatter;
import com.cux.bomberman.util.BLogger;
import com.cux.bomberman.world.generator.WorldGenerator;
//import java.util.Map.Entry;
import javax.websocket.server.PathParam;
//import com.cux.bomberman.util.BMessenger;

/**
 *
 * @author mihaicux Toate fisierele de citit/scris se pun relativ la originea
 * domeniului Windows :
 * C:\Users\mihaicux\AppData\Roaming\NetBeans\7.4\config\GF_4.0\domain1\config
 * Linux : TBD
 */
@ServerEndpoint("/bombermanendpoint/")
public class BombermanWSEndpoint {

    private static final Set<Session> peers = Collections.synchronizedSet(new HashSet<Session>());

    private static final Map<Integer, ArrayList<BBomb>> bombs = Collections.synchronizedMap(new HashMap<Integer, ArrayList<BBomb>>());

    private static final Map<Integer, Set<BBomb>> markedBombs = Collections.synchronizedMap(new HashMap<Integer, Set<BBomb>>());

    private static final Map<String, BCharacter> chars = Collections.synchronizedMap(new HashMap<String, BCharacter>());

    private static final Set<String> workingThreads = Collections.synchronizedSet(new HashSet<String>());

    private static final Map<Integer, Set<Explosion>> explosions = Collections.synchronizedMap(new HashMap<Integer, Set<Explosion>>());

    public static Map<Integer, Set<AbstractItem>> items = Collections.synchronizedMap(new HashMap<Integer, Set<AbstractItem>>());

    private static boolean isFirst = true;

    public static Map<Integer, World> map = Collections.synchronizedMap(new HashMap<Integer, World>());

    private final static int MAX_PLAYERS = 2;

    private static final Map<Integer, Integer> mapPlayers = Collections.synchronizedMap(new HashMap<Integer, Integer>());

    private static int mapNumber = 0;

    public static final Map<Integer, Boolean> charsChanged = Collections.synchronizedMap(new HashMap<Integer, Boolean>());
    public static final Map<Integer, Boolean> mapChanged = Collections.synchronizedMap(new HashMap<Integer, Boolean>());
    public static final Map<Integer, Boolean> bombsChanged = Collections.synchronizedMap(new HashMap<Integer, Boolean>());
    public static final Map<Integer, Boolean> explosionsChanged = Collections.synchronizedMap(new HashMap<Integer, Boolean>());
    public static final Map<Integer, Boolean> itemsChanged = Collections.synchronizedMap(new HashMap<Integer, Boolean>());

    public static final Map<String, Integer> peerRooms = Collections.synchronizedMap(new HashMap<String, Integer>());
    
    @OnMessage
    public synchronized String onMessage(String message, Session peer) {

        BCharacter crtChar = chars.get(peer.getId());

        int roomNr = getRoom(peer);
        
        //BLogger.getInstance().log(BLogger.LEVEL_FINE, "client message : "+message);
        switch (message) {
            case "up":
                crtChar.setDirection("Up");
                if (!crtChar.isWalking() && !map.get(roomNr).HasMapCollision(crtChar)) {
                    crtChar.moveUp();
                }
                charsChanged.put(roomNr, true);
                //else crtChar.moveDown();
                break;
            case "down":
                crtChar.setDirection("Down");
                if (!crtChar.isWalking() && !map.get(roomNr).HasMapCollision(crtChar)) {
                    crtChar.moveDown();
                }
                charsChanged.put(roomNr, true);
                //else crtChar.moveUp();
                break;
            case "left":
                crtChar.setDirection("Left");
                if (!crtChar.isWalking() && !map.get(roomNr).HasMapCollision(crtChar)) {
                    crtChar.moveLeft();
                }
                charsChanged.put(roomNr, true);
                //else crtChar.moveRight();
                break;
            case "right":
                crtChar.setDirection("Right");
                if (!crtChar.isWalking() && !map.get(roomNr).HasMapCollision(crtChar)) {
                    crtChar.moveRight();
                }
                charsChanged.put(roomNr, true);
                //else crtChar.moveLeft();
                break;
            case "bomb":
                crtChar.addOrDropBomb(); // change character state
                boolean isAllowed = canPlantNewBomb(peer, crtChar);
                if (crtChar.getState() == "Normal" && isAllowed) { // if he dropped the bomb, add the bomb to the screen
                    BBomb b = new BBomb(crtChar);
                    //this.bombs.add(b);
                    if (this.bombs.get(roomNr) == null) {
                        this.bombs.put(roomNr, new ArrayList<BBomb>());
                    }
                    this.bombs.get(roomNr).add(b);
                    map.get(roomNr).blockMatrix[b.getPosX() / World.wallDim][b.getPosY() / World.wallDim] = b;
                } else if (!isAllowed) {
                    crtChar.addOrDropBomb();
                }
                charsChanged.put(roomNr, true);
                bombsChanged.put(roomNr, true);
                break;
            case "detonate":
                if (crtChar.isTriggered()) {
                    detonateBomb(crtChar, peer);
                    bombsChanged.put(roomNr, true);
                }
                break;
            case "trap":
                crtChar.makeTrapped();
                charsChanged.put(roomNr, true);
                break;
            case "free":
                crtChar.makeFree();
                charsChanged.put(roomNr, true);
                break;
            case "blow":
                crtChar.setState("Blow");
                charsChanged.put(roomNr, true);
                break;
            case "win":
                crtChar.setState("Win");
                charsChanged.put(roomNr, true);
                break;
            case "reset":
                //resetMap();
                break;
            case "getEnvironment":
                exportEnvironment(peer);
                break;
            case "QUIT":
                chars.remove(peer.getId());
                if (peer.isOpen()) {
                    try {
                        peer.close();
                    } catch (IOException ex) {
                        BLogger.getInstance().logException2(ex);
                    }
                }
                charsChanged.put(roomNr, true);
            default:
                break;
        }

        return null; // any string will be send to the requesting peer
    }

    @OnClose
    public synchronized void onClose(Session peer) {
        this.delayedRemove(peer.getId());
        this.stopThread(peer.getId());
        if (peer.isOpen()) {
            try {
                peer.close();
            } catch (IOException ex) {
                BLogger.getInstance().logException2(ex);
            }
        }
        int roomNr = getRoom(peer);
        peers.remove(peer);
        charsChanged.put(roomNr, true);
        System.out.println("out...");
    }

    @OnOpen
    public synchronized void onOpen(Session peer, @PathParam("room") final String room) {

        peers.add(peer);

        workingThreads.add(peer.getId());

        if (peers.size() == 1) { // this is the first player?
            mapNumber = 1;
            
            if (mapPlayers.get(mapNumber) == null) {
                mapPlayers.put(mapNumber, 1); // one player in current map
            } else {
                if (mapPlayers.get(mapNumber) == MAX_PLAYERS) { // create a new room /map
                    mapNumber++;
                    mapPlayers.put(mapNumber, 1); // one player in the current map
                } else {
                    mapPlayers.put(mapNumber, 1 + mapPlayers.get(mapNumber)); // another player in the current map
                }
            }
            
            watchBombs();
            
        } else {
            if (mapPlayers.get(mapNumber) == MAX_PLAYERS) { // create a new room /map
                mapNumber++;
                mapPlayers.put(mapNumber, 1); // one player in the current map
            } else {
                mapPlayers.put(mapNumber, 1 + mapPlayers.get(mapNumber)); // another player in the current map
            }
        }
        peer.getUserProperties().put("room", mapNumber);

        //BLogger.getInstance().log(BLogger.LEVEL_INFO, "peer connected ["+peer.getId()+"], room "+peer.getUserProperties().get("room"));
        if (map.size() == 0 || map.get(mapNumber) == null) {
            //map = new World("/home/mihaicux/bomberman_java/src/main/java/com/maps/firstmap.txt");
            //BLogger.getInstance().log(BLogger.LEVEL_INFO, "first map...");
            //map.put(roomNr, new World("D:\\Programe\\hobby\\bomberman_java\\src\\main\\java\\com\\maps\\firstmap.txt"));
            map.put(mapNumber, WorldGenerator.getInstance().generateWorld(1500, 750));
            //BLogger.getInstance().log(BLogger.LEVEL_INFO, "created");
        }

        //BLogger.getInstance().log(BLogger.LEVEL_INFO, "peer connected2 ["+peer.getId()+"], room "+peer.getUserProperties().get("room"));
        BCharacter newChar = new BCharacter(peer.getId(), mapNumber);
        newChar.setPosX(0);
        newChar.setPosY(0);
        newChar.setWidth(World.wallDim);
        newChar.setHeight(World.wallDim);
        //newChar.setPeer(peer);

        chars.put(peer.getId(), newChar);

        map.get(mapNumber).chars[0][0].put(newChar.getName(), newChar);

        watchPeer(peer);

        BLogger.getInstance().log(BLogger.LEVEL_INFO, "peer connected [" + peer.getId() + "], room " + peer.getUserProperties().get("room"));
        //System.exit(0);
        charsChanged.put(mapNumber, true);
        bombsChanged.put(mapNumber, true);
        mapChanged.put(mapNumber, true);
        itemsChanged.put(mapNumber, true);
        explosionsChanged.put(mapNumber, true);
    }

    @OnError
    public synchronized void onError(Throwable t) {
    }

    public synchronized void exportEnvironment(Session peer) {
        int roomNr = getRoom(peer);
        try {
            //if (charsChanged.get(roomNr)){
            peer.getBasicRemote().sendText("chars:[" + exportChars(peer));
            //}
            //if (mapChanged.get(roomNr)){
            peer.getBasicRemote().sendText("map:[" + exportMap(peer));
            //}
            //if (bombsChanged.get(roomNr)){
            peer.getBasicRemote().sendText("bombs:[" + exportBombs(peer));
            //}
            //if (explosionsChanged.get(roomNr)){
            peer.getBasicRemote().sendText("explosions:[" + exportExplosions(peer));
            //}
            //if (itemsChanged.get(roomNr)){
            peer.getBasicRemote().sendText("items:[" + exportItems(peer));
            //}
        } catch (IOException ex) {
            BLogger.getInstance().logException2(ex);
        } catch (IllegalStateException ex) {
            BLogger.getInstance().logException2(ex);
        } catch (ConcurrentModificationException ex) {
            BLogger.getInstance().logException2(ex);
        }
    }

    public synchronized void stopThread(String threadId) {
        workingThreads.remove(threadId);
    }

    public synchronized boolean isTrapped(BCharacter crtChar, Session peer) {
        int x = crtChar.getPosX();
        int y = crtChar.getPosY();
        int w = crtChar.getWidth();
        int h = crtChar.getHeight();
        int left = (x / World.wallDim - 1);
        int right = (x / World.wallDim + 1);
        int up = (y / World.wallDim - 1);
        int down = (y / World.wallDim + 1);
        int roomNr = getRoom(peer);
        return ((x <= 0 || wallExists(map.get(roomNr).blockMatrix, left, y / World.wallDim) || bombExists(map.get(roomNr).blockMatrix, left, y / World.wallDim))
                && (x + w >= World.getWidth() || wallExists(map.get(roomNr).blockMatrix, right, y / World.wallDim) || bombExists(map.get(roomNr).blockMatrix, right, y / World.wallDim))
                && (y <= 0 || wallExists(map.get(roomNr).blockMatrix, x / World.wallDim, up) || bombExists(map.get(roomNr).blockMatrix, x / World.wallDim, up))
                && (y + h >= World.getHeight() || wallExists(map.get(roomNr).blockMatrix, x / World.wallDim, down) || bombExists(map.get(roomNr).blockMatrix, x / World.wallDim, down)));
    }

    // separate thread for watching the bombs
    public synchronized void watchBombs(){
         new Thread(new Runnable() {

            @Override
            public void run() {
                while(true){
                    try{
                        for (int roomNr = 1; roomNr <= mapNumber; roomNr++) {
                            if (bombs.get(roomNr) != null) {
                                ArrayList<BBomb> bombs2 = new ArrayList<BBomb>((ArrayList<BBomb>) bombs.get(roomNr));
                                for (BBomb bomb : bombs2) {
                                    for (Session peer : peers){
                                        if (bomb.getOwner().getId() == peer.getId()){
                                            if (bomb.isVolatileB() && (new Date().getTime() - bomb.getCreationTime().getTime()) / 1000 >= bomb.getLifeTime() && !alreadyMarked(peer, bomb)) {
                                                markForRemove(peer, bomb);
                                            }
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                        Thread.sleep(10);
                    }catch (InterruptedException ex) {
                        BLogger.getInstance().logException2(ex);
                    }catch (IllegalStateException ex) {
                        BLogger.getInstance().logException2(ex);
                    }catch (ConcurrentModificationException ex) {
                        BLogger.getInstance().logException2(ex);
                    }
                }
            }
         }).start();
         
    }
    
    public synchronized void watchPeer(final Session peer) {
        final BombermanWSEndpoint environment = this;
        new Thread(new Runnable() {

            @Override
            public void run() {
                int roomNr = getRoom(peer);
                while (peer.isOpen() && workingThreads.contains(peer.getId())) {
                    isFirst = false;
                    if (peer.getUserProperties().get("room") == null || peer.getUserProperties().get("room").equals(-1)) {
                        peer.getUserProperties().put("room", 1);
                    }
                    BCharacter crtChar = chars.get(peer.getId());
                    
                    if (isTrapped(crtChar, peer)) {
                        crtChar.setState("Trapped"); // will be automated reverted when a bomb kills him >:)
                        charsChanged.put(roomNr, true);
                    }
                    try {
                        // export chars?
                        if (charsChanged.get(roomNr)) {
                            peer.getBasicRemote().sendText("chars:[" + environment.exportChars(peer));
                            charsChanged.put(roomNr, false);
                        }

                        // export map?
                        if (mapChanged.get(roomNr)) {
                            peer.getBasicRemote().sendText("map:[" + environment.exportMap(peer));
                            mapChanged.put(roomNr, false);
                        }

                        // export bombs?
                        if (bombsChanged.get(roomNr)) {
                            peer.getBasicRemote().sendText("bombs:[" + environment.exportBombs(peer));
                            bombsChanged.put(roomNr, false);
                        }

                        // export explosions?
                        if (explosionsChanged.get(roomNr)) {
                            peer.getBasicRemote().sendText("explosions:[" + environment.exportExplosions(peer));
                            explosionsChanged.put(roomNr, false);
                        }

                        if (itemsChanged.get(roomNr)) {
                            peer.getBasicRemote().sendText("items:[" + environment.exportItems(peer));
                            itemsChanged.put(roomNr, false);
                        }

                        Thread.sleep(20); // limiteaza la 100FPS comunicarea cu clientul
                    } catch (InterruptedException ex) {
                        BLogger.getInstance().logException2(ex);
                    } catch (IOException ex) {
                        BLogger.getInstance().logException2(ex);
                    } catch (IllegalStateException ex) {
                        BLogger.getInstance().logException2(ex);
                    } catch (ConcurrentModificationException ex) {
                        BLogger.getInstance().logException2(ex);
                    }
                }
            }
        }).start();
    }

    protected synchronized void detonateBomb(BCharacter myChar, Session peer) {
        int roomNr = getRoom(peer);
        try {
            for (BBomb bomb : bombs.get(roomNr)) {
                if (bomb.getCharId().equals(myChar.getName())) {
                    bomb.setVolatileB(true);
                    markForRemove(peer, bomb);
                    break;
                }
            }
        } catch (ConcurrentModificationException ex) {
            BLogger.getInstance().logException2(ex);
        }
        bombsChanged.put(roomNr, true);
        explosionsChanged.put(roomNr, true);
    }

    protected synchronized void delayedRemove(final String peerId) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000); // wait for a one second before actual removing
                    chars.remove(peerId);
                    System.out.println("final");
                } catch (InterruptedException ex) {
                    BLogger.getInstance().logException2(ex);
                }
            }
        }).start();
    }

    public static boolean wallExists(AbstractBlock[][] data, int i, int j) {
        try {
            AbstractBlock x = data[i][j];
            if (data[i][j] == null) {
                return false;
            }
            if (!AbstractWall.class.isAssignableFrom(data[i][j].getClass())) {
                return false;
            }
            return true;
        } catch (ArrayIndexOutOfBoundsException e) {
            return false;
        }
    }

    public static synchronized boolean bombExists(AbstractBlock[][] data, int i, int j) {
        try {
            AbstractBlock x = data[i][j];
            if (data[i][j] == null) {
                return false;
            }
            if (!BBomb.class.isAssignableFrom(data[i][j].getClass())) {
                return false;
            }
            return true;
        } catch (ArrayIndexOutOfBoundsException e) {
            return false;
        }

    }

    public static synchronized boolean itemExists(AbstractBlock[][] data, int i, int j) {
        try {
            AbstractBlock x = data[i][j];
            if (data[i][j] == null) {
                return false;
            }
            if (!AbstractItem.class.isAssignableFrom(data[i][j].getClass())) {
                return false;
            }
            return true;
        } catch (ArrayIndexOutOfBoundsException e) {
            return false;
        }
    }

    public static synchronized boolean characterExists(Session peer, int i, int j) {
        int roomNr = getRoom(peer);
        return !map.get(roomNr).chars[i][j].isEmpty();
    }

    protected synchronized void triggerBlewCharacter(final Session peer, final int x, final int y) {
        System.out.println("hit...");
        new Thread(new Runnable() {
            @Override
            public void run() {
                int roomNr = getRoom(peer);
                Iterator it = map.get(roomNr).chars[x][y].entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry pairs = (Map.Entry) it.next();
                    revertState(peer, (BCharacter) pairs.getValue());
                    //it.remove(); // avoids a ConcurrentModificationException
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    BLogger.getInstance().logException2(ex);
                }
            }
        }).start();
    }

    protected synchronized void revertState(final Session peer, final BCharacter myChar) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                playSoundAll("sounds/burn.wav");
                int roomNr = getRoom(peer);
                myChar.setState("Blow");
                charsChanged.put(roomNr, true);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    BLogger.getInstance().logException2(ex);
                }
                myChar.setState("Normal");
                charsChanged.put(roomNr, true);
            }
        }).start();
    }

    protected synchronized void markForRemove(final Session peer, final BBomb bomb) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    int roomNr = getRoom(peer);
                    playSoundAll("sounds/explosion.wav");
                    Explosion exp = new Explosion(bomb.getOwner());
                    Set<String> objectHits = Collections.synchronizedSet(new HashSet<String>());
                    map.get(roomNr).blockMatrix[bomb.getPosX() / World.wallDim][bomb.getPosY() / World.wallDim] = null;
                    int charRange = bomb.getOwner().getBombRange();

                    // check if char is in the same position as the character
                    if (bomb.getPosX() + bomb.getWidth() <= World.getWidth() && BombermanWSEndpoint.characterExists(peer, (bomb.getPosX() / World.wallDim), bomb.getPosY() / World.wallDim)) {
                        triggerBlewCharacter(peer, (bomb.getPosX() / World.wallDim), bomb.getPosY() / World.wallDim);
                    }

                    // check if the explosion hits anything within it's range
                    for (int i = 1; i <= charRange; i++) {

                        // right
                        if (bomb.getPosX() + bomb.getWidth() * (i + 1) <= World.getWidth() && BombermanWSEndpoint.bombExists(map.get(roomNr).blockMatrix, (bomb.getPosX() / World.wallDim) + i, bomb.getPosY() / World.wallDim) && !objectHits.contains("right")) {
                            markForRemove(peer, (BBomb) map.get(roomNr).blockMatrix[(bomb.getPosX() / World.wallDim) + i][bomb.getPosY() / World.wallDim]);
                            objectHits.add("right");
                            //System.out.println("hit bomb right");
                        } else if (bomb.getPosX() + bomb.getWidth() * (i + 1) <= World.getWidth() && BombermanWSEndpoint.characterExists(peer, (bomb.getPosX() / World.wallDim) + i, bomb.getPosY() / World.wallDim) && !objectHits.contains("right")) {
                            triggerBlewCharacter(peer, (bomb.getPosX() / World.wallDim) + i, bomb.getPosY() / World.wallDim);
                            //objectHits.add("right");
                            exp.ranges.put("right", exp.ranges.get("right") + 1);
                            //System.out.println("hit character right");
                        } else if (bomb.getPosX() + bomb.getWidth() * (i + 1) <= World.getWidth() && BombermanWSEndpoint.wallExists(map.get(roomNr).blockMatrix, (bomb.getPosX() / World.wallDim) + i, bomb.getPosY() / World.wallDim) && !objectHits.contains("right")) {
                            exp.directions.add("right");
                            //System.out.println("hit wall right");
                            if (((AbstractWall) map.get(roomNr).blockMatrix[(bomb.getPosX() / World.wallDim) + i][bomb.getPosY() / World.wallDim]).isBlowable()) {
                                map.get(roomNr).walls.remove(map.get(roomNr).blockMatrix[(bomb.getPosX() / World.wallDim) + i][bomb.getPosY() / World.wallDim]);
                                exp.ranges.put("right", exp.ranges.get("right") + 1);
                                //map.blockMatrix[(bomb.getPosX()/World.wallDim)+i][bomb.getPosY()/World.wallDim] = null;
                                flipForItems(peer, (bomb.getPosX() / World.wallDim) + i, bomb.getPosY() / World.wallDim);
                                mapChanged.put(roomNr, true);
                            }
                            objectHits.add("right");
                        } else if (bomb.getPosX() + bomb.getWidth() * (i + 1) <= World.getWidth() && BombermanWSEndpoint.itemExists(map.get(roomNr).blockMatrix, (bomb.getPosX() / World.wallDim) + i, bomb.getPosY() / World.wallDim) && !objectHits.contains("right")) {
                            exp.directions.add("right");
                            items.get(roomNr).remove((AbstractItem) map.get(roomNr).blockMatrix[(bomb.getPosX() / World.wallDim) + i][bomb.getPosY() / World.wallDim]);
                            map.get(roomNr).blockMatrix[(bomb.getPosX() / World.wallDim) + i][bomb.getPosY() / World.wallDim] = null;
                            exp.ranges.put("right", exp.ranges.get("right") + 1);
                            objectHits.add("right");
                            itemsChanged.put(roomNr, true);
                        } else if (bomb.getPosX() + bomb.getWidth() * (i + 1) <= World.getWidth() && !BombermanWSEndpoint.wallExists(map.get(roomNr).blockMatrix, (bomb.getPosX() / World.wallDim) + i, bomb.getPosY() / World.wallDim) && !objectHits.contains("right")) {
                            exp.directions.add("right");
                            exp.ranges.put("right", exp.ranges.get("right") + 1);
                            //System.out.println("empty right");
                        }

                        // left
                        if (bomb.getPosX() - bomb.getWidth() * i >= 0 && BombermanWSEndpoint.bombExists(map.get(roomNr).blockMatrix, (bomb.getPosX() / World.wallDim) - i, bomb.getPosY() / World.wallDim) && !objectHits.contains("left")) {
                            markForRemove(peer, (BBomb) map.get(roomNr).blockMatrix[(bomb.getPosX() / World.wallDim) - i][bomb.getPosY() / World.wallDim]);
                            objectHits.add("left");
                            //System.out.println("hit bomb left");
                        } else if (bomb.getPosX() - bomb.getWidth() * i >= 0 && BombermanWSEndpoint.characterExists(peer, (bomb.getPosX() / World.wallDim) - i, bomb.getPosY() / World.wallDim) && !objectHits.contains("left")) {
                            triggerBlewCharacter(peer, (bomb.getPosX() / World.wallDim) - i, bomb.getPosY() / World.wallDim);
                            //objectHits.add("left");
                            exp.ranges.put("left", exp.ranges.get("left") + 1);
                            //System.out.println("hit character left");
                        } else if (bomb.getPosX() - bomb.getWidth() * i >= 0 && BombermanWSEndpoint.wallExists(map.get(roomNr).blockMatrix, (bomb.getPosX() / World.wallDim) - i, bomb.getPosY() / World.wallDim) && !objectHits.contains("left")) {
                            exp.directions.add("left");
                            //System.out.println("hit wall left");
                            if (((AbstractWall) map.get(roomNr).blockMatrix[(bomb.getPosX() / World.wallDim) - i][bomb.getPosY() / World.wallDim]).isBlowable()) {
                                map.get(roomNr).walls.remove(map.get(roomNr).blockMatrix[(bomb.getPosX() / World.wallDim) - i][bomb.getPosY() / World.wallDim]);
                                exp.ranges.put("left", exp.ranges.get("left") + 1);
                                //map.blockMatrix[(bomb.getPosX()/World.wallDim)-i][bomb.getPosY()/World.wallDim] = null;
                                flipForItems(peer, (bomb.getPosX() / World.wallDim) - i, bomb.getPosY() / World.wallDim);
                                mapChanged.put(roomNr, true);
                            }
                            objectHits.add("left");
                        } else if (bomb.getPosX() - bomb.getWidth() * i >= 0 && BombermanWSEndpoint.itemExists(map.get(roomNr).blockMatrix, (bomb.getPosX() / World.wallDim) - i, bomb.getPosY() / World.wallDim) && !objectHits.contains("left")) {
                            exp.directions.add("left");
                            items.get(roomNr).remove((AbstractItem) map.get(roomNr).blockMatrix[(bomb.getPosX() / World.wallDim) - i][bomb.getPosY() / World.wallDim]);
                            map.get(roomNr).blockMatrix[(bomb.getPosX() / World.wallDim) - i][bomb.getPosY() / World.wallDim] = null;
                            exp.ranges.put("left", exp.ranges.get("left") + 1);
                            objectHits.add("left");
                            itemsChanged.put(roomNr, true);
                        } else if (bomb.getPosX() - bomb.getWidth() * i >= 0 && !BombermanWSEndpoint.wallExists(map.get(roomNr).blockMatrix, (bomb.getPosX() / World.wallDim) - i, bomb.getPosY() / World.wallDim) && !objectHits.contains("left")) {
                            exp.directions.add("left");
                            exp.ranges.put("left", exp.ranges.get("left") + 1);
                            //System.out.println("empty left");
                        }

                        // down
                        if (bomb.getPosY() + bomb.getHeight() * (i + 1) <= World.getHeight() && BombermanWSEndpoint.bombExists(map.get(roomNr).blockMatrix, (bomb.getPosX() / World.wallDim), bomb.getPosY() / World.wallDim + i) && !objectHits.contains("down")) {
                            markForRemove(peer, (BBomb) map.get(roomNr).blockMatrix[(bomb.getPosX() / World.wallDim)][bomb.getPosY() / World.wallDim + i]);
                            objectHits.add("down");
                            //System.out.println("hit bomb down");
                        } else if (bomb.getPosY() + bomb.getHeight() * (i + 1) <= World.getHeight() && BombermanWSEndpoint.characterExists(peer, (bomb.getPosX() / World.wallDim), bomb.getPosY() / World.wallDim + i) && !objectHits.contains("down")) {
                            triggerBlewCharacter(peer, (bomb.getPosX() / World.wallDim), bomb.getPosY() / World.wallDim + i);
                            //objectHits.add("down");
                            exp.ranges.put("down", exp.ranges.get("down") + 1);
                            //System.out.println("hit character down");
                        } else if (bomb.getPosY() + bomb.getHeight() * (i + 1) <= World.getHeight() && BombermanWSEndpoint.wallExists(map.get(roomNr).blockMatrix, (bomb.getPosX() / World.wallDim), bomb.getPosY() / World.wallDim + i) && !objectHits.contains("down")) {
                            exp.directions.add("down");
                            //System.out.println("hit wall down");
                            if (((AbstractWall) map.get(roomNr).blockMatrix[(bomb.getPosX() / World.wallDim)][bomb.getPosY() / World.wallDim + i]).isBlowable()) {
                                map.get(roomNr).walls.remove(map.get(roomNr).blockMatrix[(bomb.getPosX() / World.wallDim)][bomb.getPosY() / World.wallDim + i]);
                                exp.ranges.put("down", exp.ranges.get("down") + 1);
                                //map.blockMatrix[(bomb.getPosX()/World.wallDim)][bomb.getPosY()/World.wallDim+i] = null;
                                flipForItems(peer, (bomb.getPosX() / World.wallDim), bomb.getPosY() / World.wallDim + i);
                                mapChanged.put(roomNr, true);
                            }
                            objectHits.add("down");
                        } else if (bomb.getPosY() + bomb.getHeight() * (i + 1) <= World.getHeight() && BombermanWSEndpoint.itemExists(map.get(roomNr).blockMatrix, (bomb.getPosX() / World.wallDim), bomb.getPosY() / World.wallDim + i) && !objectHits.contains("down")) {
                            exp.directions.add("down");
                            items.get(roomNr).remove((AbstractItem) map.get(roomNr).blockMatrix[(bomb.getPosX() / World.wallDim)][bomb.getPosY() / World.wallDim + i]);
                            map.get(roomNr).blockMatrix[(bomb.getPosX() / World.wallDim)][bomb.getPosY() / World.wallDim + i] = null;
                            exp.ranges.put("down", exp.ranges.get("down") + 1);
                            objectHits.add("down");
                            itemsChanged.put(roomNr, true);
                        } else if (bomb.getPosY() + bomb.getHeight() * (i + 1) <= World.getHeight() && !BombermanWSEndpoint.wallExists(map.get(roomNr).blockMatrix, (bomb.getPosX() / World.wallDim), bomb.getPosY() / World.wallDim + i) && !objectHits.contains("down")) {
                            exp.directions.add("down");
                            exp.ranges.put("down", exp.ranges.get("down") + 1);
                            //System.out.println("empty down");
                        }

                        // up
                        if (bomb.getPosY() - bomb.getHeight() * i >= 0 && BombermanWSEndpoint.bombExists(map.get(roomNr).blockMatrix, (bomb.getPosX() / World.wallDim), bomb.getPosY() / World.wallDim - i) && !objectHits.contains("up")) {
                            markForRemove(peer, (BBomb) map.get(roomNr).blockMatrix[(bomb.getPosX() / World.wallDim)][bomb.getPosY() / World.wallDim - i]);
                            objectHits.add("up");
                            //System.out.println("hit bomb up");
                        } else if (bomb.getPosY() - bomb.getHeight() * i >= 0 && BombermanWSEndpoint.characterExists(peer, (bomb.getPosX() / World.wallDim), bomb.getPosY() / World.wallDim - i) && !objectHits.contains("up")) {
                            triggerBlewCharacter(peer, (bomb.getPosX() / World.wallDim), bomb.getPosY() / World.wallDim - i);
                            //objectHits.add("up");
                            exp.ranges.put("up", exp.ranges.get("up") + 1);
                            //System.out.println("hit character up");
                        } else if (bomb.getPosY() - bomb.getHeight() * i >= 0 && BombermanWSEndpoint.wallExists(map.get(roomNr).blockMatrix, (bomb.getPosX() / World.wallDim), bomb.getPosY() / World.wallDim - i) && !objectHits.contains("up")) {
                            exp.directions.add("up");
                            //System.out.println("hit wall up");
                            if (((AbstractWall) map.get(roomNr).blockMatrix[(bomb.getPosX() / World.wallDim)][bomb.getPosY() / World.wallDim - i]).isBlowable()) {
                                map.get(roomNr).walls.remove(map.get(roomNr).blockMatrix[(bomb.getPosX() / World.wallDim)][bomb.getPosY() / World.wallDim - i]);
                                exp.ranges.put("up", exp.ranges.get("up") + 1);
                                //map.blockMatrix[(bomb.getPosX()/World.wallDim)][bomb.getPosY()/World.wallDim-i] = null;
                                flipForItems(peer, (bomb.getPosX() / World.wallDim), bomb.getPosY() / World.wallDim - i);
                                mapChanged.put(roomNr, true);
                            }
                            objectHits.add("up");
                        } else if (bomb.getPosY() - bomb.getHeight() * i >= 0 && BombermanWSEndpoint.itemExists(map.get(roomNr).blockMatrix, (bomb.getPosX() / World.wallDim), bomb.getPosY() / World.wallDim - i) && !objectHits.contains("up")) {
                            exp.directions.add("up");
                            items.get(roomNr).remove((AbstractItem) map.get(roomNr).blockMatrix[(bomb.getPosX() / World.wallDim)][bomb.getPosY() / World.wallDim - i]);
                            map.get(roomNr).blockMatrix[(bomb.getPosX() / World.wallDim)][bomb.getPosY() / World.wallDim - i] = null;
                            exp.ranges.put("up", exp.ranges.get("up") + 1);
                            objectHits.add("up");
                            itemsChanged.put(roomNr, true);
                        } else if (bomb.getPosY() - bomb.getHeight() * i >= 0 && !BombermanWSEndpoint.wallExists(map.get(roomNr).blockMatrix, (bomb.getPosX() / World.wallDim), bomb.getPosY() / World.wallDim - i) && !objectHits.contains("up")) {
                            exp.directions.add("up");
                            exp.ranges.put("up", exp.ranges.get("up") + 1);
                            //System.out.println("empty up");
                        }
                    }

                    if (markedBombs.get(roomNr) == null) {
                        markedBombs.put(roomNr, new HashSet<BBomb>());
                    }
                    markedBombs.get(roomNr).add(bomb);
                    if (explosions.get(roomNr) == null) {
                        explosions.put(roomNr, new HashSet<Explosion>());
                    }
                    explosions.get(roomNr).add(exp);

                    explosionsChanged.put(roomNr, true);
                    bombsChanged.put(roomNr, true);
                    mapChanged.put(roomNr, true);

                    Thread.sleep(200); // wait one second before actual removing
                    explosions.get(roomNr).remove(exp);
                    markedBombs.get(roomNr).remove(bomb);
                    bombs.get(roomNr).remove(bomb);

                    explosionsChanged.put(roomNr, true);
                    bombsChanged.put(roomNr, true);
                    mapChanged.put(roomNr, true);

                } catch (Exception ex) {
                    BLogger.getInstance().logException2(ex);
                }
            }
        }).start();
    }

    protected synchronized void flipForItems(Session peer, int x, int y) {
        int rand = (int) (Math.random() * 1000000);
        int roomNr = getRoom(peer);
        if (rand % 2 == 0) { // about 50% chance to find a hidden item behind the wall ;))
            if (wallExists(map.get(roomNr).blockMatrix, x, y)) {
                AbstractItem item = ItemGenerator.getInstance().generateRandomItem();
                item.setPosX(map.get(roomNr).blockMatrix[x][y].getPosX());
                item.setPosY(map.get(roomNr).blockMatrix[x][y].getPosY());
                map.get(roomNr).blockMatrix[x][y] = null;
                map.get(roomNr).blockMatrix[x][y] = item;
                if (this.items.get(roomNr) == null) {
                    this.items.put(roomNr, new HashSet<AbstractItem>());
                }
                items.get(roomNr).add(item);
                itemsChanged.put(roomNr, true);
            }
            else{
                map.get(roomNr).blockMatrix[x][y] = null;
            }
        } else {
            map.get(roomNr).blockMatrix[x][y] = null; // remove the block
        }
        mapChanged.put(roomNr, true);
    }

    protected synchronized boolean alreadyMarked(Session peer, BBomb bomb) {
        boolean ret = false;
        int roomNr = getRoom(peer);
        if (markedBombs.get(roomNr) == null) {
            markedBombs.put(roomNr, new HashSet<BBomb>());
        }
        ret = markedBombs.get(roomNr).contains(bomb);
        return ret;
    }

    protected String exportChars(Session peer) {
        //BLogger.getInstance().log(BLogger.LEVEL_FINE, "exporting chars...");
        String ret = "";
        ret += peer.getId() + "[#chars#]";
        HashSet<Session> peers2 = new HashSet<Session>(peers);
        for (Session peer2 : peers2) {
            if (getRoom(peer2) == getRoom(peer)) {
                ret += chars.get(peer2.getId()).toString() + "[#charSep#]";
            }
        }
        //BLogger.getInstance().log(BLogger.LEVEL_FINE, "exported chars...");
        return ret;
    }

    protected String exportMap(Session peer) {
        String ret = "";
        //BLogger.getInstance().log(BLogger.LEVEL_FINE, "exporting map...");
        int roomNr = getRoom(peer);
        ret = map.get(roomNr).toString();
        //BLogger.getInstance().log(BLogger.LEVEL_FINE, "exported map...");
        return ret;
    }

    protected String exportBombs(Session peer) {
        String ret = "";
        int roomNr = getRoom(peer);
        //BLogger.getInstance().log(BLogger.LEVEL_FINE, "exporting bombs...");
        if (bombs.get(roomNr) != null) {
            ArrayList<BBomb> bombs2 = new ArrayList<BBomb>((ArrayList<BBomb>) bombs.get(roomNr));
            for (BBomb bomb : bombs2) {
                if (bomb.isVolatileB() && (new Date().getTime() - bomb.getCreationTime().getTime()) / 1000 >= bomb.getLifeTime() && !alreadyMarked(peer, bomb)) {
                    markForRemove(peer, bomb);
                    continue;
                } else {
                    ret += bomb.toString() + "[#bombSep#]";
                }
            }
        }
        //BLogger.getInstance().log(BLogger.LEVEL_FINE, "exported bombs...");
        return ret;
    }

    protected String exportExplosions(Session peer) {
        String ret = "";
        //BLogger.getInstance().log(BLogger.LEVEL_FINE, "exporting explosions...");
        int roomNr = getRoom(peer);
        if (explosions.get(roomNr) != null) {
            HashSet<Explosion> explosions2 = new HashSet<Explosion>(explosions.get(roomNr));
            for (Explosion exp : explosions2) {
                ret += exp.toString() + "[#explosionSep#]";
            }
        }
        //BLogger.getInstance().log(BLogger.LEVEL_FINE, "exported explosions...");
        return ret;
    }

    protected String exportItems(Session peer) {
        String ret = "";
        //BLogger.getInstance().log(BLogger.LEVEL_FINE, "exporting items...");
        int roomNr = getRoom(peer);
        if (items.get(roomNr) != null) {
            HashSet<AbstractItem> items2 = new HashSet<AbstractItem>(items.get(roomNr));
            for (AbstractItem item : items2) {
                ret += item.toString() + "[#itemSep#]";
            }
            //BLogger.getInstance().log(BLogger.LEVEL_FINE, "exported items...");
        }
        return ret;
    }

    public boolean canPlantNewBomb(Session peer, BCharacter crtChar) {
        int maxBombs = crtChar.getMaxBombs();
        int plantedBombs = 0;
        int roomNr = getRoom(peer);
        if (bombs.get(roomNr) != null) {
            HashSet<BBomb> bombs2 = new HashSet<BBomb>(bombs.get(roomNr));
            for (BBomb bomb : bombs2) {
                if (bomb.getOwner().getId() == crtChar.getId()) {
                    plantedBombs++;
                }
            }
        }
        return (plantedBombs < maxBombs);
    }

    public static synchronized String checkWorldMatrix(AbstractBlock[][] data, int i, int j) {
        String ret = "";
        try {
            AbstractBlock x = data[i][j];
            if (data[i][j] == null) {
                return "blank";
            } else if (AbstractWall.class.isAssignableFrom(data[i][j].getClass())) {
                return "wall";
            } else if (BBomb.class.isAssignableFrom(data[i][j].getClass())) {
                return "bomb";
            } else if (BCharacter.class.isAssignableFrom(data[i][j].getClass())) {
                return "char";
            }
            return "else";
        } catch (ArrayIndexOutOfBoundsException e) {
            return "out of bounds";
        }
    }

    public synchronized void playSound(String sound, Session peer) {
        try {
            peer.getBasicRemote().sendText("sound:[" + sound);
        } catch (IOException ex) {
            BLogger.getInstance().logException2(ex);
        } catch (IllegalStateException ex) {
            BLogger.getInstance().logException2(ex);
        } catch (ConcurrentModificationException ex) {
            BLogger.getInstance().logException2(ex);
        }
    }

    public synchronized void playSoundAll(String sound) {
        HashSet<Session> peers2 = new HashSet<Session>(peers);

        for (Session peer : peers2) {
            playSound(sound, peer);
        }
    }

    private static synchronized int getRoom(Session peer) {
        int roomNr = Integer.parseInt(peer.getUserProperties().get("room").toString());
        if(!peerRooms.containsKey(peer.getId())){
            peerRooms.put(peer.getId(), roomNr);
        }
        return peerRooms.get(peer.getId());
    }
}
