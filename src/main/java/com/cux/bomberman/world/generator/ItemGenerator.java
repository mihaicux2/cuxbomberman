/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.cux.bomberman.world.generator;

import com.cux.bomberman.world.World;
import com.cux.bomberman.world.items.AbstractItem;
import com.cux.bomberman.world.items.EbolaItem;
import com.cux.bomberman.world.items.FlameItem;
import com.cux.bomberman.world.items.RandomItem;
import com.cux.bomberman.world.items.SkateItem;
import com.cux.bomberman.world.items.SlowItem;
import com.cux.bomberman.world.items.SpoogItem;
import com.cux.bomberman.world.items.TriggerItem;
import java.util.Random;

/**
 *
 * @author root
 */
public class ItemGenerator {
    
    private static ItemGenerator instance = null;
    
    // constructor ce duce la evitarea instantierii directe
    private ItemGenerator(){}
    
   @Override
    // nu permite clonarea obiectului
    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }
    
    // evita instantierea mai multor obiecte de acest tip si in cazul thread-urilor
    public static synchronized ItemGenerator getInstance(){
        if (instance == null){
            instance = new ItemGenerator();
        }
        return instance;
    }
    
    public AbstractItem generateRandomItem(){
        int rand = (int) (Math.random()*1000000);
        int wW = World.getWidth();
        int wH = World.getHeight();
        Random r = new Random();
        int initialX = r.nextInt(wW);
        int initialY = r.nextInt(wH);
        
        AbstractItem ret = null;
        
        if      (rand % 7 == 0) ret = new RandomItem(initialX, initialY);
        if      (rand % 6 == 0) ret = new EbolaItem(initialX, initialY);
        else if (rand % 5 == 0) ret = new SpoogItem(initialX, initialY);
        else if (rand % 4 == 0) ret = new TriggerItem(initialX, initialY);
        else if (rand % 3 == 0) ret = new SkateItem(initialX, initialY);
        else if (rand % 2 == 0) ret = new SlowItem(initialX, initialY);
        else                    ret = new FlameItem(initialX, initialY);
        
        ret = new EbolaItem(initialX, initialY);
        
        int lastX = initialX + ret.getWidth();
        int lastY = initialY + ret.getHeight();
        
        if (lastX > wW) ret.setPosX(wW - ret.getWidth());
        if (lastY > wH) ret.setPosY(wH - ret.getHeight());
        
        return ret;
        
    }
    
}
