import java.awt.*;
import java.awt.event.*;
import java.util.*;


//----------------------------------------------------------------------
//	ValueSlider
/**
 *	A text field and a scrollbar that respond to each other's actions.
 *	This widget is two interfaces to one double value.  The TextField
 *	is an exact interface, the scrollbar is only an approximation of the
 *	actual value.  For example, the slider is only capable of integer values,
 *	but the underlying value could be 1.5e23.
 *
 *	@author		<a href="mailto:dwhitney@cs.stanford.edu">David Whitney</a>
 *	@version	April 28, 1999
 */
//----------------------------------------------------------------------

public class ValueSlider	extends Panel
							implements ActionListener, AdjustmentListener
{
	double				_value;			// current value
	double				_min, _max;		// range of legal values
	
	Scrollbar 			_slider;		// represent value approximation here.
	int					_sliderSize;	// range of slider in pixels
	TextField			_textField;		// display value here
	PublicObservable	_valueObservable;	// place holder, can't make the class Observable
	
	//----------------------------------------------------------------------
	//	Constructor
	/**
	 *	Specify the numerical range of this variable, 
	 *	this is independent of the	slider pixel values.
	 *	
	 *	@param   inLabel		Text label for slider
	 *	@param   inValue		The current numerical value for slider.
	 *	@param   inMinValue		The numerical range for slider.
	 *	@param   inMaxValue		The numerical range for slider.
	 *	@param   orientation	Scrollbar.HORIZONTAL or Scrollbar.VVERTICAL
	 *
	 *	@see java.awt.Scrollbar
	 */
	//----------------------------------------------------------------------
	public ValueSlider(	String inLabel,
						double inValue,			// value params
						double inMinValue,
						double inMaxValue,		
						int orientation)		// slider
	{
		_valueObservable = new PublicObservable();
		_sliderSize = 200;	// arbitrary number that covers a lot of values
		
		_textField = new TextField("hi", 5 );
		_slider = new Scrollbar( orientation, 0, 64, 0, _sliderSize+64 );
		
		if (orientation == Scrollbar.HORIZONTAL)	// this doesn't always help
			_slider.setSize( 200, 40 );
		else
			_slider.setSize( 40, 200 );
		
		_textField.addActionListener( this );		// listen to text changes
		_slider.addAdjustmentListener( this );		// listen for scroll moves
		
		setMinMax( inMinValue, inMaxValue );
		setValue( inValue );
		
		add( new Label( inLabel ));
		add( _textField );
		add( _slider );
	}
	
	//----------------------------------------------------------------------
	//	Accessors
	/**
	 *	Value changes update the slider and textfield.
	 */
	//----------------------------------------------------------------------
	public void	setValue( double inValue )
	{
		_value = inValue;
		// System.err.println("Setting value " + _value);

		_textField.setText( Double.toString( _value ));

		// Convert to a percentage, then set the slider
		if (_max == _min)
			_slider.setValue( 0 );
		else
			_slider.setValue( (int) (_sliderSize * (_value - _min) / (_max - _min)));

		_valueObservable.setChanged();
		_valueObservable.notifyObservers( new Double( _value ));
	}
	
	public double	getValue()		{ return _value; }

	public void	setMinMax( double inMin, double inMax)
	{
		_min = inMin;
		_max = inMax;
		setValue( _value );		// reset slider to new range
	}
	

	//----------------------------------------------------------------------
	//	addValueWatcher - let interested parties know when the slider's value
	//	has changed.
	//----------------------------------------------------------------------
	public void addValueWatcher( Observer inObserver )
	{
		_valueObservable.addObserver( inObserver );
	}
	
	//----------------------------------------------------------------------
	//	actionPerformed - the incoming event is a text change.
	//----------------------------------------------------------------------
	public void actionPerformed( ActionEvent inEvent )
	{
		if (inEvent.getSource() instanceof TextField ) {

			// the text is the command, silly really, that's not a command!
			String text = inEvent.getActionCommand();	
			try {	
				setValue( Double.valueOf( text ).doubleValue());
			} catch (NumberFormatException ex) { }
			
		} else
			throw new Error("Bad action recieved by ValueSlider.");
	}
	
	//----------------------------------------------------------------------
	//	adjustmentValueChanged - the incoming event is a slider move.
	//----------------------------------------------------------------------
	public void adjustmentValueChanged( AdjustmentEvent inEvent )
	{
		if (inEvent.getSource() instanceof Scrollbar ) {
			setValue( _min + (_max - _min)*((double)inEvent.getValue() / _sliderSize) );

		} else
			throw new Error("Bad adjustment recieved by ValueSlider.");
	}
		


}		// ValueSlider