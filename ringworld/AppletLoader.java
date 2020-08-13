//----------------------------------------------------------------------
//  (c) 1999 David Whitney.    This software may be used or modified in any way, 
//  provided that this copyright notice appears on all copies of the software.
//----------------------------------------------------------------------

import java.applet.Applet;
import java.awt.*;

//----------------------------------------------------------------------
//	AppletLoader
/**
 *	Draws a progress bar with a title over it left justified.
 *
 *	@author		<a href="mailto:dwhitney@cs.stanford.edu">David Whitney</a>
 *	@version	June 1, 1999
 */
//----------------------------------------------------------------------
public class AppletLoader extends Applet
{
	Applet		_newApplet;
	ProgressBar	_progress;
	
	public static void main( String args[] ) {
		AppletLoader a = new AppletLoader();
		Frame f = new Frame("blah");
		a.init();
		a.start();
		f.add( a );
		f.pack();
		f.show();
	}
	
	public AppletLoader()
	{
		_newApplet = null;
		_progress = new ProgressBar("Loading Applet. Patience dude....");
	}
	
	public void setProgress( float inProgress ) {
		_progress.setProgress( inProgress );
	}
	
	public void setAppletLoaded()
	{
		remove( _progress );
		add( _newApplet );		// place applet GUI in Applet Panel
		_newApplet.start();
	}
	
	public void init()
	{
	//	setLayout( new GridBagLayout() );
		setLayout( new BorderLayout() );
		add( _progress );
		
		Thread t = new Thread( new Runnable() {
			public void run() {
				for (int i = 0; i <= 10; i++) {
					_progress.setProgress( i / 10f );
					try { Thread.sleep( 500 ); }
					catch (InterruptedException x ) { }
				}
			}
		} );
		t.start();
	}
	
	public void start()
	{
		if (_newApplet != null)
			_newApplet.start();	
		else {	// new thread?
			_newApplet = new RingWorldApplet();
			((AppletLoaderUser)_newApplet).addAppletLoader( this );
			_newApplet.init();
		}
	}
	public void stop()
	{
		if (_newApplet != null)
			_newApplet.stop();	
	}
	public void destroy()
	{
		if (_newApplet != null)
			_newApplet.destroy();	
	}
	
	
}	// class AppletLoader
