/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.cux.bomberman.world.walls;

/**
 *
 * @author root
 */
public class StoneWall extends AbstractWall{

    public StoneWall(int posX, int posY) {
        super("stone");
        this.texture = "stone.gif";
        this.setPosX(posX);
        this.setPosY(posY);
        this.makeUnblowable();
    }
    
}
