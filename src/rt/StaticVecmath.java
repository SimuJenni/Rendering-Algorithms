package rt;

import javax.vecmath.*;

public class StaticVecmath {

	public static float dist2(Tuple3f v1, Tuple3f v2)
	{
		Vector3f tmp = new Vector3f(v1);
		tmp.sub(v2);
		return tmp.lengthSquared();
	}
	
	public static Vector3f sub(Tuple3f v1, Tuple3f v2)
	{
		Vector3f r = new Vector3f(v1);
		r.sub(v2);
		return r;
	}
	
	public static Vector3f negate(Vector3f v)
	{
		Vector3f r = new Vector3f(v);
		r.negate();
		return r;
	}
	
	public static Matrix4f invert(Matrix4f m)
	{
		Matrix4f r = new Matrix4f(m);
		r.invert();
		return r;
	}

	public static Vector3f reflect(Vector3f w, Vector3f normal) {
		normal.normalize();
		Vector3f d=new Vector3f(w);
		d.normalize();
		d.negate();
		Vector3f tmp=new Vector3f(normal);
		tmp.scale(2*d.dot(normal));
		d.sub(tmp);
		d.normalize();
		return d;
	}

	public static Vector3f refract(Vector3f w, Vector3f hitNormal, float n) {
		Vector3f normal=new Vector3f(hitNormal);
		Vector3f v=new Vector3f(w);
		normal.normalize();
		v.normalize();
		float n1,n2;
		if(normal.dot(v)>0){
			n1=1;
			n2=n;
		}
		else{
			n1=n;
			n2=1;
			normal.negate();
		}
		float cosTheta_i=normal.dot(v);
		v.negate();
		float sin2theta_t=(n1*n1)/(n2*n2)*(1-cosTheta_i*cosTheta_i);
		Vector3f t=new Vector3f(v);
		t.scale(n1/n2);
		normal.scale((float) (n1/n2*cosTheta_i-Math.sqrt(1-sin2theta_t)));
		t.add(normal);
		return t;
	}

	public static float computeSchlick(Vector3f w, Vector3f hitNormal, float n) {
		Vector3f normal=new Vector3f(hitNormal);
		Vector3f v=new Vector3f(w);
		normal.normalize();
		v.normalize();
		float n1,n2;
		if(normal.dot(v)>0){
			n1=1;
			n2=n;
		}
		else{
			n1=n;
			n2=1;
			normal.negate();
		}	
		float cosTheta_i=normal.dot(v);
		v.negate();
		float sin2theta_t=(n1*n1)/(n2*n2)*(1-cosTheta_i*cosTheta_i);
		float cosTheta_t=(float) Math.sqrt(1-sin2theta_t);
		
		float r0=(n1-n2)/(n1+n2);
		r0=r0*r0;

		if(sin2theta_t>1)
			return 1;
		if(n1<=n2)
			return (float) (r0+(1-r0)*Math.pow(1-cosTheta_i, 5));
		else
			return (float) (r0+(1-r0)*Math.pow(1-cosTheta_t, 5));
	}
}
