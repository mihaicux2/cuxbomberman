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
public class EmptyWall extends AbstractWall{

    public EmptyWall(int posX, int posY) {
        super("empty");
        this.texture = "";
        this.setPosX(posX);
        this.setPosY(posY);
    }
    
}
