/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.cux.bomberman.world.generator;

import com.cux.bomberman.BombermanWSEndpoint;
import com.cux.bomberman.util.BLogger;
import com.cux.bomberman.world.World;
import com.cux.bomberman.world.walls.AbstractWall;

/**
 *
 * @author mihaicux
 */
public class WorldGenerator {
    
    /**
     * The only allowed instance of the WallGenerator class
     */
    private static WorldGenerator instance = null;

     /**
     * Private constructor to disallow direct instantiation
     */
    private WorldGenerator(){}

     /**
     * Public static method used to implement the Singleton pattern. Only one instance of this class is allowed
     * @return The only allowed instance of this class
     */
    public static synchronized WorldGenerator getInstance(){
        if (instance == null){
            instance = new WorldGenerator();
        }
        return instance;
    }

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
     * Public method used to generate a given size world
     * @param w The world width
     * @param h The world height
     * @param bricks The number of walls to be inserted
     * @return THe generated world
     */
    public World generateWorld(int w, int h, int bricks){
        World world = new World();
        world.setWidth(w);
        world.setHeight(h);
        int x, y;
        for (int i = 0; i < bricks; i++){
            AbstractWall wall = WallGenerator.getInstance().generateRandomWall(w, h);
            x = wall.getPosX() / World.wallDim;
            y = wall.getPosY() / World.wallDim; 
            if ((x ==0 && y ==0) || BombermanWSEndpoint.wallExists(world.blockMatrix, x, y)){
                i--;
            }
            else{
                world.walls.add(wall);
                world.blockMatrix[x][y] = wall;
                //System.out.println("wall["+x+"]["+y+"] = "+world.blockMatrix[x][y]);
            }
        }
        //BLogger.getInstance().log(BLogger.LEVEL_INFO, world.toString());
        return world;
    }
    
}
