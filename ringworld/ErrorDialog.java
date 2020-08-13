//----------------------------------------------------------------------
//  (c) 1999 David Whitney.    This software may be used or modified in any way, 
//  provided that this copyright notice appears on all copies of the software.
//----------------------------------------------------------------------

import java.awt.*;
import java.awt.event.*;

//----------------------------------------------------------------------
//	ErrorDialog
/**
 *	Pops up error messages with 'ok' button.
 *
 *	@author		<a href="mailto:dwhitney@cs.stanford.edu">David Whitney</a>
 *	@version	April 28, 1999
 */
//
//	Lines commented out with '///' are JDK 1.1
//----------------------------------------------------------------------

public class ErrorDialog
{
	Window		_errorPopup;		// error dialog Window
	Label		_errorText;
	
	//----------------------------------------------------------------------
	//	only works if we're a Frame
	//----------------------------------------------------------------------
/*
	static 		ErrorDialog	sPopup = new ErrorDialog("D'ough!");
		
	static void showError( String inErrStr )
	{
		sPopup.popupError( inErrStr );
	}
*/			
	//----------------------------------------------------------------------
	//	Constructors -- build frame with label and OK button.
	//----------------------------------------------------------------------
	public ErrorDialog( Frame inParent ) {
		this( inParent, "Shucks" );
	}
	
	public ErrorDialog( Frame inParent, String inOKStr )
	{
		_errorPopup = new Frame("Error");
	//	_errorPopup = new Window( inParent );
		_errorPopup.setLayout( new BorderLayout() );

		_errorText = new Label("No error.");
		Button ok = new Button( inOKStr );
		
		_errorPopup.add( _errorText );
		
		Panel p = new Panel();
		p.add( ok, "Center");
		_errorPopup.add( p, "South");
		//_errorPopup.add( new Label(""), "South");	// to shrink the button
		
		class WindowCloser	extends WindowAdapter 
							implements ActionListener, KeyListener
		{
			public void actionPerformed( ActionEvent inEvent ) {
				_errorPopup.setVisible( false );
			}
			public void windowClosing(WindowEvent e) {
				_errorPopup.setVisible( false );
			}
  			public void keyTyped(KeyEvent e) {
 				if (e.getKeyCode() == KeyEvent.VK_ENTER)
					_errorPopup.setVisible( false );
  			}
			public void keyPressed(KeyEvent e) {}
 			public void keyReleased(KeyEvent e) {}
		};
		WindowCloser closeWatcher = new WindowCloser();
		
		/*
		WindowAdapter closeWatcher = new WindowAdapter() implements ActionListener {
			public void actionPerformed( ActionEvent inEvent ) {
				_errorPopup.setVisible( false );
			}
			public void windowClosing(WindowEvent e) {
				_errorPopup.setVisible( false );
			}
		};
		*/
		_errorPopup.addWindowListener( closeWatcher );
		_errorPopup.addKeyListener( closeWatcher );
		ok.addActionListener( closeWatcher );
	}
	
	//----------------------------------------------------------------------
	//	display -- bring window up and display error message.
	//----------------------------------------------------------------------
	public void display( String inErrStr )
	{
		_errorText.setText( inErrStr );
		
		_errorPopup.pack();
		_errorPopup.show();
		
		System.err.println( inErrStr );
	}


}	// class ErrorDialog
