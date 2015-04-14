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
 * A cylinder for CSG operations. 
 */
public class CSGCylinder extends CSGSolid {
	
	Vector3f direction;
	float radius;
	public Material material;
	
	/**
	 * Makes a CSG sphere.
	 * 
	 * @param direction of the cylinder-axis
	 * @param radius of the cylinder
	 */
	public CSGCylinder(Vector3f dir, float radius)
	{		
		this.direction = new Vector3f(dir);
		direction.normalize();
		this.radius = radius;
		material = new Diffuse(new Spectrum(1.f, 1.f, 1.f));
	}	
	
	public CSGCylinder(Material m){
		this.direction = new Vector3f(0.f,0.f,1.f);
		direction.normalize();
		this.radius = 1;
		material = m;
	}

	
	public ArrayList<IntervalBoundary> getIntervalBoundaries(Ray r)
	{
		ArrayList<IntervalBoundary> boundaries = new ArrayList<IntervalBoundary>();
		ArrayList<HitRecord> hits = intersectCylinder(r);
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
	private ArrayList<HitRecord> intersectCylinder(Ray r) {
		
		ArrayList<HitRecord> hits = new ArrayList<HitRecord>();

		
		Vector3f m=new Vector3f(r.direction);
		Vector3f tmp=new Vector3f(this.direction);
		tmp.scale(this.direction.dot(r.direction));
		m.sub(tmp);

		Vector3f n=new Vector3f(r.origin);
		tmp=new Vector3f(this.direction);
		tmp.scale(this.direction.dot(r.origin));
		n.sub(tmp);
		
		float a = m.dot(m);
		float b = 2*m.dot(n);
		float c = (float) (n.dot(n)-Math.pow(radius, 2));
		
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
		Vector3f tmp=new Vector3f(this.direction);
		tmp.scale(position.dot(this.direction));
		retNormal.sub(tmp);
		retNormal.normalize();
		
		// Incident direction, convention is that it points away from the hit position
		Vector3f wIn = new Vector3f(r.direction);
		wIn.negate();
		
		hits.add(new HitRecord(t, position, retNormal, wIn, null, material, 0.f, 0.f));
		
		return hits;
	}
}
