/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.cux.bomberman.world;

import com.cux.bomberman.BombermanWSEndpoint;
import com.cux.bomberman.util.BLogger;
import com.cux.bomberman.world.items.AbstractItem;
import com.cux.bomberman.world.walls.AbstractWall;
import java.util.AbstractMap;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Random;
import javax.websocket.EndpointConfig;

/**
 * @author mihaicux
 */
public class BMediumBot extends BBaseBot{

    private static final String[] messages = {"Bear is love. Bear is life",
                                              "Bad luck Brian",
                                              "Scumbag Stacy",
                                              "Goog guy Greg",
                                              "Dr. Evil Air Quotes",
                                              "Doge",
                                              "Confession Kid",
                                              "That's the Evilest Thing I Can Imagine",
                                              "Bitches Be Like",
                                              "Surprised Patrick",
                                              "Fuck Logic",
                                              "Me Gusta"};
    
    public BMediumBot(String id, String name, int roomIndex, EndpointConfig config) {
        super(id, name, roomIndex, config);
        this.searchRange = 5;
    }
    
    @Override
    public void searchAndDestroy(){
        
        //System.out.println("search & destroy");
        
        // current bot position
        int x = this.posX / World.wallDim;
        int y = this.posY / World.wallDim;
        
        int xLeft = x,
            xRight = x,
            yUp = y,
            yDown = y;
        
        boolean expandUp=true,
                expandDown=true,
                expandLeft=true,
                expandRight=true,
                checkFurther=true,
                bombFound = false;
        
        // check and try to avoid bombs (self-preservation instinct :> )
        while (checkFurther){
            
            if (!expandLeft && !expandRight && !expandUp && !expandDown){
                checkFurther = false;
                break;
            }
            
            // check for bombs in the left
            if (expandLeft && xLeft > 0 ){
                String checkLeft = BombermanWSEndpoint.checkWorldMatrix(this.roomIndex, xLeft, y);
                switch(checkLeft){
                    case "bomb":
                        expandLeft = false;
                        // if the bomb range raches the bot, must avoid explosion
                        if (BombermanWSEndpoint.getInstance().bombReaches((BBomb)BombermanWSEndpoint.map.get(this.roomIndex).blockMatrix[xLeft][y], x-xLeft)){
                            this.avoidBomb("left", x, y);
                            checkFurther = false;
                            bombFound = true;
                        }
                        break;
                    case "wall":
                        expandLeft = false;
                        break;
                    default:
                        xLeft--;
                }
            }
            else{
                expandLeft = false;
            }
            
            // check for bombs in the right
            if (checkFurther && expandRight && xRight < World.getWidth() / World.wallDim ){
                String checkRight = BombermanWSEndpoint.checkWorldMatrix(this.roomIndex, xRight, y);
                switch(checkRight){
                    case "bomb":
                        expandRight = false;
                        // if the bomb range raches the bot, must avoid explosion
                        if (BombermanWSEndpoint.getInstance().bombReaches((BBomb)BombermanWSEndpoint.map.get(this.roomIndex).blockMatrix[xRight][y], xRight - x)){
                            this.avoidBomb("right", x, y);
                            checkFurther = false;
                            bombFound = true;
                        }
                        break;
                    case "wall":
                        expandRight = false;
                        break;
                    default:
                        xRight++;
                }
            }
            else{
                expandRight = false;
            }
            
            // check for bombs up
            if (checkFurther && expandUp && yUp > 0 ){
                String checkUp = BombermanWSEndpoint.checkWorldMatrix(this.roomIndex, x, yUp);
                switch(checkUp){
                    case "bomb":
                        // if the bomb range raches the bot, must avoid explosion
                        if (BombermanWSEndpoint.getInstance().bombReaches((BBomb)BombermanWSEndpoint.map.get(this.roomIndex).blockMatrix[x][yUp], y - yUp)){
                            this.avoidBomb("up", x, y);
                            checkFurther = false;
                            bombFound = true;
                        }
                        break;
                    case "wall":
                        expandUp = false;
                        break;
                    default:
                        yUp--;
                }
            }
            else{
                expandUp = false;
            }
            
            // check for bombs down
            if (checkFurther && expandDown && yDown < World.getHeight() / World.wallDim ){
                String checkDown = BombermanWSEndpoint.checkWorldMatrix(this.roomIndex, x, yDown);
                switch(checkDown){
                    case "bomb":
                        expandDown = false;
                        // if the bomb range raches the bot, must avoid explosion
                        if (BombermanWSEndpoint.getInstance().bombReaches((BBomb)BombermanWSEndpoint.map.get(this.roomIndex).blockMatrix[x][yDown], yDown - y)){
                            this.avoidBomb("down", x, y);
                            checkFurther = false;
                            bombFound = true;
                        }
                        break;
                    case "wall":
                        expandDown = false;
                        break;
                    default:
                        yDown++;
                }
            }
            else{
                expandDown = false;
            }
        }
        
        // if there is no danger, check for best next move
        if (!bombFound){
            int xMin = Math.max(0, x - searchRange);
            int xMax = Math.min(World.getWidth() / World.wallDim, x + searchRange);
            int yMin = Math.min(0, y - searchRange);
            int yMax = Math.max(World.getHeight() / World.wallDim, y + searchRange);
            int j, i;
            /**
             * Map the surrounding blocks:
             * 0  - empty block
             * 1  - blowable wall
             * -1 - unblowable wall
             * 2  - positive item
             * -2 - negative item
             * 3  - bomb
             * 4  - character
             */ 
            int blocks[][]  = new int[xMax-xMin + 1][yMax - yMin +1];
            // adjiacence graph
            HashMap<AbstractBlock, Queue<SimpleEntry<AbstractBlock, String>>> neighbours = new HashMap<AbstractBlock, Queue<SimpleEntry<AbstractBlock, String>>>();
            HashMap<String, Integer> distances = new HashMap<String, Integer>();
            ArrayList<AbstractItem> nearItems = new ArrayList<AbstractItem>();
            ArrayList<AbstractWall> nearWalls = new ArrayList<AbstractWall>();
            ArrayList<BCharacter>   nearChars = new ArrayList<BCharacter>();

            // preprocessing
            for (i = xMin; i <= xMax; i++){
                for (j = yMin; j <= yMax; j++){
                    AbstractBlock block = BombermanWSEndpoint.map.get(this.roomIndex).blockMatrix[i][j];
                    if (!neighbours.containsKey(block)){
                        neighbours.put(block, new LinkedList<SimpleEntry<AbstractBlock, String>>());
                    }
                    String type = BombermanWSEndpoint.checkWorldMatrix(this.roomIndex, i, j);
                    switch (type){
                        case "wall":
                            if (((AbstractWall)block).isBlowable()){
                                blocks[i-xMin][j-yMin] = 1; // blowable wall
                                nearWalls.add((AbstractWall)block);
                            }
                            else{
                                blocks[i-xMin][j-yMin] = -1; // unblowable wall...
                            }
                            break;
                        case "item":
                            String itemType = ((AbstractItem)block).getName();
                            switch (itemType){
                                case "ebola":
                                case "slow":
                                    blocks[i-xMin][j-yMin] = -2; // items that we don't want
                                    break;
                                default:
                                    blocks[i-xMin][j-yMin] = 2;
                                    nearItems.add((AbstractItem)block);
                                    break;
                            }
                            break;
                        case "bomb":
                            blocks[i-xMin][j-yMin] = 3;
                            break;
                        case "char":
                            blocks[i-xMin][j-yMin] = 4;
                            nearChars.add((BCharacter)block);
                            break;
                            /**
                             * The next piece of code is kept only with didactical purpose.
                             * It is commented out because JAVA, by default, sets all integers to 0
                             */
//                        case "empty":
//                        default:
//                            blocks[i-xMin][j-yMin] = 0;
                    }
                }
            }
            
            for (i = xMin; i <= xMax; i++){
                for (j = yMin; j <= yMax; j++){
                    AbstractBlock block = BombermanWSEndpoint.map.get(this.roomIndex).blockMatrix[i][j];
                    if ( i - 1 > -1){ // left block
                        if (blocks[i-1][j] == 4 || blocks[i-1][j] == 2 || blocks[i-1][j] == 0){
                            AbstractBlock block2 = BombermanWSEndpoint.map.get(this.roomIndex).blockMatrix[i-1][j];
                            neighbours.get(block).add(new SimpleEntry(block2, "left"));
                            distances.put(i+"_"+j+"_"+(i-1)+"_"+j, 1);
                        }
                        else{
                            distances.put(i+"_"+j+"_"+(i-1)+"_"+j, Integer.MAX_VALUE);
                        }
                    }
                    if (i + 1 <= xMax){ // right block
                        if (blocks[i+1][j] == 4 || blocks[i+1][j] == 2 || blocks[i+1][j] == 0){
                            AbstractBlock block2 = BombermanWSEndpoint.map.get(this.roomIndex).blockMatrix[i+1][j];
                            neighbours.get(block).add(new SimpleEntry(block2, "right"));
                            distances.put(i+"_"+j+"_"+(i+1)+"_"+j, 1);
                        }
                        else{
                            distances.put(i+"_"+j+"_"+(i+1)+"_"+j, Integer.MAX_VALUE);
                        }
                    }
                    if ( j - 1 > -1){ // up block
                        if (blocks[i][j-1] == 4 || blocks[i][j-1] == 2 || blocks[i][j-1] == 0){
                            AbstractBlock block2 = BombermanWSEndpoint.map.get(this.roomIndex).blockMatrix[i][j-1];
                            neighbours.get(block).add(new SimpleEntry(block2, "up"));
                            distances.put(i+"_"+j+"_"+i+"_"+(j-1), 1);
                        }
                        else{
                            distances.put(i+"_"+j+"_"+i+"_"+(j-1), Integer.MAX_VALUE);
                        }
                    }
                    if ( j - 1 <= yMax){ // down block
                        if (blocks[i][j+1] == 4 || blocks[i][j+1] == 2 || blocks[i][j+1] == 0){
                            AbstractBlock block2 = BombermanWSEndpoint.map.get(this.roomIndex).blockMatrix[i][j+1];
                            neighbours.get(block).add(new SimpleEntry(block2, "down"));
                            distances.put(i+"_"+j+"_"+i+"_"+(j+1), 1);
                        }
                        else{
                            distances.put(i+"_"+j+"_"+i+"_"+(j+1), Integer.MAX_VALUE);
                        }
                    }
                }
                distances.put(i+"_"+j+"_"+i+"_"+(j+1), 0);
            }
            
        }
        
    }

