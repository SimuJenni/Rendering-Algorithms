package rt.lightsources;

import javax.vecmath.Vector3f;

import rt.HitRecord;
import rt.LightGeometry;
import rt.Material;
import rt.Ray;
import rt.Spectrum;
import rt.accelerator.BoundingBox;
import rt.intersectables.Rectangle;
import rt.materials.AreaLightMaterial;
import rt.materials.PointLightMaterial;

public class RectangleLight implements LightGeometry {
	private Rectangle rectangle;
	private Vector3f bottomLeft, right, top;
	private AreaLightMaterial areaLightMaterial;

	public RectangleLight(Vector3f bottomLeft, Vector3f right, Vector3f top,
			Spectrum emission) {
		this.bottomLeft=bottomLeft;
		this.right=right;
		this.top=top;
		this.rectangle=new Rectangle(bottomLeft,right,top);
		areaLightMaterial = new AreaLightMaterial(emission, rectangle.getArea());
	}

	@Override
	public HitRecord intersect(Ray r) {
		HitRecord lightHit=rectangle.intersect(r);
		if(lightHit==null)
			return null;
		lightHit.intersectable=this;
		lightHit.p=1.f/this.rectangle.getArea();
		lightHit.material=this.areaLightMaterial;
		return lightHit;
	}

	@Override
	public BoundingBox getBoundingBox() {
		// TODO Auto-generated method stub
		return rectangle.getBoundingBox();
	}

	@Override
	public HitRecord sample(float[] s) {
		Vector3f sample=new Vector3f(bottomLeft);
		Vector3f right=new Vector3f(this.right);
		Vector3f top=new Vector3f(this.top);
		right.scale(s[0]);
		top.scale(s[1]);
		sample.add(top);
		sample.add(right);
		HitRecord hitRecord = new HitRecord();
		hitRecord.position = new Vector3f(sample);
		hitRecord.material = areaLightMaterial;
		hitRecord.normal = rectangle.getNormal();
		hitRecord.normal.normalize();
		hitRecord.p = 1.f/rectangle.getArea();
		return hitRecord;
	}

}
