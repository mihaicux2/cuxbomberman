package com.cux.bomberman.world;

import com.cux.bomberman.util.BLogger;
import java.io.IOException;
import java.util.Date;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;

/**
 * This class is used to represent the bombs planted by any of the current
 * game characters (bots & players)
 * 
 * @version 1.0
 * @author  Mihail Cuculici (mihai.cuculici@gmail.com)
 * @author  http://www.
 */
public class BBomb extends AbstractBlock {

    //protected String charId = "";
    
    /**
     * The character that planted the bomb (in it's current state)
     */
    protected BCharacter owner;
    
    /**
     * Copy of the character that planted the bomb (in it's original state)
     */
    protected BCharacter ownerOrig;
    
    /**
     * The datetime for the creation of the bomb
     */
    protected Date creationTime = null;
    
    /**
     * The default bomb lifetime
     */
    protected Double lifeTime = 1.5;
    
    /**
     * Boolean used to tell if a bomb blows after a given amount of time (it's usual lifetime)
     */
    protected boolean volatileB = true;
    
    /**
     * Public constructor used to setup a bomb instance
     * @param owner The character that planted the bomb
     */
    public BBomb(BCharacter owner){
//        this.owner = owner.clone();        
        this.owner = owner;        
        this.ownerOrig = owner.clone();
        this.posX = (owner.getPosX()/World.wallDim)*World.wallDim;
        this.posY = (owner.getPosY()/World.wallDim)*World.wallDim;
        this.creationTime = new Date();
        if (this.owner.isTriggered()){
            this.volatileB = false;
        }
    }
    
    /**
     * Public getter for the owner property
     * @return The requested property
     */
    public BCharacter getOwner(){
        return this.owner;
    }
    
    /**
     * Public getter for the ownerOrig property
     * @return The requested property
     */
    public BCharacter getOwnerOrig(){
        return this.ownerOrig;
    }
    
    /**
     * Public getter for the name of the owner
     * @return The requested property
     */
    public String getCharId() {
        return this.owner.getName();
    }

    /**
     * Public getter for the creationTime property
     * @return The requested property
     */
    public Date getCreationTime() {
        return creationTime;
    }

    /**
     * Public getter for the lifeTime property
     * @return The requested property
     */
    public double getLifeTime() {
        return lifeTime;
    }

    /**
     * Public setter for the creationTime property
     * @param creationTime The new value
     */
    public void setCreationTime(Date creationTime) {
        this.creationTime = creationTime;
    }
    
    /**
     * Public setter for the lifeTime property
     * @param lifeTime The new value
     */
    public void setLifeTime(double lifeTime) {
        this.lifeTime = lifeTime;
    }

    /**
     * Public method used to tell if a bomb should blow automatically after a given amount of time (it's lifetime)
     * @return The required property
     */
    public boolean isVolatileB() {
        return volatileB;
    }

    /**
     * Public setter for the volatileB property
     * @param volatileB The new value
     */
    public void setVolatileB(boolean volatileB) {
        this.volatileB = volatileB;
    }   
    
    /**
     * Public method used to export the bomb for the client
     * @return The JSON representation of the bomb
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
