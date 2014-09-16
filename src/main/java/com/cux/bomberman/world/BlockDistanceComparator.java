/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.cux.bomberman.world;


import com.cux.bomberman.world.walls.BrickWall;
import java.util.Comparator;

/**
 * This class is used in addition to a PriorityQueue, to store blocks depending on their distance<br />
 * to a given root node (closest node is first)
 * @author mihaicux
 */
public class BlockDistanceComparator  implements Comparator<AbstractBlock>{

    private AbstractBlock center;
    
    /**
     * Public constructor
     * @param x - the X coordinate of the root node
     * @param y - the Y coordinate of the root node
     */
    public BlockDistanceComparator(int x, int y){
        center = new BrickWall(x, y);
    }
    
    @Override
    public int compare(AbstractBlock o1, AbstractBlock o2) {        
        double dist1 = center.distance(o1);
        double dist2 = center.distance(o2);
        double diff = dist1 - dist2;
        return diff == 0 ? 0 : (diff > 0) ? 1 : -1;
    }
    
}
