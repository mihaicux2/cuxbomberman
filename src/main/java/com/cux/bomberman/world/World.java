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
 * @author root
 */
public class World {
    
    private final static int WIDTH = 640;
    private final static int HEIGHT = 480;
    private String mapContent = "";
    
    private String mapFile;
    
    public static Set<AbstractWall> bricks = Collections.synchronizedSet(new HashSet<AbstractWall>());
    
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
                AbstractWall brick = null;
                //mapContent += props[0]+"[kk]";
                switch(props[0]){
                    case "brick":
                        brick = new BrickWall(Integer.parseInt(props[1]), Integer.parseInt(props[2]));
                        break;
                    case "steel":
                        brick = new SteelWall(Integer.parseInt(props[1]), Integer.parseInt(props[2]));
                        break;
                    case "grass":
                        brick = new GrassWall(Integer.parseInt(props[1]), Integer.parseInt(props[2]));
                        break;
                    case "stone":
                        brick = new StoneWall(Integer.parseInt(props[1]), Integer.parseInt(props[2]));
                        break;
                    case "water":
                        brick = new WaterWall(Integer.parseInt(props[1]), Integer.parseInt(props[2]));
                    default:
                        //brick = new WaterWall(0, 0);
                        break;
                }
                this.bricks.add(brick);
            }
            input.close();
        } catch (IOException ex) {
            Logger.getLogger(World.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @Override
    public String toString(){
        String ret = "";
        for (AbstractWall brick : bricks){
            ret += brick.toString()+"[#brickSep#]";
            //ret += brick.getName()+"[#brickSep#]";
        }
        return ret;
        //return mapContent;
    }
    
}
