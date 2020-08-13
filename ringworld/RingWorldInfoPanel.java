import java.awt.*;
import java.awt.event.*;
import java.util.*;

//----------------------------------------------------------------------
//	RingWorldInfoPanel
/**
 *	Editable panel of the physical attributes of the world.
 * 
 *	@author		<a href="mailto:dwhitney@cs.stanford.edu">David Whitney</a>
 *	@version	May 11, 1999
 */
//----------------------------------------------------------------------

public class RingWorldInfoPanel	extends Panel
								implements ActionListener
{
	RingWorld	_ringWorld;
	Label[]		_labels;
	TextField[]	_values;
	Label[]		_units;
	Button		_dismiss;
	
	//----------------------------------------------------------------------
	//----------------------------------------------------------------------
	public Button	getDismissButton()	{ return _dismiss; }

	//----------------------------------------------------------------------
	//	Constructor -- build panel
	//----------------------------------------------------------------------
	public RingWorldInfoPanel( RingWorld inRW )
	{
		_ringWorld = inRW;
		setLayout( new GridLayout( 6, 3 ));
		
		_labels = new Label[4];
		_values = new TextField[4];
		_units = new Label[4];
		
		_labels[0] = new Label("Radius");
		_labels[1] = new Label("Apparent Gravity");
		_labels[2] = new Label("Rotational Velocity");
		_labels[3] = new Label("Period of Rotation");
	
		_units[0] = new Label("m");
		_units[1] = new Label("g");
		_units[2] = new Label("m/s");
		_units[3] = new Label("s");
		
		for (int i = 0; i < 4; i++) {
			_values[i] = new TextField();
			_values[i].setEditable( false );
			_values[i].addActionListener( this );	// see if values change
		}
		_values[0].setEditable( true );
		_values[1].setEditable( true );
			
		updateValues();

		for (int i = 0; i < 4; i++) {
			add( _labels[i] );
			add( _values[i] );
			add( _units[i] );
		}
		add( new Label(" "));
		add( new Label(" "));
		add( new Label(" "));
		add( new Label(" "));
		
		Button applyButton = new Button("Apply Changes");
		applyButton.addActionListener( this );
		
		_dismiss = new Button("Dismiss");
		
		add( applyButton );
		add( _dismiss );
	}
	
	
	//----------------------------------------------------------------------
	//	Any action results in reading the radius and gravity and rebuilding
	//	the ringworld, whether a text change or "apply" button.
	//----------------------------------------------------------------------
	public void actionPerformed( ActionEvent inEvent )
	{
		System.out.println("Rebuilding RingWorld...");
		double r, g;
		try {
			
			r = Double.valueOf( _values[0].getText() ).doubleValue();
			g = Double.valueOf( _values[1].getText() ).doubleValue();
			_ringWorld.setRadiusAndGravity( r, g );
			
		} catch (NumberFormatException ex) {
			// try and handle Netscape 4.x NT JVM bug, 
			// it does not understand decimals(!)
			System.out.println("JVM error. Could not convert string to double."+
							   "\nWorking around it. Precision lost possibly.");

			StringTokenizer tokens;
			
			tokens = new StringTokenizer( _values[0].getText().trim() );
			r = Double.valueOf( tokens.nextToken(".") ).doubleValue();
			tokens = new StringTokenizer( _values[1].getText().trim() );
			g = Double.valueOf( tokens.nextToken(".") ).doubleValue();

			_ringWorld.setRadiusAndGravity( r, g );		
		}
		
		updateValues();
	}
	
	//----------------------------------------------------------------------
	//	set text fields
	//----------------------------------------------------------------------
	public void updateValues()
	{
		_values[0].setText( _ringWorld.getRadius() + "");	// + " m");
		_values[1].setText( _ringWorld.getGravity() + "");	// + " g");
		_values[2].setText( _ringWorld.getVelocity() + ""); 	//+ " m/s");
		_values[3].setText( _ringWorld.getPeriodOfRotation() + "");	// + " s");
	}



}		// RingWorldInfoPanel
