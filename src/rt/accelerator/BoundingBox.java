package rt.accelerator;

import javax.vecmath.Matrix4f;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

import rt.Ray;
import rt.accelerator.BSPAccelerator.SplitPlane;

public class BoundingBox {
	
	public float x1, y1, z1, x2, y2, z2;
	
	public BoundingBox(float x1, float y1, float z1, float x2, float y2,
			float z2) {
		super();
		this.x1 = x1;
		this.y1 = y1;
		this.z1 = z1;
		this.x2 = x2;
		this.y2 = y2;
		this.z2 = z2;
	}

	public float[] getDimensions() {
		float[] dim=new float[3];
		dim[0]=x2-x1;
		dim[1]=y2-y1;
		dim[2]=z2-z1;
		return dim;
	}

	public BoundingBox boxAbove(SplitPlane s) {
		switch(s.axis){
			case X: 
				return new BoundingBox(s.distance,y1,z1,x2,y2,z2);
			case Y: 
				return new BoundingBox(x1,s.distance,z1,x2,y2,z2);
			case Z: 
				return new BoundingBox(x1,y1,s.distance,x2,y2,z2);
		}
		return null;
	}

	public BoundingBox boxBelow(SplitPlane s) {
		switch(s.axis){
		case X: 
			return new BoundingBox(x1,y1,z1,s.distance,y2,z2);
		case Y: 
			return new BoundingBox(x1,y1,z1,x2,s.distance,z2);
		case Z: 
			return new BoundingBox(x1,y1,z1,x2,y2,s.distance);
		}
		return null;
	}

	public BoundingBox getTransformedBox(Matrix4f m) {
		Point3f p1=new Point3f(x1,y1,z1);
		Point3f p2=new Point3f(x2,y2,z2);
		m.transform(p1);
		m.transform(p2);
		return new BoundingBox(p1.x,p1.y,p1.z,p2.x,p2.y,p2.z);
	}

	public BSPStackItem intersectBox(Ray r, BSPNode n) {
		Vector3f d = r.direction;
		Vector3f e = r.origin;
		float txmin,txmax,tymin,tymax,tzmin,tzmax;
		if(d.x>=0){
			txmin=(x1-e.x)/d.x;
			txmax=(x2-e.x)/d.x;
		} else{
			txmin=(x2-e.x)/d.x;
			txmax=(x1-e.x)/d.x;
		}
		if(d.y>=0){
			tymin=(y1-e.y)/d.y;
			tymax=(y2-e.y)/d.y;
		} else{
			tymin=(y2-e.y)/d.y;
			tymax=(y1-e.y)/d.y;
		}
		
		// Intersection testing
		if ((txmin > tymax) || tymin > txmax){
			return null;
		}
		
		float tmin = (float)Math.max(txmin,tymin);
		float tmax = (float)Math.min(txmax, tymax);
		
		if(d.z>=0){
			tzmin=(z1-e.z)/d.z;
			tzmax=(z2-e.z)/d.z;
		} else{
			tzmin=(z2-e.z)/d.z;
			tzmax=(z1-e.z)/d.z;
		}
		
		// Intersection testing
		if (( tmin > tzmax) || (tzmin > tmax)){
			return null;
		}
		
		return new BSPStackItem(n, Math.max(tmin, tzmin), Math.min(tmax, tzmax));
	}

}
