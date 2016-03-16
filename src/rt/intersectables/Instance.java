package rt.intersectables;

import javax.vecmath.Matrix4f;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

import rt.HitRecord;
import rt.Intersectable;
import rt.Material;
import rt.Ray;
import rt.accelerator.BoundingBox;

public class Instance implements Intersectable {
	private Intersectable object;
	private Matrix4f M, M_inv, M_inv_transp;
	public Material material;
	private BoundingBox box;
	
	public Instance(Intersectable object, Matrix4f m) {
		super();
		this.object = object;
		this.M = new Matrix4f(m);
		this.M_inv=new Matrix4f(m);
		this.M_inv.invert();
		this.M_inv_transp=new Matrix4f(M_inv);
		this.M_inv_transp.transpose();
		this.box=object.getBoundingBox().getTransformedBox(M);
	}

	@Override
	public HitRecord intersect(Ray r) {
		Ray newRay=transformRay(r);
		HitRecord hit=this.object.intersect(newRay);
		if(hit!=null){
			hit.material=this.material;
			return transformHit(hit);
		}
		else
			return null;
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
		
		Vector3f t1=new Vector3f(hit.t1);
		M.transform(t1);
		t1.normalize();
		hit.t1=new Vector3f(t1);
		
		Vector3f t2=new Vector3f(hit.t2);
		M.transform(t2);
		t2.normalize();
		hit.t2=new Vector3f(t2);
		
		Vector3f normal=new Vector3f(hit.normal);

		M_inv_transp.transform(normal);
		normal.normalize();
		hit.normal=new Vector3f(normal);

		return hit;
	}

	@Override
	public BoundingBox getBoundingBox() {
		return box;
	}

}
