/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.cux.bomberman.world;

import com.cux.bomberman.world.walls.*;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;

/**
 *
 * @author mihaicux
 */
public class World {
    
    private final static int WIDTH = 640;
    private final static int HEIGHT = 480;
    public final static int wallDim = 30; // width = height
    private String mapContent = "";
    
    private String mapFile;
    
    // used for iterating
    public static Set<AbstractWall> walls = Collections.synchronizedSet(new HashSet<AbstractWall>());
    
    // used for mapping and collisions
    public static AbstractBlock[][] blockMatrix = new AbstractBlock[100][100];
    
    public static int getWidth(){
        return WIDTH;
    }
    
    public static int getHeight(){
        return HEIGHT;
    }
    
    public World(String map){
        this.mapFile = map;
        try {
            BufferedReader input =  new BufferedReader(new FileReader(map));
            String line = null; //not declared within while loop
            while (( line = input.readLine()) != null){
                String[] props = line.split("##");
                AbstractWall wall = null;
                int x = Integer.parseInt(props[1]);
                int y = Integer.parseInt(props[2]);
                System.out.println(line);
                switch(props[0]){
                    case "brick":
                        wall = new BrickWall(x, y);
                        break;
                    case "steel":
                        wall = new SteelWall(x, y);
                        break;
                    case "grass":
                        wall = new GrassWall(x, y);
                        break;
                    case "stone":
                        wall = new StoneWall(x, y);
                        break;
                    case "water":
                        wall = new WaterWall(Integer.parseInt(props[1]), Integer.parseInt(props[2]));
                    default:
                        //wall = new WaterWall(0, 0);
                        break;
                }
                if (wall != null){
                    wall.setHeight(World.wallDim);
                    wall.setWidth(World.wallDim);
                    this.walls.add(wall);
                    blockMatrix[x/World.wallDim][y/World.wallDim] = wall;
                }
            }
            input.close();
        } catch (IOException ex) {
            Logger.getLogger(World.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    public synchronized boolean HasMapCollision(BCharacter myChar){
        if ((myChar.getPosX() == 0 && myChar.getDirection() == "Left") ||
            (myChar.getPosY()== 0 && myChar.getDirection() == "Up") || 
            (myChar.getPosX()+myChar.getWidth() == this.WIDTH && myChar.getDirection() == "Right") ||
            (myChar.getPosY()+myChar.getHeight() == this.HEIGHT && myChar.getDirection() == "Down"))
        {
            //myChar.stepBack(null);
            return true;
        }
        
        int x1 = myChar.getPosX()/World.wallDim;
        int y1 = myChar.getPosY()/World.wallDim;
        
        //if (blockMatrix[x1][y1] != null && myChar.hits(blockMatrix[x1][y1]) && myChar.walksTo(blockMatrix[x1][y1])) return true;
        if (x1 > 0 && blockMatrix[x1-1][y1] != null && myChar.hits(blockMatrix[x1-1][y1]) && myChar.walksTo(blockMatrix[x1-1][y1])) return true;
        if (y1 > 0 && blockMatrix[x1][y1-1] != null && myChar.hits(blockMatrix[x1][y1-1]) && myChar.walksTo(blockMatrix[x1][y1-1])) return true;
        if (x1 > 0 && y1 > 0 && blockMatrix[x1-1][y1-1] != null && myChar.hits(blockMatrix[x1-1][y1-1]) && myChar.walksTo(blockMatrix[x1-1][y1-1])) return true;
        if (x1 < 99 && blockMatrix[x1+1][y1] != null && myChar.hits(blockMatrix[x1+1][y1]) && myChar.walksTo(blockMatrix[x1+1][y1])) return true;
        if (y1 < 99 && blockMatrix[x1][y1+1] != null && myChar.hits(blockMatrix[x1][y1+1]) && myChar.walksTo(blockMatrix[x1][y1+1])) return true;
        if (x1 < 99 && y1 < 99 && blockMatrix[x1+1][y1+1] != null && myChar.hits(blockMatrix[x1+1][y1+1]) && myChar.walksTo(blockMatrix[x1+1][y1+1])) return true;
        if (x1 > 0 && y1 < 99 && blockMatrix[x1-1][y1+1] != null && myChar.hits(blockMatrix[x1-1][y1+1]) && myChar.walksTo(blockMatrix[x1-1][y1+1])) return true;
        if (x1 < 99 && y1 > 0 && blockMatrix[x1+1][y1-1] != null && myChar.hits(blockMatrix[x1+1][y1-1]) && myChar.walksTo(blockMatrix[x1+1][y1-1])) return true;
        
        return false;
    }
    
    @Override
    public String toString(){
        String ret = "";
        for (AbstractWall wall : walls){
            ret += wall.toString()+"[#wallSep#]";
            //ret += wall.getName()+"[#wallSep#]";
        }
        return ret;
        //return mapContent;
    }
    
}