    public ArrayList<String> dijkstra(AbstractBlock source, AbstractBlock dest, HashMap<AbstractBlock, Queue<SimpleEntry<AbstractBlock, String>>> neighbours, HashMap<String, Integer> distances){
        HashMap<AbstractBlock, Boolean> vizited = new HashMap<AbstractBlock, Boolean>();
        HashMap<String, ArrayList<String>> road = new HashMap<String, ArrayList<String>>();
        vizited.put(source, true);
        if (neighbours.containsKey(source)){
            Queue<SimpleEntry<AbstractBlock, String>> queue = new LinkedList<SimpleEntry<AbstractBlock, String>>();
            queue.add(new SimpleEntry(source, "self")); // add the first block (source)
            SimpleEntry<AbstractBlock, String> crtBlock;
            while (!queue.isEmpty() && !vizited.containsKey(dest)){
                crtBlock = queue.poll();
                vizited.put(crtBlock.getKey(), true);
                String key = source.getPosX()/World.wallDim+"_"+source.getPosY()/World.wallDim+"_"+crtBlock.getKey().getPosX()/World.wallDim+"_"+crtBlock.getKey().getPosY()/World.wallDim;
                if (!road.containsKey(key)){
                    road.put(key, new ArrayList<String>());
                }
                if (!crtBlock.equals(source)){
                    if (distances.get(key) == Integer.MAX_VALUE){
                        distances.put(key, 1);
                    }
                    else{
                        distances.put(key, 1 + distances.get(key));
                    }
                    road.get(key).add(crtBlock.getValue());
                }
                Queue<SimpleEntry<AbstractBlock, String>> neighbs = neighbours.get(crtBlock);
                while (!neighbs.isEmpty()){
                    SimpleEntry<AbstractBlock, String> neighb = neighbs.poll();
                    if (!vizited.containsKey(neighb.getKey())){
                        queue.add(neighb);
                    }
                }
            }
        }
        if (vizited.containsKey(dest)){
            String key = source.getPosX()/World.wallDim+"_"+source.getPosY()/World.wallDim+"_"+dest.getPosX()/World.wallDim+"_"+dest.getPosY()/World.wallDim;
            return road.get(key);
        }
        return null;
    }
    
