/*
	Application wrapper for applets
	April 97 M.Stricklin
	Feb 98 A. Gill

	Feel free to use this source code in any way.
*/

public class Main
{

	public static void main(String args[])
	{
		java.util.Hashtable params = new java.util.Hashtable();
		// If you wish, you can parse the args,
		// and insert them into the params Hashtable,
		// or simply hardwire them here.
		// The params is what the Applet's getParameter() will use.
		// This example here simulates the line
		//  <param name="MYTEXT" value="This applet is run as an application">
		// The name part should be upper cased, and
		// getParameter() normalizes the name to upppercase for you.	
		params.put("MYTEXT", "This applet is run as an application");

		// Create the applet.
		AppApplet applet = new AppApplet();

		// And fire up the applet frame.
		AppletFrame.startApplet(
						applet,
						"Java Hybrid Applet / Application",
						 params,
						 300, 200);
	}
}