package rt.materials;

import javax.vecmath.Vector3f;

import rt.HitRecord;
import rt.Material;
import rt.Spectrum;
import rt.Material.ShadingSample;

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
		if(hitRecord.normal.dot(wOut)<0)
			return new Spectrum(0);
		Spectrum s = new Spectrum(emission);
		s.mult((float) (1/(Math.PI*area)));
		return s;
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
		float psi1=sample[0];
		float psi2=sample[1];
		psi1=(float) Math.sqrt(psi1);
		psi2=(float) (Math.PI*2*psi2);
		
		Vector3f dir=new Vector3f((float)Math.cos(psi2)*psi1,
				(float)Math.sin(psi2)*psi1,(float)Math.sqrt(1-sample[0]));
		dir=hitRecord.transformToTangentSpace(dir);
		dir.normalize();
		float p=(float) (dir.dot(hitRecord.normal)/Math.PI);
		Spectrum brdf=new Spectrum();
	
		return new ShadingSample(brdf,new Spectrum(0.f, 0.f, 0.f),dir,hasSpecularReflection(),p);
	}

	@Override
	public ShadingSample getEmissionSample(HitRecord hitRecord, float[] sample) {
		float psi1=sample[0];
		float psi2=sample[1];
		psi1=(float) Math.sqrt(psi1);
		psi2=(float) (Math.PI*2*psi2);
		
		Vector3f dir=new Vector3f((float)Math.cos(psi2)*psi1,
				(float)Math.sin(psi2)*psi1,(float)Math.sqrt(1-sample[0]));
		dir=hitRecord.transformToTangentSpace(dir);
		dir.normalize();
		float p=(float) (dir.dot(hitRecord.normal)/Math.PI);
		Spectrum brdf=new Spectrum();
	
		return new ShadingSample(brdf,evaluateEmission(hitRecord,dir),dir,hasSpecularReflection(),p);
	}

	@Override
	public boolean castsShadows() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public float getPobability(Vector3f inDir, Vector3f outDir, Vector3f normal) {
		// TODO Auto-generated method stub
		return (float) Math.abs((outDir.dot(normal)/Math.PI));
	}
}
