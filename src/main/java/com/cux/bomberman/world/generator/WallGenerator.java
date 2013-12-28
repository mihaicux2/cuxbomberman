/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.cux.bomberman.world.generator;

import com.cux.bomberman.world.World;
import com.cux.bomberman.world.walls.*;
import java.util.Random;

/**
 *
 * @author root
 */
public class WallGenerator {
    
    private static WallGenerator instance = null;
    
    // constructor ce duce la evitarea instantierii directe
    private WallGenerator(){}
    
   @Override
    // nu permite clonarea obiectului
    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }
    
    // evita instantierea mai multor obiecte de acest tip si in cazul thread-urilor
    public static synchronized WallGenerator getInstance(){
        if (instance == null){
            instance = new WallGenerator();
        }
        return instance;
    }
    
    public AbstractWall generateRandomWall(){
        int rand = (int) (Math.random()*1000000);
        int wW = World.getWidth();
        int wH = World.getHeight();
        Random r = new Random();
        int initialX = (r.nextInt(wW) / World.wallDim ) * World.wallDim;
        int initialY = (r.nextInt(wH) / World.wallDim ) * World.wallDim;
        
        AbstractWall ret = null;
        
        if      (rand % 5 == 0) ret = new WaterWall(initialX, initialY);
        else if (rand % 4 == 0) ret = new StoneWall(initialX, initialY);
        else if (rand % 3 == 0) ret = new GrassWall(initialX, initialY);
        else if (rand % 2 == 0) ret = new SteelWall(initialX, initialY);
        else                    ret = new BrickWall(initialX, initialY);
        
        int lastX = initialX + ret.getWidth();
        int lastY = initialY + ret.getHeight();
        
        if (lastX > wW) ret.setPosX(wW - ret.getWidth());
        if (lastY > wH) ret.setPosY(wH - ret.getHeight());
        
        //return ret.toString();
        return ret;
        
    }
    
}
