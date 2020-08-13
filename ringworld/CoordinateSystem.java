//----------------------------------------------------------------------
//	CoordinateSystem
/**
 *	An object to do the 2-D matrix math of rotating, translating, and scaling
 *	points in a graphics system.
 *	Methods are synchronized to avoid the matrix getting changed in the
 *	process of doing a transform.
 * 
 *	@author		<a href="mailto:dwhitney@cs.stanford.edu">David Whitney</a>
 *	@version	April 27, 1999
 */
//----------------------------------------------------------------------

public class CoordinateSystem
{
	Matrix33		_matrix;
	Matrix33		_inverseMatrix;
	boolean			_cachedInverse;		// whether inverse is valid
	
	public CoordinateSystem()
	{
		_matrix = new Matrix33();
		_inverseMatrix = null;
		_cachedInverse = false;
	}	
	
	//----------------------------------------------------------------------
	//	transformations
	//----------------------------------------------------------------------
	public synchronized void transformVec( Vector2D inPt, Vector2D outPt )
	{
		_matrix.postMultVec( inPt, outPt, 1.0 );
	}

	public synchronized void inverseTransformVec( Vector2D inPt, Vector2D outPt )
	{
		if (_cachedInverse == false) {
			_inverseMatrix = _matrix.invert();
			_cachedInverse = true;
		}
		_inverseMatrix.postMultVec( inPt, outPt, getScale() );	// add w back in
	}

	public synchronized void rotateVec( Vector2D inPt, Vector2D outPt )
	{
		_matrix.postMultVec_NoTranslate( inPt, outPt );
	}
	
	public synchronized void inverseRotateVec( Vector2D inPt, Vector2D outPt )
	{
		if (_cachedInverse == false) {
			_inverseMatrix = _matrix.invert();
			_cachedInverse = true;
		}
		_inverseMatrix.postMultVec_NoTranslate( inPt, outPt );
	}
	
	//----------------------------------------------------------------------
	//	Accessors
	//----------------------------------------------------------------------
	public String	toString()			{ return _matrix.toString() + _inverseMatrix.toString() ; }
	public double	getScale()			{ return _matrix.get( 2, 2 ); }

	//----------------------------------------------------------------------
	//	Coordinate transforms
	//----------------------------------------------------------------------
	public void reset()							{ _matrix.setIdentity();	_cachedInverse = false; } 
	public void	rotate( double angle )			{ _matrix.rotate( angle );	_cachedInverse = false;  }
	public void	translate( double x, double y )	{ _matrix.translate( x, y );_cachedInverse = false; }
	public void	scale( double s )				{ _matrix.scale( s );		_cachedInverse = false; }
	
	
	//----------------------------------------------------------------------
	//	test the class
	//----------------------------------------------------------------------
	public void test()
	{
/*
		Vector2D v1 = new Vector2D( 0, 0 );
		Vector2D v2 = new Vector2D( 0, 0 );
		
		System.out.println( "Identity:\n" + _matrix );

		_matrix.translate( 5, 3 );
		transformVec( v1, v2 );
		System.out.println( "Trans (5 0):\n" + _matrix + "vt = " + v2 + "\n");

		_matrix.rotate( Math.PI/2 );
		transformVec( v1, v2 );
		System.out.println( "Rot 90:\n" + _matrix + "vr = " + v2 + "\n");
*/
		
		Matrix33 m = new Matrix33();
		m.translate( 20, 20 );
		m.scale( 0.5 );
		Matrix33 inv = m.invert();
		System.out.println( m );
		System.out.println( inv );
		m.preMult( inv );
		System.out.println( m );

		try { Thread.sleep(20000); } catch (Exception ex) {}
	}
	
	public static void main( String args[] )
	{
		CoordinateSystem c = new CoordinateSystem();
		c.test();
	}
}	// CoordinateSystem



//----------------------------------------------------------------------
//----------------------------------------------------------------------
//		3x3 Matrix
//----------------------------------------------------------------------
//----------------------------------------------------------------------

class Matrix33
{
	double[][]					_matrix;
	static final double[][] 	sIdentity ={{ 1.0, 0.0, 0.0 },
											{ 0.0, 1.0, 0.0 },
											{ 0.0, 0.0, 1.0 }};
	public Matrix33()
	{
		_matrix = new double[3][3];
		setIdentity();
	}
																
	public double get(int i, int j)		{ return _matrix[i][j]; }
	public void setIdentity()			{ set( sIdentity );}
	
	//----------------------------------------------------------------------
	//	transformations - all premultiplied
	//----------------------------------------------------------------------
	public void	rotate( double angle )
	{
		Matrix33	rot = new Matrix33();

		rot._matrix[0][0] =  Math.cos( angle );
		rot._matrix[1][0] = -Math.sin( angle );
		rot._matrix[0][1] =  Math.sin( angle );
		rot._matrix[1][1] =  Math.cos( angle );
		
		// System.out.println( "Rot:\n" + rot );
		preMult( rot );
	}

	public void	translate( double x, double y )
	{
		Matrix33	trans = new Matrix33();

		trans._matrix[2][0] = x;
		trans._matrix[2][1] = y;

		// System.out.println( "Trans:\n" + trans );
		preMult( trans );
	}
	
	public void	scale( double s )
	{
		/*
		Matrix33	scale = new Matrix33();
		scale._matrix[0][0] = s;
		scale._matrix[1][1] = s;
		scale._matrix[2][2] = s;
		preMult( scale );
		*/
		// same thing only quicker
		for (int i = 0; i < 3; i++)
			for (int j = 0; j < 3; j++)
				_matrix[i][j] *= s;	
	}

