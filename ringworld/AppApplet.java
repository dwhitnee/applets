/*
	Trivial applet that displays a string,
	as passed via getParamter.
*/

import java.awt.*;
import java.applet.Applet;

public class AppApplet extends Applet
{
	public void init() {
		setSize(200, 200);
	}
	public void paint( Graphics g ) {
		String txt = getParameter("MYTEXT");
		g.drawString("Sample Applet ...",30,30);
		g.drawString( txt, 30, 50 );
	}

}
