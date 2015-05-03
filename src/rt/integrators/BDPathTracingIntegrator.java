package rt.integrators;

import java.util.ArrayList;
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

public class BDPathTracingIntegrator implements Integrator {
	LightList lightList;
	Intersectable root;
	RandomSampler sampler;
	
	public BDPathTracingIntegrator(Scene scene)
	{
		this.lightList = scene.getLightList();
		this.root = scene.getIntersectable();
		this.sampler=new RandomSampler();
	}


	public Spectrum integrate(Ray r) {
		
		ArrayList<PathVertice> eyePath=new ArrayList<PathVertice>();
		ArrayList<PathVertice> lightPath=new ArrayList<PathVertice>();
		
		int t=1;
		Spectrum alpha_E=new Spectrum(1);
		Spectrum brdf=new Spectrum(1);
		float cos_out = 0; 
		float p=1; 
		float q=0.5f;
		float G=1;
		float cos_in = 0;
		float p_E = 0, p_Lprev = 0;
		Vector3f prevPos = null, newPos;
		
		while(russionRouletteStop(t)<q){
			
			HitRecord hitRecord = root.intersect(r);
			
			if(hitRecord==null)
				break;
						
			newPos=hitRecord.position;
			cos_in=hitRecord.normal.dot(hitRecord.w);


			float[][] samples=sampler.makeSamples(1, 2);
			ShadingSample s=hitRecord.material.getShadingSample(hitRecord, samples[0]);
			
			Vector3f sampleDir=new Vector3f(s.w);
			sampleDir.normalize();
			
			if(t>1){
				float d2=StaticVecmath.dist2(prevPos, newPos);
				G=cos_out*cos_in/(d2);
				alpha_E.mult(brdf);
				alpha_E.mult(cos_out/(p*(1-q)));
				p_E=p/cos_out*G;
				p_Lprev=hitRecord.material.getPobability(sampleDir,hitRecord.w,hitRecord.normal)/cos_in*G;
			}
			if(t==1){
				p_E=cos_in/StaticVecmath.dist2(r.origin,newPos);
				p_Lprev=hitRecord.material.getPobability(sampleDir,hitRecord.w,hitRecord.normal)/cos_in*G;
			}
			
			brdf=s.brdf;
			cos_out=sampleDir.dot(hitRecord.normal);
			p=s.p;
									
			eyePath.add(new PathVertice(hitRecord, alpha_E, G,p_E, p_Lprev, t));
			r=new Ray(hitRecord.position, sampleDir);
			prevPos=hitRecord.position;
			t++;
		}
		
		
		int s=0;
		float p_L = 0, p_Eprev = 0;
		Spectrum alpha_L=new Spectrum(1);

		while(russionRouletteStop(t)<q){
			HitRecord hitRecord;
			ShadingSample sample;
			if(s==0){
				LightGeometry light=lightList.getRandomLight();
				p=1.f/lightList.size();
				float[][] samp=sampler.makeSamples(2, 2);
				hitRecord=light.sample(samp[0]);
				hitRecord.p*=p;
				hitRecord.makeTangentFrame(hitRecord.normal);
				sample=hitRecord.material.getEmissionSample(hitRecord, samp[1]);
			} else{
				hitRecord = root.intersect(r);
				if(hitRecord==null)
					break;
				float[][] samples=sampler.makeSamples(1, 2);
				sample=hitRecord.material.getShadingSample(hitRecord, samples[0]);
				cos_in=hitRecord.normal.dot(hitRecord.w);
			}
						
			newPos=hitRecord.position;
			
			Vector3f sampleDir=new Vector3f(sample.w);
			sampleDir.normalize();
			
			if(s>1){
				float d2=StaticVecmath.dist2(prevPos, newPos);
				G=cos_out*cos_in/(d2);
				alpha_L.mult(brdf);
				alpha_L.mult(cos_out/(p*(1-q)));
				p_L=p/cos_out*G;
				p_Eprev=hitRecord.material.getPobability(sampleDir,hitRecord.w,hitRecord.normal)/cos_in*G;
				brdf=sample.brdf;
			}
			if(s==0){
				p_L=hitRecord.p;
				p_Eprev=0;
				brdf=sample.emission;
			}
			
			cos_out=sampleDir.dot(hitRecord.normal);
			p=sample.p;
									
			lightPath.add(new PathVertice(hitRecord, alpha_L, G,p_L, p_Eprev, s));
			r=new Ray(hitRecord.position, sampleDir);
			prevPos=hitRecord.position;
			s++;
		}
		
		Spectrum c=new Spectrum(0);
		Spectrum outgoing=new Spectrum(0);

		
		for(PathVertice eyeVert : eyePath){

			for(PathVertice lightVert : lightPath){
				s=lightVert.k;
				t=eyeVert.k;
				Vector3f dir=StaticVecmath.sub(lightVert.hitRecord.position, eyeVert.hitRecord.position);
				float d2=dir.lengthSquared();
				dir.normalize();
				Ray visRay=new Ray(eyeVert.hitRecord.position, dir);
				HitRecord visHit=root.intersect(visRay);
				if(visHit==null||StaticVecmath.sub(eyeVert.hitRecord.position, visHit.position).lengthSquared()<d2-0.01)
					continue;
				float cos_eye=Math.max(0, eyeVert.hitRecord.normal.dot(dir));
				dir.negate();
				float cos_light=Math.max(0, lightVert.hitRecord.normal.dot(dir));

				G=cos_eye*cos_light/d2;
				Spectrum brdf_eye=eyeVert.hitRecord.material.evaluateBRDF(eyeVert.hitRecord, dir, eyeVert.hitRecord.w);
				Spectrum brdf_light=lightVert.hitRecord.material.evaluateBRDF(lightVert.hitRecord, lightVert.hitRecord.w, dir);
				c=new Spectrum(brdf_eye);
				c.mult(brdf_light);
				c.mult(G);
				
				c.mult(eyeVert.alpha);
				c.mult(lightVert.alpha);
				outgoing.add(c);
				
			}
				
		}
		
		return c;
					
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
