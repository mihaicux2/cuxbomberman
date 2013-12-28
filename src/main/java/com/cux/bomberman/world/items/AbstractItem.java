/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.cux.bomberman.world.items;

import com.cux.bomberman.util.BLogger;
import com.cux.bomberman.world.AbstractBlock;
import java.io.IOException;
import java.util.Date;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;

/**
 *
 * @author mihaicux
 */
public abstract class AbstractItem extends AbstractBlock{
    
    public String texture;
    public String name;
    public boolean timed = true;
    protected Date creationTime = null;
    protected int lifeTime = 3;
    protected int scale = 1; // used for speed, bomb range, etc.
    
    protected AbstractItem(String name){
        this.name = name;
    }

    public int getScale() {
        return scale;
    }

    public void setScale(int scale) {
        this.scale = scale;
    }

    
    
    public Date getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Date creationTime) {
        this.creationTime = creationTime;
    }

    public int getLifeTime() {
        return lifeTime;
    }

    public void setLifeTime(int lifeTime) {
        this.lifeTime = lifeTime;
    }
            
    public boolean isTimed() {
        return timed;
    }

    public void setTimed(boolean timed) {
        this.timed = timed;
    }    
    
    public void setTexture(String texture) {
        this.texture = texture;
    }

    public String getName() {
        return name;
    }
    
    public String getTexture() {
        return texture;
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
