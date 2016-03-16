package rt.integrators;

import javax.vecmath.Vector3f;

import rt.HitRecord;
import rt.Intersectable;
import rt.Ray;
import rt.StaticVecmath;

public class ShadowHandler {
	public HitRecord shadowHit;
	public float cosOut, cosIn, G, d2;
	private Ray shadowRay;
	private float d2expected, epsilon = (float) 1e-4;
	private HitRecord startHit;

	public ShadowHandler(HitRecord startingHit, Vector3f lightDir, float dist2) {
		this.startHit = startingHit;
		this.d2expected = dist2;
		lightDir.normalize();
		this.shadowRay = new Ray(startingHit.position, lightDir);
		this.cosOut = Math.abs(startingHit.normal.dot(lightDir));
	}

	public ShadowHandler(Ray ray, float dist2) {
		this.cosOut = 1;
		this.d2expected = dist2;
		this.shadowRay = ray;
	}

	public boolean checkShadowed(Intersectable root) {
		boolean shadowed = false;
		shadowHit = root.intersect(shadowRay);
		if (shadowHit != null) {
			d2 = StaticVecmath.dist2(shadowHit.position,
					shadowRay.origin);
			this.cosIn=Math.abs(shadowHit.getCosine());
			this.G = cosOut * cosIn / d2;
			if ((d2/d2expected <  1 - epsilon)
					&& shadowHit.material.castsShadows()
					|| shadowHit.t<1e-5) {
				shadowed = true;
			}
		}
		
		if (cosOut <= 1e-15 || cosIn <= 1e-15)
			return true;
		else
			return shadowed;
	}

}