	//----------------------------------------------------------------------
	//	invert -- returns inverse of matrix.  Scary black magic linear algebra.
	//----------------------------------------------------------------------
	public Matrix33	invert()
	{
		Matrix33	inverse = new Matrix33();

		double d01d12md11d02 = _matrix[0][1] * _matrix[1][2] - 
							   _matrix[1][1] * _matrix[0][2];
		double d01d22md21d02 = _matrix[0][1] * _matrix[2][2] - 
							   _matrix[2][1] * _matrix[0][2];
		double d11d22md21d12 = _matrix[1][1] * _matrix[2][2] - 
							   _matrix[2][1] * _matrix[1][2];
		
		double determinant =	_matrix[0][0] * d11d22md21d12 -
								_matrix[1][0] * d01d22md21d02 +
								_matrix[2][0] * d01d12md11d02;
						
		if (determinant == 0d)	// inverse of zero matrix is zero matrix?
			return inverse;		
			
		double invDet = 1.0 / determinant;	// multiply is faster than divide.
		
		inverse._matrix[0][0] =  d11d22md21d12 * invDet;
		inverse._matrix[0][1] = -d01d22md21d02 * invDet;
		inverse._matrix[0][2] =  d01d12md11d02 * invDet;
		
		inverse._matrix[1][0] = (_matrix[2][0] * _matrix[1][2] - 
								 _matrix[1][0] * _matrix[2][2]) * invDet;
		inverse._matrix[1][1] = (_matrix[0][0] * _matrix[2][2] - 
								 _matrix[2][0] * _matrix[0][2]) * invDet;
		inverse._matrix[1][2] = (_matrix[1][0] * _matrix[0][2] - 
								 _matrix[0][0] * _matrix[1][2]) * invDet;

		inverse._matrix[2][0] = (_matrix[1][0] * _matrix[2][1] - 
								 _matrix[2][0] * _matrix[1][1]) * invDet;
		inverse._matrix[2][1] = (_matrix[2][0] * _matrix[0][1] - 
								 _matrix[0][0] * _matrix[2][1]) * invDet;
		inverse._matrix[2][2] = (_matrix[0][0] * _matrix[1][1] -
								 _matrix[1][0] * _matrix[0][1]) * invDet;
							
		return inverse;
	}

	//----------------------------------------------------------------------
	//	Accessor routines
	//----------------------------------------------------------------------
	public void set( Matrix33 src )
	{
		for (int i = 0; i < 3; i++)
			for (int j = 0; j < 3; j++)
				_matrix[i][j] = src._matrix[i][j];	
	}
	
	public void set( double[][] mat )
	{
		for (int i = 0; i < 3; i++)
			for (int j = 0; j < 3; j++)
				_matrix[i][j] = mat[i][j];	
	}	

	//----------------------------------------------------------------------
	//	post multiply  this * M
	//----------------------------------------------------------------------
	public void postMult( Matrix33 m )
	{
		Matrix33 temp = new Matrix33();
		
		for (int i = 0; i < 3; i++)
			for (int j = 0; j < 3; j++)
				temp._matrix[i][j] = _matrix[0][j] * m._matrix[i][0] +
									 _matrix[1][j] * m._matrix[i][1] +
									 _matrix[2][j] * m._matrix[i][2];	
		set( temp );
	}	

	//----------------------------------------------------------------------
	//	pre multiply   M * this
	//----------------------------------------------------------------------
	public void preMult( Matrix33 m )
	{
		Matrix33 temp = new Matrix33();
		
		for (int i = 0; i < 3; i++)
			for (int j = 0; j < 3; j++)
				temp._matrix[i][j] = m._matrix[0][j] * _matrix[i][0] +
									 m._matrix[1][j] * _matrix[i][1] +
									 m._matrix[2][j] * _matrix[i][2];
		set( temp );
	}	

	//----------------------------------------------------------------------
	//	pree multiply	v * M
	//----------------------------------------------------------------------
	public void preMultVec( Vector2D inSrc, Vector2D outDest, double w )
	{
		outDest.x =	inSrc.x * _matrix[0][0] +
					inSrc.y * _matrix[0][1] +
						  w * _matrix[0][2];
		outDest.y =	inSrc.x * _matrix[1][0] +
					inSrc.y * _matrix[1][1] +
						  w * _matrix[1][2];										
	}	


	//----------------------------------------------------------------------
	//	post multiply	M * v
	//----------------------------------------------------------------------
	public void postMultVec( Vector2D inSrc, Vector2D outDest, double w )
	{
		outDest.x =	_matrix[0][0] * inSrc.x +
					_matrix[1][0] * inSrc.y +
					_matrix[2][0] * w;
		outDest.y =	_matrix[0][1] * inSrc.x +
					_matrix[1][1] * inSrc.y +
					_matrix[2][1] * w;	
	}	

	//----------------------------------------------------------------------
	//	post multiply	M * v
	//----------------------------------------------------------------------
	public void postMultVec_NoTranslate( Vector2D inSrc, Vector2D outDest )
	{
		outDest.x =	_matrix[0][0] * inSrc.x +
					_matrix[1][0] * inSrc.y;
		outDest.y =	_matrix[0][1] * inSrc.x +
					_matrix[1][1] * inSrc.y;
	}	

	//----------------------------------------------------------------------
	//	print
	//----------------------------------------------------------------------
	public String toString()
	{
		StringBuffer buf = new StringBuffer();
		
		for (int j = 0; j < 3; j++) {
			for (int i = 0; i < 3; i++)
				buf.append( _matrix[i][j] + "  " );
			buf.append("\n");
		}
		buf.append("\n");
	
		return buf.toString();	
	}



}		// Matrix33
