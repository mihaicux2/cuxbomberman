package com.cux.bomberman.world;

import com.cux.bomberman.util.BLogger;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;

/**
 *
 * @author mihaicux
 */
public class Explosion extends BBomb{
    
    /**
     * Set for the directions of the explosion
     */
    public Set<String> directions = Collections.synchronizedSet(new HashSet<String>());
    
    /**
     * Map for direction : range
     */
    public ConcurrentMap<String, Integer> ranges = new ConcurrentHashMap<String, Integer>();
    
    /**
     * Public constructor used to setup the explosion
     * @param owner The character that triggered the explosion
     */
    public Explosion(BCharacter owner) {
        super(owner);
        this.ranges.put("left", 0);
        this.ranges.put("right", 0);
        this.ranges.put("up", 0);
        this.ranges.put("down", 0);
        
    }

    /**
     * Public setter for the directions property
     * @return he requested property
     */
    public Set<String> getDirections() {
        return directions;
    }

    /**
     * Public setter for the directions property
     * @param directions The new value
     */
    public void setDirections(Set<String> directions) {
        this.directions = directions;
    }
    
    /**
     * Public method used to convert the character to JSON, to be sent to a client
     * @return The JSON representation of the character
     */
    @Override
    public String toString(){
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        try {
            return ow.writeValueAsString(this);
        } catch (IOException ex) {
            BLogger.getInstance().logException2(ex);
            return ex.getMessage();
           // return "";
        }
    }
    
}
