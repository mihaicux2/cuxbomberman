/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.cux.bomberman.world;

import com.cux.bomberman.BombermanWSEndpoint;
import static com.cux.bomberman.BombermanWSEndpoint.map;
import com.cux.bomberman.util.BLogger;
import com.cux.bomberman.world.items.AbstractItem;
import com.cux.bomberman.world.walls.AbstractWall;
import com.cux.bomberman.world.walls.EmptyWall;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Random;
import javax.websocket.EndpointConfig;

/**
 * @author mihaicux
 */
public class BMediumBot extends BBaseBot{
    
    private final Queue<String> path = new LinkedList<String>();
    
    private String previousMove = null;
    
    public BMediumBot(String id, String name, int roomIndex, EndpointConfig config) {
        super(id, name, roomIndex, config);
        this.searchRange = 6; // 13 x 13 (13 = 1 + 6*2) matrix for the search area
    }
    
    public void moveRandom(){
        //if (previousMove == null){
            Random r = new Random();
            int rand = r.nextInt(100000);
            if      (rand % 4 == 0) previousMove = "left";
            else if (rand % 3 == 0) previousMove = "down";
            else if (rand % 2 == 0) previousMove = "up";
            else                    previousMove = "right";
        //}
        move(previousMove);
    }
    
    /**
     * 
     */
    @Override
    public void searchAndDestroy(){
        
        // if somehow the bot is still walking, wait for it to stop
        if (this.isWalking()) return;
        
        boolean onABomb= ISitOnABomb(), // check if I sit on a bomb
                bombFound = !onABomb && IHaveBombsNearby(), // check if any bomb can kill me
                neighbourFound = !bombFound && CheckNeighbours(); // check the neighbours for a best move
        
        /**
         * If a bomb, an item or a character has been found by this point, no further search is required.<br />
         * Also, empty any previous computed path
         */
        if (neighbourFound || bombFound || onABomb){
            this.path.clear();
            return;
        }
        
        /**
         * If there are explosions triggered, wait for them to dissapear before calculating the best move
         */
        if (BombermanWSEndpoint.explosions.get(this.roomIndex) != null && !BombermanWSEndpoint.explosions.get(this.roomIndex).isEmpty()) {
            return;
        }
        
        // if the bot cannot drop new bombs and cannot detonate them, further searching is not required anymore
        if (this.plantedBombs >= this.maxBombs && !this.triggered){
            return;
        }
        
        // if the bot can detonate bombs, tigger them with a 100 ms delay
        if (this.plantedBombs > 0 && this.triggered){
            this.triggerBomb(this, 100);
            return;
        }
        
        // if a path is already computed, follow it ;))
        if (!this.path.isEmpty()) {
            this.followPath();
            return;
        }

        /**
         * If no danger is found by this point, and no route has been computed, check for the next best move
         */
        
        // current bot position
        int x = this.posX / World.wallDim;
        int y = this.posY / World.wallDim;
        
        // bounding box for the search range
        int xMin = Math.max(0, x - searchRange);
        int xMax = Math.min(map.get(this.roomIndex).getWidth() / World.wallDim-1, x + searchRange);
        int yMin = Math.max(0, y - searchRange);
        int yMax = Math.min(map.get(this.roomIndex).getHeight() / World.wallDim-1, y + searchRange);
        int j, i;
        
        /**
         * Map the surrounding blocks:
         *  0 - empty block
         *  1 - blowable wall
         * -1 - unblowable wall
         *  2 - positive item
         * -2 - negative item
         *  3 - bomb
         *  4 - character
         *  5 - the bot itself
         */
        
        int maxSize = 2 * this.searchRange + 1;
        
        // Comparator 
        BlockDistanceComparator comparator = new BlockDistanceComparator(this.posX, this.posY);
        
        int blocks[][] = new int[xMax - xMin + 1][yMax - yMin + 1];
        AbstractBlock blocks2[][] = new AbstractBlock[xMax - xMin + 1][yMax - yMin + 1];
        // adjacency list
        HashMap<AbstractBlock, Queue<SimpleEntry<AbstractBlock, String>>> neighbours = new HashMap<>();
        // detected items
        Queue<AbstractBlock> nearItems = new PriorityQueue<>(maxSize, comparator);
        // detected walls
        Queue<AbstractBlock> nearWalls = new PriorityQueue<>(maxSize, comparator);
        // detected chars
        Queue<AbstractBlock> nearChars = new PriorityQueue<>(maxSize, comparator);

        // initialize the blocks matrix
        for (i = xMin; i <= xMax; i++) {
            for (j = yMin; j <= yMax; j++) {
                AbstractBlock block = BombermanWSEndpoint.map.get(this.roomIndex).blockMatrix[i][j];
                if (block == null) {
                    block = new EmptyWall(i * World.wallDim, j * World.wallDim);
                }
                blocks2[i - xMin][j - yMin] = block;
                if (!neighbours.containsKey(block)) {
                    neighbours.put(block, new LinkedList<SimpleEntry<AbstractBlock, String>>());
                }

                HashMap<String, BCharacter>[][] chars = BombermanWSEndpoint.map.get(this.roomIndex).chars;
                if (chars[i][j] != null && !chars[i][j].isEmpty()) {
                    if (chars[i][j].size() == 1 && i == x && j == y) {
                        blocks[i - xMin][j - yMin] = 5; // myself (to be read with irish accent)
                    } else {
                        for (Map.Entry pairs : chars[i][j].entrySet()) {
                            BCharacter nearChar = (BCharacter) pairs.getValue();
                            if (!nearChar.getId().equals(this.getId())) {
                                nearChars.add(blocks2[i - xMin][j - yMin]);
                                blocks[i - xMin][j - yMin] = 4;
                                break;
                            }
                        }
                    }
                } else {
                    String type = BombermanWSEndpoint.checkWorldMatrix(this.roomIndex, i, j);
                    switch (type) {
                        case "wall":
                            if (((AbstractWall) block).isBlowable()) {
                                blocks[i - xMin][j - yMin] = 1; // blowable wall
                                nearWalls.add(blocks2[i - xMin][j - yMin]);
                            } else {
                                blocks[i - xMin][j - yMin] = -1; // unblowable wall...
                            }
                            break;
                        case "item":
                            String itemType = ((AbstractItem) block).getName();
                            switch (itemType) {
                                case "ebola":
                                case "slow":
                                    blocks[i - xMin][j - yMin] = -2; // items that we don't want
                                    break;
                                default:
                                    blocks[i - xMin][j - yMin] = 2;
                                    nearItems.add(blocks2[i - xMin][j - yMin]);
                                    break;
                            }
                            break;
                        case "bomb":
                            blocks[i - xMin][j - yMin] = 3;
                            break;
                        case "empty":
                        default:
                            blocks[i - xMin][j - yMin] = 0;
                            break;
                    }
                }

            }
        }

        // create the adjiacency list
        for (i = xMin; i <= xMax; i++) {
            for (j = yMin; j <= yMax; j++) {
                AbstractBlock block = blocks2[i - xMin][j - yMin];
                if (!neighbours.containsKey(block)) {
                    neighbours.put(block, new LinkedList<SimpleEntry<AbstractBlock, String>>());
                }

                if (i > xMin) { // left block
                    // check if the neighbour is an empty block or a character (even the bot itself)
                    if (blocks[i - 1 - xMin][j - yMin] == 5 || blocks[i - 1 - xMin][j - yMin] == 4 || blocks[i - 1 - xMin][j - yMin] == 0) {
                        AbstractBlock block2 = blocks2[i - 1 - xMin][j - yMin];
                        SimpleEntry<AbstractBlock, String> entry = new SimpleEntry(block2, "left");
                        if (!neighbours.get(block).contains(entry)) {
                            neighbours.get(block).add(entry);
                        }

                        if (blocks[i - xMin][j - yMin] == 5 || blocks[i - xMin][j - yMin] == 4 || blocks[i - xMin][j - yMin] == 0) {
                            // reversed link, opposite direction ;)
                            if (!neighbours.containsKey(block2)) {
                                neighbours.put(block2, new LinkedList<SimpleEntry<AbstractBlock, String>>());
                            }
                            SimpleEntry<AbstractBlock, String> entry2 = new SimpleEntry(block, "right");
                            if (!neighbours.get(block2).contains(entry2)) {
                                neighbours.get(block2).add(entry2);
                            }
                        }
                    }
                }

                if (i < xMax) { // right block
                    // check if the neighbour is an empty block or a character (even the bot itself)
                    if (blocks[i + 1 - xMin][j - yMin] == 5 || blocks[i + 1 - xMin][j - yMin] == 4 || blocks[i + 1 - xMin][j - yMin] == 0) {
                        AbstractBlock block2 = blocks2[i - xMin + 1][j - yMin];
                        SimpleEntry<AbstractBlock, String> entry = new SimpleEntry(block2, "right");
                        if (!neighbours.get(block).contains(entry)) {
                            neighbours.get(block).add(entry);
                        }

                        if (blocks[i - xMin][j - yMin] == 5 || blocks[i - xMin][j - yMin] == 4 || blocks[i - xMin][j - yMin] == 0) {
                            // reversed link, opposite direction ;)
                            if (!neighbours.containsKey(block2)) {
                                neighbours.put(block2, new LinkedList<SimpleEntry<AbstractBlock, String>>());
                            }
                            SimpleEntry<AbstractBlock, String> entry2 = new SimpleEntry(block, "left");
                            if (!neighbours.get(block2).contains(entry2)) {
                                neighbours.get(block2).add(entry2);
                            }
                        }
                    }
                }

                if (j > yMin) { // up block
                    // check if the neighbour is an empty block or a character (even the bot itself)
                    if (blocks[i - xMin][j - 1 - yMin] == 5 || blocks[i - xMin][j - 1 - yMin] == 4 || blocks[i - xMin][j - 1 - yMin] == 0) {
                        AbstractBlock block2 = blocks2[i - xMin][j - yMin - 1];
                        SimpleEntry<AbstractBlock, String> entry = new SimpleEntry(block2, "up");
                        if (!neighbours.get(block).contains(entry)) {
                            neighbours.get(block).add(entry);
                        }

                        if (blocks[i - xMin][j - yMin] == 5 || blocks[i - xMin][j - yMin] == 4 || blocks[i - xMin][j - yMin] == 0) {
                            // reversed link, opposite direction ;)
                            if (!neighbours.containsKey(block2)) {
                                neighbours.put(block2, new LinkedList<SimpleEntry<AbstractBlock, String>>());
                            }
                            SimpleEntry<AbstractBlock, String> entry2 = new SimpleEntry(block, "down");
                            if (!neighbours.get(block2).contains(entry2)) {
                                neighbours.get(block2).add(entry2);
                            }
                        }
                    }
                }

                if (j < yMax) { // down block
                    // check if the neighbour is an empty block or a character (even the bot itself)
                    if (blocks[i - xMin][j + 1 - yMin] == 5 || blocks[i - xMin][j + 1 - yMin] == 4 || blocks[i - xMin][j + 1 - yMin] == 0) {
                        AbstractBlock block2 = blocks2[i - xMin][j - yMin + 1];
                        SimpleEntry<AbstractBlock, String> entry = new SimpleEntry(block2, "down");
                        if (!neighbours.get(block).contains(entry)) {
                            neighbours.get(block).add(entry);
                        }

                        if (blocks[i - xMin][j - yMin] == 5 || blocks[i - xMin][j - yMin] == 4 || blocks[i - xMin][j - yMin] == 0) {
                            // reversed link, opposite direction ;)
                            if (!neighbours.containsKey(block2)) {
                                neighbours.put(block2, new LinkedList<SimpleEntry<AbstractBlock, String>>());
                            }
                            SimpleEntry<AbstractBlock, String> entry2 = new SimpleEntry(block, "up");
                            if (!neighbours.get(block2).contains(entry2)) {
                                neighbours.get(block2).add(entry2);
                            }
                        }
                    }
                }
            }
        }

        boolean pathFound = false;

        ArrayList<AbstractBlock> nearBlocks = new ArrayList<>();
        if (!nearItems.isEmpty()) nearBlocks.addAll(nearItems);
        if (!nearChars.isEmpty()) nearBlocks.addAll(nearChars);
        if (!nearWalls.isEmpty()) nearBlocks.addAll(nearWalls);
        
        if (!nearBlocks.isEmpty()){
            for (AbstractBlock item : nearBlocks) {
                boolean foundNeighbour = false;
                AbstractBlock crtBlock;
                int x2 = -1,
                        y2 = -1;
                Queue<SimpleEntry<AbstractBlock, String>> neighbs = neighbours.get(item);
                if (neighbs != null) {
                    while (!neighbs.isEmpty()) {
                        crtBlock = neighbs.poll().getKey();
                        x2 = crtBlock.getPosX() / World.wallDim;
                        y2 = crtBlock.getPosY() / World.wallDim;

                        if (blocks[x2 - xMin][y2 - yMin] == 5 || blocks[x2 - xMin][y2 - yMin] == 4 || blocks[x2 - xMin][y2 - yMin] == 0) {
                            foundNeighbour = true;
                            break;
                        }
                    }
                }
                if (foundNeighbour) { // found a block
                    ArrayList<String> directions = META_Dijkstra(blocks2[x - xMin][y - yMin], blocks2[x2 - xMin][y2 - yMin], neighbours);
                    if (directions != null && directions.size() > 0) { // path found
                        this.path.clear();
                        for (String pathDirection : directions) {
                            if (!pathDirection.equals("self")) {
                                pathFound = true;
                                this.path.add(pathDirection);
                            }
                        }
                        break;
                    }
                }
            }
        }

        // if no decision is made by this point
        if (pathFound) {
            //System.out.println("------------------");
            this.followPath();
        } else { // if no suitable path is found, simply make a random move
            this.moveRandom();
            this.path.clear();
        }
        
    }

