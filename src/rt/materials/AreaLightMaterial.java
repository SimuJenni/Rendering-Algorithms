package rt.materials;

import javax.vecmath.Vector3f;

import rt.HitRecord;
import rt.Material;
import rt.Spectrum;

public class AreaLightMaterial implements Material {

	private Spectrum emission;
	private float area;

	
	public AreaLightMaterial(Spectrum emission, float area) {
		this.emission = new Spectrum(emission);
		this.area=area;
		emission.mult((float) (1/(Math.PI*area)));
	}

	@Override
	public Spectrum evaluateBRDF(HitRecord hitRecord, Vector3f wOut,
			Vector3f wIn) {
		return new Spectrum();
	}

	public Spectrum evaluateEmission(HitRecord hitRecord, Vector3f wOut) {
		if(wOut==null||hitRecord.normal.dot(wOut)<0)
			return new Spectrum();
		else
			return new Spectrum(emission);
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
		return new ShadingSample();
	}

	@Override
	public boolean castsShadows() {
		// TODO Auto-generated method stub
		return true;
	}

}
