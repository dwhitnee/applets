//----------------------------------------------------------------------
//  RelationMatrix                              © 1997 David Whitney
//
//  David Whitney                               dwhitney@cs.stanford.edu
//  September 18, 1997
//----------------------------------------------------------------------

import java.util.*;

import Variable;


class LogicFailureException extends Exception {
    public LogicFailureException( String inMessage )
    {
        super( inMessage );
    }
}

public class RelationMatrix
{
    int         _matrix[][];
    Variable    _varA, _varB;
    int         _size;
    static Stack    sSetCmdList = new Stack();

    static final int UNSET = 0;
    static final int YES = 1;
    static final int NO = 2;

    // class variables to check on recent activity
    static Value    sMostRecentValueA = null, sMostRecentValueB = null;
    static boolean  sChanged = false;
    static boolean  sDoWaitOnChange = true;

    //----------------------------------------------------------------------
    //  Constructor -- assumes a square matrix, can't 1-to-1 relate unequal
    //  size sets.
    //----------------------------------------------------------------------
    public RelationMatrix( Variable inVarA, Variable inVarB )
    {
        _size = inVarA.getValueCount();
        _matrix = new int[_size][_size];
        _varA = inVarA;
        _varB = inVarB;
    }

    //----------------------------------------------------------------------
    //  static accessors for state of class
    //----------------------------------------------------------------------
    public static Value getMostRecentValueA()   { return sMostRecentValueA; }
    public static Value getMostRecentValueB()   { return sMostRecentValueB; }
    public static boolean getChanged()          { return sChanged; }
    public static void setChanged( boolean inFlag ) { sChanged = inFlag; }
    public static void setWaitOnChange( boolean inFlag )    { sDoWaitOnChange = inFlag; }

    //----------------------------------------------------------------------
    //  Accessors
    //----------------------------------------------------------------------
    public Variable getVariableA()                  { return _varA; }
    public Variable getVariableB()                  { return _varB; }
    public Variable getOtherVariable( Variable inVar )
        throws Exception
    {
        if (inVar == _varA)
            return _varB;
        else if (inVar == _varB)
            return _varA;
        else
            throw new Exception("ERROR: " + inVar.getName() + " not a member of " +
                        "matrix: " + this );
    }

    //----------------------------------------------------------------------
    //  isFullyRelated -- return true if no element is UNSET
    //----------------------------------------------------------------------
    public boolean isFullyRelated( )
    {
        for (int i = 0; i < _size; i++ )
            for (int j = 0; j < _size; j++ )
                if (_matrix[i][j] == UNSET)
                    return false;
        return true;
    }

    //----------------------------------------------------------------------
    //  relates -- return true if this variable defines one of the two sides
    //  of this matrix.
    //----------------------------------------------------------------------
    public boolean relates( Variable inVar )
    {
        if ((inVar == _varA) || (inVar == _varB))
            return true;
        else
            return false;
    }

    //----------------------------------------------------------------------
    //  getRelation
    //----------------------------------------------------------------------
    public int getRelation( Value inValueA, Value inValueB )
    {
        int i = inValueA.getIndex();
        int j = inValueB.getIndex();

        if (_varA != inValueA.getVariable() ) {
            int temp = i; i = j; j = temp;      // swap indices
        }

        return _matrix[i][j];
    }

    //----------------------------------------------------------------------
    //  setRelation
    //  Throw on overwriting set.
    //----------------------------------------------------------------------
    public void setRelation( Value inValueA, Value inValueB )
        throws Exception, LogicFailureException
    {
        if (inValueA == inValueB)
            return;

        int i = inValueA.getIndex();
        int j = inValueB.getIndex();

        if (_varA != inValueA.getVariable() ) {
            int temp = i; i = j; j = temp;      // swap indices
        }

        set( i, j, YES );               // throws on bad set
    }

    //----------------------------------------------------------------------
    //  setNoRelation -- set these two values to no have relation in the matrix
    //  Also, check to see if this change creates a positive relation
    //  in the same row and column.
    //  Throw on overwriting set.
    //----------------------------------------------------------------------
    public void setNoRelation( Value inValueA, Value inValueB )
        throws Exception, LogicFailureException
    {
        int i = inValueA.getIndex();
        int j = inValueB.getIndex();

        if (_varA != inValueA.getVariable() ) {
            int temp = i; i = j; j = temp;      // swap indices
        }

        set( i, j, NO );
    }

