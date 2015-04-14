package rt.intersectables;

import javax.vecmath.Point2f;
import javax.vecmath.Vector3f;

import rt.HitRecord;
import rt.Material;
import rt.Ray;
import rt.Spectrum;
import rt.intersectables.CSGSolid.IntervalBoundary;
import rt.materials.Diffuse;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * A cone for CSG operations. 
 */
public class CSGTwoSidedInfiniteCone extends CSGSolid {
	
	Vector3f direction;
	float angle;
	public Material material;
	
	/**
	 * Makes a CSG sphere.
	 * 
	 * @param center of the sphere
	 * @param radius of the sphere
	 */
	public CSGTwoSidedInfiniteCone(Vector3f dir, float angle)
	{		
		this.direction = new Vector3f(dir);
		direction.normalize();
		this.angle = (float) Math.toRadians(angle);
		material = new Diffuse(new Spectrum(1.f, 1.f, 1.f));
	}	
	
	public CSGTwoSidedInfiniteCone(Material m) {
		this.direction = new Vector3f(0.f,0.f,1.f);
		direction.normalize();
		this.angle = (float) Math.toRadians(45);
		material = m;	
	}

	public ArrayList<IntervalBoundary> getIntervalBoundaries(Ray r)
	{
		ArrayList<IntervalBoundary> boundaries = new ArrayList<IntervalBoundary>();
		ArrayList<HitRecord> hits = intersectCone(r);
		Iterator<HitRecord> iter = hits.iterator();
		HitRecord hitRecord;
		while(iter.hasNext()){
			hitRecord=iter.next();
			IntervalBoundary b=new IntervalBoundary();
			b.hitRecord = hitRecord;
			b.t = hitRecord.t;	
			
			// Determine if ray entered or left the cone.
			if(hitRecord.normal.dot(r.direction) > 0)
				b.type = BoundaryType.END;
			else
				b.type = BoundaryType.START;
			
			boundaries.add(b);
		}
		
		if(!hits.isEmpty()){
			if(hits.get(0).t>hits.get(1).t)
				hits.size();
			if ((hits.get(0).position.z < 0 && hits.get(1).position.z > 0) ||
					(hits.get(0).position.z > 0 && hits.get(1).position.z < 0)) 
			{
				IntervalBoundary b1=new IntervalBoundary();
				IntervalBoundary b2=new IntervalBoundary();
				if ((hits.get(0).position.z < 0&&hits.get(0).t<hits.get(1).t)||
						(hits.get(0).position.z > 0&&hits.get(0).t>hits.get(1).t)){
					b1.t=Float.NEGATIVE_INFINITY;
					b1.type=BoundaryType.START;
					b2.t=Float.POSITIVE_INFINITY;
					b2.type=BoundaryType.END;
				} 
				else {
					b1.t=Float.POSITIVE_INFINITY;
					b1.type=BoundaryType.START;
					b2.t=Float.NEGATIVE_INFINITY;
					b2.type=BoundaryType.END;
				}
				boundaries.add(b1);
				boundaries.add(b2);
			}
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
	private ArrayList<HitRecord> intersectCone(Ray r) {
		
		ArrayList<HitRecord> hits = new ArrayList<HitRecord>();
		
		Vector3f m=new Vector3f(r.direction);
		Vector3f tmp=new Vector3f(this.direction);
		tmp.scale(this.direction.dot(r.direction));
		m.sub(tmp);

		Vector3f n=new Vector3f(r.origin);
		tmp=new Vector3f(this.direction);
		tmp.scale(this.direction.dot(r.origin));
		n.sub(tmp);
		
		float a = (float) (m.dot(m) - Math.pow(Math.tan(angle)*r.direction.dot(this.direction), 2));
		float b = (float) (2*m.dot(n)-2*Math.pow(Math.tan(angle), 2)*r.origin.dot(direction)*r.direction.dot(direction));
		float c = (float) (n.dot(n)-Math.pow(Math.tan(angle)*r.origin.dot(this.direction), 2));
		
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
		Vector3f tmp = new Vector3f(position);
		float l=tmp.length();
		tmp.normalize();
		Vector3f dir=new Vector3f(this.direction);
		float projLength=tmp.dot(this.direction);
		dir.scale(l/projLength);
		Vector3f retNormal=new Vector3f(position);
		retNormal.sub(dir);
		retNormal.normalize();
		
		// Incident direction, convention is that it points away from the hit position
		Vector3f wIn = new Vector3f(r.direction);
		wIn.negate();
		
		hits.add(new HitRecord(t, position, retNormal, wIn, null, material, 0.f, 0.f));
		
		return hits;
	}
}
