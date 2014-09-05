/**
 * Database queries.
 *
 * CREATE DATABASE `bomberman`;
 *
 * GRANT ALL PRIVILEGES ON `bomberman`.* TO 'bomberman'@'%' IDENTIFIED BY
 * 'bomberman';
 *
 * USE `bomberman`;
 *
 * CREATE TABLE `chat_message` ( `id` int(11) NOT NULL AUTO_INCREMENT, `user_id`
 * int(11) NOT NULL, `message_time` DATETIME NOT NULL, `message` TEXT NOT NULL,
 * PRIMARY KEY (`id`) ) ENGINE=InnoDB DEFAULT CHARSET=utf8;
 *
 * CREATE TABLE `characters` ( `id` int(11) NOT NULL AUTO_INCREMENT, `user_id`
 * int(11) NOT NULL, `name` varchar(128) NOT NULL, `speed` tinyint(2) NOT NULL,
 * `bomb_range` tinyint(2) NOT NULL, `max_bombs` tinyint(3) NOT NULL,
 * `triggered` tinyint(1) NOT NULL DEFAULT 0, `kills` int(11) NOT NULL DEFAULT
 * 0, `deaths` int(11) NOT NULL DEFAULT 0, `creation_time` DATETIME NULL,
 * `modification_time` DATETIME NULL, PRIMARY KEY (`id`) ) ENGINE=InnoDB DEFAULT
 * CHARSET=utf8;
 * 
 * CREATE TABLE `user` ( `id` int(11) NOT NULL AUTO_INCREMENT, `email`
 * varchar(256) NOT NULL, `username` varchar(256) NOT NULL, `password`
 * varchar(256) NOT NULL, `registered_at` DATETIME NULL, `last_login` DATETIME
 * NULL, PRIMARY KEY (`id`) ) ENGINE=InnoDB DEFAULT CHARSET=utf8;
 * 
 * alter table user add column `admin` tinyint(1) not null default '0' after `password`;
 * 
 */
package com.cux.bomberman;

import com.cux.bomberman.util.BBase64;
import com.cux.bomberman.util.BLogger;
import com.cux.bomberman.world.AbstractBlock;
import com.cux.bomberman.world.BBaseBot;
import com.cux.bomberman.world.BBomb;
import com.cux.bomberman.world.BCharacter;
import com.cux.bomberman.world.BDumbBot;
import com.cux.bomberman.world.BMediumBot;
import com.cux.bomberman.world.Explosion;
import com.cux.bomberman.world.World;
import com.cux.bomberman.world.generator.ItemGenerator;
import com.cux.bomberman.world.items.AbstractItem;
import com.cux.bomberman.world.walls.AbstractWall;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.http.HttpSession;
import javax.websocket.CloseReason;
import javax.websocket.EndpointConfig;
import javax.websocket.Extension;
import javax.websocket.MessageHandler;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.RemoteEndpoint;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

/**
 *
 * @author mihaicux Endpoint server
 */
@ServerEndpoint(value = "/bombermanendpoint/", configurator = BombermanHttpSessionConfigurator.class)
public class BombermanWSEndpoint {

    /**
     * Static variable. Collection used to keep track of all the opened
     * connections.<br />
     * Indexed by peer id
     */
    public static final Map<String, Session> peers = Collections.synchronizedMap(new HashMap<String, Session>());
    
    /**
     * Static variable. Collection used to find users by their nicknames ( really quick )<br />
     * Indexed by player name, stores peer id
     */
    public static final Map<String, String> charMapByName = Collections.synchronizedMap(new HashMap<String, String>());

    /**
     * Static variable. Collection used to keep track of all the existing
     * planted bombs.<br />
     * Indexed by peer map number.<br />
     * It is constantly changed/updated
     */
    public static final Map<Integer, ArrayList<BBomb>> bombs = Collections.synchronizedMap(new HashMap<Integer, ArrayList<BBomb>>());

    /**
     * Static variable. Collection used to keep track of all the existing bombs
     * marked for explosion.<br />
     * Indexed by peer map number.<br />
     * It is constantly changed/updated
     */
    private static final Map<Integer, Set<BBomb>> markedBombs = Collections.synchronizedMap(new HashMap<Integer, Set<BBomb>>());

    /**
     * Static variable. Collection used to keep track of all the connected
     * users.<br />
     * Indexed by peer id
     */
    private static final Map<String, BCharacter> chars = Collections.synchronizedMap(new HashMap<String, BCharacter>());

    /**
     * Static variable. Collection used to keep track of all the connected
     * users.<br />
     * Indexed by peer map number
     */
    private static final Map<Integer, Set<BCharacter>> chars2 = Collections.synchronizedMap(new HashMap<Integer, Set<BCharacter>>());

    /**
     * Static variable. Collection used to keep track of the threads used for
     * all the opened connections
     */
    private static final Set<String> workingThreads = Collections.synchronizedSet(new HashSet<String>());

    /**
     * Static variable. Collection used to keep track of all the current
     * explosions.<br />
     * Indexed by peer map number.<br />
     * It is constantly changed/updated
     */
    private static final Map<Integer, Set<Explosion>> explosions = Collections.synchronizedMap(new HashMap<Integer, Set<Explosion>>());

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
    public static Map<Integer, Set<AbstractItem>> items = Collections.synchronizedMap(new HashMap<Integer, Set<AbstractItem>>());

    /**
     * Static variable. Collection used to keep track of all the current
     * sessions<br />
     * Indexed by peer id.<br />
     * It is used to identify an already connected client
     */
    private static final Map<String, HttpSession> httpSessions = Collections.synchronizedMap(new HashMap<String, HttpSession>());

    /**
     * Static variable. Collection used to keep track of all the current maps<br
     * />
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
     * Static variable. Collection used to keep track of the number of players
     * connected to each map<br />
     * Indexed by peer map number
     */
    private static final Map<Integer, Integer> mapBots = Collections.synchronizedMap(new HashMap<Integer, Integer>());
    
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
    public static final Map<String, Integer> peerRooms = Collections.synchronizedMap(new HashMap<String, Integer>());

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
    private static final String DBUser = "bomberman";

