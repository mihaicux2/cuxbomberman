/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.cux.bomberman.world.walls;

import com.cux.bomberman.util.BLogger;
import com.cux.bomberman.world.AbstractBlock;
import java.io.IOException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;



/**
 *
 * @author mihaicux
 */
public abstract class AbstractWall extends AbstractBlock{

    /**
     * The texture of the wall
     */
    public String texture;
    
    /**
     * The name of the wall
     */
    public String name;
    
    /**
     * Tells if the wall can be blown by an explosion
     */
    public boolean blowable = false;
    
    /**
     * The id of the wall
     */
    public String wallId;
    
    /**
     * Public constructor
     * @param name The name of the wall
     */
    protected AbstractWall(String name){
        this.name = name;
    }

    /**
     * Public setter for the scale property
     * @param texture The new value
     */
    public void setTexture(String texture) {
        this.texture = texture;
    }
    
    /**
     * Make a wall blowable
     */
    public void makeBlowable(){
        this.blowable = true;
    }
    
    /**
     * Make a wall unblowable
     */
    public void makeUnblowable(){
        this.blowable = false;
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
     * Public getter for the creationTime property
     * @return The requested property
     */
    public boolean isBlowable(){
        return this.blowable;
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
