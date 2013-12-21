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
public class SlowItem extends AbstractItem{

    public SlowItem(int posX, int posY) {
        super("slow");
        this.setLifeTime(10);
        this.setScale(3);
        this.texture = "item-slow.PNG";
        this.setPosX(posX);
        this.setPosY(posY);
    }
    
}
