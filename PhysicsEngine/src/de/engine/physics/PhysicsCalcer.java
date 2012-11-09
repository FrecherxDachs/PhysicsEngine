package de.engine.physics;

import de.engine.colldetect.CollisionData;
import de.engine.colldetect.ContactCreator;
import de.engine.environment.EnvProps;
import de.engine.math.Util;
import de.engine.math.Vector;
import de.engine.objects.Circle;
import de.engine.objects.Polygon;

public class PhysicsCalcer
{
    
    public static void calcCircles(Circle c1, Circle c2, double collTime)
    {
        Vector dist = Util.minus(c2.getPosition(), c1.getPosition());
        
        double alpha = Util.scalarProduct(c1.velocity, dist) / Util.scalarProduct(dist, dist);
        Vector c1_vp = Util.scale(dist, alpha);
        Vector c1_vs = Util.minus(c1.velocity, c1_vp);
        
        double beta = Util.scalarProduct(c2.velocity, dist) / Util.scalarProduct(dist, dist);
        Vector c2_vp = Util.scale(dist, beta);
        Vector c2_vs = Util.minus(c2.velocity, c2_vp);
        
        double afterCollTime = EnvProps.deltaTime() - collTime;
        
        c1.update(collTime);
        c2.update(collTime);
        if (c1.mass == c2.mass)
        {
            c1.velocity = c1_vs.add(c2_vp);
            c2.velocity = c2_vs.add(c1_vp);
        }
        else
        {
            Vector u = Util.scale(c1_vp, c1.mass).add(Util.scale(c2_vp, c2.mass)).scale(2 / (c1.mass + c2.mass));
            c1.velocity = Util.add(Util.minus(u, c1_vp), c1_vs);
            c2.velocity = Util.add(Util.minus(u, c2_vp), c2_vs);
        }
        c1.update(afterCollTime);
        c2.update(afterCollTime);
    }
    
    public static void calcCirclePolygon(Circle o1, Polygon o2, double collTime) {
        CollisionData cd = ContactCreator.getCirclePolygonContact(o1, o2, collTime);
        
    }

    public static void calcPolygons(Polygon o1, Polygon o2, double collTime) {
        
    }
}
