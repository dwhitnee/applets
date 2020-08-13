import java.util.*;
import java.lang.Math.*;

//----------------------------------------------------------------------
//	RingWorldPhysics
/**
 *	Perform physical operations on all the objects in space, including the
 *	interactions with the ring itself.
 *
 *	@author		<a href="mailto:dwhitney@cs.stanford.edu">David Whitney</a>
 *	@version	April 27, 1999
 */
//----------------------------------------------------------------------

public class RingWorldPhysics extends Observable implements Runnable
{
	RingWorld	_ringWorld;
	int			_sleepTime;				// time to sleep after each go round (ms)
	double 		_tickTime;				// seconds between simulation ticks.
	long		_lastComputeTime_ms;	// time in milliseconds
	double		_magnification;			// time magnification factor

	// Real-Time parameters
	boolean		_attemptRealTime;	// try and lock speed
	long		_simStartTime_ms;	// system time in ms when we started,
	long		_simTime_ms;		// reset when mag or realtime mode changed.
	boolean		_resetSim;			// update real-time parameters.
	long		_pauseStartTime;	// remember when a pause started to adjust for it later.
	boolean		_recoverFromPause;
	
	//----------------------------------------------------------------------
	//	Constructor
	//----------------------------------------------------------------------
	public RingWorldPhysics( RingWorld inRingWorld )
	{
		_ringWorld = inRingWorld;
		_sleepTime = 50;		// ms
		_tickTime = 0.1;		// seconds	
		_attemptRealTime = true;
		resetSimulation();
		_magnification = 1.0;
		_lastComputeTime_ms = System.currentTimeMillis();
	}
	
	//----------------------------------------------------------------------
	//	Accessors
	//----------------------------------------------------------------------
	public double	getTickTime()					{  return _tickTime; }
	public int		getSleepTime()					{  return _sleepTime; }
	public boolean	isRealTime()					{  return _attemptRealTime; }
	public double	getTimeMagnification()			{  return _magnification; }

	void		setTickTime( double inTime)		{  _tickTime = inTime; }

	/**  sets simulation speed compared to real-time. */
	public void		setTimeMagnification( double inMag )	
	{
		if (isRealTime())
			_magnification = inMag; 
		else
			_tickTime *= inMag / _magnification;
		_resetSim = true;
	}
	
	/**  sets simulation to run in real time (try really hard anyway) */
	public void	setRealTime( boolean b )  
	{
		_attemptRealTime = b;
		_resetSim = true;
		setChanged();
		notifyObservers();	// tell others we've changed modes
	}
	
	/**  sets time to sleep after each iteration.  Helpful for frame rates. */
	public void setSleepTime(int inSleepTime_ms)
	{  
		_sleepTime = inSleepTime_ms; 
		if (_sleepTime < 5)
			_sleepTime = 5;	// ensure at least a 5ms pause. 
	}							// is this bad on systems with good multitasking?
	
	public void		startPause() {	
		_pauseStartTime = _lastComputeTime_ms;
	}
	public void		stopPause() {	
	//	_simStartTime_ms += System.currentTimeMillis() - _pauseStartTime;
		_recoverFromPause = true;
	}
	
	void resetSimulation()
	{
		_simStartTime_ms = System.currentTimeMillis();
		_simTime_ms = 0;		
		_resetSim = false;
	}
	
	//----------------------------------------------------------------------
	//	run the thread
	//----------------------------------------------------------------------
	public void run()
	{
		System.out.println("Physics thread starting...");
		while (true) {
			doPhysics();
			
			try { Thread.sleep( _sleepTime ); }	// pause for life to go by.
			catch (InterruptedException ex) { }
			Thread.yield();
		}
	}
	
