package rt.intersectables;

import javax.vecmath.Point2f;
import javax.vecmath.Vector3f;

import rt.HitRecord;
import rt.Material;
import rt.Ray;
import rt.Spectrum;
import rt.intersectables.CSGSolid.BoundaryType;
import rt.intersectables.CSGSolid.IntervalBoundary;
import rt.materials.Diffuse;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * A sphere for CSG operations. 
 */
public class CSGSphere extends CSGSolid {
	
	Vector3f center;
	float radius;
	public Material material;
	
	/**
	 * Makes a CSG sphere.
	 * 
	 * @param center of the sphere
	 * @param radius of the sphere
	 */
	public CSGSphere(Vector3f center, float radius)
	{		
		this.center = new Vector3f(center);
		this.radius = radius;
		material = new Diffuse(new Spectrum(1.f, 1.f, 1.f));
	}		
	
	public ArrayList<IntervalBoundary> getIntervalBoundaries(Ray r)
	{
		ArrayList<IntervalBoundary> boundaries = new ArrayList<IntervalBoundary>();
		ArrayList<HitRecord> hits = intersectSphere(r);
		Iterator<HitRecord> iter = hits.iterator();
		HitRecord hitRecord;
		while(iter.hasNext()){
			hitRecord=iter.next();
			IntervalBoundary b=new IntervalBoundary();
			b.hitRecord = hitRecord;
			b.t = hitRecord.t;	
			
			// Determine if ray entered or left the sphere.
			if(hitRecord.normal.dot(r.direction) > 0)
				b.type = BoundaryType.END;
			else
				b.type = BoundaryType.START;
			
			boundaries.add(b);
		}
		return boundaries;
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
	private ArrayList<HitRecord> intersectSphere(Ray r) {
		
		ArrayList<HitRecord> hits = new ArrayList<HitRecord>();

		
		Vector3f d=new Vector3f(r.direction);
		Vector3f tmp=new Vector3f(r.origin);
		tmp.sub(center);
		
		float a = d.dot(d);
		float b = 2*tmp.dot(d);
		float c = (float) (tmp.dot(tmp)-Math.pow(radius, 2));
		
		Point2f solutions = solveQuadratic(a, b, c);
		if (solutions == null)
			return hits;
		else{
			hits=addHitpoint(r, hits, solutions.x);
			hits=addHitpoint(r, hits, solutions.y);
		}
		
		return hits;
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
}
