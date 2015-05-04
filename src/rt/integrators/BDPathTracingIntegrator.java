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
		
		ArrayList<PathVertice> eyePath = generateEyePath(r);
		
		if(eyePath.size()==0)
			return new Spectrum();
		
		ArrayList<PathVertice> lightPath=generateLightPath();
		
		Spectrum outgoing=new Spectrum(0);
		Spectrum c=new Spectrum(0);

		float v_prod=1;
		float v_sum=v_prod;	
		float p_backL=1; //?? otherway around?
		float p_backE=1;

		for(PathVertice eyeVert : eyePath){
			float u_prod=1;
			float u_sum=u_prod;

			v_prod*=eyeVert.p_backw/eyeVert.p_forw;
			v_sum+=v_prod;
			
			c=eyeVert.hitRecord.material.evaluateEmission(eyeVert.hitRecord, eyeVert.hitRecord.w);
			if(c!=null){
				c.mult(eyeVert.alpha);
				float w=1f/(1f+v_sum+u_sum);
				c.mult(w);
				outgoing.add(c);
				break;
			}

			for(PathVertice lightVert : lightPath){
//				PathVertice lightVert=lightPath.get(0);
				u_prod*=lightVert.p_backw/lightVert.p_forw;
				u_sum+=u_prod;
				int s = lightVert.k;
				int t = eyeVert.k;
				Vector3f dir=StaticVecmath.sub(lightVert.hitRecord.position, eyeVert.hitRecord.position);
				float d2=dir.lengthSquared();
				dir.normalize();
				Ray visRay=new Ray(eyeVert.hitRecord.position, dir);
				HitRecord visHit=root.intersect(visRay);
				if(visHit==null||visHit.t<0.001||StaticVecmath.sub(eyeVert.hitRecord.position, visHit.position).lengthSquared()<d2-0.0001)
					continue;
				float cos_eye=Math.max(0, eyeVert.hitRecord.normal.dot(dir));
				dir.negate();
				float cos_light=Math.max(0, lightVert.hitRecord.normal.dot(dir));

				float G = cos_eye*cos_light/d2;
				Spectrum brdf_light;
				if(s==1)
					brdf_light=lightVert.hitRecord.material.evaluateEmission(lightVert.hitRecord, dir);
				else
					brdf_light=lightVert.hitRecord.material.evaluateBRDF(lightVert.hitRecord, lightVert.hitRecord.w, dir);
				dir.negate();
				Spectrum brdf_eye=eyeVert.hitRecord.material.evaluateBRDF(eyeVert.hitRecord, dir, eyeVert.hitRecord.w);

				c=new Spectrum(brdf_eye);
				c.mult(brdf_light);
				c.mult(G);
				
				c.mult(eyeVert.alpha);
				c.mult(lightVert.alpha);
				float w=1f/(1f+v_sum+u_sum);
				c.mult(w);
				outgoing.add(new Spectrum(c));
			}
				
		}
		return outgoing;			
	}


	private ArrayList<PathVertice> generateLightPath() {
		ArrayList<PathVertice> lightPath=new ArrayList<PathVertice>();
		int s=1;
		float p_L = 1, p_Eprev = 1, q=0.5f;
		float p=1, cos_in = 1, cos_out = 1, G=1;
		Vector3f newPos, prevPos = null;
		Spectrum alpha_L=new Spectrum(1), brdf=new Spectrum(1);
		Ray r = null;

		while(russionRouletteStop(s)<q){
			HitRecord hitRecord;
			ShadingSample sample;
			if(s==1){
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
				cos_in=Math.max(hitRecord.normal.dot(hitRecord.w), 0);
			}
			
			if(cos_in==0)
				return lightPath;
						
			newPos = hitRecord.position;
			
			Vector3f sampleDir=new Vector3f(sample.w);
			sampleDir.normalize();
			
			if(s>1){
				float d2=StaticVecmath.dist2(prevPos, newPos);
				G=cos_out*cos_in/(d2);
				alpha_L.mult(brdf);
				if(s==2)
					alpha_L.mult(cos_out/(p));
				else
					alpha_L.mult(cos_out/(p*(1-q)));
				p_L=p/cos_out*G;
				p_Eprev=hitRecord.material.getPobability(sampleDir,hitRecord.w,hitRecord.normal)/cos_in*G;
				brdf=sample.brdf;
			}
			if(s==1){
				p_L=hitRecord.p;
				p_Eprev=1;
				alpha_L.mult(1.f/hitRecord.p);
				brdf=sample.emission;
			}
			
			cos_out=Math.max(sampleDir.dot(hitRecord.normal), 0);
			p=sample.p;
									
			lightPath.add(new PathVertice(hitRecord, new Spectrum(alpha_L), G,p_L, p_Eprev, s));
			r=new Ray(hitRecord.position, sampleDir);
			prevPos=hitRecord.position;
			s++;
		}
		
		return lightPath;
	}


	private ArrayList<PathVertice> generateEyePath(Ray r) {
		
		ArrayList<PathVertice> eyePath=new ArrayList<PathVertice>();
		
		int t=1;
		Spectrum alpha_E=new Spectrum(1);
		Spectrum brdf=new Spectrum(1);
		float cos_out = 1; 
		float p=1; 
		float q=0.5f;
		float G=1;
		float cos_in = 1;
		float p_E = 1, p_Lprev = 1;
		Vector3f prevPos = r.origin, newPos;
		
		while(russionRouletteStop(t)<q){
			
			HitRecord hitRecord = root.intersect(r);
			
			if(hitRecord==null)
				break;
						
			newPos=hitRecord.position;
			float d2=StaticVecmath.dist2(prevPos, newPos);
			cos_in=Math.max(hitRecord.normal.dot(hitRecord.w), 0);
			
			if(cos_in==0)
				return eyePath;

			float[][] samples=sampler.makeSamples(1, 2);
			ShadingSample s=hitRecord.material.getShadingSample(hitRecord, samples[0]);
			
			Vector3f sampleDir=new Vector3f(s.w);
			sampleDir.normalize();
			
			if(t>1){
				G=cos_out*cos_in/(d2);
				alpha_E.mult(brdf);
				if(t==2)
					alpha_E.mult(cos_out/(p));
				else
					alpha_E.mult(cos_out/(p*(1-q)));
				p_E=p/cos_out*G;
				p_Lprev=hitRecord.material.getPobability(sampleDir,hitRecord.w,hitRecord.normal)/cos_in*G;
			}
			if(t==1){
				G=cos_in/d2;
				p_E=cos_in/StaticVecmath.dist2(r.origin,newPos);
				p_Lprev=hitRecord.material.getPobability(sampleDir,hitRecord.w,hitRecord.normal)/cos_in*G;
			}
			
			brdf=s.brdf;
			cos_out=Math.max(sampleDir.dot(hitRecord.normal), 0);
			p=s.p;
									
			eyePath.add(new PathVertice(hitRecord, new Spectrum(alpha_E), G,p_E, p_Lprev, t));
			r=new Ray(hitRecord.position, sampleDir);
			prevPos=hitRecord.position;
			t++;
		}
		
		return eyePath;
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
