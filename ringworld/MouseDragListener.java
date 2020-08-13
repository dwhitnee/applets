import java.awt.*;
import java.util.*;
import java.awt.event.*;

//----------------------------------------------------------------------
//	MouseDragListener
/**
 *	Listens for a mouse click and then monitors the mouse drag, and then
 *	updates any Observers when the drag is complete.
 * 
 *	@author		<a href="mailto:dwhitney@cs.stanford.edu">David Whitney</a>
 *	@version	May 11, 1999
 */
//----------------------------------------------------------------------

public class MouseDragListener	extends Observable
								implements MouseListener
{
	boolean			_isDragging;
	boolean			_isShiftPressed;
	Point			_startDrag, _endDrag;
	Component		_listenee;
	MouseMotionListener	_dragger;
	
	//----------------------------------------------------------------------
	//----------------------------------------------------------------------
	public MouseDragListener( Component inTarget )
	{
		_isDragging = false;
		_isShiftPressed = false;
		_listenee = inTarget;	// who we're listening to.
		_startDrag = null;
		_endDrag = null;
		
		_listenee.addMouseListener( this );

		_dragger = new MouseMotionListener()
		{
			public void mouseMoved(MouseEvent e) { }
			public void mouseDragged(MouseEvent e)
			{
				_endDrag = e.getPoint();
			}
		};
	}

	//----------------------------------------------------------------------
	//	Accessors
	//----------------------------------------------------------------------
	public boolean	isDragging()	{ return _isDragging; }
	public boolean	isShiftPressed(){ return _isShiftPressed; }
	public Point	getDragStart()	{ return _startDrag; }
	public Point	getDragEnd()	{ return _endDrag; }
		
	//----------------------------------------------------------------------
	//	mousePressed - register where user clicked, rubber band a line
	//	from there, and return the vector when mouse released.
	//----------------------------------------------------------------------
	public  void mousePressed( MouseEvent e )
	{
		_startDrag = e.getPoint();
		_endDrag = e.getPoint();
		_isDragging = true;
		_isShiftPressed = e.isShiftDown();
		setChanged(); notifyObservers( _startDrag );

		_listenee.addMouseMotionListener( _dragger );
	}
	
	public void stopDrag() 
	{
		_isDragging = false;
		_listenee.removeMouseMotionListener( _dragger );	
	}
	 
	public void mouseReleased( MouseEvent e ) 
	{
		if (isDragging()) {
			stopDrag();
			_endDrag = e.getPoint();
			setChanged(); notifyObservers( _endDrag );
		}
	}

	public  void mouseClicked( MouseEvent e ) {}	// ignore
	public  void mouseEntered( MouseEvent e ) {}
	public  void mouseExited( MouseEvent e ) {}
}
