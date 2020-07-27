//----------------------------------------------------------------------
//  ZebraApplet                                 © 1997 David Whitney
//
//  Determine "Who owns the Zebra?"
//
//
//  David Whitney                               dwhitney@cs.stanford.edu
//  September 18, 1997
//----------------------------------------------------------------------


import java.awt.*;
import java.applet.*;
import com.metrowerks.AppletFrame;

//----------------------------------------------------------------------
//      Simple callback interface
//----------------------------------------------------------------------
interface Callback {
    public void execute( Object inObj );
}



//----------------------------------------------------------------------
//      Zebra Applet
//----------------------------------------------------------------------

public class ZebraApplet extends Applet
    implements Callback
{
    ClueInputFrame      _clueFrame;

    public static void main( String args[])
    {
        AppletFrame.startApplet("ZebraApplet", "Who Owns the Zebra?", args);
    }

    //----------------------------------------------------------------------
    //  init -- get clues
    //----------------------------------------------------------------------
    public void init()
    {
    //  resize(500, 500);

        _clueFrame = new ClueInputFrame( this );
        _clueFrame.pack();
        _clueFrame.show();
    }

    //----------------------------------------------------------------------
    //  execute -- start engine and start view
    //----------------------------------------------------------------------
    public void execute( Object inObject )
    {
        String clues[] = _clueFrame.getClues();
        boolean visualFlag = ((Boolean)inObject).booleanValue();

        LogicEngine engine = new LogicEngine( _clueFrame.getVarCount(), clues );
        Thread thread = new Thread( engine );

        if (visualFlag) {
            DumbFrame f = new DumbFrame("Who owns the Zebra?");
            f.setLayout( new FlowLayout() );
            f.add( new PuzzlePanel( thread, engine, clues ) );
            f.pack();
            f.show();
        }

        thread.start();
    }

}   // end of class



//----------------------------------------------------------------------
//  Dumb frame that closes
//----------------------------------------------------------------------
class DumbFrame extends Frame {

    public DumbFrame(String name) {
        super(name);
    }

    public boolean handleEvent(Event e) {
        if (e.id == Event.WINDOW_DESTROY) {
            dispose();
            return true;
        }

        return super.handleEvent(e);
    }
}   // end class

