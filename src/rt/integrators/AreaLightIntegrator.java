package rt.integrators;

import java.util.Iterator;

import javax.vecmath.Vector3f;

import rt.HitRecord;
import rt.Integrator;
import rt.Intersectable;
import rt.LightGeometry;
import rt.LightList;
import rt.Ray;
import rt.Sampler;
import rt.Scene;
import rt.Spectrum;
import rt.StaticVecmath;
import rt.Material.ShadingSample;

public class AreaLightIntegrator implements Integrator {

	LightList lightList;
	Intersectable root;
	
	public AreaLightIntegrator(Scene scene)
	{
		this.lightList = scene.getLightList();
		this.root = scene.getIntersectable();
	}

	/**
	 * Basic integrator that simply iterates over the light sources and accumulates
	 * their contributions. No shadow testing, reflection, refraction, or 
	 * area light sources, etc. supported.
	 */
	public Spectrum integrate(Ray r) {

		HitRecord hitRecord = root.intersect(r);
		// immediately return background color if nothing was hit
		if(hitRecord == null) { 
			return new Spectrum(0,0,0);
		}
		if(hitRecord.material.getEmissionSample(hitRecord, getRandomSample())!=null)
			return hitRecord.material.evaluateEmission(hitRecord, hitRecord.w);
		
		Spectrum outgoing=new Spectrum();
		outgoing.add(sampledLightSpectrum(hitRecord));
		outgoing.add(sampledEnvironmentSpectrum(hitRecord));

		return outgoing;	
	}

	private Spectrum sampledEnvironmentSpectrum(HitRecord hitRecord) {
		// TODO Auto-generated method stub
		ShadingSample sample=hitRecord.material.getShadingSample(hitRecord, getRandomSample());
		Ray sampleRay=new Ray(hitRecord.position,sample.w);
		HitRecord sampleHit=root.intersect(sampleRay);
		if(sampleHit!=null){
			//emission
			Spectrum outgoing=sampleHit.material.evaluateEmission(sampleHit, sampleHit.w);
			outgoing.mult(sample.brdf);
			outgoing.mult(1/sample.p);
			// Multiply with cosine of surface normal and incident direction
			float ndotl = hitRecord.normal.dot(sample.w);
			ndotl = Math.max(ndotl, 0.f);
			outgoing.mult(ndotl);
			outgoing.mult(0.2f);
			return outgoing;
		}
		else
			return new Spectrum();
	}

	private Spectrum sampledLightSpectrum(HitRecord hitRecord) {
		
		LightGeometry lightSource = lightList.getRandomLight();
			
		// Make direction from hit point to light source position; this is only supposed to work with point lights
		float randomSample[] = getRandomSample();
		HitRecord lightHit = lightSource.sample(randomSample);
		lightHit.p*=1f/(lightList.size());
		Vector3f lightDir = StaticVecmath.sub(lightHit.position, hitRecord.position);
		Vector3f w=new Vector3f(lightDir);
		w.negate();
		w.normalize();
		lightHit.w=w;
		float d2 = lightDir.lengthSquared();
		lightDir.normalize();
		
		// Shadow ray
		Ray shadowRay=new Ray(hitRecord.position, lightDir);
		HitRecord shadowHit=root.intersect(shadowRay);
		
		if(shadowHit!=null&& shadowHit.material.castsShadows()){
			Vector3f dist = StaticVecmath.sub(shadowHit.position, hitRecord.position);
			if(shadowHit.t>0.00001&&dist.lengthSquared()<d2){
				return new Spectrum(0,0,0);
			}
		}
					
		// Evaluate the BRDF
		Spectrum brdfValue = hitRecord.material.evaluateBRDF(hitRecord, hitRecord.w, lightDir);
		
		// Multiply together factors relevant for shading, that is, brdf * emission * ndotl * geometry term
		Spectrum s = new Spectrum(brdfValue);
		
		// Multiply with emission
		s.mult(lightHit.material.evaluateEmission(lightHit, lightHit.w));
		
		// Multiply with cosine of surface normal and incident direction		
		float ndotl = shadowHit.normal.dot(shadowHit.getNormalizedDirection());
		ndotl = Math.max(ndotl, 0.f);
		s.mult(ndotl);

		ndotl = hitRecord.normal.dot(lightDir);
		ndotl = Math.max(ndotl, 0.f);
		s.mult(ndotl);
				
		// Geometry term: multiply with 1/(squared distance), only correct like this 
		// for point lights (not area lights)!
		s.mult(1.f/d2);
		s.mult(1/lightHit.p);
		
		s.mult(0.8f);
		return s;
	}

	private float[] getRandomSample() {
		float[] sample={(float) Math.random(),(float) Math.random()};
		return sample;
	}

	public float[][] makePixelSamples(Sampler sampler, int n) {
		return sampler.makeSamples(n, 2);
	}


}
