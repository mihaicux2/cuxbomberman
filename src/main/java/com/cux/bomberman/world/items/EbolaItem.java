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
public class EbolaItem extends AbstractItem{

    public EbolaItem(int posX, int posY) {
        super("ebola");
        this.setLifeTime(10);
        this.setScale(10);
        this.texture = "item-ebola.PNG";
        this.setPosX(posX);
        this.setPosY(posY);
    }
    
}
