import java.awt.*;
import java.awt.event.*;
import java.util.*;

//----------------------------------------------------------------------
//	RWCommander
/**
 *	Handles commands from various sources.  This class defines the behavior
 *	of the whole applet.  It is the "Controller" in Model-View-Controller.
 *
 *	@author		<a href="mailto:dwhitney@cs.stanford.edu">David Whitney</a>
 *	@version	April 28, 1999
 */
//
//	Lines commented out with '///' are JDK 1.1
//----------------------------------------------------------------------

public class RWCommander	implements ActionListener, ItemListener
{
	RingWorld			_ringWorld;
	RingWorldGraphics	_graphics;
	RingWorldPhysics	_physics;

	DialogBox			_helpWindow;		// popup for help window
	Frame				_infoFrame;			// popup that holds stats
	RingWorldInfoPanel	_infoPanel;			// really shouldn't be here
		
	public final static	String CMD_RESET_WORLD = "Reset World";
	public final static	String CMD_NEW_BALL = "New Ball At Altitude";
	public final static	String CMD_NEW_RANDOM_BALL = "Random Ball";
	public final static	String CMD_DROP_BALLS = "Jump!";
	public final static	String CMD_VIEWER_LOCALE = "Lock Viewpoint [L]";
	public final static	String CMD_VIEWER_EXTERNAL = "View Externally";
	public final static	String CMD_VIEWER_LOCAL = "View Locally";
	public final static	String CMD_DRAW_VECTORS = "Show Velocity Vectors [V]";
	public final static	String CMD_DRAW_PLATFORMS = "Show Platforms [F]";
	public final static	String CMD_DRAW_TRAILS = "Show Trails [T]";
	public final static	String CMD_PAUSE = "Pause [P]";
	public final static	String CMD_ZOOM_IN = "Zoom In [+]";
	public final static	String CMD_ZOOM_OUT = "Zoom Out [-]";
	public final static	String CMD_SPEED_UP = "Faster";
	public final static	String CMD_SLOW_DOWN = "Slower";
	public final static	String CMD_REAL_TIME = "Constant Speed";
	public final static	String CMD_SHOW_STATS = "Edit World";
	public final static	String CMD_HELP = "Help";
	

	//----------------------------------------------------------------------
	//	Constructor
	//----------------------------------------------------------------------
	public RWCommander( RingWorld inRW,
						RingWorldGraphics inRWG,
						RingWorldPhysics inRWP )
	{
		_ringWorld = inRW;
		_graphics = inRWG;
		_physics = inRWP;
		
		buildInfoFrame();
		buildHelpWindow();
	}
	
	//----------------------------------------------------------------------
	//	Handle commands.
	//----------------------------------------------------------------------
	public void itemStateChanged( ItemEvent inEvent )
	{
		if (inEvent.getSource() instanceof Checkbox)
		{
			Checkbox src = (Checkbox)inEvent.getSource();
			String cmd = src.getLabel();
			
			if (cmd.equals( CMD_VIEWER_LOCALE )) {
				_graphics.setLockView( inEvent.getStateChange() == 
											ItemEvent.SELECTED );
			}

			else if (cmd.equals( CMD_DRAW_VECTORS )) {
				_graphics.setDrawVelocityVectors( inEvent.getStateChange() == 
													ItemEvent.SELECTED );
			}

			else if (cmd.equals( CMD_DRAW_PLATFORMS )) {
				_graphics.setDrawPlatforms( inEvent.getStateChange() == 
													ItemEvent.SELECTED );
			}

			else if (cmd.equals( CMD_DRAW_TRAILS )) {
				_graphics.setDrawTrails( inEvent.getStateChange() == 
													ItemEvent.SELECTED );
			}
			else if (cmd.equals( CMD_REAL_TIME )) {
				_physics.setRealTime( inEvent.getStateChange() == 
													ItemEvent.SELECTED );
				if (inEvent.getStateChange() == ItemEvent.SELECTED)
					_physics.setTimeMagnification( 1.0 );									
			}
		}
	}
	
