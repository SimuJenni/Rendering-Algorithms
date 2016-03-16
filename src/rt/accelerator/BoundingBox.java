package rt.accelerator;

import javax.vecmath.Matrix4f;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

import rt.Ray;
import rt.accelerator.BSPAccelerator.SplitPlane;

public class BoundingBox {
	
	@Override
	public String toString() {
		return "[x1=" + x1 + ", y1=" + y1 + ", z1=" + z1 + ", x2="
				+ x2 + ", y2=" + y2 + ", z2=" + z2 + "]";
	}

	public float x1, y1, z1, x2, y2, z2;
	private Point3f[] bounds;
	
	public BoundingBox(float x1, float y1, float z1, float x2, float y2,
			float z2) {
		super();
		this.x1 = x1;
		this.y1 = y1;
		this.z1 = z1;
		this.x2 = x2;
		this.y2 = y2;
		this.z2 = z2;
		this.bounds = new Point3f[]{new Point3f(x1,y1,z1), new Point3f(x2,y2,z2)};

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
		Point3f p2=new Point3f(x1,y1,z2);
		Point3f p3=new Point3f(x1,y2,z1);
		Point3f p4=new Point3f(x1,y2,z2);
		Point3f p5=new Point3f(x2,y1,z1);
		Point3f p6=new Point3f(x2,y1,z2);
		Point3f p7=new Point3f(x2,y2,z1);
		Point3f p8=new Point3f(x2,y2,z2);
		m.transform(p1);
		m.transform(p2);
		m.transform(p3);
		m.transform(p4);
		m.transform(p5);
		m.transform(p6);
		m.transform(p7);
		m.transform(p8);
		float xmin = getMin(new float[]{p1.x,p2.x,p3.x,p4.x,p5.x,p6.x,p7.x,p8.x});
		float ymin = getMin(new float[]{p1.y,p2.y,p3.y,p4.y,p5.y,p6.y,p7.y,p8.y});
		float zmin = getMin(new float[]{p1.z,p2.z,p3.z,p4.z,p5.z,p6.z,p7.z,p8.z});
		float xmax = getMax(new float[]{p1.x,p2.x,p3.x,p4.x,p5.x,p6.x,p7.x,p8.x});
		float ymax = getMax(new float[]{p1.y,p2.y,p3.y,p4.y,p5.y,p6.y,p7.y,p8.y});
		float zmax = getMax(new float[]{p1.z,p2.z,p3.z,p4.z,p5.z,p6.z,p7.z,p8.z});

		return new BoundingBox(xmin,ymin,zmin,xmax,ymax,zmax);
	}

	private float getMin(float[] fs) {
		float min = Float.MAX_VALUE;
		for(int i=0; i<fs.length; i++){
			if(fs[i]<min)
				min = fs[i];
		}
		return min;
	}
	
	private float getMax(float[] fs) {
		float max = Float.MIN_VALUE;
		for(int i=0; i<fs.length; i++){
			if(fs[i]>max)
				max = fs[i];
		}
		return max;
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
