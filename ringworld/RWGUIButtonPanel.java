import java.awt.*;
import java.awt.event.*;
import java.util.*;

//----------------------------------------------------------------------
//	RWGUIButtonPanel
/**
 *	A panel of buttons to control the GUI.
 *	Observes the graphics object for changes to update GUI.
 *
 *	@author		<a href="mailto:dwhitney@cs.stanford.edu">David Whitney</a>
 *	@version	April 28, 1999
 */
//----------------------------------------------------------------------

public class RWGUIButtonPanel	extends Panel
								implements Observer
{	
	RingWorldGraphics	_graphics;
	RingWorldPhysics	_physics;
	
	// check boxes watching the environment
	Checkbox			_viewBox, _drawVecBox, _realTimeBox;
	Checkbox			_drawPlatformBox, _drawTrailsBox;

	Button				_pauseButton;		// button watching the pauser
	Button				_faster, _slower;	// buttons to disable in Real-Time
	
	Button				_dropButton;
	
	//----------------------------------------------------------------------
	//	Buttons that would be menus in an application.
	//----------------------------------------------------------------------
	public RWGUIButtonPanel( ActionListener inListener, 
							RingWorldGraphics inGraphics,
							RingWorldPhysics inPhysics,
							ActionListener inPauseListener,
							Observable inPauser )
	{		
		_graphics = inGraphics;
		_physics = inPhysics;
		
		setLayout( new BorderLayout() );
		
		
		Panel checkPanel = new Panel();
		checkPanel.setLayout( new GridLayout( 9, 1 ));
		
		_realTimeBox = newCheckButton(		RWCommander.CMD_REAL_TIME,
											checkPanel, (ItemListener)inListener );		
		checkPanel.add( new Label("") );

		_drawVecBox = newCheckButton(	RWCommander.CMD_DRAW_VECTORS,
										checkPanel, (ItemListener)inListener );
		_drawPlatformBox = newCheckButton(	RWCommander.CMD_DRAW_PLATFORMS,
											checkPanel, (ItemListener)inListener );		
		_drawTrailsBox = newCheckButton(	RWCommander.CMD_DRAW_TRAILS,
											checkPanel, (ItemListener)inListener );		
		checkPanel.add( new Label("") );

		_viewBox = newCheckButton(	RWCommander.CMD_VIEWER_LOCALE, 
									checkPanel, (ItemListener)inListener );

		_dropButton = ButtonPlusData.newButton( RWCommander.CMD_DROP_BALLS, 
												checkPanel, inListener );

		Panel bottomPanel = new Panel();
		bottomPanel.setLayout( new GridLayout() );
		
		_pauseButton = ButtonPlusData.newButton( RWCommander.CMD_PAUSE, 
												bottomPanel, inPauseListener );
		ButtonPlusData.newButton( RWCommander.CMD_NEW_RANDOM_BALL, bottomPanel,
											 inListener );
		checkPanel.add( bottomPanel );
		
		add( makeSideBySideButtons( inListener ), "North");
	//	add( new Label(""), "Center" );
		add( checkPanel, "South");

		// tell us when data changes under us
		_graphics.addObserver( this );
		_physics.addObserver( this );
		inPauser.addObserver( this );
		
		update( null, null );
	}

	//----------------------------------------------------------------------
	//----------------------------------------------------------------------
	Panel makeSideBySideButtons(ActionListener inListener )
	{
		Panel square = new Panel();
		
		square.setLayout( new GridLayout( 4, 2 ));
		
		ButtonPlusData.newButton( RWCommander.CMD_SHOW_STATS, square, inListener ); //.setEnabled( false );
		ButtonPlusData.newButton( RWCommander.CMD_RESET_WORLD, square, inListener );
		square.add( new Label("") );		
		square.add( new Label("") );		
		ButtonPlusData.newButton( RWCommander.CMD_ZOOM_IN, square, inListener );
		ButtonPlusData.newButton( RWCommander.CMD_ZOOM_OUT, square, inListener );
		_faster = ButtonPlusData.newButton( RWCommander.CMD_SPEED_UP, square, inListener );
		_slower = ButtonPlusData.newButton( RWCommander.CMD_SLOW_DOWN, square, inListener );
		
		return square;
	}
	
	//----------------------------------------------------------------------
	//	addNotify -- when there is a real widget, make the Jump! button
	//	stand out with a bold font.
	//----------------------------------------------------------------------
	public void addNotify()
	{
		super.addNotify();
		// make this button special
	//	System.out.println( "Using " + getFont() + "\n");
		_dropButton.setFont( new Font(	_dropButton.getFont().getName(), 
										Font.BOLD,
										_dropButton.getFont().getSize() ) );

	}
	
	//----------------------------------------------------------------------
	//	Graphics env changed, update the checkboxes
	//----------------------------------------------------------------------
	public void update( Observable inTarget, Object inArg)
	{
		_viewBox.setState( _graphics.isViewLocked() );
		_drawVecBox.setState( _graphics.isVelocityVectorDrawn() );
		_drawPlatformBox.setState( _graphics.isPlatformsDrawn() );
		_drawTrailsBox.setState( _graphics.isTrailsDrawn() );
		_realTimeBox.setState( _physics.isRealTime() );
		
		if (inArg != null)
			_pauseButton.setLabel( ((Boolean)inArg).booleanValue() ? 
									"Resume (P)" : RWCommander.CMD_PAUSE);
	}

	//----------------------------------------------------------------------
	//	builds a checkbox
	//----------------------------------------------------------------------
	Checkbox newCheckButton(	String inCommand, Container inContainer,
								ItemListener inListener )
	{
		Checkbox b = new Checkbox( inCommand );
		b.addItemListener( inListener );
		inContainer.add( b );
		
		return b;
	}

}	// class RWGUIButtonPanel 
