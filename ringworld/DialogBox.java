import java.awt.*;
import java.awt.event.*;

//----------------------------------------------------------------------
//	DialogBox
/**
 *	Pops up messages with 'ok' button.  Could be error, info, or warning, etc.
 *
 *	@author		<a href="mailto:dwhitney@cs.stanford.edu">David Whitney</a>
 *	@version	April 28, 1999
 */
//----------------------------------------------------------------------

public class DialogBox
{
	Window		_popup;		// dialog Window
	TextArea	_text;
	Button		_ok;
		
	//----------------------------------------------------------------------
	//	provide simple interface to make a dialog.
	//----------------------------------------------------------------------
	static 		DialogBox	sDialog = new DialogBox("Dialog", "D'ough!");
		
	static void showText( String inStr )
	{
		sDialog.popup( inStr );
	}
			
	//----------------------------------------------------------------------
	//	Constructors -- build frame with label and OK button.
	//----------------------------------------------------------------------
	public DialogBox() 					{  this( "Dialog", "D'ough!" ); }
	public DialogBox( String inTitle )	{  this( inTitle, "D'ough!" ); }
	
	public DialogBox( String inTitle, String inOKStr )
	{
		_popup = new Frame( inTitle );
		_text = new TextArea("Woohoo!");
		_ok = new Button( inOKStr );
		
		_text.setEditable( false );
		
		_popup.setLayout( new BorderLayout() );
		_popup.add( _text );
		
		Panel p = new Panel();
		p.add( _ok, "Center");
		_popup.add( p, "South");
		//_popup.add( new Label(""), "South");	// to shrink the button
		
		manageWindowClosing();
	}

	//----------------------------------------------------------------------
	//	popup -- bring window up and display message, hide it if already up.
	//----------------------------------------------------------------------
	public void popup()
	{
		if (_popup.isVisible())
			_popup.setVisible( false );
		else 
			_popup.show();
	}
	
	public void popup( String inText )
	{
		setText( inText );
		_popup.show();		// if there's new text, keep window up.
	}

	//----------------------------------------------------------------------
	//	setText -- set message without displaying it yet.
	//----------------------------------------------------------------------
	public void setText( String inText )
	{
		_text.setText( inText );
		_popup.pack();
	}

	//----------------------------------------------------------------------
	//	manageWindowClosing -- Do what it takes to enable user to close this
	//	window.  Clicking OK, pressing return, clicking  close box....
	//----------------------------------------------------------------------
	void manageWindowClosing()
	{
		// inner class to listen for events to that should close the window
		class WindowCloser	extends WindowAdapter 
							implements ActionListener, KeyListener
		{
			public void windowClosing(WindowEvent e) {
				_popup.setVisible( false );
			}
			public void actionPerformed( ActionEvent inEvent ) {
				_popup.setVisible( false );
			}
  			public void keyPressed(KeyEvent e) {
 				if (e.getKeyCode() == KeyEvent.VK_ENTER)
					_popup.setVisible( false );
	//			KeyCodes generated only in KeyPressed/KeyReleased
 	//			if (e.getKeyChar() == '\n') {
	//				_popup.setVisible( false );
	//			}
  			}
			public void keyTyped(KeyEvent e) {}
 			public void keyReleased(KeyEvent e) {}
		};
		
		WindowCloser closeWatcher = new WindowCloser();
		
		_popup.addWindowListener( closeWatcher );
		_popup.addKeyListener( closeWatcher );
		_text.addKeyListener( closeWatcher );
		_ok.addKeyListener( closeWatcher );
		_ok.addActionListener( closeWatcher );
	}	
	

}	// class DialogBox