	//----------------------------------------------------------------------
	//	doPhysics -- perform all interactions required in this quantum of time.
	//	make sure all sub operations are done before someone messes with the
	//	object lists.
	//	If in real time mode, see how much time passed since last time and
	//	adjust.
	//----------------------------------------------------------------------
	void doPhysics()
	{
		// Find out what time it is, set ticktime to advance simulation time
		// to current time.
		if (isRealTime()) {
		
			if (_resetSim) {		// use old tickTime once more, and reset for 
				resetSimulation();	// next loop. done here to avoid hiccups in timing.
			
			} else {
			//	if (System.currentTimeMillis() == _lastComputeTime_ms)
			//		System.out.println("Time did not pass. [ doPhysics()]  Windows sux.");
					
				if (_recoverFromPause) {
					_simStartTime_ms += System.currentTimeMillis() - _pauseStartTime - _tickTime;
					_recoverFromPause = false;
				}
				
				_tickTime = _magnification * (System.currentTimeMillis() - _simStartTime_ms) 
							 - _simTime_ms;

				// Windows bug,  System.time does not always advance to Microsoft?
				if (_tickTime == 0) {
			//		System.out.println("Tick time zero!  Windows sux.");
					_tickTime = 0.00001;	
				}
				
				
				_simTime_ms += _tickTime;
				_tickTime /= 1000d;		// convert millis to seconds.
			//	Assert.condition( _tickTime != 0 );
			}

		} else {
			// tickTime stays constant, just keep track of the current time dilation.		 
			_magnification = _tickTime*1000 / 
							 		(System.currentTimeMillis() - _lastComputeTime_ms);
		}
		_lastComputeTime_ms = System.currentTimeMillis();
		
//		if (isRealTime())
//			_tickTime *= _magnification / lastMagnification;
//		else
//			_magnification = lastMagnification;	// cache if anyone's curious
		
		synchronized( _ringWorld ) { rotateRing(); }
		synchronized( _ringWorld ) { moveObjects(); }
		synchronized( _ringWorld ) { collisionDetect(); }
	}
	
	//----------------------------------------------------------------------
	// 	rotateRing -- Move the ring and set velocities of objects attached to it.
	//----------------------------------------------------------------------
	void rotateRing()
	{
		for (Enumeration e = _ringWorld.getStuckObjects().elements() ;
			 e.hasMoreElements() ; ) 
		{
			setToRingVelocity( (NewtonianObject) e.nextElement() );
		}
		
		for (Enumeration e = _ringWorld.getRaisedObjects().elements() ;
			 e.hasMoreElements() ; ) 
		{
			setToRingVelocity( (NewtonianObject) e.nextElement() );
		}
		// rotate ring last to minimize jerkiness in graphics thread 
		// if multithreaded, (we aren't usually.)
		// (If there are too many objects they will jerk anyway)
		_ringWorld.rotate( _tickTime );	// advance a quantum of time
	}
	
	//----------------------------------------------------------------------
	// 	setToRingVelocity -- set velocity of this object to match the ring's
	//	velocity.  (i.e. it is attached to the ring)
	//----------------------------------------------------------------------
	void setToRingVelocity( NewtonianObject obj )
	{
		Vector2D p = obj.getPosition();

		// find out where the object is in spherical coordinates (radius, angle)
		
		// ensure it's not outside ring.
		double r = Math.sqrt( p.x*p.x + p.y*p.y );
		if (r > (_ringWorld.getRadius() - obj.getRadius()) )
			r = _ringWorld.getRadius() - obj.getRadius();
		
		double angle = p.calcAngle();
		angle += _ringWorld.rotationPerTime( _tickTime );
		angle %= Math.PI*2;		// toss excess

		Assert.condition( _tickTime != 0d ); 

		// set velocity such that when applied, object will be in right place
		// NOTE:  This is not instantaneous velocity, it is discrete.
		obj.setVelocity( (r*Math.cos( angle ) - p.x) / _tickTime,
		 				 (r*Math.sin( angle ) - p.y) / _tickTime );
	
	
	}
	
