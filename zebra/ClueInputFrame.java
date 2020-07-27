//----------------------------------------------------------------------
//      ClueInputFrame                                                                  © 1997 David Whitney
//
//      A text frame to load clues from disk or to paste in.
//
//      David Whitney                                                           dwhitney@cs.stanford.edu
//      October 1, 1997
//----------------------------------------------------------------------

import java.util.*;
import java.awt.*;
import java.io.*;

public class ClueInputFrame extends Frame
{
        Checkbox        _visualFlag;
        TextArea        _inputArea;
        Callback        _callback;
        int                     _numVars = -1;

        //----------------------------------------------------------------------
        //      Contstruct a frame with a file menu, text area, and button
        //----------------------------------------------------------------------
        public ClueInputFrame( Callback inCallback )
        {
                super("Input Clues Here");

                _callback = inCallback;

                setLayout( new BorderLayout());

                MenuBar menuBar = new MenuBar();
                Menu fileMenu = new Menu("File");
                fileMenu.add("Open...");
                fileMenu.addSeparator();
                fileMenu.add("Quit");
                menuBar.add( fileMenu );

                _inputArea = new TextArea( 15, 40 );
                _visualFlag = new Checkbox("Visual?");
                _visualFlag.setState( true );

                Panel buttonPanel = new Panel();

                buttonPanel.setLayout( new FlowLayout() );
                buttonPanel.add( new Button("Enter Clues"));
                buttonPanel.add( new Button("Clear"));
                buttonPanel.add( _visualFlag );

                add("Center", _inputArea );
                add("South", buttonPanel );

                setMenuBar( menuBar);
        }

        //----------------------------------------------------------------------
        //----------------------------------------------------------------------
        public int getVarCount()        { return _numVars; }

        //----------------------------------------------------------------------
        //      getClues -- returns the array of Strings in our text widget.
        //----------------------------------------------------------------------
        public String[] getClues()
        {
                String clues = _inputArea.getText();
                StringTokenizer tokenizer = new StringTokenizer( clues, "\n");

                int numTokens = tokenizer.countTokens();
                String outClues[] = new String[numTokens];

                for (int i = 0; i < numTokens; i++) {
                        outClues[i] = tokenizer.nextToken();
                }
                return outClues;
        }

        //----------------------------------------------------------------------
        //      action -- handle button press
        //----------------------------------------------------------------------
        public boolean action( Event inEvent, Object inName )
        {
                int count = 0;

                if (inEvent.target instanceof MenuItem) {
                        MenuItem choice = (MenuItem) inEvent.target;

                        if (choice.getLabel().equals("Quit")) {
                                System.exit(0);

                        } else if (choice.getLabel().equals("Open...")) {
                                readClueFile();
                        }
                }

                if (inEvent.target instanceof Button) {

                        Button button = (Button) inEvent.target;

                        if (button.getLabel().equals("Clear")) {
                                _inputArea.setText("");

                        } else {

                                String clues[] = getClues();
                                for (int i = 0; i < clues.length; i++) {
                                        System.out.println( i + ": " + clues[i]);
                                        if (clues[i].indexOf("ISA") >= 0)
                                                count++;
                                }
                                _numVars = (int) Math.sqrt( count );
                                System.out.println("Number of variables = " + _numVars +
                                                                        "  count = " + count);

                                // start puzzle panel here.  or give clues to logic engine.
                                _callback.execute( new Boolean( _visualFlag.getState()) );
                        }
                        return true;
                }
                return false;
        }

        //----------------------------------------------------------------------
        //      readClueFile -- get a file to paste into text area.
        //      Probably nukes applet capability.
        //----------------------------------------------------------------------
        void readClueFile()
        {
                FileDialog dialog = new FileDialog( this, "Clue File");
                dialog.show();

                try {
                        FileInputStream clueFile = new FileInputStream(
                                                                        dialog.getDirectory() +
                                                                        dialog.getFile() );

                        DataInputStream inline = new DataInputStream( clueFile );

                        while (inline.available() > 0)
                                _inputArea.appendText( inline.readLine() + "\n" );
                }
                catch(IOException e) {} ;

        }


}
