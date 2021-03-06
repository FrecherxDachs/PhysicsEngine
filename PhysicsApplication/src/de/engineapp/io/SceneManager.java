package de.engineapp.io;

import java.awt.*;
import java.io.*;
import java.util.zip.*;

import javax.swing.*;

import de.engine.environment.*;
import de.engine.math.*;
import de.engine.objects.ObjectProperties;
import de.engine.objects.Material;
import de.engineapp.PresentationModel;
import de.engineapp.io.xml.*;
import de.engineapp.rec.Recorder;
import de.engineapp.util.Localizer;
import de.engineapp.visual.*;
import de.engineapp.visual.Polygon;

import static de.engineapp.Constants.*;


/**
 * Class that manages the load and save of scenes and recorded animations.
 * 
 * @author Micha
 */
public final class SceneManager
{
    private final static Localizer LOCALIZER = Localizer.getInstance();
    
    private PresentationModel pModel;
    private Component topLevelComponent;
    
    
    /**
     * Creates a new instance of the <code>SceneManager</code>
     * 
     * @param model - application PresentationModel
     * @param topLevelComponent - related component for displaying model dialogs
     */
    public SceneManager(PresentationModel model, Component topLevelComponent)
    {
        pModel = model;
        this.topLevelComponent = topLevelComponent;
    }
    
    
    /**
     * Loads a scene from file.
     * 
     * @param file - file to be laoded from
     * @return - loaded scene
     */
    public Scene loadScene(File file)
    {
        if (file.exists() && file.isFile())
        {
            if (isXmlFile(file))
            {
                try
                {
                    FileInputStream stream = new FileInputStream(file);
                    Scene scene = loadSceneFromStream(stream);
                    stream.close();
                    
                    return scene;
                }
                catch (IOException ex)
                {
                    ex.printStackTrace();
                }
            }
            else
            {
                try
                {
                    ZipFile zipFile = new ZipFile(file);
                    ZipEntry entry = zipFile.getEntry("document.xml");
                    
                    if (entry != null)
                    {
                        InputStream stream = zipFile.getInputStream(entry);
                        Scene scene = loadSceneFromStream(stream);
                        stream.close();
                        zipFile.close();
                        
                        return scene;
                    }
                    zipFile.close();
                }
                catch (IOException ex)
                {
                    ex.printStackTrace();
                }
            }
        }
        
        return null;
    }
    
    
    /**
     * Loads an animation from a file.
     * 
     * @param file - file to be loaded from
     * @return - loaded animation
     */
    public Recorder loadAnimation(File file)
    {
        if (file.exists() && file.isFile())
        {
            try
            {
                ZipFile zipFile = new ZipFile(file);
                
                Recorder recorder = Recorder.newInstance();
                
                for (int i = 0; i < zipFile.size(); i++)
                {
                    ZipEntry entry = zipFile.getEntry((i + 1) + ".xml");
                    
                    if (entry == null)
                    {
                        zipFile.close();
                        
                        return null;
                    }
                    
                    InputStream stream = zipFile.getInputStream(entry);
                    
                    if (!loadAnimationFromStream(stream, recorder))
                    {
                        stream.close();
                        zipFile.close();
                        
                        return null;
                    }
                    
                    stream.close();
                }
                zipFile.close();
                
                return recorder;
                
            }
            catch (IOException ex)
            {
                ex.printStackTrace();
            }
        }
        
        return null;
    }
    
    
    /**
     * Saves a scene into a file.
     * 
     * @param file - file be written to
     * @param scene - scene to be saved
     */
    public void saveScene(File file, Scene scene)
    {
        try
        {
            FileOutputStream fileStream = new FileOutputStream(file);
            ZipOutputStream zipStream = new ZipOutputStream(fileStream);
            
            ZipEntry entry = new ZipEntry("document.xml");
            zipStream.putNextEntry(entry);
            
            saveSceneToStream(zipStream, scene);
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
        }
    }
    
    
    /**
     * Saves an animation into a file.
     * 
     * @param file - file to be written to
     * @param recorder - animation to be saved
     */
    public void saveAnimation(File file, Recorder recorder)
    {
        try
        {
            FileOutputStream fileStream = new FileOutputStream(file);
            ZipOutputStream zipStream = new ZipOutputStream(fileStream);
            
            int maxFramesPerDocument = 30;
            
            int documentCount = (recorder.getFrameCount() + maxFramesPerDocument - 1) / maxFramesPerDocument;
            
            for (int i = 0; i < documentCount; i++)
            {
                int currentFrame = i * maxFramesPerDocument;
                int frameCount = Math.min(recorder.getFrameCount() - currentFrame, maxFramesPerDocument);
                
                ZipEntry entry = new ZipEntry((i + 1) + ".xml");
                zipStream.putNextEntry(entry);
                
                saveAnimationToStream(zipStream, recorder, currentFrame, frameCount);
                
                zipStream.closeEntry();
            }
            
            zipStream.close();
            
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
        }
    }
    
    
    /**
     * Checks, wether the specified file as an XML-file.
     * 
     * @param file - file to be checked
     * @return - true, if file is an XML-file
     */
    private static boolean isXmlFile(File file)
    {
        try
        {
            FileInputStream fis = new FileInputStream(file);
            InputStreamReader reader = new InputStreamReader(fis, "UTF-8");
            
            char[] buffer = new char[5];
            reader.read(buffer);
            reader.close();
            
            return buffer[0] == '<' && buffer[1] == '?' && buffer[2] == 'x' && buffer[3] == 'm' && buffer[4] == 'l';
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
        
        return false;
    }
    
    
    /**
     * Loads a scene from stream.
     * 
     * @param stream - stream that contains a scene
     * @return - loaded scene
     */
    private Scene loadSceneFromStream(InputStream stream)
    {
        XMLReader reader = new XMLReader(stream);
        
        Element node = reader.getNode("Scene");
        
        if (node != null)
        {
            String version = node.getAttribute("version");
            
            if (!FILE_VERSION.equals(version))
            {
                String[] options = new String[] { LOCALIZER.getString(L_YES), LOCALIZER.getString(L_NO) };
                
                if (JOptionPane.showOptionDialog(topLevelComponent, LOCALIZER.getString(L_WRONG_VERSION), 
                        LOCALIZER.getString(L_TITLE_IMPORT), JOptionPane.YES_NO_OPTION, 
                        JOptionPane.WARNING_MESSAGE, null, options, options[0]) != 0)
                {
                    return null;
                }
            }
            
            Scene scene = new Scene();
            
            scene.gravitational_acceleration = getDouble(node.getAttribute("gravity"), 
                                                         EnvProps.getInstance().default_gravitational_acceleration);
            
            ObjectProperties object = null;
            node = reader.getNode("Scene/Ground");
            
            if (node != null)
            {
                scene.setGround(new Ground(pModel, getInt(node.getAttribute("type"),      DEFAULT_GROUND), 
                                                   getInt(node.getAttribute("watermark"), 0)));
            }
            
            for (Element obj : reader.getNodes("Scene/Circle | Scene/Square | Scene/Polygon"))
            {
                switch (obj.getName())
                {
                    case "Circle":
                        object = new Circle(pModel, new Vector(getDouble(obj.getAttribute("x"), 0), 
                                                               getDouble(obj.getAttribute("y"), 0)), 
                                getDouble(obj.getAttribute("radius"), 0));
                        break;
                        
                    case "Polygon":
                        Polygon polygon = new Polygon(pModel, new Vector(getDouble(obj.getAttribute("x"), 0), 
                                                               getDouble(obj.getAttribute("y"), 0)), 
                                getDouble(obj.getAttribute("radius"), 0));
                        
                        java.util.List<Element> points = obj.getNodes("Point");
                        Vector[] pointList = new Vector[points.size()];
                        for (int i = 0; i < points.size(); i++)
                        {
                            double x = getDouble(points.get(i).getAttribute("x"), polygon.points[i].getX());
                            double y = getDouble(points.get(i).getAttribute("y"), polygon.points[i].getX());
                            pointList[i] = new Vector(x, y);
                        }
                        polygon.points = pointList;
                        object = polygon;
                        break;
                        
                    case "Square":
                        object = new Polygon(pModel, new Vector(getDouble(obj.getAttribute("x"), 0), 
                                getDouble(obj.getAttribute("y"), 0)), 
                                getDouble(obj.getAttribute("radius"), 0));
                        break;
                        
                    default:
                        continue;
                }
                
                ((ISelectable) object).setName(obj.getAttribute("name"));
                ((IDrawable) object).setColor(new Color(getInt(obj.getAttribute("color"), Color.RED.getRGB())));
                object.setMass(getDouble(obj.getAttribute("mass"), 10));
                object.velocity = new Vector(getDouble(obj.getAttribute("vx"), 0), 
                                             getDouble(obj.getAttribute("vy"), 0));
                object.setRotationAngle(getDouble(obj.getAttribute("rotation"), 0));
                
                String strMat = obj.getAttribute("material");
                for (Material mat : Material.values())
                {
                    if (mat.toString().equals(strMat))
                    {
                        object.surface = mat;
                        break;
                    }
                }
                
                object.isPinned = "true".equals(obj.getAttribute("pinned"));
                
                scene.add(object);
            }
            
            return scene;
        }
        
        return null;
    }
    
    
    /**
     * Loads an animation from stream.
     * 
     * @param stream - stream that contains an animation
     * @param recorder - animated scene container
     * @return
     */
    private boolean loadAnimationFromStream(InputStream stream, Recorder recorder)
    {
        XMLReader reader = new XMLReader(stream);
        
        Element node = reader.getNode("Animation");
        
        if (node != null)
        {
            String version = node.getAttribute("version");
            
            if (!FILE_VERSION.equals(version))
            {
                String[] options = new String[] { LOCALIZER.getString(L_YES), LOCALIZER.getString(L_NO) };
                
                if (JOptionPane.showOptionDialog(topLevelComponent, LOCALIZER.getString(L_WRONG_VERSION), 
                        LOCALIZER.getString(L_TITLE_IMPORT), JOptionPane.YES_NO_OPTION, 
                        JOptionPane.WARNING_MESSAGE, null, options, options[0]) != 0)
                {
                    return false;
                }
            }
            
            for (Element sceneFrame : node.getNodes("Scene"))
            {
                Scene scene = new Scene();
                
                scene.gravitational_acceleration = getDouble(sceneFrame.getAttribute("gravity"), 
                                                             EnvProps.getInstance().default_gravitational_acceleration);
                
                ObjectProperties object = null;
                node = sceneFrame.getNode("Ground");
                
                if (node != null)
                {
                    scene.setGround(new Ground(pModel, getInt(node.getAttribute("type"), DEFAULT_GROUND), 
                                                       getInt(node.getAttribute("watermark"), 0)));
                }
                
                for (Element obj : sceneFrame.getNodes("Circle | Square | Polygon"))
                {
                    switch (obj.getName())
                    {
                        case "Circle":
                            object = new Circle(pModel, new Vector(getDouble(obj.getAttribute("x"), 0), 
                                                                   getDouble(obj.getAttribute("y"), 0)), 
                                    getDouble(obj.getAttribute("radius"), 10));
                            break;
                            
                        case "Polygon":
                            Polygon polygon = new Polygon(pModel, new Vector(getDouble(obj.getAttribute("x"), 0), 
                                                                   getDouble(obj.getAttribute("y"), 0)), 
                                    getDouble(obj.getAttribute("radius"), 0));
                            
                            java.util.List<Element> points = obj.getNodes("Point");
                            Vector[] pointList = new Vector[points.size()];
                            for (int i = 0; i < points.size(); i++)
                            {
                                double x = getDouble(points.get(i).getAttribute("x"), polygon.points[i].getX());
                                double y = getDouble(points.get(i).getAttribute("y"), polygon.points[i].getX());
                                pointList[i] = new Vector(x, y);
                            }
                            polygon.points = pointList;
                            object = polygon;
                            break;
                            
                        case "Square":
                            object = new Polygon(pModel, new Vector(getDouble(obj.getAttribute("x"), 0), 
                                                                   getDouble(obj.getAttribute("y"), 0)), 
                                    getDouble(obj.getAttribute("radius"), 10));
                            break;
                            
                        default:
                            continue;
                    }
                    
                    ((ISelectable) object).setName(obj.getAttribute("name"));
                    ((IDrawable) object).setColor(new Color(getInt(obj.getAttribute("color"), Color.red.getRGB())));
                    object.setMass(getDouble(obj.getAttribute("mass"), 10));
                    object.velocity = new Vector(getDouble(obj.getAttribute("vx"), 0), 
                                                 getDouble(obj.getAttribute("vy"), 0));
                    object.setRotationAngle(getDouble(obj.getAttribute("rotation"), 0));
                    
                    String strMat = obj.getAttribute("material");
                    for (Material mat : Material.values())
                    {
                        if (mat.toString().equals(strMat))
                        {
                            object.surface = mat;
                            break;
                        }
                    }
                    
                    object.isPinned = "true".equals(obj.getAttribute("pinned"));
                    
                    scene.add(object);
                }
                
                recorder.addFrame(scene);
            }
            
            return true;
        }
        
        return false;
    }
    
    
    /**
     * Saves a scene into a stream.
     * 
     * @param stream - stream to be written to
     * @param scene - scene to be saved
     */
    private void saveSceneToStream(OutputStream stream, Scene scene)
    {
        XMLWriter writer = new XMLWriter(stream);
        
        writer.writeDeclaration();
        writer.writeStartElement("Scene");
        writer.writeAttribute("version", FILE_VERSION);
        writer.writeAttribute("gravity", "" + scene.gravitational_acceleration);
        
        if (scene.getGround() != null)
        {
            writer.writeStartElement("Ground");
            writer.writeAttribute("type", "" + scene.getGround().getType());
            writer.writeAttribute("watermark", "" + scene.getGround().getWatermark());
            writer.writeEndElement();
        }
        
        for (ObjectProperties object : scene.getObjects())
        {
            if (object instanceof Circle)
            {
                writer.writeStartElement("Circle");
            }
            else if (object instanceof Polygon)
            {
                writer.writeStartElement("Polygon");
            }
            
            writer.writeAttribute("name", ((ISelectable) object).getName());
            writer.writeAttribute("color", "" + ((IDrawable) object).getColor().getRGB());
            writer.writeAttribute("material", "" + object.surface);
            writer.writeAttribute("x", "" + object.getPosition().getX());
            writer.writeAttribute("y", "" + object.getPosition().getY());
            writer.writeAttribute("vx", "" + object.velocity.getX());
            writer.writeAttribute("vy", "" + object.velocity.getY());
            writer.writeAttribute("mass", "" + object.getMass());
            writer.writeAttribute("radius", "" + object.getRadius());
            writer.writeAttribute("rotation", "" + object.getRotationAngle());
            writer.writeAttribute("pinned", object.isPinned ? "true" : "false");
            
            if (object instanceof Polygon)
            {
                for (Vector point : ((Polygon) object).points)
                {
                    writer.writeStartElement("Point");
                    writer.writeAttribute("x", "" + point.getX());
                    writer.writeAttribute("y", "" + point.getY());
                    writer.writeEndElement();
                }
            }
            
            writer.writeEndElement();
        }
        
        writer.writeEndElement();
        writer.close();
    }
    
    
    /**
     * Saves an animation into a stream. 
     * 
     * @param stream - strsam to be written to
     * @param recorder - animations to be saved
     * @param frameStart - first animation frame index
     * @param frameCount - last animation frame index
     */
    private void saveAnimationToStream(OutputStream stream, Recorder recorder, int frameStart, int frameCount)
    {
        XMLWriter writer = new XMLWriter(stream);
        
        writer.writeDeclaration();
        writer.writeStartElement("Animation");
        writer.writeAttribute("version", FILE_VERSION);
        
        for (int i = 0; i < frameCount; i++)
        {
            Scene scene = recorder.getFrame(frameStart + i);
            
            writer.writeStartElement("Scene");
            writer.writeAttribute("gravitation", "" + scene.gravitational_acceleration);
            
            if (scene.getGround() != null)
            {
                writer.writeStartElement("Ground");
                writer.writeAttribute("type", "" + scene.getGround().getType());
                writer.writeAttribute("watermark", "" + scene.getGround().getWatermark());
                writer.writeEndElement();
            }
            
            for (ObjectProperties object : scene.getObjects())
            {
                if (object instanceof Circle)
                {
                    writer.writeStartElement("Circle");
                }
                else if (object instanceof Polygon)
                {
                    writer.writeStartElement("Polygon");
                }
                
                writer.writeAttribute("name", ((ISelectable) object).getName());
                writer.writeAttribute("color", "" + ((IDrawable) object).getColor().getRGB());
                writer.writeAttribute("material", "" + object.surface);
                writer.writeAttribute("x", "" + object.getPosition().getX());
                writer.writeAttribute("y", "" + object.getPosition().getY());
                writer.writeAttribute("vx", "" + object.velocity.getX());
                writer.writeAttribute("vy", "" + object.velocity.getY());
                writer.writeAttribute("mass", "" + object.getMass());
                writer.writeAttribute("radius", "" + object.getRadius());
                writer.writeAttribute("rotation", "" + object.getRotationAngle());
                writer.writeAttribute("pinned", object.isPinned ? "true" : "false");
                
                if (object instanceof Polygon)
                {
                    for (Vector point : ((Polygon) object).points)
                    {
                        writer.writeStartElement("Point");
                        writer.writeAttribute("x", "" + point.getX());
                        writer.writeAttribute("y", "" + point.getY());
                        writer.writeEndElement();
                    }
                }
                
                writer.writeEndElement();
            }
            
            writer.writeEndElement();
        }
        
        writer.writeEndElement();
        
        writer.flush();
    }
    
    
    /**
     * Converts a String value to a double if possible, or uses a default value if not.
     * 
     * @param value - value to be converted
     * @param defaultValue - alternative value, if convertion fails
     * @return - converted value or defaultValue
     */
    private double getDouble(String value, double defaultValue)
    {
        if (value == null)
        {
            return defaultValue;
        }
        
        return Double.parseDouble(value);
    }
    
    
    /**
     * Converts a String value to a double if possible, or uses a default value if not.
     * 
     * @param value - value to be converted
     * @param defaultValue - alternative value, if convertion fails
     * @return - converted value or defaultValue
     */
    private int getInt(String value, int defaultValue)
    {
        if (value == null)
        {
            return defaultValue;
        }
        
        return Integer.parseInt(value);
    }
}