package com.cux.bomberman.world;

import com.cux.bomberman.util.BLogger;
import com.cux.bomberman.world.walls.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This class is the "code" representation for the playable
 * worlds. It contains a matrix for all of it's building blocks
 * (AbstractWall's)
 * 
 * @version 1.0
 * @author  Mihail Cuculici (mihai.cuculici@gmail.com)
 * @author  http://www.
 */
public class World {
    
    /**
     * The block width
     */
    public final static int wallDim = 30; // width = height
    
    /**
     * The default world width
     */
    private int WIDTH = 660;
    
    /**
     * The default world height
     */
    private int HEIGHT = 510;
    
    /**
     * The default world width (in blocks numbers)
     */
    private int worldWidth = WIDTH > 0 && wallDim > 0 ? WIDTH/wallDim : 1;
    
    /**
     * The default world height (in blocks numbers)
     */
    private int worldHeight = HEIGHT > 0 && wallDim > 0 ? HEIGHT/wallDim: 1;
    
    /**
     * The file containing the map
     */
    private String mapFile;
    
    /**
     * Matrix with all the blocks on the map<br />
     * Used for mapping and collisions
     */
    public AbstractBlock[][] blockMatrix = null;
    
    /**
     * Matrix with all the characters in the map ()
     */
    public List<String>[][] chars = null;
    
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
     * Public getter for the worldWidth property
     * @return The requester property
     */
    public int getWorldWidth(){
        return worldWidth;
    }
    
    /**
     * Public getter for the worldHeight property
     * @return The requester property
     */
    public int getWorldHeight(){
        return worldHeight;
    }
    
    /**
     * Public getter for the mapFile property
     * @return The requester property
     */
    public String getMapFile(){
        return mapFile;
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
     * Public setter for the worldWidth property
     * @param worldWidth The new value
     */
    public void setWorldWidth(int worldWidth){
        this.worldWidth = worldWidth;
    }
    
    /**
     * Public setter for the worldHeight property
     * @param worldHeight The new value
     */
    public void setWorldHeight(int worldHeight){
        this.worldHeight = worldHeight;
    }
    
    /**
     * Public constructor (simple)
     */
    public World(){
        
        this.blockMatrix = new AbstractBlock[worldWidth+1][worldHeight+1];
        this.chars = new ArrayList[worldWidth+1][worldHeight+1];
        
        for (int i = 0; i <= worldWidth; i++){
            for (int j = 0; j <= worldHeight; j++){
                this.chars[i][j] = new ArrayList<String>();
            }
        }        
    }
    
//    public void addWall(AbstractWall wall){
//        this.walls.add(wall);
//        this.blockMatrix[wall.getBlockPosX()][wall.getBlockPosX()] = wall;
//    }
    
    /**
     * Public constructor used to load a map from a file
     * @param map The file to be loaded
     */
    public World(String map){
        this.mapFile = map;
        
        try(BufferedReader input = new BufferedReader(new FileReader(map))) {
            String line; //not declared within while loop
            Boolean firstLine = true;
            String first = line = input.readLine();
            String[] dims = line.split("x");
            this.worldWidth  = Integer.parseInt(dims[0]);
            this.worldHeight = Integer.parseInt(dims[1]);
            this.WIDTH = worldWidth * World.wallDim;
            if (this.WIDTH == 0) this.WIDTH = 660;
            this.HEIGHT = worldHeight * World.wallDim;
            if (this.HEIGHT == 0) this.WIDTH = 510;
            
            this.blockMatrix = new AbstractBlock[worldWidth+1][worldHeight+1];
            this.chars = new ArrayList[worldWidth+1][worldHeight+1];
            
            for (int i = 0; i <= worldWidth; i++){
                for (int j = 0; j <= worldHeight; j++){
                    this.chars[i][j] = new ArrayList<String>();
                }
            }
            
            AbstractWall wall;
            int x, y;
            //System.out.println(x1+", "+y1);
            for (int i = 0; i < worldHeight; i++){
                y = i * World.wallDim;
                line = input.readLine();
                if (line == null) break;
                for (int j = 0; j < worldWidth; j++){
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
    public boolean hasMapCollision(BCharacter myChar){
        
        int x = myChar.getBlockPosX();
        int y = myChar.getBlockPosY();
        
        // map bounds
        if ((x < 1 && "left".equals(myChar.getDirection())) ||
            (y < 1 && "up".equals(myChar.getDirection())) || 
            (x >= this.worldWidth-1 && "right".equals(myChar.getDirection())) ||
            (y >= this.worldHeight-1 && "down".equals(myChar.getDirection())))
        {
//            BLogger.getInstance().log(BLogger.LEVEL_FINE, "map margin");
            //myChar.stepBack(null);
            return true;
        }
        
        if (x > 0 && blockMatrix[x-1][y] != null && myChar.hits(blockMatrix[x-1][y]) && myChar.walksTo(blockMatrix[x-1][y])) return true;
        if (y > 0 && blockMatrix[x][y-1] != null && myChar.hits(blockMatrix[x][y-1]) && myChar.walksTo(blockMatrix[x][y-1])) return true;
        if (blockMatrix[x+1][y] != null && myChar.hits(blockMatrix[x+1][y]) && myChar.walksTo(blockMatrix[x+1][y])) return true;
        if (blockMatrix[x][y+1] != null && myChar.hits(blockMatrix[x][y+1]) && myChar.walksTo(blockMatrix[x][y+1])) return true;
        
        return false;
    }
    
    /**
     * Public synchronized method used to check if a wall exists in a
     * given position
     *
     * @param i The x coordinate of the checked position
     * @param j The y coordinate of the checked position
     * @return True if there is a wall at the given position
     */
    public synchronized boolean wallExists(int i, int j) {
        if (i < 0 || j < 0 || i >= this.worldWidth || j >= this.worldHeight) {
            return false;
        }
        try {
            AbstractBlock x = this.blockMatrix[i][j];
            if (x == null) {
                return false;
            }
            return AbstractWall.class.isAssignableFrom(x.getClass());
        } catch (ArrayIndexOutOfBoundsException e) {
            BLogger.getInstance().logException2(e);
            return false;
        }
    }
    
     /**
     * Public synchronized method used to check if a bomb exists in a
     * given position
     *
     * @param i The x coordinate of the checked position
     * @param j The y coordinate of the checked position
     * @return True if there is a bomb at the given position
     */
    public synchronized boolean bombExists(int i, int j) {
        if (i < 0 || j < 0 || i >= this.worldWidth || j >= this.worldHeight) {
            return false;
        }
        try {
            AbstractBlock x = this.blockMatrix[i][j];
            if (x == null) {
                return false;
            }
            return BBomb.class.isAssignableFrom(x.getClass());
        } catch (ArrayIndexOutOfBoundsException e) {
            BLogger.getInstance().logException2(e);
            return false;
        }
    }
    
    /**
     * Public method used to convert the world to JSON, to be sent to a client
     * @return The JSON representation of the character
     */
    @Override
    public String toString(){
        
        String ret = "";
        ret += this.WIDTH+"x"+this.HEIGHT+"[#walls#]";
        
        for (int i = 0; i < worldWidth; i++){
            for (int j = 0; j < worldHeight; j++){
                if (this.blockMatrix[i][j] != null){
                    ret += this.blockMatrix[i][j].toString()+"[#wallSep#]";
                }
            }
        }
        
        return ret;
    }
    
}
