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
public class SteelWall extends AbstractWall{

    public SteelWall(int posX, int posY) {
        super("steel");
        this.texture = "steel.png";
        this.setHeight(30);
        this.setWidth(30);
        this.setPosX(posX);
        this.setPosY(posY);
        this.makeUnblowable();
    }
    
}
