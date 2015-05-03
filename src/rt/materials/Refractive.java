package rt.materials;

import javax.vecmath.Vector3f;

import rt.HitRecord;
import rt.Material;
import rt.Spectrum;
import rt.StaticVecmath;
import rt.Material.ShadingSample;

public class Refractive implements Material {
	
	private float n;
	public Spectrum ks;

	public Refractive(float n) {
		this.n=n;
		this.ks=new Spectrum(1,1,1);
	}

	@Override
	public Spectrum evaluateBRDF(HitRecord hitRecord, Vector3f wOut,
			Vector3f wIn) {
		return new Spectrum();
	}

	@Override
	public Spectrum evaluateEmission(HitRecord hitRecord, Vector3f wOut) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasSpecularReflection() {
		return true;
	}

	@Override
	public ShadingSample evaluateSpecularReflection(HitRecord hitRecord) {
		float F=StaticVecmath.computeSchlick(hitRecord.w,hitRecord.normal,n);
//		if(F<0.0001)
//			return null;
		Vector3f reflected=StaticVecmath.reflect(hitRecord.w,hitRecord.normal);
		Spectrum reflSpectrum=new Spectrum(ks);
		reflSpectrum.mult(F);
		ShadingSample s=new ShadingSample(reflSpectrum, new Spectrum(0,0,0), reflected, true, F);
		return s;		
	}

	@Override
	public boolean hasSpecularRefraction() {
		return true;
	}

	@Override
	public ShadingSample evaluateSpecularRefraction(HitRecord hitRecord) {
		float F=StaticVecmath.computeSchlick(hitRecord.w,hitRecord.normal,n);
		if(F==1)
			return null;
		Vector3f refracted=StaticVecmath.refract(hitRecord.w,hitRecord.normal,n);
		Spectrum reffSpectrum=new Spectrum(ks);
		reffSpectrum.mult(1-F);
		ShadingSample s=new ShadingSample(reffSpectrum, new Spectrum(0,0,0), refracted, true, 1-F);
		return s;
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
		return true;
	}

	@Override
	public float getPobability(Vector3f sampleDir, Vector3f w, Vector3f normal) {
		// TODO Auto-generated method stub
		return 0;
	}

}
