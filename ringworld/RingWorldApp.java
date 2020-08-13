import java.awt.*;
import java.awt.event.*;
import java.util.*;

//----------------------------------------------------------------------
//	RingWorldApp
/**
 *	A simulation of a rotating space colony in a weightless environment, 
 *	like those of David Niven's 'RingWorld' or Arthur C. Clark's 'Rama' 
 * 
 *	@author		<a href="mailto:dwhitney@cs.stanford.edu">David Whitney</a>
 *	@version	April 28, 1999
 */
//----------------------------------------------------------------------

public class RingWorldApp implements Runnable, ActionListener
{
	RingWorld			_ringWorld;
	RingWorldPhysics	_physics;
	Frame				_appFrame;
	RingWorldGraphics	_view;
	DialogBox			_errorPopup;

	Thread				_physicsThread, _graphicsThread;	// multi thread
	Thread				_mainThread;						// single thread
	boolean				_multiThreaded;
	boolean				_paused;
	boolean				_running;

	PublicObservable	_pauseState;	// hack instead of extending from Observable

	public final static	String FILE_OPEN =		"Open...";
	public final static	String FILE_SAVE =		"Save...";
	public final static	String FILE_GET_INFO =	"Get Info";
	public final static	String FILE_QUIT =		"Quit";

	//----------------------------------------------------------------------
	//----------------------------------------------------------------------
	public static void main( String args[] )
	{
 		RingWorldApp rw = new RingWorldApp();

//		while (true) {
//			try { Thread.sleep( 5000 ); }
//			catch (InterruptedException ex) { }
// 		}
  	}	

	//----------------------------------------------------------------------
	//	Create everything
	//----------------------------------------------------------------------
	RingWorldApp()
	{
		_ringWorld = new RingWorld();
		_physics = new RingWorldPhysics( _ringWorld );
		
		_paused = true;
		_multiThreaded = false;

		_pauseState = new PublicObservable();		// paused state

		configureRingWorld();
		buildGUI();
		
		// crank up the world
 		new Thread( this ).start();
 		
 		// now, will the spawning thread continue and do GUI 
 		// interaction for us?  JVM does this?
	}
	
	
	//----------------------------------------------------------------------
	//	Accessors
	//----------------------------------------------------------------------
 	public void addObserver(Observer inObserver) {
 		_pauseState.addObserver( inObserver ); 
 	}
		
	//----------------------------------------------------------------------
	//	pause physical world, but keep drawing
	//----------------------------------------------------------------------
	void togglePause()
	{
		if (_multiThreaded) {
			if (_paused)
				_physicsThread.resume();
			else
				_physicsThread.suspend();
		}
		_paused = !_paused;
		
		_pauseState.setChanged();
		_pauseState.notifyObservers( new Boolean( _paused ));
	}

	//----------------------------------------------------------------------
	//	buildGUI - create all interface elements.
	//----------------------------------------------------------------------
	void buildGUI()
	{
		_view = new RingWorldGraphics( _ringWorld, _physics );
		_view.setSize( 480, 400 );

		RWCommander commandHandler = new RWCommander( _ringWorld, _view, _physics );

		_appFrame = new Frame("RingWorld");
		
		RWButtonPanel controls = 
			new RWButtonPanel( _ringWorld, _physics, commandHandler );
		
		_appFrame.setLayout( new BorderLayout() );
		_appFrame.add( _view, "Center" );
		_appFrame.add( controls, "South");
		//_appFrame.add( makeTitle(), "North");
		//_appFrame.add( buildAppletButtonPanel( commandHandler ), "West");
		_appFrame.setMenuBar( makeMenus( commandHandler ));

		_view.setBackground( _appFrame.getBackground() );

		_appFrame.addWindowListener( new WindowAdapter() {
			public void windowClosing(WindowEvent e) { quit(); } 
		} );
	
		_errorPopup = new DialogBox("D'ough!");
		
		_appFrame.pack();	// this doesn't send a resize event to _view ???
		_appFrame.show();
	}
	
	//----------------------------------------------------------------------
	//	configureRingWorld -- see if any paramters were set, set attributes
	//	and populate the ringworld.
	//----------------------------------------------------------------------
	void configureRingWorld()
	{
		double radius = 150;
		double gravity = 1;
		
		_ringWorld.setRadiusAndGravity( radius, gravity );
		//_ringWorld.setTimeAndGravity( 108000, .992 );	// Niven's Ringworld
		//_ringWorld.setRadiusAndGravity( 1.5e11, .992 );	// Niven's Ringworld
		
		// Niven's Ringworld was 1.5e11 m in radius (1 AU) and rotated at
		// 770 miles/s (1.2e6 m/s) with gravity of .992g and a 30 hour rotation
		
		NewtonianObject ball1 = new NewtonianObject("Ball1", 10);
		NewtonianObject ball2 = new NewtonianObject("Ball2", 5);
		_ringWorld.addAtAltitude( ball1, _ringWorld.getRadius()/3 );
		_ringWorld.addAtAltitude( ball2, _ringWorld.getRadius()/2 );

		System.out.println("RingWorld:\n" + _ringWorld );
	}
	
	//----------------------------------------------------------------------
	//	startThreads
	//----------------------------------------------------------------------
	void startPhysicsAndGraphicsThreads()
	{
		System.out.println("Multi threaded version starting...");
		
		_physicsThread = new Thread( _physics, "Physics" );
		_graphicsThread = new Thread( _view, "Graphics" );
		_physicsThread.start();
		_graphicsThread.start();
	}
	
