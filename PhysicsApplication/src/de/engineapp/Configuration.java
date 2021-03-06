package de.engineapp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.*;
import java.util.Map.Entry;

import de.engineapp.PresentationModel.*;

import static de.engineapp.Constants.*;


/**
 * Handles the load and save of config files.
 * 
 * @author Micha
 */
public final class Configuration implements Serializable, Cloneable, StorageListener, ViewBoxListener
{
    private static final long serialVersionUID = 6847314270730904825L;
    
    
    private static Configuration instance = null;
    
    private double zoom;
    private Map<String, Boolean> states;
    private Map<String, String> properties;
    
    
    /**
     * Private Constructor to create a new Configuration.
     */
    private Configuration()
    {
        states = new HashMap<>();
        properties = new HashMap<>();
        
        // set default values
        zoom = 1.0;
        states.put(STG_GRID, false);
        states.put(STG_MAXIMIZED, false);
        states.put(STG_DBLCLICK_SHOW_PROPERTIES, false);
        states.put(STG_DEBUG, false);
        states.put(STG_SHOW_ARROWS_ALWAYS, false);
        properties.put(PRP_LANGUAGE_CODE, null);
        properties.put(PRP_LOOK_AND_FEEL, null);
        properties.put(PRP_CURRENT_FILE, null);
    }
    
    
    /**
     * Creates a new Configuration.
     * 
     * @return - new Configuration
     */
    public static Configuration getInstance()
    {
        if (instance == null)
        {
            instance = new Configuration();
        }
        
        return instance;
    }
    
    
    /**
     * Checks, wether a config file is within the application directory, if so, it will be loaded.
     */
    // HINT - will be propably replaced by some xml file
    public static void load()
    {
        File configFile = new File("config.dat");
        
        if (configFile.exists())
        {
            try (FileInputStream   fis = new FileInputStream(configFile);
                 ObjectInputStream ois = new ObjectInputStream(fis))
            {
                instance = (Configuration) ois.readObject();
            }
            catch (IOException | ClassNotFoundException ex)
            {
                ex.printStackTrace();
            }
            
        }
    }
    
    
    /**
     * Saves the current configurations to a config file.
     */
    public static void save()
    {
        try (FileOutputStream   fos = new FileOutputStream("config.dat");
             ObjectOutputStream oos = new ObjectOutputStream(fos))
        {
            oos.writeObject(getInstance());
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
        }
    }
    
    
    @Override
    public Configuration clone()
    {
        try
        {
            return (Configuration) super.clone();
        }
        catch (CloneNotSupportedException e)
        {
            e.printStackTrace();
        }
        return null;
    }
    
    
    /**
     * Replaces the current application Configuration by a new one.
     * 
     * @param newInstance - new application Configuration
     */
    public static void overrideInstance(Configuration newInstance)
    {
        instance = newInstance;
    }
    
    
    /**
     * Connects the PresentationModel to the Configuration, 
     * this is necessary to initialite the PresentationModel 
     * with parameters on start up and to save the last done 
     * actions within the config file on shut down.
     * 
     * @param model
     */
    public void attachPresentationModel(PresentationModel model)
    {
        model.addViewBoxListener(this);
        model.addStorageListener(this);
        
        model.setZoom(zoom);
        
        for (Entry<String, Boolean> entry : states.entrySet())
        {
            model.setState(entry.getKey(), entry.getValue());
        }
        for (Entry<String, String> entry : properties.entrySet())
        {
            model.setProperty(entry.getKey(), entry.getValue());
        }
    }
    
    
    /**
     * Checks, wether a state has been set.
     * (Check <code>Constants.java</code> for supported states.)
     * 
     * @param id - id of the state
     * @return - true, if the state is set
     */
    public boolean isState(String id)
    {
        return Boolean.TRUE.equals(states.get(id));
    }
    
    /**
     * Sets a state.
     * (Check <code>Constants.java</code> for supported states.)
     * 
     * @param id - id of the state
     * @param value - new boolean value of the state
     */
    public void setState(String id, boolean value)
    {
        states.put(id, value);
    }
    
    
    /**
     * Retrieves the string value of a property.
     * (Check <code>Constants.java</code> for supported properties.)
     * 
     * @param id - id of the property
     * @return - string value of the property
     */
    public String getProperty(String id)
    {
        return properties.get(id);
    }
    
    /**
     * Sets the string value of a property.
     * (Check <code>Constants.java</code> for supported properties.)
     * 
     * @param id - id of the property
     * @param value - new string value of the property
     */
    public void setProperty(String id, String value)
    {
        properties.put(id, value);
    }
    
    
    /**
     * Returns the current zoom.
     * 
     * @return - current zoom
     */
    public double getZoom()
    {
        return zoom;
    }
    
    /**
     * Sets the current zoom.
     * 
     * @param zoom - new zoom
     */
    public void setZoom(double zoom)
    {
        this.zoom = zoom;
    }
    
    
    @Override
    public void offsetChanged(int offsetX, int offsetY) { }
    
    @Override
    public void sizeChanged(int width, int height) { }
    
    @Override
    public void zoomChanged(double zoom)
    {
        this.zoom = zoom;
    }
    
    
    @Override
    public void stateChanged(String id, boolean value)
    {
        if (states.containsKey(id))
        {
            states.put(id, value);
        }
    }
    
    @Override
    public void propertyChanged(String id, String value)
    {
        if (properties.containsKey(id))
        {
            properties.put(id, value);
        }
    }
}