    /**
     * Static constant. Used to define the database user password
     */
    private static final String DBPass = "bomberman";

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
     * @param message - String containting the message sent by the connected
     * peer
     * @param peer - The connected peer
     * @param config - The endpoint config, containg information about the peer
     * session (if any)
     * @return null - any returned message will be sent back to the peer
     */
    @OnMessage
    public synchronized String onMessage(String message, final Session peer, EndpointConfig config) {

        // player is ready to join the game
        if (message.equals("ready")) {
            makePlayerReady(peer);
            return null;
        }

        // send the world data to the current peer (one time only :> )
        if (message.equals("getEnvironment")) {
            exportEnvironment(peer);
            return null;
        }

        // login
        if (message.length() > 6 && message.substring(0, 6).toLowerCase().equals("login ")) {
            String credentials = message.substring(6).trim();
            String username = decodeBase64(credentials.substring(0, credentials.indexOf("#")));
            String password = decodeBase64(credentials.substring(credentials.indexOf("#") + 1));
            //System.out.println("login :  " + username + ", " + password);
            logIn(peer, username, password, config);
            return null;
        }

        // register
        if (message.length() > 9 && message.substring(0, 9).toLowerCase().equals("register ")) {
            String credentials = message.substring(9).trim();
            String username = decodeBase64(credentials.substring(0, credentials.indexOf("#")));
            String password = decodeBase64(credentials.substring(credentials.indexOf("#") + 1, credentials.lastIndexOf("#")));
            String email = decodeBase64(credentials.substring(credentials.lastIndexOf("#") + 1));
            //System.out.println("register :  " + username + ", " + password + ", " + email);
            register(peer, username, password, email, config);
            return null;
        }

        // any other message must be from an already logged in player
        BCharacter crtChar = null;
        int roomNr = 0;
        if (peer.getUserProperties().get("loggedIn").equals(true)) {
            crtChar = chars.get(peer.getId());
            roomNr = getRoom(peer);
        }

        if (crtChar == null) {
            return null; // message from non-logged in user
        }
        
        // change player name
        if (message.length() > 5 && message.substring(0, 5).toLowerCase().equals("name ")) {
            String name = message.substring(message.indexOf(" ")).trim();
            if (name.length() > 0) {
                String initialName = crtChar.getName();
                crtChar.setName(name);
                sendMessageAll(roomNr, "<b>" + initialName + " is now known as <u>" + name + "</u> </b>");
                return null;
            }
        }

        // the current player sends a message to all players from the same room
        if (message.length() > 4 && message.substring(0, 4).toLowerCase().equals("msg ")) {
            //System.out.println("message for chat");
            String msg = message.substring(message.indexOf(" ")).trim();
            if (msg.length() > 0) {
                logChatMessage(chars.get(peer.getId()), msg);
                sendMessageAll(roomNr, "<b>" + chars.get(peer.getId()).getName() + " : </b>" + msg);
                return null;
            }
        }

        // the current player tries to kick a player out of the game :>
        if (message.length() > 5 && message.substring(0, 5).toLowerCase().equals("kick ")){
            if (!isAdmin(peer)){
                sendNotAdminMessage(peer);
                return null;
            }
            String name = message.substring(message.indexOf(" ")).trim();
            if (name.length() > 0){
                BCharacter kickedChar = findCharByName(name);
                if (kickedChar == null){
                    sendStatusMessage(peer, name + " is not connected dummy ;)");
                }
                else{
                    BCharacter crtPlayer = chars.get(peer.getId());
                    if (crtPlayer != null && name.equals(crtPlayer.getName())){
                        sendStatusMessage(peer, "You cannot kick yourself silly :>");
                        return null;
                    }
                    else if (peers.containsKey(kickedChar.getId())){
                        this.onMessage("QUIT", peers.get(kickedChar.getId()), config);
                    }
                    else{
                        kickBot((BBaseBot)kickedChar);
                    }
                    sendStatusMessage(peer, name + " is out. Are you happy?");
                }
            }
            return null;
        }
        
        if (message.toLowerCase().equals("help")){
            if (!isAdmin(peer)){
                sendNotAdminMessage(peer);
                return null;
            }
            sendStatusMessage(peer, getHelpMenu());
            return null;
        }
        
        String stdMessage = message.toLowerCase();
        
        // other messages from the current player
        switch (stdMessage) {
            case "addmediumbot":
                if (!this.isAdmin(peer)){
                    sendNotAdminMessage(peer);
                }
                else{
                    addBot(peer, 1, getRoom(peer));
                }
                break;
            case "adddumbbot":
                if (!this.isAdmin(peer)){
                    sendNotAdminMessage(peer);
                }
                else{
                    addBot(peer, 0, getRoom(peer));
                }
                break;
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
                boolean isAllowed = canPlantNewBomb(crtChar);
                if (isAllowed && crtChar.getState().equals("Normal")) { // if he dropped the bomb, add the bomb to the screen
                    final BBomb b = new BBomb(crtChar);
                    if (bombExists(map.get(roomNr).blockMatrix, b.getPosX() / World.wallDim, b.getPosY() / World.wallDim)) {
                        break;
                    }
                    BombermanWSEndpoint.bombs.get(roomNr).add(b);
                    map.get(roomNr).blockMatrix[b.getPosX() / World.wallDim][b.getPosY() / World.wallDim] = b;
                    crtChar.incPlantedBombs();
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
            case "quit":
                String initialName = crtChar.getName();
                System.out.println(initialName + " has left the game");
                charMapByName.remove(initialName);
                HttpSession httpSession = (HttpSession) config.getUserProperties().get(HttpSession.class.getName());
                String sessionId = httpSession.getId();
                httpSession.invalidate();
                httpSessions.remove(sessionId);
                httpSessions.remove(peer.getUserProperties().get("sessionId"));
                config.getUserProperties().remove(HttpSession.class.getName());
                peer.getUserProperties().remove("sessionId");
                peer.getUserProperties().remove("loggedIn");
                peer.getUserProperties().remove("username");
                peer.getUserProperties().remove("user_id");
                this.onClose(peer);

                if (!httpSessions.isEmpty() && httpSessions.containsKey(sessionId)) {
                    if (!peers.isEmpty()) {
                        Iterator it = peers.entrySet().iterator();
                        while (it.hasNext()) {
                            Map.Entry pairs = (Map.Entry) it.next();
                            Session peer2 = (Session) pairs.getValue();
                            // daca exista deja conectat, inchide vechea conexiune
                            if (peer2.getUserProperties().containsKey("sessionId") && peer2.getUserProperties().get("sessionId").equals(sessionId)) {
                                this.onMessage("QUIT", peer2, config);
                            }
                        }
                    }
                }
            default:
                if (isAdmin(peer)){
                    sendStatusMessage(peer, "command not found [ "+message+" ]\nTYPE `help` to see the available commands");
                }
                break;
        }

        //System.out.println(message);
        return null; // any string will be send to the requesting peer
    }

    /**
     * Public synchronized method handling the closing connection of the peer
     *
     * @param peer - The connected peer
     */
    @OnClose
    public synchronized void onClose(Session peer) {
        int roomNr = getRoom(peer);
        String initialName = chars.get(peer.getId()).getName();
        System.out.println(initialName + " has left the game");
        if (peer.isOpen()) {
            try {
                peer.close();
            } catch (IOException ex) {
                BLogger.getInstance().logException2(ex);
            }
        }

        BCharacter myChar = chars.get(peer.getId());
        int x = myChar.posX / World.wallDim;
        int y = myChar.posY / World.wallDim;
        map.get(myChar.roomIndex).chars[x][y].remove(myChar.getId());
        map.get(myChar.roomIndex).blockMatrix[x][y] = null;

        this.stopThread(peer.getId());
        chars2.get(roomNr).remove(myChar);
        chars.remove(peer.getId());
        peers.remove(peer);
//        httpSessions.remove(peer.getUserProperties().get("sessionId"));

        charsChanged.put(roomNr, true);
        //mapChanged.put(roomNr, true);
        mapPlayers.put(roomNr, mapPlayers.get(roomNr) - 1);
        System.out.println("out...");
//        if (peers.size() == 0){
//            BombermanWSEndpoint.initialized = false;
//        }
        playSoundAll(roomNr, "sounds/elvis.mp3");
        sendMessageAll(roomNr, "<b>ELVIS [ " + initialName + " ]  has left the building </b>");
    }

    /**
     * Public synchronized method handling a new connection
     *
     * @param peer - The connected peer
     * @param room - The room of the connected peer
     * @param config - The endpoint config, containg information about the peer
     * session (if any)
     */
    @OnOpen
    public synchronized void onOpen(Session peer, @PathParam("room") final String room, EndpointConfig config) {

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
            } catch (Exception e) {
                BLogger.getInstance().logException2(e);
            }
            
        }

