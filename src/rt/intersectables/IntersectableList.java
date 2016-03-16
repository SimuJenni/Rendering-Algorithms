package rt.intersectables;

import java.util.LinkedList;
import java.util.Iterator;
import rt.*;

public class IntersectableList extends Aggregate {

	@Override
	public String toString() {
		return "[size=" + list.size() + "]";
	}

	public LinkedList<Intersectable> list;
	
	public IntersectableList()
	{
		list = new LinkedList<Intersectable>();
	}
	
	public void add(Intersectable i)
	{
		list.add(i);
	}
	
	public Iterator<Intersectable> iterator() {
		return list.iterator();
	}

	@Override
	public int numElements() {
		return list.size();
	}

	@Override
	public IntersectableList getObjects() {
		return this;
	}

}
