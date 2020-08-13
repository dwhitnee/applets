import java.awt.Color;
import java.util.Vector;	// as in 'list', not as in Vector2D

//----------------------------------------------------------------------
//	NewtonianObject
/**
 *	An object in Netwonian space, i.e. it has velocity and hits things.
 *	Each new object is created with a random color.
 *	Previous path can be saved, but must be called explicitly with savePosition().
 *
 *	@author		<a href="mailto:dwhitney@cs.stanford.edu">David Whitney</a>
 *	@version	April 27, 1999
 */
//----------------------------------------------------------------------

public class NewtonianObject
{
	Vector2D	_position;	// m
	Vector2D	_velocity;	// m/s
	double		_radius; 	// m
	String		_name;		// what to call this object on screen

	Vector2D	_startPosition;	// cache where this object started.
	Vector		_oldPositions;	// list of places this object has been.
	boolean		_saveOldPositions;
	
	Color		_color;		// color of object
	
	// list of colors an object will have assigned to it.
	static int		sColorIndex = 0;
//	static Color	sColors[] = { 
//			Color.blue, Color.cyan, Color.green,
//  			Color.magenta, Color.darkGray };
	static Color	sColors[] = { 
		// 90% saturation, 60% brightness
		new Color( 153,  15, 139 ),		// light purple 
		new Color(  43,  15, 153 ),		// dark purple 
		new Color(  15,  61, 153 ),		// blue
		new Color(  15, 128, 153 ),		// aqua 
		new Color(  15, 153,  29 ),		// green 
		new Color(  151, 98,  15 ) 		// yellow brown 
	};

/*
E72520 (redish)
31B1D1 (bluish)
359830 (greenish)
EC1881 (fuchia)
D4E84A (yellowish) 0xB4C83A
E34E1A (orange)
*/
	// black, lightGray, darkGray, gray, white, yellow, orange, Color.red, Color.pink,
	
	
	//----------------------------------------------------------------------
	//	Constructor
	/** 
	 *	@param   inName         What to call this object on screen.
	 *	@param   inRadius       Size of object, in meters.
	 */
	//----------------------------------------------------------------------
	public NewtonianObject( String inName, double inRadius)
	{
		_name = inName;
		_position = new Vector2D( 0, 0 );
		_startPosition = new Vector2D( 0, 0 );
		_velocity = new Vector2D( 0, 0 );
		_radius = inRadius;
		_saveOldPositions = false;		// dont save trail yet
		_oldPositions = new Vector();
		_color = sColors[ sColorIndex++ ];
		sColorIndex %= sColors.length;
		
	}
	
	public Vector2D		getVelocity()		{ return _velocity; }
	public Vector2D		getPosition()		{ return _position; }
	public Vector2D		getStartPosition()	{ return _startPosition; }
	public double		getRadius()			{ return _radius; }
	public Color		getColor()			{ return _color; }
	public Vector		getTrail()			{ return _oldPositions; }
	public boolean		isSavingPositions()	{ return _saveOldPositions; }

	public void	setVelocity( double x, double y) { 	_velocity.set( x, y ); }
	public void	setVelocity( Vector2D v) 		 { _velocity.set( v.x, v.y ); }
	public void	setStartPosition()				{ _startPosition.x = _position.x; 
												  _startPosition.y = _position.y; }
	public void	setRadius( double r )			{ _radius = r; }
	public void	setColor( Color c )				{ _color = c; }

	public void	setPosition( double x, double y) {
		 _position.set( x, y ); 
//		if (_saveOldPositions)		// remember this position
//			savePosition( _position );		
	}
	
	public void	setPosition( Vector2D p)		{
		_position.set( p.x, p.y ); 
//		if (_saveOldPositions)		// remember this position
//			savePosition( _position );		
	}
		
	public void	setSavePositions( boolean b )	{ 
		_saveOldPositions = b; 
		if (_saveOldPositions)		// if enabling memory, clear out old positions
			_oldPositions.removeAllElements();
	}
	public void savePosition( Vector2D inPos )
	{
		_oldPositions.addElement( new Vector2D( inPos ));		
	}
	
	public String toString()
	{
		return _name + ": " + "position = " + _position + "velocity = " + _velocity;
	}
	
	//----------------------------------------------------------------------
	//	updatePosition
	/**	Apply object's velocity to itself
	*	@param	time	how long to apply the velocity (seconds).
	*/
	//----------------------------------------------------------------------
	public void updatePosition( double time )
	{
		_position.add( _velocity.x * time, _velocity.y * time  );

//		if (_saveOldPositions)		// remember this position
//			savePosition( _position );		

	}

	//----------------------------------------------------------------------
	//	contains
	/** 
	 *	@param		inPt
	 *	@return		true if inPt is within the radius of this object.
	 */
	//----------------------------------------------------------------------
	public boolean contains( Vector2D inPt)
	{
		return	(inPt.x - _position.x) * (inPt.x - _position.x) +
				(inPt.y - _position.y) * (inPt.y - _position.y) <= _radius*_radius;
	}	 

}	// NewtonianObject