	//----------------------------------------------------------------------
	//	Handle commands.
	//----------------------------------------------------------------------
	public void actionPerformed( ActionEvent inEvent )
	{
		String cmd = inEvent.getActionCommand();
		
		// Add new ball at altidue given in userData. Always appears at screen bottom.
		if (cmd.equals( CMD_NEW_BALL )) {
			NewtonianObject ball = new NewtonianObject("Ball", 10);
			ButtonPlusData b = (ButtonPlusData) inEvent.getSource();
			_ringWorld.addAtAltitude( ball, ((ValueSlider) b.userData).getValue() );
			// rotate ball so it appears to drop straight down
			Vector2D pos = ball.getPosition();
			CoordinateSystem m = new CoordinateSystem();
			m.rotate( _ringWorld.getRotation() - _graphics.getRotation() + Math.PI/2);
			Vector2D rotatedPos = pos.rotate( m );
			ball.setPosition( rotatedPos );
			ball.setStartPosition();
			ball.getStartPosition().rotate( _ringWorld.getRotation() );
		}
		
		// Add new ball at random altitude, but at least a ball's 
		// radius away from the extremes.
		else if (cmd.equals( CMD_NEW_RANDOM_BALL )) {
			NewtonianObject ball = new NewtonianObject("Ball", 10);
			double altitude = ball.getRadius() + 
					Math.random() * (_ringWorld.getRadius() - 2*ball.getRadius());
			_ringWorld.addAtAltitude( ball, altitude );
		}
		
		else if (cmd.equals( CMD_RESET_WORLD )) {
			_ringWorld.reset();
			NewtonianObject center = new NewtonianObject("Center", 1);
			center.setColor( Color.black );
			_ringWorld.addAtAltitude( center, _ringWorld.getRadius() );

		} else if (cmd.equals( CMD_VIEWER_LOCALE )) {
			_graphics.toggleViewMode();
			if (inEvent.getSource() instanceof Button) {
				Button b = (Button) inEvent.getSource();
				if (_graphics.getViewMode() == RingWorldGraphics.EXTERNAL_VIEW)
					b.setLabel( CMD_VIEWER_LOCAL );
				else
					b.setLabel( CMD_VIEWER_EXTERNAL );
			}
			if (inEvent.getSource() instanceof MenuItem) {
				MenuItem b = (MenuItem) inEvent.getSource();
				if (_graphics.getViewMode() == RingWorldGraphics.EXTERNAL_VIEW)
					b.setLabel( CMD_VIEWER_LOCAL );
				else
					b.setLabel( CMD_VIEWER_EXTERNAL );
			}
		}
		
		else if (cmd.equals( CMD_DROP_BALLS ))
			_ringWorld.releaseObjects();
			
		else if (cmd.equals( CMD_DRAW_VECTORS )) {
			_graphics.toggleVelocityVectors();
			if (inEvent.getSource() instanceof Button) {
				Button b = (Button) inEvent.getSource();
				if (_graphics.isVelocityVectorDrawn())
					b.setLabel("Hide Velocity Vectors");
				else
					b.setLabel("Draw Velocity Vectors");
			}
			if (inEvent.getSource() instanceof MenuItem) {
				MenuItem b = (MenuItem) inEvent.getSource();
				if (_graphics.isVelocityVectorDrawn())
					b.setLabel("Hide Velocity Vectors");
				else
					b.setLabel("Draw Velocity Vectors");
			}
		}
		
		else if (cmd.equals( CMD_ZOOM_IN )) {
			_graphics.zoomIn();
		}
		
		else if (cmd.equals( CMD_ZOOM_OUT )) {
			_graphics.zoomOut();
		}
		
		else if (cmd.equals( CMD_SPEED_UP )) {
			_physics.setTimeMagnification( _physics.getTimeMagnification() * 1.5d );
		//	_physics.setTickTime( _physics.getTickTime() * 1.5d );
		}
		
		else if (cmd.equals( CMD_SLOW_DOWN )) {
			_physics.setTimeMagnification( _physics.getTimeMagnification() * 2d/3d );
		//	_physics.setTickTime( _physics.getTickTime() * 2d/3d );
		}
		
		else if (cmd.equals( CMD_SHOW_STATS )) {
			popupInfoWindow();
		}
		else if (cmd.equals( CMD_HELP )) {
			_helpWindow.popup();
		}
		
		else 
			System.out.println("Command '" + cmd + "' not implemented.");

	}


