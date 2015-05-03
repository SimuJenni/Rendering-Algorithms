package rt.materials;

import javax.vecmath.Vector3f;

import rt.HitRecord;
import rt.Material;
import rt.Spectrum;

public class Blinn implements Material {
	
	private Spectrum kd, ks;
	private float s;

	public Blinn(Spectrum kd, Spectrum ks, float s) {
		this.kd = new Spectrum(kd);
		// Normalize
//		this.kd.mult(1/(float)Math.PI);
		this.ks=new Spectrum(ks);
		this.s=s;
	}

	@Override
	public Spectrum evaluateBRDF(HitRecord hitRecord, Vector3f wOut,
			Vector3f wIn) {
		// Diffuse term
		float ndotl = hitRecord.normal.dot(wIn);
		ndotl = Math.max(ndotl, 0.f);
		Spectrum diff=new Spectrum(kd);
		diff.mult(ndotl);
		
		// Specular term
		Vector3f h=new Vector3f(wIn);
		h.add(wOut);
		h.normalize();
		float hdotl=h.dot(hitRecord.normal);
		Spectrum spec=new Spectrum(ks);
		spec.mult((float) Math.pow(hdotl, s));
		spec.add(diff);
		
		return spec;
	}

	@Override
	public Spectrum evaluateEmission(HitRecord hitRecord, Vector3f wOut) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasSpecularReflection() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public ShadingSample evaluateSpecularReflection(HitRecord hitRecord) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasSpecularRefraction() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public ShadingSample evaluateSpecularRefraction(HitRecord hitRecord) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ShadingSample getShadingSample(HitRecord hitRecord, float[] sample) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ShadingSample getEmissionSample(HitRecord hitRecord, float[] sample) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean castsShadows() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public float getPobability(Vector3f sampleDir, Vector3f w, Vector3f normal) {
		// TODO Auto-generated method stub
		return 0;
	}


}
