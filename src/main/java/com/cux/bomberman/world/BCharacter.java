/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.cux.bomberman.world;

import com.cux.bomberman.world.walls.AbstractWall;
import java.io.IOException;
import java.util.HashMap;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;

/**
 *
 * @author root
 */
public class BCharacter {
    
    private int posX = 0;
    protected int posY = 0;
    protected int width = 20;
    protected int height = 30;
    protected String name;
    private HashMap<String, Integer> textures = new HashMap<String, Integer>(); // direction+state, texture = int(.gif)
    public int crtTexture = 2; // can also be {1, 3, 4, 14, 15, 16, 17, 20, 22, 23, 24}
    private String state = "Normal"; // can also be "Bomb", "Blow", "Win" and "Trapped"
    private String direction = "Right"; // can also be "Up", "Down" and "Left"
    private String id;
    
    {
        // walk in normal state
        textures.put("walkUpNormal", 1);
        textures.put("walkRightNormal", 2);
        textures.put("walkDownNormal", 3);
        textures.put("walkLeftNormal", 4);
        
        // walk with bomb
        textures.put("walkUpBomb", 14);
        textures.put("walkRightBomb", 16);
        textures.put("walkDownBomb", 17);
        textures.put("walkLeftBomb", 15);
        
        // walk while trapped
        textures.put("walkUpTrapped", 20);
        textures.put("walkRightTrapped", 24);
        textures.put("walkDownTrapped", 22);
        textures.put("walkLeftTrapped", 23);
        
         // walk while blow
        textures.put("walkUpBlow", 18);
        textures.put("walkRightBlow", 18);
        textures.put("walkDownBlow", 18);
        textures.put("walkLeftBlow", 18);
        
        // walk while win
        textures.put("walkUpWin", 10);
        textures.put("walkRightWin", 10);
        textures.put("walkDownWin", 10);
        textures.put("walkLeftWin", 10);
    }
    
    public BCharacter(String id){
        this.id = id;
        this.name = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public int getPosX() {
        return posX;
    }

    public int getPosY() {
        return posY;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
    
    public void setPosX(int posX) {
        this.posX = posX;
    }

    public void setPosY(int posY) {
        this.posY = posY;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setHeight(int height) {
        this.height = height;
    }
    
    public int addOrDropBomb(){
        if (state == "Normal") state = "Bomb";
        else if (state == "Bomb") state = "Normal";
        return 0;
    }
    
    public int makeTrapped(){
        state = "Trapped";
        return 0;
    }
    
    public int makeFree(){
        state = "Normal";
        return 0;
    }

    public int getCrtTexture() {
        return crtTexture;
    }

    public void setCrtTexture(int crtTexture) {
        this.crtTexture = crtTexture;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }
    
    public void moveUp(){
        this.posY--;
    }
    
    public void moveDown(){
        this.posY++;
    }
    
    public void moveLeft(){
        this.posX--;
    }
    
    public void moveRight(){
        this.posX++;
    }
    
    public boolean hits(AbstractWall brick){
        return (this.posX + this.width >= brick.getPosX() && this.posX <= brick.getPosX() + brick.getWidth() &&
                this.posY + this.height >= brick.getPosY() && this.posY <= brick.getPosY() + brick.getHeight());
    }
    
    public void stepBack(AbstractWall brick){
        
        if (this.direction == "Right") this.posX--;
        if (this.direction == "Left") this.posX++;
        if (this.direction == "Down") this.posY--;
        if (this.direction == "Up") this.posY++;
        
//        if (this.posX + this.width > brick.getPosX()) this.posX++;
//        else this.posX--;
//        
//        if (this.posY + this.height > brick.getPosY()) this.posY++;
//        else this.posY--;
    }
    
    public boolean walksTo(AbstractWall brick){
        if (this.direction == "Right" && this.posX + this.width <= brick.getPosX()) return true;
        if (this.direction == "Left" && this.posX >= brick.getPosX() + brick.getWidth()) return true;
        if (this.direction == "Down" && this.posY +this.height <= brick.getPosY()) return true;
        if (this.direction == "Up" && this.posY >= brick.getPosY() + brick.getHeight()) return true;
        return false;
    }
    
    @Override
    public String toString(){
        this.crtTexture = textures.get("walk"+this.direction+this.state);
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        try {
            return ow.writeValueAsString(this);
        } catch (IOException ex) {
           // Logger.getLogger(AbstractWall.class.getName()).log(Level.SEVERE, null, ex);
            return ex.getMessage();
           // return "";
        }
    }
    
}
