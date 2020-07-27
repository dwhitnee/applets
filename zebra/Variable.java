//----------------------------------------------------------------------
//  Variable                                    © 1997 David Whitney
//
//
//  David Whitney                               dwhitney@cs.stanford.edu
//  September 18, 1997
//----------------------------------------------------------------------

import java.util.*;

import ValueEnumeration;
import RelationMatrix;

public class Variable
{
    String  _name;          // unique string defining variable
    Vector  _valueList;     // values of this varaible
    Vector  _matrixList;    // relation matrices between this variables and others

    //----------------------------------------------------------------------
    //  Constructor
    //----------------------------------------------------------------------
    public Variable( String inName )
    {
        _name = new String( inName );
        _valueList = new Vector();
        _matrixList = new Vector(); // should be a Set
    }

    //----------------------------------------------------------------------
    //  Accessors
    //----------------------------------------------------------------------
    public String   getName()           { return _name; }
    public int      getValueCount()     { return _valueList.size(); }
    public Vector   getValueVector()    { return _valueList; }
    public Vector   getMatrixVector()   { return _matrixList; }
    public ValueEnumeration elements()  { return new ValueEnumeration( this ); }

    //----------------------------------------------------------------------
    //  getValueBy* -- retrieve one of variables values
    //----------------------------------------------------------------------
    public Value getValueByIndex( int inIndex )
    {
        return (Value) _valueList.elementAt( inIndex );
    }

    public Value getValueByName( String inName )
        throws Exception
    {
        Value val;
        for (ValueEnumeration e = elements() ; e.hasMoreValues() ; ) {
            val = e.nextValue();
            if (val.getName().equals( inName ))
                return val;
        }

        throw new Exception("Variable '" + _name + "' has no value '" + inName
                            + "'");
    }

    //----------------------------------------------------------------------
    //  addValueBy* -- give variable another value.
    //----------------------------------------------------------------------
    public void     addValue( Value inValue )
    {
        inValue.setIndex( _valueList.size() );
        _valueList.addElement( inValue );
    }
    public void     addValueByName( String inValueName )
    {
        Value value = new Value( inValueName, this, _valueList.size() );
        _valueList.addElement( value );
    }

    //----------------------------------------------------------------------
    public void     addMatrix( RelationMatrix inMatrix )
    {
        _matrixList.addElement( inMatrix );
    }

    //----------------------------------------------------------------------
    //  getMatrixForVariable -- find Relation Matrix for both this Variable
    //  and the input Variable.
    //
    //  Throws if variables identical, or no matrix for some other reason
    //----------------------------------------------------------------------
    public RelationMatrix getMatrixForVariable( Variable inOtherVariable )
        throws Exception
    {
        if (this != inOtherVariable) {
            // search for our matrix that relates us to other Variable.
            for (Enumeration e = _matrixList.elements() ; e.hasMoreElements() ; ) {
                RelationMatrix mat = (RelationMatrix) e.nextElement();
                if (mat.relates( inOtherVariable ))
                    return mat;
            }
        }
        throw new Exception("No Matrix found in " + _name +
                            " corresponding to " + inOtherVariable );
    }

    //----------------------------------------------------------------------
    //  containsValue -- returns true if the variable has arg as a value.
    //----------------------------------------------------------------------
    public boolean hasValueName( String inValueName )
    {
        for (ValueEnumeration e = elements() ; e.hasMoreValues() ;)
            if (inValueName.equals( e.nextValue().getName() ))
                return true;
        return false;
    }


    //----------------------------------------------------------------------
    //  toString -- print
    //----------------------------------------------------------------------
    public String toString()
    {
        StringBuffer s = new StringBuffer( _name + ": ");
        for (ValueEnumeration e = elements() ; e.hasMoreValues() ;)
           s.append( e.nextValue() + " ");
        return s.toString();
    }

}   // end of class definition
