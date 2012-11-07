package de.engine.colldetect;

import java.util.Vector;

import de.engine.environment.Scene;
import de.engine.math.Util;
import de.engine.objects.Circle;
import de.engine.objects.Ground;
import de.engine.objects.ObjectProperties;
import de.engine.objects.Polygon;
import de.engine.physics.PhysicsCalcer;

public class CollisionDetector
{
    
    private Grid grid;
    private Scene scene;
    de.engine.math.Vector v = null;
    
    public CollisionDetector(Scene scene)
    {
        v = new de.engine.math.Vector();
        v = v.setUnitVector(v);
        
        grid = new Grid(scene);
        this.scene = scene;
    }
    
    public void checkScene()
    {
        grid.scanScene();
        Vector<Integer[]> collPairs = grid.getCollisionPairs();
        for (int i = 0; i < collPairs.size(); i++)
        {
            ObjectProperties o1 = grid.scene.getObject(collPairs.get(i)[0]);
            ObjectProperties o2 = grid.scene.getObject(collPairs.get(i)[1]);
            double coll_time = CollisionTimer.getCollTime(o1, o2, grid.coll_times.get(i)[0], grid.coll_times.get(i)[1]);
            if (-1 != coll_time)
            {
                if (o1 instanceof Circle && o2 instanceof Circle)
                {
                    PhysicsCalcer.calcCircles((Circle) o1, (Circle) o2, coll_time);
                }
                else if (o1 instanceof Circle && o2 instanceof Polygon)
                {
                    PhysicsCalcer.calcCirclePolygon((Circle) o1, (Polygon) o2, coll_time);
                }
                else if (o1 instanceof Polygon && o2 instanceof Circle)
                {
                    PhysicsCalcer.calcCirclePolygon((Circle) o2, (Polygon) o1, coll_time);
                }
                else if (o1 instanceof Polygon && o2 instanceof Polygon)
                {
                    PhysicsCalcer.calcPolygons((Polygon) o2, (Polygon) o1, coll_time);
                }
            }
        }
        
        objectGroundCollision();
    }
    
    
    public void objectGroundCollision() 
    {
        if (scene.getCount()>0 && scene.getObject(0)!=null && scene.getGround()!=null)
        {
            Ground ground = scene.getGround();
            
            long time = System.currentTimeMillis();

            v = Util.solveNonLEQ( scene.getObject(0), ground );
            scene.getObject(0).last_intersection.setX( v.get(0) );
            scene.getObject(0).last_intersection.setY( ground.function( ground.ACTUAL_FUNCTION, v.get(0).intValue() ));
            
            System.out.println( System.currentTimeMillis() - time +" ms / "+ "SP: = "+ scene.getObject(0).last_intersection.getX()+", "+scene.getObject(0).last_intersection.getY());
        }
    }
}
