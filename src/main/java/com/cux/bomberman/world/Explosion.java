/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.cux.bomberman.world;

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
 * @author root
 */
public class Explosion extends BBomb{
    
    public Set<String> directions = Collections.synchronizedSet(new HashSet<String>());
    public ConcurrentMap<String, Integer> ranges = new ConcurrentHashMap<String, Integer>();
    
    public Explosion(BCharacter owner) {
        super(owner);
        this.ranges.put("left", 0);
        this.ranges.put("right", 0);
        this.ranges.put("up", 0);
        this.ranges.put("down", 0);
        
    }

    public Set<String> getDirections() {
        return directions;
    }

    public void setDirections(Set<String> directions) {
        this.directions = directions;
    }
    
    @Override
    public String toString(){
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        try {
            return ow.writeValueAsString(this);
        } catch (IOException ex) {
           // Logger.getLogger(AbstractWall.class.getName()).log(Level.SEVERE, null, ex);
            return ex.getMessage();
           // return "";
        }
    }
    
}