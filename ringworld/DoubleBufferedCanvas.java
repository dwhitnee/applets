package ZeroGSoccer.graphics;

import java.awt.*;
import java.awt.event.*;

//----------------------------------------------------------------------
//	DoubleBufferedCanvas
/**
 *	Canvas for drawing flicker-free animation.
 *	Override drawScene() with drawing code, and use redraw() to force an
 *	immediate draw (unlike the asynchronous repaint().)
 * 
 *	@author		<a href="mailto:dwhitney@cs.stanford.edu">David Whitney</a>
 *	@version	April 28, 1999
 */
//----------------------------------------------------------------------

public abstract class DoubleBufferedCanvas	extends Canvas
											implements ComponentListener
{
	Image		_image;
	Graphics	_backBuffer;
	Graphics	_frontBuffer;
	int			_height, _width;
	
	//----------------------------------------------------------------------
	//	The interesting stuff to a user.
	//----------------------------------------------------------------------

	/**	Override this and put drawing code here.  */
	public abstract void drawScene( Graphics g );
	
	/**	Convenience routine to force an immediate draw. repaint(1) might be better. */
	public void drawNow()
	{
		if (_frontBuffer == null)
			_frontBuffer = getGraphics();	// is this an expensive routine? yes.			

		paint( _frontBuffer );
	}
	
	//----------------------------------------------------------------------
	/**
	 *	Constructs a double buffered canvas.
	 */
	//----------------------------------------------------------------------
	public DoubleBufferedCanvas()
	{
		setSize( getSize() );
		setBackground( Color.black );
		addComponentListener( this );		// listen for resize events
		disableEvents(	ComponentEvent.COMPONENT_MOVED | 
						ComponentEvent.COMPONENT_SHOWN | 
						ComponentEvent.COMPONENT_HIDDEN );
	}	
	
	//----------------------------------------------------------------------
	/**
	 *	Erases the buffer to the background color.
	 */
	//----------------------------------------------------------------------
	public void	clearBuffer( Graphics g )
	{
		if (g != null) {
			g.setColor( getBackground() );
			g.fillRect( 0, 0, _width, _height );
		}
	}
	public void	clearBuffer()
	{
		if (_backBuffer != null) {
			_backBuffer.setColor( getBackground() );
			_backBuffer.fillRect( 0, 0, _width, _height );
		}
	}

	//----------------------------------------------------------------------
	//	Routines to manage the buffers.
	//----------------------------------------------------------------------
	public	void	componentMoved( ComponentEvent e )	{}	// ignored
	public	void	componentShown( ComponentEvent e )	{}
	public	void	componentHidden( ComponentEvent e )	{}
	public	void	componentResized( ComponentEvent inEvent )
	{
		setSize( inEvent.getComponent().getSize() );
	}

	/** @see java.awt.Component#setSize( Dimension ) */
	public void setSize( Dimension inDim ) 
	{
		setSize( inDim.width, inDim.height );
	}
	
	//----------------------------------------------------------------------
	//	rebuild the image buffer to draw into
	/** @see java.awt.Component#setSize( int, int ) */
	//----------------------------------------------------------------------
	public void setSize( int inWidth, int inHeight )
	{
		super.setSize( inWidth, inHeight );
		_width = inWidth;
		_height = inHeight;
	//	System.out.println("Resize " + _width + " " + _height);
		
		_image = createImage( _width, _height );

		if (_image != null) {
	//		System.err.println("Allocated image.");
			_backBuffer = _image.getGraphics();
		} else {
	//		System.err.println("Failed to allocated image, trying again later...");
			_backBuffer = null;
		}
	
	}
	
	//----------------------------------------------------------------------
	//	update -- override Component.update which clears the component.
	//	That is unnecesary since we copy an image into it.
	//----------------------------------------------------------------------
	public void update( Graphics g ) 
	{
		paint( g ); 
	}

	//----------------------------------------------------------------------
	//	paint -- draw new scene into the backbuffer, then "swap buffers"
	//	i.e., copy the backbuffer image onto the canvas
	//
	//	This routine can get called by the user or the appletviewer's 
	//	interaction thread.  So use the given Graphics object and not
	//	_frontBuffer, it may be null.  It could be written to
	//	use _frontBuffer always, unless null, but why bother?
	//----------------------------------------------------------------------
	public void paint( Graphics g ) 
	{
		if (g == null) {
			System.out.println("Graphics null");
			return;
		}
		if (_backBuffer == null) {
			setSize( getSize() );		// make another attempt to get a backbuffer
			drawScene( g );
		} else {
			drawScene( _backBuffer );
			g.drawImage( _image, 0, 0, this );	// swapBuffers
		//	g.drawRect( 1, 1, _width-2, _height-2 );	// draw border
		}
	}

	
}	// DoubleBufferedCanvas

            
       