package rt.intersectables;

import java.util.ArrayList;
import javax.vecmath.Vector3f;
import rt.HitRecord;
import rt.Intersectable;
import rt.Ray;
import rt.accelerator.BoundingBox;

public class Cube implements Intersectable {

	ArrayList<Rectangle> planes = new ArrayList<Rectangle>();

	public Cube() {
		float size = 2;
		planes.add(new Rectangle(new Vector3f(1,1,1), new Vector3f(0,0,-size), new Vector3f(-size,0,0)));
		planes.add(new Rectangle(new Vector3f(1,1,1), new Vector3f(0,-size,0), new Vector3f(0,0,-size)));
		planes.add(new Rectangle(new Vector3f(1,1,1), new Vector3f(-size,0, 0), new Vector3f(0,-size,0)));
		
		planes.add(new Rectangle(new Vector3f(-1,-1,-1), new Vector3f(size,0,0), new Vector3f(0,0,size)));
		planes.add(new Rectangle(new Vector3f(-1,-1,-1), new Vector3f(0,0,size), new Vector3f(0,size,0)));
		planes.add(new Rectangle(new Vector3f(-1,-1,-1), new Vector3f(0,size,0), new Vector3f(size,0, 0)));
	}
	
	@Override
	public HitRecord intersect(Ray r) {
		HitRecord h = null;
		float t = Float.POSITIVE_INFINITY;
		for (Rectangle side: planes){
			HitRecord currentHit = side.intersect(r);
			if (currentHit != null && currentHit.t < t) {
				t = currentHit.t;
				h = currentHit;
			}
		}
		return h;
	}

	@Override
	public BoundingBox getBoundingBox() {
		return new BoundingBox(-1,-1,-1,1,1,1);
	}

}