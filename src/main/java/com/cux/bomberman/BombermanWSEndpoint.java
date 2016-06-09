package com.cux.bomberman;

import com.cux.bomberman.util.BBase64;
import com.cux.bomberman.util.BChatMessage;
import com.cux.bomberman.util.BLogger;
import com.cux.bomberman.util.BStringEncrypter;
import com.cux.bomberman.world.AbstractBlock;
import com.cux.bomberman.world.BBaseBot;
import com.cux.bomberman.world.BBomb;
import com.cux.bomberman.world.BCharacter;
import com.cux.bomberman.world.BDumbBot;
import com.cux.bomberman.world.BMediumBot;
import com.cux.bomberman.world.Explosion;
import com.cux.bomberman.world.World;
import com.cux.bomberman.world.generator.ItemGenerator;
import com.cux.bomberman.world.generator.WorldGenerator;
import com.cux.bomberman.world.items.AbstractItem;
import com.cux.bomberman.world.walls.AbstractWall;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.http.HttpSession;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import javax.websocket.server.PathParam;

/**
 * This is the main class of the project; it is the endpoint and acts as a 
 * websockets server, maintaining and controlling all of the connecting clients
 * 
 * @version 1.0
 * @author  Mihail Cuculici (mihai.cuculici@gmail.com)
 * @author  http://www.
 */
@ServerEndpoint(value = "/bombermanendpoint/{token}", configurator = BombermanHttpSessionConfigurator.class)
public class BombermanWSEndpoint {

    /**
     * Static final variable. Used to track the current application version
     */
    public static final double VERSION = 0.89;
    /**
     * Static variable. Collection used to find users by their nicknames (
     * really quick )<br />
     * Indexed by player name, stores peer room number and peer id
     */
    public static final Map<String, Map.Entry<Integer, String>> charMapByName = Collections.synchronizedMap(new LinkedHashMap<String, Map.Entry<Integer, String>>());
    
    /**
     * Static variable. Collection used to keep track of all the existing
     * planted bombs.<br />
     * Indexed by peer map number.<br />
     * It is constantly changed/updated
     */
    public static final Map<Integer, Map<String, BBomb>> bombs = Collections.synchronizedMap(new ConcurrentHashMap<Integer, Map<String, BBomb>>());

    /**
     * Static variable. Collection used to keep track of all the existing bombs
     * marked for explosion.<br />
     * Indexed by peer map number.<br />
     * It is constantly changed/updated
     */
    private static final Map<Integer, Map<String, BBomb>> markedBombs = Collections.synchronizedMap(new ConcurrentHashMap<Integer, Map<String, BBomb>>());

    /**
     * Static variable. Collection used to keep track of all the connected
     * users.<br />
     * Indexed by peer map number & peer Id
     */
    public static final Map<Integer, Map<String, BCharacter>> chars = Collections.synchronizedMap(new ConcurrentHashMap<Integer, Map<String, BCharacter>>());
    
    /**
     * Static variable. Collection used to keep track of all the current
     * explosions.<br />
     * Indexed by peer map number.<br />
     * It is constantly changed/updated
     */
    public static final Map<Integer, Map<String, Explosion>> explosions = Collections.synchronizedMap(new ConcurrentHashMap<Integer, Map<String, Explosion>>());

    /**
     * Static variable. Collection used to keep track of all the current blown
     * walls.<br />
     * Indexed by peer map number.<br />
     * It is constantly changed/updated
     */
    private static final Map<Integer, Set<String>> blownWalls = Collections.synchronizedMap(new HashMap<Integer, Set<String>>());

    /**
     * Static variable. Collection used to keep track of all the current
     * existing items.<br />
     * Indexed by peer map number.<br />
     * It is constantly changed/updated
     */
    public static Map<Integer, Map<String, AbstractItem>> items = Collections.synchronizedMap(new ConcurrentHashMap<Integer, Map<String, AbstractItem>>());

    /**
     * Static variable. Collection used to keep track of all the current
     * sessions<br />
     * Indexed by peer id.<br />
     * It is used to identify an already connected client
     */
    private static final Map<String, HttpSession> httpSessions = Collections.synchronizedMap(new HashMap<String, HttpSession>());

    /**
     * Static variable. Collection used to keep track of all the current
     * maps<br />
     * Indexed by peer map number
     */
    public static Map<Integer, World> map = Collections.synchronizedMap(new HashMap<Integer, World>());

    /**
     * Static variable. Collection used to keep track of the number of players
     * connected to each map<br />
     * Indexed by peer map number
     */
    private static final Map<Integer, Integer> mapPlayers = Collections.synchronizedMap(new HashMap<Integer, Integer>());

    /**
     * Static variable. Collection used to mark if the characters changed.<br />
     * Indexed by peer map number
     */
    public static final Map<Integer, Boolean> charsChanged = Collections.synchronizedMap(new HashMap<Integer, Boolean>());

    /**
     * Static variable. Collection used to mark if the map has changed.<br />
     * Indexed by peer map number
     */
    public static final Map<Integer, Boolean> mapChanged = Collections.synchronizedMap(new HashMap<Integer, Boolean>());

    /**
     * Static variable. Collection used to mark if the walls changed.<br />
     * Indexed by peer map number
     */
    public static final Map<Integer, Boolean> wallsChanged = Collections.synchronizedMap(new HashMap<Integer, Boolean>());

    /**
     * Static variable. Collection used to mark if the bombs changed.<br />
     * Indexed by peer map number
     */
    public static final Map<Integer, Boolean> bombsChanged = Collections.synchronizedMap(new HashMap<Integer, Boolean>());

    /**
     * Static variable. Collection used to mark if the explosions changed.<br />
     * Indexed by peer map number
     */
    public static final Map<Integer, Boolean> explosionsChanged = Collections.synchronizedMap(new HashMap<Integer, Boolean>());

    /**
     * Static variable. Collection used to mark if the items changed.<br />
     * Indexed by peer map number
     */
    public static final Map<Integer, Boolean> itemsChanged = Collections.synchronizedMap(new HashMap<Integer, Boolean>());

    /**
     * Static variable. Collection used store the room number for each connected
     * user.<br />
     * Indexed by peer map number
     */
//    public static final Map<String, Integer> peerRooms = Collections.synchronizedMap(new HashMap<String, Integer>());

    /**
     * Static variable. Used to keep track of the last opened map
     */
    private static int mapNumber = 0;

    /**
     * Static constant. Used to limit the maximum number of players connected to
     * one map
     */
    private final static int MAX_PLAYERS = 6;

    /**
     * Static variable. Used to check if the server should start monitoring the
     * generated games
     */
    private static boolean initialized = false;

    /**
     * Static variable. Singleton pattern, giving the only allowed instance of
     * the Server class
     */
    private static BombermanWSEndpoint instance = null;

    /**
     * Static variable. Used to communicate with the existing database
     */
    public static Connection con = null;

    /**
     * Static constant. Used to define the database connection string
     */
    private static final String DBConnectionString = "jdbc:mysql://localhost:3306/";

    /**
     * Static constant. Used to define the database name
     */
    private static final String DBName = "bomberman";

    /**
     * Static constant. Used to define the database user
     */
    private static final String DBUser = "bomberman_admin";

    /**
     * Static constant. Used to define the database user password
     */
    private static final String DBPass = "bomberman_password";

    /**
     * Static constant. Used as a key passphrase for the encryption of the user IP
     */
    public static final String passKey = "b0mb3rm4nCuxWSap"; // 128 bit key
    
    /**
     * Static list of names to be given to BOT characters
     */
    protected static List<String> names = new ArrayList<String>();
    static{
        names.add("LadyKiller");
        names.add("MoonDancer");
        names.add("Kowalski");
        names.add("Skipper");
        names.add("Thor");
        names.add("Loki");
        names.add("MrPutin");
        names.add("WindCatcher");
        names.add("Wendy");
        names.add("PopCorn");
        names.add("Orange");
        names.add("Ace");
        names.add("MorganFreeman");
        names.add("DeVito");
        names.add("Pepe");
        names.add("Arnold");
        names.add("Doctor Strange");
    }
    
    /**
     * Public method, enhancing access to the application database wrapper
     *
     * @return Connection : database connection wrapper
     */
    public Connection getConnection() {
        return BombermanWSEndpoint.con;
    }

    /**
     * Public static method giving access to the instance of the current class
     *
     * @return BombermanWSEndpoint : the instance of the server class (this)
     */
    public static BombermanWSEndpoint getInstance() {
        return BombermanWSEndpoint.instance;
    }

    /**
     * Public synchronized method handling messages received from connected
     * peer.
     *
     * @param message String containting the message sent by the connected peer
     * @param peer The connected peer
     * @param config The endpoint config, containing information about the peer
     * session (if any)
     * @return null - any returned message will be sent back to the peer
     */
    @OnMessage
    public synchronized String onMessage(String message, final Session peer, EndpointConfig config) {
        
        if (peer == null) return null;
        
//        BLogger.getInstance().log(BLogger.LEVEL_FINE, "mesaj: "+message);
        
        message = message.trim();

        String command,
                toProcess = "";
        
        BCharacter crtChar = null;
        int roomNr = getRoom(peer);
        
        if (peer.getUserProperties().containsKey("loggedIn") && peer.getUserProperties().get("loggedIn").equals(true)) {
            crtChar = chars.get(roomNr).get(peer.getId());
        }
        
        if (message.contains(" ")) {
            command = message.substring(0, message.indexOf(" ")).trim();
            toProcess = message.substring(message.indexOf(" ")).trim();
        } else {
            command = message;
        }
        command = command.toLowerCase();
        
        // any other message must come from a logged in user
        if (crtChar == null && !(command.equals("ready") || command.equals("getenvironment") || command.equals("login") || command.equals("register") || command.equals("ip"))) {
            return null;
        }
        
//        BLogger.getInstance().log(BLogger.LEVEL_FINE, "processing command: "+command);
        
        switch (command) {
            case "ready":
                makePlayerReady(peer);
                break;
            case "getenvironment":
                exportEnvironment(peer);
                break;
            case "login":
                loginProtocol(peer, toProcess, config);
                break;
            case "register":
                registerProtocol(peer, toProcess, config);
                break;
            case "name":
                changeNameProtocol(crtChar, toProcess, roomNr);
                break;
            case "msg":
                messageProtocol(peer, toProcess, roomNr);
                break;
            case "kick":
                kickProtocol(peer, toProcess, config);
                break;
            case "teleport":
                teleportProtocol(peer, toProcess, config);
                break;
            case "map":
                changeMapProtocol(peer, toProcess, roomNr);
                break;
            case "maps":
                listMapsProtocol(peer);
                break;
            case "resetall":
                resetAllProtocol(peer, roomNr);
                break;
            case "help":
                showHelpProtocol(peer);
                break;
            case "addmediumbot":
                addBotProtocol(peer, 1, roomNr);
                break;
            case "adddumbbot":
                addBotProtocol(peer, 0, roomNr);
                break;
            case "up":
            case "down":
            case "left":
            case "right":
                moveProtocol(crtChar, command, roomNr);
                break;
            case "bomb":
                bombProtocol(crtChar, roomNr);
                break;
            case "detonate":
                detonateProtocol(crtChar, roomNr);
                break;
            case "trap":
                changeStateProtocol(crtChar, roomNr, "Trapped");
                break;
            case "free":
                changeStateProtocol(crtChar, roomNr, "Normal");
                break;
            case "blow":
                changeStateProtocol(crtChar, roomNr, "Blow");
                break;
            case "win":
                changeStateProtocol(crtChar, roomNr, "Win");
                break;
            case "quit":
                quitProtocol(peer, crtChar, config);
                break;
            case "ip":
                checkBannedProtocol(peer, toProcess);
                break;
            case "getip":
                getIPProtocol(peer, toProcess);
                break;
            case "banip":
                banIPProtocol(peer, toProcess, config);
                break;
            case "unbanip":
                unbanIPProtocol(peer, toProcess);
                break;
            case "addtestbot":
                addTestBotProtocol(peer, roomNr, toProcess);
                break;
            case "movechar":
                moveCharProtocol(peer, toProcess);
                break;
            case "searchanddestroy":
                searchAndDestroyProtocol(peer, toProcess);
                break;
            case "startbot":
                startBotProtocol(peer, toProcess);
                break;
            case "stopbot":
                stopBotProtocol(peer, toProcess);
                break;
            case "rename":
                renameProtocol(peer, toProcess, roomNr);
                break;
            case "dropbomb":
                dropBombProtocol(peer, toProcess, roomNr);
                break;
            case "detonatebomb":
                detonateBombProtocol(peer, toProcess, roomNr);
                break;
            case "getchat":
                exportChatProtocol(peer, roomNr, Integer.parseInt(toProcess));
                break;
            default:
                if (isAdmin(peer)) {
                    sendStatusMessage(peer, "command not found [ " + message + " ]\nTYPE `help` to see the available commands");
                }
                break;
        }

        return null; // any string will be send to the requesting peer
    }

