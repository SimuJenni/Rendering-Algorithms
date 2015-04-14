package rt.cameras;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

import rt.Camera;
import rt.Ray;

public class PinholeCamera implements Camera{

	private Vector3f eye, lookAt, up;
	private float aspect, vertFOV, t, r, b, l;
	private int n, m;
	private Vector3f w, u, v;
	private Matrix4f mat;

	
	public PinholeCamera(Vector3f eye, Vector3f lookAt, Vector3f up,
			float vertFOV, float aspect, int width, int height) {
		super();
		this.eye = eye;
		this.lookAt = lookAt;
		this.up = up;
		this.vertFOV = (float) Math.toRadians(vertFOV);
		this.aspect = aspect;
		this.n = height;
		this.m = width;
		w=new Vector3f(eye);
		w.sub(lookAt);
		w.normalize();
		u=new Vector3f();
		u.cross(up, w);
		u.normalize();
		v=new Vector3f();
		v.cross(w, u);
		t=(float) Math.tan(this.vertFOV/2);
		b=-t;
		r=aspect*t;
		l=-r;
		mat=new Matrix4f();
		mat.setColumn(0, new Vector4f(u));
		mat.setColumn(1, new Vector4f(v));
		mat.setColumn(2, new Vector4f(w));
		mat.setColumn(3, new Vector4f(eye));
		mat.m33=1;
	}




	@Override
	public Ray makeWorldSpaceRay(int i, int j, float[] sample) {
//		float u=(float) l+(r-l)*(i+0.5f)/m;
//		float v=(float) b+(t-b)*(j+0.5f)/n;
		float u=(float) l+(r-l)*(i+sample[0])/m;
		float v=(float) b+(t-b)*(j+sample[1])/n;

		Vector4f s=new Vector4f(u,v,-1f,1f);
		
		// Transform it back to world coordinates
		mat.transform(s);
		
		// Make ray consisting of origin and direction in world coordinates
		Vector3f dir = new Vector3f();
		dir.sub(new Vector3f(s.x, s.y, s.z), eye);
		Ray r = new Ray(new Vector3f(eye), dir);
		return r;
	}

}
