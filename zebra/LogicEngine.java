//----------------------------------------------------------------------
//  LogicEngine                                 © 1997 David Whitney
//
//  Given a number of clues and a numberof variables, figure out a
//  logical solution.
//
//  David Whitney                               dwhitney@cs.stanford.edu
//  September 18, 1997
//----------------------------------------------------------------------

import java.util.*;
import java.awt.*;


public class LogicEngine implements Runnable
{
    int             _numVars;       // number of variables
    int             _numValues;     // number of values each variable can have
    Variable        _varList[];     // list of problem's variables and their values
    RelationMatrix  _matrixList[];
    String          _solution[];    // list of solution n-tuples
    Vector          _relationList;  // user defined relations
    String          _clues[];
    Vector          _relativeClues;     // clues that may need to be reapplied
    boolean         _firstpass;         // true if first pass through clues
    int             _answerID;          // clue id of ANSWER
    int             _currentClue;       // clue id we're currently working on
    Stack           _markStack;         // list of undoable guesses we've made

    Variable    _solutionVar = null;        // variable to solve for (e.g. person owns zebra)
    String      _solutionValue = null;      // value variable should equal in solution
    Vector      _answerVariableOrder;           // order to print solution values

    int _matrixIndex = 0;
    int _varIndex = 0;

    //----------------------------------------------------------------------
    //  Construct space for variables, variable relation matrices, and solution
    //----------------------------------------------------------------------
    public LogicEngine( int inNumVars, String[] inClues )
    {
        _clues = inClues;
        _numVars = inNumVars + 1;      // add one for implied "position" variable
        _numValues = inNumVars;
        _varList = new Variable[_numVars];
        _solution = new String[_numVars];
        _matrixList = new RelationMatrix[ (_numVars * (_numVars-1))/2 ];    // n(n-1)/2
        _relationList = new Vector();
        _relativeClues = new Vector();
        _answerVariableOrder = new Vector();
        _markStack = new Stack();
    }

    //----------------------------------------------------------------------
    //  Accessors
    //----------------------------------------------------------------------
    public Variable[]   getVarList()    { return _varList; }
    public int          getClueID()     { return _currentClue; }
    public int          getNumVars()    { return _numVars; }
    public int          getNumValues()  { return _numValues; }

    //----------------------------------------------------------------------
    //  setWaitOnChange -- Say whether to sleep and wait to be woken when a
    //  new relation occurs (for monitoring).
    //----------------------------------------------------------------------
    public void setWaitOnChange( boolean inPauseFlag )
    {
        RelationMatrix.setWaitOnChange( inPauseFlag );
    }

    //----------------------------------------------------------------------
    //  Main Thread -- build structures from clues and solve
    //----------------------------------------------------------------------
    public void run()
    {
        try {
            buildVarList( _clues );     // first pass
            initRelationMatrices();
            generateSolution( _clues ); // second pass
        }
        catch (Exception ex) {
            System.err.println("Bad Clues: " + ex);
        }

        System.out.println("\nSOLUTION: \n" + getSolutionString() + "\n");
    }

    //----------------------------------------------------------------------
    //  isSolved -- determine if we've found a relation for all values.
    //----------------------------------------------------------------------
    public boolean isSolved()
    {
        for (int i = 0; i < _matrixList.length; i++ )
            if (!_matrixList[i].isFullyRelated())
                return false;

        return true;
    }

    //----------------------------------------------------------------------
    //  getSolutionString -- put solution list into a single String
    //----------------------------------------------------------------------
    public String getSolutionString()
    {
        Variable thisVar = null;
        Value value = null;
        RelationMatrix mat = null;
        StringBuffer solution = new StringBuffer();

        Variable firstVar = (Variable) _answerVariableOrder.elementAt( 0 );

        for (int i = 0; i < _numValues; i++) {
            for (Enumeration e = _answerVariableOrder.elements() ; e.hasMoreElements() ; ) {

                thisVar = (Variable) e.nextElement();

                if (thisVar == firstVar) {
                    value = firstVar.getValueByIndex( i );
                    solution.append( value + "\t");
                } else {
                    try {
                        mat = thisVar.getMatrixForVariable( firstVar );
                        solution.append( mat.getPositiveRelation( value ) + "\t" );
                    } catch (Exception ex) { System.err.println( ex ); }
                }
            }
            solution.append("\n");
        }

        return solution.toString();
    }

