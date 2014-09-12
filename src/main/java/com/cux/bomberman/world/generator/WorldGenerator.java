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
    
    private static WorldGenerator instance = null;

    // constructor ce duce la evitarea instantierii directe
    private WorldGenerator(){}

    // evita instantierea mai multor obiecte de acest tip si in cazul thread-urilor
    public static synchronized WorldGenerator getInstance(){
        if (instance == null){
            instance = new WorldGenerator();
        }
        return instance;
    }

    // nu permite clonarea obiectului
    @Override
    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }
    
    public World generateWorld(int w, int h, int bricks){
        World world = new World();
        world.setWidth(w);
        world.setHeight(h);
        int x, y;
        for (int i = 0; i < bricks; i++){
            AbstractWall wall = WallGenerator.getInstance().generateRandomWall();
            x = wall.getPosX() / World.wallDim;
            y = wall.getPosY() / World.wallDim; 
            if ((x ==0 && y ==0) || BombermanWSEndpoint.getInstance().wallExists(world.blockMatrix, x, y)){
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
