package de.engineapp.visual;

import java.awt.*;


/**
 * Interface that provides methods for visual objects.
 * 
 * @author Micha
 */
public interface IDrawable
{
    public Color getColor();
    public void setColor(Color color);
    
    public Color getBorder();
    public void setBorder(Color color);
    
    public int getDrawPriority();
    public void setDrawPriority(int priority);
    
    public void render(Graphics2D g);
}