    //----------------------------------------------------------------------
    //  generateSolution -- engine tourine.
    //  Take info from clues and put into engine to infer answer.
    //----------------------------------------------------------------------
    public void generateSolution( String inClues[] )
        throws Exception
    {
        boolean validInput = true;
        _firstpass = true;

        for (int i = 0; i < inClues.length; i++) {
            try {
                _currentClue = i;
                processRelation( inClues[i] );
            }
            catch (Exception ex) {
                validInput = false;
                System.err.println( "ERROR: Clue # " + i + ":" + ex.getMessage() );
            }
        }

        if (validInput) {

            _firstpass = false;

            if ((_solutionValue == null) || (_answerVariableOrder.size() == 0))
                throw new Exception("Missing SOLVE or ANSWER tag.");

             // these clues depend on the current relations, so repply them whenever
             // relation matrices change

            boolean solved = isSolved();
            boolean guessing = false;
            boolean undoGuess = false;

            while (!solved) {
                // go as far as we can with these clues and state
                try {
                    if (undoGuess) {
                        undoGuessAndTryNext();
                        undoGuess = false;
                    }

                    while (RelationMatrix.getChanged() == true) {
                        reapplyRelativeClues();
                        RelationMatrix.setChanged( false );
                    }

                    // if that didn't solve it, make a guess and try again
                    if (!isSolved()) {
                        guessing = true;
                        makeGuess();
                    } else
                        solved = true;
                }
                catch (LogicFailureException ex) {
                    // if we're here then there was a logic failure.
                    // probably because of a bad guess
                    if (!guessing)
                        throw ex;
                    else
                        undoGuess = true;   // try again in try block.

                }
            }


            // done.
            for (int i = 0; i < _clues.length; i++ )
                if (_clues[i].indexOf("ANSWER") > 0)
                    _currentClue = i;

        } else
            throw new Exception("ERROR: illegal input");
    }

    //----------------------------------------------------------------------
    //  reapplyRelativeClues -- clues that are relative need to be reapplied
    //  after changes occur.  Make a single pass of these clues.
    //----------------------------------------------------------------------
    void reapplyRelativeClues()
        throws Exception
    {
        for (Enumeration e = _relativeClues.elements() ; e.hasMoreElements() ; ) {
            String clue = (String) e.nextElement();
            for (int i = 0; i < _clues.length; i++ )
                if (_clues[i] == clue)
                    _currentClue = i;
            processRelation( clue );
        }
    }


    //----------------------------------------------------------------------
    //  makeGuess -- Randomly choose a relation to make and see if it
    //  results in a legal solution.
    //----------------------------------------------------------------------
    public void makeGuess()
        throws Exception
    {
        SetCommand command;

        System.out.println("Making a guess");
    //  setWaitOnChange( true );

        for (int i = 0; i < _matrixList.length; i++ ) {
            if (!_matrixList[i].isFullyRelated()) {
                _markStack.push( RelationMatrix.lastCommand() );    // set undo mark
                command = _matrixList[i].setNextPositiveRelation();
                return;
            }
        }
        throw new Exception("Could not make any logically consistent guesses");
    }

