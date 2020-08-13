//----------------------------------------------------------------------
//	Assert
/**
 *	Generic assertion class, throws exception when assertion condition is false.
 *
 *	@author		<a href="mailto:dwhitney@cs.stanford.edu">David Whitney</a>
 *	@version	May 20, 1999
 */
//----------------------------------------------------------------------
public class Assert
{
	// What's the difference between an Error and a RuntimeExcpetion?
	// Both Throwable and need not be caught.
	// Error - abnormal conditions that should never occur. Does not stop world

	static public final void condition( boolean inAssertion) {
		if (!inAssertion)
			throw new Error("<<<Assertion Failed>>>");
//			throw new RuntimeException("<<<Assertion Failed>>>");
	}
	static public final void assert( boolean inAssertion) {
		if (!inAssertion)
			throw new Error("<<<Assertion Failed>>>");
	}
}