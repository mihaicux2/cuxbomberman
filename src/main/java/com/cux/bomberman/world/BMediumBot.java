/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.cux.bomberman.world;

import com.cux.bomberman.BombermanWSEndpoint;
//import static com.cux.bomberman.BombermanWSEndpoint.map;
import com.cux.bomberman.util.BLogger;
import com.cux.bomberman.world.items.AbstractItem;
import com.cux.bomberman.world.walls.AbstractWall;
import com.cux.bomberman.world.walls.EmptyWall;
//import java.util.AbstractMap;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
//import java.util.Map.Entry;
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
    
    public void move(String direction){
        switch (direction){
            case "left":
                moveLeft();
                break;
            case "down":
                moveDown();
                break;
            case "right":
                moveRight();
                break;
            case "up":
                moveUp();
                break;
        }
    }
    
    public boolean checkNextBlock(int x, int y, String direction){
        
        if (BombermanWSEndpoint.characterExists(this.roomIndex, x, y)){
            dropBomb();
            return false;
        }
        
        AbstractBlock block = BombermanWSEndpoint.map.get(this.roomIndex).blockMatrix[x][y];
        String type = BombermanWSEndpoint.checkWorldMatrix(this.roomIndex, x, y);
        boolean ret = true;
        switch (type){
            case "item": // try to grab the item, if it is positive
                String itemType = ((AbstractItem) block).getName();
                switch (itemType) {
                    case "ebola":
                    case "slow":
                        break;
                    default: // positive item, try to grab it
                        ret = false;
                        move(direction);
                        break;
                }
                break;
            case "char": // try to kill the bot
                ret = false;
                dropBomb();
                break;
            case "wall": // try to blow it, if it is blowable
                if (((AbstractWall)block).isBlowable()){
                    ret = false;
                    dropBomb();
                }
                break;
        }
        return ret;
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
        
        // if you sit on a bomb, ruuuunn!!!
        if (BombermanWSEndpoint.bombExists(BombermanWSEndpoint.map.get(this.roomIndex).blockMatrix, x, y)){
            checkFurther = false;
            Random r = new Random();
            int rand = r.nextInt(100000);
            if (rand % 4 == 0){
                avoidBomb("left", x, y);
            }
            else if (rand % 3 == 3){
                avoidBomb("down", x, y);
            }
            else if (rand % 2 == 3){
                avoidBomb("right", x, y);
            }
            else {
                avoidBomb("up", x, y);
            }
        }
        
        // check and try to avoid bombs (self-preservation instinct :> )
        while (checkFurther){
            
            if (!expandLeft && !expandRight && !expandUp && !expandDown){
                break;
            }
            
            // check for bombs in the left
            if (expandLeft && xLeft > 0 ){
                String checkLeft = BombermanWSEndpoint.checkWorldMatrix(this.roomIndex, xLeft, y);
                if (BombermanWSEndpoint.bombExists(BombermanWSEndpoint.map.get(this.roomIndex).blockMatrix, xLeft, y)){
                    expandLeft = false;
                    // if the bomb range raches the bot, must avoid explosion
                    if (BombermanWSEndpoint.getInstance().bombReaches((BBomb) BombermanWSEndpoint.map.get(this.roomIndex).blockMatrix[xLeft][y], x - xLeft)) {
                        this.avoidBomb("left", x, y);
                        checkFurther = false;
                        bombFound = true;
                    }
                }
                else if (checkLeft.equals("wall")){
                    expandLeft = false;
                }
                else{
                    xLeft--;
                }
            }
            else{
                expandLeft = false;
            }
            
            // check for bombs in the right
            if (checkFurther && expandRight && xRight < World.getWidth() / World.wallDim ){
                String checkRight = BombermanWSEndpoint.checkWorldMatrix(this.roomIndex, xRight, y);
                if (BombermanWSEndpoint.bombExists(BombermanWSEndpoint.map.get(this.roomIndex).blockMatrix, xRight, y)){
                    expandRight = false;
                    // if the bomb range raches the bot, must avoid explosion
                    if (BombermanWSEndpoint.getInstance().bombReaches((BBomb)BombermanWSEndpoint.map.get(this.roomIndex).blockMatrix[xRight][y], xRight - x)){
                        this.avoidBomb("right", x, y);
                        checkFurther = false;
                        bombFound = true;
                    }
                }
                else if (checkRight.equals("wall")){
                    expandRight = false;
                }
                else{
                    xRight++;
                }
            }
            else{
                expandRight = false;
            }
            
            // check for bombs up
            if (checkFurther && expandUp && yUp > 0 ){
                String checkUp = BombermanWSEndpoint.checkWorldMatrix(this.roomIndex, x, yUp);
                if (BombermanWSEndpoint.bombExists(BombermanWSEndpoint.map.get(this.roomIndex).blockMatrix, x, yUp)){
                    expandUp = false;
                    // if the bomb range raches the bot, must avoid explosion
                    if (BombermanWSEndpoint.getInstance().bombReaches((BBomb)BombermanWSEndpoint.map.get(this.roomIndex).blockMatrix[x][yUp], y - yUp)){
                        this.avoidBomb("up", x, y);
                        checkFurther = false;
                        bombFound = true;
                    }
                }
                else if (checkUp.equals("wall")){
                    expandUp = false;
                }
                else{
                    yUp--;
                }
            }
            else{
                expandUp = false;
            }
            
            // check for bombs down
            if (checkFurther && expandDown && yDown < World.getHeight() / World.wallDim ){
                String checkDown = BombermanWSEndpoint.checkWorldMatrix(this.roomIndex, x, yDown);
                if (BombermanWSEndpoint.bombExists(BombermanWSEndpoint.map.get(this.roomIndex).blockMatrix, x, yDown)){
                    expandDown = false;
                    // if the bomb range raches the bot, must avoid explosion
                    if (BombermanWSEndpoint.getInstance().bombReaches((BBomb) BombermanWSEndpoint.map.get(this.roomIndex).blockMatrix[x][yDown], yDown - y)) {
                        this.avoidBomb("down", x, y);
                        checkFurther = false;
                        bombFound = true;
                    }
                }
                else if (checkDown.equals("wall")){
                    expandDown = false;
                }
                else{
                    yDown++;
                }
            }
            else{
                expandDown = false;
            }
        }
                
        // if there is no danger, check for the best next move
        if (!bombFound){
            boolean panoramicView = true; // tells weather or not to check for further blocks
            // first, check close neighbours
            if (x > 0){ // check left
                panoramicView = checkNextBlock(x-1, y, "left");
            }
            
            if (x < World.getWidth() / World.wallDim){ // check right
                String type = BombermanWSEndpoint.checkWorldMatrix(this.roomIndex, x+1, y);
                panoramicView = checkNextBlock(x+1, y, "right");
            }
            
            if (y > 0){ // check up
                String type = BombermanWSEndpoint.checkWorldMatrix(this.roomIndex, x, y-1);
                panoramicView = checkNextBlock(x, y-1, "up");
            }
            
            if (y < World.getHeight() / World.wallDim){ // check down
                String type = BombermanWSEndpoint.checkWorldMatrix(this.roomIndex, x, y+1);
                panoramicView = checkNextBlock(x, y+1, "down");
            }
            
            if (!panoramicView) return; // if a decision is made at this point, it is useless to search for another
            
            int xMin = Math.max(0, x - searchRange);
            int xMax = Math.min(World.getWidth() / World.wallDim, x + searchRange);
            int yMin = Math.max(0, y - searchRange);
            int yMax = Math.min(World.getHeight() / World.wallDim, y + searchRange);
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
            int blocks[][]  = new int[xMax-xMin + 2][yMax - yMin +2];
            // adjacency list
            HashMap<AbstractBlock, Queue<SimpleEntry<AbstractBlock, String>>> neighbours = new HashMap<>();
            // detected items
            ArrayList<AbstractItem> nearItems = new ArrayList<>();
            // detected walls
            ArrayList<AbstractWall> nearWalls = new ArrayList<>();
            // detected chars
            ArrayList<BCharacter>   nearChars = new ArrayList<>();
            
            // preprocessing
            for (i = xMin; i <= xMax; i++){
                for (j = yMin; j <= yMax; j++){
                    AbstractBlock block = BombermanWSEndpoint.map.get(this.roomIndex).blockMatrix[i][j];
                    if (block == null){
                        block = new EmptyWall(i * World.wallDim, j * World.wallDim);
                    }
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
                            HashMap<String, BCharacter>[][] chars = BombermanWSEndpoint.map.get(this.roomIndex).chars;
                            if (chars[i][j] != null && !chars[i][j].isEmpty()) {
                                blocks[i-xMin][j-yMin] = 4;
                                Iterator it = chars[i][j].entrySet().iterator();
                                // add in the list only one character
                                Map.Entry pairs = (Map.Entry) it.next();
                                BCharacter nearChar = (BCharacter)pairs.getValue();
                                // obviousl
                                if (!nearChar.getId().equals(this.getId())){
                                    nearChars.add(nearChar);
                                }
                            }
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
                    if (block == null){
                        block = new EmptyWall(i * World.wallDim, j * World.wallDim);
                    }
                    if (!neighbours.containsKey(block)){
                        neighbours.put(block, new LinkedList<SimpleEntry<AbstractBlock, String>>());
                    }
                    if ( i - 1 - xMin > -1 ){ // left block
                        // item, char or empty
                        if (blocks[i-1-xMin][j-yMin] == 4 || /*blocks[i-1-xMin][j-yMin] == 2 ||*/ blocks[i-1-xMin][j-yMin] == 0){
                            AbstractBlock block2 = BombermanWSEndpoint.map.get(this.roomIndex).blockMatrix[i-1][j];
                            if (block2 == null){
                                neighbours.get(block).add(new SimpleEntry(new EmptyWall( (i - 1) * World.wallDim, j * World.wallDim), "left"));
                            }
                            else{
                                neighbours.get(block).add(new SimpleEntry(block2, "left"));
                            }
                        }
                    }
                    if (i + 1 <= xMax){ // right block
                        // item, char or empty
                        if (blocks[i+1-xMin][j-yMin] == 4 || /*blocks[i+1-xMin][j-yMin] == 2 ||*/ blocks[i+1-xMin][j-yMin] == 0){
                            AbstractBlock block2 = BombermanWSEndpoint.map.get(this.roomIndex).blockMatrix[i+1][j];
                            if (block2 == null){
                                neighbours.get(block).add(new SimpleEntry(new EmptyWall( (i + 1) * World.wallDim, j * World.wallDim), "right"));
                            }
                            else{
                                neighbours.get(block).add(new SimpleEntry(block2, "right"));
                            }
                        }
                    }
                    if ( j - 1 - yMin > -1){ // up block
                        // item, char or empty
                        if (blocks[i-xMin][j-1-yMin] == 4 || /*blocks[i-xMin][j-1-yMin] == 2 ||*/ blocks[i-xMin][j-1-yMin] == 0){
                            AbstractBlock block2 = BombermanWSEndpoint.map.get(this.roomIndex).blockMatrix[i][j-1];
                            if (block2 == null){
                                neighbours.get(block).add(new SimpleEntry(new EmptyWall(i * World.wallDim, (j - 1) * World.wallDim), "up"));
                            }
                            else{
                                neighbours.get(block).add(new SimpleEntry(block2, "up"));
                            }
                        }
                    }
                    if ( j + 1 <= yMax){ // down block
                        if (blocks[i-xMin][j+1-yMin] == 4 || /*blocks[i-xMin][j+1-yMin] == 2 ||*/ blocks[i-xMin][j+1-yMin] == 0){
                            AbstractBlock block2 = BombermanWSEndpoint.map.get(this.roomIndex).blockMatrix[i][j+1];
                            if (block2 == null){
                                neighbours.get(block).add(new SimpleEntry(new EmptyWall(i * World.wallDim, (j + 1) * World.wallDim), "down"));
                            }
                            else{
                                neighbours.get(block).add(new SimpleEntry(block2, "down"));
                            }
                        }
                    }
                }
            }
            
            AbstractBlock block = (AbstractBlock)this;
            neighbours.put(block, new LinkedList<SimpleEntry<AbstractBlock, String>>());
            if ( x - 1 - xMin > -1 ){ // left block
                // item, char or empty
                if (blocks[x-1-xMin][y-yMin] == 4 || /*blocks[x-1-xMin][y-yMin] == 2 ||*/ blocks[x-1-xMin][y-yMin] == 0){
                    AbstractBlock block2 = BombermanWSEndpoint.map.get(this.roomIndex).blockMatrix[x-1][y];
                    if (block2 == null){
                        neighbours.get(block).add(new SimpleEntry(new EmptyWall( (x - 1) * World.wallDim, y * World.wallDim), "left"));
                    }
                    else{
                        neighbours.get(block).add(new SimpleEntry(block2, "left"));
                    }
                }
            }
            if (x + 1 <= xMax) { // right block
                // item, char or empty
                if (blocks[x + 1 - xMin][y - yMin] == 4 || /*blocks[x + 1 - xMin][y - yMin] == 2 ||*/ blocks[x + 1 - xMin][y - yMin] == 0) {
                    AbstractBlock block2 = BombermanWSEndpoint.map.get(this.roomIndex).blockMatrix[x + 1][y];
                    if (block2 == null) {
                        neighbours.get(block).add(new SimpleEntry(new EmptyWall((x + 1) * World.wallDim, y * World.wallDim), "right"));
                    } else {
                        neighbours.get(block).add(new SimpleEntry(block2, "right"));
                    }
                }
            }
            if (y - 1 - yMin > -1) { // up block
                // item, char or empty
                if (blocks[x - xMin][y - 1 - yMin] == 4 || /*blocks[x - xMin][y - 1 - yMin] == 2 ||*/ blocks[x - xMin][y - 1 - yMin] == 0) {
                    AbstractBlock block2 = BombermanWSEndpoint.map.get(this.roomIndex).blockMatrix[x][y - 1];
                    if (block2 == null) {
                        neighbours.get(block).add(new SimpleEntry(new EmptyWall(x * World.wallDim, (y - 1) * World.wallDim), "up"));
                    } else {
                        neighbours.get(block).add(new SimpleEntry(block2, "up"));
                    }
                }
            }
            if (y + 1 <= yMax) { // down block
                if (blocks[x - xMin][y + 1 - yMin] == 4 || /*blocks[x - xMin][y + 1 - yMin] == 2 ||*/ blocks[x - xMin][y + 1 - yMin] == 0) {
                    AbstractBlock block2 = BombermanWSEndpoint.map.get(this.roomIndex).blockMatrix[x][y + 1];
                    if (block2 == null) {
                        neighbours.get(block).add(new SimpleEntry(new EmptyWall(x * World.wallDim, (y + 1) * World.wallDim), "down"));
                    } else {
                        neighbours.get(block).add(new SimpleEntry(block2, "down"));
                    }
                }
            }
            
            /*
            Iterator it = neighbours.entrySet().iterator();
            while (it.hasNext()){
                Map.Entry pairs = (Map.Entry) it.next();
                AbstractBlock block = (AbstractBlock)pairs.getKey();
                Queue blockNeighbours = (LinkedList)pairs.getValue();
                System.out.println("New block : "+"("+block.getPosX()/World.wallDim+", "+block.getPosY()/World.wallDim+")");
                while (!blockNeighbours.isEmpty()){
                    SimpleEntry neighbourPair = (SimpleEntry)blockNeighbours.poll();
                    AbstractBlock neightbour = (AbstractBlock)neighbourPair.getKey();
                    String direction = (String)neighbourPair.getValue();
                    System.out.println("("+block.getPosX()/World.wallDim+", "+block.getPosY()/World.wallDim+")" +" -- "+direction+" --"+ "("+neightbour.getPosX()/World.wallDim+", "+neightbour.getPosY()/World.wallDim+")");
                }
            }
            System.out.println("...........................");
            
            System.out.println("Items : "+nearItems.size());
            System.out.println("Chars : "+nearChars.size());
            System.out.println("Walls : "+nearWalls.size());
            */
            
            boolean directionFound = false;
            if (!nearItems.isEmpty()){
//                System.out.println("found near item...");
                for (AbstractItem item : nearItems){
                    ArrayList<String> directions = dijkstra((AbstractBlock)this, item, neighbours);
                    if (directions != null && directions.size() > 0){
                        System.out.println("am gasit item...");
                        for (String direction : directions){
                            System.out.println(direction);
                            if (!direction.equals("self")){
                                directionFound = true;
                                move(direction);
                                break;
                            }
                        }
                        break;
                    }
                }
            }
            
            if (!directionFound && !nearChars.isEmpty()){
                for (BCharacter crtChar : nearChars){
                    ArrayList<String> directions = dijkstra((AbstractBlock)this, (AbstractBlock)crtChar, neighbours);
                    if (directions != null && directions.size() > 0){
                        System.out.println("am gasit caracter...");
                        for (String direction : directions){
                            System.out.println(direction);
                            if (!direction.equals("self")){
                                directionFound = true;
                                move(direction);
                                break;
                            }
                        }
                        break;
                    }
                }
            }
            
            if (!directionFound && !nearWalls.isEmpty()){
                for (AbstractWall wall : nearWalls){
                    ArrayList<String> directions = dijkstra((AbstractBlock)this, (AbstractBlock)wall, neighbours);
                    if (directions != null && directions.size() > 0){
                        System.out.println("am gasit perete...");
                        for (String direction : directions){
                            System.out.println(direction);
                            if (!direction.equals("self")){
                                directionFound = true;
                                move(direction);
                                break;
                            }
                        }
                        break;
                    }
                }
            }
         
            if (1==0 && !directionFound){
                // a new bomb, maby? :D
                Random r = new Random();
                int rand = r.nextInt(100);
                if (rand % 5 == 0){
                    this.dropBomb();
                }
            }
            
        }
        
    }

    public ArrayList<String> dijkstra(AbstractBlock source, AbstractBlock dest, HashMap<AbstractBlock, Queue<SimpleEntry<AbstractBlock, String>>> neighbours){
        HashMap<String, ArrayList<String>> road = new HashMap<>();
        ArrayList<AbstractBlock> vizited = new ArrayList<>();
        int x = source.getPosX()/World.wallDim,
            y = source.getPosY()/World.wallDim;
        
        if (neighbours.containsKey(source) && neighbours.containsKey(dest)){
            Queue<SimpleEntry<AbstractBlock, String>> queue = new LinkedList<>();
            queue.add(new SimpleEntry(source, "self")); // add the first block (source)
            vizited.add(source);
            SimpleEntry<AbstractBlock, String> crtBlock = null;
            while (!queue.isEmpty()){
                crtBlock = queue.poll();
                // will be used to store the distance from source to the current node
                String key = source.getPosX()/World.wallDim+"_"+source.getPosY()/World.wallDim+"_"+crtBlock.getKey().getPosX()/World.wallDim+"_"+crtBlock.getKey().getPosY()/World.wallDim;
                
                Queue<SimpleEntry<AbstractBlock, String>> neighbs = neighbours.get(crtBlock.getKey());
                if (neighbs != null){
                    while (!neighbs.isEmpty()){
                        // for the current node, get all of it's neighbours
                        SimpleEntry<AbstractBlock, String> neighb = neighbs.poll();
                        if (!vizited.contains(neighb.getKey())){
                            queue.add(neighb);
                            vizited.add(neighb.getKey());
                            String key2 = crtBlock.getKey().getPosX()/World.wallDim+"_"+crtBlock.getKey().getPosY()/World.wallDim + "_" + neighb.getKey().getPosX()/World.wallDim+"_"+crtBlock.getKey().getPosY()/World.wallDim;
                            if (!road.containsKey(key2)){
                                road.put(key2, new ArrayList<String>());
                            }
                            road.get(key2).add(neighb.getValue());
                            if (/*!key.equals(key2) && */road.containsKey(key)){
                                String key3 = source.getPosX()/World.wallDim+"_"+source.getPosY()/World.wallDim+"_"+neighb.getKey().getPosX()/World.wallDim+"_"+neighb.getKey().getPosY()/World.wallDim;
                                road.put(key3, new ArrayList<String>());
                                road.get(key3).addAll(road.get(key));
//                                road.get(key3).addAll(road.get(key2));
                                road.get(key3).add(neighb.getValue());
                            }
                        }
                    }
                }
            }
            
//            Iterator it = road.entrySet().iterator();
//            while (it.hasNext()){
//                Map.Entry pairs = (Map.Entry) it.next();
//                String key = (String)pairs.getKey();
//                ArrayList<String> directions = (ArrayList)pairs.getValue();
//                if (directions.isEmpty()) continue;
//                System.out.println("New road : "+key);
//                for (String direction : directions){
//                    System.out.println(key+" : "+direction);
//                }
//                System.out.println("...........................");
//            }
//            System.out.println();
            
        }
        else{
//            System.out.println("There is no road");
            return null;
        }
        if (vizited.contains(dest)){
            String key = x+"_"+y+"_"+dest.getPosX()/World.wallDim+"_"+dest.getPosY()/World.wallDim;
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
