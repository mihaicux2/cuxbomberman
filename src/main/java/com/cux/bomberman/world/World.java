/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.cux.bomberman.world;

import com.cux.bomberman.util.BLogger;
import com.cux.bomberman.world.walls.*;
import java.io.BufferedReader;
//import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.Collections;
import java.util.HashMap;
//import java.util.HashSet;
//import java.util.Set;
//import java.util.Vector;
//import java.util.logging.Level;
//import java.util.logging.Logger;
//import org.codehaus.jackson.map.ObjectMapper;
//import org.codehaus.jackson.map.ObjectWriter;

/**
 *
 * @author mihaicux
 */
public class World {
    
    private static int WIDTH = 660;
    private static int HEIGHT = 510;
    public final static int wallDim = 30; // width = height
    private String mapContent = "";
    
    private String mapFile;
    
    // used for iterating
    public ArrayList<AbstractWall> walls = new ArrayList<>();
    
    // used for mapping and collisions
    public AbstractBlock[][] blockMatrix = new AbstractBlock[300][300];
    
    public HashMap<String, BCharacter>[][] chars = new HashMap[300][300];
    
    public static int getWidth(){
        return WIDTH;
    }
    
    public static int getHeight(){
        return HEIGHT;
    }
    
    public static void setWidth(int width){
        WIDTH = width;
    }
    
    public static void setHeight(int height){
        HEIGHT = height;
    }
    
    public World(){
        for (int i = 0; i < 300; i++){
            for (int j = 0; j < 300; j++){
                chars[i][j] = new HashMap<String, BCharacter>();
            }
        }        
    }
    
//    public void addWall(AbstractWall wall){
//        this.walls.add(wall);
//        this.blockMatrix[wall.getPosX()/World.wallDim][wall.getPosX()/World.wallDim] = wall;
//    }
    
    public World(String map){
        this.mapFile = map;
        try {
            for (int i = 0; i < 100; i++){
                for (int j = 0; j < 100; j++){
                    chars[i][j] = new HashMap<String, BCharacter>();
                }
            }
            BufferedReader input =  new BufferedReader(new FileReader(map));
            String line = null; //not declared within while loop
            Boolean firstLine = true;
            while (( line = input.readLine()) != null){
                if (firstLine == true){
                    String[] dims = line.split("x");
                    World.WIDTH = (Integer.parseInt(dims[0])/World.wallDim) * World.wallDim;
                    if (World.WIDTH == 0) World.WIDTH = 660;
                    World.HEIGHT = (Integer.parseInt(dims[1])/World.wallDim) * World.wallDim;
                    if (World.HEIGHT == 0) World.WIDTH = 510;
                    firstLine = false;
                    continue;
                }
                String[] props = line.split("##");
                AbstractWall wall = null;
                int x = Integer.parseInt(props[1]);
                int y = Integer.parseInt(props[2]);
                //System.out.println(line);
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
                        wall = new WaterWall(x, y);
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
            BLogger.getInstance().logException2(ex);
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
        
        ArrayList<AbstractWall> walls2 = (ArrayList<AbstractWall>)walls.clone();
        
        String ret = "";
        ret += World.WIDTH+"x"+World.HEIGHT+"[#walls#]";
        for (AbstractWall wall : walls2){
            ret += wall.toString()+"[#wallSep#]";
        }
        return ret;
    }
    
}