    /**
     * Method used to detonate bombs with a given delay
     * @param bot - the bot that is detonating bombs (inside of a new thread, "this" has different scope)
     * @param delay - time to wait until actual detonation
     */
    public void triggerBomb(final BBaseBot bot, final int delay){
        new Thread(new Runnable() {

            @Override
            public void run() {
                try{
                    Thread.sleep(delay);
                }
                catch (InterruptedException ex){
                    BLogger.getInstance().logException2(ex);
                }
                BombermanWSEndpoint.getInstance().detonateBomb(bot);
            }
        }).start();
    }
    
    public void followPath(){
        if (!this.path.isEmpty()) this.move(this.path.poll());
    }
    
    /**
     * This method uses the Dijkstra Algorithm to find the shortest path between any two given vertices,<br />
     * with a simple observation : <br />
     * all distances have the same length (i.e. all edges have the same weight), meaning that any existing<br />
     * edge is guaranteed to assure the shortest path between the two neighbour vertices it connects :>
     * @param source - the source node for the required path
     * @param dest   - the destination node for the required path
     * @param neighbours - the list of neighbours
     * @return The required path (if any), or NULL if such a path does not exist
     */
    public ArrayList<String> META_Dijkstra(AbstractBlock source, AbstractBlock dest, HashMap<AbstractBlock, Queue<SimpleEntry<AbstractBlock, String>>> neighbours){
        HashMap<String, ArrayList<String>> road = new HashMap<>();
        ArrayList<AbstractBlock> vizited = new ArrayList<>();
        int x = source.getPosX()/World.wallDim,
            y = source.getPosY()/World.wallDim;
        
        if (neighbours.containsKey(source)/* && neighbours.containsKey(dest)*/){
            Queue<SimpleEntry<AbstractBlock, String>> queue = new LinkedList<>();
            queue.add(new SimpleEntry(source, "self")); // add the first block (source)
            vizited.add(source);
            SimpleEntry<AbstractBlock, String> crtBlock;
            while (!queue.isEmpty()){
                
                // return the first found path (because we use BFS graph search, the first found path is also the shortest)
                if (vizited.contains(dest)){ // stop computing any other routes
                    //System.out.println("found");
                    String key = x+"_"+y+"_"+dest.getPosX()/World.wallDim+"_"+dest.getPosY()/World.wallDim;
                    return road.get(key);
                }
                
                crtBlock = queue.poll();
                
                // will be used to store the distance from source to the current node
                String key = source.getPosX()/World.wallDim+"_"+source.getPosY()/World.wallDim+"_"+crtBlock.getKey().getPosX()/World.wallDim+"_"+crtBlock.getKey().getPosY()/World.wallDim;
                if(!road.containsKey(key)){
                    road.put(key, new ArrayList<String>());
                    //System.out.println("key : "+key);
                }
                else{
                    //System.out.println("key[d] : "+key);
                }
                
                Queue<SimpleEntry<AbstractBlock, String>> neighbs = neighbours.get(crtBlock.getKey());
                if (neighbs != null){
                    while (!neighbs.isEmpty()){
                        // for the current node, get all of it's neighbours
                        SimpleEntry<AbstractBlock, String> neighb = neighbs.poll();
                        if (!vizited.contains(neighb.getKey())){
                            queue.add(neighb);
                            vizited.add(neighb.getKey());
                            // will be used to store the distance to the current node to is's neighbours
                            String key2 = crtBlock.getKey().getPosX()/World.wallDim+"_"+crtBlock.getKey().getPosY()/World.wallDim + "_" + neighb.getKey().getPosX()/World.wallDim+"_"+neighb.getKey().getPosY()/World.wallDim;
                            if (!road.containsKey(key2)){
                                road.put(key2, new ArrayList<String>());
                                //System.out.println("key2 : "+key2);
                            }
                            else{
                                //System.out.println("key2[d] : "+key2);
                            }
                            
                            road.get(key2).add(neighb.getValue());
                            
                            // will be used to store the distance from the source node to the current's node neighbours
                            String key3 = source.getPosX()/World.wallDim+"_"+source.getPosY()/World.wallDim+"_"+neighb.getKey().getPosX()/World.wallDim+"_"+neighb.getKey().getPosY()/World.wallDim;
                            if (!key2.equals(key3)){
                                if (!road.containsKey(key3)){
                                    road.put(key3, new ArrayList<String>());
                                    //System.out.println("key3 : "+key3);
                                }
                                else{
                                    //System.out.println("key3[d] : "+key3);
                                }
                                
                                road.get(key3).addAll(road.get(key));
                                road.get(key3).addAll(road.get(key2));
//                                road.get(key3).add(neighb.getValue());
                            }
                        }
                    }
                }
            }
            
        }
        else{ // unreachable destination
            return null;
        }
        if (vizited.contains(dest)){
            String key = x+"_"+y+"_"+dest.getPosX()/World.wallDim+"_"+dest.getPosY()/World.wallDim;
            return road.get(key);
        }
        return null; // no route found
    }
    
    @Override
    public void run() {
        while (this.running) {
            try {
                if (!this.walking){
                    this.searchAndDestroy();
                }
                Thread.sleep(100); // limit medium bot action to 10 FPS
            } catch (InterruptedException ex) {
                BLogger.getInstance().logException2(ex);
            }
        }
    }
    
    @Override
    public String getDescription() {
        return "BMediumBot ";
    }
    
}