        HttpSession httpSession = (HttpSession) config.getUserProperties().get(HttpSession.class.getName());
        String sessionId = httpSession.getId();

        // check to see if the user isn't already recognized by the server (has a known cookie)
        if (!httpSessions.isEmpty() && httpSessions.containsKey(sessionId)) {
//            System.out.println("session id recognized");
            if (!peers.isEmpty()) { // foreach stored httpSession
//                System.out.println("we have active connections");
                Iterator it = peers.entrySet().iterator();
                while (it.hasNext()) { // foreach connected peer
                    Map.Entry pairs = (Map.Entry) it.next();
                    Session peer2 = (Session) pairs.getValue();
                    // close the old connection
                    if (peer2.getUserProperties().containsKey("sessionId") && peer2.getUserProperties().get("sessionId").equals(sessionId)) {
                        //System.out.println("I know you");
                        // if the current user is already loggedIn, restore it's properties and close the old connection
                        if (peer2.getUserProperties().containsKey("loggedIn") && peer2.getUserProperties().get("loggedIn").equals(true)) {
                            //System.out.println("You are already logged in");
                            addPlayerToGame(peer, config, peer2.getUserProperties().get("username").toString(), Integer.valueOf(peer2.getUserProperties().get("user_id").toString()));
                            sendReadyMessage(peer);
                            if (peer2.getUserProperties().containsKey("isAdmin") && peer2.getUserProperties().get("isAdmin").equals(true)) {
                                makePlayerAdmin(peer);
                            }
                            this.onMessage("QUIT", peer2, config);
                            return;
                        } else { // else, just close the old connection
                            //System.out.println("Somehow, I feel like I know you...");
                            this.onMessage("QUIT", peer2, config);
                        }
                    }
                } // end loop for connected peers
            }
            //System.out.println("end httpSession loop");
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
     * @param peer - The connected peer
     * @param user - The name of the player trying to register
     * @param pass - The password of the player trying to register
     * @param email - The email address of the player trying to register
     * @param config - The endpoint config, containg information about the peer
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

            query = "INSERT INTO `user` SET `email`=?, `username`=?, `password`=?, `registered_at`=NOW()";
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
     * @param peer - The connected peer
     * @param user - The name of the player trying to register
     * @param pass - The password of the player trying to register
     * @param config - The endpoint config, containg information about the peer
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
                if (ret.getInt("admin")>0){
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
     * @param s - the string to be decoded
     * @return the decoded string
     */
    public static String decodeBase64(String s) {
        //return new String(Base64.decode(s));
        return BBase64.decode(s);
    }

    /**
     * Public static method that encodes a string using the base64 algorithm
     *
     * @param s - the string to be encoded
     * @return the encoded string
     */
    public static String encodeBase64(String s) {
//        return Base64.encode(s.getBytes());
        return BBase64.encode(s);
    }

    /**
     * Public static method that hashes a string using the MD5 algorithm
     *
     * @param message - the string to be hashed
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

    private String getHelpMenu(){
        return "cuxBomberman v.07\n"+
                "TYPE `addMediumBot` to add a new bot to the game\n"+
                "TYPE `addDumbBot` to add a new dumb bot to the game\n"+
                "TYPE `kick [username]` to remove a player from the game (bots included)\n"+
                "TYPE `help` to view this message again\n";
    }
    
    /**
     * Removes a bot from the game
     * @param kickedChar - the bot to be kicked
     */
    public void kickBot(BBaseBot kickedChar){
        try{
            kickedChar.setRunning(false);
            chars.remove(kickedChar.getId());
            chars2.get(kickedChar.roomIndex).remove(kickedChar);
        }
        catch(Exception e){
            BLogger.getInstance().logException2(e);
        }
    }
    
    public BCharacter findCharByName(String name){
        if (charMapByName.containsKey(name)){
            return chars.get(charMapByName.get(name));
        }
        return null;
    }
    
    /**
     * Public method used add a player to the game
     *
     * @param peer - The connected peer
     * @param config - The endpoint config, containg information about the peer
     * session (if any)
     * @param user - The name of the player trying to connect
     * @param user_id - The id of the player trying to connect
     */
    public void addPlayerToGame(Session peer, EndpointConfig config, String username, int user_id) {
        HttpSession httpSession = (HttpSession) config.getUserProperties().get(HttpSession.class.getName());
        String sessionId = httpSession.getId();

        peer.getUserProperties().put("loggedIn", true);
        peer.getUserProperties().put("username", username);
        peer.getUserProperties().put("user_id", user_id);
        peers.put(peer.getId(), peer);

        //sendMessage(httpSession.getId(), peer);
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
//            map.put(mapNumber, new World("/home/mihaicux/NetBeansProjects/bomberman_sf/maps/firstmap.txt"));
            map.put(mapNumber, new World("maps/firstmap.txt"));
//            map.put(mapNumber, new World("/home/mihaicux/projects/bomberman/maps/map2.txt"));
//            map.put(mapNumber, WorldGenerator.getInstance().generateWorld(3000, 1800, 1200));
            //BLogger.getInstance().log(BLogger.LEVEL_INFO, "created");
            
        }

        //BLogger.getInstance().log(BLogger.LEVEL_INFO, "peer connected2 ["+peer.getId()+"], room "+peer.getUserProperties().get("room"));
        BCharacter newChar = new BCharacter(peer.getId(), username, mapNumber, config);
        newChar.setPosX(0);
        newChar.setPosY(0);
        newChar.setWidth(World.wallDim);
        newChar.setHeight(World.wallDim);
        newChar.setUserId(user_id);
        newChar.restoreFromDB();
        newChar.logIn();
//        newChar.setPeer(peer);
        
        charMapByName.put(username, peer.getId());
        
        if (blownWalls.isEmpty() || blownWalls.get(mapNumber) == null) {
            blownWalls.put(mapNumber, new HashSet<String>());
        }

        if (markedBombs.isEmpty() || markedBombs.get(mapNumber) == null) {
            markedBombs.put(mapNumber, new HashSet<BBomb>());
        }

        if (chars2.isEmpty() || chars2.get(mapNumber) == null) {
            chars2.put(mapNumber, new HashSet<BCharacter>());
        }

        if (explosions.isEmpty() || explosions.get(mapNumber) == null) {
            explosions.put(mapNumber, new HashSet<Explosion>());
        }

        if (items.isEmpty() || items.get(mapNumber) == null) {
            items.put(mapNumber, new HashSet<AbstractItem>());
        }

        if (bombs.isEmpty() || bombs.get(mapNumber) == null) {
            bombs.put(mapNumber, new ArrayList<BBomb>());
        }

        if (wallsChanged.isEmpty() || wallsChanged.get(mapNumber) == null) {
            wallsChanged.put(mapNumber, true);
        }

        chars.put(peer.getId(), newChar);
        chars2.get(mapNumber).add(newChar);
        setCharPosition(mapNumber, newChar);
        
        BLogger.getInstance().log(BLogger.LEVEL_INFO, "peer connected [" + peer.getId() + "], room " + peer.getUserProperties().get("room"));
        //System.exit(0);
        charsChanged.put(mapNumber, true);
        bombsChanged.put(mapNumber, true);
        //mapChanged.put(mapNumber, true);
        itemsChanged.put(mapNumber, true);
        explosionsChanged.put(mapNumber, true);

        sendMessageAll(mapNumber, "<b>" + newChar.getName() + " has joined");
    }

    public void addBot(Session peer, int type, int roomNr){
        if (mapBots.get(roomNr) == null) {
            mapBots.put(roomNr, 1); // one bot in current map
        } else {
            mapBots.put(roomNr, 1 + mapBots.get(roomNr)); // another bot in the current map
        }
        switch (type){
            case 1:
                addMediumBot(peer, roomNr);
                break;
            default:
                addDumbBot(peer, roomNr);
                break;
        }
    }
    
    private void addMediumBot(Session peer, int roomNr){    
        String botName = "BOT_medium_"+mapBots.get(roomNr);
        BBaseBot bot = new BMediumBot(botName, botName, mapNumber, null);
        bot.setPosX(0);
        bot.setPosY(0);
        bot.setWidth(World.wallDim);
        bot.setHeight(World.wallDim);
        
        chars.put(botName, bot);
        chars2.get(roomNr).add(bot);
        setCharPosition(roomNr, bot);
        new Thread(bot).start();

        bot.setReady(true);

        System.out.println("medium_bot added...");
        sendStatusMessage(peer, "bot added - "+botName);
        charMapByName.put(botName, botName);
    }
    
    private void addDumbBot(Session peer, int roomNr){
        String botName = "BOT_dumb_"+mapBots.get(roomNr);
        BBaseBot bot = new BDumbBot(botName, botName, mapNumber, null);
        bot.setPosX(0);
        bot.setPosY(0);
        bot.setWidth(World.wallDim);
        bot.setHeight(World.wallDim);
        
        chars.put(botName, bot);
        chars2.get(roomNr).add(bot);
        setCharPosition(roomNr, bot);
        new Thread(bot).start();

        bot.setReady(true);

        System.out.println("dumb_bot added...");
        sendStatusMessage(peer, "bot added - "+botName);
        charMapByName.put(botName, botName);
    }
    
    /**
     * Public method giving admin privileges to the player
     * 
     * @param peer - The connected peer
     */
    public void makePlayerAdmin(Session peer){
        BCharacter crtPlayer = chars.get(peer.getId());
        if (crtPlayer == null) return;
        crtPlayer.setIsAdmin(true);
        sendAdminModMessage(peer);
    }
    
    public boolean isAdmin(Session peer){
        BCharacter crtPlayer = chars.get(peer.getId());
        if (crtPlayer == null) return false;
        return crtPlayer.getIsAdmin();
    }
    
    /**
     * Public synchronized method used to manage the server errors. Only
     * mentioned here...
     *
     * @param t - the Throwable exception
     */
    @OnError
    public synchronized void onError(Throwable t) {
    }

    /**
     * Public synchronized method used add send the whole environment to the
     * current player
     *
     * @param peer - The connected peer
     */
    public synchronized void exportEnvironment(Session peer) {
        int roomNr = getRoom(peer);
        try {
            exportChars(peer);
            exportMap(peer);
            exportBombs(peer);
            exportExplosions(peer);
            exportItems(peer);
        } catch (IllegalStateException | ConcurrentModificationException ex) {
            BLogger.getInstance().logException2(ex);
        }
    }

    /**
     * Public synchronized method used stop a working thread - used for
     * monitoring informations to be sent to a player
     *
     * @param threadId - The connected peer id
     */
    public synchronized void stopThread(String threadId) {
        workingThreads.remove(threadId);
    }

    /**
     * Public synchronized method used stop a working thread - used for
     * monitoring informations to be sent to a player
     *
     * @param crtChar - The current connected player
     * @param peer - The connected peer
     * @return Returns true if the current player if blocked (ie. it cannot move
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

    /**
     * Public method using a separate thread for watching the bombs (useful to
     * check if a bomb is about to detonate).
     */
    public void watchBombs() {
        new Thread(new Runnable() {

            @Override
            public void run() {
                while (true) {
                    try {
                        for (int roomNr = 1; roomNr <= mapNumber; roomNr++) {
                            if (bombs.get(roomNr) != null) {
                                ArrayList<BBomb> bombs2 = new ArrayList<BBomb>((ArrayList<BBomb>) bombs.get(roomNr));
                                for (BBomb bomb : bombs2) {
                                    if (bomb == null) {
                                        continue;
                                    }
                                    if (bomb.isVolatileB() && (new Date().getTime() - bomb.getCreationTime().getTime()) / 1000 >= bomb.getLifeTime() && !alreadyMarked(bomb)) {
                                        markForRemove(bomb);
                                    }
                                }
                            }
                        }
                        Thread.sleep(10);
                    } catch (InterruptedException ex) {
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

    /**
     * Public method using a separate thread for watching the connected players
     * (useful to check if we need to send messages to each player).
     */
    public void watchPeers() {
        final BombermanWSEndpoint environment = this;
        new Thread(new Runnable() {

            @Override
            public synchronized void run() {
                while (true) {
                    try {
                        int max = mapNumber + 1;
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
                            if (peer == null) continue;

                            if (peer.isOpen() && workingThreads.contains(peer.getId())) {
                                if (peer.getUserProperties().get("room") == null || peer.getUserProperties().get("room").equals(-1)) {
                                    peer.getUserProperties().put("room", 1);
                                }
                                BCharacter crtChar = chars.get(peer.getId());
                                if (crtChar == null) continue;

                                int roomNr = getRoom(peer);
                                
                                if (roomNr == -1) continue;
                                
                                if (isTrapped(crtChar, peer)) {
                                    crtChar.setState("Trapped"); // will be automated reverted when a bomb kills him >:)
                                    //charsChanged.put(roomNr, true);
                                }
                                try {
                                    // export chars?
                                    //if (charsChanged.get(roomNr)) {

                                    //peer.getBasicRemote().sendText("chars:[" + environment.exportChars(peer));
                                    environment.exportChars(peer);
                                    //    charChanged[roomNr] = true;
                                    //}

                                    // export map?
                                    if (mapChanged.containsKey(roomNr) && mapChanged.get(roomNr)) {
                                        environment.exportMap(peer);
                                        map2Changed[roomNr] = true;
                                    }

                                    // export walls?
                                    if (wallsChanged.containsKey(roomNr) && wallsChanged.get(roomNr)) {
                                        //peer.getBasicRemote().sendText("blownWalls:[" + environment.exportWalls(peer));
                                        exportWalls(peer);
                                        wallChanged[roomNr] = true;
                                    }

                                    // export bombs?
                                    if (bombsChanged.containsKey(roomNr) && bombsChanged.get(roomNr)) {
                                        //peer.getBasicRemote().sendText("bombs:[" + environment.exportBombs(peer));
                                        environment.exportBombs(peer);
                                        bombChanged[roomNr] = true;
                                    }

                                    // export explosions?
                                    if (explosionsChanged.containsKey(roomNr) && explosionsChanged.get(roomNr)) {
                                        //peer.getBasicRemote().sendText("explosions:[" + environment.exportExplosions(peer));
                                        exportExplosions(peer);
                                        explosionChanged[roomNr] = true;
                                    }

                                    // eport items?
                                    if (itemsChanged.containsKey(roomNr) && itemsChanged.get(roomNr)) {
                                        //peer.getBasicRemote().sendText("items:[" + environment.exportItems(peer));
                                        environment.exportItems(peer);
                                        itemChanged[roomNr] = true;
                                    }
                                    /*} catch (IOException ex) {
                                     BLogger.getInstance().logException2(ex);*/
                                } catch (ConcurrentModificationException | IllegalStateException ex) {
                                    BLogger.getInstance().logException2(ex);
                                } catch (RuntimeException ex) {
                                    BLogger.getInstance().logException2(ex);
                                }
                            }
                        }
                        for (int i = 1; i <= mapNumber; i++) {
                            //if (charChanged[i]){
                            //charsChanged.put(i, false);
                            //}
                            try {
                                if (map2Changed[i]) {
                                    mapChanged.put(i, false);
                                }
                                if (bombChanged[i]) {
                                    bombsChanged.put(i, false);
                                }
                                if (explosionChanged[i]) {
                                    explosionsChanged.put(i, false);
                                }
                                if (itemChanged[i]) {
                                    itemsChanged.put(i, false);
                                }
                                if (wallChanged[i]) {
                                    wallsChanged.put(i, false);
                                    blownWalls.get(i).clear();
                                }
                            } catch (ArrayIndexOutOfBoundsException e) {
                                BLogger.getInstance().logException2(e);
                            }
                        }
                        Thread.sleep(10); // limiteaza la 100FPS comunicarea cu clientul
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
     * @param myChar - The current connected player
     * @param peer - The connected peer
     */
    protected synchronized void detonateBomb(BCharacter myChar, Session peer) {
        //int roomNr = getRoom(peer);
        int roomNr = myChar.roomIndex;
        try {
            for (BBomb bomb : bombs.get(roomNr)) {
                if (bomb.getCharId().equals(myChar.getName())) {
                    bomb.setVolatileB(true);
                    markForRemove(bomb);
                    break;
                }
            }
        } catch (ConcurrentModificationException ex) {
            BLogger.getInstance().logException2(ex);
        }
        bombsChanged.put(roomNr, true);
        explosionsChanged.put(roomNr, true);
    }

    /**
     * Public static synchronized method used to check if a wall exists in a
     * given position
     *
     * @param data - The block matrix
     * @param i - The x coordinate of the checked position
     * @param j - The y coordinate of the checked position
     * @return True if there is a wall at the given position
     */
    public static synchronized boolean wallExists(AbstractBlock[][] data, int i, int j) {
        if (i < 0 || j < 0) {
            return false;
        }
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

    /**
     * Public static synchronized method used to check if a bomb exists in a
     * given position
     *
     * @param data - The block matrix
     * @param i - The x coordinate of the checked position
     * @param j - The y coordinate of the checked position
     * @return True if there is a bomb at the given position
     */
    public static synchronized boolean bombExists(AbstractBlock[][] data, int i, int j) {
        if (i < 0 || j < 0) {
            return false;
        }
        if (data == null) {
            return false;
        }
        try {
            AbstractBlock x = data[i][j];
            if (data[i][j] == null) {
                return false;
            }
            return BBomb.class.isAssignableFrom(data[i][j].getClass());
        } catch (ArrayIndexOutOfBoundsException e) {
            BLogger.getInstance().logException2(e);
            return false;
        }

    }

    /**
     * Public static synchronized method used to check if an item exists in a
     * given position
     *
     * @param data - The block matrix
     * @param i - The x coordinate of the checked position
     * @param j - The y coordinate of the checked position
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
     * @param peer - The connected peer
     * @param i - The x coordinate of the checked position
     * @param j - The y coordinate of the checked position
     * @return True if there is a character at the given position
     */
    public static synchronized boolean characterExists(int roomNr, int i, int j) {
        if (i < 0 || j < 0) {
            return false;
        }

        World world = map.get(roomNr);
        if (world == null) {
            return false;
        }
        return !world.chars[i][j].isEmpty();
    }

    /**
     * Protected synchronized method used to explode a given player (if it has
     * in the way of an explosion)
     *
     * @param peer - The connected peer
     * @param x - The x coordinate of the checked position
     * @param y - The y coordinate of the checked position
     */
//    protected synchronized void triggerBlewCharacter(final Session peer, final int x, final int y) {
    protected synchronized void triggerBlewCharacter(final BCharacter winner, final int x, final int y) {
        System.out.println("hit...");
        new Thread(new Runnable() {
            @Override
            public synchronized void run() {
                //BCharacter winner = chars.get(peer.getId());
                
                if (winner == null) {
                    return;
                }
                //int roomNr = getRoom(peer);
                int roomNr = winner.roomIndex;
                Iterator it = map.get(roomNr).chars[x][y].entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry pairs = (Map.Entry) it.next();
                    BCharacter looser = (BCharacter) pairs.getValue();
                    if (looser.getReady()) { // change game stats only if the character within a bomb range is ready to play
                        looser.incDeaths();
                        winner.incKills();
                        winner.setState("Win");
                        if (looser.equals(winner)) {
                            looser.decKills(); // first, revert the initial kill
                            looser.decKills(); // second, "steal" one of the user kills (suicide is a crime)
                        }
//                        revertState(peer, looser);
                        revertState(looser);
                        // clear the block containing the character
                        map.get(roomNr).chars[x][y].remove(looser);
                        map.get(roomNr).blockMatrix[x][y] = null;
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

    /**
     * Protected synchronized method used to revert the state of a given player
     * to "Normal"
     *
     * @param peer - The connected peer
     * @param myChar - The current connected player
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

    /**
     * Public synchronized method used to detonate the bomb of a given player
     *
     * @param peer - The connected peer
     * @param bomb - The detonated bomb
     */
    public synchronized void markForRemove(final BBomb bomb) {

        if (bomb == null) {
            return;
        }

        new Thread(new Runnable() {
            @Override
            public synchronized void run() {
                final BCharacter crtChar = bomb.getOwner();
                
                try {
                    //final int roomNr = getRoom(peer);
                    final int roomNr = crtChar.roomIndex;
                    if (roomNr == -1) return;
                    playSoundAll(roomNr, "sounds/explosion.wav");
                    //System.out.println("BombermanWSEndpoint.markForRemove : "+crtChar.getId()+", pot pune "+crtChar.getMaxBombs()+" bombe, am pus "+crtChar.getPlantedBombs());
                    crtChar.decPlantedBombs();
                    
                    //if (crtChar.getId().substring(0, 3).equals("BOT")){
                        //System.out.println("BombermanWSEndpoint.markForRemove (2) : "+crtChar.getId()+", pot pune "+crtChar.getMaxBombs()+" bombe, am pus "+crtChar.getPlantedBombs());
                    //}
                    
//                    final Explosion exp = new Explosion(bomb.getOwner());
                    final Explosion exp = new Explosion(bomb.getOwnerOrig());
                    Set<String> objectHits = Collections.synchronizedSet(new HashSet<String>());
                    map.get(roomNr).blockMatrix[bomb.getPosX() / World.wallDim][bomb.getPosY() / World.wallDim] = null;
                    int charRange = bomb.getOwner().getBombRange();

                    markedBombs.get(roomNr).add(bomb);

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

                    int posX = bomb.getPosX();
                    int posY = bomb.getPosY();
                    int width = bomb.getWidth();
                    int height = bomb.getHeight();
                    int wWidth = World.getWidth();
                    int wHeight = World.getHeight();
                    int blockX = posX / World.wallDim;
                    int blockY = posY / World.wallDim;

                    /**
                     * check to see if the explosion hits anything within it's
                     * range
                     */
                    // in it's  current position                    
                    if (posX + width <= wWidth && BombermanWSEndpoint.characterExists(crtChar.roomIndex, (posX / World.wallDim), posY / World.wallDim)) {
                        bomb.getOwner().decPlantedBombs();
                        //triggerBlewCharacter(peer, (posX / World.wallDim), posY / World.wallDim);
                        triggerBlewCharacter(crtChar, (posX / World.wallDim), posY / World.wallDim);
                    }
                    // in it's external range
                    for (int i = 1; i <= charRange; i++) {
                        // right
                        final int xR = blockX + i;
                        final int yR = blockY;
                        String checkedRight = BombermanWSEndpoint.checkWorldMatrix(roomNr, xR, yR);
                        boolean hitRight = objectHits.contains("right");
                        boolean crtPosRight = (posX + width * (i + 1) <= wWidth);
                        if (!hitRight && crtPosRight) {
                            if (checkedRight.equals("bomb")) {
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        markForRemove((BBomb) map.get(roomNr).blockMatrix[xR][yR]);
                                    }
                                }).start();
                                objectHits.add("right");
                                //System.out.println("hit bomb right");
                            } else if (checkedRight.equals("char")) {
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        //triggerBlewCharacter(peer, xR, yR);
                                        triggerBlewCharacter(crtChar, xR, yR);
                                    }
                                }).start();
                                //objectHits.add("right");
                                exp.ranges.put("right", exp.ranges.get("right") + 1);
                                //System.out.println("hit character right");
                            } else if (checkedRight.equals("wall")) {
                                exp.directions.add("right");
                                //System.out.println("hit wall right");
                                AbstractWall wall = ((AbstractWall) map.get(roomNr).blockMatrix[xR][yR]);
                                if (wall.isBlowable()) {
                                    map.get(roomNr).walls.remove(map.get(roomNr).blockMatrix[xR][yR]);
                                    exp.ranges.put("right", exp.ranges.get("right") + 1);
                                    flipForItems(crtChar.roomIndex, xR, yR);
                                    //mapChanged.put(roomNr, true);
                                    blownWalls.get(roomNr).add(wall.wallId);
                                    wallsChanged.put(roomNr, true);
                                }
                                objectHits.add("right");
                            } else if (checkedRight.equals("item")) {
                                exp.directions.add("right");
                                items.get(roomNr).remove((AbstractItem) map.get(roomNr).blockMatrix[xR][yR]);
                                map.get(roomNr).blockMatrix[xR][yR] = null;
                                exp.ranges.put("right", exp.ranges.get("right") + 1);
                                objectHits.add("right");
                                itemsChanged.put(roomNr, true);
                            } else if (!checkedRight.equals("wall")) {
                                exp.directions.add("right");
                                exp.ranges.put("right", exp.ranges.get("right") + 1);
                                //System.out.println("empty right");
                            }
                        }

                        // left
                        final int xL = blockX - i;
                        final int yL = blockY;
                        String checkedLeft = BombermanWSEndpoint.checkWorldMatrix(roomNr, xL, yL);
                        boolean hitLeft = objectHits.contains("left");
                        boolean crtPosLeft = (posX - width * i >= 0);
                        if (!hitLeft && crtPosLeft) {
                            if (checkedLeft.equals("bomb")) {
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        markForRemove((BBomb) map.get(roomNr).blockMatrix[xL][yL]);
                                    }
                                }).start();
                                objectHits.add("left");
                                //System.out.println("hit bomb left");
                            } else if (checkedLeft.equals("char")) {
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        //triggerBlewCharacter(peer, xL, yL);
                                        triggerBlewCharacter(crtChar, xL, yL);
                                    }
                                }).start();
                                //objectHits.add("left");
                                exp.ranges.put("left", exp.ranges.get("left") + 1);
                                //System.out.println("hit character left");
                            } else if (checkedLeft.equals("wall")) {
                                exp.directions.add("left");
                                //System.out.println("hit wall left");
                                AbstractWall wall = ((AbstractWall) map.get(roomNr).blockMatrix[xL][yL]);
                                if (wall.isBlowable()) {
                                    map.get(roomNr).walls.remove(map.get(roomNr).blockMatrix[xL][yL]);
                                    exp.ranges.put("left", exp.ranges.get("left") + 1);
                                    flipForItems(crtChar.roomIndex, xL, yL);
                                    //mapChanged.put(roomNr, true);
                                    blownWalls.get(roomNr).add(wall.wallId);
                                    wallsChanged.put(roomNr, true);
                                }
                                objectHits.add("left");
                            } else if (checkedLeft.equals("item")) {
                                exp.directions.add("left");
                                items.get(roomNr).remove((AbstractItem) map.get(roomNr).blockMatrix[xL][yL]);
                                map.get(roomNr).blockMatrix[xL][yL] = null;
                                exp.ranges.put("left", exp.ranges.get("left") + 1);
                                objectHits.add("left");
                                itemsChanged.put(roomNr, true);
                            } else if (!checkedLeft.equals("wall")) {
                                exp.directions.add("left");
                                exp.ranges.put("left", exp.ranges.get("left") + 1);
                                //System.out.println("empty left");
                            }
                        }

                        // down
                        final int xD = blockX;
                        final int yD = blockY + i;
                        String checkedDown = BombermanWSEndpoint.checkWorldMatrix(roomNr, xD, yD);
                        boolean hitDown = objectHits.contains("down");
                        boolean crtPosDown = (posY + height * (i + 1) <= wHeight);
                        if (!hitDown && crtPosDown) {
                            if (checkedDown.equals("bomb")) {
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        markForRemove((BBomb) map.get(roomNr).blockMatrix[xD][yD]);
                                    }
                                }).start();
                                objectHits.add("down");
                                //System.out.println("hit bomb down");
                            } else if (checkedDown.equals("char")) {
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        //triggerBlewCharacter(peer, xD, yD);
                                        triggerBlewCharacter(crtChar, xD, yD);
                                    }
                                }).start();
                                //objectHits.add("down");
                                exp.ranges.put("down", exp.ranges.get("down") + 1);
                                //System.out.println("hit character down");
                            } else if (checkedDown.equals("wall")) {
                                exp.directions.add("down");
                                //System.out.println("hit wall down");
                                AbstractWall wall = ((AbstractWall) map.get(roomNr).blockMatrix[xD][yD]);
                                if (wall.isBlowable()) {
                                    map.get(roomNr).walls.remove(map.get(roomNr).blockMatrix[xD][yD]);
                                    exp.ranges.put("down", exp.ranges.get("down") + 1);
                                    flipForItems(crtChar.roomIndex, xD, yD);
                                    //mapChanged.put(roomNr, true);
                                    blownWalls.get(roomNr).add(wall.wallId);
                                    wallsChanged.put(roomNr, true);
                                }
                                objectHits.add("down");
                            } else if (checkedDown.equals("item")) {
                                exp.directions.add("down");
                                items.get(roomNr).remove((AbstractItem) map.get(roomNr).blockMatrix[xD][yD]);
                                map.get(roomNr).blockMatrix[xD][yD] = null;
                                exp.ranges.put("down", exp.ranges.get("down") + 1);
                                objectHits.add("down");
                                itemsChanged.put(roomNr, true);
                            } else if (!checkedDown.equals("wall")) {
                                exp.directions.add("down");
                                exp.ranges.put("down", exp.ranges.get("down") + 1);
                                //System.out.println("empty down");
                            }
                        }

                        // up
                        final int xU = blockX;
                        final int yU = blockY - i;
                        String checkedUp = BombermanWSEndpoint.checkWorldMatrix(roomNr, xU, yU);
                        boolean hitUp = objectHits.contains("up");
                        boolean crtPosUp = (posY - height * i >= 0);
                        if (!hitUp && crtPosUp) {
                            if (checkedUp.equals("bomb")) {
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        markForRemove((BBomb) map.get(roomNr).blockMatrix[xU][yU]);
                                    }
                                }).start();
                                objectHits.add("up");
                                //System.out.println("hit bomb up");
                            } else if (checkedUp.equals("char")) {
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        //triggerBlewCharacter(peer, xU, yU);
                                        triggerBlewCharacter(crtChar, xU, yU);
                                    }
                                }).start();
                                //objectHits.add("up");
                                exp.ranges.put("up", exp.ranges.get("up") + 1);
                                //System.out.println("hit character up");
                            } else if (checkedUp.equals("wall")) {
                                exp.directions.add("up");
                                //System.out.println("hit wall up");
                                AbstractWall wall = ((AbstractWall) map.get(roomNr).blockMatrix[xU][yU]);
                                if (wall.isBlowable()) {
                                    map.get(roomNr).walls.remove(map.get(roomNr).blockMatrix[xU][yU]);
                                    exp.ranges.put("up", exp.ranges.get("up") + 1);
                                    flipForItems(crtChar.roomIndex, xU, yU);
                                    //mapChanged.put(roomNr, true);
                                    blownWalls.get(roomNr).add(wall.wallId);
                                    wallsChanged.put(roomNr, true);
                                }
                                objectHits.add("up");
                            } else if (checkedUp.equals("item")) {
                                exp.directions.add("up");
                                items.get(roomNr).remove((AbstractItem) map.get(roomNr).blockMatrix[xU][yU]);
                                map.get(roomNr).blockMatrix[xU][yU] = null;
                                exp.ranges.put("up", exp.ranges.get("up") + 1);
                                objectHits.add("up");
                                itemsChanged.put(roomNr, true);
                            } else if (!checkedUp.equals("wall")) {
                                exp.directions.add("up");
                                exp.ranges.put("up", exp.ranges.get("up") + 1);
                                //System.out.println("empty up");
                            }
                        }
                    }

                } catch (Exception ex) {
                    BLogger.getInstance().logException2(ex);
                }
            }
        }).start();
    }

    /**
     * Protected synchronized method used to check if there is an item behind a
     * given blown wall
     *
     * @param peer - The connected peer
     * @param x - The x coordinate of the checked position
     * @param y - The y coordinate of the checked position
     */
    protected synchronized void flipForItems(int roomNr, int x, int y) {
        Random r = new Random();
        int rand = r.nextInt(1000000);
        if (rand % 5 == 0) { // 20% chance to find a hidden item behind the wall ;))
            if (wallExists(map.get(roomNr).blockMatrix, x, y)) {
                AbstractWall wall = ((AbstractWall) map.get(roomNr).blockMatrix[x][y]);
                //blownWalls.get(roomNr).add(wall.wallId);

                AbstractItem item = ItemGenerator.getInstance().generateRandomItem();
                item.setPosX(map.get(roomNr).blockMatrix[x][y].getPosX());
                item.setPosY(map.get(roomNr).blockMatrix[x][y].getPosY());
                map.get(roomNr).blockMatrix[x][y] = null;
                map.get(roomNr).blockMatrix[x][y] = item;
                items.get(roomNr).add(item);
            } else {
                map.get(roomNr).blockMatrix[x][y] = null;
            }
        } else {
            map.get(roomNr).blockMatrix[x][y] = null; // remove the block
        }
        //mapChanged.put(roomNr, true);
        itemsChanged.put(roomNr, true);
//        wallsChanged.put(roomNr, true);
    }

    /**
     * Protected synchronized method used to check if a given bomb isn't already
     * marker for removal
     *
     * @param peer - The connected peer
     * @param bomb - The checked bomb
     * @return True if the given bomb is already marked
     */
    public synchronized boolean alreadyMarked(BBomb bomb) {
        return markedBombs.get(bomb.getOwner().roomIndex).contains(bomb);
    }

    /**
     * Protected synchronized method used to send the characters to a given
     * player
     *
     * @param peer - The connected peer
     */
    protected synchronized void exportChars(final Session peer) {
        String ret = "";
        ret += peer.getId() + "[#chars#]";
        int roomNr = getRoom(peer);
        if (chars2.get(roomNr) == null){
            roomNr--;
        }
        if (chars2.get(roomNr) == null) return;
        Set<BCharacter> myChars = Collections.synchronizedSet(new HashSet<BCharacter>(chars2.get(roomNr)));
        for (BCharacter crtChar : myChars) {
            ret += crtChar.toString() + "[#charSep#]";
        }
        sendClearMessage("chars:[" + ret, peer);
    }

    /**
     * Protected synchronized method used to send the map to a given player
     *
     * @param peer - The connected peer
     */
    protected synchronized void exportMap(final Session peer) {
        String ret = "";
        int roomNr = getRoom(peer);
        ret = map.get(roomNr).toString();
        sendClearMessage("map:[" + ret, peer);
    }

    /**
     * Protected synchronized method used to send the walls to a given player
     *
     * @param peer - The connected peer
     */
    protected synchronized void exportWalls(final Session peer) {
        String ret = "";
        int roomNr = getRoom(peer);
        //BLogger.getInstance().log(BLogger.LEVEL_FINE, "exporting bombs...");
        if (blownWalls.get(roomNr) != null) {
            Set<String> walls2 = Collections.synchronizedSet(new HashSet<String>(blownWalls.get(roomNr)));
            for (String wall : walls2) {
                ret += wall + "[#brickSep#]";
            }
        }
        sendClearMessage("blownWalls:[" + ret, peer);
    }

    /**
     * Protected synchronized method used to send the bombs to a given player
     *
     * @param peer - The connected peer
     */
    protected synchronized void exportBombs(final Session peer) {
        String ret = "";
        int roomNr = getRoom(peer);
        //BLogger.getInstance().log(BLogger.LEVEL_FINE, "exporting bombs...");
        if (bombs.get(roomNr) != null) {
            Set<BBomb> bombs2 = Collections.synchronizedSet(new HashSet<BBomb>(bombs.get(roomNr)));
            for (BBomb bomb : bombs2) {
                ret += bomb.toString() + "[#bombSep#]";
            }
        }
        sendClearMessage("bombs:[" + ret, peer);
    }

    /**
     * Protected synchronized method used to send the explosions to a given
     * player
     *
     * @param peer - The connected peer
     */
    protected synchronized void exportExplosions(final Session peer) {
        String ret = "";
        int roomNr = getRoom(peer);
        if (explosions.get(roomNr) != null) {
            Set<Explosion> explosions2 = Collections.synchronizedSet(new HashSet<Explosion>(explosions.get(roomNr)));
            for (Explosion exp : explosions2) {
                ret += exp.toString() + "[#explosionSep#]";
            }
        }
        sendClearMessage("explosions:[" + ret, peer);
    }

    /**
     * Protected synchronized method used to send the items to a given player
     *
     * @param peer - The connected peer
     */
    protected synchronized void exportItems(final Session peer) {
        String ret = "";
        int roomNr = getRoom(peer);
        if (items.get(roomNr) != null) {
            Set<AbstractItem> items2 = Collections.synchronizedSet(new HashSet<AbstractItem>(items.get(roomNr)));
            for (AbstractItem item : items2) {
                ret += item.toString() + "[#itemSep#]";
            }
        }
        sendClearMessage("items:[" + ret, peer);
    }

    /**
     * Public method used to check if a given player can plant a new bomb
     *
     * @param peer - The connected peer
     * @param crtChar - The current connected player
     * @return True if the player can plant a new bomb
     */
    public boolean canPlantNewBomb(BCharacter crtChar) {
        return crtChar.getPlantedBombs() < crtChar.getMaxBombs();
    }

    /**
     * Public static synhronized method used to check the type of a given block
     * from the world
     *
     * @param roomNr - The room to be checked
     * @param i - The x coordinate of the checked position
     * @param j - The y coordinate of the checked position
     * @return Strings expressing the type of the block existing at the given
     * position
     */
    public static synchronized String checkWorldMatrix(int roomNr, int i, int j) {
        HashMap<String, BCharacter>[][] chars = map.get(roomNr).chars;
        AbstractBlock[][] data = map.get(roomNr).blockMatrix;
        if (i < 0 || j < 0) {
            return "empty";
        }
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

    /**
     * Public method used to send make a given peer play a given sound
     *
     * @param sound - The sound to be played
     * @param peer - The connected peer
     */
    public void playSound(String sound, Session peer) {
        if (peer == null) return;
        sendClearMessage("sound:[" + sound, peer);
    }

    /**
     * Public method used to make all players from a given map play a given
     * sound
     *
     * @param roomNr - The room number
     * @param sound - The sound to be played
     */
    public void playSoundAll(int roomNr, String sound) {
        Set<BCharacter> myChars = Collections.synchronizedSet(new HashSet<BCharacter>(chars2.get(roomNr)));
        for (BCharacter crtChar : myChars) {
            if (peers.get(crtChar.getId()) == null) continue;
            playSound(sound, peers.get(crtChar.getId()));
        }
    }

    /**
     * Private static method returning the map number of a given player
     *
     * @param peer - The connected peer
     * @return - The room of the connected player
     */
    private static int getRoom(Session peer) {
        if (peer == null) return -1;
        try{
            int roomNr = Integer.parseInt(peer.getUserProperties().get("room").toString());
            if (roomNr <= 0){
                return -1;
            }
            if (!peerRooms.containsKey(peer.getId())) {
                peerRooms.put(peer.getId(), roomNr);
            }
            return peerRooms.get(peer.getId());
        }
        catch (NumberFormatException ex){
            return -1;
        }
    }

    /**
     * Public synchronized method updating a player state, so that it can
     * actually actually
     *
     * @param peer - The connected peer
     */
    public synchronized void makePlayerReady(Session peer) {
        BCharacter crtChar = chars.get(peer.getId());
        crtChar.setReady(true);
    }

    /**
     * Public synchronized method used to place a player randomly on the map
     *
     * @param mapNumber - The connected peer room
     * @param newChar - The player associated to the connected peer
     */
    public synchronized void setCharPosition(final int mapNumber, final BCharacter newChar) {
        new Thread(new Runnable() {

            @Override
            public void run() {
                newChar.setWalking(false);
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
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                    Logger.getLogger(BombermanWSEndpoint.class.getName()).log(Level.SEVERE, null, ex);
                }
                // character's previous position
                int X1 = newChar.getPosX() / World.wallDim;
                int Y1 = newChar.getPosY() / World.wallDim;
                newChar.setPosX(X * World.wallDim);
                newChar.setPosY(Y * World.wallDim);
                map.get(mapNumber).chars[X][Y].put(newChar.getId(), newChar);
                // clear character's previuos position
                map.get(mapNumber).chars[X1][Y1].remove(newChar.getId());
                map.get(mapNumber).blockMatrix[X1][Y1] = null;
            }

        }).start();
    }

    /**
     * Public method used to tell the player that he/she entered an invalid
     * email address
     *
     * @param peer - The connected peer
     */
    public void sendInvalidEmailMessage(Session peer) {
        sendClearMessage("invalidAddress:[{}", peer);
    }

    /**
     * Public method used to tell the player that the email/username selected is
     * already registered
     *
     * @param peer - The connected peer
     */
    public void sendAlreadyRegisteredMessage(Session peer) {
        sendClearMessage("alreadyTaken:[{}", peer);
    }

    /**
     * Public method used to tell the player that he/she is ready to play
     *
     * @param peer - The connected peer
     */
    public void sendReadyMessage(Session peer) {
        sendClearMessage("ready:[{}", peer);
    }

    /**
     * Public method used to tell the player that he/she is registered as admin in the app
     *
     * @param peer - The connected peer
     */
    public void sendAdminModMessage(Session peer){
        sendClearMessage("admin:[{}", peer);
    }
    
    /**
     * Public method used to tell the player that he/she doesn't have admin privileges
     *
     * @param peer - The connected peer
     */
    public void sendNotAdminMessage(Session peer){
        sendClearMessage("notadmin:[{}", peer);
    }
    
    public void sendStatusMessage(Session peer, String msg){
        sendClearMessage("status:["+msg, peer);
    }
    
    /**
     * Public method used to tell the player that he's/she's successfully
     * registered
     *
     * @param peer - The connected peer
     */
    public void sendRegistrationSuccessMessage(Session peer) {
        sendClearMessage("registerSuccess:[{}", peer);
    }

    /**
     * Public method used to tell the player that he/she could not be registered
     *
     * @param peer - The connected peer
     */
    public void sendRegisterFailedMessage(Session peer) {
        sendClearMessage("registerFailed:[{}", peer);
    }

    /**
     * Public method used to tell the player that he/she could not log in
     *
     * @param peer - The connected peer
     */
    public void sendLoginFailedMessage(Session peer) {
        sendClearMessage("loginFailed:[{}", peer);
    }

    /**
     * Public method used to tell the player that he/she must be logged in in
     * order to play
     *
     * @param peer - The connected peer
     */
    public void sendLoginFirstMessage(Session peer) {
        sendClearMessage("loginFirst:[{}", peer);
    }

    /**
     * Public method used to send messages to the connected peer
     *
     * @param msg -The message to be sent
     * @param peer - The connected peer
     */
    public void sendClearMessage(String msg, Session peer) {
        if (peer == null) return;
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
     * @param msg -The message to be sent
     * @param peer - The connected peer
     */
    public void sendMessage(String msg, Session peer) {
        sendClearMessage("msg:[" + msg, peer);
    }

    /**
     * Public method used to send messages to all players connected to a room
     *
     * @param roomNr - the room number
     * @param msg -The message to be sent
     */
    public void sendMessageAll(int roomNr, String msg) {
        Set<BCharacter> myChars = Collections.synchronizedSet(new HashSet<BCharacter>(chars2.get(roomNr)));
        for (BCharacter crtChar : myChars) {
            sendMessage(msg, peers.get(crtChar.getId()));
        }
    }

    /**
     * Private method used to store all messages sent from the players
     *
     * @param myChar - the player that sent the message
     * @param msg -The message sent by the player
     */
    private void logChatMessage(BCharacter myChar, String msg) {
        try {
            String query = "INSERT INTO `chat_message` (id, user_id, message_time, message)"
                    + "VALUES (NULL, ?, NOW(), ?)";
            PreparedStatement st = (PreparedStatement) BombermanWSEndpoint.con.prepareStatement(query);
            st.setInt(1, myChar.getUserId());
            st.setString(2, msg);
            int affectedRows = st.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Cannot log chat message : `" + msg + "` from user `" + myChar.getName() + " ( " + myChar.getId() + " )" + "`");
            }
        } catch (SQLException ex) {
            BLogger.getInstance().logException2(ex);
        }
    }

    public boolean bombReaches(BBomb bomb, int minReach){
        return bomb.getOwner().getBombRange() >= minReach;
    }
    
}
