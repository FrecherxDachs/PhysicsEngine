package de.engine.colldetect;

import java.util.*;

import de.engine.DebugMonitor;
import de.engine.colldetect.Grid.CollPair;
import de.engine.environment.Scene;
import de.engine.math.*;
import de.engine.math.DistanceCalcer.*;
import de.engine.math.Vector;
import de.engine.objects.Circle;
import de.engine.objects.Ground;
import de.engine.objects.ObjectProperties;
import de.engine.objects.Polygon;
import de.engine.physics.PhysicsCalcer;

public class CollisionDetector
{
    
    private Grid grid;
    private Scene scene;
    private de.engine.math.Vector v = null;
    
    private DistanceCalcer distCalcer;
    
    public CollisionDetector(Scene scene)
    {
        v = new de.engine.math.Vector();
        v = v.setUnitVector(v);
        
        grid = new Grid(scene);
        this.scene = scene;
        
        distCalcer = new DistanceCalcer(0.1);
    }
    
    public void checkScene()
    {
        grid.scanScene();
        CollPair collPair = grid.getNextCollision();
        while (collPair != null)
        {
            if (null != collPair.coll_time)
            {
                PhysicsCalcer.run(collPair.obj1, collPair.obj2, collPair.coll_time);
                grid.update(collPair.obj1);
                grid.update(collPair.obj2);
            }
            collPair = grid.getNextCollision();
        }
        
        // Tests the collision between objects and ground
        if (scene.existGround())
        {
            objectGroundCollision();
//            objectGroundCollision2();
        }
        
    }
    
    public void objectGroundCollision()
    {
        long time = System.currentTimeMillis();
        
        for (ObjectProperties object : scene.getObjects())
        {
            if (scene.getCount() > 0 && object != null && scene.getGround() != null)
            {
                Ground ground = scene.getGround();
                Util.ground = ground;
                Util.object = object;
                Double xn = Util.newtonIteration();
                
                object.last_intersection.setX(xn);
                object.last_intersection.setY(ground.function(xn.intValue()));
                
                int x = (int) object.last_intersection.getX();
                int y = (int) object.last_intersection.getY();
                
                // calc distance by pythagoras
                double c = Math.sqrt( Math.pow(x - object.getPosition().getX(), 2d) + Math.pow(y - object.getPosition().getY(), 2d) );

                double angle = Math.atan( Util.used_m ) - Math.atan( Util.derive1Dr( xn ) );  
                           
//                System.out.println(Math.toDegrees( Math.atan2( object.velocity.getY(), object.velocity.getX() )));
//                System.out.println(Math.toDegrees( Math.atan2( Util.derive1Dr( xn ), 1 )));
//                
//                System.out.println( "x="+object.getPosition().getX()+", y="+Util.used_m*object.getPosition().getX()+" | SP="+ xn +" | m_g= "+ Util.used_m +" | m_f="+ Util.derive1Dr( xn ) +" | angle="+ Math.floor(180*angle*100d/Math.PI)/100d);
                
                // the faster the object, the ealier it must be stopped
                double corrFactor = 0.01*Math.sqrt( object.velocity.getX()*object.velocity.getX() + object.velocity.getY()*object.velocity.getY());

                if (c <= object.getRadius() + corrFactor )
                {
                    object.velocity.setX(0);
                    object.velocity.setY(0);
                    object.isPinned = true;
                }
            }
        }
        
        DebugMonitor.getInstance().updateMessage("groundColl", "" + (System.currentTimeMillis() - time));
        // System.out.println(System.currentTimeMillis() - time + " ms / " + "SP: [ " + (int) scene.getObject(0).last_intersection.getX() + ", " + (int) scene.getObject(0).last_intersection.getY() + " ]");
    }
    
    public void objectGroundCollision2()
    {
        long t = System.currentTimeMillis();
        IFunction func = new IFunction()
        {
            @Override
            public double function(double x)
            {
                return scene.getGround().function((int) x);
            }
        };
        
        for (ObjectProperties object : scene.getObjects())
        {
            if (object.isPinned)
                continue;
            // this helps to define an interval
            // double range = object.velocity.getX() * 2;
            double range = object.getRadius() * 5;
            de.engine.math.Vector nextPos = object.getNextPosition();
            
            distCalcer.setPoint(nextPos);
            distCalcer.setFunction(func);
            double dist = distCalcer.calculateDistanceBetweenFunctionPoint(nextPos.getX() - range, nextPos.getX() + range);
            
            object.closest_point = new de.engine.math.Vector(distCalcer.getLastSolvedX(), scene.getGround().function((int) distCalcer.getLastSolvedX()));
            
            if (dist < object.getRadius())
            {
                if (object instanceof Circle)
                {
                    object.isPinned = true;
                }
                else if (object instanceof Polygon)
                {
                    Polygon polygon = (Polygon) object;
                    List<Vector> rotatedPoints = new ArrayList<>();
                    
                    for (Vector point : polygon.points)
                    {
                        
                        rotatedPoints.add(Util.add(polygon.world_position.rotation.getMatrix().multVector(point),
                                polygon.getPosition()));
                    }
                    
                    int length = polygon.points.length;
                    
                    for (int i = 0; i < length; i++)
                    {
                        Vector p1 = rotatedPoints.get(i);
                        Vector p2 = rotatedPoints.get((i + 1)% length);
                        
                        if (p1.getX() == p2.getX())
                        {
                            continue;
                        }
                        
                        StraightLine line = new StraightLine(p1, p2);
                        
                        distCalcer.setStraightLine(line);
                        dist = distCalcer.findRootBetweenFunctionLine();
                        
                        if (dist <= 0.1)
                        {
                            double x = distCalcer.getLastSolvedX();
                            double y = line.function(x);
                            
                            polygon.closest_point = new Vector(x, y);
                            polygon.isPinned = true;
                            break;
                        }
                    }
                }
            }
        }
        DebugMonitor.getInstance().updateMessage("distCalc", "" + (System.currentTimeMillis() - t));
    }
}