    //----------------------------------------------------------------------
    //  undoGuessAndTryNext -- Last guess was a bad one, undo all changes
    //  back to that guess, then make a different, later guess.
    //----------------------------------------------------------------------
    public void undoGuessAndTryNext()
        throws Exception
    {
        SetCommand mark = (SetCommand) _markStack.pop();
        SetCommand lastGuess, nextGuess;
        boolean guessNow = false;

        System.out.println("Undoing guess.  Making another one.");
        Thread.yield();

        // undo all commands up to, but not including the command before the
        // last guess.  Get the first command made in that stream,
        // i.e. the last guess.

        lastGuess = RelationMatrix.undoToCommand( mark );

        _markStack.push( RelationMatrix.lastCommand() );    // set undo mark

        for (int i = 0; i < _matrixList.length; i++ ) {

            // dont make a guess before the last guess
            if (_matrixList[i] == lastGuess.matrix)
                guessNow = true;

            if (guessNow) {
                if (!_matrixList[i].isFullyRelated()) {
                    nextGuess =
                        _matrixList[i].setNextPositiveRelationAfter( lastGuess );

                    if (nextGuess != null)  // successful guess
                        return;
                    // else keep going until a valid guess can be made
                }
            }
        }

        // we've exhausted the guesses possible at this level, move up the
        //  guess stack and try again.  If stack is empty then we're screwed.
        //  Either no consistent solution or we've got an error.

        if (!_markStack.empty()) {
            undoGuessAndTryNext();
        } else {
            // if we got here, no guess was made.  Either an error or it's solved,
            // but we shouldn't be called if it was solved.
            throw new Exception("Could not make any logically consistent guesses");
        }
    }

    //----------------------------------------------------------------------
    //  initRelationMatrices -- assign each variable pair a relation matrix.
    //----------------------------------------------------------------------
    void initRelationMatrices()
    {
        Variable variable;
        int matIndex = 0;

        for (int i = 0; i < _numVars; i++ ) {
            for (int j = i+1; j < _numVars; j++) {
                RelationMatrix m = new RelationMatrix( _varList[i], _varList[j] );
                _varList[i].addMatrix( m );
                _varList[j].addMatrix( m );
                _matrixList[matIndex++] = m;
            }
        }
    }


    //----------------------------------------------------------------------
    //  find the variables and their values from the clue list.  ("ISA" relations)
    //----------------------------------------------------------------------
    public void buildVarList( String inClues[] )
    {
        for (int i = 0; i < inClues.length; i++) {

            StringTokenizer tokenizer = new StringTokenizer( inClues[i] );

            if (tokenizer.countTokens() != 3)   // must be a SOLVE, ANSWER, or error
                break;                          // handle in generateSolution()

            String a = tokenizer.nextToken();
            String relation = tokenizer.nextToken();
            String b = tokenizer.nextToken();

            // if ISA, add var to varList
            if (relation.equals("ISA"))         // value ISA variable
                addVariableAndValue( b, a );
        }

        //  Add a phantom variable for the special IS_LOCATED relation
        for (int i = 0; i < _numValues; i++ )
            addVariableAndValue("@position", Integer.toString( i+1 ));
    }


    //----------------------------------------------------------------------
    //  addVariableAndValue -- search list for existing variable name,
    //  if variable doesn't yet exist by this name, create it.
    //  Add new value for this variable.
    //----------------------------------------------------------------------
    void addVariableAndValue( String inVarName, String inValueName )
    {
        Variable theVar = null;

        for (int i = 0; (i < _numVars) && (_varList[i] != null); i++ ) {
            if (_varList[i].getName().equals( inVarName )) {
                theVar = _varList[i];
                break;
            }
        }
        if (theVar == null) {
            theVar = new Variable( inVarName );
            _varList[_varIndex++] = theVar;
        }
        theVar.addValueByName( inValueName );
    }

    //----------------------------------------------------------------------
    //  find all other relations in the clues
    //----------------------------------------------------------------------
    public void processRelation( String inClue )
        throws Exception
    {
//      System.out.println( inClue );

        StringTokenizer tokenizer = new StringTokenizer( inClue );

        String a = tokenizer.nextToken();

        if (a.equals("SOLVE"))
            parseWhatToSolveFor( tokenizer );

        else if (a.equals("ANSWER"))
            parseAnswerFormat( tokenizer );

        else if (tokenizer.countTokens() != 2)  // should be 2 tokens left
            throw new Exception("More than three strings in input string:" +
                                    inClue );
        else {      // a relation clue

            String relation = tokenizer.nextToken();
            String b = tokenizer.nextToken();

            if (relation.equals("IS_LOCATED"))
                makePositionRelation( a, b );

            else if ((relation.equals("NEXT_TO")) ||
                (relation.equals("IMMED_RIGHT_OF")) ||
                (relation.equals("IMMED_LEFT_OF")))
            {
                if (_firstpass)     // save relative clues for later reapplication
                    _relativeClues.addElement( inClue );

                makeRelativePositionRelation( a, b, relation );

            } else if (relation.equals("ISA")) {
                // skip definitions, handled elsewhere

            } else {        // found a user defined relation
                handleUserRelation( a, b, relation );
            }
        }
    }