    //----------------------------------------------------------------------
    //  checkPositiveRelation -- see if we can deduce a relation from the
    //  current matrix.  i.e. if all cells in this row or column are NO's
    //  but one, then, ergo, we have a YES.
    //----------------------------------------------------------------------
    public void checkPositiveRelationByElimination( int x, int y )
        throws Exception, LogicFailureException     // asserts sets are done properly
    {
        int no = 0;

        // check row
        for (int i = 0; i < _size; i++)
            if (_matrix[i][y] == NO)
                no++;
        if (no == _size-1) {        // found a positive hit
            for (int i = 0; i < _size; i++)
                if (_matrix[i][y] == UNSET)
                    set( i, y, YES );
        }

        // check column
        no = 0;
        for (int i = 0; i < _size; i++)
            if (_matrix[x][i] == NO)
                no++;
        if (no == _size-1) {        // found a positive hit
            for (int i = 0; i < _size; i++)
                if (_matrix[x][i] == UNSET)
                    set( x, i, YES );
        }
    }

    //----------------------------------------------------------------------
    //  set -- set a matrix value.  Create a list of these so we can undo
    //  later.  Also handle faulty logic by throwing something.
    //  If a YES setting, see if this yes means any transitive relationd
    //  in other matrices (e.g. if "American owns dog" and
    //  "American livesIn blueDoor" then so does the dog.
    //
    //  Returns the command generated by this set.
    //
    //  Throws if overwriting an existing 'set'
    //----------------------------------------------------------------------
    SetCommand set( int x, int y, int what )
        throws Exception, LogicFailureException
    {
        SetCommand outCommand = null;

        if (((_matrix[x][y] == NO) && (what == YES)) ||
            ((_matrix[x][y] == YES) && (what == NO)))
        {
            // Uh oh, bad logic to override previous deduction
            throw new LogicFailureException("Request to overwrite Matrix entry "
                + x + ", " + y + " in matrix " + _varA.getName() + " vs. "
                + _varB.getName() + " with " + what);
        }

        if (_matrix[x][y] == UNSET) {

            // wait for a click or something here.
            if (sDoWaitOnChange)
                Thread.currentThread().suspend();

            outCommand = new SetCommand( this, x, y, what );
            sSetCmdList.push( outCommand );
            _matrix[x][y] = what;

            Value valA = _varA.getValueByIndex( x );
            Value valB = _varB.getValueByIndex( y );

            sMostRecentValueA = valA;
            sMostRecentValueB = valB;
            sChanged = true;

            if (what == YES) {
                // we have a relation, fill out rest of row and column with NO's
                for (int i = 0; i < _size; i++ ) {
                    if (i != x)
                        set( i, y, NO );
                    if (i != y)
                        set( x, i, NO );
                }
                // propagate positive and negative relations to other related matrices
                findTransitiveRelations( valA, valB );
                findTransitiveRelations( valB, valA );

            } else if (what == NO) {
                checkPositiveRelationByElimination( x, y );
            }
        }
        return outCommand;
    }

    //----------------------------------------------------------------------
    //  unset -- stuff for undoing hypotheses, see class SetCommand
    //----------------------------------------------------------------------
    void unset( int x, int y )
    {
        _matrix[x][y] = UNSET;
        // wait for a click or something here.
        if (sDoWaitOnChange)
            Thread.currentThread().suspend();
    }

    //----------------------------------------------------------------------
    //  findTransitiveRelations -- See if a positive relation between these
    //  two values transitively leads to a positive relation between other
    //  variable's values. Also check for transitive negative relations
    //  i.e. we know X<->Y, so if X<->Z then make Y<->Z
    //  also if X<->Y, then if X !-> Z then Y !-> Z
    //----------------------------------------------------------------------
    public void findTransitiveRelations( Value inValueX, Value inValueY )
        throws Exception, LogicFailureException
    {
        // go through all of variable A's matrices
        for (Enumeration e = inValueX.getVariable().getMatrixVector().elements() ; e.hasMoreElements() ; ) {

            RelationMatrix mat = (RelationMatrix) e.nextElement();

            // skip A relates to B, we know that already
            if (mat.relates( inValueY.getVariable() ))
                continue;

            // see if we match anything in this matrix
            Value valueZ = mat.getPositiveRelation( inValueX );

            try {
                // if a match also (Z), then make the transitive relation
                // between this match and X's first value match (Y).

                if (valueZ != null) {
                    inValueY.setRelation( valueZ );
                }
            } catch (Exception ex) {    // if we're here then inconsistent logic
                throw new LogicFailureException("Inconsistent Logic:" + ex );
            }

            // find any negative matches also (Z), then make the transitive
            // negative relation between this match and X's first value match (Y).
            mat.setNegativeRelations( inValueX, inValueY );
        }
    }

    //----------------------------------------------------------------------
    //  getPositiveRelation -- return what value this value relates to.
    //  Return null if no relation yet.
    //----------------------------------------------------------------------
    public Value getPositiveRelation( Value inValue )
        throws Exception
    {
        if (_varA == inValue.getVariable()) {
            for (int i = 0; i < _size; i++)
                if (_matrix[inValue.getIndex()][i] == YES)
                    return _varB.getValueByIndex( i );

        } else if (_varB == inValue.getVariable()) {
            for (int i = 0; i < _size; i++)
                if (_matrix[i][inValue.getIndex()] == YES)
                    return _varA.getValueByIndex( i );
        } else
            throw new Exception( inValue.getName() + " not a member of matrix ("
                                    + getVariableA().getName() + ", "
                                    + getVariableB().getName() + ")");
        return null;
    }

