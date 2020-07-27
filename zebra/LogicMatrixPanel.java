//----------------------------------------------------------------------
//  LogicMatrixPanel                            © 1997 David Whitney
//
//  A panel that displays a grid of correlations between variables.
//
//  David Whitney
//  September 23, 1997
//----------------------------------------------------------------------

import java.awt.*;


public class LogicMatrixPanel extends Panel
{
    Point       _gridSize;
    LogicEngine _engine;
    int         _space;
    boolean     _partialUpdate;

    //----------------------------------------------------------------------
    //  Constructor
    //----------------------------------------------------------------------
    public LogicMatrixPanel( LogicEngine inEngine )
    {
        _gridSize = new Point( 0, 0);
        _engine = inEngine;

        setBackground( Color.green );
    }

    public void setPartialUpdate( boolean inFlag )  { _partialUpdate = inFlag; }

    //----------------------------------------------------------------------
    //  getPreferredSize -- 10 spaces for text and values^2 boxes
    //----------------------------------------------------------------------
    public Dimension preferredSize()
    {
        int f = 12 + _engine.getNumValues() * _engine.getNumValues();

        return new Dimension( _gridSize.x * f, _gridSize.y * f );
    }

    //----------------------------------------------------------------------
    //  update
    //----------------------------------------------------------------------
    public void update( Graphics inGfx )
    {
        paint( inGfx );     // don't erase
    }

    public void repaint( int x, int y, int width, int height )
    {
        _partialUpdate = true;
        super.repaint( x, y, width, height );
    }

    //----------------------------------------------------------------------
    //  addNotify - initGraphics
    //----------------------------------------------------------------------
    public void addNotify()
    {
        super.addNotify();

        setFont( new Font("Times", Font.PLAIN, 14) );

        FontMetrics fm = getGraphics().getFontMetrics();

        _space = fm.charWidth('X') / 2;     // extra, eye-pleasing white space
        _gridSize.x = fm.charWidth('X') + _space;
        _gridSize.y = fm.getHeight() + _space;

        System.out.println("Grid size = " + _gridSize );


        // isn't there a nice way to force a re-layout?  This works
        // but it's a hack

//      validate();     // validate does not have the effect of re-layout
                        // like a manual resize does
//      repaint();

//      getParent().layout();
    }

    //----------------------------------------------------------------------
    //  paint
    //----------------------------------------------------------------------
    public void paint( Graphics inGfx )
    {
    //  System.out.println("Painting with partial = " + _partialUpdate );
        boolean newGuy = false;

        int numVars = _engine.getNumVars();
        int numValues = _engine.getNumValues();

        int varBoxWidth  = numValues * _gridSize.x + 1;
        int varBoxHeight = numValues * _gridSize.y + 1;

        int xOffset = _gridSize.x * 10;
        int yOffset = _gridSize.y * 10;

        Variable    varList[] = _engine.getVarList();
        StringBuffer relationString = new StringBuffer("E");    // X, O, Error, ' '

        FontMetrics fm = inGfx.getFontMetrics();

        inGfx.setColor( Color.black );

        for (int vy = 0; vy < numVars-1; vy++ ) {

          // right justify.  Put in BorderLayout?

          for (int vx = vy; vx < numVars-1; vx++ ) {

            Variable varA = varList[vy];
            Variable varB = varList[numVars - (vx-vy) - 1];

            for (int ly = 0; ly < numValues; ly++) {

                if ( vx == vy )
                    inGfx.drawString( varList[vy].getValueByIndex( ly ).getName(),
                        xOffset - _space -
                                fm.stringWidth( varList[vy].getValueByIndex( ly ).getName()),
                        yOffset + vy * varBoxHeight + (ly+1) * _gridSize.y
                                - _space/2 - 1 );

              for (int lx = 0; lx < numValues; lx++) {
                inGfx.drawRect(
                    xOffset + (vx-vy) * varBoxWidth + lx * _gridSize.x,
                    yOffset +   vy * varBoxHeight + ly * _gridSize.y,
                    _gridSize.x, _gridSize.y );

                try {
                    int relation = varA.getValueByIndex( ly ).getRelation( varB.getValueByIndex( lx ));

                    if (relation == RelationMatrix.YES)
                        relationString.setCharAt(0, 'O');
                    else if (relation == RelationMatrix.NO)
                        relationString.setCharAt(0, 'X');
                    else
                        relationString.setCharAt(0, ' ');
                }
                catch (Exception ex) {
                    relationString.setCharAt(0, 'E');
                }

                if (((varA.getValueByIndex( ly ) == RelationMatrix.getMostRecentValueA()) ||
                     (varA.getValueByIndex( ly ) == RelationMatrix.getMostRecentValueB())) &&
                    ((varB.getValueByIndex( lx ) == RelationMatrix.getMostRecentValueA()) ||
                     (varB.getValueByIndex( lx ) == RelationMatrix.getMostRecentValueB()))) {

                    inGfx.setColor( Color.red );
                    newGuy = true;      // new X/O to draw
                }

                if ((!_partialUpdate) || (newGuy)) {
                    inGfx.drawString( relationString.toString(),
                        xOffset + (vx-vy) * varBoxWidth + lx * _gridSize.x
                                                            + _space/2 + 1,
                        yOffset +  vy * varBoxHeight + (ly+1) * _gridSize.y
                                                        - _space/2 );

                    inGfx.setColor( Color.black );
                    newGuy = false;
                }
              }
            }
          }
        }

        if (!_partialUpdate) {
            for (int vx = 0; vx < numVars-1; vx++ )
                for (int i = 0; i < numValues; i++ )
                    drawVertString( inGfx, varList[numVars-vx-1].getValueByIndex( i ).getName(),
                                    xOffset + vx*varBoxWidth + (i *
                                    _gridSize.x) + _space/2,
                                     yOffset - _space );
        }
        _partialUpdate = false;
    }

    //----------------------------------------------------------------------
    //  drawVertString
    //----------------------------------------------------------------------
    void drawVertString( Graphics inGfx, String inStr, int inX, int inY )
    {
        int x = inX;
        int y = inY - inStr.length() * (_gridSize.y - 7);   // lose leading

        for (int i = 0; i < inStr.length(); i++ )
            inGfx.drawString( String.valueOf (inStr.charAt(i) ),
                              x, y + (i+1) * (_gridSize.y - 7));
    }


    //----------------------------------------------------------------------
    //  main test program
    //----------------------------------------------------------------------
/*
    public static void main( String args[] )
    {
        Frame f = new Frame("grid test");
        f.setLayout( new FlowLayout() );
        f.add( new LogicMatrixPanel() );
        f.resize( 100,600 );
        f.pack();
        f.show();
    }
*/
}   // end of class