    //----------------------------------------------------------------------
    //  handleRelation -- given a user-defined relation, if this is the first
    //  mention of the relation then it is a definition, otherwise it is an
    //  application of the relation.
    //----------------------------------------------------------------------
    void handleUserRelation(    String inValueA, String inValueB,
                                String inRelation )
        throws Exception
    {
        boolean relationExists = false;

        // see if this relation is already defined
        for (Enumeration e = _relationList.elements() ; e.hasMoreElements() ; ) {
            String existingRelation = (String) e.nextElement();
            if (existingRelation.equals( inRelation ))
                relationExists = true;
        }

        if (relationExists) {
            makeRelationBetweenValues( inValueA, inValueB );    // apply relation
            // throws on error

        } else {                                    // define relation
            try {
                getVariableByName( inValueA );      // verify a and b are
                getVariableByName( inValueB );      // valid variables
                _relationList.addElement( inRelation );
            }
            catch (Exception ex) {      // a or b are not valid variables
                throw new Exception("ERROR: Relation '" + inRelation +
                        "' not defined " + " before used.");
            }
        }
    }

    //----------------------------------------------------------------------
    //  makeLocationRelation -- handle IS_LOCATED relation using the
    //  position variable.
    //  propagate all results throughout all relation matrices.
    //
    //  Exception thrown when the parser believe the input is in a bad format.
    //----------------------------------------------------------------------
    void makePositionRelation( String inValueNameA, String inWhere )
        throws Exception
    {
        int pos;

        // set an absolute location
        if (inWhere.equals("AT_LEFT"))
            pos = 1;
        else if (inWhere.equals("AT_RIGHT"))
            pos = _numValues;
        else if (inWhere.equals("IN_MIDDLE")) {
            pos = (_numValues+1) / 2;
            if ((_numValues % 2) == 0)
                throw new Exception("Can't set value IN_MIDDLE when there are" +
                    " an even number of values: " + inValueNameA + " IS_LOCATED " +
                     inWhere);
        } else
            throw new Exception("ERROR: Bad value :" + inValueNameA +
                        " IS_LOCATED " + inWhere);

        makeRelationBetweenValues( inValueNameA, Integer.toString( pos ));
    }

    //----------------------------------------------------------------------
    //  makeRelativePositionRelation --
    //  handle NEXT_TO, IMMED_RIGHT_OF, IMMED_LEFT_OF
    //
    //  Relative relations need to be reapplied after changes occur
    //  to the matrices since new information could increase the usefulness
    //  of these rules.
    //----------------------------------------------------------------------
    void makeRelativePositionRelation(  String inValueNameA,
                                        String inValueNameB,
                                        String inRelation )
        throws Exception
    {
        if (inRelation.equals("NEXT_TO") ||
            inRelation.equals("IMMED_RIGHT_OF") ||
            inRelation.equals("IMMED_LEFT_OF")) {

            Variable a = getVariableByValueName( inValueNameA );
            Variable b = getVariableByValueName( inValueNameB );

            Value valueA = a.getValueByName( inValueNameA );
            Value valueB = b.getValueByName( inValueNameB );

            calcRelativePosition( valueA, valueB, inRelation );

        } else
            throw new Exception("ERROR: Bad relative relation :" + inValueNameA + " " +
                        inRelation + " " + inValueNameB);

    }

