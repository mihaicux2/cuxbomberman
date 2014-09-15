/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.cux.bomberman.world;

import java.util.Comparator;

/**
 * This class is used in addition to a PriorityQueue, to store blocks depending on their distance<br />
 * to a given root node (closest node is first)
 * @author mihaicux
 */
public class BlockDistanceComparator  implements Comparator<AbstractBlock>{

    private int centerX, centerY;
    
    /**
     * Public constructor
     * @param x - the X coordinate of the root node
     * @param y - the Y coordinate of the root node
     */
    public BlockDistanceComparator(int x, int y){
        this.centerX = x;
        this.centerY = y;
    }
    
    @Override
    public int compare(AbstractBlock o1, AbstractBlock o2) {
        double dist1 = Math.sqrt(Math.pow(Math.abs(centerX - o1.posX), 2) + Math.pow(Math.abs(centerY - o1.posY), 2));
        double dist2 = Math.sqrt(Math.pow(Math.abs(centerX - o2.posX), 2) + Math.pow(Math.abs(centerY - o2.posY), 2));
        if (dist1 < dist2) return 1;
        if (dist1 == dist2) return 0;
        return -1;
    }
    
}
