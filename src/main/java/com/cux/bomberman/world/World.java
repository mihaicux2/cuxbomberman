package com.cux.bomberman.world;

import com.cux.bomberman.util.BLogger;
import com.cux.bomberman.world.walls.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author mihaicux
 */
public class World {
    
    /**
     * The default world width
     */
    private int WIDTH = 660;
    
    /**
     * The default world height
     */
    private int HEIGHT = 510;
    
    /**
     * The block width
     */
    public final static int wallDim = 30; // width = height
    
    /**
     * The file containing the map
     */
    private String mapFile;
    
    /**
     * List of the walls,<br />
     * Used for iteration
     */
    public ArrayList<AbstractWall> walls = new ArrayList<>();
    
    /**
     * Matrix with all the blocks on the map<br />
     * Used for mapping and collisions
     */
    public AbstractBlock[][] blockMatrix = new AbstractBlock[100][100];
    
    /**
     * Matrix with all the characters in the map
     */
    public HashMap<String, BCharacter>[][] chars = new HashMap[100][100];
    
    /**
     * Public getter for the WIDTH property
     * @return The requester property
     */
    public int getWidth(){
        return WIDTH;
    }
    
    /**
     * Public getter for the HEIGHT property
     * @return The requester property
     */
    public int getHeight(){
        return HEIGHT;
    }
    
    /**
     * Public setter for the WIDTH property
     * @param width The new value
     */
    public void setWidth(int width){
        WIDTH = width;
    }
    
    /**
     * Public setter for the HEIGHT property
     * @param height The new value
     */
    public void setHeight(int height){
        HEIGHT = height;
    }
    
    /**
     * Public constructor (simple)
     */
    public World(){
        for (int i = 0; i < 100; i++){
            for (int j = 0; j < 100; j++){
                chars[i][j] = new HashMap<String, BCharacter>();
            }
        }        
    }
    
//    public void addWall(AbstractWall wall){
//        this.walls.add(wall);
//        this.blockMatrix[wall.getPosX()/World.wallDim][wall.getPosX()/World.wallDim] = wall;
//    }
    
    /**
     * Public constructor used to load a map from a file
     * @param map The file to be loaded
     */
    public World(String map){
        this.mapFile = map;
        for (int i = 0; i < 100; i++){
            for (int j = 0; j < 100; j++){
                chars[i][j] = new HashMap<String, BCharacter>();
            }
        }
        try(BufferedReader input = new BufferedReader(new FileReader(map))) {
            String line; //not declared within while loop
            Boolean firstLine = true;
            String first = line = input.readLine();
            String[] dims = line.split("x");
            this.WIDTH = Integer.parseInt(dims[0]) * World.wallDim;
            if (this.WIDTH == 0) this.WIDTH = 660;
            this.HEIGHT = Integer.parseInt(dims[1]) * World.wallDim;
            if (this.HEIGHT == 0) this.WIDTH = 510;
            AbstractWall wall;
            int x1 = this.WIDTH / World.wallDim,
                y1 = this.HEIGHT / World.wallDim,
                x,
                y;
            //System.out.println(x1+", "+y1);
            for (int i = 0; i < y1; i++){
                y = i * World.wallDim;
                line = input.readLine();
                if (line == null) break;
                for (int j = 0; j < x1; j++){
                    wall = null;
                    x = j * World.wallDim;
                    switch(line.charAt(j)){
                        case 'b':
                            wall = new BrickWall(x, y);
                            break;
                        case 's':
                            wall = new SteelWall(x, y);
                            break;
                        case 'g':
                            wall = new GrassWall(x, y);
                            break;
                        case 'r':
                            wall = new StoneWall(x, y);
                            break;
                        case 'w':
                            wall = new WaterWall(x, y);
                            break;
                        default:
                            //wall = new WaterWall(0, 0);
                            break;
                    }
                    if (wall != null) {
                        wall.setHeight(World.wallDim);
                        wall.setWidth(World.wallDim);
                        wall.wallId = java.util.UUID.randomUUID().toString();
                        this.walls.add(wall);
                        this.blockMatrix[j][i] = wall;
                        
                    }   
                }
                //System.out.println();
            }
            input.close();
        } catch (IOException ex) {
            BLogger.getInstance().logException2(ex);
        }
        
    }
    
    /**
     * Public method used to test if a given character has collisions with any of the existing blocks in the map
     * @param myChar The character to be tested
     * @return TRUE if such a collision is found
     */
    public synchronized boolean HasMapCollision(BCharacter myChar){
        if ((myChar.getPosX() == 0 && "Left".equals(myChar.getDirection())) ||
            (myChar.getPosY()== 0 && "Up".equals(myChar.getDirection())) || 
            (myChar.getPosX()+myChar.getWidth() == this.WIDTH && "Right".equals(myChar.getDirection())) ||
            (myChar.getPosY()+myChar.getHeight() == this.HEIGHT && "Down".equals(myChar.getDirection())))
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
        return x1 < 99 && y1 > 0 && blockMatrix[x1+1][y1-1] != null && myChar.hits(blockMatrix[x1+1][y1-1]) && myChar.walksTo(blockMatrix[x1+1][y1-1]);
    }
    
    /**
     * Public method used to convert the character to JSON, to be sent to a client
     * @return The JSON representation of the character
     */
    @Override
    public String toString(){
        
//        ArrayList<AbstractWall> walls2 = (ArrayList<AbstractWall>)walls.clone();
        Set<AbstractWall> walls2 = Collections.synchronizedSet(new HashSet<AbstractWall>(walls));
        
        String ret = "";
        ret += this.WIDTH+"x"+this.HEIGHT+"[#walls#]";
        for (AbstractWall wall : walls2){
            ret += wall.toString()+"[#wallSep#]";
        }
        return ret;
    }
    
}
