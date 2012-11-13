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
        grid.calcCollisionPairs();
        Vector<Integer> collisions = grid.getNextCollisions();
        while (collisions != null)
        {
            Vector<Integer> ids = new Vector<>();
            for (int i = 0; i < collisions.size(); i++)
            {
                ObjectProperties o1 = grid.scene.getObject(grid.collisionPairs.get(i)[0]);
                ObjectProperties o2 = grid.scene.getObject(grid.collisionPairs.get(i)[1]);
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
                if (!ids.contains(o1.getId()))
                    ids.add(o1.getId());
                if (!ids.contains(o2.getId()))
                    ids.add(o2.getId());
            }
            for (Integer id : ids)
            {
                ObjectProperties op = scene.getObject(id);
                op.update(op.next_time);
                grid.update(id);
            }
            collisions = grid.getNextCollisions();
        }
        
        // Tests the collision between objects and ground
        if (scene.existGround())
            objectGroundCollision();
    }
    
    public void objectGroundCollision()
    {
        long time = System.currentTimeMillis();
        
        for (ObjectProperties object : scene.getObjects())
        {
            if (scene.getCount() > 0 && object != null && scene.getGround() != null)
            {
                Ground ground = scene.getGround();
                Double xn = Util.newtonIteration(object, ground);
                
                object.last_intersection.setX(xn);
                object.last_intersection.setY(ground.function(ground.ACTUAL_FUNCTION, xn.intValue()));
                
                int x = (int) object.last_intersection.getX();
                int y = (int) object.last_intersection.getY();
                
                // calc distance by pythagoras
                int c = (int) Math.sqrt(Math.pow(x - object.getPosition().getX(), 2d) + Math.pow(y - object.getPosition().getY(), 2d));
                
                if (c < object.getRadius())
                {
                    object.velocity.setX(0);
                    object.velocity.setY(0);
                }
            }
        }
        
        System.out.println(System.currentTimeMillis() - time + " ms / " + "SP: [ " + (int) scene.getObject(0).last_intersection.getX() + ", " + (int) scene.getObject(0).last_intersection.getY() + " ]");
    }
}