    //----------------------------------------------------------------------
    //  setNegativeRelation -- if any negative relations for inValue in this
    //  matrix (valueZ), then add transitive negative relation with inValueY
    //----------------------------------------------------------------------
    public void setNegativeRelations( Value inValue, Value inValueY )
        throws Exception
    {
        if (_varA == inValue.getVariable()) {
            for (int i = 0; i < _size; i++)
                if (_matrix[inValue.getIndex()][i] == NO)
                    inValueY.setNoRelation( _varB.getValueByIndex( i ));

        } else if (_varB == inValue.getVariable()) {
            for (int i = 0; i < _size; i++)
                if (_matrix[i][inValue.getIndex()] == NO)
                    inValueY.setNoRelation( _varA.getValueByIndex( i ));
        } else
            throw new Exception( inValue.getName() + " not a member of matrix ("
                                    + getVariableA().getName() + ", "
                                    + getVariableB().getName() + ")");
    }

    //----------------------------------------------------------------------
    //  setNextPositiveRelation -- pick a relation and set it to
    //  YES.  Used for casting about for a final solution when clues are
    //  exhausted.  This should probably be done more deterministically.
    //
    //  Throws if this 'set' propagates out to an illegal operation.
    //----------------------------------------------------------------------
    public SetCommand setNextPositiveRelation()
        throws Exception, LogicFailureException
    {
        for (int i = 0; i < _size; i++ )
            for (int j = 0; j < _size; j++ )
                if (_matrix[i][j] == UNSET)
                    return set( i, j, YES );

        return null;    // no set made
    }

    //----------------------------------------------------------------------
    //  setNextPositiveRelationAfter -- find next unset relation after that
    //  given in the last command/guess.
    //
    //  Throws if this 'set' propagates out to an illegal operation.
    //----------------------------------------------------------------------
    public SetCommand setNextPositiveRelationAfter( SetCommand inCommand )
        throws Exception, LogicFailureException
    {
        boolean setNow = false;
        int ix = 0;     // start indices for next guess search.
        int jy = 0;

        if (inCommand.matrix == this) {
            ix = inCommand.x;       // start stepping through from last command
            jy = inCommand.y + 1;   // plus one, if last position in matrix then
            if (jy >= _size) {      // skip whole matrix
                jy = 0;
                ix += 1;
                if (ix >= _size)
                    return null;
            }
        }

        for (int i = ix; i < _size; i++ ) {

            if (i != ix)
                jy = 0;

            for (int j = jy; j < _size; j++ ) {
                if (_matrix[i][j] == UNSET)
                    return set( i, j, YES );
            }
        }

        return null;    // no set made
    }


    //----------------------------------------------------------------------
    // unused
    //----------------------------------------------------------------------
    static public void undo()
    {
        SetCommand command = (SetCommand) sSetCmdList.pop();
        command.undo();
    }

    //----------------------------------------------------------------------
    //  lastCommand -- return last 'set' command issued.
    //----------------------------------------------------------------------
    public static SetCommand lastCommand()
    {
        return (SetCommand) sSetCmdList.peek();
    }

    //----------------------------------------------------------------------
    //  undoToCommand -- undo all commands up to, but not including, the
    //  given command.
    //  Return the last command undone.
    //----------------------------------------------------------------------
    public static SetCommand undoToCommand( SetCommand inCommand )
    {
        SetCommand lastCommand = null;

        for (SetCommand cmd = (SetCommand) sSetCmdList.pop(); cmd != inCommand;
                        cmd = (SetCommand) sSetCmdList.pop()) {
            lastCommand = cmd;
            cmd.undo();
        }

        return lastCommand;
    }

    //----------------------------------------------------------------------
    //  toString -- print
    //----------------------------------------------------------------------
    public String toString()
    {
        StringBuffer s = new StringBuffer();

        for (int i = 0; i < _size; i++ ) {
            for (int j = 0; j < _size; j++ )
                s.append( _matrix[i][j] );
            s.append("\n");
        }
        return s.toString();
    }

}   // end of class definition





//----------------------------------------------------------------------
//  SetCommand
//
//  A command that can be undone to set a matrix cell to a particular value.
//----------------------------------------------------------------------

class SetCommand
{
    int             x, y, what;
    RelationMatrix  matrix;

    public SetCommand( RelationMatrix inMatrix, int inX, int inY, int inWhat )
    {
        x = inX;
        y = inY;
        matrix = inMatrix;
        what = inWhat;
    }

    public void undo()  {   matrix.unset( x, y );   }
    public void redo()
    {   try { matrix.set( x, y, what ); }
        catch (Exception ex) { }
    }

}