    /**
     * Public synchronized method handling the closing connection of the peer
     *
     * @param peer The connected peer
     */
    @OnClose
    public synchronized void onClose(Session peer) {
        int roomNr = getRoom(peer);
        String initialName = chars.get(roomNr).get(peer.getId()).getName();
        BLogger.getInstance().log(BLogger.LEVEL_INFO, initialName + " has left the game");
        if (peer.isOpen()) {
            try {
                peer.close();
            } catch (IOException ex) {
                BLogger.getInstance().logException2(ex);
            }
        }

        removeUser(peer, roomNr);
        
        playSoundAll(roomNr, "sounds/elvis.mp3");
        sendMessageAll(roomNr, "<b>ELVIS [ " + initialName + " ]  has left the building </b>");
    }

    public void removeUser(Session peer, int roomNr){
        BCharacter myChar = chars.get(roomNr).get(peer.getId());
        int x = myChar.getBlockPosX();
        int y = myChar.getBlockPosY();
        synchronized(map){
            map.get(myChar.roomIndex).chars[x][y].remove(myChar.getId());
        }

        chars.get(roomNr).remove(peer.getId());

        charsChanged.put(roomNr, true);
        mapPlayers.put(roomNr, Math.min(mapPlayers.get(roomNr) - 1, 0));
        sendClearMessage("removed:[{}", peer);
    }
    
    /**
     * Public synchronized method handling a new connection
     *
     * @param peer The connected peer
     * @param config The endpoint config, containing information about the peer
     * session (if any)
     */
    @OnOpen
    public synchronized void onOpen(Session peer, EndpointConfig config, @PathParam("token") String token) {

        if (!BombermanWSEndpoint.initialized) {
            watchBombs();
            watchPeers();
            BombermanWSEndpoint.initialized = true;
            BombermanWSEndpoint.instance = this;
            try {
                Class.forName("com.mysql.jdbc.Driver").newInstance();
                BombermanWSEndpoint.con = DriverManager.getConnection(BombermanWSEndpoint.DBConnectionString + BombermanWSEndpoint.DBName,
                        BombermanWSEndpoint.DBUser, BombermanWSEndpoint.DBPass);
                if (!con.isClosed()) {
                    BLogger.getInstance().log(BLogger.LEVEL_FINE, "Connected to MySQL Database...");
                }
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | SQLException e) {
                BLogger.getInstance().logException2(e);
            }
            
        }

        BStringEncrypter desEncrypter = new BStringEncrypter(BombermanWSEndpoint.passKey);
        String userIP = desEncrypter.decrypt(token.replace("__slash__", "/"));
        
//        System.out.println(userIP+"asd");
        
        peer.getUserProperties().put("ip", userIP);
        
        if (this.isBanned(peer, userIP)){
            this.sendBannedMessage(peer);
        }
        
        /*
        // check user IP...
        sendClearMessage("getip:[{}", peer);
        */
        
        HttpSession httpSession = (HttpSession) config.getUserProperties().get(HttpSession.class.getName());
        String sessionId = httpSession.getId();

        // check to see if the user isn't already recognized by the server (has a known cookie)
        if (!httpSessions.isEmpty() && httpSessions.containsKey(sessionId)) {
            if (!chars.isEmpty()){
                Iterator<Map.Entry<Integer, Map<String, BCharacter>>> it = chars.entrySet().iterator();
                while (it.hasNext()){
                    Map.Entry<Integer, Map<String, BCharacter>> pairs = it.next();
                    int roomNr = pairs.getKey();
                    Iterator<Map.Entry<String, BCharacter>> it2 = pairs.getValue().entrySet().iterator();
                    while (it2.hasNext()){
                        Map.Entry<String, BCharacter> pair = it2.next();
                        BCharacter crtChar = (BCharacter) pair.getValue();
                        Session peer2 = crtChar.getPeer();
                        if (peer2.equals(null)) continue;
                        if (peer2.getUserProperties().containsKey("sessionId") && peer2.getUserProperties().get("sessionId").equals(sessionId)) {
                            // if the current user is already loggedIn, restore it's properties and close the old connection
                            if (peer2.getUserProperties().containsKey("loggedIn") && peer2.getUserProperties().get("loggedIn").equals(true)) {
                                boolean toBeMadeAdmin = false;
                                if (peer2.getUserProperties().containsKey("isAdmin") && peer2.getUserProperties().get("isAdmin").equals(true)) {
                                    toBeMadeAdmin = true;
                                }
                                peer.getUserProperties().putAll(peer2.getUserProperties());
                                addPlayerToGame(peer, config, peer2.getUserProperties().get("username").toString(), Integer.valueOf(peer2.getUserProperties().get("user_id").toString()));
                                sendReadyMessage(peer);
                                if (toBeMadeAdmin){
                                    makePlayerAdmin(peer);
                                }
                                httpSessions.put(sessionId, (HttpSession) config.getUserProperties()
                                    .get(HttpSession.class.getName()));
                                return;
                            }
                        }
                    }
                }
                
            }
        }

        httpSessions.put(sessionId, (HttpSession) config.getUserProperties()
                .get(HttpSession.class.getName()));
        peer.getUserProperties().put("sessionId", sessionId);
        peer.getUserProperties().put("loggedIn", false);

        sendLoginFirstMessage(peer);

    }