    //----------------------------------------------------------------------
    //  calcRelativePosition -- handle values positioned in relation to each
    //  other.
    //  Search for all positive relations to A and B and set their positions, too.
    //
    //  This relation should be repeated later when more relations are set
    //  because of other clues.
    //
    //  Throws if lower level routines are hosed (e.g. no matrix for vars)
    //----------------------------------------------------------------------
    void calcRelativePosition(  Value inValueA, Value inValueB,
                                String inRelation )
        throws Exception
    {
        Value relatedValue;

        // if one has a position then we know the position of the other
        if (inRelation.equals("IMMED_RIGHT_OF") ||
            inRelation.equals("IMMED_LEFT_OF")) {

            // if one position is known then set the other's
            setPositionRelativeToFixedValue( inValueA, inValueB, inRelation );

            // find all positively related values and set their positions as well
            for (int i = 0; i < _varList.length; i++) {

                if (inValueA.getVariable() != _varList[i]) {
                    relatedValue = inValueA.getPositiveRelation( _varList[i] );
                    if (relatedValue != null)
                        setPositionRelativeToFixedValue( relatedValue, inValueB, inRelation );
                }

                if (inValueB.getVariable() != _varList[i]) {
                    relatedValue = inValueB.getPositiveRelation( _varList[i] );
                    if (relatedValue != null)
                        setPositionRelativeToFixedValue( inValueA, relatedValue, inRelation );
                }
            }

        } else if (inRelation.equals("NEXT_TO")) {
            handleNextTo( inValueA, inValueB );

        } else
            throw new Exception("ERROR: Bad relation (" + inRelation +
                        ") passed to calcRelativePosition()");
    }

    //----------------------------------------------------------------------
    //  handleNextTo -- NEXT_TO means that these values are adjacent, but we don't
    //  know which side, so if anyone knows where they are we can limit
    //  ourselves to the adjacent positions
    //----------------------------------------------------------------------
    void handleNextTo( Value inValueA, Value inValueB )
        throws Exception
    {
        Value relatedValue;

        eliminateNonAdjacentPositions( inValueA, inValueB );

        // find all positively related values and set their positions as well
        for (int i = 0; i < _varList.length; i++) {

            if (inValueA.getVariable() != _varList[i]) {
                relatedValue = inValueA.getPositiveRelation( _varList[i] );
                if (relatedValue != null)
                    eliminateNonAdjacentPositions( relatedValue, inValueB );
            }

            if (inValueB.getVariable() != _varList[i]) {
                relatedValue = inValueB.getPositiveRelation( _varList[i] );
                if (relatedValue != null)
                    eliminateNonAdjacentPositions( inValueA, relatedValue );
            }
        }
    }

    //----------------------------------------------------------------------
    //  eliminateNonAdjacentPositions -- these two values are next to each other.
    //  Therefore they are not related, and if one has a position then we can
    //  eliminate all but two positions for the other.
    //
    //  both values mave have positions already, but check to see if
    //  this clue is consistent with those values.
    //----------------------------------------------------------------------
    void eliminateNonAdjacentPositions( Value inValueA, Value inValueB )
        throws Exception
    {
        int pos;
        Variable positionVar = getVariableByName("@position");

        // we know these values cannot be related directly
        if (inValueA.getVariable() != inValueB.getVariable())
            inValueA.setNoRelation( inValueB );

        Value positionValA = inValueA.getPositiveRelation( positionVar );
        Value positionValB = inValueB.getPositiveRelation( positionVar );

        // both values have non-identical positions already, no need to check.
        if ((positionValA != null) && (positionValB != null) &&
            (positionValA.getIndex() != positionValB.getIndex()))
            return;

        //  eliminate all position relations except those immediately next door
        if (positionValA != null) {
            pos = positionValA.getIndex();
            for (int p = 0; p < _numValues; p++)
                if ((p != pos-1) && (p != pos+1))
                    inValueB.setNoRelation( positionVar.getValueByIndex( p ));

        }

        // do again for other variable
        if (positionValB != null) {
            pos = positionValB.getIndex();
            for (int p = 0; p < _numValues; p++)
                if ((p != pos-1) && (p != pos+1))
                    inValueA.setNoRelation( positionVar.getValueByIndex( p ));

        }
    }


