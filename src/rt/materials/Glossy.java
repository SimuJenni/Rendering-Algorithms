package rt.materials;

import javax.vecmath.Vector3f;

import rt.HitRecord;
import rt.Material;
import rt.Spectrum;
import rt.StaticVecmath;

public class Glossy implements Material {

	private float e;
	private Spectrum n;
	private Spectrum k;
	private Spectrum addedSpecs;

	public Glossy(float shininess, Spectrum n, Spectrum k){
		this.e = shininess;
		this.n = n;
		this.k = k;
		Spectrum n2 = new Spectrum(n); n2.sqrt(); 
		Spectrum k2 = new Spectrum(k); k2.sqrt(); 
		this.addedSpecs = new Spectrum(n2); addedSpecs.add(k2); 
	}
	
	@Override
	public Spectrum evaluateBRDF(HitRecord hitRecord, Vector3f wOut, Vector3f wIn) {

		Vector3f w_h = new Vector3f();
		w_h.add(wIn,wOut);
		w_h.normalize();
		
		Spectrum F = computeFresnel(wIn, hitRecord.normal);
		float G = computeGeometryTerm(wIn, wOut, w_h, hitRecord.normal);
		float D = computeMicrofacetDistribution(e, hitRecord.normal, w_h);
		
		F.mult(G);
		F.mult(D);
		F.divide(4 * wIn.dot(hitRecord.normal) * wOut.dot(hitRecord.normal));
		return F;
	}

	private float computeMicrofacetDistribution(float shininess,Vector3f normal, Vector3f w_h) {
		return (shininess + 2)/((float)(2*Math.PI)) * (float)Math.pow(normal.dot(w_h),shininess);
	}

	private float computeGeometryTerm(Vector3f wIn, Vector3f wOut, Vector3f w_h, Vector3f normal) {
		
		float nWh = normal.dot(w_h);
		float nWi = normal.dot(wIn);
		float nWo = normal.dot(wOut);
		float w_oDotw_h = wOut.dot(w_h);
		
		float result = Math.min((2 * nWh * nWo)/w_oDotw_h,(2 * nWh * nWi)/w_oDotw_h);
		
		result = Math.min(1,result);
		return result;
	}

	private Spectrum computeFresnel(Vector3f wIn, Vector3f normal){
		float cosThetaI = wIn.dot(normal); // cos(theta_i)
		Spectrum twoNCosThetaI = new Spectrum(n); twoNCosThetaI.mult(2 * cosThetaI); // 2n*cos(theta_i) 
		
		Spectrum r1 = new Spectrum(addedSpecs); 
		r1.mult(cosThetaI*cosThetaI); 
		r1.sub(twoNCosThetaI);
		r1.add(1);
		
		Spectrum r1_denom = new Spectrum(addedSpecs);
		r1_denom.mult(cosThetaI*cosThetaI);
		r1_denom.add(twoNCosThetaI);
		r1_denom.add(1);

		r1.divide(r1_denom);
		
		Spectrum r2 = new Spectrum(addedSpecs);
		r2.sub(twoNCosThetaI);
		r2.add(cosThetaI*cosThetaI);
		
		Spectrum r2_denom = new Spectrum(addedSpecs);
		r2_denom.add(twoNCosThetaI);
		r2_denom.add(cosThetaI*cosThetaI);
		
		r2.divide(r2_denom);
		
		Spectrum avg = new Spectrum(r1);
		avg.add(r2);
		avg.mult(0.5f);

		return avg;
	}
	@Override
	public Spectrum evaluateEmission(HitRecord hitRecord, Vector3f wOut) {
		return new Spectrum(0,0,0);
	}

	@Override
	public boolean hasSpecularReflection() {
		return false;
	}

	@Override
	public ShadingSample evaluateSpecularReflection(HitRecord hitRecord) {
		return null;
	}

	@Override
	public boolean hasSpecularRefraction() {
		return false;
	}

	@Override
	public ShadingSample evaluateSpecularRefraction(HitRecord hitRecord) {
		return null;
	}

	@Override
	public ShadingSample getShadingSample(HitRecord hitRecord, float[] sample) {
		float psi1 = sample[0], psi2 = sample[1];

		float cosTheta = (float) Math.pow(psi1, 1f/(e+1));
		float sinTheta = (float) Math.sqrt(1-cosTheta*cosTheta);
		float phi = (float) (2 * Math.PI * psi2);

		Vector3f w_h = new Vector3f();
		w_h.x = (float) (sinTheta * Math.cos(phi));
		w_h.y = (float) (sinTheta * Math.sin(phi));
		w_h.z = cosTheta;
		
		w_h = hitRecord.transformToTangentSpace(w_h);
		
		Vector3f w_o = new Vector3f(hitRecord.w);
		w_o.normalize();
		
		Vector3f w_i = StaticVecmath.reflect(w_h, w_o);
		assert(Math.abs(w_i.length() - 1) < 1e-5f);
		
		float p = (float) (((e+1)/(8*w_o.dot(w_h)*Math.PI)) * Math.pow(cosTheta, e));
		
		ShadingSample shadingSample = new ShadingSample(evaluateBRDF(hitRecord,w_o,w_i), new Spectrum(0,0,0),w_i,false,p);
		return shadingSample;
	}

	@Override
	public ShadingSample getEmissionSample(HitRecord hitRecord, float[] sample) {
		return null;
	}

	@Override
	public boolean castsShadows() {
		return true;
	}

	@Override
	public float getPobability(Vector3f inDir, Vector3f outDir, Vector3f normal) {
		// TODO Auto-generated method stub
		return 0;
	}

}