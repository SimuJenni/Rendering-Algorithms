package rt.intersectables;

import java.util.ArrayList;

import javax.vecmath.*;

import rt.HitRecord;
import rt.Intersectable;
import rt.Material;
import rt.Ray;
import rt.Spectrum;
import rt.accelerator.BoundingBox;
import rt.materials.Diffuse;

/**
 * A plane that can be intersected by a ray.
 */
public class Sphere implements Intersectable {

	Vector3f center;
	float radius;
	public Material material;

	/**
	 * Makes a CSG sphere.
	 * 
	 * @param center of the sphere
	 * @param radius of the sphere
	 */
	public Sphere(Vector3f center, float radius)
	{		
		this.center = new Vector3f(center);
		this.radius = radius;
		
		material = new Diffuse(new Spectrum(1.f, 1.f, 1.f));
	}	
	
	public Sphere()
	{		
		this.center = new Vector3f();
		this.radius = 1.f;
		
		material = new Diffuse(new Spectrum(1.f, 1.f, 1.f));
	}	

	
	/**
	 * Computes ray-sphere intersection. Note: we return all hit points,
	 * also the ones with negative t-value, that is, points that lie "behind"
	 * the origin of the ray. This is necessary for CSG operations to work
	 * correctly!  
	 * 
	 * @param r the ray
	 * @return the hit record of the intersection point, or null 
	 */
	public HitRecord intersect(Ray r) {
		
		ArrayList<HitRecord> hits = new ArrayList<HitRecord>();

		
		Vector3f d=new Vector3f(r.direction);
		Vector3f tmp=new Vector3f(r.origin);
		tmp.sub(center);
		
		float a = d.dot(d);
		float b = 2*tmp.dot(d);
		float c = (float) (tmp.dot(tmp)-Math.pow(radius, 2));
		Point2f solutions = CSGSolid.solveQuadratic(a, b, c);

		if (solutions == null)
			return null;
		else{
			hits=addHitpoint(r, hits, solutions.x);
			hits=addHitpoint(r, hits, solutions.y);
		}

		if(hits.get(0).t<hits.get(1).t&&hits.get(0).t>0)
			return hits.get(0);
		if(hits.get(0).t>hits.get(1).t&&hits.get(1).t>0)
			return hits.get(1);
		else
			return null;
	}

private ArrayList<HitRecord> addHitpoint(Ray r, ArrayList<HitRecord> hits, float t) {
	// Hit position
	Vector3f position = new Vector3f(r.direction);
	position.scaleAdd(t, r.origin);
	
	// Hit normal
	Vector3f retNormal = new Vector3f(position);
	retNormal.sub(center);
	retNormal.normalize();
	
	// Incident direction, convention is that it points away from the hit position
	Vector3f wIn = new Vector3f(r.direction);
	wIn.negate();
	
	hits.add(new HitRecord(t, position, retNormal, wIn, null, material, 0.f, 0.f));
	
	return hits;
}

@Override
public BoundingBox getBoundingBox() {
	return new BoundingBox(center.x-radius/2,center.y-radius/2,center.z-radius/2,
			center.x+radius/2,center.y+radius/2,center.z+radius/2);
}


}


