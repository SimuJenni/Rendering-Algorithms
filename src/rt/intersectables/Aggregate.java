package rt.intersectables;

import java.util.Iterator;

import rt.HitRecord;
import rt.Intersectable;
import rt.Ray;
import rt.accelerator.BoundingBox;

/**
 * A group of {@link Intersectable} objects.
 */
public abstract class Aggregate implements Intersectable {

	public HitRecord intersect(Ray r) {

		HitRecord hitRecord = null;
		float t = Float.MAX_VALUE;
		
		// Intersect all objects in group, return closest hit
		Iterator<Intersectable> it = iterator();
		while(it.hasNext())
		{
			Intersectable o = it.next();
			HitRecord tmp = o.intersect(r);
			if(tmp!=null && tmp.t<t)
			{
				t = tmp.t;
				hitRecord = tmp;
			}
		}
		return hitRecord;
	}
	
	public abstract Iterator<Intersectable> iterator();

	public abstract int numElements();
	
	public BoundingBox getBoundingBox(){
		float xmin=Float.POSITIVE_INFINITY,ymin=Float.POSITIVE_INFINITY,zmin=Float.POSITIVE_INFINITY,
				xmax=Float.NEGATIVE_INFINITY,ymax=Float.NEGATIVE_INFINITY,zmax=Float.NEGATIVE_INFINITY;
		Iterator<Intersectable> it = iterator();
		while(it.hasNext())
		{
			Intersectable o = it.next();
			BoundingBox b=o.getBoundingBox();
			xmin=Math.min(xmin, b.x1);
			ymin=Math.min(ymin, b.y1);
			zmin=Math.min(zmin, b.z1);
			xmax=Math.max(xmax, b.x2);
			ymax=Math.max(ymax, b.y2);
			zmax=Math.max(zmax, b.z2);			
		}
		return new BoundingBox(xmin,ymin,zmin,xmax,ymax,zmax);
	}

	public abstract IntersectableList getObjects();

}
