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
	float p = 0.5f;
	TextureMaterial bumper = null;

	public Refractive(float n) {
		this.n=n;
		this.ks=new Spectrum(1,1,1);
	}

	public Refractive(float f, String string) {
		this(f);
		this.bumper=new TextureMaterial(null, string);
	}

	public Refractive(float f, Spectrum spectrum) {
		this(f);
		this.ks=spectrum;
	}

	@Override
	public Spectrum evaluateBRDF(HitRecord hitRecord, Vector3f wOut,
			Vector3f wIn) {
		return ks;
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
		Vector3f reflected=StaticVecmath.reflect(hitRecord.w,hitRecord.normal);
		Spectrum reflSpectrum=new Spectrum(ks);
		reflSpectrum.mult(F);
		float cos = Math.abs(hitRecord.normal.dot(reflected));
		reflSpectrum.mult(cos);
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
		Vector3f refracted=StaticVecmath.refract(hitRecord.w,hitRecord.normal,n);
		Spectrum reffSpectrum=new Spectrum(ks);
		reffSpectrum.mult(1-F);
		float cos = Math.abs(hitRecord.normal.dot(refracted));
		reffSpectrum.mult(cos);
		ShadingSample s=new ShadingSample(reffSpectrum, new Spectrum(0,0,0), refracted, true, 1-F);
		return s;
	}

	@Override
	public ShadingSample getShadingSample(HitRecord hitRecord, float[] sample) {
		if(this.bumper!=null)
			bumper.evaluateBumpMap(hitRecord);
		float F=StaticVecmath.computeSchlick(hitRecord.w,hitRecord.normal,n);
		if(F>sample[0])
			return evaluateSpecularReflection(hitRecord);
		else
			return evaluateSpecularRefraction(hitRecord);
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
	public float getPobability(Vector3f d, Vector3f w, Vector3f normal) {
		// TODO Auto-generated method stub
		float k = StaticVecmath.computeSchlick(w,normal,n);
		float w_n = w.dot(normal);
		float d_n = d.dot(normal);
		if(w_n>=0&&d_n>=0||w_n<=0&&d_n<=0)
			return k;
		else
			return 1-k;
	}

}
