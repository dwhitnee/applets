import java.util.*;

//----------------------------------------------------------------------
//	setChanged is protected, this allows Observables to be contained, too.
//----------------------------------------------------------------------
public class PublicObservable extends Observable {
	public void setChanged()	{ super.setChanged(); }
}

