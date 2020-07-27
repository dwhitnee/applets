//----------------------------------------------------------------------
//  Value                                       © 1997 David Whitney
//
//  A Variable has multiple values.
//
//  David Whitney                               dwhitney@cs.stanford.edu
//  September 18, 1997
//----------------------------------------------------------------------

import java.util.*;

class Value
{
    String  _name;      //  name of value e.g. "dog"
    Variable _var;      //  variable type, e.g. "pet"
    int     _index;     //  which value of the variable this is.

    //----------------------------------------------------------------------
    //  Constructor
    //----------------------------------------------------------------------
    public Value( String inName, Variable inVar, int inIndex )
    {
        _var = inVar;
        _name = inName;
        _index = inIndex;
    }

    //----------------------------------------------------------------------
    //  Accessors
    //----------------------------------------------------------------------
    public int      getIndex()      { return _index; }
    public Variable getVariable()   { return _var; }
    public String   getName()       { return _name; }

    public void     setIndex( int inIndex)      { _index = inIndex; }

    //----------------------------------------------------------------------
    //  setRelation -- these two variable are positively related.
    //  Throws if no matrix for this variable.
    //----------------------------------------------------------------------
    public void setRelation( Value inValue )
        throws Exception
    {
        if (getVariable() == inValue.getVariable())
            return;

        try {
            RelationMatrix mat = _var.getMatrixForVariable( inValue._var );
            mat.setRelation( this, inValue );
        }
        catch (Exception ex) {
            throw ex;
        }
    }

    //----------------------------------------------------------------------
    //  setNoRelation -- these two variable are negatively related.
    //  Throws if no matrix for this variable.
    //----------------------------------------------------------------------
    public void setNoRelation( Value inValue )
        throws Exception
    {
        if (getVariable() == inValue.getVariable())
            return;

        try {
            RelationMatrix mat = _var.getMatrixForVariable( inValue._var );
            mat.setNoRelation( this, inValue );
        }
        catch (Exception ex) {
            throw ex;
        }
    }

    //----------------------------------------------------------------------
    //  getRelation -- Return the relation between these two values.
    //  Throws if no matrix for this variable.
    //----------------------------------------------------------------------
    public int getRelation( Value inValue )
        throws Exception
    {
        // identity relation
        if (this == inValue)
            return RelationMatrix.YES;

        // two values of the same variable are unrelated
        if (getVariable() == inValue.getVariable())
            return RelationMatrix.NO;

        try {
            RelationMatrix mat = _var.getMatrixForVariable( inValue.getVariable() );
            return mat.getRelation( this, inValue );
        }
        catch (Exception ex) {
            throw ex;
        }
    }

    //----------------------------------------------------------------------
    //  getPositiveRelation -- Return the positive relation (if any) between
    //  this value and the given variable.
    //
    //  Throws if no matrix for this variable.
    //----------------------------------------------------------------------
    public Value getPositiveRelation( Variable inVar )
        throws Exception
    {
        if (inVar == _var)
            return null;

        try {
            RelationMatrix mat = _var.getMatrixForVariable( inVar );
            return mat.getPositiveRelation( this );
        }
        catch (Exception ex) {
            throw ex;
        }
    }

    //----------------------------------------------------------------------
    //  toString -- print
    //----------------------------------------------------------------------
    public String toString()
    {
        return getName();
    }

}   // end of class
