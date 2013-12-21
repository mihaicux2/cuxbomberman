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
public class RandomItem extends AbstractItem{

    public RandomItem(int posX, int posY) {
        super("random");
        this.texture = "item-random.PNG";
        this.scale = 1;
        this.setTimed(false);
        this.setPosX(posX);
        this.setPosY(posY);
    }
    
}
