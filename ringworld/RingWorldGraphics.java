import java.awt.*;
import java.awt.event.*;
import java.util.*;

//----------------------------------------------------------------------
//	RingWorldGraphics
/**
 *	Canvas for drawing the ringworld itself.  The ring, its buildings,
 *	the balls in the world, the paths the balls travel, etc.
 *	Routines for turning on and off these drawing features.
 * 
 *	@author		<a href="mailto:dwhitney@cs.stanford.edu">David Whitney</a>
 *	@version	April 28, 1999
 */
//----------------------------------------------------------------------

public class RingWorldGraphics	extends DoubleBufferedCanvas
								implements Runnable, Observer, MouseListener
{
	RingWorld			_ringWorld;		// the physical model
	CoordinateSystem	_xform;			// view transformation

	RingWorldPhysics	_physics;		// really shouldn't need this, but 
										// used for draw rate feedback
	long				_lastDrawTime;	// time in milliseconds
	
	int		_view;				// where are we looking at the world from?
	double	_apparentRotation;	// where ring appears to have rotated, in view.
	double	_lastRingPosition;	// for local view transformation

	boolean	_drawVelocity;		// draw velocity vectors on objects?
	boolean	_drawPlatforms;		// draw place where objects started
	boolean	_drawTrails;		// draw object paths

	double	_vectorScale;		// scale up velocity vectors to draw better.
	double	_zoomFactor;		// far away or close up view
	
	float	_speedMagnification;	// how much faster than real we're going.

	boolean	_debugging;			// temp values for debugging
	int		_a, _b, _d;
	
	PublicObservable	_observable;	// we are observable, use this to fake 'extends'
	
	NewtonianObject		_selectedObj;	// ball clicked on by user
	MouseDragListener	_mouseDragListener;	// watches for click and drag by user.

	// object store for drawing routines. Does this help much?  Saves constructors...
	Vector2D _p_dv, _v_dv;		// drawVector
	Vector2D _p_dp;				// drawPoint
	Vector2D _center_dc;		// draw/fillCircle

	static Color	sRingColor =  Color.gray; //new Color( 151, 64, 15 );
	
	public static final int	EXTERNAL_VIEW = 0;
	public static final int	LOCAL_VIEW = 1;

	
	//----------------------------------------------------------------------
	//	constructor	
	/**
	 *	@param   inWorld			The ringworld to draw.
	 *	@param   inPhysics			The physics model to use.
	 */
	//----------------------------------------------------------------------
	public RingWorldGraphics( RingWorld inWorld, RingWorldPhysics inPhysics )
	{
		_ringWorld = inWorld;
		_physics = inPhysics;
		_lastRingPosition = 0;
		_apparentRotation = 0;
		
		_drawVelocity = false;
		_drawPlatforms = true;
		_drawTrails = true;
		
		_debugging = false;
		
		_p_dv = new Vector2D( 0, 0);
		_v_dv = new Vector2D( 0, 0);
		_p_dp = new Vector2D( 0, 0);
		_center_dc = new Vector2D( 0, 0);

		_xform = new CoordinateSystem();
		_view = EXTERNAL_VIEW;
		_vectorScale = 1.0;
		_zoomFactor = 1.0;
		_selectedObj = null;
		
		_observable = new PublicObservable();
		
		setCursor( Cursor.getPredefinedCursor( Cursor.CROSSHAIR_CURSOR ));
		
		// we only care about press, release, and drag events
		// Does this really speed anything up?
		disableEvents(	MouseEvent.MOUSE_ENTERED | 
						MouseEvent.MOUSE_EXITED | 
						MouseEvent.MOUSE_MOVED );

		_mouseDragListener = new MouseDragListener( this );	// handle vector drags
		_mouseDragListener.addObserver( this );
		addMouseListener( this );		// listen for new object double clicks
	}

 	//----------------------------------------------------------------------
	//	run - Thread loop
	//----------------------------------------------------------------------
	public void run()
	{
		System.out.println("Graphics thread starting...");
		while (true) {
			repaint();
			//Thread.yield();
		}
	}
	
	//----------------------------------------------------------------------
	//	Accessors
	//----------------------------------------------------------------------
	public double	getRotation()			{ return _apparentRotation; }
	public int 		getViewMode()			{ return _view; }

	public boolean	isViewLocked()			{ return _view == LOCAL_VIEW; }
	public boolean	isVelocityVectorDrawn()	{ return _drawVelocity; }
	public boolean	isPlatformsDrawn()		{ return _drawPlatforms; }
	public boolean	isTrailsDrawn()			{ return _drawTrails; }
	
 	public void addObserver(Observer inObserver) {
 		_observable.addObserver( inObserver ); 
 	}
 	 
	//----------------------------------------------------------------------
	//	state toggles
	//----------------------------------------------------------------------
	public void toggleDebugging() {  _debugging = !_debugging; }
	
	/**	Toggles vector drawing on each object.	 */
	public void toggleVelocityVectors()	{  
		_drawVelocity = !_drawVelocity; 
		_observable.setChanged(); 
	}
	public void setDrawVelocityVectors( boolean inMode ) {
		_drawVelocity = inMode; 
		_observable.setChanged(); 
	}
	
	/**	Toggles initial platform drawing on each object.	 */
	public void togglePlatforms()	{  
		_drawPlatforms = !_drawPlatforms; 
		_observable.setChanged(); 
	}
	public void setDrawPlatforms( boolean inMode ) {
		_drawPlatforms = inMode; 
		_observable.setChanged(); 
	}

	/**	Toggles path drawing for each object.	 */
	public void toggleTrails()	{  
		_drawTrails = !_drawTrails; 
		_observable.setChanged(); 
	}
	public void setDrawTrails( boolean inMode ) {
		_drawTrails = inMode; 
		_observable.setChanged(); 
	}

	/**	Draw world closer up.  */
	public void zoomIn()				{  _zoomFactor *= 1.5;	}

	/**	Draw world farther out.  */
	public void zoomOut() 				{  _zoomFactor *= 2./3.;
		if (_zoomFactor < 1)
			_zoomFactor = 1.0;
	}
	/**	Toggles the user's view between local and world views. */
	public void toggleViewMode()
	{
		// compensate for how much the viewer *thinks* the ring is rotated.
		if (_view == LOCAL_VIEW)
			_lastRingPosition = _ringWorld.getRotation() - _apparentRotation;
			
		_view = 1 - _view; 		// toggle mode
		
		_observable.setChanged();
	}
	
	public void setViewMode( int inMode )
	{
		if (((_view == LOCAL_VIEW) && (inMode == EXTERNAL_VIEW)) ||
			((_view == EXTERNAL_VIEW) && (inMode == LOCAL_VIEW)))
		{
			toggleViewMode();
		}	
	}
	public void setLockView( boolean inSetLocked )
	{
		if (inSetLocked)
			setViewMode( LOCAL_VIEW );
		else
			setViewMode( EXTERNAL_VIEW );
	}
	
	//----------------------------------------------------------------------
	//	calcTransform - make transformation matrix so everything fits on
	//	screen.  
	//	This is the only place _xform is modified.  Ensure that
	//	no one in other threads mucks with _xform during this time also, 
	//	via	'synchronized' access in non-draw routines (listeners, interaction, ...)
	//----------------------------------------------------------------------
	void calcTransform( double inRotation )
	{
		Dimension screenSize = getSize();
			
		double hscale = screenSize.width / (_ringWorld.getRadius() * 2.5);
		double vscale = screenSize.height / (_ringWorld.getRadius() * 2.5);
		double scale;
		
		if (hscale < vscale )
			scale = hscale;
		else
			scale = vscale;

		scale *= _zoomFactor;
		
		// draw thread, don't allow any mucking until xform is complete
		synchronized( _xform ) 
		{
			_xform.reset();	
			_xform.rotate( inRotation );
			// doesn't matter where scale happens with object centered coords
			_xform.scale( scale );			
			
			if (_zoomFactor == 1.0)							// centered, save some math
				_xform.translate( _ringWorld.getRadius() * 1.2,
								  _ringWorld.getRadius() * 1.2 );
			else {											// zoom in on bottom
				_xform.translate( _ringWorld.getRadius() * 1.2 / _zoomFactor, 
								  _ringWorld.getRadius() * (2.2 / _zoomFactor - 1.0));
			}
		}	

	}

	//----------------------------------------------------------------------
	//	Get focus when peer added so key shortcuts work.
	//----------------------------------------------------------------------
	public void addNotify() 
	{
		super.addNotify();
		requestFocus();
	}
	
	//----------------------------------------------------------------------
	//	update - get Observable notification of a mouse drag completion.
	//	Accelerate the object dragged.
	//----------------------------------------------------------------------
	public void update( Observable inTarget, Object inArg)
	{
		if (inTarget instanceof MouseDragListener) {	
			
			requestFocus();		// get focus when we are clicked on key events work.
			
			if (_mouseDragListener.isDragging()) {
				// drag started, are we on any balls?
				_selectedObj = pick( _mouseDragListener.getDragStart() );
				if (_selectedObj == null)
					_mouseDragListener.stopDrag();	// no hit, no reason to care
					
			} else {	// drag ended
			
				if (_selectedObj == null) 
					return;
					
				if (_mouseDragListener.isShiftPressed()) {
					// set new velocity
					Point p1 = _mouseDragListener.getDragStart();
					Point p2 = _mouseDragListener.getDragEnd();
					Vector2D velocity = new Vector2D( 	(p2.x - p1.x) / _vectorScale, 
														(p2.y - p1.y) / _vectorScale );
					Vector2D velocityT;
					synchronized( _xform ) {	// interaction thread
						velocityT = velocity.inverseRotate( _xform );
					}
					velocityT.add( _selectedObj.getVelocity() );

					_ringWorld.releaseObject( _selectedObj );
					_selectedObj.setVelocity( velocityT );
				} else {
					if (!_ringWorld.getFreeObjects().contains( _selectedObj ))
						_ringWorld.fixObject( _selectedObj );
				}
				_selectedObj = null;		// we're done with this guy				
			}
		} else {
			Exception ex = new Exception("Unknown updater called\n");
			ex.printStackTrace();
		}
		
	}

	//----------------------------------------------------------------------
	//	Create new ball on double click.
	//----------------------------------------------------------------------
	public  void mousePressed	(MouseEvent e ) {}	// ignore
	public	void mouseReleased	(MouseEvent e ) {}
	public  void mouseEntered	(MouseEvent e ) {}
	public  void mouseExited	(MouseEvent e ) {}
	
	public  void mouseClicked	(MouseEvent e ) 
	{
		if (e.getClickCount() > 1) {
			NewtonianObject ball = new NewtonianObject("Ball", 10);
			Vector2D screenPt = new Vector2D( e.getPoint() );
			synchronized( _xform ) {	// interaction thread
				ball.setPosition( screenPt.inverseTransform( _xform ));
			}
			_ringWorld.addFixed( ball );
		}
	}
	
	//----------------------------------------------------------------------
	//	pick - see if we hit one of our objects.  Transform screen position
	//	to world coordinates.
	//	Returns the object hit, or null if no hit.
	//----------------------------------------------------------------------
	NewtonianObject pick( Point inP )
	{
		Vector2D p = new Vector2D( inP.x, inP.y );
		Vector2D pt;
		
		synchronized( _xform ) {	// interaction thread
			pt = p.inverseTransform( _xform );
		}
		// ensure object list does not change on us
		synchronized( _ringWorld )
		{
			for (Enumeration e = _ringWorld.getAllObjects().elements() ;
				 e.hasMoreElements() ; ) 
			{
				NewtonianObject obj = (NewtonianObject) e.nextElement();
				if (obj.contains( pt ))
					return obj;
			}
		}
		return null;
	}


	//----------------------------------------------------------------------
	//	drawScene - draw the world, overrides DoubleBufferedCanvas
	//----------------------------------------------------------------------
	public void drawScene( Graphics g )
	{
		long deltaT = System.currentTimeMillis() - _lastDrawTime;
		_lastDrawTime = System.currentTimeMillis();
		
		_observable.notifyObservers();
		
		clearBuffer( g );
		
		double viewRotation = 0.0;	// for drawing the world's objects
		
		switch (_view)
		{
			case LOCAL_VIEW: {
				viewRotation = _ringWorld.getRotation() - _apparentRotation;
				break;
			}
			case EXTERNAL_VIEW: {
				viewRotation = _lastRingPosition;	// where we left off
				_apparentRotation = _ringWorld.getRotation() - _lastRingPosition;
				break;
			}
			default: 
				throw new Error("ERROR: Unknow view parameter");
		}
		
		calcTransform( -viewRotation );		// spin back so we're level

		// draw ringworld
		g.setColor( sRingColor );
		drawCircle( g, 0, 0, _ringWorld.getRadius(), _apparentRotation );

		synchronized( _ringWorld )	// ensure object list does not change on us
		{
			// draw objects in world
			for (Enumeration e = _ringWorld.getAllObjects().elements() ;
				 e.hasMoreElements() ; ) 
			{
				NewtonianObject obj = (NewtonianObject) e.nextElement();
				Vector2D pos = obj.getPosition();
				if ( obj == _selectedObj)
					g.setColor( Color.red );
				else
					g.setColor( obj.getColor() );
				
				fillCircle( g, pos, obj.getRadius());

				if (obj.isSavingPositions()) {
					Vector2D rotPos = new Vector2D( pos );
					rotPos.rotate( _ringWorld.getRotation() );
 					obj.savePosition( rotPos );
				}
								
				if (_drawPlatforms)
					drawPlatform( g, obj );	// draw plac where this ball started.

				if (_drawTrails)
					drawTrail( g, obj );	// draw plac where this ball started.

				if (_drawVelocity) {
					g.setColor( Color.gray );
					drawVector( g, pos, obj.getVelocity(), _vectorScale );	// double the size

				}
			}
			// draw cities
			for (Enumeration e = _ringWorld.getBuildings().elements() ;
				 e.hasMoreElements() ; ) 
			{
				drawBuilding( g, (NewtonianObject) e.nextElement(), 1 );
			}
		}
		
		// drag a vector rubber band on shift drag, move the ball on drag
		// selectedObj is set by interaction process, we may be ahead of it.
		if (_mouseDragListener.isDragging() && (_selectedObj != null)) {
		
			if (_mouseDragListener.isShiftPressed()) {
				// draw velocity vector rubberband			
				g.setColor( Color.gray );
				g.drawLine( _mouseDragListener.getDragStart().x, 
							_mouseDragListener.getDragStart().y, 
							_mouseDragListener.getDragEnd().x, 
							_mouseDragListener.getDragEnd().y );
			} else {		
				// move selected ball
				Vector2D mouse = new Vector2D( _mouseDragListener.getDragEnd() );
				_selectedObj.setPosition( mouse.inverseTransform( _xform ));
			}
		}
		
		g.setColor( Color.black );
		
		if (_physics.isRealTime() || (_d++ > 10)) {	// only update these values every 30 frames
			_speedMagnification = (int) (10*_physics.getTimeMagnification()) / 10f;
			
			// This is physically impossible, but Windows98 manages to screw up sleep()
			// allowing an immediate redraw.
			if (deltaT != 0)
				_a = (int) (1000f / (float) deltaT );
			else
				_a = 666;	
			_b = (int) _physics.getSleepTime();
			_d = 0;
		}

		int lineHeight = g.getFontMetrics().getHeight();

		g.drawString( String.valueOf( _speedMagnification ) + "x speed", 
						20, lineHeight );

		if (_debugging) {			
			g.drawString( String.valueOf( _a ) + " fps", 20, 2*lineHeight );
			g.drawString( String.valueOf( _physics.getTickTime() ) + " s tick", 20, 3*lineHeight );
			g.drawString( String.valueOf( _b ) + " ms", 20, 4*lineHeight );
		}
		
					
		// draw world info in corners
		Dimension d = getSize();
		
		g.drawString( "r = " + String.valueOf( Math.round(_ringWorld.getRadius()) ) + " m", 
						20, d.height - 4*lineHeight );
		g.drawString( "g = " + String.valueOf( round1(_ringWorld.getGravity()) ) + " g",
						20, d.height - 3*lineHeight );
		g.drawString( "v = " + String.valueOf( round1(_ringWorld.getVelocity()) ) + " m/s",
						20, d.height - 2*lineHeight );
		g.drawString( "t = " + String.valueOf( round1(_ringWorld.getPeriodOfRotation()) ) + " s", 
						20, d.height - lineHeight );
		
		
		// based on frame rate modify pause time to optimize drawing
		// should probably have a moving average of deltaT's
		// deltaT of 33 is ideal (30 fps)
		// Note:  this gets messed up in "real-time" because tick-time gets
		// modified as well.
		
		if (deltaT < 30)
			_physics.setSleepTime( (int) (_physics.getSleepTime() * 1.05 + 1));
		if (deltaT > 50)
			_physics.setSleepTime( (int) (_physics.getSleepTime() * 0.95 - 1));
	}

	//----------------------------------------------------------------------
	//	round1 -- round to one decimal place
	//----------------------------------------------------------------------
	static final float round1( double d )
	{
		return Math.round( d*10 ) / 10f;
	}
	
	//----------------------------------------------------------------------
	//	drawTrail -- draw path object travelled.
	//----------------------------------------------------------------------
	void drawTrail( Graphics g, NewtonianObject obj )
	{
		for (Enumeration e = obj.getTrail().elements() ; e.hasMoreElements() ; ) {
			Vector2D pt = new Vector2D( (Vector2D) e.nextElement() );
			pt.rotate( -_ringWorld.getRotation() );		
			drawPoint( g, pt );
		}
	}
	
	//----------------------------------------------------------------------
	//	drawPlatform -- draw a line underneath the object's original position.
	//----------------------------------------------------------------------
	void drawPlatform( Graphics g, NewtonianObject obj )
	{
		Vector2D p = new Vector2D( obj.getStartPosition() );

		// platform is in ring coordinates, must rotate into world coords
		p.rotate( -_ringWorld.getRotation() );		
		double angle = p.calcAngle();
		
		Vector2D towardFloor = 
				new Vector2D( Math.cos( angle ), Math.sin( angle ));
		Vector2D parallelToFloor = 
				new Vector2D( Math.sin( angle ), -Math.cos( angle ));

		towardFloor.scale( obj.getRadius() );
		parallelToFloor.scale( obj.getRadius() );
		p.add( towardFloor );
		p.add( parallelToFloor );

		drawVector( g, p, parallelToFloor, -2.0 );	
	}
	
	//----------------------------------------------------------------------
	//	drawBuilding --
	//----------------------------------------------------------------------
	void drawBuilding( Graphics g, NewtonianObject obj, double inHeight )
	{
		Vector2D p = new Vector2D( obj.getPosition() );

		// platform is in ring coordinates,must rotate into world coords
		p.rotate( -_ringWorld.getRotation() );		
		double angle = p.calcAngle();
		
		Vector2D towardFloor = 
				new Vector2D( Math.cos( angle ), Math.sin( angle ));
		Vector2D awayFromFloor = 
				new Vector2D( -Math.cos( angle ), -Math.sin( angle ));
		Vector2D parallelToFloor = 
				new Vector2D( Math.sin( angle ), -Math.cos( angle ));
	
		awayFromFloor.scale( obj.getRadius() );
		parallelToFloor.scale( obj.getRadius()/2 );
		towardFloor.scale( obj.getRadius() );

		p.add( awayFromFloor );	
		
		// roof
		g.setColor( obj.getColor() );
		p.sub( parallelToFloor );
		drawVector( g, p, parallelToFloor, 2d );

		// walls
		drawVector( g, p, towardFloor, 1d );
		p.add( parallelToFloor );
		p.add( parallelToFloor );
		drawVector( g, p, towardFloor, 1d );	
	}
		
	//----------------------------------------------------------------------
	//	round -- rounds to nearest integer.  Math.round(d) returns 'long'.
	//----------------------------------------------------------------------
	static final int round( double d )	{ return (int) Math.round( d ); }

	//----------------------------------------------------------------------
	//	drawVector -- starting at point p, along vector v, magnified by size.
	//----------------------------------------------------------------------
	void drawVector( Graphics g, Vector2D p, Vector2D v, double size )
	{
		_xform.transformVec( p, _p_dv );
		_xform.rotateVec( v, _v_dv );

	//	Vector2D pt = p.transform( _xform );
	//	Vector2D vt = v.rotate( _xform );

		g.drawLine(	round( _p_dv.x ), round( _p_dv.y ), 
					round( _p_dv.x + size*_v_dv.x ), round (_p_dv.y + size*_v_dv.y) );
	}
	
	//----------------------------------------------------------------------
	//	drawPoint -- 
	//----------------------------------------------------------------------
	void drawPoint( Graphics g, Vector2D p )
	{
		_xform.transformVec( p, _p_dp );
		
	//	Vector2D pt = p.transform( _xform );
		g.drawLine(	round( _p_dp.x ), round( _p_dp.y ), 
					round( _p_dp.x ), round( _p_dp.y ));
	}
	
	
	//----------------------------------------------------------------------
	//	drawCircle -- draw a circle centered at x, y with radius r.
	//	Takes doubles as arguements for precision, casts to int only
	//	for actual drawing.
	//----------------------------------------------------------------------
	void drawCircle( Graphics g, double x, double y, double r, double wedge )
	{
		wedge = wedge * 180/Math.PI;	// convert to degrees

		// transform center to world coord system
		Vector2D c = new Vector2D( x, y);
		_xform.transformVec( c, _center_dc );

		// transform radius to world coord system
		r *= _xform.getScale();
		if (r < 0.5)
			r = 1;
		
		// cast to int as the final step, not sooner
		// add 1 to radius so circle is truly radius r, and not inside a
		// box of side r.
		g.drawArc(	round(_center_dc.x - r), round(_center_dc.y - r), 
					round(r*2)+1, round(r*2)+1, 0, 360 );
	}

	//----------------------------------------------------------------------
	//	fillCircle -- returns transformed center point.
	//----------------------------------------------------------------------
   	void fillCircle( Graphics g, Vector2D inCenter, double r )
	{
		// transform center to world coord system
		_xform.transformVec( inCenter, _center_dc );
		
		// transform radius to world coord system
		r *= _xform.getScale();
		if (r < 0.5)
			r = 1;
		
		// cast to int as the final step, not sooner
		// add 1 to radius so circle is truly radius r, and not inside a
		// box of side r.
		g.fillArc(	round(_center_dc.x - r), round(_center_dc.y - r), 
					round(r*2)+1, round(r*2)+1, 0, 360 );
	}
	
	
}		// class RingWorldGraphics