	//----------------------------------------------------------------------
	// 	setInstantaneousVelocityToRing -- set velocity of this object to match
	//	the ring's velocity at this instant.  Different than above routine in
	//	that this velocity ignores the discrete time steps the simulation uses.
	//	i.e. it will not work to use this velocity above because it cannot be 
	//	updated	fast enough.  
	//	It is useful for knowing the velocity at the instant a ball is let go.
	//----------------------------------------------------------------------
	void setInstantaneousVelocityToRing( NewtonianObject obj )
	{			
		Vector2D p = obj.getPosition();

		Assert.condition( !Double.isNaN( p.x ) ); 
		Assert.condition( !Double.isNaN( p.y ) ); 

		// find out where the object is in spherical coordinates (rad, angle)
		double r = Math.sqrt( p.x*p.x + p.y*p.y );
		double angle = p.calcAngle();
		double rotVelocity = Math.sqrt( Math.PI * r * _ringWorld.getGravity() * 4.9 );
		
		obj.setVelocity( rotVelocity * -Math.sin( angle ),
		 				 rotVelocity * Math.cos( angle ));
	}
	
	//----------------------------------------------------------------------
	//	moveObjects
	//----------------------------------------------------------------------
	void moveObjects()
	{
		// update positions based on last velocity
		for (Enumeration e = _ringWorld.getAllObjects().elements() ;
			 e.hasMoreElements() ; ) 
		{
			NewtonianObject obj = (NewtonianObject) e.nextElement();
			obj.updatePosition( _tickTime );
		}
		
		// Update the velocity vectors of stuck objects so they draw
		// better (as opposed to the now out of date velocity vectors)
		// And are precise when/if object is dropped.

		for (Enumeration e = _ringWorld.getStuckObjects().elements() ;
			 e.hasMoreElements() ; ) 
		{
			setInstantaneousVelocityToRing((NewtonianObject) e.nextElement());
		}
		for (Enumeration e = _ringWorld.getRaisedObjects().elements() ;
			 e.hasMoreElements() ; ) 
		{
			setInstantaneousVelocityToRing((NewtonianObject) e.nextElement());
		}

	}
	
	//----------------------------------------------------------------------
	//	collisionDetect -- iterate over all objects in world, see if they've
	//	collided with the RingWorld, make them stick if so.
	//----------------------------------------------------------------------
	void collisionDetect()
	{
		// attach free objects that have hit the ring
		for (Enumeration e = _ringWorld.getFreeObjects().elements() ;
			 e.hasMoreElements() ; ) 
		{
			NewtonianObject obj = (NewtonianObject) e.nextElement();
			
			// TODO: should determine intersection of velocity vector and
			// ring to determine where to attach the object.
			if (!objectWithinRing( obj, _ringWorld.getRadius()))
				_ringWorld.attach( obj );
		}		
	}
	
	
	//----------------------------------------------------------------------
	//	foo -- determine if an object with radius is within the ring w/o 
	//	using sqrt().
	//
	//	a^2 + b^2 = c^2 where obj.pos = (a, b)
	//	We want: ring.radius < obj.pos + obj.radius
	//	obj.pos > ring.radius - obj.radius
	//	obj.pos ^ 2 < (ring.radius - obj.radius) ^ 2
	//	c^2 < (ring.radius - obj.radius) ^ 2
	//----------------------------------------------------------------------
	boolean objectWithinRing( NewtonianObject obj, double ringRadius )
	{
		Vector2D p = obj.getPosition();
		double r = obj.getRadius();
		
		return (p.x*p.x + p.y*p.y) < (ringRadius - r) * (ringRadius - r);
	}
			
	boolean pointsCloserThanDistace( Vector2D a, Vector2D b, float inDistance )
	{
		return inDistance*inDistance > 
			(b.x-a.x)*(b.x-a.x) + (b.y-a.y)*(b.y-a.y);
	}

}	// RingWorldPhysics
