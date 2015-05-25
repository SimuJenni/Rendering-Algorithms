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

public class Copy_2_of_BDPathTracingIntegrator implements Integrator {
	LightList lightList;
	Intersectable root;
	RandomSampler sampler;
	
	public Copy_2_of_BDPathTracingIntegrator(Scene scene)
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


		for(PathVertice eyeVert : eyePath){
			int t = eyeVert.k;
			
			c=eyeVert.hitRecord.material.evaluateEmission(eyeVert.hitRecord, eyeVert.hitRecord.w);
			if(c!=null){
				c.mult(eyeVert.alpha);
				float w=computeWeightNoLight(t,eyePath,lightPath);
				c.mult(w);
				outgoing.add(c);
				continue;
			}

			for(PathVertice lightVert : lightPath){
				int s = lightVert.k;
				Vector3f dir=StaticVecmath.sub(lightVert.hitRecord.position, eyeVert.hitRecord.position);
				float d2=dir.lengthSquared();
				dir.normalize();
				Ray visRay=new Ray(eyeVert.hitRecord.position, dir);
				HitRecord visHit=root.intersect(visRay);
				if(visHit==null||visHit.t<0.001||StaticVecmath.sub(eyeVert.hitRecord.position, visHit.position).lengthSquared()<d2-0.0001)
					continue;
				
				float cos_eye=Math.max(0, eyeVert.hitRecord.normal.dot(dir));
				float p_s=eyeVert.hitRecord.material.getPobability(eyeVert.hitRecord.w,dir,eyeVert.hitRecord.normal);
				dir.negate();
				float cos_light=Math.max(0, lightVert.hitRecord.normal.dot(dir));
				float p_t=lightVert.hitRecord.material.getPobability(lightVert.hitRecord.w,dir,lightVert.hitRecord.normal);

				float G = cos_eye*cos_light/d2;
				if(Math.abs(G)<0.0001)
					continue;

				Spectrum brdf_light;
				if(s==0)
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
				float w=computeWeight(s,t,eyePath,lightPath,p_s,p_t,G);
				c.mult(w);
				outgoing.add(new Spectrum(c));
			}		
		}
		return outgoing;			

	}
	
	private float computeWeightNoLight(int t, ArrayList<PathVertice> eyePath,
			ArrayList<PathVertice> lightPath) {
		float v_prod;
		float p_t=lightPath.get(0).hitRecord.p;
//		float p_t=1;

		if(t==1)
			v_prod=p_t/eyePath.get(0).hitRecord.p;
		else
			v_prod=p_t/(eyePath.get(t-2).p_forw);
		
		float v_sum=v_prod;

		for(int i=t-1;i>1;i--){
			v_prod*=eyePath.get(i).p_backw/(eyePath.get(i-2).p_forw);
			v_sum+=v_prod;
		}
		if(t>1){
			v_prod*=eyePath.get(1).p_backw/eyePath.get(0).hitRecord.p;
		}
		
		return 1.f/(v_sum);
	}


	private float computeWeight(int s, int t, ArrayList<PathVertice> eyePath,
			ArrayList<PathVertice> lightPath, float p_s, float p_t, float g) {
		float u_prod;
		if(s==0)
			u_prod=p_s/lightPath.get(0).hitRecord.p;
		else
			u_prod=p_s/(lightPath.get(s-1).p_forw);
		

		float v_prod;
		if(t==1)
			v_prod=p_t/eyePath.get(0).hitRecord.p;
		else
			v_prod=p_t/eyePath.get(t-2).p_forw;
		
		float u_sum=u_prod;
		float v_sum=v_prod;
		
		for(int i=s;i>1;i--){
			u_prod*=lightPath.get(i).p_backw/(lightPath.get(i-2).p_forw);
			u_sum+=u_prod;
		}
		if(s>0){
			u_prod*=lightPath.get(1).p_backw/lightPath.get(0).hitRecord.p;
		}

		for(int i=t-1;i>1;i--){
			v_prod*=eyePath.get(i).p_backw/(eyePath.get(i-2).p_forw);
			v_sum+=v_prod;
		}
		if(t>1){
			v_prod*=eyePath.get(1).p_backw/eyePath.get(0).hitRecord.p;
		}
		
		return 1.f/(v_sum+u_sum+1.f);
	}


	private ArrayList<PathVertice> generateLightPath() {
		ArrayList<PathVertice> lightPath=new ArrayList<PathVertice>();
		int s=0;
		float p_forw = 1, p_backw = 1, q=0.5f;
		float p=1, cos_in = 1, cos_out = 1, G=1;
		Vector3f newPos, prevPos = null;
		Spectrum alpha_L=new Spectrum(1), brdf=new Spectrum(1);
		Ray r = null;

		while(russionRouletteStop(s)<q){
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
				p_forw=sample.p;
				p_backw=0;
				brdf=sample.emission;
			} else{
				hitRecord = root.intersect(r);
				if(hitRecord==null)
					break;
				float[][] samples=sampler.makeSamples(1, 2);
				sample=hitRecord.material.getShadingSample(hitRecord, samples[0]);
//				cos_in=Math.max(hitRecord.normal.dot(hitRecord.w), 0);
				cos_in=Math.abs(hitRecord.normal.dot(hitRecord.w));
				p_forw=sample.p/cos_out*G;  // cos_out not correct here!
				p_backw=hitRecord.material.getPobability(sample.w,hitRecord.w,hitRecord.normal)/cos_in*G;
				brdf=sample.brdf;
			}
			
			if(cos_in==0)
				return lightPath;
			
			lightPath.add(new PathVertice(hitRecord, new Spectrum(alpha_L), G,p_forw, p_backw, s));

						
			newPos = hitRecord.position;
			
			Vector3f sampleDir=new Vector3f(sample.w);
			sampleDir.normalize();
//			cos_out=Math.max(sampleDir.dot(hitRecord.normal), 0);
			cos_out=Math.abs(sampleDir.dot(hitRecord.normal));
			
			if(s>0){
				float d2=StaticVecmath.dist2(prevPos, newPos);
				G=cos_out*cos_in/(d2);
				alpha_L.mult(brdf);
				if(s==1)
					alpha_L.mult(cos_out/(p));
				else
					alpha_L.mult(cos_out/(p*(1-q)));

			}
			if(s==0){
				alpha_L.mult(1.f/hitRecord.p);
			}
			
			p=sample.p;
									
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
		float p_forw = 1, p_backw = 1;
		Vector3f prevPos = r.origin, newPos;
		
		while(russionRouletteStop(t)<q){
			
			HitRecord hitRecord = root.intersect(r);
			
			if(hitRecord==null)
				break;
						
			newPos=hitRecord.position;
			float d2=StaticVecmath.dist2(prevPos, newPos);
//			cos_in=Math.max(hitRecord.normal.dot(hitRecord.w), 0);
			cos_in=Math.abs(hitRecord.normal.dot(hitRecord.w));

			if(cos_in==0)
				return eyePath;

			float[][] samples=sampler.makeSamples(1, 2);
			ShadingSample sample=hitRecord.material.getShadingSample(hitRecord, samples[0]);
			
			Vector3f sampleDir=new Vector3f(sample.w);
			sampleDir.normalize();
			
			if(t>1){
				G=cos_out*cos_in/(d2);
				alpha_E.mult(brdf);
				if(t==2)
					alpha_E.mult(cos_out/(p));
				else
					alpha_E.mult(cos_out/(p*(1-q)));
				p_forw=sample.p/cos_out*G;
				p_backw=hitRecord.material.getPobability(sampleDir,hitRecord.w,hitRecord.normal)/cos_in*G;
			}
			if(t==1){
				G=cos_in/d2;
				hitRecord.p=cos_in/d2;
				p_forw=sample.p/cos_out*G;
				p_backw=hitRecord.material.getPobability(sampleDir,hitRecord.w,hitRecord.normal)/cos_in*G;
			}
			
			brdf=sample.brdf;
//			cos_out=Math.max(sampleDir.dot(hitRecord.normal), 0);
			cos_out=Math.abs(sampleDir.dot(hitRecord.normal));

			p=sample.p;
									
			eyePath.add(new PathVertice(hitRecord, new Spectrum(alpha_E), G,p_forw, p_backw, t));
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
