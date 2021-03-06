package de.engineapp.controls;

import java.awt.*;
import java.awt.dnd.*;
import java.awt.event.*;
import java.io.Serializable;

import javax.swing.*;

import de.engineapp.controls.dnd.CommandHandler;


/**
 * Simple button that provides an easy interface for Drag'n'Drop.
 * 
 * @author Micha
 */
public final class DragButton extends JToggleButton implements MouseMotionListener, DropTargetListener, Serializable
{
    private static final long serialVersionUID = 6946598301964868381L;
    
    
    public DragButton(ImageIcon buttonIcon, String command)
    {
        this(buttonIcon, command, null, null);
    }
    
    public DragButton(ImageIcon buttonIcon, String command, boolean useIconAsImage)
    {
        this(buttonIcon, command, useIconAsImage ? buttonIcon.getImage() : null);
    }
    
    public DragButton(ImageIcon buttonIcon, String command, Point dragImageOffset)
    {
        this(buttonIcon, command, buttonIcon.getImage(), dragImageOffset);
    }
    
    public DragButton(ImageIcon buttonIcon, String command, Image dragImage)
    {
        this(buttonIcon, command, dragImage, new Point(dragImage.getWidth(null) / 2, dragImage.getHeight(null) / 2));
    }
    
    public DragButton(ImageIcon buttonIcon, String command, Image dragImage, Point dragImageOffset)
    {
        super(buttonIcon);
        
        this.setFocusable(false);
        
        this.addMouseMotionListener(this);
        new DropTarget(this, this);
        
        if (dragImage == null)
        {
            new CommandHandler(this, command);
        }
        else
        {
            new CommandHandler(this, command, dragImage, dragImageOffset);
        }
    }
    
    
    @Override
    public void dragEnter(DropTargetDragEvent dtde) { }
    
    @Override
    public void dragExit(DropTargetEvent dte)
    {
        // fire a mouseExited event to the target component
        
        int absX = MouseInfo.getPointerInfo().getLocation().x;
        int absY = MouseInfo.getPointerInfo().getLocation().y;
        int x = absX - dte.getDropTargetContext().getComponent().getLocationOnScreen().x;
        int y = absY - dte.getDropTargetContext().getComponent().getLocationOnScreen().y;
        this.dispatchEvent(new MouseEvent(this, MouseEvent.MOUSE_EXITED, System.currentTimeMillis(), 
                MouseEvent.BUTTON1_DOWN_MASK, x, y, absX, absY, 1, false, MouseEvent.BUTTON1));
        this.dispatchEvent(new MouseEvent(this, MouseEvent.MOUSE_RELEASED, System.currentTimeMillis(), 
                MouseEvent.BUTTON1_DOWN_MASK, x, y, absX, absY, 1, false, MouseEvent.BUTTON1));
    }
    
    @Override
    public void dragOver(DropTargetDragEvent dtde) { }
    
    @Override
    public void drop(DropTargetDropEvent dtde) { }
    
    @Override
    public void dropActionChanged(DropTargetDragEvent dtde) { }
    
    @Override
    public void mouseDragged(MouseEvent e)
    {
        if (SwingUtilities.isLeftMouseButton(e))
        {
            JComponent comp = (JComponent) e.getSource();
            TransferHandler handler = comp.getTransferHandler();
            handler.exportAsDrag(comp, e, TransferHandler.COPY);
        }
    }
    
    @Override
    public void mouseMoved(MouseEvent e) { }
}