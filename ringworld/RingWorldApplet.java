import java.applet.Applet;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

//----------------------------------------------------------------------
//	RingWorldApplet
/**
 *	A simulation of a rotating space colony in a weightless environment, 
 *	like those of Larry Niven's 'RingWorld' or Arthur C. Clark's 'Rama' 
 * 
 *	@author		<a href="mailto:dwhitney@cs.stanford.edu">David Whitney</a>
 *	@version	April 28, 1999
 */
//----------------------------------------------------------------------

public class RingWorldApplet extends Applet 
							implements	Runnable, ActionListener, KeyListener
										// , AppletLoaderUser
{
	RingWorld			_ringWorld;
	RingWorldPhysics	_physics;
	boolean				_paused;
	RingWorldGraphics	_canvas;
	Thread				_physicsThread, _graphicsThread;	// multi thread
	Thread				_mainThread;						// single thread
	boolean				_multiThreaded;
	boolean				_ownWindow;			// run in it's own window?
	
	PublicObservable	_pauseState;	// hack instead of extending from Observable
	boolean				_appletStopped;	// security hack, I think.
	
	// stuff to display message while loading
//	AppletLoader		_loader;
	boolean				_initialized;
	ProgressBar			_progress;
	int					_windowsSucks;	// check for Win9x bug in System.time
	
	static public final int	MAJOR_VERSION = 1;
	static public final int	MINOR_VERSION = 0;
	
	//----------------------------------------------------------------------
	//	constructor -- build objects that must be configured in init().
	//----------------------------------------------------------------------
	public RingWorldApplet()
	{
		_paused = true;
		_multiThreaded = false;
		_appletStopped = false;
		_initialized = false;
		
		System.out.println( "\n" + getAppletInfo() + "\n");
		
// 		String envfonts[] = Toolkit.getDefaultToolkit().getFontList();
// 		for ( int i = 1; i < envfonts.length; i++ )
// 			System.out.println( envfonts[i] );
 
		// Helvetica == SansSerif (1.1)
		setBackground( Color.white );		// set defaults
// 		Font f = new Font("SansSerif", Font.PLAIN, 12);		
// 		if (f != null)
// 			setFont( f );
	}

	//----------------------------------------------------------------------
	//	Accesors
	//----------------------------------------------------------------------
	public RingWorld			getWorld()			{ return _ringWorld; }
	public RingWorldPhysics		getPhysics()		{ return _physics; }
	public RingWorldGraphics	getGraphicalView()	{ return _canvas; }
	public boolean				isPaused()			{ return _paused; }

 	public void addObserver(Observer inObserver) {
 		_pauseState.addObserver( inObserver ); 
 	}
// 	public void addAppletLoader(AppletLoader inLoader) {
//		_loader = inLoader;
//	}
	
	//----------------------------------------------------------------------
	//	Applet descriptions
	//----------------------------------------------------------------------
	public String getAppletInfo() {
		return	"RingWorld v" + MAJOR_VERSION + "." + MINOR_VERSION + 
				", by David Whitney (dwhitney@cs.stanford.edu)\n" +
				"A physical simulation of life in a rotating space settlement.";
	}
	public String[][] getParameterInfo() {
		String pinfo[][] = {
				{"MultiThread",	"boolean",	"Whether to use more than one thread"},
				{"Radius",		"double",	"Radius of RingWorld"},
				{"Gravity",		"double",	"Apparent gravity at surface of RingWorld"},
				{"RealTime",	"boolean",	"Turn on Constant Speed mode"},
				{"NewWindow",	"boolean",	"Run applet in its own window?"},
				};
		return pinfo;
	};
             
             
	//----------------------------------------------------------------------
	//	run the main thread -- just move objects and draw.
	//----------------------------------------------------------------------
	public void run()
	{
		System.out.println("Single threaded version starting...");
		
		while (true) {
				
			if (!_paused)
				_physics.doPhysics();
	
			// repaint calls update eventually, but not necesarily now.
			// If it waits too long we get contention and jerkiness.
			
			// _canvas.repaint( 1 );		// draw in next millisecond.
			_canvas.drawNow();			// Draw _now_ dammit

			long time = System.currentTimeMillis();
			
			// pause for life to go by.
			try { 
				if (_physics.getSleepTime() == 0)
					System.out.println("Zero sleep time!");
				Thread.sleep( _physics.getSleepTime() ); 
			}
			catch (InterruptedException ex) { 
				System.out.println("Sleep was interrupted!  Windows is evil!");
			}
			
			if (time == System.currentTimeMillis()) {
				_windowsSucks++;
				if (_windowsSucks%100 == 1) 
					System.out.println("Time did not pass in sleep!" + 
						"  Applet.run()\n" + 
						_physics.getSleepTime() + 
						" ms should have passed.\n" +
						"Suppressing further time errors (" + 
						_windowsSucks + " so far).\n\n");
			}
		}
	}
	
	//----------------------------------------------------------------------
	//----------------------------------------------------------------------
	Label makeTitle()
	{
		Label title = new Label("Ring World v" + MAJOR_VERSION + "." + MINOR_VERSION);
		String[] fontList = title.getToolkit().getFontList();
		// pick the first font, often a cool one
		title.setFont( new Font( fontList[0], Font.BOLD, 18 ));
	//	title.setFont( new Font( "SansSerif", Font.BOLD, 18 ));
		title.setAlignment( Label.CENTER );
		title.setForeground( Color.darkGray );
		return title;
	}
	
	//----------------------------------------------------------------------
	//	pause physical world, but keep drawing
	//----------------------------------------------------------------------
	public void togglePause()
	{
		if (_multiThreaded) {
			if (_paused)
				_physicsThread.resume();
			else
				_physicsThread.suspend();
		}
		
		_paused = !_paused;
		
		if (_paused)
			_physics.startPause();
		else
			_physics.stopPause();
		
		_pauseState.setChanged();
		_pauseState.notifyObservers( new Boolean(_paused) );
	}
		

	//----------------------------------------------------------------------
	//	Handle key commands.  Just the pause command here.
	//----------------------------------------------------------------------
	public void keyTyped( KeyEvent inEvent )
	{
		switch ( inEvent.getKeyChar() ) {
			case 'd': 	_canvas.toggleDebugging();	break;
			case 'p':
			case ' ':
			case 'P':	togglePause();	break;
			case 'l':
			case 'L':	_canvas.toggleViewMode();	break;
			case 'v':
			case 'V':	_canvas.toggleVelocityVectors(); 	break;
			case 'f':
			case 'F':	_canvas.togglePlatforms(); 	break;
			case 't':
			case 'T':	_canvas.toggleTrails(); 	break;
			case '=':
			case '+':	_canvas.zoomIn(); 	break;
			case '-':
			case '_':	_canvas.zoomOut(); 	break;
			default: ;
		}
	}
	public void keyReleased( KeyEvent inEvent ) {}
	public void keyPressed( KeyEvent inEvent ) {}

	//----------------------------------------------------------------------
	//	Handle action commands.  Just the pause command here.
	//----------------------------------------------------------------------
	public void actionPerformed( ActionEvent inEvent )
	{
		String cmd = inEvent.getActionCommand();

		if (cmd.equals( RWCommander.CMD_PAUSE )) {
			togglePause();
			if (inEvent.getSource() instanceof Button) {
				Button b = (Button) inEvent.getSource();
				if (_paused)
					b.setLabel("Resume [P]");
				else
					b.setLabel( RWCommander.CMD_PAUSE );
			}
		}
	}	


	//----------------------------------------------------------------------
	//	buildRingWorld -- build the basic objects
	//----------------------------------------------------------------------
	void buildRingWorld()
	{		
		_ringWorld = new RingWorld();
		_physics = new RingWorldPhysics( _ringWorld );
		_canvas = new RingWorldGraphics( _ringWorld, _physics );
		_pauseState = new PublicObservable();
	}

	//----------------------------------------------------------------------
	//	configureRingWorld -- see if any paramters were set, set attributes
	//	and populate the ringworld.
	//----------------------------------------------------------------------
	void configureRingWorld()
	{
		String param;
		double radius = 250;
		double gravity = 1;

		try {
			param = getParameter("MultiThread");
			if ((param != null) && (param.equalsIgnoreCase("true")))
				_multiThreaded = true;
				
			param = getParameter("Radius");
			if (param != null)
				radius = Double.valueOf( param ).doubleValue();
				
			param = getParameter("Gravity");
			if (param != null)
				gravity = Double.valueOf( param ).doubleValue();

			param = getParameter("RealTime");
			if (param != null)
				_physics.setRealTime( param.equalsIgnoreCase("true") );
				
			param = getParameter("OwnWindow");
			if ((param != null) && (param.equalsIgnoreCase("true")))
				_ownWindow = true;
		} 
		catch (Exception ex) {
			System.err.println("Error parsing the paramters.");
		}
		
		_ringWorld.setRadiusAndGravity( radius, gravity );
		//_ringWorld.setTimeAndGravity( 108000, .992 );	// Niven's Ringworld
		//_ringWorld.setRadiusAndGravity( 1.5e11, .992 );	// Niven's Ringworld
		
		// Niven's Ringworld was 1.5e11 m in radius (1 AU) and rotated at
		// 770 miles/s (1.2e6 m/s) with gravity of .992g and a 30 hour rotation
		
		NewtonianObject ball1 = new NewtonianObject("Ball1", 10);
		NewtonianObject ball2 = new NewtonianObject("Ball2", 5);
		NewtonianObject center = new NewtonianObject("Center", 1);
		center.setColor( Color.black );
		_ringWorld.addAtAltitude( ball1, _ringWorld.getRadius()/3 );
		_ringWorld.addAtAltitude( ball2, _ringWorld.getRadius()/2 );
		_ringWorld.addAtAltitude( center, _ringWorld.getRadius() );
	}
	

	//----------------------------------------------------------------------
	//	configureGUI -- make interface.
	//----------------------------------------------------------------------
	void configureGUI( Container w )
	{
		RWCommander commander = new RWCommander( _ringWorld, _canvas, _physics );
		RWGUIButtonPanel buttons = new RWGUIButtonPanel( commander, _canvas, _physics, 
														this, _pauseState );
	//	RWButtonPanel ballControls = new RWButtonPanel(	_ringWorld, _physics, commander );
		
		_canvas.setSize( 400, 400 );
		_canvas.setBackground( getBackground() );
		_canvas.addKeyListener( this );			// listen for keyboard shortcuts

//	GridBagLayout layout = new GridBagLayout();
	//	GridBagConstraints c = new GridBagConstraints();
	//	buttonHolder.setLayout( layout );
	//	c.gridheight = GridBagConstraints.RELATIVE;
	//	buttonHolder.add( new Label(" "));	// hack to make buttons stick together
	//	layout.setConstraints( buttons, c );

		Panel buttonHolder = new Panel();
	//	buttonHolder.setLayout( new GridLayout( 3, 1 ));
		buttonHolder.setLayout( new GridBagLayout() );
		buttonHolder.add( buttons );
	
		// put all the pieces together
		w.setLayout( new BorderLayout() );
		try {
			w.add( _canvas, "Center" );
			w.add( makeTitle(), "North");
			w.add( buttonHolder, "East");
		}
		catch (NullPointerException ex) {
			// Another Microsoft bug in IE 4.0 Mac JVM
			System.out.println("Bug in JVM for BorderLayout add()." + 
							   "  Working around it.");
			w.add("Center", _canvas );
			w.add("North", makeTitle() );
			w.add("East", buttonHolder );
		}
	//	w.add( ballControls, "South");
	//	w.add( new Label(	"Double-click to add new balls." + 
	//					" Click and drag to change velocity.", Label.CENTER), 
	//		"South");
	
//		Panel testPanel = new Panel();
//		testPanel.setLayout( new FlowLayout() );
//		testPanel.add( new Label(" ") );
//		testPanel.add( commander._infoPanel );
//		testPanel.doLayout();
//		add( testPanel, "South");
	}
		
	
	//----------------------------------------------------------------------
	//	applet startup -- AWT components are created by now.
	//----------------------------------------------------------------------
	public void init()
	{
		add( new Label("Building Space Settlement...."));

		_progress = new ProgressBar("Building Space Settlement....");
	//	add( _progress );
		
		// asychronous initialization
		Thread t = new Thread( new Runnable() {
			public void run() { initialize(); } } );
		t.start();
	}
	
	//----------------------------------------------------------------------
	//	initialize -- do everything init should do, but do it asynchronously
	//----------------------------------------------------------------------
	void initialize() 
	{
		final Frame f = new Frame("RingWorld v" + MAJOR_VERSION + "." + MINOR_VERSION );
		_progress.setProgress( 0.1f );
		
		buildRingWorld();		_progress.setProgress( 0.2f );
		configureRingWorld();	_progress.setProgress( 0.5f );
		
		_progress.setProgress( 1.0f );
		
		removeAll();
		
		if (_ownWindow) {
			configureGUI( f );
		} else
			configureGUI( this );				
		
		if (!_ownWindow) {
			validate();
			// getParent().doLayout();		// necessary?  Not sure
		} else {
			// make window closable
			f.addWindowListener( new WindowAdapter() {
				public void windowClosing(WindowEvent e) { f.dispose(); }} );
			f.pack();
			f.show();
		}
		
		// start up threads
		if (_multiThreaded) {
			System.out.println("Multi threaded version starting...");
			
			_physicsThread = new Thread( _physics, "Physics" );
			_graphicsThread = new Thread( _canvas, "Graphics" );
			_physicsThread.start();
			_graphicsThread.start();
		} else {
			_mainThread = new Thread( this, "RingWorld Operation");
			_mainThread.start();
		}

		_paused = false;

//		if (_loader != null)
//			_loader.setAppletLoaded();		// message to replace us as GUI

		_initialized = true;
	}
	
	//----------------------------------------------------------------------
	//	applet thread control
	//----------------------------------------------------------------------
	public void start()
	{
		if (!_initialized)
			return;
			
		if (!_appletStopped)		// hack to get around SGI security exc.
			return;
			
		if (_multiThreaded) {
			_physicsThread.resume();
			_graphicsThread.resume();
		} else
			_mainThread.resume();
			
		_appletStopped = false;
	}
	public void stop()
	{
		if (_multiThreaded) {
			_physicsThread.suspend();
			_graphicsThread.suspend();
		} else
			_mainThread.suspend();

		_appletStopped = true;
	}
	public void destroy()
	{
		if (_multiThreaded) {
			_physicsThread.stop();
			_graphicsThread.stop();
		} else
			_mainThread.stop();
	}


	//----------------------------------------------------------------------
	//	For starting as an application
	//----------------------------------------------------------------------
	public static void main(String args[])
	{
 		AppletFrame.startApplet( new RingWorldApplet(), "RingWorld",
 								 null, 570, 450);
	}	

}		// class RingWorldApplet