	//----------------------------------------------------------------------
	//	main loop
	//----------------------------------------------------------------------
	public void run()
	{
		if (_multiThreaded) {
			startPhysicsAndGraphicsThreads();
			return;
		}
		
		_running = true;
		_paused = false;
		
		while (_running) {
			if (!_paused)
				_physics.doPhysics();
			_view.repaint();

			// pause for life to go by.
			try { Thread.sleep( _physics.getSleepTime() ); }
			catch (InterruptedException ex) {
				System.out.println("Sleep was interrupted!  Windows is evil!");
			}
		}	
	}
	
	//----------------------------------------------------------------------
	//	Menus
	//----------------------------------------------------------------------
	MenuBar makeMenus( ActionListener inCommander )
	{
		MenuBar menubar = new MenuBar();
		MenuItem item;
		
		Menu file = new Menu("File");
		file.add( newMenuItem( FILE_OPEN, KeyEvent.VK_O, this));
		file.add( newMenuItem( FILE_SAVE, KeyEvent.VK_S, this));
		file.add( newMenuItem( FILE_GET_INFO,
								RWCommander.CMD_SHOW_STATS, 
								KeyEvent.VK_I, inCommander ));
		file.addSeparator();
		file.add( newMenuItem( FILE_QUIT, KeyEvent.VK_Q, this));
		file.getItem(0).setEnabled( false );
		file.getItem(1).setEnabled( false );
			
		Menu edit = new Menu("Edit");
		edit.add( newMenuItem("Undo", KeyEvent.VK_Z, this ));
		edit.addSeparator();
		edit.add( newMenuItem("Cut", KeyEvent.VK_X, this ));
		edit.add( newMenuItem("Copy", KeyEvent.VK_C, this ));
		edit.add( newMenuItem("Paste",KeyEvent.VK_V, this ));
		edit.getItem(0).setEnabled( false );
		edit.getItem(2).setEnabled( false );
		edit.getItem(3).setEnabled( false );
		edit.getItem(4).setEnabled( false );

		Menu view = new Menu("View");
		view.add( newMenuItem(	RWCommander.CMD_VIEWER_LOCAL, 
								RWCommander.CMD_VIEWER_LOCALE, 
								KeyEvent.VK_V, inCommander ));
//		view.add( newMenuItem( RWCommander.CMD_VIEWER_LOCAL, 
//							   KeyEvent.VK_I,inCommander ));
		view.addSeparator();
		view.add( newMenuItem( RWCommander.CMD_ZOOM_IN, 
							   KeyEvent.VK_EQUALS, inCommander ));
		view.add( newMenuItem( RWCommander.CMD_ZOOM_OUT, 
							   KeyEvent.VK_SUBTRACT, inCommander ));
		view.addSeparator();
		view.add( newMenuItem( "Draw Velocity Vectors",
								RWCommander.CMD_DRAW_VECTORS, 
								KeyEvent.VK_D, inCommander ));
		view.add( newMenuItem( RWCommander.CMD_PAUSE, 
							   KeyEvent.VK_P, this ));

		Menu help = new Menu("Help");
		help.add( newMenuItem("About RingWorld", this ));
		help.add( newMenuItem("Tough noogies", this ));
		help.getItem(0).setEnabled( false );
		
		menubar.add( file );
		menubar.add( edit );
		menubar.add( view );
		menubar.add( help );
		menubar.setHelpMenu( help );
		
		return menubar;
	}
	
	
	//----------------------------------------------------------------------
	//	MenuItem creation utilities.
	//----------------------------------------------------------------------
	MenuItem newMenuItem( String inLabel, String inCommand,
						  int inShortcutKey, ActionListener inListener )
	{
		MenuItem item = new MenuItem( inLabel, new MenuShortcut( inShortcutKey ));
		if (inCommand != null)
			item.setActionCommand( inCommand );
		else
			item.setActionCommand( inLabel );
		item.addActionListener( inListener );
		return item;
	}

	MenuItem newMenuItem( String inLabel, 
						  int inShortcutKey, ActionListener inListener )	
	{
		return newMenuItem( inLabel, null, inShortcutKey, inListener );
	}
	
	MenuItem newMenuItem( String inLabel, ActionListener inListener )	
	{
		MenuItem item = new MenuItem( inLabel );
		item.addActionListener( inListener );
		return item;
	}
	
	
	
	//----------------------------------------------------------------------
	//	quit -- kill threads, windows, exit
	//----------------------------------------------------------------------
	void quit()
	{
		if (_multiThreaded) {
			_physicsThread.stop();
			_graphicsThread.stop();
		}
		
		_running = false;
		_appFrame.dispose();
				
		//Thread.currentThread().stop();	// this would kill VM
	}

	//----------------------------------------------------------------------
	//	Handle commands.
	//----------------------------------------------------------------------
	public void actionPerformed( ActionEvent inEvent )
	{
		String cmd = inEvent.getActionCommand();

		if (cmd.equals( FILE_QUIT )) {
			quit();
		}
		
		else if (cmd.equals( RWCommander.CMD_PAUSE )) {
			togglePause();
			if (inEvent.getSource() instanceof MenuItem) {
			 	MenuItem m = (MenuItem) inEvent.getSource();
				if (_paused)
					m.setLabel("Resume (P)");
				else
					m.setLabel( RWCommander.CMD_PAUSE );
			}
		}	
		
		else
			_errorPopup.popup("Sorry, " + cmd + " Not Implemented");
	}
	
		
} 	// RingWorldApp
