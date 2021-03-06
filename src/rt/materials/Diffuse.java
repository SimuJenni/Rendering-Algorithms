package rt.materials;

import javax.vecmath.Vector3f;

import rt.*;

/**
 * A basic diffuse material.
 */
public class Diffuse implements Material {

	Spectrum kd;
	
	/**
	 * Note that the parameter value {@param kd} is the diffuse reflectance,
	 * which should be in the range [0,1], a value of 1 meaning all light
	 * is reflected (diffusely), and none is absorbed. The diffuse BRDF
	 * corresponding to {@param kd} is actually {@param kd}/pi.
	 * 
	 * @param kd the diffuse reflectance
	 */
	public Diffuse(Spectrum kd)
	{
		this.kd = new Spectrum(kd);
		// Normalize
		this.kd.mult(new Spectrum(1/(float)Math.PI));
	}
	
	/**
	 * Default diffuse material with reflectance (1,1,1).
	 */
	public Diffuse()
	{
		this(new Spectrum(1.f, 1.f, 1.f));
	}

	/**
	 * Returns diffuse BRDF value, that is, a constant.
	 * 
	 *  @param wOut outgoing direction, by convention towards camera
	 *  @param wIn incident direction, by convention towards light
	 *  @param hitRecord hit record to be used
	 */
	public Spectrum evaluateBRDF(HitRecord hitRecord, Vector3f wOut, Vector3f wIn) {
		return new Spectrum(kd);
	}

	public boolean hasSpecularReflection()
	{
		return false;
	}
	
	public ShadingSample evaluateSpecularReflection(HitRecord hitRecord)
	{
		return null;
	}
	public boolean hasSpecularRefraction()
	{
		return false;
	}

	public ShadingSample evaluateSpecularRefraction(HitRecord hitRecord)
	{
		return null;
	}
	
	// To be implemented for path tracer!
	public ShadingSample getShadingSample(HitRecord hitRecord, float[] sample)
	{
		float psi1=sample[0];
		float psi2=sample[1];
		psi1=(float) Math.sqrt(psi1);
		psi2=(float) (Math.PI*2*psi2);
		
		Vector3f dir=new Vector3f((float)Math.cos(psi2)*psi1,
				(float)Math.sin(psi2)*psi1,(float)Math.sqrt(1-sample[0]));
		dir=hitRecord.transformToTangentSpace(dir);
		dir.normalize();
		float p=(float) Math.abs((dir.dot(hitRecord.normal)/Math.PI));
		Spectrum brdf=evaluateBRDF(hitRecord,hitRecord.getNormalizedDirection(),dir);
	
		return new ShadingSample(brdf,new Spectrum(0.f, 0.f, 0.f),dir,hasSpecularReflection(),p);	
	}
		
	public boolean castsShadows()
	{	
		return true;
	}
	
	public Spectrum evaluateEmission(HitRecord hitRecord, Vector3f wOut) {
		return null;
	}

	public ShadingSample getEmissionSample(HitRecord hitRecord, float[] sample) {
		return null;
	}

	@Override
	public float getPobability(Vector3f inDir, Vector3f outDir, Vector3f normal) {
		// TODO Auto-generated method stub
		return (float) Math.abs((outDir.dot(normal)/Math.PI));
	}

}
