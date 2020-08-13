import java.awt.Point;

//----------------------------------------------------------------------
//	Vector2D
/**
 *	A simple (x, y) mathematical vector.  Threadsafe.
 * 
 *	@author		<a href="mailto:dwhitney@cs.stanford.edu">David Whitney</a>
 *	@version	April 27, 1999
 */
//----------------------------------------------------------------------

public class Vector2D implements Cloneable
{
	public double	x, y;
	
	public Vector2D( double inX, double inY)
	{
		x = inX;
		y = inY;
	}
	public Vector2D( Vector2D v )
	{
		this( v.x, v.y );
	}
	
	public Vector2D( Point v )
	{
		this( v.x, v.y );
	}
	
	public String toString()
	{
		return "(" + x + ", " + y + ")";
	}
	
	//----------------------------------------------------------------------
	//	calcAngle -- determine the cylindrical coordinates of this point.
	//	(theta, r)
	//----------------------------------------------------------------------
	public synchronized double calcAngle()
	{
		double angle;
		Assert.condition( !Double.isNaN( x ) );
		Assert.condition( !Double.isNaN( y ) );
		
		if (x == 0)				// ensure no divide by zero
			angle = (y > 0) ? 	Math.PI / 2:
							3 * Math.PI / 2;
		else	
			angle = Math.atan( y / x );

		if (x < 0)			// correct for other hemisphere
			angle += Math.PI;
		
		Assert.condition( !Double.isNaN( angle ) );
		
		return angle;	
	}

	public synchronized double calcRadius()
	{
		return Math.sqrt( x*x + y*y );
	}
	
	//----------------------------------------------------------------------
	//	Accessors
	//----------------------------------------------------------------------
	
	/** converts from cylindrical coordinates (radius, angle in radians) */
	public synchronized void setRadiusAngle( double inRadius, double inAngle )
	{
		x = inRadius * Math.cos( inAngle );
		y = inRadius * Math.sin( inAngle );
	}

	public synchronized void set( double inX, double inY )
	{
		x = inX;
		y = inY;
	}

	public synchronized void add( double xd, double yd )
	{
		x += xd;
		y += yd;
	}

	public synchronized void add( Vector2D v )
	{
		x += v.x;
		y += v.y;
	}
	
	public synchronized void sub( Vector2D v )
	{
		x -= v.x;
		y -= v.y;
	}
	
	public synchronized void scale( double s )
	{
		x *= s;
		y *= s;
	}
	
	/** Rotates vector by 'a' radians. */
	public synchronized void rotate( double a )
	{
		double tx = x;
		x = tx *  Math.cos( a ) + y * Math.sin( a );
		y = tx * -Math.sin( a ) + y * Math.cos( a );
	}

	/** Transforms vector by given matrix. e.g. World coords to screen coords. */
	public synchronized Vector2D transform( CoordinateSystem inTransform ) 
	{
		Vector2D outPt = new Vector2D( 0, 0 );
		inTransform.transformVec( this, outPt );
		return outPt;
	}

	/** Transforms vector by given matrix. e.g. Screen coords to world coords. */
	public synchronized Vector2D inverseTransform( CoordinateSystem inTransform ) 
	{
		Vector2D outPt = new Vector2D( 0, 0 );
		inTransform.inverseTransformVec( this, outPt );
		return outPt;
	}

	/** Rotates vector by given matrix. */
	public synchronized Vector2D rotate( CoordinateSystem inTransform ) 
	{
		Vector2D outPt = new Vector2D( 0, 0 );
		inTransform.rotateVec( this, outPt );
		return outPt;
	}

	/** Unrotates vector by given matrix. */
	public synchronized Vector2D inverseRotate( CoordinateSystem inTransform ) 
	{
		Vector2D outPt = new Vector2D( 0, 0 );
		inTransform.inverseRotateVec( this, outPt );
		return outPt;
	}

}	// Vector2D

