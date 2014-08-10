/**
 * Query-uri pentru baza de date.
 * CREATE DATABASE `bomberman`;
 * CREATE TABLE `chat_message` (
 `id` int(11) NOT NULL AUTO_INCREMENT,
 `peer_id` varchar(128) NOT NULL,
 `peer_name` varchar(128) NOT NULL,
 `message_time` DATETIME NOT NULL,
 `message` TEXT NOT NULL,
 PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
 */
package com.cux.bomberman;

import com.cux.bomberman.util.BLogger;
import com.cux.bomberman.world.AbstractBlock;
import com.cux.bomberman.world.BBomb;
import com.cux.bomberman.world.BCharacter;
import com.cux.bomberman.world.Explosion;
import com.cux.bomberman.world.World;
import com.cux.bomberman.world.generator.ItemGenerator;
import com.cux.bomberman.world.generator.WorldGenerator;
import com.cux.bomberman.world.items.AbstractItem;
import com.cux.bomberman.world.walls.AbstractWall;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
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

    public static final Map<String, Session> peers = Collections.synchronizedMap(new HashMap<String, Session>());

    private static final Map<Integer, ArrayList<BBomb>> bombs = Collections.synchronizedMap(new HashMap<Integer, ArrayList<BBomb>>());

    private static final Map<Integer, Set<BBomb>> markedBombs = Collections.synchronizedMap(new HashMap<Integer, Set<BBomb>>());

    private static final Map<String, BCharacter> chars = Collections.synchronizedMap(new HashMap<String, BCharacter>());
    
    private static final Map<Integer, Set<BCharacter>> chars2 = Collections.synchronizedMap(new HashMap<Integer, Set<BCharacter>>());

    private static final Set<String> workingThreads = Collections.synchronizedSet(new HashSet<String>());

    private static final Map<Integer, Set<Explosion>> explosions = Collections.synchronizedMap(new HashMap<Integer, Set<Explosion>>());
    
    private static final Map<Integer, Set<String>> blownWalls = Collections.synchronizedMap(new HashMap<Integer, Set<String>>());
    
    public static Map<Integer, Set<AbstractItem>> items = Collections.synchronizedMap(new HashMap<Integer, Set<AbstractItem>>());

    private static boolean isFirst = true;

    public static Map<Integer, World> map = Collections.synchronizedMap(new HashMap<Integer, World>());

    private final static int MAX_PLAYERS = 6;

    private static final Map<Integer, Integer> mapPlayers = Collections.synchronizedMap(new HashMap<Integer, Integer>());

    private static int mapNumber = 0;

    public static final Map<Integer, Boolean> charsChanged = Collections.synchronizedMap(new HashMap<Integer, Boolean>());
    public static final Map<Integer, Boolean> mapChanged = Collections.synchronizedMap(new HashMap<Integer, Boolean>());
    public static final Map<Integer, Boolean> wallsChanged = Collections.synchronizedMap(new HashMap<Integer, Boolean>());
    public static final Map<Integer, Boolean> bombsChanged = Collections.synchronizedMap(new HashMap<Integer, Boolean>());
    public static final Map<Integer, Boolean> explosionsChanged = Collections.synchronizedMap(new HashMap<Integer, Boolean>());
    public static final Map<Integer, Boolean> itemsChanged = Collections.synchronizedMap(new HashMap<Integer, Boolean>());

    public static final Map<String, Integer> peerRooms = Collections.synchronizedMap(new HashMap<String, Integer>());
    
    private static boolean initialized = false;
    
    private static BombermanWSEndpoint instance = null;
    
    private static Connection con = null;
    
    private static final String DBConnectionString = "jdbc:mysql://localhost:3306/";
    private static final String DBName = "bomberman";
    private static final String DBUser = "root";
    private static final String DBPass = "P@sswordcux";
            
    public Connection getConnection(){
        return BombermanWSEndpoint.con;
    }
    
    public static BombermanWSEndpoint getInstance(){
        return BombermanWSEndpoint.instance;
    }
    
    @OnMessage
    public synchronized String onMessage(String message, final Session peer) {

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
                    final BBomb b = new BBomb(crtChar);
                    if (bombExists(map.get(roomNr).blockMatrix, b.getPosX() / World.wallDim, b.getPosY() / World.wallDim)){
                        break;
                    }
                    this.bombs.get(roomNr).add(b);
                    map.get(roomNr).blockMatrix[b.getPosX() / World.wallDim][b.getPosY() / World.wallDim] = b;
                    //crtChar.incPlantedBombs();
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
            case "ready":
                makePlayerReady(peer);
                break;
            case "QUIT":
                this.onClose(peer);
            default:
                break;
        }
        
        String namePattern = "name ([a-zA-Z0-9. ]+)";
        Pattern p = Pattern.compile(namePattern);
        Matcher m = p.matcher(message);
        if (m.matches()){
            String name = message.substring(message.indexOf(" ")).trim();
            if (name.length() > 0){
                String initialName = chars.get(peer.getId()).getName();
                chars.get(peer.getId()).setName(name);
                sendMessageAll(roomNr, "<b>"+initialName+" is now known as <u>"+name+"</u> </b>");
            }
        }        
        
        if (message.length() > 4 && message.substring(0, 4).toLowerCase().equals("msg ")){
            System.out.println("message for chat");
            String msg = message.substring(message.indexOf(" ")).trim();
            if (msg.length() > 0){
                logChatMessage(chars.get(peer.getId()), msg);
                sendMessageAll(roomNr, "<b>"+chars.get(peer.getId()).getName()+" : </b>"+msg);
            }
        }
        
        //System.out.println(message);
        return null; // any string will be send to the requesting peer
    }

    @OnClose
    public synchronized void onClose(Session peer) {
        this.delayedRemove(peer.getId());
        this.stopThread(peer.getId());
        int roomNr = getRoom(peer);
        String initialName = chars.get(peer.getId()).getName();
        chars2.get(roomNr).remove(chars.get(peer.getId()));
        chars.remove(peer.getId());
        peers.remove(peer);
        if (peer.isOpen()) {
            try {
                peer.close();
            } catch (IOException ex) {
                BLogger.getInstance().logException2(ex);
            }
        }
        charsChanged.put(roomNr, true);
        //mapChanged.put(roomNr, true);
        mapPlayers.put(roomNr, mapPlayers.get(roomNr) - 1);
        System.out.println("out...");
//        if (peers.size() == 0){
//            BombermanWSEndpoint.initialized = false;
//        }
        sendMessageAll(roomNr, "<b>ELVIS [ "+initialName+" ]  has left the building </b>");
    }

    @OnOpen
    public synchronized void onOpen(Session peer, @PathParam("room") final String room) {

        peers.put(peer.getId(), peer);

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
//            map.put(mapNumber, new World("/home/mihaicux/projects/bomberman/maps/firstmap.txt"));
//            map.put(mapNumber, new World("/home/mihaicux/NetBeansProjects/bomberman/maps/firstmap.txt"));
            map.put(mapNumber, new World("/home/mihaicux/projects/bomberman/maps/map2.txt"));
//            map.put(mapNumber, WorldGenerator.getInstance().generateWorld(3000, 1800, 1200));
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

        setCharPosition(mapNumber, newChar);
        
        if (blownWalls.size() == 0 || blownWalls.get(mapNumber) == null) {
            blownWalls.put(mapNumber, new HashSet<String>());
        }

        if (markedBombs.size() == 0 || markedBombs.get(mapNumber) == null) {
            markedBombs.put(mapNumber, new HashSet<BBomb>());
        }
        
        if (chars2.size() == 0 || chars2.get(mapNumber) == null){
            chars2.put(mapNumber, new HashSet<BCharacter>());
        }
        
        if (explosions.size() == 0 || explosions.get(mapNumber) == null) {
            explosions.put(mapNumber, new HashSet<Explosion>());
        }
        
        if (items.size() == 0 || items.get(mapNumber) == null) {
            items.put(mapNumber, new HashSet<AbstractItem>());
        }
        
        if (bombs.size() == 0 || bombs.get(mapNumber) == null) {
            bombs.put(mapNumber, new ArrayList<BBomb>());
        }

        if (wallsChanged.size() == 0 || wallsChanged.get(mapNumber) == null){
            wallsChanged.put(mapNumber, true);
        }
        
        chars2.get(mapNumber).add(newChar);
        
        BLogger.getInstance().log(BLogger.LEVEL_INFO, "peer connected [" + peer.getId() + "], room " + peer.getUserProperties().get("room"));
        //System.exit(0);
        charsChanged.put(mapNumber, true);
        bombsChanged.put(mapNumber, true);
        mapChanged.put(mapNumber, true);
        itemsChanged.put(mapNumber, true);
        explosionsChanged.put(mapNumber, true);
        
        if (!BombermanWSEndpoint.initialized){
            watchBombs();
            watchPeers();
            BombermanWSEndpoint.initialized = true;
            BombermanWSEndpoint.instance = this;
            try{
                Class.forName("com.mysql.jdbc.Driver").newInstance();
                BombermanWSEndpoint.con = DriverManager.getConnection(BombermanWSEndpoint.DBConnectionString+BombermanWSEndpoint.DBName,
                    BombermanWSEndpoint.DBUser, BombermanWSEndpoint.DBPass);
                if (!con.isClosed()) {
                    BLogger.getInstance().log(BLogger.LEVEL_FINE, "Connected to MySQL Database...");
                }
            } catch (Exception e) {
                BLogger.getInstance().logException2(e);
            }
        }
        
        sendMessageAll(mapNumber, "<b>"+newChar.getName()+" has joined");
        
    }

    @OnError
    public synchronized void onError(Throwable t) {
    }

    public synchronized void exportEnvironment(Session peer) {
        int roomNr = getRoom(peer);
        try {
            //if (charsChanged.get(roomNr)){
            //peer.getBasicRemote().sendText("chars:[" + exportChars(peer));
            exportChars(peer);
            //}
            //if (mapChanged.get(roomNr)){
            //peer.getBasicRemote().sendText("map:[" + exportMap(peer));
            exportMap(peer);
            //}
            //if (bombsChanged.get(roomNr)){
            //peer.getBasicRemote().sendText("bombs:[" + exportBombs(peer));
            exportBombs(peer);
            //}
            //if (explosionsChanged.get(roomNr)){
            //peer.getBasicRemote().sendText("explosions:[" + exportExplosions(peer));
            exportExplosions(peer);
            //}
            //if (itemsChanged.get(roomNr)){
            //peer.getBasicRemote().sendText("items:[" + exportItems(peer));
            exportItems(peer);
            //}
        /*} catch (IOException ex) {
            BLogger.getInstance().logException2(ex);*/
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
        if (crtChar == null) return false;
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
    public  void watchBombs(){
         new Thread(new Runnable() {

            @Override
            public  void run() {
                while(true){
                    try{
                        for (int roomNr = 1; roomNr <= mapNumber; roomNr++) {
                            if (bombs.get(roomNr) != null) {
                                ArrayList<BBomb> bombs2 = new ArrayList<BBomb>((ArrayList<BBomb>) bombs.get(roomNr));
                                for (BBomb bomb : bombs2) {
                                    if (bomb == null) continue;
                                    Session peer = peers.get(bomb.getOwner().getId());
                                    if (bomb.isVolatileB() && (new Date().getTime() - bomb.getCreationTime().getTime()) / 1000 >= bomb.getLifeTime() && !alreadyMarked(peer, bomb)) {
                                        markForRemove(peer, bomb);
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
    
    public  void watchPeers() {
        final BombermanWSEndpoint environment = this;
        new Thread(new Runnable() {

            @Override
            public synchronized void run() {
                while (true){
                    try{
                        int max = mapNumber+1;
                        boolean[] charChanged = new boolean[max];
                        boolean[] map2Changed = new boolean[max];
                        boolean[] bombChanged = new boolean[max];
                        boolean[] explosionChanged = new boolean[max];
                        boolean[] itemChanged = new boolean[max];
                        boolean[] wallChanged = new boolean[max];
                        
                        Iterator it = peers.entrySet().iterator();
                        while (it.hasNext()) {
                            Map.Entry pairs = (Map.Entry) it.next();
                            Session peer = (Session) pairs.getValue();
                        
                            if (peer.isOpen() && workingThreads.contains(peer.getId())){
                                if (peer.getUserProperties().get("room") == null || peer.getUserProperties().get("room").equals(-1)) {
                                    peer.getUserProperties().put("room", 1);
                                }
                                BCharacter crtChar = chars.get(peer.getId());
                                
                                int roomNr = getRoom(peer);
                                if (isTrapped(crtChar, peer)) {
                                    crtChar.setState("Trapped"); // will be automated reverted when a bomb kills him >:)
                                    charsChanged.put(roomNr, true);
                                }
                                try {
                                    // export chars?
                                    //if (charsChanged.get(roomNr)) {
                                    
                                        //peer.getBasicRemote().sendText("chars:[" + environment.exportChars(peer));
                                        environment.exportChars(peer);
                                    //    charChanged[roomNr] = true;
                                    //}

                                    // export map?
                                    if (mapChanged.get(roomNr)) {
//                                        peer.getBasicRemote().sendText("map:[" + environment.exportMap(peer));
                                        environment.exportMap(peer);
                                        map2Changed[roomNr] = true;
                                    }

                                    // export walls?
                                    if (wallsChanged.get(roomNr)){
                                        //peer.getBasicRemote().sendText("blownWalls:[" + environment.exportWalls(peer));
                                        exportWalls(peer);
                                        wallChanged[roomNr] = true;
                                    }
                                    
                                    // export bombs?
                                    if (bombsChanged.get(roomNr)) {
                                        //peer.getBasicRemote().sendText("bombs:[" + environment.exportBombs(peer));
                                        environment.exportBombs(peer);
                                        bombChanged[roomNr] = true;
                                    }
                                    
                                    // export explosions?
                                    if (explosionsChanged.get(roomNr)) {
                                        //peer.getBasicRemote().sendText("explosions:[" + environment.exportExplosions(peer));
                                        exportExplosions(peer);
                                        explosionChanged[roomNr] = true;
                                    }

                                    // eport items?
                                    if (itemsChanged.get(roomNr)) {
                                        //peer.getBasicRemote().sendText("items:[" + environment.exportItems(peer));
                                        environment.exportItems(peer);
                                        itemChanged[roomNr] = true;
                                    }
                                /*} catch (IOException ex) {
                                    BLogger.getInstance().logException2(ex);*/
                                } catch (IllegalStateException ex) {
                                    BLogger.getInstance().logException2(ex);
                                } catch (ConcurrentModificationException ex) {
                                    BLogger.getInstance().logException2(ex);
                                } catch (RuntimeException ex){
                                    BLogger.getInstance().logException2(ex);
                                } 
                            }
                        }
                        for (int i = 1; i <= mapNumber; i++){
                            //if (charChanged[i]){
                                //charsChanged.put(i, false);
                            //}
                            try{
                                if (map2Changed[i]){
                                    mapChanged.put(i, false);
                                }
                                if (bombChanged[i]){
                                    bombsChanged.put(i, false);
                                }
                                if (explosionChanged[i]){
                                    explosionsChanged.put(i, false);
                                }
                                if (itemChanged[i]){
                                    itemsChanged.put(i, false);
                                }
                                if (wallChanged[i]){
                                    wallsChanged.put(i, false);
                                    blownWalls.get(i).clear();
                                }
                            }
                            catch (ArrayIndexOutOfBoundsException e){
                                BLogger.getInstance().logException2(e);
                            }
                        }
                        Thread.sleep(10); // limiteaza la 100FPS comunicarea cu clientul
                    }
                    catch(InterruptedException ex){
                        BLogger.getInstance().logException2(ex);
                    }
                    catch ( java.util.ConcurrentModificationException ex){
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
            public synchronized void run() {
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

    public static synchronized boolean wallExists(AbstractBlock[][] data, int i, int j) {
        if (i < 0 || j < 0) return false;
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
            BLogger.getInstance().logException2(e);
            return false;
        }
    }

    public static synchronized boolean bombExists(AbstractBlock[][] data, int i, int j) {
        if (i < 0 || j < 0) return false;
        if (data == null) return false; 
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
            BLogger.getInstance().logException2(e);
            return false;
        }

    }

    public static synchronized boolean itemExists(AbstractBlock[][] data, int i, int j) {
        if (i < 0 || j < 0) return false;
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
            BLogger.getInstance().logException2(e);
            return false;
        }
    }

    public static synchronized boolean characterExists(Session peer, int i, int j) {
        if (i < 0 || j < 0) return false;
        int roomNr = getRoom(peer);
        World world = map.get(roomNr);
        if (world == null) return false;
        return !world.chars[i][j].isEmpty();
    }

    protected synchronized void triggerBlewCharacter(final Session peer, final int x, final int y) {
        System.out.println("hit...");
        new Thread(new Runnable() {
            @Override
            public synchronized void run() {
                BCharacter winner = chars.get(peer.getId());
                if (winner == null) return;
                int roomNr = getRoom(peer);
                Iterator it = map.get(roomNr).chars[x][y].entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry pairs = (Map.Entry) it.next();
                    BCharacter looser = (BCharacter) pairs.getValue();
                    if (looser.getReady()){ // change game stats only if the character within a bomb range is ready to play
                        looser.incDeaths();
                        winner.incKills();
                        winner.setState("Win");
                        if (looser.equals(winner)){
                            looser.decKills(); // first, revert the initial kill
                            looser.decKills(); // second, "steal" one of the user kills (suicide is a crime)
                        }
                        revertState(peer, looser);
                    }
                    //it.remove(); // avoids a ConcurrentModificationException
                }
                try {
                    Thread.sleep(1000);
                    winner.setState("Normal");
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
                int roomNr = getRoom(peer);
                playSoundAll(roomNr, "sounds/burn.wav");
                myChar.setState("Blow");
                charsChanged.put(roomNr, true);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    BLogger.getInstance().logException2(ex);
                }
                myChar.setReady(false);
                new Thread(new Runnable() {

                    @Override
                    public void run() {
                        try {
                            Thread.sleep(3000);
                        } catch (InterruptedException ex) {
                            BLogger.getInstance().logException2(ex);
                        }
                        myChar.setReady(true);
                    }
                }).start();
                myChar.setState("Normal");
                setCharPosition(roomNr, myChar);
                charsChanged.put(roomNr, true);
            }
        }).start();
    }

    public synchronized void markForRemove(final Session peer, final BBomb bomb) {
        
        if (bomb == null) return;
        
        new Thread(new Runnable() {
            @Override
            public synchronized void run() {
                
                try {
                    final int roomNr = getRoom(peer);
                    playSoundAll(roomNr, "sounds/explosion.wav");
                    final Explosion exp = new Explosion(bomb.getOwner());
                    Set<String> objectHits = Collections.synchronizedSet(new HashSet<String>());
                    map.get(roomNr).blockMatrix[bomb.getPosX() / World.wallDim][bomb.getPosY() / World.wallDim] = null;
                    int charRange = bomb.getOwner().getBombRange();

                    markedBombs.get(roomNr).add(bomb);
                    
                    //bomb.getOwner().decPlantedBombs();
                    
                    explosions.get(roomNr).add(exp);

                    explosionsChanged.put(roomNr, true);
                    bombsChanged.put(roomNr, true);
                    //mapChanged.put(roomNr, true);
                    
                    new Thread(new Runnable() {
                        @Override
                        public synchronized void run() {
                            try {
                                Thread.sleep(100); // wait .1 second before actual removing
                                explosions.get(roomNr).remove(exp);
                                markedBombs.get(roomNr).remove(bomb);
                                bombs.get(roomNr).remove(bomb);
                                explosionsChanged.put(roomNr, true);
                                bombsChanged.put(roomNr, true);
                                //mapChanged.put(roomNr, true);
                            } catch (InterruptedException ex) {
                                BLogger.getInstance().logException2(ex);
                            }
                        }
                    }).start();
                    
                    // check if char is in the same position as the bomb
                    if (bomb.getPosX() + bomb.getWidth() <= World.getWidth() && BombermanWSEndpoint.characterExists(peer, (bomb.getPosX() / World.wallDim), bomb.getPosY() / World.wallDim)) {
                        triggerBlewCharacter(peer, (bomb.getPosX() / World.wallDim), bomb.getPosY() / World.wallDim);
                    }

                    int posX = bomb.getPosX();
                    int posY = bomb.getPosY();
                    int width = bomb.getWidth();
                    int height = bomb.getHeight();
                    int wWidth = World.getWidth();
                    int wHeight = World.getHeight();
                    int blockX = posX / World.wallDim;
                    int blockY = posY / World.wallDim;
                    
                    // check if the explosion hits anything within it's range
                    // in it's position                    
                    
                    for (int i = 1; i <= charRange; i++) { // in it's range
                        
                        // right
                        final int xR = blockX + i;
                        final int yR = blockY;
                        String checkedRight = BombermanWSEndpoint.checkWorldMatrix(roomNr, xR, yR);
                        
                        if (posX + width * (i + 1) <= wWidth && checkedRight.equals("bomb") && !objectHits.contains("right")) {
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    markForRemove(peer, (BBomb) map.get(roomNr).blockMatrix[xR][yR]);
                                }
                            }).start();
                            objectHits.add("right");
                            //System.out.println("hit bomb right");
                        } else if (posX + width * (i + 1) <= wWidth &&  checkedRight.equals("char") && !objectHits.contains("right")) {
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    triggerBlewCharacter(peer, xR, yR);
                                }
                            }).start();
                            //objectHits.add("right");
                            exp.ranges.put("right", exp.ranges.get("right") + 1);
                            //System.out.println("hit character right");
                        } else if (posX + width * (i + 1) <= wWidth && checkedRight.equals("wall") && !objectHits.contains("right")) {
                            exp.directions.add("right");
                            //System.out.println("hit wall right");
                            AbstractWall wall = ((AbstractWall) map.get(roomNr).blockMatrix[xR][yR]);
                            if (wall.isBlowable()) {
                                map.get(roomNr).walls.remove(map.get(roomNr).blockMatrix[xR][yR]);
                                exp.ranges.put("right", exp.ranges.get("right") + 1);
                                flipForItems(peer, xR, yR);
                                //mapChanged.put(roomNr, true);
                                blownWalls.get(roomNr).add(wall.wallId);
                                wallsChanged.put(roomNr, true);
                            }
                            objectHits.add("right");
                        } else if (posX + width * (i + 1) <= wWidth && checkedRight.equals("item") && !objectHits.contains("right")) {
                            exp.directions.add("right");
                            items.get(roomNr).remove((AbstractItem) map.get(roomNr).blockMatrix[xR][yR]);
                            map.get(roomNr).blockMatrix[xR][yR] = null;
                            exp.ranges.put("right", exp.ranges.get("right") + 1);
                            objectHits.add("right");
                            itemsChanged.put(roomNr, true);
                        } else if (posX + width * (i + 1) <= wWidth && !checkedRight.equals("wall") && !objectHits.contains("right")) {
                            exp.directions.add("right");
                            exp.ranges.put("right", exp.ranges.get("right") + 1);
                            //System.out.println("empty right");
                        }

                        // left
                        final int xL = blockX - i;
                        final int yL = blockY;
                        String checkedLeft = BombermanWSEndpoint.checkWorldMatrix(roomNr, xL, yL);
                        
                        /*
                        boolean bombExistsLeft = BombermanWSEndpoint.bombExists(map.get(roomNr).blockMatrix, xL, yL);
                        boolean charExistsLeft = BombermanWSEndpoint.characterExists(peer, xL, yL);
                        boolean wallExistsLeft = BombermanWSEndpoint.wallExists(map.get(roomNr).blockMatrix, xL, yL);
                        boolean itemExistsLeft = BombermanWSEndpoint.itemExists(map.get(roomNr).blockMatrix, xL, yL);
                        */
                        
                        if (bomb.getPosX() - bomb.getWidth() * i >= 0 && checkedLeft.equals("bomb") && !objectHits.contains("left")) {
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    markForRemove(peer, (BBomb) map.get(roomNr).blockMatrix[xL][yL]);
                                }
                            }).start();
                            objectHits.add("left");
                            //System.out.println("hit bomb left");
                        } else if (posX - width * i >= 0 && checkedLeft.equals("char") && !objectHits.contains("left")) {
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    triggerBlewCharacter(peer, xL, yL);
                                }
                            }).start();
                            //objectHits.add("left");
                            exp.ranges.put("left", exp.ranges.get("left") + 1);
                            //System.out.println("hit character left");
                        } else if (posX - width * i >= 0 && checkedLeft.equals("wall") && !objectHits.contains("left")) {
                            exp.directions.add("left");
                            //System.out.println("hit wall left");
                            AbstractWall wall = ((AbstractWall) map.get(roomNr).blockMatrix[xL][yL]);
                            if (wall.isBlowable()) {
                                map.get(roomNr).walls.remove(map.get(roomNr).blockMatrix[xL][yL]);
                                exp.ranges.put("left", exp.ranges.get("left") + 1);
                                flipForItems(peer, xL, yL);
//                                mapChanged.put(roomNr, true);
                                blownWalls.get(roomNr).add(wall.wallId);
                                wallsChanged.put(roomNr, true);
                            }
                            objectHits.add("left");
                        } else if (posX - width * i >= 0 && checkedLeft.equals("item") && !objectHits.contains("left")) {
                            exp.directions.add("left");
                            items.get(roomNr).remove((AbstractItem) map.get(roomNr).blockMatrix[xL][yL]);
                            map.get(roomNr).blockMatrix[xL][yL] = null;
                            exp.ranges.put("left", exp.ranges.get("left") + 1);
                            objectHits.add("left");
                            itemsChanged.put(roomNr, true);
                        } else if (posX - width * i >= 0 && !checkedLeft.equals("wall") && !objectHits.contains("left")) {
                            exp.directions.add("left");
                            exp.ranges.put("left", exp.ranges.get("left") + 1);
                            //System.out.println("empty left");
                        }

                        // down
                        final int xD = blockX;
                        final int yD = blockY + i;
                        String checkedDown = BombermanWSEndpoint.checkWorldMatrix(roomNr, xD, yD);
                        
                        /*
                        boolean bombExistsDown = BombermanWSEndpoint.bombExists(map.get(roomNr).blockMatrix, xD, yD);
                        boolean charExistsDown = BombermanWSEndpoint.characterExists(peer, xD, yD);
                        boolean wallExistsDown = BombermanWSEndpoint.wallExists(map.get(roomNr).blockMatrix, xD, yD);
                        boolean itemExistsDown = BombermanWSEndpoint.itemExists(map.get(roomNr).blockMatrix, xD, yD);
                        */
                        
                        if (posY + height * (i + 1) <= wHeight && checkedDown.equals("bomb") && !objectHits.contains("down")) {
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    markForRemove(peer, (BBomb) map.get(roomNr).blockMatrix[xD][yD]);
                                }
                            }).start();
                            objectHits.add("down");
                            //System.out.println("hit bomb down");
                        } else if (posY + height * (i + 1) <= wHeight && checkedDown.equals("char") && !objectHits.contains("down")) {
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    triggerBlewCharacter(peer, xD, yD);                                    
                                }
                            }).start();
                            //objectHits.add("down");
                            exp.ranges.put("down", exp.ranges.get("down") + 1);
                            //System.out.println("hit character down");
                        } else if (posY + height * (i + 1) <= wHeight && checkedDown.equals("wall") && !objectHits.contains("down")) {
                            exp.directions.add("down");
                            //System.out.println("hit wall down");
                            AbstractWall wall = ((AbstractWall) map.get(roomNr).blockMatrix[xD][yD]);
                            if (wall.isBlowable()) {
                                map.get(roomNr).walls.remove(map.get(roomNr).blockMatrix[xD][yD]);
                                exp.ranges.put("down", exp.ranges.get("down") + 1);
                                flipForItems(peer, xD, yD);
//                                mapChanged.put(roomNr, true);
                                blownWalls.get(roomNr).add(wall.wallId);
                                wallsChanged.put(roomNr, true);
                            }
                            objectHits.add("down");
                        } else if (posY + height * (i + 1) <= wHeight && checkedDown.equals("item") && !objectHits.contains("down")) {
                            exp.directions.add("down");
                            items.get(roomNr).remove((AbstractItem) map.get(roomNr).blockMatrix[xD][yD]);
                            map.get(roomNr).blockMatrix[xD][yD] = null;
                            exp.ranges.put("down", exp.ranges.get("down") + 1);
                            objectHits.add("down");
                            itemsChanged.put(roomNr, true);
                        } else if (posY + height * (i + 1) <= wHeight && !checkedDown.equals("wall") && !objectHits.contains("down")) {
                            exp.directions.add("down");
                            exp.ranges.put("down", exp.ranges.get("down") + 1);
                            //System.out.println("empty down");
                        }

                        // up
                        final int xU = blockX;
                        final int yU = blockY - i;
                        String checkedUp = BombermanWSEndpoint.checkWorldMatrix(roomNr, xU, yU);
                        
                        /*
                        boolean bombExistsUp = BombermanWSEndpoint.bombExists(map.get(roomNr).blockMatrix, xU, yU);
                        boolean charExistsUp = BombermanWSEndpoint.characterExists(peer, xU, yU);
                        boolean wallExistsUp = BombermanWSEndpoint.wallExists(map.get(roomNr).blockMatrix, xU, yU);
                        boolean itemExistsUp = BombermanWSEndpoint.itemExists(map.get(roomNr).blockMatrix, xU, yU);
                        */
                        
                        if (posY - height * i >= 0 && checkedUp.equals("bomb") && !objectHits.contains("up")) {
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    markForRemove(peer, (BBomb) map.get(roomNr).blockMatrix[xU][yU]);
                                }
                            }).start();
                            objectHits.add("up");
                            //System.out.println("hit bomb up");
                        } else if (posY - height * i >= 0 && checkedUp.equals("char") && !objectHits.contains("up")) {
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    triggerBlewCharacter(peer, xU, yU);
                                }
                            }).start();
                            //objectHits.add("up");
                            exp.ranges.put("up", exp.ranges.get("up") + 1);
                            //System.out.println("hit character up");
                        } else if (posY - height * i >= 0 && checkedUp.equals("wall") && !objectHits.contains("up")) {
                            exp.directions.add("up");
                            //System.out.println("hit wall up");
                            AbstractWall wall = ((AbstractWall) map.get(roomNr).blockMatrix[xU][yU]);
                            if (wall.isBlowable()) {
                                map.get(roomNr).walls.remove(map.get(roomNr).blockMatrix[xU][yU]);
                                exp.ranges.put("up", exp.ranges.get("up") + 1);
                                flipForItems(peer, xU, yU);
//                                mapChanged.put(roomNr, true);
                                blownWalls.get(roomNr).add(wall.wallId);
                                wallsChanged.put(roomNr, true);
                            }
                            objectHits.add("up");
                        } else if (posY - height * i >= 0 && checkedUp.equals("item") && !objectHits.contains("up")) {
                            exp.directions.add("up");
                            items.get(roomNr).remove((AbstractItem) map.get(roomNr).blockMatrix[xU][yU]);
                            map.get(roomNr).blockMatrix[xU][yU] = null;
                            exp.ranges.put("up", exp.ranges.get("up") + 1);
                            objectHits.add("up");
                            itemsChanged.put(roomNr, true);
                        } else if (posY - height * i >= 0 && !checkedUp.equals("wall") && !objectHits.contains("up")) {
                            exp.directions.add("up");
                            exp.ranges.put("up", exp.ranges.get("up") + 1);
                            //System.out.println("empty up");
                        }
                    }

                } catch (Exception ex) {
                    BLogger.getInstance().logException2(ex);
                }
            }
        }).start();
    }

    protected synchronized void flipForItems(Session peer, int x, int y) {
        int rand = (int) (Math.random() * 1000000);
        int roomNr = getRoom(peer);
        if (rand % 2 == 0) { // 50% chance to find a hidden item behind the wall ;))
            if (wallExists(map.get(roomNr).blockMatrix, x, y)) {
                AbstractWall wall = ((AbstractWall) map.get(roomNr).blockMatrix[x][y]);
                //blownWalls.get(roomNr).add(wall.wallId);
                
                AbstractItem item = ItemGenerator.getInstance().generateRandomItem();
                item.setPosX(map.get(roomNr).blockMatrix[x][y].getPosX());
                item.setPosY(map.get(roomNr).blockMatrix[x][y].getPosY());
                map.get(roomNr).blockMatrix[x][y] = null;
                map.get(roomNr).blockMatrix[x][y] = item;
                items.get(roomNr).add(item);
            }
            else{
                map.get(roomNr).blockMatrix[x][y] = null;
            }
        } else {
            map.get(roomNr).blockMatrix[x][y] = null; // remove the block
        }
        //mapChanged.put(roomNr, true);
        itemsChanged.put(roomNr, true);
//        wallsChanged.put(roomNr, true);
    }

    public synchronized boolean alreadyMarked(Session peer, BBomb bomb) {
        return markedBombs.get(getRoom(peer)).contains(bomb);
    }

    protected synchronized void exportChars(final Session peer) {
//        new Thread(new Runnable(){
//
//            @Override
//            public synchronized void run() {
                String ret = "";
                ret += peer.getId() + "[#chars#]";
                int roomNr = getRoom(peer);
                Set<BCharacter> myChars = Collections.synchronizedSet(new HashSet<BCharacter>(chars2.get(roomNr)));
                for (BCharacter crtChar : myChars){
                    ret += crtChar.toString() + "[#charSep#]";
                }
                try {
                    peer.getBasicRemote().sendText("chars:[" + ret);
                } catch (IOException ex) {
                    BLogger.getInstance().logException2(ex);
                }
//            }
//            
//        }).start();
    }

    protected synchronized void exportMap(final Session peer) {
        new Thread(new Runnable(){

            @Override
            public synchronized void run() {
                String ret = "";
                int roomNr = getRoom(peer);
                ret = map.get(roomNr).toString();
                try {
                    peer.getBasicRemote().sendText("map:[" + ret);
                } catch (IOException ex) {
                    BLogger.getInstance().logException2(ex);
                }
            }
            
        }).start();
    }

    protected synchronized void exportWalls(final Session peer){
//        new Thread(new Runnable(){
//
//            @Override
//            public synchronized void run() {
                String ret = "";
                int roomNr = getRoom(peer);
                //BLogger.getInstance().log(BLogger.LEVEL_FINE, "exporting bombs...");
                if (blownWalls.get(roomNr) != null) {
                    Set<String> walls2 = Collections.synchronizedSet(new HashSet<String>(blownWalls.get(roomNr)));
                    for (String wall : walls2) {
                        ret += wall + "[#brickSep#]";
                    }
                }
                try {
                    peer.getBasicRemote().sendText("blownWalls:[" + ret);
                } catch (IOException ex) {
                    BLogger.getInstance().logException2(ex);
                }
//            }
//            
//        }).start();
    }
    
    protected synchronized void exportBombs(final Session peer) {
//        new Thread(new Runnable(){
//
//            @Override
//            public synchronized void run() {
                String ret = "";
                int roomNr = getRoom(peer);
                //BLogger.getInstance().log(BLogger.LEVEL_FINE, "exporting bombs...");
                if (bombs.get(roomNr) != null) {
                    Set<BBomb> bombs2 = Collections.synchronizedSet(new HashSet<BBomb>(bombs.get(roomNr)));
                    for (BBomb bomb : bombs2) {
                        ret += bomb.toString() + "[#bombSep#]";
                    }
                }
                try {
                    peer.getBasicRemote().sendText("bombs:[" + ret);
                    
                } catch (IOException ex) {
                    BLogger.getInstance().logException2(ex);
                }
//            }
//            
//        }).start();
    }

    protected synchronized void exportExplosions(final Session peer) {
//        new Thread(new Runnable(){
//
//            @Override
//            public synchronized void run() {
                String ret = "";
                int roomNr = getRoom(peer);
                if (explosions.get(roomNr) != null) {
                    Set<Explosion> explosions2 = Collections.synchronizedSet(new HashSet<Explosion>(explosions.get(roomNr)));
                    for (Explosion exp : explosions2) {
                        ret += exp.toString() + "[#explosionSep#]";
                    }
                }
                try {
                    peer.getBasicRemote().sendText("explosions:[" + ret);
                    
                } catch (IOException ex) {
                    BLogger.getInstance().logException2(ex);
                }
//            }
//            
//        }).start();
    }

    protected synchronized void exportItems(final Session peer) {
//        new Thread(new Runnable(){
//
//            @Override
//            public synchronized void run() {
                String ret = "";
                int roomNr = getRoom(peer);
                if (items.get(roomNr) != null) {
                    Set<AbstractItem> items2 = Collections.synchronizedSet(new HashSet<AbstractItem>(items.get(roomNr)));
                    for (AbstractItem item : items2) {
                        ret += item.toString() + "[#itemSep#]";
                    }
                }
                try {
                    peer.getBasicRemote().sendText("items:[" + ret);
                    
                } catch (IOException ex) {
                    BLogger.getInstance().logException2(ex);
                }
//            }
//            
//        }).start();
    }

    public boolean canPlantNewBomb(Session peer, BCharacter crtChar) {
        //return crtChar.getPlantedBombs() < crtChar.getMaxBombs();
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

    public static synchronized String checkWorldMatrix(int roomNr, int i, int j) {
        HashMap<String, BCharacter>[][] chars = map.get(roomNr).chars;
        AbstractBlock[][] data = map.get(roomNr).blockMatrix;
        if (i < 0 || j < 0) return "empty";
        String ret = "";
        try {
            Class<?> cls = (data[i][j] != null) ? data[i][j].getClass() : "".getClass();
            if (chars[i][j] != null && !chars[i][j].isEmpty()) {
                return "char";
            } else if (AbstractWall.class.isAssignableFrom(cls)) {
                return "wall";
            } else if (BBomb.class.isAssignableFrom(cls)) {
                return "bomb";
            } else if (AbstractItem.class.isAssignableFrom(cls)) {
                return "item";
            }
            return "empty";
        } catch (ArrayIndexOutOfBoundsException e) {
            BLogger.getInstance().logException2(e);
            return "empty";
        }
    }

    public void playSound(String sound, Session peer) {
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

    public void playSoundAll(int roomNr, String sound) {
        Set<BCharacter> myChars = Collections.synchronizedSet(new HashSet<BCharacter>(chars2.get(roomNr)));
        for (BCharacter crtChar : myChars){
            playSound(sound, peers.get(crtChar.getId()));
        }
    }

    private static int getRoom(Session peer) {
        int roomNr = Integer.parseInt(peer.getUserProperties().get("room").toString());
        if(!peerRooms.containsKey(peer.getId())){
            peerRooms.put(peer.getId(), roomNr);
        }
        return peerRooms.get(peer.getId());
    }
    
    public synchronized void makePlayerReady(Session peer){
        BCharacter crtChar = chars.get(peer.getId());
        crtChar.setReady(true);
    }
    
    public synchronized void setCharPosition(int mapNumber, BCharacter newChar){
        Random r = new Random();
        int Low = 0;
        int HighW = World.getWidth() / World.wallDim - 1;
        int HighH = World.getHeight() / World.wallDim - 1;
        int X = r.nextInt(HighW - Low) + Low;
        int Y = r.nextInt(HighH) + Low;
        while (!BombermanWSEndpoint.checkWorldMatrix(mapNumber, X, Y).equals("empty")) {
            X = r.nextInt(HighW - Low) + Low;
            Y = r.nextInt(HighH) + Low;
        }

        newChar.setPosX(X * World.wallDim);
        newChar.setPosY(Y * World.wallDim);
        map.get(mapNumber).chars[X][Y].put(newChar.getId(), newChar);
    }
    
    public void sendMessage(String msg, Session peer) {
        try {
            peer.getBasicRemote().sendText("msg:[" + msg);
        } catch (IOException ex) {
            BLogger.getInstance().logException2(ex);
        } catch (IllegalStateException ex) {
            BLogger.getInstance().logException2(ex);
        } catch (ConcurrentModificationException ex) {
            BLogger.getInstance().logException2(ex);
        }
    }
    
    public void sendMessageAll(int roomNr, String msg){
        Set<BCharacter> myChars = Collections.synchronizedSet(new HashSet<BCharacter>(chars2.get(roomNr)));
        for (BCharacter crtChar : myChars){
            sendMessage(msg, peers.get(crtChar.getId()));
        }
    }

    private void logChatMessage(BCharacter myChar, String msg) {
        try {
            String query = "INSERT INTO `chat_message` (id, peer_id, peer_name, message_time, message)"
                    + "VALUES (NULL, ?, ?, NOW(), ?)";
            PreparedStatement st = (PreparedStatement)BombermanWSEndpoint.con.prepareStatement(query);
            st.setString(1, myChar.getId());
            st.setString(2, myChar.getName());
            st.setString(3, msg);
            int affectedRows = st.executeUpdate();
            if (affectedRows == 0){
                throw new SQLException("Cannot log chat message : `"+msg+"` from user `"+myChar.getName()+" ( "+myChar.getId()+" )"+"`");
            }
        } catch (SQLException ex) {
            BLogger.getInstance().logException2(ex);
        }
    }
    
}