    //----------------------------------------------------------------------
    //  setPositionRelativeToFixedValue -- handle IMMED_RIGHT_OF and IMMED_LEFT_OF.
    //  If one value has a position set then we know the position of the other.
    //
    //  Throws if lower level routines are hosed (e.g. no matrix for vars)
    //----------------------------------------------------------------------
    void setPositionRelativeToFixedValue( Value inValueA, Value inValueB, String inRelation )
        throws Exception
    {
        if (inValueA == inValueB)
            return;

        // we know these values cannot be related directly
        if (inValueA.getVariable() != inValueB.getVariable())
            inValueA.setNoRelation( inValueB );

        Variable positionVar = getVariableByName("@position");
        Value positionVal;

        // cannot be at ends
        if (inRelation.equals("IMMED_RIGHT_OF")) {
            inValueA.setNoRelation( positionVar.getValueByIndex( 0 ));
            inValueB.setNoRelation( positionVar.getValueByIndex( _numValues - 1 ));
        }

        if (inRelation.equals("IMMED_LEFT_OF")) {
            inValueA.setNoRelation( positionVar.getValueByIndex( _numValues - 1));
            inValueB.setNoRelation( positionVar.getValueByIndex( 0 ));
        }

        // Check for position of value A first
        positionVal = inValueA.getPositiveRelation( positionVar );

        if (positionVal != null) {
            int pos = positionVal.getIndex();

            // a is just right of b, set B
            if (inRelation.equals("IMMED_RIGHT_OF") && (pos > 0))
                inValueB.setRelation( positionVar.getValueByIndex( pos-1 ));

            // a is just left of b, set B
            if (inRelation.equals("IMMED_LEFT_OF") && (pos < (_numValues-1)))
                inValueB.setRelation( positionVar.getValueByIndex( pos+1 ));
        }

        // Now check position of value B
        positionVal = inValueB.getPositiveRelation( positionVar );

        if (positionVal != null) {
            int pos = positionVal.getIndex();

            // a is just right of b, set A
            if (inRelation.equals("IMMED_RIGHT_OF") & (pos < (_numValues-1)))
                inValueA.setRelation( positionVar.getValueByIndex( pos+1 ));

            // a is just left of b, set A
            if (inRelation.equals("IMMED_LEFT_OF") && (pos > 0))
                inValueA.setRelation( positionVar.getValueByIndex( pos-1 ));
        }

        // for all negative relations to positions
        propagateNegativePositions( inValueA, inValueB, inRelation );
    }

    //----------------------------------------------------------------------
    // propagateNegativePositions -- if one of the two values is known to
    //  *not* be in a particular position, then certain positions of the
    //  other value can be ruled out as well.
    //
    //  Throws if can't find position variable
    //----------------------------------------------------------------------
    void propagateNegativePositions( Value inValueA, Value inValueB,
                                     String inRelation )
        throws Exception
    {
        Variable positionVar = getVariableByName("@position");

        // iterate over all possible positions
        for (int i = 0; i < _numValues; i++) {

            // Check for impossible positions of value B
            int relation = inValueB.getRelation( positionVar.getValueByIndex( i ));

            if (relation == RelationMatrix.NO) {
                // Set where A cannot be either
                if (inRelation.equals("IMMED_RIGHT_OF") && (i < (_numValues-1)))
                    inValueA.setNoRelation( positionVar.getValueByIndex( i+1 ));
                if (inRelation.equals("IMMED_LEFT_OF") && (i > 0))
                    inValueA.setNoRelation( positionVar.getValueByIndex( i-1 ));
            }

            // Check for impossible positions of value A
            relation = inValueA.getRelation( positionVar.getValueByIndex( i ));

            if (relation == RelationMatrix.NO) {
                // Set where B cannot be either
                if (inRelation.equals("IMMED_RIGHT_OF") && (i > 0))
                    inValueB.setNoRelation( positionVar.getValueByIndex( i-1 ));
                if (inRelation.equals("IMMED_LEFT_OF") && (i < (_numValues-1)))
                    inValueB.setNoRelation( positionVar.getValueByIndex( i+1 ));
            }
        }
    }

