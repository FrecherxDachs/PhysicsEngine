package de.engine.colldetect;

import java.util.ArrayList;

import de.engine.colldetect.CollisionData.Contact;
import de.engine.math.Util;
import de.engine.math.Vector;
import de.engine.objects.Circle;
import de.engine.objects.Polygon;

public class ContactCreator
{
    public static void getCirclesContact(CollisionData cd)
    {
        //CollisionData cd = new CollisionData(o1, o2, time);
        
        Vector pos_o2 = cd.o2.getPosition(cd.time);
        Vector pos_o1 = cd.o1.getPosition(cd.time);
        Vector dist = Util.minus(pos_o2, pos_o1);
        Vector normal = dist.getNormalVector().getUnitVector();
        Vector coll_point = Util.add(pos_o1, dist.scale(cd.o1.getRadius() / dist.getLength()));
        cd.contacts.add(new Contact(coll_point, normal));
        
    }
    
    public static void getCirclePolygonContact(CollisionData cd)
    {
        //CollisionData cd = new CollisionData(o1, o2, time);
        
        Vector circle_pos = cd.o1.getPosition(cd.time);
        for (int i = 0; i < ((Polygon)cd.o2).points.length; i++)
        {
            int j = (i == (((Polygon)cd.o2).points.length - 1)) ? 0 : i + 1;
            Vector point_pos = ((Polygon)cd.o2).getWorldPointPos(i, cd.time);
            Vector edge_ray = Util.minus(((Polygon)cd.o2).getWorldPointPos(j, cd.time), point_pos);
            Vector pos_ray = edge_ray.getNormalVector().getUnitVector();
            
            Vector coll_point = Util.crossEdges(circle_pos, Util.scale(pos_ray, -1 * cd.o1.getRadius()), point_pos, edge_ray);
            if (coll_point != null)
            {
                cd.contacts.add(new Contact(coll_point, pos_ray));
            }
        }
    }
    
    public static void getPolygonsContact(CollisionData cd)
    {
        //CollisionData cd = new CollisionData(o1, o2, time);
        
        ArrayList<Contact> contacts = searchContact((Polygon)cd.o1, (Polygon)cd.o2, cd.time);
        for (Contact contact : contacts)
        {
            cd.contacts.add(contact);
        }
        contacts = searchContact((Polygon)cd.o1, (Polygon)cd.o2, cd.time);
        for (Contact contact : contacts)
        {
            cd.contacts.add(contact);
        }
    }
    
    private static ArrayList<Contact> searchContact(Polygon o1, Polygon o2, double time)
    {
        ArrayList<Contact> contacts = new ArrayList<>();
        for (int i = 0; i < o1.points.length; i++)
        {
            for (int j = 0; j < o2.points.length; j++)
            {
                int k = (j == (o2.points.length - 1)) ? 0 : j + 1;
                Vector corner_o1 = o1.getWorldPointPos(i, time);
                Vector corner_o2 = o2.getWorldPointPos(j, time);
                Vector dist = Util.minus(corner_o1, corner_o2);
                Vector ray_normal = o2.getWorldPointPos(k, time).minus(corner_o2).getNormalVector().getUnitVector();
                double distance = Util.scalarProduct(dist, ray_normal);
                if (distance < 0)
                {
                    contacts.add(new Contact(corner_o1, ray_normal));
                }
            }
        }
        return contacts;
    }
}
