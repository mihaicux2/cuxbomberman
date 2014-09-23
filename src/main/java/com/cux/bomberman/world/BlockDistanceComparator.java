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
     * @param x the X coordinate of the root node
     * @param y the Y coordinate of the root node
     */
    public BlockDistanceComparator(int x, int y){
        center = new BrickWall(x, y);
    }
    
    /**
     * Public method used to compare the distances between a known block (center) to other two given blocks
     * @param o1 The first block
     * @param o2 The second block
     * @return 0 if the distances are equal. -1 if the distance to o2 is the smaller one. >1 in any other case
     */
    @Override
    public int compare(AbstractBlock o1, AbstractBlock o2) {        
        double dist1 = center.distance(o1);
        double dist2 = center.distance(o2);
        double diff = dist1 - dist2;
        return diff == 0 ? 0 : (diff > 0) ? 1 : -1;
    }
    
}
