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
public class TriggerItem extends AbstractItem{

    public TriggerItem(int posX, int posY) {
        super("trigger");
        this.texture = "item-trig.PNG";
        this.setTimed(true);
        this.setLifeTime(5);
        this.setPosX(posX);
        this.setPosY(posY);
    }
    
}
