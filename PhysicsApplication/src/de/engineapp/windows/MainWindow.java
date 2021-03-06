package de.engineapp.windows;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.*;

import javax.swing.*;

import de.engine.environment.Scene;
import de.engineapp.*;
import de.engineapp.PresentationModel.StorageListener;
import de.engineapp.containers.*;
import de.engineapp.controls.Canvas;
import de.engineapp.controls.dnd.DragAndDropController;
import de.engineapp.io.SceneManager;
import de.engineapp.util.*;
import de.engineapp.visual.Renderer;

import static de.engineapp.Constants.*;


/**
 * Main Application Window.
 * 
 * @author Micha
 */
public final class MainWindow extends JFrame implements StorageListener
{
    private static final long serialVersionUID = -1405279482198323306L;
    
    private final static Localizer LOCALIZER = Localizer.getInstance();
    
     
    private PresentationModel pModel = null;
    
    private Canvas canvas;
    private Renderer renderer;
    
    
    public MainWindow()
    {
        super(LOCALIZER.getString(L_APP_NAME));
        
        initializeLookAndFeel();
        
        pModel = new PresentationModel();
        pModel.setProperty(PRP_MODE, CMD_PHYSICS_MODE);
        
        Scene scene = null;
        
        // open a scene, if there is one assigned
        if (pModel.getProperty(PRP_CURRENT_FILE) != null)
        {
            SceneManager sceneManager = new SceneManager(pModel, this);
            File sceneFile = new File(pModel.getProperty(PRP_CURRENT_FILE));
            scene = sceneManager.loadScene(sceneFile);
            
            if (scene != null)
            {
                this.setTitle(LOCALIZER.getString(L_APP_NAME) +  " [" + sceneFile.getName() + "]");
            }
            else
            {
                pModel.setProperty(PRP_CURRENT_FILE, null);
            }
        }
        
        // sets the path to the used skin
        if (Configuration.getInstance().getProperty(PRP_SKIN) != null)
        {
            GuiUtil.setImageSourcePath(Configuration.getInstance().getProperty(PRP_SKIN));
        }
        
        // Free objects (if necessary) before this application ends
        this.addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosing(WindowEvent e)
            {
                pModel.getPhysicsState().pause();
                MainWindow.this.dispose();
                Configuration.save();
            }
        });
        // handle full screen and windows mode
        this.addWindowStateListener(new WindowStateListener()
        {
            @Override
            public void windowStateChanged(WindowEvent e)
            {
                if ((MainWindow.this.getExtendedState() & MAXIMIZED_BOTH) != 0)
                {
                    pModel.setState(STG_MAXIMIZED, true);
                }
                else
                {
                    pModel.setState(STG_MAXIMIZED, false);
                }
            }
        });
        
        initializeWindow();
        
        // create a new scene, if there is not already one loaded
        if (scene == null)
        {
            scene = new Scene();
        }
        
        pModel.setScene(scene);
        
        pModel.setPhysicsState(new PhysicsConnector(pModel, 1000L / 30L, new PhysicsConnector.FinishedCallback()
        {
            @Override
            public void done()
            {
                pModel.fireSceneUpdated();
                renderer.pushScene(pModel.getScene());
            }
        }));
        
        
        initializeComponents();
        
        renderer = new Renderer(pModel, canvas);
        
        pModel.addStorageListener(this);
        
        
        this.setVisible(true);
    }
    
    
    private void initializeLookAndFeel()
    {
        Configuration config = Configuration.getInstance();
        
        if (!LookAndFeelManager.applyLookAndFeelByName(config.getProperty(PRP_LOOK_AND_FEEL)))
        {
            LookAndFeelManager.applySystemLookAndFeel();
        }
        
        config.setProperty(PRP_LOOK_AND_FEEL, LookAndFeelManager.getCurrentLookAndFeelName());
    }
    
    
    private void initializeComponents()
    {
        // initiate controls
        canvas = new Canvas(pModel);
        MainToolBar toolBarMain = new MainToolBar(pModel);
        StatusBar statusBar = new StatusBar(pModel);
        ObjectToolBar toolBarObjects = new ObjectToolBar(pModel);
        PropertiesPanel panelProperties = new PropertiesPanel(pModel);
        
        
        // set up upper toolbar
        this.add(toolBarMain, BorderLayout.PAGE_START);
        
        
        // set up statusbar
        this.add(statusBar, BorderLayout.PAGE_END);
        
        
        // set up left toolbar, enabling drag'n'drop objects
        this.add(toolBarObjects, BorderLayout.LINE_START);
        
        
        // set up right panel
//        this.add(new JScrollPane(panelProperties), BorderLayout.LINE_END);
        this.add(panelProperties, BorderLayout.LINE_END);
        panelProperties.setVisible(false);
        
        
        // this one is handling all the drag'n'drop stuff
        new DragAndDropController(pModel, canvas);
        
        
        canvas.setBackground(new Color(250, 250, 250, 255));
        
        
        // set up canvas
        this.add(canvas);
    }
    
    
    private void initializeWindow()
    {
        this.setSize(800, 600);
        this.setLocationRelativeTo(null);
        
        ArrayList<Image> iconList = new ArrayList<>();
        iconList.add(GuiUtil.getImage(ICO_MAIN_256));
        iconList.add(GuiUtil.getImage(ICO_MAIN_128));
        iconList.add(GuiUtil.getImage(ICO_MAIN_64));
        iconList.add(GuiUtil.getImage(ICO_MAIN_48));
        iconList.add(GuiUtil.getImage(ICO_MAIN_32));
        iconList.add(GuiUtil.getImage(ICO_MAIN_24));
        iconList.add(GuiUtil.getImage(ICO_MAIN_16));
        
        this.setIconImages(iconList);
        
        if (pModel.isState(STG_MAXIMIZED))
        {
            this.setExtendedState(this.getExtendedState() | MAXIMIZED_BOTH);
        }
    }
    
    
    @Override
    public void stateChanged(String id, boolean value) { }
    
    @Override
    public void propertyChanged(String id, String value)
    {
        // handle window title (dependent on the opened file)
        if (id.equals(PRP_CURRENT_FILE))
        {
            if (value == null)
            {
                this.setTitle(LOCALIZER.getString(L_APP_NAME));
            }
            else
            {
                this.setTitle(LOCALIZER.getString(L_APP_NAME) +  " [" + new File(value).getName() + "]");
            }
        }
    }
}