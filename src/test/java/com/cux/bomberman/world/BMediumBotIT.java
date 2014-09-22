/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cux.bomberman.world;

import com.cux.bomberman.BombermanWSEndpoint;
import static com.cux.bomberman.BombermanWSEndpoint.map;
import com.cux.bomberman.world.items.*;
import com.cux.bomberman.world.walls.*;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author mihaicux
 */
public class BMediumBotIT {
    
    public BMediumBotIT() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of META_Dijkstra method, of class BMediumBot.
     * 
     * The given map: (XY coordinates are inverted)
     *    0 1 2 3 4 5 6
     *   ---------------
     * 0 | |b|b|b|b|b|b|
     *   ---------------
     * 1 | | | | | |D|b|
     *   ---------------
     * 2 | |b|b| |b| |b|
     *   ---------------
     * 3 | |b|b|S|b| |b|
     *   ---------------
     * 4 | | | | |b|b|b|
     *   ---------------
     * 5 |b|b|b|b|b|b| |
     *   ---------------
     * 6 | | | | | | | |
     *   ---------------
     * 
     * We must find the road from the source node S to a neighbour of the destination node D (i.e. : from [3.3] to [4,1])
     * The road must be {"up", "up", "right"}
     */
    @Test
    public void testMETA_Dijkstra() {
        System.out.println("META_Dijkstra");
        
        // setup BOT
        int mapNumber = 1;
        
        BombermanWSEndpoint.map.put(mapNumber, new World("maps/map2.txt"));
        
        BMediumBot bot = new BMediumBot("1", "john", 1, null);  // ion :>
        bot.setPosX(3 * World.wallDim);
        bot.setPosY(3 * World.wallDim);
        bot.setWidth(World.wallDim);
        bot.setHeight(World.wallDim);
        bot.roomIndex = mapNumber;
        
        AbstractItem dest = new SkateItem(5 * World.wallDim, 1 * World.wallDim);
        
        int[][] blocks = new int[7][7];
        AbstractBlock[][] blocks2 = new AbstractBlock[7][7];
        
        // first row
        blocks[0][0] = 0;
        blocks[0][1] = 0;
        blocks[0][2] = 0;
        blocks[0][3] = 0;
        blocks[0][4] = 0;
        blocks[0][5] = 1;
        blocks[0][6] = 0;
        
        // second row
        blocks[1][0] = 1;
        blocks[1][1] = 0;
        blocks[1][2] = 1;
        blocks[1][3] = 1;
        blocks[1][4] = 0;
        blocks[1][5] = 1;
        blocks[1][6] = 0;
        
        // third row
        blocks[2][0] = 1;
        blocks[2][1] = 0;
        blocks[2][2] = 1;
        blocks[2][3] = 1;
        blocks[2][4] = 0;
        blocks[2][5] = 1;
        blocks[2][6] = 0;
        
        // fourth row
        blocks[3][0] = 1;
        blocks[3][1] = 0;
        blocks[3][2] = 0;
        blocks[3][3] = 5;
        blocks[3][4] = 0;
        blocks[3][5] = 1;
        blocks[3][6] = 0;
        
        // fifth row
        blocks[4][0] = 1;
        blocks[4][1] = 0;
        blocks[4][2] = 1;
        blocks[4][3] = 1;
        blocks[4][4] = 1;
        blocks[4][5] = 1;
        blocks[4][6] = 0;
        
        // sixth row
        blocks[5][0] = 1;
        blocks[5][1] = 2;
        blocks[5][2] = 0;
        blocks[5][3] = 0;
        blocks[5][4] = 1;
        blocks[5][5] = 1;
        blocks[5][6] = 0;
        
        // seventh row
        blocks[6][0] = 1;
        blocks[6][1] = 1;
        blocks[6][2] = 1;
        blocks[6][3] = 1;
        blocks[6][4] = 1;
        blocks[6][5] = 1;
        blocks[6][6] = 0;
        
        
        // first row
        blocks2[0][0] = new EmptyWall(0, 0);
        blocks2[0][1] = new EmptyWall(0, 1 * World.wallDim);
        blocks2[0][2] = new EmptyWall(0, 2 * World.wallDim);
        blocks2[0][3] = new EmptyWall(0, 3 * World.wallDim);
        blocks2[0][4] = new EmptyWall(0, 4 * World.wallDim);
        blocks2[0][5] = new BrickWall(0, 5 * World.wallDim);
        blocks2[0][6] = new EmptyWall(0, 6 * World.wallDim);
        
        // second row
        blocks2[1][0] = new BrickWall(1 * World.wallDim, 0);
        blocks2[1][1] = new EmptyWall(1 * World.wallDim, 1 * World.wallDim);
        blocks2[1][2] = new BrickWall(1 * World.wallDim, 2 * World.wallDim);
        blocks2[1][3] = new BrickWall(1 * World.wallDim, 3 * World.wallDim);
        blocks2[1][4] = new EmptyWall(1 * World.wallDim, 4 * World.wallDim);
        blocks2[1][5] = new BrickWall(1 * World.wallDim, 5 * World.wallDim);
        blocks2[1][6] = new EmptyWall(1 * World.wallDim, 6 * World.wallDim);
        
        // third row
        blocks2[2][0] = new BrickWall(2 * World.wallDim, 0);
        blocks2[2][1] = new EmptyWall(2 * World.wallDim, 1 * World.wallDim);
        blocks2[2][2] = new BrickWall(2 * World.wallDim, 2 * World.wallDim);
        blocks2[2][3] = new BrickWall(2 * World.wallDim, 3 * World.wallDim);
        blocks2[2][4] = new EmptyWall(2 * World.wallDim, 4 * World.wallDim);
        blocks2[2][5] = new BrickWall(2 * World.wallDim, 5 * World.wallDim);
        blocks2[2][6] = new EmptyWall(2 * World.wallDim, 6 * World.wallDim);
        
        // fourth row
        blocks2[3][0] = new BrickWall(3 * World.wallDim, 0);
        blocks2[3][1] = new EmptyWall(3 * World.wallDim, 1 * World.wallDim);
        blocks2[3][2] = new EmptyWall(3 * World.wallDim, 2 * World.wallDim);
        blocks2[3][3] = bot;
        blocks2[3][4] = new EmptyWall(3 * World.wallDim, 4 * World.wallDim);
        blocks2[3][5] = new BrickWall(3 * World.wallDim, 5 * World.wallDim);
        blocks2[3][6] = new EmptyWall(3 * World.wallDim, 6 * World.wallDim);
        
        // fifth row
        blocks2[4][0] = new BrickWall(4 * World.wallDim, 0);
        blocks2[4][1] = new EmptyWall(4 * World.wallDim, 1 * World.wallDim);
        blocks2[4][2] = new BrickWall(4 * World.wallDim, 2 * World.wallDim);
        blocks2[4][3] = new BrickWall(4 * World.wallDim, 3 * World.wallDim);
        blocks2[4][4] = new BrickWall(4 * World.wallDim, 4 * World.wallDim);
        blocks2[4][5] = new BrickWall(4 * World.wallDim, 5 * World.wallDim);
        blocks2[4][6] = new EmptyWall(4 * World.wallDim, 6 * World.wallDim);
        
        // sixth row
        blocks2[5][0] = new BrickWall(5 * World.wallDim, 0);
        blocks2[5][1] = dest;
        blocks2[5][2] = new EmptyWall(5 * World.wallDim, 2 * World.wallDim);
        blocks2[5][3] = new EmptyWall(5 * World.wallDim, 3 * World.wallDim);
        blocks2[5][4] = new BrickWall(5 * World.wallDim, 4 * World.wallDim);
        blocks2[5][5] = new BrickWall(5 * World.wallDim, 5 * World.wallDim);
        blocks2[5][6] = new EmptyWall(5 * World.wallDim, 6 * World.wallDim);
        
        // seventh row
        blocks2[6][0] = new BrickWall(6 * World.wallDim, 0);
        blocks2[6][1] = new BrickWall(6 * World.wallDim, 1 * World.wallDim);
        blocks2[6][2] = new BrickWall(6 * World.wallDim, 2 * World.wallDim);
        blocks2[6][3] = new BrickWall(6 * World.wallDim, 3 * World.wallDim);
        blocks2[6][4] = new BrickWall(6 * World.wallDim, 4 * World.wallDim);
        blocks2[6][5] = new EmptyWall(6 * World.wallDim, 5 * World.wallDim);
        blocks2[6][6] = new EmptyWall(6 * World.wallDim, 6 * World.wallDim);
        
        // create the adjiacency list
        HashMap<AbstractBlock, Queue<AbstractMap.SimpleEntry<AbstractBlock, String>>> neighbours = new HashMap<>();
        for (int i = 0; i <= 6; i++) {
            for (int j = 0; j <= 6; j++) {
                AbstractBlock block = blocks2[i][j];
                if (!neighbours.containsKey(block)) {
                    neighbours.put(block, new LinkedList<AbstractMap.SimpleEntry<AbstractBlock, String>>());
                }

                if (i > 0) { // left block
                    // check if the neighbour is an empty block or a character (even the bot itself)
                    if (blocks[i - 1][j] == 5 || blocks[i - 1][j] == 4 || blocks[i - 1][j] == 0) {
                        AbstractBlock block2 = blocks2[i - 1][j];
                        AbstractMap.SimpleEntry<AbstractBlock, String> entry = new AbstractMap.SimpleEntry(block2, "left");
                        if (!neighbours.get(block).contains(entry)) {
                            neighbours.get(block).add(entry);
                        }

                        if (blocks[i][j] == 5 || blocks[i][j] == 4 || blocks[i][j] == 0) {
                            // reversed link, opposite direction ;)
                            if (!neighbours.containsKey(block2)) {
                                neighbours.put(block2, new LinkedList<AbstractMap.SimpleEntry<AbstractBlock, String>>());
                            }
                            AbstractMap.SimpleEntry<AbstractBlock, String> entry2 = new AbstractMap.SimpleEntry(block, "right");
                            if (!neighbours.get(block2).contains(entry2)) {
                                neighbours.get(block2).add(entry2);
                            }
                        }
                    }
                }

                if (i < 6) { // right block
                    // check if the neighbour is an empty block or a character (even the bot itself)
                    if (blocks[i + 1][j] == 5 || blocks[i + 1][j] == 4 || blocks[i + 1][j] == 0) {
                        AbstractBlock block2 = blocks2[i + 1][j];
                        AbstractMap.SimpleEntry<AbstractBlock, String> entry = new AbstractMap.SimpleEntry(block2, "right");
                        if (!neighbours.get(block).contains(entry)) {
                            neighbours.get(block).add(entry);
                        }

                        if (blocks[i][j] == 5 || blocks[i][j] == 4 || blocks[i][j] == 0) {
                            // reversed link, opposite direction ;)
                            if (!neighbours.containsKey(block2)) {
                                neighbours.put(block2, new LinkedList<AbstractMap.SimpleEntry<AbstractBlock, String>>());
                            }
                            AbstractMap.SimpleEntry<AbstractBlock, String> entry2 = new AbstractMap.SimpleEntry(block, "left");
                            if (!neighbours.get(block2).contains(entry2)) {
                                neighbours.get(block2).add(entry2);
                            }
                        }
                    }
                }

                if (j > 0) { // up block
                    // check if the neighbour is an empty block or a character (even the bot itself)
                    if (blocks[i][j - 1] == 5 || blocks[i][j - 1] == 4 || blocks[i][j - 1] == 0) {
                        AbstractBlock block2 = blocks2[i][j - 1];
                        AbstractMap.SimpleEntry<AbstractBlock, String> entry = new AbstractMap.SimpleEntry(block2, "up");
                        if (!neighbours.get(block).contains(entry)) {
                            neighbours.get(block).add(entry);
                        }

                        if (blocks[i][j] == 5 || blocks[i][j] == 4 || blocks[i][j] == 0) {
                            // reversed link, opposite direction ;)
                            if (!neighbours.containsKey(block2)) {
                                neighbours.put(block2, new LinkedList<AbstractMap.SimpleEntry<AbstractBlock, String>>());
                            }
                            AbstractMap.SimpleEntry<AbstractBlock, String> entry2 = new AbstractMap.SimpleEntry(block, "down");
                            if (!neighbours.get(block2).contains(entry2)) {
                                neighbours.get(block2).add(entry2);
                            }
                        }
                    }
                }

                if (j < 6) { // down block
                    // check if the neighbour is an empty block or a character (even the bot itself)
                    if (blocks[i][j + 1] == 5 || blocks[i][j + 1] == 4 || blocks[i][j + 1] == 0) {
                        AbstractBlock block2 = blocks2[i][j + 1];
                        AbstractMap.SimpleEntry<AbstractBlock, String> entry = new AbstractMap.SimpleEntry(block2, "down");
                        if (!neighbours.get(block).contains(entry)) {
                            neighbours.get(block).add(entry);
                        }

                        if (blocks[i][j] == 5 || blocks[i][j] == 4 || blocks[i][j] == 0) {
                            // reversed link, opposite direction ;)
                            if (!neighbours.containsKey(block2)) {
                                neighbours.put(block2, new LinkedList<AbstractMap.SimpleEntry<AbstractBlock, String>>());
                            }
                            AbstractMap.SimpleEntry<AbstractBlock, String> entry2 = new AbstractMap.SimpleEntry(block, "up");
                            if (!neighbours.get(block2).contains(entry2)) {
                                neighbours.get(block2).add(entry2);
                            }
                        }
                    }
                }
            }
        }
        
        //writeNeighbours(blocks2, neighbours, 3, 3, new HashSet<AbstractBlock>());
        
        ArrayList<String> expResult = new ArrayList<String>();
        expResult.add("up");
        expResult.add("up");
        expResult.add("right");
        ArrayList<String> result = bot.META_Dijkstra(blocks2[3][3], blocks2[4][1], neighbours);
        if (result == null){
            //fail("No route found");
            System.out.println("No route found...");
        }
        else{
            for (String dir : result){
                System.out.println(dir);
            }
            assertEquals(expResult, result);
        }
    }
    
    public void writeNeighbours(AbstractBlock[][] blocks2, HashMap<AbstractBlock, Queue<AbstractMap.SimpleEntry<AbstractBlock, String>>> neighbours, int i, int j, HashSet<AbstractBlock> visited){
        System.out.println("Neighbours for "+i+","+j+" : ");
        visited.add(blocks2[i][j]);
        if (!neighbours.get(blocks2[i][j]).isEmpty()){
            for (AbstractMap.SimpleEntry<AbstractBlock, String> entry : neighbours.get(blocks2[i][j])){
                System.out.println(entry.getKey().getPosX() / World.wallDim + ", " + entry.getKey().getPosY() / World.wallDim + " : " + entry.getValue());
            }
            for (AbstractMap.SimpleEntry<AbstractBlock, String> entry : neighbours.get(blocks2[i][j])){
                if (!visited.contains(entry.getKey())){
                    writeNeighbours(blocks2, neighbours, entry.getKey().getPosX()/World.wallDim, entry.getKey().getPosY()/World.wallDim, visited);
                }
            }
        }
    }
}
