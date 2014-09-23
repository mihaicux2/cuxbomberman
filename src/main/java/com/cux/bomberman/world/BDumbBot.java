package com.cux.bomberman.world;

import com.cux.bomberman.BombermanWSEndpoint;
import com.cux.bomberman.util.BLogger;
import java.util.Random;
import javax.websocket.EndpointConfig;

/**
 * It simply works based on total randomness
 * @author mihaicux
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
            this.moveUp();
        } else if (rand % 4 == 0) {
            this.moveLeft();
        } else if (rand % 3 == 0) {
            this.moveDown();
        } else if (rand % 2 == 0) {
            this.moveRight();
        } 
        
        // a new bomb, maby? :D
        rand = r.nextInt(100);
        if (rand % 5 == 0){
            this.dropBomb();
        }
    }
    
    /**
     * Public method used to loop the Search&Destroy directive
     */
    @Override
    public void run() {
        while (this.running) {
            try {
                if (!this.walking){
                    this.searchAndDestroy();
                }
                Thread.sleep(200); // limit dumb bot action to 5 FPS
            } catch (InterruptedException ex) {
                BLogger.getInstance().logException2(ex);
            }
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
