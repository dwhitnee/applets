import java.util.*;
import java.awt.Color;

//----------------------------------------------------------------------
//	RingWorld
//
//	Synchronized methods when vectors begin modified.  Perhaps unnecessary?
/**
 *	A simulation of a rotating space colony in a weightless environment, 
 *	like those of David Niven's 'RingWorld' or Arthur C. Clark's 'Rama'
 *
 *	When the parameters of the world have changed, observers are notified.
 *
 *	@author		<a href="mailto:dwhitney@cs.stanford.edu">David Whitney</a>
 *	@version	April 27, 1999
 */
//----------------------------------------------------------------------
 
public class RingWorld extends Observable
{
	// dynamic
	double	_rotation;		// how far around we're rotated (radians)

	// fixed
	double	_radius;				// meters
	double	_gravity;				// apparent gravity at surface of ring
	double	_timeOfRevolution;		// time to complete one rotation
	double	_velocity;				// rotational, m/s
	double	_radiansPerSecond;		// velocity in radians/s
	boolean	_valuesCalculated;		// is object in consistent state?
	
	Vector	_allObjects;
	Vector	_freeObjects;
	Vector	_raisedObjects;	// objects elevated on platforms
	Vector	_stuckObjects;	// objects stuck to the ring

	Vector	_buildings;		// structures on ring
	int		_numBuildings;
	
	static final double	ACCELERATION_OF_GRAVITY = 9.8;	// m/s
	
	//----------------------------------------------------------------------
	/**
	 *	Builds a RingWorld.  Must use one of the set() routines to configure it.
	 */
	//----------------------------------------------------------------------
	public RingWorld()
	{
		_allObjects = new Vector();
		_freeObjects = new Vector();
		_raisedObjects = new Vector();
		_stuckObjects = new Vector();
		_buildings = new Vector();
		
		_numBuildings = 14;
	}

	//----------------------------------------------------------------------
	//	Accessors
	//----------------------------------------------------------------------
	public double	getRadius()			{ return _radius; }
	public double	getVelocity()		{ return _velocity; }
	public double	getGravity()		{ return _gravity; }
	public double	getPeriodOfRotation()		{ return _timeOfRevolution; }

	public double	getRotation()		{ return _rotation; }
	public Vector	getAllObjects()		{ return _allObjects; }
	public Vector	getFreeObjects()	{ return _freeObjects; }
	public Vector	getRaisedObjects()	{ return _raisedObjects; }
	public Vector	getStuckObjects()	{ return _stuckObjects; }
	public Vector	getBuildings()		{ return _buildings; }
	public void		setNumBuildings( int inNum )	{ _numBuildings = inNum; }
	
	//----------------------------------------------------------------------
	//	print
	//----------------------------------------------------------------------
	public String toString()
	{
		return	"radius = " + _radius + " m\n" +
				"gravity at surface = " + _gravity + " g\n" +
				"rotational velocity = " + _velocity + " m/s\n" +
				"period of revolution = " + _timeOfRevolution + " s\n" +
				"current rotation = " + _rotation + " radians\n" +
				"valid calculations? = " + _valuesCalculated + "\n";
	}

	//----------------------------------------------------------------------
	//	Surface orbital velocity satisfies g = v^2 / r
	//	v = x/t = 2*PI*r/t     (x == circumference)
	//	substituting for r: g = (2*PI*r/t)^2 / r
	//	t^2 = (2*PI*r)^2/gr = (2*PI)^2 * r/g
	//	t = 2*PI*sqrt(r/g)
	//	v = sqrt(rg)
	//
	//	Basic physics math used: 
	//	x = vt				distance, velocity, time
	//	a = delta_v/time	acceleration
	//
	//	Take the situation of half a revolution....
	//	x = PI*r	distance travelled over half a revolution
	//	t = x/v		time
	//	a = 2v/t	acceleration (in one dimension, scalar) over half a revolution.
	//				v, here, is the velocity in the x direction.
	//				Over half a revolution, velocity(x) goes from 
	//				+rotational_velocity to -rotational_velocity (or 2v )
	//----------------------------------------------------------------------