    /**
     * Public method used to store a new player in the database
     *
     * @param peer The connected peer
     * @param user The name of the player trying to register
     * @param pass The password of the player trying to register
     * @param email The email address of the player trying to register
     * @param config The endpoint config, containing information about the peer
     * session (if any)
     */
    public void register(Session peer, String user, String pass, String email, EndpointConfig config) {
        String emailPattern = "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))$";
        Pattern p = Pattern.compile(emailPattern);
        Matcher m = p.matcher(email);
        if (!m.matches()) {
            sendInvalidEmailMessage(peer);
        }
        try {
            String query = "SELECT id FROM `user` WHERE `email`=? OR `username`=?";
            PreparedStatement st2 = (PreparedStatement) BombermanWSEndpoint.con.prepareStatement(query);
            st2.setString(1, email);
            st2.setString(2, user);
            ResultSet ret = st2.executeQuery();
            if (ret.next()) {
                sendAlreadyRegisteredMessage(peer);
                return;
            }

            query = "INSERT INTO `user` SET `email`=?, `username`=?, `password`=?, `registered_at`='"+this.getMySQLDateTime()+"'";
            PreparedStatement st = (PreparedStatement) BombermanWSEndpoint.con.prepareStatement(query);
            st.setString(1, email);
            st.setString(2, user);
            st.setString(3, md5Java(pass));
            int affectedRows = st.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Cannot register user " + email);
            }
            sendRegistrationSuccessMessage(peer);
        } catch (SQLException ex) {
            BLogger.getInstance().logException2(ex);
            sendRegisterFailedMessage(peer);
        }
    }

    /**
     * Public method used to login an existing player
     *
     * @param peer The connected peer
     * @param user The name of the player trying to register
     * @param pass The password of the player trying to register
     * @param config The endpoint config, containing information about the peer
     * session (if any)
     */
    public void logIn(Session peer, String user, String pass, EndpointConfig config) {
        try {
            String query = "SELECT id, admin FROM `user` WHERE `username`=? AND `password`=?;";
            PreparedStatement st = (PreparedStatement) BombermanWSEndpoint.con.prepareStatement(query);
            st.setString(1, user);
            st.setString(2, md5Java(pass));
            //System.out.println(query);
            ResultSet ret = st.executeQuery();
            if (ret.next()) {
                addPlayerToGame(peer, config, user, ret.getInt(1));
                sendReadyMessage(peer);
                if (ret.getInt("admin") > 0) {
                    peer.getUserProperties().put("isAdmin", true);
                    makePlayerAdmin(peer);
                }
            } else {
                sendLoginFailedMessage(peer);
            }
        } catch (SQLException ex) {
            BLogger.getInstance().logException2(ex);
            sendLoginFailedMessage(peer);
        }
    }

    /**
     * Public static method that decodes an encoded string using the
     * base64_decode algorithm
     *
     * @param s the string to be decoded
     * @return the decoded string
     */
    public static String decodeBase64(String s) {
        return BBase64.decode(s);
    }

    /**
     * Public static method that encodes a string using the base64 algorithm
     *
     * @param s the string to be encoded
     * @return the encoded string
     */
    public static String encodeBase64(String s) {
        return BBase64.encode(s);
    }

    /**
     * Public static method that hashes a string using the MD5 algorithm
     *
     * @param message the string to be hashed
     * @return the hashed string
     */
    public static String md5Java(String message) {
        String digest = null;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hash = md.digest(message.getBytes("UTF-8")); //converting byte array to Hexadecimal String
            StringBuilder sb = new StringBuilder(2 * hash.length);
            for (byte b : hash) {
                sb.append(String.format("%02x", b & 0xff));
            }
            digest = sb.toString();
        } catch (UnsupportedEncodingException | NoSuchAlgorithmException ex) {
            BLogger.getInstance().logException2(ex);
        }
        return digest;
    }

    /**
     * Private method used to get the help menu for a requesting admin
     *
     * @return String containing the help menu for the admin
     */
    private String getHelpMenu() {
        return "cuxBomberman v." + BombermanWSEndpoint.VERSION + "\n"
                + "TYPE `addMediumBot` to add a new bot to the game\n"
                + "TYPE `addDumbBot` to add a new dumb bot to the game\n"
                + "TYPE `kick <i>&lt;username&gt;</i>` to remove a player from the game (bots included)\n"
                + "TYPE `teleport <i>&lt;username&gt;</i>` to change a player position (bots included)\n"
                + "TYPE `getip <i>&lt;username&gt;</i>` to get the player connection IP (bots not included)\n"
                + "TYPE `banip <i>&lt;ip&gt;</i>` to ban an given player by it's IP\n"
                + "TYPE `unbanip <i>&lt;ip&gt;</i>` to unban an given player by it's IP\n"
                + "TYPE `addTestBot <i>&lt;dumb|medium&gt;</i>` to add a given bot that you can test and control\n"
                + "TYPE `moveChar <i>&lt;charName&gt;</i> [direction]` to move a player either random or towards a given direction\n"
                + "TYPE `startBot <i>&lt;botName&gt;</i>` to run the Search&Destroy protocol of a given bot \n"
                + "TYPE `stopBot <i>&lt;botName&gt;</i>` to stop the Search&Destroy protocol of a given bot \n"
                + "TYPE `dropBomb <i>&lt;charName&gt;</i>` to force a given player to drop a bomb (if possible) \n"
                + "TYPE `detonateBomb <i>&lt;charName&gt;</i>` to force a given player to detonate a bomb (if possible) \n"
                + "TYPE `map <i>&lt;mapName&gt;</i>` to change the current map \n"
                + "TYPE `maps` to get a list of all the current maps \n"
                + "TYPE `resetAll` to reset all the characters properties and restart the game\n"
                + "TYPE `help` to view this message again\n";
    }

    /**
     * Private method used to get the list of the available maps
     *
     * @return String containing the available maps
     */
    private String getMaps(){
        String ret = "<b>Available maps</b>\n";
        File dir = new File("maps");
        File[] files = dir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(".txt");
            }
        });
        for (int i = 0; i < files.length; i++){
            ret += (i+1)+". "+files[i].getName()+"\n";
        }
        return ret;
    }
    
    /**
     * Removes a bot from the game
     *
     * @param kickedChar the bot to be kicked
     */
    public void kickBot(BBaseBot kickedChar) {
        try {
            kickedChar.setRunning(false);
            chars.get(kickedChar.roomIndex).remove(kickedChar.getId());
        } catch (Exception e) {
            BLogger.getInstance().logException2(e);
        }
    }

    /**
     * Public method returning the player with a given name (if any)
     *
     * @param name The name to be found
     * @return The searched character or NULL
     */
    public BCharacter findCharByName(String name) {
        if (charMapByName.containsKey(name)) {
            Map.Entry pairs = charMapByName.get(name);
            return chars.get((Integer)pairs.getKey()).get((String)pairs.getValue());
        }
        return null;
    }

    /**
     * Public method used add a player to the game
     *
     * @param peer The connected peer
     * @param config The endpoint config, containing information about the peer
     * session (if any)
     * @param username The name of the player trying to connect
     * @param user_id The id of the player trying to connect
     */
    public void addPlayerToGame(Session peer, EndpointConfig config, String username, int user_id) {
        
        boolean multipleSessions = false;
        Iterator<Map.Entry<Integer, Map<String, BCharacter>>> it = chars.entrySet().iterator();
        while (it.hasNext()){
            Map.Entry<Integer, Map<String, BCharacter>> pairs = it.next();
            int roomNr = pairs.getKey();
            Iterator<Map.Entry<String, BCharacter>> it2 = pairs.getValue().entrySet().iterator();
            while (it2.hasNext()){
                Map.Entry<String, BCharacter> pair = it2.next();
                BCharacter crtChar = (BCharacter) pair.getValue();
                Session peer2 = crtChar.getPeer();
                if (peer2 != null){
                    if (!peer.equals(peer2) && crtChar.getUserId() == user_id){ // same login
//                        this.onMessage("QUIT", peer2, crtChar.getConfig());
                        removeUser(peer2, getRoom(peer2));
                    }
                }
            }
        }
        
        HttpSession httpSession = (HttpSession) config.getUserProperties().get(HttpSession.class.getName());
        String sessionId = httpSession.getId();

        peer.getUserProperties().put("loggedIn", true);
        peer.getUserProperties().put("username", username);
        peer.getUserProperties().put("user_id", user_id);

        if (chars.size() == 0) { // is this the first player?
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

        if (map.isEmpty() || map.get(mapNumber) == null) {
            /**
             * The maps folder must be placed in the glassfish current domain
             * config folder.<br />
             * For me, this is
             * <b>/home/mihaicux/GlassFish_Server/glassfish/domains/domain1/config</b><br
             * />
             * Also, you can change this line, and enter the full path to the
             * maps file
             */
//            map.put(mapNumber, new World("/home/mihaicux/projects/bomberman/maps/firstmap.txt"));
            map.put(mapNumber, new World("maps/firstmap.txt"));

        }
        
        charMapByName.put(username, new AbstractMap.SimpleEntry(mapNumber, peer.getId()));

        if (blownWalls.isEmpty() || blownWalls.get(mapNumber) == null) {
            blownWalls.put(mapNumber,  Collections.synchronizedSet(new LinkedHashSet<String>()));
        }

        if (markedBombs.isEmpty() || markedBombs.get(mapNumber) == null) {
//            markedBombs.put(mapNumber,  Collections.synchronizedSet(new HashSet<BBomb>()));
            markedBombs.put(mapNumber, Collections.synchronizedMap(new LinkedHashMap<String, BBomb>()));
        }

        if (chars.isEmpty() || chars.get(mapNumber) == null){
            chars.put(mapNumber, Collections.synchronizedMap(new LinkedHashMap<String, BCharacter>()));
        }
        
        if (explosions.isEmpty() || explosions.get(mapNumber) == null) {
            explosions.put(mapNumber, Collections.synchronizedMap(new LinkedHashMap<String, Explosion>()));
        }

        if (items.isEmpty() || items.get(mapNumber) == null) {
//            items.put(mapNumber,  Collections.synchronizedSet(new HashSet<AbstractItem>()));
            items.put(mapNumber,  Collections.synchronizedMap(new LinkedHashMap<String, AbstractItem>()));
        }

        if (bombs.isEmpty() || bombs.get(mapNumber) == null) {
            bombs.put(mapNumber, Collections.synchronizedMap(new LinkedHashMap<String, BBomb>()));
        }

        if (wallsChanged.isEmpty() || wallsChanged.get(mapNumber) == null) {
            wallsChanged.put(mapNumber, true);
        }

        peer.getUserProperties().put("room", mapNumber);
        
        BCharacter newChar = new BCharacter(peer.getId(), username, mapNumber, config);
        newChar.setPosX(0);
        newChar.setPosY(0);
        newChar.setWidth(World.wallDim);
        newChar.setHeight(World.wallDim);
        newChar.setUserId(user_id);
        newChar.restoreFromDB();
        newChar.setIp(peer.getUserProperties().get("ip").toString());
        newChar.storeLogIn();
        newChar.setPeer(peer);        
        
        chars.get(mapNumber).put(peer.getId(), newChar);
        
        setCharPosition(mapNumber, newChar);

        BLogger.getInstance().log(BLogger.LEVEL_INFO, "User connected [" + username + "], room " + mapNumber);
        //System.exit(0);
        charsChanged.put(mapNumber, true);
        bombsChanged.put(mapNumber, true);
        //mapChanged.put(mapNumber, true);
        itemsChanged.put(mapNumber, true);
        explosionsChanged.put(mapNumber, true);

        sendMessageAll(mapNumber, "<b>" + newChar.getName() + " has joined");
    }

    /**
     * Public method user to add a bot to a given map
     *
     * @param peer The peer associated with the requesting admin
     * @param type The type of bot to be added (0 - dumb, 1 - medium)
     * @param roomNr The room where the bot will be added
     * @return The created bot
     */
    public BBaseBot addBot(Session peer, int type, int roomNr) {
        BBaseBot bot;
        
        switch (type) {
            case 1:
                bot = addMediumBot(peer, roomNr);
                break;
            default:
                bot = addDumbBot(peer, roomNr);
                break;
        }
        
        if (mapPlayers.get(roomNr) == null) {
            mapPlayers.put(roomNr, 1); // one player in current map
        } else {
            mapPlayers.put(roomNr, 1 + mapPlayers.get(roomNr));
        }
        
        return bot;
    }

    /**
     * Private method used to manually run a given bot's Search&Destroy
     * directive
     *
     * @param bot The bot to run
     */
    private void startBot(BBaseBot bot) {
        bot.setReady(true);
        bot.setRunning(true);
    }

    /**
     * Private method used to update the world for a given bot
     *
     * @param roomNr The room to be updates
     * @param bot The added bot
     */
    private void initBot(int roomNr, BBaseBot bot) {
        bot.setPosX(0);
        bot.setPosY(0);
        bot.setWidth(World.wallDim);
        bot.setHeight(World.wallDim);
        chars.get(roomNr).put(bot.getName(), bot);
        setCharPosition(roomNr, bot);
        bot.setReady(true);
        new Thread(bot).start();
    }

    private String getBotName(){
        Random r = new Random();
        int nameIndex = r.nextInt(names.size());
        String botName = names.get(nameIndex);
        while (charMapByName.containsKey(botName)){
            botName += r.nextInt();
        }
        return botName;
    }
    
    /**
     * Private method used to add a medium bot to a given map
     *
     * @param peer The peer associated to the admin that requested the addition
     * @param roomNr The selected world
     * @return The created bot
     */
    private BBaseBot addMediumBot(Session peer, int roomNr) {
        String botName = this.getBotName();
        BBaseBot bot = new BMediumBot(botName, botName, roomNr, null);
        initBot(roomNr, bot);
        BLogger.getInstance().log(BLogger.LEVEL_INFO, "Medium BOT [" + botName + "] added, room " + roomNr);
        sendStatusMessage(peer, "bot added - " + botName);
        charMapByName.put(botName, new AbstractMap.SimpleEntry(roomNr, botName));
        //startBot(bot);
        return bot;
    }

    /**
     * Private method used to add a dumb bot to a given map
     *
     * @param peer The peer associated to the admin that requested the addition
     * @param roomNr The selected world
     * @return The created bot
     */
    private BBaseBot addDumbBot(Session peer, int roomNr) {
        String botName = this.getBotName();
        BBaseBot bot = new BDumbBot(botName, botName, mapNumber, null);
        initBot(roomNr, bot);
        BLogger.getInstance().log(BLogger.LEVEL_INFO, "Dumb BOT [" + botName + "] added, room " + roomNr);
        sendStatusMessage(peer, "bot added - " + botName);
        charMapByName.put(botName, new AbstractMap.SimpleEntry(roomNr, botName));
        //startBot(bot);
        return bot;
    }

    /**
     * Public method giving admin privileges to the player
     *
     * @param peer The connected peer
     */
    public void makePlayerAdmin(Session peer) {
        int roomNr = getRoom(peer);
        if (roomNr == -1) {
            return;
        }
        BCharacter crtPlayer = chars.get(roomNr).get(peer.getId());
        if (crtPlayer == null) {
            return;
        }
        crtPlayer.setIsAdmin(true);
        sendAdminModMessage(peer);
    }

    /**
     * Public method used to test if a given peer has an admin accound
     * associated
     *
     * @param peer The checked peer
     * @return TRUE if the peer is an admin
     */
    public boolean isAdmin(Session peer) {
        int roomNr = getRoom(peer);
        if (roomNr == -1) {
            return false;
        }
        BCharacter crtPlayer = chars.get(roomNr).get(peer.getId());
        if (crtPlayer == null) {
            return false;
        }
        return crtPlayer.getIsAdmin();
    }

    /**
     * Public synchronized method used to manage the server errors. Only
     * mentioned here...
     *
     * @param t the exception
     */
    @OnError
    public synchronized void onError(Throwable t) {
    }

    /**
     * Public synchronized method used add send the whole environment to the
     * current player
     *
     * @param peer The connected peer
     */
    public synchronized void exportEnvironment(Session peer) {
        int roomNr = getRoom(peer);
        try {
            exportChars(peer);
            exportMap(peer);
            exportBombs(peer, 0);
            exportExplosions(peer, 0);
            exportItems(peer, 0);
        } catch (IllegalStateException | ConcurrentModificationException ex) {
            BLogger.getInstance().logException2(ex);
        }
    }

    /**
     * Public synchronized method used stop a working thread - used for
     * monitoring informations to be sent to a player
     *
     * @param crtChar The current connected player
     * @param peer The connected peer
     * @return TRUE if the current player if blocked (ie. it cannot move
     * anymore)
     */
    public synchronized boolean isTrapped(BCharacter crtChar, Session peer) {
        if (crtChar == null) {
            return false;
        }
        int x = crtChar.getPosX();
        int y = crtChar.getPosY();
        int w = crtChar.getWidth();
        int h = crtChar.getHeight();
        int blockX = crtChar.getBlockPosX();
        int blockY = crtChar.getBlockPosY();
        int left = blockX - 1;
        int right = blockX + 1;
        int up = blockY - 1;
        int down = blockY + 1;
        int roomNr = crtChar.roomIndex;
        return ((x <= 0 || map.get(roomNr).wallExists(left, blockY) || map.get(roomNr).bombExists(left, blockY))
                && (x + w >= map.get(roomNr).getWidth() || map.get(roomNr).wallExists(right, blockY) || map.get(roomNr).bombExists(right, blockY))
                && (y <= 0 || map.get(roomNr).wallExists(blockX, up) || map.get(roomNr).bombExists(blockX, up))
                && (y + h >= map.get(roomNr).getHeight() || map.get(roomNr).wallExists(blockX, down) || map.get(roomNr).bombExists(blockX, down)));
    }

    /**
     * Public method using a separate thread for watching the bombs (useful to
     * check if a bomb is about to detonate).
     */
    public synchronized void watchBombs() {
        new Thread(new Runnable() {

            @Override
            public void run() {
                while (true) {
                    try {
                        for (int roomNr = 1; roomNr <= mapNumber; roomNr++) {
                            if (bombs.containsKey(roomNr)) {
//                                Iterator<BBomb> it = bombs.get(roomNr).iterator();
                                Iterator<Map.Entry<String, BBomb>> it = bombs.get(roomNr).entrySet().iterator();
                                while (it.hasNext()){
                                    Map.Entry<String, BBomb> pair = it.next();
                                    final BBomb bomb = pair.getValue();
                                    if (bomb == null) {
                                        continue;
                                    }
                                    if (bomb.isVolatileB() && (new Date().getTime() - bomb.getCreationTime().getTime()) / 1000 >= bomb.getLifeTime() && !alreadyMarked(bomb)) {
//                                        new Thread(new Runnable() {
//
//                                            @Override
//                                            public synchronized void run() {
                                                markForRemove(bomb);
//                                            }
//                                        }).start();
                                    }
                                }
                            }
                        }
                        Thread.sleep(10);
                    } catch (InterruptedException | IllegalStateException | ConcurrentModificationException ex) {
//                        BLogger.getInstance().logException2(ex);
                    }
                }
            }
        }).start();

    }

    /**
     * Public method using a separate thread for watching the connected players
     * (useful to check if we need to send messages to each player).
     */
    public void watchPeers() {
//        final BombermanWSEndpoint environment = this;
        new Thread(new Runnable() {

            @Override
            public void run() {
                while (true) {
                    try {
                        int max = mapNumber + 1;
                        boolean[] charChanged = new boolean[max];
                        boolean[] map2Changed = new boolean[max];
                        boolean[] bombChanged = new boolean[max];
                        boolean[] explosionChanged = new boolean[max];
                        boolean[] itemChanged = new boolean[max];
                        boolean[] wallChanged = new boolean[max];
                        
                        Iterator<Map.Entry<Integer, Map<String, BCharacter>>> it = chars.entrySet().iterator();
                        while (it.hasNext()){
                            Map.Entry<Integer, Map<String, BCharacter>> pairs = it.next();
                            int roomNr = pairs.getKey();
                            Iterator<Map.Entry<String, BCharacter>> it2 = pairs.getValue().entrySet().iterator();
                            while (it2.hasNext()){
                                Map.Entry<String, BCharacter> pair = it2.next();
                                BCharacter crtChar = (BCharacter) pair.getValue();
                                Session peer = crtChar.getPeer();
                                
                                if (peer == null) {
                                    continue;
                                }

                                if (peer.isOpen()) {
                                    if (peer.getUserProperties().get("room") == null || peer.getUserProperties().get("room").equals(-1)) {
                                        peer.getUserProperties().put("room", roomNr);
                                    }

                                    if (isTrapped(crtChar, peer)) {
                                        crtChar.setState("Trapped"); // will be automated reverted when a bomb kills him >:)
                                        charsChanged.put(roomNr, true);
                                    }

                                    try {

                                        // export characters
                                        if (charsChanged.containsKey(roomNr) && charsChanged.get(roomNr)) {
                                            exportChars(peer);
                                            charChanged[roomNr] = true;
                                        }

                                        // export map?
                                        if (mapChanged.containsKey(roomNr) && mapChanged.get(roomNr)) {
                                            exportMap(peer);
                                            map2Changed[roomNr] = true;
                                        }

                                        // export walls?
                                        if (wallsChanged.containsKey(roomNr) && wallsChanged.get(roomNr)) {
                                            exportWalls(peer, 0);
                                            wallChanged[roomNr] = true;
                                        }

                                        // export bombs?
                                        if (bombsChanged.containsKey(roomNr) && bombsChanged.get(roomNr)) {
                                            exportBombs(peer, 0);
                                            bombChanged[roomNr] = true;
                                        }

                                        // export explosions?
                                        if (explosionsChanged.containsKey(roomNr) && explosionsChanged.get(roomNr)) {
                                            exportExplosions(peer, 0);
                                            explosionChanged[roomNr] = true;
                                        }

                                        // eport items?
                                        if (itemsChanged.containsKey(roomNr) && itemsChanged.get(roomNr)) {
                                            exportItems(peer, 0);
                                            itemChanged[roomNr] = true;
                                        }
                                    } catch (ConcurrentModificationException | IllegalStateException ex) {
//                                    BLogger.getInstance().logException2(ex);
                                    } catch (RuntimeException ex) {
//                                    BLogger.getInstance().logException2(ex);
                                    }
                                }
                                
                            }
                            
                        }
                        
                        Thread.sleep(10); // limiteaza la 100FPS comunicarea cu clientul
                        
                        for (int i = 1; i <= mapNumber; i++) {
                            try {
                                if (charChanged[i]){
                                    charsChanged.put(i, false);
                                }
                                if (map2Changed[i]) {
                                    mapChanged.put(i, false);
                                }
                                if (bombChanged[i]) {
                                    bombsChanged.put(i, false);
                                }
                                if (explosionChanged[i]) {
                                    explosionsChanged.put(i, false);
                                    explosions.get(i).clear();
                                }
                                if (itemChanged[i]) {
                                    itemsChanged.put(i, false);
                                }
                                if (wallChanged[i]) {
                                    wallsChanged.put(i, false);
                                    blownWalls.get(i).clear();
                                }
                            } catch (ArrayIndexOutOfBoundsException e) {
//                                BLogger.getInstance().logException2(e);
                            }
                        }
                    } catch (ConcurrentModificationException | InterruptedException ex) {
                        BLogger.getInstance().logException2(ex);
                    }
                }

            }
        }).start();
    }

    /**
     * Protected synchronized method used to detonate bombs of a given player
     *
     * @param myChar The current connected player
     */
    public synchronized void detonateBomb(BCharacter myChar) {
        //int roomNr = getRoom(peer);
        int roomNr = myChar.roomIndex;
        if (bombs.containsKey(roomNr)){
            Iterator<Map.Entry<String, BBomb>> it = bombs.get(roomNr).entrySet().iterator();
            while (it.hasNext()){
                Map.Entry<String, BBomb> pair = it.next();
                BBomb bomb = pair.getValue();
                if (bomb.getCharId().equals(myChar.getName())) {
                    bomb.setVolatileB(true);
                    markForRemove(bomb);
                    break;
                }
            }
        }
        bombsChanged.put(roomNr, true);
        explosionsChanged.put(roomNr, true);
    }

    /**
     * Public static synchronized method used to check if an item exists in a
     * given position
     *
     * @param data The block matrix
     * @param i The x coordinate of the checked position
     * @param j The y coordinate of the checked position
     * @return True if there is an item at the given position
     */
    public static synchronized boolean itemExists(AbstractBlock[][] data, int i, int j) {
        if (i < 0 || j < 0) {
            return false;
        }
        try {
            AbstractBlock x = data[i][j];
            if (data[i][j] == null) {
                return false;
            }
            return AbstractItem.class.isAssignableFrom(data[i][j].getClass());
        } catch (ArrayIndexOutOfBoundsException e) {
            BLogger.getInstance().logException2(e);
            return false;
        }
    }

    /**
     * Public static synchronized method used to check if a player exists in a
     * given position
     *
     * @param mapNr The map number
     * @param i The x coordinate of the checked position
     * @param j The y coordinate of the checked position
     * @return True if there is a character at the given position
     */
    public static synchronized boolean characterExists(int mapNr, int i, int j) {
        if (i < 0 || j < 0) {
            return false;
        }

        World world = map.get(mapNr);
        if (world == null) {
            return false;
        }
        
        return (world.chars[i][j] != null && !world.chars[i][j].isEmpty());
    }

    /**
     * Protected synchronized method used to explode a given player (if it has
     * in the way of an explosion)
     *
     * @param winner The blown character
     * @param x The x coordinate of the checked position
     * @param y The y coordinate of the checked position
     */
//    protected synchronized void triggerBlewCharacter(final Session peer, final int x, final int y) {
    protected synchronized void triggerBlewCharacter(final BCharacter winner, final int x, final int y) {
        //System.out.println("hit...");
        new Thread(new Runnable() {
            @Override
            public synchronized void run() {
                if (winner == null) {
                    return;
                }
                //int roomNr = getRoom(peer);
                int roomNr = winner.roomIndex;
                if (map.get(roomNr).chars[x][y] != null && !map.get(roomNr).chars[x][y].isEmpty()) {
                    Iterator<String> it = map.get(roomNr).chars[x][y].iterator();
                    synchronized(it){
                        while (it.hasNext()){
                            try{
                                String charId = it.next();
                                BCharacter looser = (BCharacter) chars.get(roomNr).get(charId);
                                if (looser != null && looser.getReady()) { // change game stats only if the character within a bomb range is ready to play
                                    looser.incDeaths();
                                    winner.incKills();
                                    winner.setState("Win");
                                    if (looser.equals(winner)) {
                                        looser.decKills(); // first, revert the initial kill
                                        looser.decKills(); // second, "steal" one of the user kills (suicide is a crime)
                                    }
                                    revertState(looser);
                                    // clear the block containing the character
    //                            map.get(roomNr).chars[x][y].remove(looser.getId());
                                    it.remove();
                                }
                            }
                            catch (ConcurrentModificationException ex){
    //                            BLogger.getInstance().logException2(ex);
                            }
                        }
                    }
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

    /**
     * Protected synchronized method used to revert the state of a given player
     * to "Normal"
     *
     * @param myChar The current connected player
     */
//    protected synchronized void revertState(final Session peer, final BCharacter myChar) {
    protected synchronized void revertState(final BCharacter myChar) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                //int roomNr = getRoom(peer);
                int roomNr = myChar.roomIndex;
                playSoundAll(roomNr, "sounds/burn.wav");
                myChar.setState("Blow");
                charsChanged.put(roomNr, true);
                myChar.setWalking(false);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    BLogger.getInstance().logException2(ex);
                }
                myChar.setReady(false);
                myChar.setWalking(false);
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

    public synchronized void processHitBlock(String hitObject, String direction, final int x, final int y, Set<String> objectHits, final Explosion exp, final BCharacter crtChar){
        switch (hitObject) {
            case "bomb":
                new Thread(new Runnable() {
                    @Override
                    public /*synchronized*/ void run() {
//                    try {
//                        Thread.sleep(5);
//                    } catch (InterruptedException ex) {
//
//                    }
                        markForRemove((BBomb) map.get(crtChar.roomIndex).blockMatrix[x][y]);
                    }
                }).start();
                objectHits.add("direction");
                break;
            case "char":
//                new Thread(new Runnable() {
//                    @Override
//                    public /*synchronized*/  void run() {
//                    try {
//                        Thread.sleep(5);
//                    } catch (InterruptedException ex) {
//
//                    }
                        triggerBlewCharacter(crtChar, x, y);
//                    }
//                }).start();
                exp.ranges.put(direction, exp.ranges.get(direction) + 1);
                break;
            case "wall":
                AbstractWall wall = ((AbstractWall) map.get(crtChar.roomIndex).blockMatrix[x][y]);
                if (wall != null && (crtChar.getGold() || wall.isBlowable())) {
                    exp.ranges.put(direction, exp.ranges.get(direction) + 1);
//                    new Thread(new Runnable() {
//                        @Override
//                        public /*synchronized*/  void run() {
//                        try {
//                            Thread.sleep(5);
//                        } catch (InterruptedException ex) {
//                            
//                        }
                            flipForItems(crtChar.roomIndex, x, y);
//                        }
//                    }).start();
                    exp.directions.add(direction);
                }
                objectHits.add(direction);
                break;
            case "item":
                items.get(crtChar.roomIndex).remove(((AbstractItem) map.get(crtChar.roomIndex).blockMatrix[x][y]).itemId);
                map.get(crtChar.roomIndex).blockMatrix[x][y] = null;
                exp.ranges.put(direction, exp.ranges.get(direction) + 1);
                objectHits.add(direction);
                itemsChanged.put(crtChar.roomIndex, true);
                exp.directions.add(direction);
                break;
            default:
                exp.directions.add(direction);
                exp.ranges.put(direction, exp.ranges.get(direction) + 1);
                break;
        }
//        BLogger.getInstance().log(BLogger.LEVEL_FINE, "hit `"+hitObject+"` => "+direction);
    }
    
    /**
     * Public synchronized method used to detonate the bomb of a given player
     *
     * @param bomb The detonated bomb
     */
    public synchronized void markForRemove(final BBomb bomb) {

        if (bomb == null) {
            return;
        }

        try {
            final BCharacter crtChar = bomb.getOwner();
            
            final int roomNr = crtChar.roomIndex;
            if (roomNr == -1) {
                return;
            }
            
//            if (!markedBombs.get(roomNr).isEmpty()) return;
            
            playSoundAll(roomNr, "sounds/explosion.wav");
            crtChar.decPlantedBombs();

            Explosion exp = new Explosion(bomb.getOwnerOrig()); // get the real position of the bomb
            Set<String> objectHits = Collections.synchronizedSet(new HashSet<String>());
            map.get(roomNr).blockMatrix[bomb.getBlockPosX()][bomb.getBlockPosY()] = null; // clear the bomb's position
            int charRange = crtChar.getBombRange();

            markedBombs.get(roomNr).put(bomb.objId, bomb);
            explosions.get(roomNr).put(exp.objId, exp);

            explosionsChanged.put(roomNr, true);
            bombsChanged.put(roomNr, true);
            //mapChanged.put(roomNr, true);

            int posX    = bomb.getPosX();
            int posY    = bomb.getPosY();
            int width   = bomb.getWidth();
            int height  = bomb.getHeight();
            int wWidth  = map.get(roomNr).getWidth();
            int wHeight = map.get(roomNr).getHeight();
            final int blockX  = bomb.getBlockPosX();
            final int blockY  = bomb.getBlockPosY();

            /**
             * check to see if the explosion hits anything within it's
             * range
             */
            // in it's  current position                    
            if (posX + width <= wWidth && BombermanWSEndpoint.characterExists(crtChar.roomIndex, blockX, blockY)) {
                triggerBlewCharacter(crtChar, blockX, blockY);
            }
            
            // in it's external range
            for (int i = 1; i <= charRange; i++) {
                // right
                String checkedRight = BombermanWSEndpoint.checkWorldMatrix(roomNr, blockX + i, blockY);
                if (!objectHits.contains("right") && (posX + width * (i + 1) <= wWidth)) {
                    processHitBlock(checkedRight, "right", blockX + i, blockY, objectHits, exp, crtChar);
                }

                // left
                String checkedLeft = BombermanWSEndpoint.checkWorldMatrix(roomNr, blockX - i, blockY);
                if (!objectHits.contains("left") && (posX - width * i >= 0)) {
                    processHitBlock(checkedLeft, "left", blockX - i, blockY, objectHits, exp, crtChar);
                }

                // down
                String checkedDown = BombermanWSEndpoint.checkWorldMatrix(roomNr, blockX, blockY + i);
                if (!objectHits.contains("down") && (posY + height * (i + 1) <= wHeight)) {
                    processHitBlock(checkedDown, "down", blockX, blockY + i, objectHits, exp, crtChar);
                }

                // up
                String checkedUp = BombermanWSEndpoint.checkWorldMatrix(roomNr, blockX, blockY - i);
                if (!objectHits.contains("up") && (posY - height * i >= 0)) {
                    processHitBlock(checkedUp, "up", blockX, blockY - i, objectHits, exp, crtChar);
                }
            }

            explosionsChanged.put(roomNr, true);
            bombsChanged.put(roomNr, true);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(100); // wait .1 second before actual removing
//                        explosions.get(roomNr).remove(exp.objId);
                        markedBombs.get(roomNr).remove(bomb.objId);
                        bombs.get(roomNr).remove(bomb.objId);

                        explosionsChanged.put(roomNr, true);
                        bombsChanged.put(roomNr, true);

                        //mapChanged.put(roomNr, true);
                    } catch (InterruptedException ex) {
                        BLogger.getInstance().logException2(ex);
                    }
                }
            }).start();
            
        } catch (Exception ex) {
            BLogger.getInstance().logException2(ex);
        }
                
    }

    /**
     * Protected synchronized method used to check if there is an item behind a
     * given blown wall
     *
     * @param mapNr The map number
     * @param x The x coordinate of the checked position
     * @param y The y coordinate of the checked position
     */
    protected synchronized void flipForItems(final int mapNr, final int x, final int y) {
        wallsChanged.put(mapNr, true);
        itemsChanged.put(mapNr, true);
        if (!map.get(mapNr).wallExists(x, y)) return;
        blownWalls.get(mapNr).add(((AbstractWall)map.get(mapNr).blockMatrix[x][y]).wallId);
        Random r = new Random();
        AbstractItem item = null;
        int rand = r.nextInt(1000000);
        if (rand % 5 == 0) { // ~20% chance to find a hidden item behind the wall ;))
            while (item == null){
                item = ItemGenerator.getInstance().generateRandomItem(map.get(mapNr).getWidth(), map.get(mapNr).getHeight());
                if (item == null){
//                    BLogger.getInstance().log(BLogger.LEVEL_INFO, "random generation failed...");
                }
                else{
                    item.setPosX(x/map.get(mapNr).getWidth());
                    item.setPosY(y/map.get(mapNr).getHeight());
                    break;
                }
            }
        }
        updateBlockItem(mapNr, item, x, y);
    }

    protected void updateBlockItem(final int mapNr, final AbstractItem item, final int x, final int y){
//        BLogger.getInstance().log(BLogger.LEVEL_INFO, item != null ? "item" : "empty");
        new Thread(new Runnable(){
            @Override
            public void run() {
                try {
                    Thread.sleep(15);
                } catch (InterruptedException ex) {}
                map.get(mapNr).blockMatrix[x][y] = item;
                if (item != null){
                    items.get(mapNr).put(item.itemId, item);
                }
                wallsChanged.put(mapNr, true);
                itemsChanged.put(mapNr, true);
            }
        }).start();
    }
    
    /**
     * Protected synchronized method used to check if a given bomb isn't already
     * marker for removal
     *
     * @param bomb The checked bomb
     * @return True if the given bomb is already marked
     */
    public synchronized boolean alreadyMarked(BBomb bomb) {
        return markedBombs.get(bomb.getOwner().roomIndex).containsKey(bomb);
    }

    /**
     * Protected synchronized method used to send the characters to a given
     * player
     *
     * @param peer The connected peer
     */
    protected synchronized void exportChars(final Session peer) {
        try{
            String ret = "";
            int roomNr = getRoom(peer);
            ret += peer.getId() + "[#chars#]";
            if (!chars.containsKey(roomNr)){
                roomNr--;
            }
//            BLogger.getInstance().log(BLogger.LEVEL_FINE, "export chars: "+roomNr);
            if (!chars.containsKey(roomNr)){
                return;
            }
            Iterator<Map.Entry<String, BCharacter>> it = chars.get(roomNr).entrySet().iterator();
            while (it.hasNext()){
                Map.Entry<String, BCharacter> pair = it.next();
                BCharacter crtChar = (BCharacter) pair.getValue();
                ret += crtChar.toString() + "[#charSep#]";
            }
            sendClearMessage("chars:[" + ret, peer);
//            BLogger.getInstance().log(BLogger.LEVEL_FINE, "export characters: "+ret);
        }
        catch (ConcurrentModificationException ex){
            BLogger.getInstance().logException2(ex);
        }
    }

    /**
     * Protected synchronized method used to send the map to a given player
     *
     * @param peer The connected peer
     */
    protected synchronized void exportMap(final Session peer) {
        sendClearMessage("map:[" + map.get(getRoom(peer)).toString(), peer);
    }

    /**
     * Protected synchronized method used to send the walls to a given player
     *
     * @param peer The connected peer
     */
    protected synchronized void exportWalls(final Session peer, int tries) {
        try{
            String ret = "";
            int roomNr = getRoom(peer);
            //BLogger.getInstance().log(BLogger.LEVEL_FINE, "exporting walls...");
            if (!blownWalls.containsKey(roomNr)) return;

            Iterator<String> it = blownWalls.get(roomNr).iterator();
            while (it.hasNext()){
                String wall = it.next();
                ret += wall + "[#brickSep#]";
            }
            sendClearMessage("blownWalls:[" + ret, peer);
        }
        catch (ConcurrentModificationException ex){
//            BLogger.getInstance().logException2(ex);
            if (tries < 3){
                exportWalls(peer, tries+1);
            }
        }
    }

    /**
     * Protected synchronized method used to send the bombs to a given player
     *
     * @param peer The connected peer
     */
    protected synchronized void exportBombs(final Session peer, int tries) {
        try{
            String ret = "";
            int roomNr = getRoom(peer);
            //BLogger.getInstance().log(BLogger.LEVEL_FINE, "exporting bombs...");
            if (!bombs.containsKey(roomNr)) return;
            Iterator<Map.Entry<String, BBomb>> it = bombs.get(roomNr).entrySet().iterator();
            while (it.hasNext()){
                Map.Entry<String, BBomb> pair = it.next();
                BBomb bomb = pair.getValue();
                ret += bomb.toString() + "[#bombSep#]";
            }
            sendClearMessage("bombs:[" + ret, peer);
        }
        catch (ConcurrentModificationException ex){
//            BLogger.getInstance().logException2(ex);
            if (tries < 3){
                exportBombs(peer, tries+1);
            }
        }
    }

    /**
     * Protected synchronized method used to send the explosions to a given
     * player
     *
     * @param peer The connected peer
     */
    protected synchronized void exportExplosions(final Session peer, int tries) {
        try{
            String ret = "";
            int roomNr = getRoom(peer);
            if (explosions.containsKey(roomNr)) {
                Iterator<Map.Entry<String, Explosion>> it = explosions.get(roomNr).entrySet().iterator();
                while (it.hasNext()){
                    Map.Entry<String, Explosion> pair = it.next();
                    Explosion exp = pair.getValue();
                    ret += exp.toString() + "[#explosionSep#]";
                }
            }
            sendClearMessage("explosions:[" + ret, peer);
        }
        catch (ConcurrentModificationException ex){
//            BLogger.getInstance().logException2(ex);
            if (tries < 3){
                exportExplosions(peer, tries+1);
            }
        }
    }

    /**
     * Protected synchronized method used to send the items to a given player
     *
     * @param peer The connected peer
     */
    protected synchronized void exportItems(final Session peer, int tries) {
        try{
            String ret = "";
            int roomNr = getRoom(peer);
            if (items.containsKey(roomNr)) {
                Iterator<Map.Entry<String, AbstractItem>> it = items.get(roomNr).entrySet().iterator();
                while (it.hasNext()){
                    Map.Entry<String, AbstractItem> pair = it.next();
                    AbstractItem item = pair.getValue();
                    ret += item.toString() + "[#itemSep#]";
                }
            }
            sendClearMessage("items:[" + ret, peer);
        }
        catch (ConcurrentModificationException ex){
//            BLogger.getInstance().logException2(ex);
            if (tries < 3){
                exportItems(peer, tries+1);
            }
        }
    }

    /**
     * Public method used to check if a given player can plant a new bomb
     *
     * @param crtChar The current connected player
     * @return True if the player can plant a new bomb
     */
    public boolean canPlantNewBomb(BCharacter crtChar) {
        return crtChar.getPlantedBombs() < crtChar.getMaxBombs();
    }

    /**
     * Public static synhronized method used to check the type of a given block
     * from the world
     *
     * @param roomNr The room to be checked
     * @param i The x coordinate of the checked position
     * @param j The y coordinate of the checked position
     * @return Strings expressing the type of the block existing at the given
     * position
     */
    public static synchronized String checkWorldMatrix(int roomNr, int i, int j) {
        AbstractBlock[][] data = map.get(roomNr).blockMatrix;
        if (i < 0 || j < 0 || i >= data.length || j >= data[i].length) {
            return "empty";
        }
        String ret = "";
        try {
            Class<?> cls = (data[i][j] != null) ? data[i][j].getClass() : "".getClass();
            if (map.containsKey(roomNr) && !map.get(roomNr).chars[i][j].isEmpty()) {
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

    /**
     * Public method used to send make a given peer play a given sound
     *
     * @param sound The sound to be played
     * @param peer The connected peer
     */
    public void playSound(String sound, Session peer) {
        if (peer == null) {
            return;
        }
        sendClearMessage("sound:[" + sound, peer);
    }

    /**
     * Public method used to make all players from a given map play a given
     * sound
     *
     * @param roomNr The room number
     * @param sound The sound to be played
     */
    public void playSoundAll(int roomNr, String sound) {
        if (!chars.containsKey(roomNr)) return;
        Iterator<Map.Entry<String, BCharacter>> it = chars.get(roomNr).entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, BCharacter> pair = it.next();
            BCharacter crtChar = (BCharacter) pair.getValue();
            Session peer = crtChar.getPeer();
            if (peer == null) {
                continue;
            }
            playSound(sound, peer);
        }        
    }

    /**
     * Private static method returning the map number of a given player
     *
     * @param peer The connected peer
     * @return The room of the connected player
     */
    private static int getRoom(Session peer) {
        if (peer == null || !peer.getUserProperties().containsKey("room")){
            return -1;
        }        
        try {
            int roomNr = Integer.parseInt(peer.getUserProperties().get("room").toString());
            if (roomNr <= 0) {
                return -1;
            }
//            if (!peerRooms.containsKey(peer.getId())) {
//                peerRooms.put(peer.getId(), roomNr);
//            }
//            return peerRooms.get(peer.getId());
            return roomNr;
        } catch (NumberFormatException ex) {
            return -1;
        }
    }

    /**
     * Public synchronized method updating a player state, so that it can
     * actually actually
     *
     * @param peer The connected peer
     */
    public synchronized void makePlayerReady(Session peer) {
        int roomNr = getRoom(peer);
        if (roomNr == -1) {
            return;
        }
        BCharacter crtChar = chars.get(roomNr).get(peer.getId());
        crtChar.setReady(true);
    }

    /**
     * Public synchronized method used to place a player randomly on the map
     *
     * @param mapNumber The connected peer room
     * @param newChar The player associated to the connected peer
     */
    public synchronized void setCharPosition(final int mapNumber, final BCharacter newChar) {
        if (newChar == null) return;
        new Thread(new Runnable() {

            @Override
            public void run() {
                newChar.setWalking(false);
                Random r = new Random();
                int Low = 0;
                int HighW = map.get(mapNumber).getWidth() / World.wallDim - 1;
                int HighH = map.get(mapNumber).getHeight() / World.wallDim - 1;
                int X = r.nextInt(HighW - Low) + Low;
                int Y = r.nextInt(HighH) + Low;
                while (!BombermanWSEndpoint.checkWorldMatrix(mapNumber, X, Y).equals("empty")) {
                    X = r.nextInt(HighW - Low) + Low;
                    Y = r.nextInt(HighH) + Low;
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                    Logger.getLogger(BombermanWSEndpoint.class.getName()).log(Level.SEVERE, null, ex);
                }
                // character's previous position
                int X1 = newChar.getBlockPosX();
                int Y1 = newChar.getBlockPosY();
                newChar.setPosX(X * World.wallDim);
                newChar.setPosY(Y * World.wallDim);
                // clear character's previuos position
                map.get(mapNumber).chars[X1][Y1].remove(newChar.getId());
                map.get(mapNumber).chars[X][Y].add(newChar.getId());
                
            }

        }).start();
    }

    /**
     * Public method used to tell the player that he/she entered an invalid
     * email address
     *
     * @param peer The connected peer
     */
    public void sendInvalidEmailMessage(Session peer) {
        sendClearMessage("invalidAddress:[{}", peer);
    }

    /**
     * Public method used to tell the player that the email/username selected is
     * already registered
     *
     * @param peer The connected peer
     */
    public void sendAlreadyRegisteredMessage(Session peer) {
        sendClearMessage("alreadyTaken:[{}", peer);
    }

    /**
     * Public method used to tell the player that he/she is ready to play
     *
     * @param peer The connected peer
     */
    public void sendReadyMessage(Session peer) {
//        BLogger.getInstance().log(BLogger.LEVEL_FINE, "player ready");
        sendClearMessage("ready:[{}", peer);
    }

    /**
     * Public method used to tell the player that he/she is registered as admin
     * in the app
     *
     * @param peer The connected peer
     */
    public void sendAdminModMessage(Session peer) {
        sendClearMessage("admin:[{}", peer);
    }

    /**
     * Public method used to tell the player that he/she doesn't have admin
     * privileges
     *
     * @param peer The connected peer
     */
    public void sendNotAdminMessage(Session peer) {
        sendClearMessage("notadmin:[{}", peer);
    }

    /**
     * Public method used to tell the player that the current map is full
     *
     * @param peer The connected peer
     */
    public void sendMapFullMessage(Session peer) {
        sendClearMessage("fullmap:[{}", peer);
    }
    
    /**
     * Public method used to send to a given admin the error message in case
     * he/she requested an invalid map
     * @param peer The connected peer
     */
    public void sendInvalidMapMessage(Session peer) {
        sendClearMessage("invalidmap:[{}", peer);
    }
    
    /**
     * Public method used to send to a given admin the status message for the command
     * that he/she requested for execution
     * @param peer The connected peer
     * @param msg The message to be sent
     */
    public void sendStatusMessage(Session peer, String msg) {
        sendClearMessage("status:[" + msg, peer);
    }

    /**
     * Public method used to tell the player that he's/she's successfully
     * registered
     *
     * @param peer The connected peer
     */
    public void sendRegistrationSuccessMessage(Session peer) {
        sendClearMessage("registerSuccess:[{}", peer);
    }

    /**
     * Public method used to tell the player that he/she could not be registered
     *
     * @param peer The connected peer
     */
    public void sendRegisterFailedMessage(Session peer) {
        sendClearMessage("registerFailed:[{}", peer);
    }

    /**
     * Public method used to tell the player that he/she could not log in
     *
     * @param peer The connected peer
     */
    public void sendLoginFailedMessage(Session peer) {
        sendClearMessage("loginFailed:[{}", peer);
    }

    /**
     * Public method used to tell the player that he/she must be logged in in
     * order to play
     *
     * @param peer The connected peer
     */
    public void sendLoginFirstMessage(Session peer) {        
        sendClearMessage("loginFirst:[{}", peer);
    }

    /**
     * Public method used to tell the player that he/she is banned from this
     * site
     *
     * @param peer The connected peer
     */
    public void sendBannedMessage(Session peer) {
        sendClearMessage("banned:[{}", peer);
    }

    /**
     * Public method used to send messages to the connected peer
     *
     * @param msg The message to be sent
     * @param peer The connected peer
     */
    public void sendClearMessage(String msg, Session peer) {
        if (peer == null) {
            return;
        }
        if (peer.isOpen()) {
            try {
                peer.getBasicRemote().sendText(msg);
            } catch (IOException | IllegalStateException | ConcurrentModificationException ex) {
                BLogger.getInstance().logException2(ex);
            }
        }
    }

    /**
     * Public method used to send chat messages to the connected peer
     *
     * @param msg The message to be sent
     * @param peer The connected peer
     */
    public void sendMessage(String msg, Session peer) {
        sendClearMessage("msg:[" + msg, peer);
    }

    /**
     * Public method used to send messages to all players connected to a room
     *
     * @param roomNr the room number
     * @param msg The message to be sent
     */
    public void sendMessageAll(int roomNr, String msg) {
        if (!chars.containsKey(roomNr)) return;
        Iterator<Map.Entry<String, BCharacter>> it = chars.get(roomNr).entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, BCharacter> pair = it.next();
            BCharacter crtChar = (BCharacter) pair.getValue();
            Session peer = crtChar.getPeer();
            if (peer == null) {
                continue;
            }
            sendMessage(msg, peer);
        }
    }

    /**
     * Private method used to store all messages sent from the players
     *
     * @param myChar the player that sent the message
     * @param msg The message sent by the player
     */
    private void logChatMessage(BCharacter myChar, String msg) {
        BChatMessage chatMsg = new BChatMessage(myChar.getUserId(), msg, myChar.roomIndex);
        chatMsg.saveToDB();
    }

    /**
     * Public method used to check the range of a bomb
     *
     * @param bomb the bomb to be tested
     * @param minReach the minimum explosion range
     * @return TRUE if the bomb has at least minReach range
     */
    public boolean bombReaches(BBomb bomb, int minReach) {
        return bomb.getOwner().getBombRange() >= minReach;
    }

    /**
     * Public method used to initiate the login protocol
     *
     * @param peer the connected peer
     * @param credentials the credentials for login
     * @param config The endpoint config, containing information about the peer
     */
    public void loginProtocol(Session peer, String credentials, EndpointConfig config) {
        String username = decodeBase64(credentials.substring(0, credentials.indexOf("#")));
        String password = decodeBase64(credentials.substring(credentials.indexOf("#") + 1));
//        BLogger.getInstance().log(BLogger.LEVEL_FINE, "login :  " + username + ", " + password);
        logIn(peer, username, password, config);
    }

    /**
     * Public method used to initiate the register protocol
     *
     * @param peer the connected peer
     * @param credentials the credentials for login
     * @param config The endpoint config, containing information about the peer
     */
    public void registerProtocol(Session peer, String credentials, EndpointConfig config) {
        String username = decodeBase64(credentials.substring(0, credentials.indexOf("#")));
        String password = decodeBase64(credentials.substring(credentials.indexOf("#") + 1, credentials.lastIndexOf("#")));
        String email = decodeBase64(credentials.substring(credentials.lastIndexOf("#") + 1));
        //System.out.println("register :  " + username + ", " + password + ", " + email);
        register(peer, username, password, email, config);
    }

    /**
     * Public method used to change a character name
     *
     * @param crtChar the connected player
     * @param name the new name
     * @param roomNr room of the connected player
     */
    public void changeNameProtocol(BCharacter crtChar, String name, int roomNr) {
        if (name.length() > 0) {
            String initialName = crtChar.getName();
            crtChar.setName(name);
            charMapByName.remove(initialName);
            charMapByName.put(name, new AbstractMap.SimpleEntry(roomNr, crtChar.getId()));
            sendMessageAll(roomNr, "<b>" + initialName + " is now known as <u>" + name + "</u> </b>");
        }
    }

    /**
     * Public method used by admins to force a given player to detonate a bomb
     *
     * @param peer The connected admin
     * @param charName The name of the player to detonate a bomb
     * @param roomNr The room of the players
     */
    public void detonateBombProtocol(Session peer, String charName, int roomNr) {
        if (!isAdmin(peer)) {
            sendNotAdminMessage(peer);
            return;
        }

        if (charName.length() == 0) {
            sendStatusMessage(peer, "Usage : `detonateBomb <i>&lt;charName&gt;</i>`");
            return;
        }

        BCharacter changedChar = findCharByName(charName);
        if (changedChar == null) {
            sendStatusMessage(peer, charName + " is not connected dummy ;)");
        } else {
            detonateProtocol(changedChar, roomNr);
        }
    }

    /**
     * Public method used by admins to force a given player to drop a bomb
     *
     * @param peer The connected admin
     * @param charName The name of the player to drop a bomb
     * @param roomNr The room of the players
     */
    public void dropBombProtocol(Session peer, String charName, int roomNr) {
        if (!isAdmin(peer)) {
            sendNotAdminMessage(peer);
            return;
        }

        if (charName.length() == 0) {
            sendStatusMessage(peer, "Usage : `dropBomb <i>&lt;charName&gt;</i>`");
            return;
        }

        BCharacter changedChar = findCharByName(charName);
        if (changedChar == null) {
            sendStatusMessage(peer, charName + " is not connected dummy ;)");
        } else {
            bombProtocol(changedChar, roomNr);
        }

    }

    /**
     * Public method used by admins to change a given player's name
     *
     * @param peer The connected admin
     * @param toProcess The name of the player and the new name
     * @param roomNr The room of the players
     */
    public void renameProtocol(Session peer, String toProcess, int roomNr) {
        if (!isAdmin(peer)) {
            sendNotAdminMessage(peer);
            return;
        }

        if (toProcess.length() == 0 || !toProcess.contains(" ")) {
            sendStatusMessage(peer, "Usage : `rename <i>&lt;charName&gt;</i> <i>&lt;newName&gt;</i>`");
            return;
        }

        String charName,
                newName;

        charName = toProcess.substring(0, toProcess.indexOf(" ")).trim();
        newName = toProcess.substring(toProcess.indexOf(" ")).trim();

        if (charName.length() > 0) {
            BCharacter changedChar = findCharByName(charName);
            if (changedChar == null) {
                sendStatusMessage(peer, charName + " is not connected dummy ;)");
            } else {
                changeNameProtocol(changedChar, newName, roomNr);
                sendStatusMessage(peer, "Renamed `" + charName + "` to `" + newName + "`");
            }
        }
    }

    /**
     * Public method used to send a message to all players from a given room
     * (chat system)
     *
     * @param peer the connected player
     * @param msg the message to be sent
     * @param roomNr room of the connected player
     */
    public void messageProtocol(Session peer, String msg, int roomNr) {
        BCharacter crtPlayer = chars.get(roomNr).get(peer.getId());
        if (crtPlayer == null) return;
        if (msg.length() > 0) {
            logChatMessage(crtPlayer, msg);
            sendMessageAll(roomNr, "<b>" + crtPlayer.getName() + " : </b>" + msg);
        }
    }

    /**
     * Public method used to kick a given player
     *
     * @param peer the connected peer
     * @param name the name of the player to be kicked
     * @param config The endpoint config, containing information about the peer
     */
    public void kickProtocol(Session peer, String name, EndpointConfig config) {
        if (!isAdmin(peer)) {
            sendNotAdminMessage(peer);
            return;
        }
        if (name.length() == 0) {
            sendStatusMessage(peer, "Usage : `kick <i>&lt;charName&gt;</i>`");
            return;
        }

        BCharacter kickedChar = findCharByName(name);
        if (kickedChar == null) {
            sendStatusMessage(peer, name + " is not connected dummy ;)");
        } else {
            int roomNr = getRoom(peer);
            Session peer2 = kickedChar.getPeer();
            if (peer.equals(peer2)) {
                sendStatusMessage(peer, "You cannot kick yourself silly :>");
                BLogger.getInstance().log(BLogger.LEVEL_FINE, "kicking yourself");
            } else if (peer2 != null) {
                BLogger.getInstance().log(BLogger.LEVEL_FINE, "kicking user");
                this.onMessage("QUIT", peer2, config);
                sendStatusMessage(peer, name + " is out. Are you happy?");
            } else {
                BLogger.getInstance().log(BLogger.LEVEL_FINE, "kicking bot");
                kickBot((BBaseBot) kickedChar);
                sendStatusMessage(peer, "BOT "+name + " is out. Are you happy?");
            }
        }
    }

    /**
     * Public method used to change the position of a given player
     *
     * @param peer the connected peer
     * @param name the name of the player to be kicked
     * @param config The endpoint config, containing information about the peer
     */
    public void teleportProtocol(Session peer, String name, EndpointConfig config) {
        if (!isAdmin(peer)) {
            sendNotAdminMessage(peer);
            return;
        }
        if (name.length() == 0) {
            sendStatusMessage(peer, "Usage : `teleport <i>&lt;charName&gt;</i>`");
            return;
        }
        BCharacter freedChar = findCharByName(name);
        if (freedChar == null) {
            sendStatusMessage(peer, name + " is not connected dummy ;)");
        } else {
            setCharPosition(getRoom(peer), freedChar);
        }
    }

    /**
     * Public method used to change the current map
     *
     * @param peer the connected peer
     * @param mapName the name of the player to be kicked (can be either an
     * existing file or "random")
     * @param roomNr room of the connected player
     */
    public void changeMapProtocol(Session peer, String mapName, int roomNr) {
        if (!isAdmin(peer)) {
            sendNotAdminMessage(peer);
            return;
        }
        if (mapName.length() == 0) {
            sendStatusMessage(peer, "Usage : `map <i>&lt;$mapName|<b>random</b>&gt;</i>`");
            return;
        }
        if (mapName.trim().toLowerCase().equals("random")) {
            map.put(roomNr, WorldGenerator.getInstance().generateWorld(3000, 1800, 1200));
        } else {
            
            String mapPath = "maps/" + mapName + ".txt";
            File f = new File(mapPath);
            if(f.exists() && !f.isDirectory()) {
                map.put(roomNr, new World(mapPath));
            }
            else{
                sendInvalidMapMessage(peer);
                return;
            }
        }
        exportMap(peer);
        setCharPosition(roomNr, chars.get(roomNr).get(peer.getId()));
        if (mapName.length() > 0) {
            if (chars.get(roomNr) != null && !chars.get(roomNr).isEmpty()) {
                for (Map.Entry pairs : chars.get(roomNr).entrySet()) {
                    BCharacter crtChar = (BCharacter) pairs.getValue();
                    if (crtChar.getPeer().equals(null)) {
                        continue;
                    }
                    setCharPosition(roomNr, crtChar);
                }
            }
        }
    }
    
    /**
     * Public method used to reset the current game
     *
     * @param peer the connected peer
     * @param roomNr room of the connected player
     */
    public void resetAllProtocol(Session peer, int roomNr) {
        if (!isAdmin(peer)) {
            sendNotAdminMessage(peer);
            return;
        }
        
        for (Map.Entry pairs : chars.get(roomNr).entrySet()) {
            BCharacter crtChar = (BCharacter) pairs.getValue();
            if (crtChar.getPeer().equals(null)) {
                continue;
            }
            crtChar.resetEvolution();
        }
        String[] mapFile = ((World)map.get(roomNr)).getMapFile().split("/");
        this.changeMapProtocol(peer, mapFile[mapFile.length-1].substring(0,  mapFile[mapFile.length-1].indexOf(".")), roomNr);
        this.messageProtocol(peer, "GAME RESET`", roomNr);
    }

    /**
     * Public method used show the help menu to a given connected admin
     *
     * @param peer the connected peer
     */
    public void showHelpProtocol(Session peer) {
        if (!isAdmin(peer)) {
            sendNotAdminMessage(peer);
            return;
        }
        sendStatusMessage(peer, getHelpMenu());
    }

    /**
     * Public method used to send to the requesting peer (if admin) a list
     * of the current existing maps
     * @param peer The connected peer
     */
    public void listMapsProtocol(Session peer){
        if (!isAdmin(peer)) {
            sendNotAdminMessage(peer);
            return;
        }
        sendStatusMessage(peer, getMaps());
    }
    
    /**
     * Public method used to add a bot to the game
     *
     * @param peer the connected peer
     * @param botType the bot to be added type (1 - medium, 0 - dumb)
     * @param roomNr room of the connected player
     */
    public void addBotProtocol(Session peer, int botType, int roomNr) {
        if (!this.isAdmin(peer)) {
            sendNotAdminMessage(peer);
        } else {
            if (mapPlayers.get(roomNr) == MAX_PLAYERS){
                sendMapFullMessage(peer);
            }
            else{
                startBot(addBot(peer, botType, roomNr));
            }
        }
    }

    /**
     * Public method used by the admins to add a static bot of a given type
     *
     * @param peer The requesting admin
     * @param roomNr The room of the game
     * @param botType The type of the bot to be added
     */
    public void addTestBotProtocol(Session peer, int roomNr, String botType) {
        if (!this.isAdmin(peer)) {
            sendNotAdminMessage(peer);
        }
        botType = botType.toLowerCase();
        if (!botType.equals("medium") && !botType.equals("dumb")) {
            sendStatusMessage(peer, "Usage : `addTestBot <i>&lt;dumb|medium&gt;</i>`");
            return;
        } else {
            if (botType.length() == 0) {
                sendStatusMessage(peer, "Usage : `addTestBot <i>&lt;dumb|medium&gt;</i>`");
                return;
            }
            
            BBaseBot bot;
            switch (botType) {
                case "medium":
                    bot = addMediumBot(peer, roomNr);
                    break;
                case "dumb":
                default:
                    bot = addDumbBot(peer, roomNr);
                    break;
            }
            bot.setRunning(false);
            sendStatusMessage(peer, "Test BOT " + bot.getName() + " added ;)");
        }
    }

    /**
     * Public method used by admins to run a given bot's Search&Destroy
     * directive
     *
     * @param peer The connected admin
     * @param botName The bot to start
     */
    public void startBotProtocol(Session peer, String botName) {
        if (!isAdmin(peer)) {
            sendNotAdminMessage(peer);
            return;
        }

        if (botName.length() > 0) {
            BBaseBot bot = (BBaseBot) findCharByName(botName);
            if (bot == null) {
                sendStatusMessage(peer, botName + " is not connected dummy ;)");
            } else {
                bot.setRunning(true);
            }
        } else {
            sendStatusMessage(peer, "Usage : `startBot <i>&lt;botName&gt;</i>`");
        }
    }

    /**
     * Public method used by admins to stop a given bot's Search&Destroy
     * directive
     *
     * @param peer The connected admin
     * @param botName The bot to start
     */
    public void stopBotProtocol(Session peer, String botName) {
        if (!isAdmin(peer)) {
            sendNotAdminMessage(peer);
            return;
        }

        if (botName.length() > 0) {
            BBaseBot bot = (BBaseBot) findCharByName(botName);
            if (bot == null) {
                sendStatusMessage(peer, botName + " is not connected dummy ;)");
            } else {
                bot.setRunning(false);
            }
        } else {
            sendStatusMessage(peer, "Usage : `stopBot <i>&lt;botName&gt;</i>`");
        }
    }

    /**
     * Public method used by admins to run a given bot's Search&Destroy
     * directive (only one step)
     *
     * @param peer
     * @param botName
     */
    public void searchAndDestroyProtocol(Session peer, String botName) {
        if (!isAdmin(peer)) {
            sendNotAdminMessage(peer);
            return;
        }

        if (botName.length() > 0) {
            BBaseBot bot = (BBaseBot) findCharByName(botName);
            if (bot == null) {
                sendStatusMessage(peer, botName + " is not connected dummy ;)");
            } else {
                bot.searchAndDestroy();
            }
        } else {
            sendStatusMessage(peer, "Usage : `searchAndDestroy <i>&lt;botName&gt;</i>`");
        }
    }

    /**
     * Public method used by the admins to make a given player walk to a
     * given(or random) direction
     *
     * @param peer The connected admin
     * @param toProcess The name of the player (and a direction if random is not
     * a gool alternative)
     */
    public void moveCharProtocol(Session peer, String toProcess) {

        if (!isAdmin(peer)) {
            sendNotAdminMessage(peer);
            return;
        }

        String charName,
                direction = "";

        if (toProcess.contains(" ")) {
            charName = toProcess.substring(0, toProcess.indexOf(" ")).trim();
            direction = toProcess.substring(toProcess.indexOf(" ")).trim();
        } else {
            charName = toProcess;
        }

        if (charName.length() > 0) {
            BCharacter movedChar = findCharByName(charName);
            if (movedChar == null) {
                sendStatusMessage(peer, charName + " is not connected dummy ;)");
            } else {
                if (direction.equals("")) {
                    movedChar.moveRandom();
                } else {
                    this.moveProtocol(movedChar, direction, getRoom(peer));
                }
                sendStatusMessage(peer, charName + " moved " + direction);
            }
        } else {
            sendStatusMessage(peer, "Usage : `moveChar <i>&lt;charName&gt;</i>[ <i><b>up|down|left|right</b></i>]`");
        }
    }

    /**
     * Public method used to move a given character
     *
     * @param crtChar the connected player
     * @param direction the direction to move to
     * @param roomNr room of the connected player
     */
    public void moveProtocol(BCharacter crtChar, String direction, int roomNr) {
//        BLogger.getInstance().log(BLogger.LEVEL_FINE, "moving "+direction);
        crtChar.setDirection(direction);
        boolean hasCollisions = map.get(roomNr).hasMapCollision(crtChar);
        if (hasCollisions){
//            BLogger.getInstance().log(BLogger.LEVEL_FINE, "collision "+direction);
            crtChar.setWalking(false);
            Session peer = crtChar.getPeer();
            if (peer != null){
                exportChars(crtChar.getPeer());
            }
        }
        else if (!crtChar.isWalking()) {
//            BLogger.getInstance().log(BLogger.LEVEL_FINE, "new move "+direction);
            crtChar.move(direction);
        }
//        else if (crtChar.isWalking()){
//            BLogger.getInstance().log(BLogger.LEVEL_FINE, "already moving");
//        }
        charsChanged.put(roomNr, true);
    }

    /**
     * Public method used to drop a bomb for a given player
     *
     * @param crtChar the connected player
     * @param roomNr room of the connected player
     */
    public void bombProtocol(BCharacter crtChar, int roomNr) {
        crtChar.addOrDropBomb(); // change character state
        boolean isAllowed = canPlantNewBomb(crtChar);
        if (isAllowed && crtChar.getState().equals("Normal")) { // if he dropped the bomb, add the bomb to the screen
            final BBomb b = new BBomb(crtChar);
            if (map.get(roomNr).bombExists(b.getBlockPosX(), b.getBlockPosY())) {
                return;
            }
            BombermanWSEndpoint.bombs.get(roomNr).put(b.objId, b);
            map.get(roomNr).blockMatrix[b.getBlockPosX()][b.getBlockPosY()] = b;
            crtChar.incPlantedBombs();
        } else if (!isAllowed) {
            crtChar.addOrDropBomb();
        }
        charsChanged.put(roomNr, true);
        bombsChanged.put(roomNr, true);
    }

    /**
     * Public method used to detonate the bomb of a given player
     *
     * @param crtChar the connected player
     * @param roomNr room of the connected player
     */
    public void detonateProtocol(BCharacter crtChar, int roomNr) {
        if (crtChar.isTriggered()) {
            detonateBomb(crtChar);
            bombsChanged.put(roomNr, true);
        }
    }

    /**
     * Public method used to change the state of a given player
     *
     * @param crtChar the connected player
     * @param roomNr room of the connected player
     * @param state the new state
     */
    public void changeStateProtocol(BCharacter crtChar, int roomNr, String state) {
        crtChar.setState(state);
        charsChanged.put(roomNr, true);
    }

    /**
     * Public method used to close the connection of a given player
     *
     * @param peer the connected peer
     * @param crtChar the connected player
     * @param config The endpoint config, containing information about the peer
     */
    public void quitProtocol(Session peer, BCharacter crtChar, EndpointConfig config) {
        String initialName = crtChar.getName();
        charMapByName.remove(initialName);
        HttpSession httpSession = (HttpSession) config.getUserProperties().get(HttpSession.class.getName());
        String sessionId = httpSession.getId();
        httpSession.invalidate();
        httpSessions.remove(sessionId);
        httpSessions.remove(peer.getUserProperties().get("sessionId").toString());
        config.getUserProperties().remove(HttpSession.class.getName());
        peer.getUserProperties().remove("sessionId");
        peer.getUserProperties().remove("loggedIn");
        peer.getUserProperties().remove("username");
        peer.getUserProperties().remove("user_id");
        this.onClose(peer);
    }

    /**
     * Public method used by the admin to find a given player's IP address
     *
     * @param peer The connected admin
     * @param name The name of the tested player
     */
    public void getIPProtocol(Session peer, String name) {
        if (!isAdmin(peer)) {
            sendNotAdminMessage(peer);
            return;
        }
        if (name.length() > 0) {
            BCharacter freedChar = findCharByName(name);
            if (freedChar == null) {
                sendStatusMessage(peer, name + " is not connected dummy ;)");
            } else {
                Session foundPeer = freedChar.getPeer();
                if (foundPeer == null) { // bot found
                    sendStatusMessage(peer, name + " is a bot silly ;)");
                } else {
                    sendStatusMessage(peer, name + " : " + foundPeer.getUserProperties().get("ip"));
                }
            }
        } else {
            sendStatusMessage(peer, "Usage : `getip <i>&lt;charName&gt;</i>`");
        }
    }

    /**
     * Public method used by the admin to ban a given IP address
     *
     * @param peer The connected admin
     * @param ip The IP to be banned
     * @param config The server configuration
     */
    public void banIPProtocol(Session peer, String ip, EndpointConfig config) {
        if (!isAdmin(peer)) {
            sendNotAdminMessage(peer);
            return;
        }
        if (ip.length() > 0) {
            int roomNr = getRoom(peer);
            if (roomNr == -1) {
                return;
            }
            BCharacter admin = chars.get(roomNr).get(peer.getId());
            banIP(admin, ip);
            sendStatusMessage(peer, ip + " has been banned");
            
            Iterator<Map.Entry<Integer, Map<String, BCharacter>>> it = chars.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<Integer, Map<String, BCharacter>> pairs = it.next();
                Iterator<Map.Entry<String, BCharacter>> it2 = pairs.getValue().entrySet().iterator();
                while (it2.hasNext()) {
                    Map.Entry<String, BCharacter> pair = it2.next();
                    BCharacter crtChar = (BCharacter) pair.getValue();
                    Session peer2 = crtChar.getPeer();
                    if (peer2.equals(null)) {
                        continue;
                    }
                    if (peer2.getUserProperties().get("ip").equals(ip)) {
                        this.onMessage("QUIT", peer2, config);
                    }
                }
            }
            
        } else {
            sendStatusMessage(peer, "Usage : `banIP <i>&lt;charIP&gt;</i>`");
        }
    }

    /**
     * Public method used by the server to store a given IP in the banlist
     *
     * @param admin The requesting admin
     * @param ip The IP to be stored
     */
    public void banIP(BCharacter admin, String ip) {
        try {
            String query = "INSERT INTO `banlist` VALUES("
                    + " NULL, ?, ?, '"+this.getMySQLDateTime()+"'"
                    + ")";
            PreparedStatement st2 = (PreparedStatement) BombermanWSEndpoint.con.prepareStatement(query);
            System.out.println(st2.toString());
            st2.setString(1, ip);
            st2.setInt(2, admin.getDbId());
            st2.execute();
        } catch (SQLException ex) {
            BLogger.getInstance().logException2(ex);
        }
    }

    /**
     * Public method used by the admin to unban a given IP address
     *
     * @param peer The connected admin
     * @param ip The IP to be unbanned
     */
    public void unbanIPProtocol(Session peer, String ip) {
        if (!isAdmin(peer)) {
            sendNotAdminMessage(peer);
            return;
        }
        if (ip.length() > 0) {
            unbanIP(ip);
            sendStatusMessage(peer, ip + " has been unbanned");
        } else {
            sendStatusMessage(peer, "Usage : `unbanIP <i>&lt;charIP&gt;</i>`");
        }
    }

    /**
     * Public method used by the server to delete a given IP from the banlist
     *
     * @param ip The IP to be stored
     */
    public void unbanIP(String ip) {
        try {
            String query = "DELETE FROM `banlist` WHERE `user_ip`=?";
            PreparedStatement st2 = (PreparedStatement) BombermanWSEndpoint.con.prepareStatement(query);
            st2.setString(1, ip);
            st2.execute();
        } catch (SQLException ex) {
            BLogger.getInstance().logException2(ex);
        }
    }

    /**
     * Public method used by the server to check if a given peer is banned based
     * on it's current IP address
     *
     * @param peer The connected peer
     * @param ip The peer's IP address
     */
    public boolean isBanned(Session peer, String ip) {
        if (ip.length() > 0) {
            try {
                String query = "SELECT 1 FROM `banlist` WHERE `user_ip`=?";
                PreparedStatement st2 = (PreparedStatement) BombermanWSEndpoint.con.prepareStatement(query);
                st2.setString(1, ip);
                ResultSet ret = st2.executeQuery();
                if (ret.next()) {
                    return true;
                }
            } catch (SQLException ex) {
                BLogger.getInstance().logException2(ex);
                return false;
            }
            return false;
        }
        return false;
    }

    /**
     * Public method used by the server to send the chat history to a given 
     * connected peer
     * @param peer The connected peer
     * @param roomNr The room of the connected peer
     * @param firstMessageID The ID of the last message (can be 0)
     */
    public void exportChatProtocol(Session peer, int roomNr, int firstMessageID){
        if (firstMessageID == 0 ) firstMessageID = Integer.MAX_VALUE;
        try {
            String query = "SELECT a.* FROM ("
                    + " SELECT u.username, u.id, m.message_time, m.message, m.id as chat_id"
                    + " FROM chat_message m JOIN user u ON m.user_id=u.id"
                    + " WHERE room_nr=?"
                    + "   AND m.id < ?"
                    + " ORDER BY message_time DESC"
                    + " LIMIT 100"
                    + ") a"
                    + " ORDER BY message_time ASC";
//            BLogger.getInstance().log(BLogger.LEVEL_INFO, query);
            PreparedStatement st2 = (PreparedStatement) BombermanWSEndpoint.con.prepareStatement(query);
            st2.setInt(1, roomNr);
            st2.setInt(2, firstMessageID);
            //System.out.println(st2.toString());
            ResultSet ret = st2.executeQuery();
            ArrayList<BChatMessage> chatLog = new ArrayList<BChatMessage>();
            while (ret.next()){
                BChatMessage chatMsg = new BChatMessage(ret.getInt("id"), ret.getString("message"), roomNr);
                chatMsg.setTimestamp(ret.getString("message_time"));
                chatMsg.setAuthor(ret.getString("username"));
                chatMsg.setId(ret.getInt("chat_id"));
                chatLog.add(chatMsg);
            }
            sendClearMessage("chatLog:["+chatLog.toString(), peer);
        } catch (SQLException ex) {
            BLogger.getInstance().logException2(ex);
        }
    }
    
    /**
     * Public method used to see if a connected peer has access to the game
     * @param peer The connected peer
     * @param ip The peer's IP address
     */
    public void checkBannedProtocol(Session peer, String ip){
        if (this.isBanned(peer, ip)){
            this.sendBannedMessage(peer);
        }
    }
    
    /**
     * Public method used to format the current datetime as a MySQL DATETIME String
     * @return The datetime value as a MySQL DATETIME string
     */
    public String getMySQLDateTime(){
        java.util.Date dt = new java.util.Date();
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(dt);
    }
    
}
