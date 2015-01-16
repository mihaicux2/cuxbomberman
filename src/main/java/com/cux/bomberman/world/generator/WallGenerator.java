package com.cux.bomberman.world.generator;

import com.cux.bomberman.world.World;
import com.cux.bomberman.world.walls.*;
import java.util.Random;

/**
 * This class uses random to generate random walls
 * 
 * @version 1.0
 * @author  Mihail Cuculici (mihai.cuculici@gmail.com)
 * @author  http://www.
 */
public class WallGenerator {
    
    /**
     * The only allowed instance of the WallGenerator class
     */
    private static WallGenerator instance = null;
    
    /**
     * Private constructor to disallow direct instantiation
     */
    private WallGenerator(){}
    
    /**
     * Overwritten method to disallow cloning of the instantiated object. [Singleton pattern]
     * @return NULL
     * @throws CloneNotSupportedException 
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }
    
    /**
     * Public static method used to implement the Singleton pattern. Only one instance of this class is allowed
     * @return The only allowed instance of this class
     */
    public static synchronized WallGenerator getInstance(){
        if (instance == null){
            instance = new WallGenerator();
        }
        return instance;
    }
    
    /**
     * Public method used to generate a wall (random, any of the defined walls)
     * @param wW The World width
     * @param wH The World height
     * @return The generated wall
     */
    public AbstractWall generateRandomWall(int wW, int wH){
        int rand = (int) (Math.random()*1000000);
        Random r = new Random();
        int initialX = (r.nextInt(wW) / World.wallDim ) * World.wallDim;
        int initialY = (r.nextInt(wH) / World.wallDim ) * World.wallDim;
        
        AbstractWall ret;
        
        if      (rand % 5 == 0) ret = new WaterWall(initialX, initialY);
        else if (rand % 4 == 0) ret = new StoneWall(initialX, initialY);
        else if (rand % 3 == 0) ret = new GrassWall(initialX, initialY);
        else if (rand % 2 == 0) ret = new SteelWall(initialX, initialY);
        else                    ret = new BrickWall(initialX, initialY);
        
        int lastX = initialX + ret.getWidth();
        int lastY = initialY + ret.getHeight();
        
        if (lastX > wW) ret.setPosX(wW - ret.getWidth());
        if (lastY > wH) ret.setPosY(wH - ret.getHeight());
    
        ret.wallId = java.util.UUID.randomUUID().toString();
        
        //return ret.toString();
        return ret;
        
    }
    
}