	//----------------------------------------------------------------------
	//	setRadiusAndGravity -- 
	/**	
	 *	Calculates the paramters of the RingWorld given it's radius (in meters)
	 *	and the desired gravity (in g's) at the surface.
	 */
	//	Given r, a, and d, solve for v and t.
	//	a = 2v / t
	//	v = at / 2
	//	v = a(x/v) / 2
	//	v^2 = ax / 2
	//	v = sqrt( ax/2 )
	//----------------------------------------------------------------------
	public synchronized void	setRadiusAndGravity(double r, double g)	
	{
		_radius = r; 
		_gravity = g;					// a = g * ACCELERATION_OF_GRAVITY
	
		double x = Math.PI * _radius;	// half a circumference
		_velocity = Math.sqrt(  x * _gravity * ACCELERATION_OF_GRAVITY / 2.0 );

		if ( _velocity != 0)
			_timeOfRevolution = 2 * x / _velocity;	// t = d/v for half a revolution
		else
			_timeOfRevolution = Double.POSITIVE_INFINITY;
			
		updateParameters();
	}
	
	//----------------------------------------------------------------------
	//	setTimeAndGravity -- 
	/**	
	 *	Calculates the paramters of the RingWorld given it's time of rotation
	 *	(in seconds) and the desired gravity (in g's) at the surface.
	 */
	//	Given a and t, solve for v and r.
	//	v = at / 2, and
	//	r = d / PI		(d = vt)
	//	r = vt / PI
	//----------------------------------------------------------------------	
	public synchronized void	setTimeAndGravity(double t, double g)	
	{
		_timeOfRevolution = t;
		_gravity = g;					// a = g * ACCELERATION_OF_GRAVITY
		_velocity = g * ACCELERATION_OF_GRAVITY * _timeOfRevolution / 4.0;
		_radius = _velocity * _timeOfRevolution / (2 * Math.PI);

		updateParameters();
	}

	//----------------------------------------------------------------------
	//	updateParameters -- once calculations are done, make everything consistent.
	//	and release all balls on ring to float until they hit something again.
	//----------------------------------------------------------------------
	void updateParameters()
	{
		_radiansPerSecond = 2 * Math.PI / _timeOfRevolution;
		_valuesCalculated = true;
		
		// release all stuck balls
		// do while instead of enumeration because list is being modified.
		while (!_stuckObjects.isEmpty())
			releaseObject( (NewtonianObject) _stuckObjects.firstElement() );
			
		generateBuildings();
		
		setChanged();
		notifyObservers();
	}
	
	//----------------------------------------------------------------------
	//	reset -- 
	/**	Removes all objects from the world, set rotation to zero.
	 */
	//----------------------------------------------------------------------
	public synchronized void reset()	
	{
		_allObjects.removeAllElements();
		_freeObjects.removeAllElements();
		_stuckObjects.removeAllElements();
		_raisedObjects.removeAllElements();
		_rotation = 0.;
	 }

	//----------------------------------------------------------------------
	//	generateBuildings -- sprinkle buildings around world floor
	//----------------------------------------------------------------------
	void generateBuildings()	
	{
		NewtonianObject	building;
		
		_buildings.removeAllElements();
		
		for (int i = 0; i < _numBuildings; i++ ) {
			building = new NewtonianObject("Building", 5 * (int)(4 * Math.random() + 1) );
			building.getPosition().setRadiusAngle( _radius, 2 * Math.PI * Math.random()  );
			building.setColor( Color.gray );
			_buildings.addElement( building );
		} 
	}

	//----------------------------------------------------------------------
	//	rotate -- 
	/**
	 *	Rotates the ring for the given amount of time.
	 *	@param		inTime		time to rotate for.
	 */
	//----------------------------------------------------------------------
   	public void	rotate( double inTime )
	{
		_rotation += rotationPerTime( inTime );
		_rotation %= Math.PI*2;		// toss excess
	}
	//----------------------------------------------------------------------
	//	rotationPerTime -- 
	/**
	 *	@param		inTime		time to rotate for.
	 *	@return		The angle the ring rotates through in given time, in radians.
	 */
	//----------------------------------------------------------------------
	public double rotationPerTime( double inTime )
   		throws IllegalStateException
	{
		if (!_valuesCalculated)
			throw new IllegalStateException("RingWorld paramters not yet set.");
			
		return inTime * _radiansPerSecond;
	}
	