    @Override
    public void avoidBomb(String bombLocation, int x, int y){
//        System.out.println("bomb detected "+bombLocation);
        boolean move = true;
        switch(bombLocation){
            case "up":
                if (x > 0 && !this.markedBlock[x-1][y]){ // try to go left
                    String checkLeft = BombermanWSEndpoint.checkWorldMatrix(this.roomIndex, x-1, y);
                    if (checkLeft.equals("char") || checkLeft.equals("empty")){
                        this.moveLeft();
                        move = false;
                        this.markedBlock[x-1][y] = true;
                    }
                }
                else if (x > 0){
                    this.markedBlock[x-1][y] = false;
                }
                
                if (move && x+1 < World.getWidth() / World.wallDim && !this.markedBlock[x+1][y]){ // try to go right
                    String checkRight = BombermanWSEndpoint.checkWorldMatrix(this.roomIndex, x+1, y);
                    if (checkRight.equals("char") || checkRight.equals("empty")){
                        this.moveRight();
                        move = false;
                        this.markedBlock[x+1][y] = true;
                    }
                }
                else if (x+1 < World.getWidth() / World.wallDim){
                    this.markedBlock[x+1][y] = false;
                }
                
                if (move && y+1 < World.getHeight() / World.wallDim && !this.markedBlock[x][y+1]){ // try to go down
                    String checkDown = BombermanWSEndpoint.checkWorldMatrix(this.roomIndex, x, y+1);
                    if (checkDown.equals("char") || checkDown.equals("empty")){
                        this.moveDown();
                        move = false;
                        this.markedBlock[x][y+1] = true;
                    }
                }
                else if (y+1 < World.getHeight() / World.wallDim){
                    this.markedBlock[x][y+1] = false;
                }
                break;
            case "down":
                if (x > 0 && !this.markedBlock[x-1][y]){ // try to go left
                    String checkLeft = BombermanWSEndpoint.checkWorldMatrix(this.roomIndex, x-1, y);
                    if (checkLeft.equals("char") || checkLeft.equals("empty")){
                        this.moveLeft();
                        move = false;
                        this.markedBlock[x-1][y] = true;
                    }
                }
                else if (x > 0){
                    this.markedBlock[x-1][y] = false;
                }
                
                if (move && x+1 < World.getWidth() / World.wallDim && !this.markedBlock[x+1][y]){ // try to go right
                    String checkRight = BombermanWSEndpoint.checkWorldMatrix(this.roomIndex, x+1, y);
                    if (checkRight.equals("char") || checkRight.equals("empty")){
                        this.moveRight();
                        move = false;
                        this.markedBlock[x+1][y] = true;
                    }
                }
                else if (x+1 < World.getWidth() / World.wallDim){
                    this.markedBlock[x+1][y] = false;
                }
                
                if (move && y > 0 && !this.markedBlock[x][y-1]){ // try to go up
                    String checkUp = BombermanWSEndpoint.checkWorldMatrix(this.roomIndex, x, y-1);
                    if (checkUp.equals("char") || checkUp.equals("empty")){
                        this.moveUp();
                        move = false;
                        this.markedBlock[x][y-1] = true;
                    }
                }
                else if (y > 0){
                    this.markedBlock[x][y-1] = true;
                }
                break;
            case "left":
                if (move && y > 0 && !this.markedBlock[x][y-1]){ // try to go up
                    String checkUp = BombermanWSEndpoint.checkWorldMatrix(this.roomIndex, x, y-1);
                    if (checkUp.equals("char") || checkUp.equals("empty")){
                        this.moveUp();
                        move = false;
                        this.markedBlock[x][y-1] = true;
                    }
                }
                else if (y > 0){
                    this.markedBlock[x][y-1] = true;
                }
                
                if (move && y+1 < World.getHeight() / World.wallDim && !this.markedBlock[x][y+1]){ // try to go down
                    String checkDown = BombermanWSEndpoint.checkWorldMatrix(this.roomIndex, x, y+1);
                    if (checkDown.equals("char") || checkDown.equals("empty")){
                        this.moveDown();
                        move = false;
                        this.markedBlock[x][y+1] = true;
                    }
                }
                else if (y+1 < World.getHeight() / World.wallDim){
                    this.markedBlock[x][y+1] = false;
                }
                
                if (move && x+1 < World.getWidth() / World.wallDim && !this.markedBlock[x+1][y]){ // try to go right
                    String checkRight = BombermanWSEndpoint.checkWorldMatrix(this.roomIndex, x+1, y);
                    if (checkRight.equals("char") || checkRight.equals("empty")){
                        this.moveRight();
                        move = false;
                        this.markedBlock[x+1][y] = true;
                    }
                }
                else if (x+1 < World.getWidth() / World.wallDim){
                    this.markedBlock[x+1][y] = false;
                }
                break;
            case "right":
                if (move && y > 0 && !this.markedBlock[x][y-1]){ // try to go up
                    String checkUp = BombermanWSEndpoint.checkWorldMatrix(this.roomIndex, x, y-1);
                    if (checkUp.equals("char") || checkUp.equals("empty")){
                        this.moveUp();
                        move = false;
                        this.markedBlock[x][y-1] = true;
                    }
                }
                else if (y > 0){
                    this.markedBlock[x][y-1] = true;
                }
                
                if (move && y+1 < World.getHeight() / World.wallDim && !this.markedBlock[x][y+1]){ // try to go down
                    String checkDown = BombermanWSEndpoint.checkWorldMatrix(this.roomIndex, x, y+1);
                    if (checkDown.equals("char") || checkDown.equals("empty")){
                        this.moveDown();
                        move = false;
                        this.markedBlock[x][y+1] = true;
                    }
                }
                else if (y+1 < World.getHeight() / World.wallDim){
                    this.markedBlock[x][y+1] = false;
                }
                
                if (x > 0 && !this.markedBlock[x-1][y]){ // try to go left
                    String checkLeft = BombermanWSEndpoint.checkWorldMatrix(this.roomIndex, x-1, y);
                    if (checkLeft.equals("char") || checkLeft.equals("empty")){
                        this.moveLeft();
                        move = false;
                        this.markedBlock[x-1][y] = true;
                    }
                }
                else if (x > 0){
                    this.markedBlock[x-1][y] = false;
                }
                break;
        }
    }
    
    @Override
    public void run() {
        while (this.running) {
            try {
                this.searchAndDestroy();
                Thread.sleep(100); // limit bot action to 10 FPS
            } catch (InterruptedException ex) {
                BLogger.getInstance().logException2(ex);
            }
        }
    }
    
}
