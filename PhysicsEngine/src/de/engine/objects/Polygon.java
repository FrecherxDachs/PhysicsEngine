package de.engine.objects;

import de.engine.environment.EnvProps;
import de.engine.math.Transformation;
import de.engine.math.Util;
import de.engine.math.Vector;

public class Polygon extends ObjectProperties implements Cloneable
{
    public Vector[] points;
    
    public Polygon(Vector position)
    {
        this.world_position = new Transformation(position, 0);
        radius = 0;
    }
    
    public Polygon(Vector position, double rotation)
    {
        this.world_position = new Transformation(position, rotation);
        radius = 0;
    }
    
    public Polygon(Vector position, Vector[] points)
    {
        this(position);
        this.points = points;
        calcRadius();
        calcMomentOfInertia();
    }
    
    protected void calcRadius()
    {
        radius = 0;
        for (Vector point : points)
        {
            double corner_dist = point.getLength();
            radius = (corner_dist > radius) ? corner_dist : radius;
        }
    }
    
    protected void calcMomentOfInertia()
    {
        double zaehler = 0;
        double nenner = 0;
        
        for (int j = points.length - 1, i = 0; i < points.length; j = i, i++)
        {
            Vector p0 = points[j];
            Vector p1 = points[i];
            
            double a = Util.crossProduct(p0, p1);
            double b = Util.scalarProduct(p1, p1) + Util.scalarProduct(p0, p1) + Util.scalarProduct(p0, p0);
            
            zaehler += (a * b);
            nenner += a;
        }
        moment_of_inertia = (mass / 6.0) * (zaehler / nenner);
    }
    
    public Vector getWorldPointPos(int i) {
        return world_position.getPostion(points[i]);
    }
    
    public Vector getWorldPointPos(int i, double time)
    {
    	if (isPinned)
    		return getWorldPointPos(i); 
        double localtime = getTime(time);
        Transformation tr = new Transformation(Util.add(world_position.translation, Util.scale(velocity, localtime)), world_position.rotation.getAngle() + angular_velocity * localtime);
        return tr.getPostion(points[i]);
    }
    
    @Override
    public double getRadius() {
        return radius;
    }
    
    @Override
    public void setRadius(double radius)
    {
        double scale = radius / getRadius();
        for (Vector point : points) {
            point.scale(scale);
        }
        this.radius = radius;
        calcMomentOfInertia();
    }
    
    
    @Override
    public Polygon clone()
    {
        return clone(true);
    }
    
    @Override
    public Polygon clone(boolean cloneId)
    {
        Polygon newPolygon = new Polygon(getPosition());
        clone(newPolygon);
        
        return newPolygon;
    }
    
    
    public void clone(Polygon newPolygon, boolean cloneId)
    {
        // TODO
        newPolygon.points = new Vector[this.points.length];
        
        for (int i = 0; i < this.points.length; i++)
        {
            newPolygon.points[i] = this.points[i].clone();
        }
        
        newPolygon.setRotationAngle(this.getRotationAngle());
        newPolygon.setMass(getMass());
        newPolygon.velocity = this.velocity.clone();
        if (cloneId)
        {
            newPolygon.id = this.id;
        }
        newPolygon.surface = this.surface;
        newPolygon.isPinned = this.isPinned;
        
    }
    
    public void clone(Polygon newPolygon)
    {
        clone(newPolygon, true);
    }
    
    
    @Override
    public boolean contains(double x, double y)
    {
        Vector pos = new Vector(x, y);
        for (int i = 0; i < points.length; i++)
        {
            int j = (i == points.length - 1) ? 0 : i + 1;
            boolean e1 = Util.crossProduct(Util.minus(pos, this.getWorldPointPos(i)), Util.minus(this.getPosition(), this.getWorldPointPos(i))) < 0.0;
            boolean e2 = Util.crossProduct(Util.minus(pos, this.getWorldPointPos(j)), Util.minus(this.getWorldPointPos(i), this.getWorldPointPos(j))) < 0.0;
            boolean e3 = Util.crossProduct(Util.minus(pos, this.getPosition()), Util.minus(this.getWorldPointPos(j), this.getPosition())) < 0.0;
            if(e1 == e2 && e2 == e3)
                return true;
        }
        return false;
    }
    
    @Override
    public Vector[] getAABB()
    {
        return getAABB(0);
    }
    
    @Override
    public Vector[] getAABB(double time)
    {
        Vector aabb[] = new Vector[2];
        aabb[0] = new Vector(Double.MAX_VALUE, Double.MAX_VALUE);
        aabb[1] = new Vector(-1 * Double.MAX_VALUE, -1 * Double.MAX_VALUE);
        for (int i = 0; i < points.length; i++)
        {
            Vector v;
            if(time == 0)
                v = getWorldPointPos(i);
            else
                v = getWorldPointPos(i, time);
            
            if(v.getX() > aabb[1].getX())
                aabb[1].setX(v.getX());
            
            if(v.getY() > aabb[1].getY())
                aabb[1].setY(v.getY());
            
            if(v.getX() < aabb[0].getX())
                aabb[0].setX(v.getX());
            
            if(v.getY() < aabb[0].getY())
                aabb[0].setY(v.getY());
        }
        return aabb;
    }
    
    @Override
    public Vector[] getNextAABB()
    {
        return getAABB(EnvProps.deltaTime());
    }
    
    @Override
    public void setMass(double mass)
    {
        moment_of_inertia *= (mass / this.mass);
        this.mass = mass;
    }
}