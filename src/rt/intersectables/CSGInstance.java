package rt.intersectables;

import java.util.ArrayList;
import java.util.Iterator;

import javax.vecmath.Matrix4f;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

import rt.HitRecord;
import rt.Intersectable;
import rt.Ray;
import rt.intersectables.CSGSolid.IntervalBoundary;

public class CSGInstance extends CSGSolid {
	
	private CSGSolid object;
	private Matrix4f M, M_inv, M_inv_transp; 
	
	public CSGInstance(CSGSolid object, Matrix4f M){
		this.object  = object;
		this.M = new Matrix4f(M);
		this.M_inv = new Matrix4f(M);
		M_inv.invert();
		this.M_inv_transp = new Matrix4f(M_inv);
		M_inv_transp.transpose();
	}


	@Override
	ArrayList<IntervalBoundary> getIntervalBoundaries(Ray r) {
		r=transformRay(r);
		ArrayList<IntervalBoundary> bounds=this.object.getIntervalBoundaries(r);
		Iterator<IntervalBoundary> iter = bounds.iterator();
		while(iter.hasNext()){
			IntervalBoundary b=iter.next();
			if(b.hitRecord!=null)
				b.hitRecord=transformHit(b.hitRecord);
		}
		return bounds;
	}
	
	private Ray transformRay(Ray r) {
		Vector3f newDir=new Vector3f(r.direction);
		M_inv.transform(newDir);
		Point3f newOri=new Point3f(r.origin);
		M_inv.transform(newOri);
		Ray newRay=new Ray(new Vector3f(newOri), newDir);
		return newRay;
	}
	
	private HitRecord transformHit(HitRecord hit) {
		Point3f newPos = new Point3f(hit.position);
		M.transform(newPos);
		hit.position=new Vector3f(newPos);
		
		Vector3f w=new Vector3f(hit.w);
		M.transform(w);
		w.normalize();
		hit.w=new Vector3f(w);
		
		Vector3f normal=new Vector3f(hit.normal);
		M_inv_transp.transform(normal);
		normal.normalize();
		hit.normal=new Vector3f(normal);

		return hit;
	}

}
