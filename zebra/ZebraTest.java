//----------------------------------------------------------------------
//----------------------------------------------------------------------

import java.io.*;
import java.util.*;
import java.awt.*;

class ZebraTest
{
    static int sNumClues;
    static int sNumValues;
    static String sClues[];


    //----------------------------------------------------------------------
    //----------------------------------------------------------------------
    public static void main( String args[] )
    {
        try {
            readClueFile( );
        }
        catch (IOException ex) {
            System.err.println("File access error: " + args[0] + ".\n" + ex);
        }

        for (int i = 0; i < sNumClues; i++)
            System.out.println( sClues[i] );

        String[] solution = new String[sNumValues];

        System.out.println("Starting...");
        long start = System.currentTimeMillis();
        WhoOwnsZebra( sNumValues, sNumClues, sClues, solution );
        long end = System.currentTimeMillis();
        System.out.println("Finished...");

        System.out.println("Time = " + (end - start) + "ms");
        for (int i = 0; i < sNumValues; i++)
            System.out.println( solution );
    }


    //----------------------------------------------------------------------
    //  interface to Logic code
    //----------------------------------------------------------------------
    public static void WhoOwnsZebra( int problemDimension, int numClues,
                                    String clues[], String solution[] )
    {
        LogicEngine engine = new LogicEngine( problemDimension, clues );
        engine.run();

        String solutionPrint = engine.getSolutionString();
        StringTokenizer tok = new StringTokenizer( solutionPrint, "\n");
        int i = 0;
        while (tok.hasMoreTokens())
            solution[i++] = tok.nextToken();
    }

    //----------------------------------------------------------------------
    //  readClueFile -- reads a file of clues with the first line
    //  being two integers: # of clues, and problem dimension
    //  The rest should be clues.
    //  Values stored in the static variables
    //----------------------------------------------------------------------
    public static void readClueFile( )
        throws FileNotFoundException
    {
        FileDialog dialog = new FileDialog( null, "Clue File");
        dialog.show();

        try {
            FileInputStream clueFile = new FileInputStream(
                                                dialog.getDirectory() +
                                                dialog.getFile() );

            DataInputStream inline = new DataInputStream( clueFile );

            if (inline.available() > 0) {
                String header = inline.readLine();
                StringTokenizer tok = new StringTokenizer( header );
                sNumClues  = Integer.valueOf( tok.nextToken() ).intValue();
                sNumValues = Integer.valueOf( tok.nextToken() ).intValue();
            }

            sClues = new String[sNumClues];

            for (int i = 0; (i < sNumClues) && (inline.available() > 0); i++)
                sClues[i] = inline.readLine();

        }
        catch(IOException e) {} ;
    }
}