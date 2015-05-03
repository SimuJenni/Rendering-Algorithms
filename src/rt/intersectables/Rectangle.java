package rt.intersectables;

import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

import rt.HitRecord;
import rt.Intersectable;
import rt.Material;
import rt.Ray;
import rt.Spectrum;
import rt.StaticVecmath;
import rt.accelerator.BoundingBox;
import rt.materials.Diffuse;

public class Rectangle implements Intersectable {
	private Vector3f bottomLeft, right, top, normal;
	private Plane plane;
	float d, area, width, height;
	public Material material;

	public Rectangle(Vector3f bottomLeft, Vector3f right, Vector3f top) {
		normal=new Vector3f();
		normal.cross(right, top);
		area=normal.length();
		normal.normalize();
		d=-bottomLeft.dot(normal);
		plane=new Plane(normal, d);
		this.bottomLeft=bottomLeft;
		this.right=right;
		this.top=top;
		width=right.length();
		height=top.length();
		material=new Diffuse();
	}

	public Rectangle(Point3f point1, Point3f point2, Point3f point3) {
		this(new Vector3f(point1), StaticVecmath.sub(point2, point1), StaticVecmath.sub(point3, point1));
	}

	@Override
	public HitRecord intersect(Ray r) {
		HitRecord hit=this.plane.intersect(r);
		if(hit==null)
			return null;
		hit.material=this.material;
		if(isOnRectangle(hit.position))
			return hit;
		else
			return null;
	}

	private boolean isOnRectangle(Vector3f position) {
		Vector3f sample=new Vector3f(position);
		sample.sub(this.bottomLeft);
		Vector3f widthVec=new Vector3f(right);
		Vector3f heightVec=new Vector3f(top);
		float width=this.right.length();
		float height=this.top.length();
		widthVec.normalize();
		heightVec.normalize();
		float projectedWidth=sample.dot(widthVec);
		float projectedHeight=sample.dot(heightVec);
		if(projectedWidth<0||projectedHeight<0||projectedWidth>width||projectedHeight>height)
			return false;
		else
			return true;
	}

	@Override
	public BoundingBox getBoundingBox() {
		Vector3f topRight=new Vector3f(this.bottomLeft);
		Vector3f topLeft=new Vector3f(this.bottomLeft);
		Vector3f bottomRight=new Vector3f(this.bottomLeft);
		topRight.add(right);
		topRight.add(top);
		topLeft.add(top);
		bottomRight.add(right);
		float xmin=Math.min(bottomLeft.x, topLeft.x);
		float zmin=Math.min(bottomLeft.z, bottomRight.z);
		float ymin=Math.min(bottomLeft.y, topLeft.y);
		ymin=Math.min(ymin, bottomRight.y);
		ymin=Math.min(ymin, topRight.y);
		float xmax=Math.max(bottomRight.x, topRight.x);
		float zmax=Math.max(topLeft.z, topRight.z);
		float ymax=Math.max(bottomLeft.y, topLeft.y);
		ymax=Math.max(ymax, bottomRight.y);
		ymax=Math.max(ymax, topRight.y);
		return new BoundingBox(xmin,ymin,zmin,xmax,ymax,zmax);
	}

	public float getArea() {
		return area;
	}

	public Vector3f getNormal() {
		// TODO Auto-generated method stub
		return normal;
	}


}