    //----------------------------------------------------------------------
    //  makeRelationBetweenValues -- handle an absolute relation,
    //  propagate all results throughout all relation matrices.
    //
    //  Throws when relation is declared without two valid variables.
    //----------------------------------------------------------------------
    void makeRelationBetweenValues( String inValueNameA, String inValueNameB )
        throws Exception
    {
        try {   // look up objects in our tables
            Variable a = getVariableByValueName( inValueNameA );
            Variable b = getVariableByValueName( inValueNameB );

            Value valueA = a.getValueByName( inValueNameA );
            Value valueB = b.getValueByName( inValueNameB );

            valueA.setRelation( valueB );
        }
        catch (Exception ex) {
    //      System.err.println( ex );
            throw ex;
        }
    }

    //----------------------------------------------------------------------
    //  parseWhatToSolveFor -- Look for "SOLVE variable relation value"
    //  e.g. "person owns zebra"
    //----------------------------------------------------------------------
    public void parseWhatToSolveFor( StringTokenizer inTokenizer )
        throws Exception
    {
        _solutionVar = getVariableByName( inTokenizer.nextToken() );
        String relation = inTokenizer.nextToken();
        _solutionValue = inTokenizer.nextToken();
    }

    //----------------------------------------------------------------------
    //  parseAnswerFormat -- what order of variables to print solution values.
    //----------------------------------------------------------------------
    public void parseAnswerFormat( StringTokenizer inTokenizer )
        throws Exception
    {
        while (inTokenizer.hasMoreTokens())
            _answerVariableOrder.addElement( getVariableByName( inTokenizer.nextToken() ));
    }


    //----------------------------------------------------------------------
    //  getVariableByName -- find variable string name in list and return object.
    //  Returns null if no variable by that name.
    //----------------------------------------------------------------------
    public Variable getVariableByName( String inVarName )
        throws Exception
    {
        for (int i = 0; i < _numVars; i++)
            if (_varList[i].getName().equals( inVarName ))
                return _varList[i];

        throw new Exception("ERROR: Cannot find variable with the name: " +
                            inVarName);
    }

    //----------------------------------------------------------------------
    //  getVariableByValue -- find variable that this value is a value of.
    //  Returns variable belonging to this value.
    //  Returns null if no value by that name.
    //----------------------------------------------------------------------
    public Variable getVariableByValueName( String inValueName )
        throws Exception
    {
        for (int i = 0; i < _numVars; i++)
            if (_varList[i].hasValueName( inValueName ))
                return _varList[i];

        throw new Exception("ERROR: Cannot find variable with the value " +
                            inValueName);
    }

    //----------------------------------------------------------------------
    //  toString -- print
    //----------------------------------------------------------------------
    public String toString()
    {
        StringBuffer s = new StringBuffer();

        try {
            s.append("Variables: \n");
            for (int i = 0; i < _numVars; i++ )
                s.append( _varList[i].getName() + "\n" );

            s.append("\nRelations: ");
            for (Enumeration e = _relationList.elements() ; e.hasMoreElements() ; )
                s.append( (String) e.nextElement() + " ");

            s.append("\n\n");

            for (int i = 0; i < _numVars; i++ ) {
                for (int j = i+1; j < _numVars; j++) {
                    s.append(   _varList[i].getName() + " " +
                                _varList[j].getName() + "\n" +
                                _varList[i].getMatrixForVariable( _varList[j] ));
                }
            }
            s.append("\nSolving for " + _solutionValue + " relates to which "
                        + _solutionVar.getName() + "\n");
            s.append("Solution in format: ");
            for (Enumeration e = _answerVariableOrder.elements() ; e.hasMoreElements() ; )
                s.append( ((Variable) e.nextElement()).getName() + " ");

        }
        catch (Exception ex) {
            System.err.println("Error printing Engine: " + ex );
        }
        return s.toString();
    }

}



