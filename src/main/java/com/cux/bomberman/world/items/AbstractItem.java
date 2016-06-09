package com.cux.bomberman.world.items;

import com.cux.bomberman.util.BLogger;
import com.cux.bomberman.world.AbstractBlock;
import java.io.IOException;
import java.util.Date;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;

/**
 * This class represents the base for all of the game's items;
 * it provides methods for defining, changing and exporting
 * all the extending items
 * 
 * @version 1.0
 * @author  Mihail Cuculici (mihai.cuculici@gmail.com)
 * @author  http://www.
 */
public abstract class AbstractItem extends AbstractBlock{
    
    /**
     * The texture of the item
     */
    public String texture;
    
    /**
     * The name of the item
     */
    public String name;
    
    /**
     * Tells if the item will disappear after a given amount of time
     */
    public boolean timed = true;
    
    /**
     * The creation time of the item
     */
    protected Date creationTime = null;
    
    /**
     * Default lifetime for a timed item
     */
    protected int lifeTime = 3;
    
    /**
     * The scale(impact) of the item after it's attachment to a given character
     */
    protected int scale = 1; // used for speed, bomb range, etc.
    
    /**
     * The id of the item
     */
    public String itemId;
    
    /**
     * Public constructor
     * @param name The name of the item
     */
    protected AbstractItem(String name){
        this.name = name;
        this.itemId = java.util.UUID.randomUUID().toString();
    }

    /**
     * Public getter for the scale property
     * @return The requested property
     */
    public int getScale() {
        return scale;
    }

    /**
     * Public setter for the scale property
     * @param scale The new value
     */
    public void setScale(int scale) {
        this.scale = scale;
    }    
    
    /**
     * Public getter for the creationTime property
     * @return The requested property
     */
    public Date getCreationTime() {
        return creationTime;
    }

    /**
     * Public setter for the creationTime property
     * @param creationTime The new value
     */
    public void setCreationTime(Date creationTime) {
        this.creationTime = creationTime;
    }

    /**
     * Public getter for the lifeTime property
     * @return The requested property
     */
    public int getLifeTime() {
        return lifeTime;
    }

    /**
     * Public setter for the lifeTime property
     * @param lifeTime The new value
     */
    public void setLifeTime(int lifeTime) {
        this.lifeTime = lifeTime;
    }
            
    /**
     * Public getter for the timed property
     * @return The requested property
     */
    public boolean isTimed() {
        return timed;
    }

    /**
     * Public setter for the timed property
     * @param timed The new value
     */
    public void setTimed(boolean timed) {
        this.timed = timed;
    }    
    
    /**
     * Public setter for the texture property
     * @param texture The new value
     */
    public void setTexture(String texture) {
        this.texture = texture;
    }

    /**
     * Public getter for the name property
     * @return The requested property
     */
    public String getName() {
        return name;
    }
    
    /**
     * Public getter for the texture property
     * @return The requested property
     */
    public String getTexture() {
        return texture;
    }
    
    /**
     * Public method used to convert the item to JSON, to be sent to a client
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
