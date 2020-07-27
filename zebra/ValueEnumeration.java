//----------------------------------------------------------------------
// ValueEnumeration                                 � 1997 David Whitney
//
// Enumerates the values a variables has.
//
//  Ex:
//    Variable var;
//    for (ValueEnumeration e = var.elements() ; e.hasMoreValues() ;)
//        String s = e.nextValue();
//
//  David Whitney       dwhitney@cs.stanford.edu
//  September 18, 1997
//----------------------------------------------------------------------

import java.util.*;

import Variable;
import Value;

public class ValueEnumeration
{
    Enumeration     _enum;

    public ValueEnumeration( Variable inVar )
    {
        _values = inVar.getValueVector();
        _enum = _values.elements();
    }
    public boolean hasMoreValues() { return _enum.hasMoreElements(); }
    public Value nextValue() {      return (Value) _enum.nextElement(); }

}       // end of class definition

