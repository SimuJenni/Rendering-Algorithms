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
import rt.samplers.RandomSampler;

public class PathTracingIntegrator implements Integrator {
	LightList lightList;
	Intersectable root;
	RandomSampler sampler;
	
	public PathTracingIntegrator(Scene scene)
	{
		this.lightList = scene.getLightList();
		this.root = scene.getIntersectable();
		this.sampler=new RandomSampler();
	}


	public Spectrum integrate(Ray r) {

		HitRecord hitRecord = root.intersect(r);
		// immediately return background color if nothing was hit
		if(hitRecord == null) { 
			return new Spectrum(0,0,0);
		}	
		Spectrum color = new Spectrum(0,0,0);	
		Spectrum alpha = new Spectrum(1,1,1);
		int k=0;
		LightGeometry light=lightList.getRandomLight();
		float p_w=1.f/lightList.size();
		
		while(true){
			if(hitRecord==null)
				break;
			float[][] samples=sampler.makeSamples(2, 2);
			HitRecord lightSample=light.sample(samples[0]);
			Vector3f lightDir = new Vector3f(lightSample.position);
			lightDir.sub(hitRecord.position);
			lightSample.w=new Vector3f(lightDir);
			lightSample.w.normalize();
			lightSample.w.negate();
			float cosTheta=lightSample.normal.dot(lightSample.w);
			p_w=p_w*lightSample.p*lightDir.lengthSquared()*cosTheta;
			lightDir.normalize();
			Spectrum shadeLight=hitRecord.material.evaluateBRDF(hitRecord, hitRecord.w, lightDir);
			shadeLight.mult(lightSample.material.evaluateEmission(hitRecord, lightSample.w));
			shadeLight.divide(p_w);
			shadeLight.mult(hitRecord.normal.dot(lightDir));
			shadeLight.mult(alpha);
			color.add(shadeLight);
			float q=russionRouletteStop(k);
			if(q>0.5f)
				break;
			ShadingSample s=hitRecord.material.getShadingSample(hitRecord, samples[1]);
			Vector3f sampleDir=new Vector3f(s.w);
			Ray newRay=new Ray(hitRecord.position, sampleDir);
			Spectrum brdf=hitRecord.material.evaluateBRDF(hitRecord, hitRecord.w, sampleDir);
			float cos=sampleDir.dot(hitRecord.normal);
			alpha.mult(brdf);
			alpha.mult(cos/(s.p*(1-q)));
			k++;
			hitRecord=root.intersect(newRay);
		}
			
		return color;
		
//		if(hitRecord.material.hasSpecularReflection()&&r.recursionDepth>0){
//			ShadingSample s=hitRecord.material.evaluateSpecularReflection(hitRecord);
//			if(s!=null){
//				reflected=new Spectrum(s.brdf);
//				Vector3f reflOri=new Vector3f(s.w);
//				reflOri.scaleAdd(0.0001f, hitRecord.position);
//				Ray reflectedRay=new Ray(reflOri,s.w);
//				reflectedRay.recursionDepth=r.recursionDepth-1;
//				reflected.mult(integrate(reflectedRay));
//			}
//		}
//		
//		Spectrum refracted = new Spectrum();	
//		if(hitRecord.material.hasSpecularRefraction()&&r.recursionDepth>0){
//			ShadingSample s=hitRecord.material.evaluateSpecularRefraction(hitRecord);
//			if(s!=null){
//				refracted=new Spectrum(s.brdf);
//				Vector3f reffOri=new Vector3f(s.w);
//				reffOri.scaleAdd(0.0001f, hitRecord.position);
//				Ray refractedRay=new Ray(reffOri,s.w);
//				refractedRay.recursionDepth=r.recursionDepth-1;
//				refracted.mult(integrate(refractedRay));
//			}
//		}
//		
//		if(hitRecord.material.hasSpecularRefraction()||hitRecord.material.hasSpecularReflection()){
//			Spectrum refractPlusReflect = new Spectrum();
//			refractPlusReflect.add(refracted);
//			refractPlusReflect.add(reflected);
//			return refractPlusReflect;
//		}
//			
//			
//		// Iterate over all light sources
//		Iterator<LightGeometry> it = lightList.iterator();
//		Spectrum outgoing = new Spectrum();
//
//		while(it.hasNext()) {
//			LightGeometry lightSource = it.next();
//			
//			// Make direction from hit point to light source position; this is only supposed to work with point lights
//			float dummySample[] = new float[2];
//			HitRecord lightHit = lightSource.sample(dummySample);
//			Vector3f lightDir = StaticVecmath.sub(lightHit.position, hitRecord.position);
//			float d2 = lightDir.lengthSquared();
//			lightDir.normalize();
//			
//			// Shadow ray
//			Ray shadowRay=new Ray(hitRecord.position, lightDir);
//			HitRecord shadowHit=root.intersect(shadowRay);
//			
//			if(shadowHit!=null&& shadowHit.material.castsShadows()){
//				Vector3f dist = StaticVecmath.sub(shadowHit.position, hitRecord.position);
//				if(shadowHit.t>0.00001&&dist.lengthSquared()<d2){
//					continue;
//				}
//			}
//						
//			// Evaluate the BRDF
//			Spectrum brdfValue = hitRecord.material.evaluateBRDF(hitRecord, hitRecord.w, lightDir);
//			
//			// Multiply together factors relevant for shading, that is, brdf * emission * ndotl * geometry term
//			Spectrum s = new Spectrum(brdfValue);
//			
//			// Multiply with emission
//			s.mult(lightHit.material.evaluateEmission(lightHit, StaticVecmath.negate(lightDir)));
//			
//			// Multiply with cosine of surface normal and incident direction
//			float ndotl = hitRecord.normal.dot(lightDir);
//			ndotl = Math.max(ndotl, 0.f);
//			s.mult(ndotl);
//			
//			// Geometry term: multiply with 1/(squared distance), only correct like this 
//			// for point lights (not area lights)!
//			s.mult(1.f/d2);
//			
//			// Accumulate
//			outgoing.add(s);
//		}
//		return outgoing;	
	}


	private float russionRouletteStop(int k) {
		if(k<3)
			return 0;
		else{
			return (float) Math.random();
		}
	}


	public float[][] makePixelSamples(Sampler sampler, int n) {
		return sampler.makeSamples(n, 2);
	}
}
