package de.engineapp.controls;

import java.awt.Point;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.ChangeListener;

public final class PropertySpinner extends JSpinner implements MouseMotionListener, MouseListener
{
    private static final long serialVersionUID = -7629422754400062262L;
    
    
    private double min;
    private double max;
    private Point mouseDown;
    
    
    public PropertySpinner(double start, double min, double max, double step, ChangeListener cl)
    {
        this(start, min, max, step, cl, false);
    }
    
    public PropertySpinner(double start, double min, double max, double step, ChangeListener cl, boolean wrap)
    {
        super(wrap ? new CyclingSpinnerNumberModel(start, min, max, step) : 
                     new SpinnerNumberModel(start, min, max, step));
        this.min = min;
        this.max = max;
        
        this.addChangeListener(cl);
        
        NumberEditor numEditor = (NumberEditor) this.getEditor();
        numEditor.getTextField().addMouseMotionListener(this);
        numEditor.getTextField().addMouseListener(this);
        
    }
    
    @Override
    public Double getValue()
    {
        return (Double) this.getModel().getValue();
    }
    
    @Override
    public void setValue(Object value)
    {
        this.getModel().setValue(value);
    }
    
    
    @Override
    public void mouseDragged(MouseEvent e)
    {
        if (SwingUtilities.isMiddleMouseButton(e) || SwingUtilities.isRightMouseButton(e))
        {
            int diff = e.getX() - mouseDown.x;
            
            double value = Math.min(Math.max(getValue() + diff, min), max);
            
            setValue(value);
            
            mouseDown = e.getPoint();
        }
    }
    
    @Override
    public void mouseMoved(MouseEvent e) { }
    
    
    @Override
    public void mouseClicked(MouseEvent e) { }
    
    @Override
    public void mousePressed(MouseEvent e)
    {
        if (SwingUtilities.isMiddleMouseButton(e))
        {
            mouseDown = e.getPoint();
        }
    }
    
    @Override
    public void mouseReleased(MouseEvent e) { }
    
    @Override
    public void mouseEntered(MouseEvent e) { }
    
    @Override
    public void mouseExited(MouseEvent e) { }
}