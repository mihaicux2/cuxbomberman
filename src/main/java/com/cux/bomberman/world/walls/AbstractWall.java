/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.cux.bomberman.world.walls;

import com.cux.bomberman.util.BLogger;
import com.cux.bomberman.world.AbstractBlock;
import java.io.IOException;
//import java.util.logging.Level;
//import java.util.logging.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;



/**
 *
 * @author mihaicux
 */
public abstract class AbstractWall extends AbstractBlock{

    public String texture;
    public String name;
    public boolean blowable = false;
    public String wallId;
    
    protected AbstractWall(String name){
        this.name = name;
    }

    public void setTexture(String texture) {
        this.texture = texture;
    }
    
    public void makeBlowable(){
        this.blowable = true;
    }
    
    public void makeUnblowable(){
        this.blowable = false;
    }

    public String getName() {
        return name;
    }
    
    public String getTexture() {
        return texture;
    }
    
    public boolean isBlowable(){
        return this.blowable;
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
