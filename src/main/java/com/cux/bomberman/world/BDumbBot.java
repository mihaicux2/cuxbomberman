package com.cux.bomberman.world;

import com.cux.bomberman.BombermanWSEndpoint;
import com.cux.bomberman.util.BLogger;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.websocket.EndpointConfig;

/**
 * This is the first type of the game bots; it simply works on total
 * randomness, making it's intelligence practically null
 * 
 * @version 1.0
 * @author  Mihail Cuculici (mihai.cuculici@gmail.com)
 * @author  http://www.
 */
public class BDumbBot extends BBaseBot{

    /**
     * Public constructor used only to call the BBaseBot constructor
     * @param id The bot id
     * @param name The bot name
     * @param roomIndex The room of the game
     * @param config The server endpoint configuration object
     */
    public BDumbBot(String id, String name, int roomIndex, EndpointConfig config) {
        super(id, name, roomIndex, config);
    }
    
    /**
     * Public method used for the Search&Destroy directive.<br />
     * All random :P
     */
    @Override
    public void searchAndDestroy(){
        Random r = new Random();
        // random moves
        int rand = r.nextInt(1000000);
        if (rand % 5 == 0) {
            this.move("up");
        } else if (rand % 4 == 0) {
            this.move("left");
        } else if (rand % 3 == 0) {
            this.move("down");
        } else if (rand % 2 == 0) {
            this.move("right");
        } 
        
        // a new bomb, maby? :D
        rand = r.nextInt(100);
        if (rand % 5 == 0){
            this.dropBomb();
        }
    }

    /**
     * Public method used to get the description of the BOT
     * @return The description of the BOT
     */
    @Override
    public String getDescription() {
        return "BDumbBot ";
    }
    
}
