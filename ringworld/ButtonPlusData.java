import java.awt.*;
import java.awt.event.*;

//----------------------------------------------------------------------
//	ButtonPlusData.java
/**
 *	An AWT Button with a data field attached.
 *
 *	@author		<a href="mailto:dwhitney@cs.stanford.edu">David Whitney</a>
 *	@version	May 15, 1999
 */
//----------------------------------------------------------------------
public class ButtonPlusData	extends Button
{
	/** Anything the user wants to attach to this object */
	public Object	userData;
		
	public ButtonPlusData()					{ super(); }
	public ButtonPlusData(String inString)	{ super(inString); }


	//----------------------------------------------------------------------
	//	This is what I'd like to do.  Attach the action directly to the event.
	//----------------------------------------------------------------------
	/*
	public void processActionEvent(ActionEvent e) 
	{
		e.setData( myAction );
	}         
    */
         
	//----------------------------------------------------------------------
	//	Utility routine to build a useful button.
	//----------------------------------------------------------------------
	public static ButtonPlusData	newButton(	String inCommand, 
												Container inContainer,
												ActionListener inListener )
	{
		ButtonPlusData b = new ButtonPlusData( inCommand );
		b.setActionCommand( inCommand );
		b.addActionListener( inListener );
		inContainer.add( b );		
		return b;
	}

}	// class ButtonPlusData
