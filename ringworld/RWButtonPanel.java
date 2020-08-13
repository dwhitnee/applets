import java.awt.*;
import java.awt.event.*;
import java.util.*;

//----------------------------------------------------------------------
//	RWButtonPanel
/**
 *	A panel of buttons to control the environment.
 *	Observes the ringworld for changes to update GUI.
 *
 *	@author		<a href="mailto:dwhitney@cs.stanford.edu">David Whitney</a>
 *	@version	April 28, 1999
 */
//----------------------------------------------------------------------

public class RWButtonPanel	extends Panel
							implements Observer
{
	RingWorld			_ringWorld;
	RingWorldPhysics	_physics;
	ValueSlider 		_altitudeSlider;
	
	//----------------------------------------------------------------------
	//	construct the widgets
	//----------------------------------------------------------------------
	public RWButtonPanel(	RingWorld inRW,
							RingWorldPhysics inRWP,
							ActionListener inBallCmdListener )
	{
		_ringWorld = inRW;
		_physics = inRWP;
		
		_altitudeSlider = 
			new ValueSlider( "Altitude:", 0, 
						0, _ringWorld.getRadius(),
						Scrollbar.HORIZONTAL );

		ButtonPlusData b = 
			ButtonPlusData.newButton( RWCommander.CMD_NEW_BALL,
									 _altitudeSlider, inBallCmdListener );
		b.userData = _altitudeSlider;		
		ButtonPlusData.newButton( RWCommander.CMD_DROP_BALLS, 
									_altitudeSlider, inBallCmdListener );
		
		setLayout( new BorderLayout() );
		try {
			add( new Label("Click and drag a ball to change its velocity", Label.CENTER), 
				"Center");
			add( new Label("Double click to add a new ball", Label.CENTER), 
				"North");
			add( _altitudeSlider, "South");
		}
		catch (Exception ex) {
			// Another Microsoft bug in IE 4.0 Mac JVM
			add( "Center",
					new Label("Click and drag a ball to change its velocity", Label.CENTER));
			add( "North",
					new Label("Double click to add a new ball", Label.CENTER)); 			
			add( "South", _altitudeSlider);
		}
		
		_ringWorld.addObserver( this );
	}
		
	//----------------------------------------------------------------------
	//	update - get Observable notification of ringworld change
	//----------------------------------------------------------------------
	public void update( Observable inTarget, Object inArg)
	{
		_altitudeSlider.setMinMax( 0, _ringWorld.getRadius() );
	}
	
	//----------------------------------------------------------------------
	//	makeThrottlePanel -- make a slider to control the speed of the simulation.
	//	Use an anonymous class to watch for value changes to tell the physics
	//	object what the speed should be.
	//----------------------------------------------------------------------
	Panel makeThrottlePanel()
	{
		ValueSlider throttleSlider = 
			new ValueSlider("Animation (0=smoothest)", 50, 0, 400, Scrollbar.HORIZONTAL );

		// inner class to update values from the throttle
		Observer throttleObserver = new Observer() {
			public void update(Observable inObserved, Object inArg) {
				_physics.setSleepTime( ((Double) inArg).intValue() );
			}
		};
		
		throttleSlider.addValueWatcher( throttleObserver );
		
		return throttleSlider;
	}
	
}		// class RWButtonPanel
