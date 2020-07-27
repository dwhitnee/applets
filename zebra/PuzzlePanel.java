//----------------------------------------------------------------------
//  PuzzlePanel                                 © 1997 David Whitney
//
//  Panel that contains the logic matrices, the clues, the
//  solution, and buttons that control the simulation.
//
//
//  David Whitney                               dwhitney@cs.stanford.edu
//  September 18, 1997
//----------------------------------------------------------------------

import java.util.*;
import java.awt.*;


class PuzzlePanel extends Panel
{
    Thread      _thread;
    LogicEngine _engine;
    LogicMatrixPanel    _logic;
    List        _clueList;
    TextArea    _solutionText;
    boolean     _solutionPosted = false;
    boolean     _solve = false;

    //----------------------------------------------------------------------
    //  Constructor -- build frame with logic grid and clues and button
    //----------------------------------------------------------------------
    public PuzzlePanel( Thread thread, LogicEngine inEngine, String inClues[] )
    {
        super();
        _thread = thread;
        _engine = inEngine;
        _engine.setWaitOnChange( true );    // have engine wait for our clicks

        Panel panel = new Panel();

        _logic = new LogicMatrixPanel( _engine );
        _clueList = new List( 10, false );      // 10 lines visible
        _solutionText = new TextArea("Solution:\n", 6, 80);

        for (int i = 0; i < inClues.length; i++)
            _clueList.addItem( inClues[i] );

        panel.setLayout( new FlowLayout() );
        panel.add( _logic );
        panel.add( new Button("Next") );
        panel.add( new Button("Solve") );
        panel.add( _clueList );

        setLayout( new BorderLayout() );
        add( "Center", panel );
        add( "South", _solutionText );
    }


    //----------------------------------------------------------------------
    //  action -- handle button press
    //----------------------------------------------------------------------
    public boolean action( Event inEvent, Object inName )
    {
        if (inEvent.target instanceof Button) {

            if (((String)inName).equals("Solve")) {

                _engine.setWaitOnChange( false );
                _thread.resume();
                while (_thread.isAlive())
                    Thread.yield();

                if (!_solutionPosted)
                    _solutionText.appendText( _engine.getSolutionString() );
                _solutionPosted = true;
            }

        //  if (((String)inName).equals("Next"))
        //      _logic.setPartialUpdate( true );        // just one click

            if (_thread.isAlive()) {

                _clueList.select( _engine.getClueID() );
                _clueList.makeVisible( _engine.getClueID() );
                _thread.resume();

            } else {

                _clueList.select( _clueList.countItems() );
                _clueList.makeVisible( _clueList.countItems() );
                if (!_solutionPosted)
                    _solutionText.appendText( _engine.getSolutionString() );
                _solutionPosted = true;
            }

            _logic.repaint();       // this repaints the frame
            repaint();              // this doesn't ?!?!?!
        }

        return false;
    }


    public boolean handleEvent(Event e)
    {
       if (e.id == Event.WINDOW_DESTROY)  {
          ((Frame)getParent()).dispose();                     // close window
          return true;
       }

       return super.handleEvent(e);

    }


}

