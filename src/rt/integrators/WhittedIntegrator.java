package rt.integrators;

import java.util.Iterator;

import javax.vecmath.*;

import rt.HitRecord;
import rt.Integrator;
import rt.Intersectable;
import rt.LightList;
import rt.LightGeometry;
import rt.Material.ShadingSample;
import rt.Ray;
import rt.Sampler;
import rt.Scene;
import rt.Spectrum;
import rt.StaticVecmath;

/**
 * Integrator for Whitted style ray tracing. This is a basic version that needs to be extended!
 */
public class WhittedIntegrator implements Integrator {

	LightList lightList;
	Intersectable root;
	
	public WhittedIntegrator(Scene scene)
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
		Spectrum reflected = new Spectrum();	
		if(hitRecord.material.hasSpecularReflection()&&r.recursionDepth>0){
			ShadingSample s=hitRecord.material.evaluateSpecularReflection(hitRecord);
			if(s!=null){
				reflected=new Spectrum(s.brdf);
				Vector3f reflOri=new Vector3f(s.w);
				reflOri.scaleAdd(0.0001f, hitRecord.position);
				Ray reflectedRay=new Ray(reflOri,s.w);
				reflectedRay.recursionDepth=r.recursionDepth-1;
				reflected.mult(integrate(reflectedRay));
			}
		}
		
		Spectrum refracted = new Spectrum();	
		if(hitRecord.material.hasSpecularRefraction()&&r.recursionDepth>0){
			ShadingSample s=hitRecord.material.evaluateSpecularRefraction(hitRecord);
			if(s!=null){
				refracted=new Spectrum(s.brdf);
				Vector3f reffOri=new Vector3f(s.w);
				reffOri.scaleAdd(0.0001f, hitRecord.position);
				Ray refractedRay=new Ray(reffOri,s.w);
				refractedRay.recursionDepth=r.recursionDepth-1;
				refracted.mult(integrate(refractedRay));
			}
		}
		
		if(hitRecord.material.hasSpecularRefraction()||hitRecord.material.hasSpecularReflection()){
			Spectrum refractPlusReflect = new Spectrum();
			refractPlusReflect.add(refracted);
			refractPlusReflect.add(reflected);
			return refractPlusReflect;
		}
			
			
		// Iterate over all light sources
		Iterator<LightGeometry> it = lightList.iterator();
		Spectrum outgoing = new Spectrum();

		while(it.hasNext()) {
			LightGeometry lightSource = it.next();
			
			// Make direction from hit point to light source position; this is only supposed to work with point lights
			float dummySample[] = new float[2];
			HitRecord lightHit = lightSource.sample(dummySample);
			Vector3f lightDir = StaticVecmath.sub(lightHit.position, hitRecord.position);
			float d2 = lightDir.lengthSquared();
			lightDir.normalize();
			
			// Shadow ray
			Ray shadowRay=new Ray(hitRecord.position, lightDir);
			HitRecord shadowHit=root.intersect(shadowRay);
			
			if(shadowHit!=null&& shadowHit.material.castsShadows()){
				Vector3f dist = StaticVecmath.sub(shadowHit.position, hitRecord.position);
				if(shadowHit.t>0.00001&&dist.lengthSquared()<d2){
					continue;
				}
			}
						
			// Evaluate the BRDF
			Spectrum brdfValue = hitRecord.material.evaluateBRDF(hitRecord, hitRecord.w, lightDir);
			
			// Multiply together factors relevant for shading, that is, brdf * emission * ndotl * geometry term
			Spectrum s = new Spectrum(brdfValue);
			
			// Multiply with emission
			s.mult(lightHit.material.evaluateEmission(lightHit, StaticVecmath.negate(lightDir)));
			
			// Multiply with cosine of surface normal and incident direction
			float ndotl = hitRecord.normal.dot(lightDir);
			ndotl = Math.max(ndotl, 0.f);
			s.mult(ndotl);
			
			// Geometry term: multiply with 1/(squared distance), only correct like this 
			// for point lights (not area lights)!
			s.mult(1.f/d2);
			
			// Accumulate
			outgoing.add(s);
		}
		return outgoing;	
	}

	public float[][] makePixelSamples(Sampler sampler, int n) {
		return sampler.makeSamples(n, 2);
	}

}
