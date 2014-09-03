/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.cux.bomberman.world;

import com.cux.bomberman.BombermanWSEndpoint;
import com.cux.bomberman.util.BLogger;
import java.io.IOException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.websocket.Session;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;

/**
 *
 * @author root
 * @todo check if owner has remote for bomb => will set isVolatile : false & lifeTime = -1
 */
public class BBomb extends AbstractBlock {

    //protected String charId = "";
    protected BCharacter owner, ownerOrig;
    protected Date creationTime = null;
    protected Double lifeTime = 1.5;
    protected boolean volatileB = true;
    
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
    
    public BCharacter getOwner(){
        return this.owner;
    }
    
    public BCharacter getOwnerOrig(){
        return this.ownerOrig;
    }
    
    public String getCharId() {
        return this.owner.getName();
    }

    public Date getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Date creationTime) {
        this.creationTime = creationTime;
    }

    public double getLifeTime() {
        return lifeTime;
    }

    public void setLifeTime(double lifeTime) {
        this.lifeTime = lifeTime;
    }

    public boolean isVolatileB() {
        return volatileB;
    }

    public void setVolatileB(boolean volatileB) {
        this.volatileB = volatileB;
    }   
    
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