	//----------------------------------------------------------------------
	//	buildInfoFrame -- make a popup window that watches for closes.
	//----------------------------------------------------------------------
	void buildInfoFrame()
	{
		WindowAdapter closeWatcher = new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				_infoFrame.setVisible( false );
			}
		};
		ActionListener buttonWatcher = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				_infoFrame.setVisible( false );
			}
		};
		
		//  build frame
		_infoFrame = new Frame("RingWorld Statistics");	
		_infoPanel = new RingWorldInfoPanel( _ringWorld );
			
		_infoPanel.getDismissButton().addActionListener( buttonWatcher );
		_infoFrame.addWindowListener( closeWatcher );
		_infoFrame.add( _infoPanel );
	//	_infoFrame.pack();		// this causes a dump in MRJ 2.1.2 TextFieldPeer
	}
	
	void popupInfoWindow()
	{
		if (_infoFrame.isVisible())
			_infoFrame.setVisible( false );
		else {
			_infoPanel.updateValues();
			_infoFrame.pack();
			_infoFrame.show();
		}
	}

	//----------------------------------------------------------------------
	//	buildHelpWindow -- help text.
	//----------------------------------------------------------------------
	void buildHelpWindow()
	{
		_helpWindow = new DialogBox("Help", "OK");
		
		_helpWindow.setText(
		"ABOUT RINGWORLD\n" +
		"-----\n" +
		"This is a simulation of a rotating space settlement without gravity.\n" +
		"The only forces influencing the objects in it are the rotation of the ring.\n" +
		"The black squares are six story buildings 20m on each side.\n" +
		"The balls are weather ballons?  They're artificially large so they can be grabbed.\n" +
		"\n" +
		"HOW TO USE\n" +
		"----------\n" +
		" * Viewing the world: you can view the simulation from afar, watching the\n" +
		"ring rotate freely.  Or you can attach yourself to the ring so the rotation\n" +
		"appears to stop, with '" + CMD_VIEWER_LOCALE + "'.\n" +
		"\n" +
		" * Buttons with letters in parentheses have keyboard short cuts.  For example,\n" +
		"you can pause the simulatiopn by typing 'p'.  If it does not appear to work,\n" +
		"click inside the ring and try again.\n" +
		"\n" +
		" * To add balls to the simulation, double click anywhere inside the ring,\n" +
		"or press the '" + CMD_NEW_RANDOM_BALL + "' button to have a ball placed randomly.\n" +
		"New balls will appear with a line under them.  This is a stationary platform\n" +
		"that rotates with the ring.\n" +
		"\n" +
		" * Press '" + CMD_DROP_BALLS + "' to have the balls fall off of their platforms.\n" +
		"\n" +
		" * Click and drag to move balls.\n" +
		"\n" +
		" * Shift click and drag to throw a ball. (change its velocity).\n" +
		"Hold the mouse button down over a ball and it will turn red.\n" +
		"Move the mouse in the direction you want the ball to travel, \n" +
		"and a line will appear indicating the velocity vector you are adding to\n" +
		"the ball's current velocity.\n" +
		"\n" +
		" * Click the check boxes to turn on and off various features.\n" +
		"The platforms are where the balls started, \n" +
		"Trails are where the ball travelled after jumping,\n" +
		"and velocity vectors point in the direction the ball would travel if left alone.\n" +
		"The length of the vector indicates how fast the ball is travelling.\n" +
		"\n" +
		" * Click on '" + CMD_SHOW_STATS + "' to see the dimensions of the world.\n" +
		"Change the radius and the gravity and see what happens.\n" +
		"\n" +
		" * Speed and zooming are controled with the four buttons on the top right.\n" +
		"The speed multiplier is shown in the upper left.  For example, '2x' means\n" +
		"that time is passing twice as fast as normal.\n" +
		"\n" +
		"BUGS\n" +
		"----\n" +
		"Java is new and this applet will probably behave differently on different systems. \n" +
		"If you have problems, find bugs, or have suggestions, \n" +
		"please send email to dwhitney@cs.stanford.edu\n" +
		"\n" +
		"May 1999.  By David Whitney, concept by Al Globus.\n" +
		"\n" );		
		
	}
	
		
}	// class RWCOmmander

