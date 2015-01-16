package com.cux.bomberman.world.generator;

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
 * This class uses random to generate random items
 * 
 * @version 1.0
 * @author  Mihail Cuculici (mihai.cuculici@gmail.com)
 * @author  http://www.
 */
public class ItemGenerator {
    
    /**
     * The only allowed instance of the ItemGenerator class
     */
    private static ItemGenerator instance = null;
    
    /**
     * Private constructor to disallow direct instantiation
     */
    private ItemGenerator(){}
    
    /**
     * Overwritten method to disallow cloning of the instantiated object. [Singleton pattern]
     * @return NULL
     * @throws CloneNotSupportedException 
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }
    
    /**
     * Public static method used to implement the Singleton pattern. Only one instance of this class is allowed
     * @return The only allowed instance of this class
     */
    public static synchronized ItemGenerator getInstance(){
        if (instance == null){
            instance = new ItemGenerator();
        }
        return instance;
    }
    
    /**
     * Public method used to generate an item (random, any of the defined items)
     * @param wW The World width
     * @param wH The World height
     * @return The generated item
     */
    public AbstractItem generateRandomItem(int wW, int wH){
        int rand = (int) (Math.random()*1000000);
        Random r = new Random();
        int initialX = r.nextInt(wW);
        int initialY = r.nextInt(wH);
        
        AbstractItem ret;
        
        if      (rand % 7 == 0) ret = new RandomItem(initialX, initialY);
        else if (rand % 6 == 0) ret = new EbolaItem(initialX, initialY);
        else if (rand % 5 == 0) ret = new SpoogItem(initialX, initialY);
        else if (rand % 4 == 0) ret = new TriggerItem(initialX, initialY);
        else if (rand % 3 == 0) ret = new SkateItem(initialX, initialY);
        else if (rand % 2 == 0) ret = new SlowItem(initialX, initialY);
        else                    ret = new FlameItem(initialX, initialY);
        
        int lastX = initialX + ret.getWidth();
        int lastY = initialY + ret.getHeight();
        
        if (lastX > wW) ret.setPosX(wW - ret.getWidth());
        if (lastY > wH) ret.setPosY(wH - ret.getHeight());
        
        return ret;
        
    }
    
}