	//----------------------------------------------------------------------
	//	releaseObjects
	/**
	 *	Allows objects that are hovering (raised) to fall.
	 */
	//----------------------------------------------------------------------
	public synchronized void releaseObjects()
	{
		// do while instead of enumeration because list is being modified.
		while (!_raisedObjects.isEmpty())
			releaseObject( (NewtonianObject) _raisedObjects.firstElement() );
	}

	public synchronized void releaseObject( NewtonianObject inObject )
	{
		// been there, done that.
		if (_freeObjects.contains( inObject ))
			return;
		
		inObject.setSavePositions( true );	// remember path	
		_freeObjects.addElement( inObject );
		
		_raisedObjects.removeElement( inObject );
		_stuckObjects.removeElement( inObject );	// take it off ring if need be
	}
	
	//----------------------------------------------------------------------
	//	add
	/**
	 *	Add a free floating physical thing to the RingWorld environment.
	 *	@param   inObject		The object to add.
	 */
	//----------------------------------------------------------------------
	public synchronized void addFree( NewtonianObject inObject )
	{
		addFixed( inObject );
		releaseObject( inObject );
	}

	//----------------------------------------------------------------------
	//	addFixed
	/**
	 *	Add a physical thing to the RingWorld environment as if stuck to a
	 *	ladder at its present position.
	 *
	 *	@param   inObject         The object to add.
	 */
	//----------------------------------------------------------------------
	public synchronized void addFixed( NewtonianObject inObject )
	{
		_allObjects.addElement( inObject );
		fixObject( inObject );
	}
	
	//----------------------------------------------------------------------
	/**	Fix an existing object to the ring.  Resets the starting position. */
	//----------------------------------------------------------------------
	public synchronized void fixObject( NewtonianObject inObject )
	{
		_freeObjects.removeElement( inObject );	// remove from old lists
		_stuckObjects.removeElement( inObject );

		if (!_raisedObjects.contains( inObject ))
			_raisedObjects.addElement( inObject );
		
		// save where this object started in ringworld coordinates
		// i.e. unwind the current rotation.
		inObject.setStartPosition();
		inObject.getStartPosition().rotate( _rotation );
	}
	
	//----------------------------------------------------------------------
	//	addAtAltitude
	/**
	 *	Add a physical thing to the RingWorld environment as if stuck to a
	 *	ladder at this altitude above the ring surface.
	 *
	 *	@param   inObject         The object to add.
	 *	@param   inAltitude       Object's altitude above the ring surface.
	 */
	//----------------------------------------------------------------------
	public synchronized void addAtAltitude( NewtonianObject inObject, double inAltitude )
		throws IllegalStateException
	{
		if (!_valuesCalculated)
			throw new IllegalStateException("RingWorld parameters not yet set.");
			
		if (inAltitude < inObject.getRadius()) 					// below floor
			inObject.setPosition( _radius - inObject.getRadius(), 0 );	
		else
			inObject.setPosition( _radius - inAltitude, 0 );	// in air
			
		addFixed( inObject );
	}

	//----------------------------------------------------------------------
	//	attach
	/**
	 *	Attach an object to the ring itself, such that the object will rotate
	 *	at the same speed as the ring.
	 *	@param   inObject         The object to attach.
	 */
	//----------------------------------------------------------------------
	public synchronized void attach( NewtonianObject inObject )
	{
		// been there, done that.
		if (_stuckObjects.contains( inObject ))
			return;

		inObject.setSavePositions( false );		// stop remembering path	
		_stuckObjects.addElement( inObject );
		_freeObjects.removeElement( inObject );
	}


}	// RingWorld
