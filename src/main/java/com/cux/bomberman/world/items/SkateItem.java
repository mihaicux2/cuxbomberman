/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.cux.bomberman.world.items;

/**
 *
 * @author root
 */
public class SkateItem extends AbstractItem{

    public SkateItem(int posX, int posY) {
        super("skate");
        this.setTimed(true);
        this.setLifeTime(5);
        this.texture = "item-skate.PNG";
        this.setPosX(posX);
        this.setPosY(posY);
    }
    
}
