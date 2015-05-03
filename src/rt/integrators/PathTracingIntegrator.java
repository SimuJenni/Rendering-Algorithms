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
import rt.lightsources.RectangleLight;
import rt.materials.AreaLightMaterial;
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
		Spectrum color = new Spectrum(0,0,0);	
		Spectrum alpha = new Spectrum(1,1,1);
		
		if (hitRecord == null){
			return new Spectrum();
		}
		
		if(hitRecord.material instanceof AreaLightMaterial){
			return hitRecord.material.evaluateEmission(hitRecord, hitRecord.w);
		}
		
		int k=0;
		boolean specular=false;
		float p_w=1.f/lightList.size();
		
		while(true){
			LightGeometry light=lightList.getRandomLight();

			if(hitRecord==null)
				break;
			
			Spectrum emission = hitRecord.material.evaluateEmission(hitRecord, hitRecord.w);
			if (emission != null) {
				if (k == 0 || specular)
					color.add(emission);
				break;
			}
								
			// Sample the light source
			float[][] samples=sampler.makeSamples(2, 2);
			HitRecord lightSample=light.sample(samples[0]);
			Vector3f lightDir = new Vector3f(lightSample.position);
			lightDir.sub(hitRecord.position);
			float d2=lightDir.lengthSquared();
			lightSample.w=new Vector3f(lightDir);
			lightSample.w.normalize();
			lightSample.w.negate();
			float cosTheta=Math.max(lightSample.normal.dot(lightSample.w), 0);
			p_w=lightSample.p*d2/cosTheta;
			lightDir.normalize();		
			Spectrum shadeLight=hitRecord.material.evaluateBRDF(hitRecord, hitRecord.w, lightDir);
			shadeLight.mult(lightSample.material.evaluateEmission(lightSample, lightSample.w));
			shadeLight.divide(new Spectrum(p_w));
			shadeLight.mult(new Spectrum(Math.max(hitRecord.normal.dot(lightDir),0)));
			
			// Shadow ray
			// russian roulette
			float q=(float) Math.random();
			float p=computeContribution(shadeLight);
			if(q<p){	
				Ray shadowRay=new Ray(hitRecord.position, lightDir);
				HitRecord shadowHit=root.intersect(shadowRay);	
				if(shadowHit!=null&& shadowHit.material.castsShadows()){
					Vector3f dist = StaticVecmath.sub(shadowHit.position, hitRecord.position);
					if(shadowHit.t>0.001&&dist.lengthSquared()<d2-0.0001){
						shadeLight=new Spectrum();
					}
				}
				shadeLight.mult(1/(p));
			} else
				shadeLight=new Spectrum();

			shadeLight.mult(alpha);
			color.add(shadeLight);
			
			// russian roulette
			q=russionRouletteStop(k);
			if(q>0.5f)
				break;
			
			// sample next ray
			ShadingSample s=hitRecord.material.getShadingSample(hitRecord, samples[1]);
			specular=s.isSpecular;
			Vector3f sampleDir=new Vector3f(s.w);
			sampleDir.normalize();
			Ray newRay=new Ray(hitRecord.position, sampleDir);
			Spectrum brdf=hitRecord.material.evaluateBRDF(hitRecord, hitRecord.w, sampleDir);
			alpha.mult(brdf);
			if (!specular) {
				float cos=sampleDir.dot(hitRecord.normal);
				alpha.mult(cos);
			} 
			alpha.mult(new Spectrum(1/(s.p*(1-q))));
			k++;
			hitRecord=root.intersect(newRay);
		}
			
		return color;
			
	}


	private float computeContribution(Spectrum shadeLight) {
		return Math.max((Math.abs(shadeLight.r)+Math.abs(shadeLight.g)+Math.abs(shadeLight.b))/3,0);
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
