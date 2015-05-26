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
import rt.films.BoxFilterFilm;
import rt.lightsources.RectangleLight;
import rt.materials.AreaLightMaterial;
import rt.samplers.RandomSampler;

public class BDPathTracingIntegrator implements Integrator {
	LightList lightList;
	Intersectable root;
	RandomSampler sampler;
	final float delta=0.00001f;
	private BoxFilterFilm lightImage;
	private Scene scene;
	
	public BDPathTracingIntegrator(Scene scene, BoxFilterFilm lightImage)
	{
		this.scene = scene;
		this.lightList = scene.getLightList();
		this.root = scene.getIntersectable();
		this.sampler=new RandomSampler();
		this.lightImage = lightImage;
	}

	public Spectrum integrate(Ray r) {
		
//		ArrayList<PathVertice> lightPath=generateLightPath();
//		computeLightImage(lightPath, r);
//		
//		ArrayList<PathVertice> eyePath = generateEyePath(r);
//		if(eyePath.size()==0)
//			return new Spectrum();

		
		float[][] samples=sampler.makeSamples(3, 2);
		HitRecord lightSample=lightList.getRandomLight().sample(samples[0]);
		lightSample.p*=1.f/lightList.size();
		lightSample.makeTangentFrame(lightSample.normal);
		ShadingSample sampleLight=lightSample.material.getEmissionSample(lightSample, samples[1]);
		Spectrum alpha_init = new Spectrum(1,1,1);
		alpha_init.mult(1.f/lightSample.p);
		ArrayList<PathVertice> lightPath = generatePath(lightSample, 0, sampleLight.emission, alpha_init, 
				sampleLight, 1, 0);
		
		computeLightImage(lightPath, r);
		
		HitRecord hit = root.intersect(r);
		if(hit == null)
			return new Spectrum();
		ShadingSample sample = hit.material.getShadingSample(hit, samples[1]);
		float dist2=StaticVecmath.dist2(r.origin, hit.position);
		float cos_in=Math.abs(hit.normal.dot(hit.w));
		hit.p=cos_in/dist2;
		ArrayList<PathVertice> eyePath = generatePath(hit, 0, sample.brdf, new Spectrum(1,1,1), 
				sample, cos_in/dist2, hit.material.getPobability(sample.w,hit.w,hit.normal)/dist2);
		
		
		Spectrum outgoing=new Spectrum(0);
		Spectrum c=new Spectrum(0);

		for(PathVertice eyeVert : eyePath){
			int t = eyeVert.k;
			if(eyeVert.hitRecord.material.hasSpecularReflection())
				continue;
			
			c=eyeVert.hitRecord.material.evaluateEmission(eyeVert.hitRecord, eyeVert.hitRecord.w);
			if(c!=null){
				c.mult(eyeVert.alpha);
				float w=computeWeightNoLight(t,eyePath,lightPath);
				c.mult(w);
				outgoing.add(c);
//				continue;
			}

			for(PathVertice lightVert : lightPath){
//				PathVertice lightVert=lightPath.get(0);
				if(lightVert.hitRecord.material.hasSpecularReflection())
					continue;
				int s = lightVert.k;
				Vector3f dir=StaticVecmath.sub(lightVert.hitRecord.position, eyeVert.hitRecord.position);
				float d2=dir.lengthSquared();
				dir.normalize();
				Ray visRay=new Ray(eyeVert.hitRecord.position, dir);
				HitRecord visHit=root.intersect(visRay);
				if(visHit==null||visHit.t<0.001||StaticVecmath.sub(eyeVert.hitRecord.position, visHit.position).lengthSquared()<d2-0.001)
					continue;
				float cos_eye=eyeVert.hitRecord.normal.dot(dir);
				float p_s=eyeVert.hitRecord.material.getPobability(eyeVert.hitRecord.w,dir,eyeVert.hitRecord.normal);
				dir.negate();
				float cos_light=lightVert.hitRecord.normal.dot(dir);
				float p_t=lightVert.hitRecord.material.getPobability(lightVert.hitRecord.w,dir,lightVert.hitRecord.normal);

				if(cos_eye<=delta||cos_light<=delta)
					continue;
				
				float G = cos_eye*cos_light/d2;
				p_t=p_t/cos_eye*G;
				p_s=p_s/cos_light*G;

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
	
	private ArrayList<PathVertice> generateLightPath() {
		ArrayList<PathVertice> lightPath=new ArrayList<PathVertice>();
		int s=0;
		float p_forw = 1, p_backw = 1, q=0.5f;
		float p=1, cos_in = 1, cos_out = 1, G=1;
		boolean wasSpecular = false;
		Vector3f prevPos = null;
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
				p_forw=hitRecord.p;
				p_backw=0;
				alpha_L.mult(1.f/hitRecord.p);
				brdf=sample.emission;
			} else{
				hitRecord = root.intersect(r);
				if(hitRecord==null)
					break;
				float[][] samples=sampler.makeSamples(1, 2);
				sample=hitRecord.material.getShadingSample(hitRecord, samples[0]);
				cos_in=Math.abs(hitRecord.normal.dot(hitRecord.w));
				float d2=StaticVecmath.dist2(prevPos, hitRecord.position);
				G=cos_out*cos_in/(d2);
				if(wasSpecular){
					p_forw=1;
					wasSpecular = false;
				} else
					p_forw=p/cos_out*G;  
				if(hitRecord.material.hasSpecularReflection())
					p_backw = 1;
				else
					p_backw=hitRecord.material.getPobability(sample.w,hitRecord.w,hitRecord.normal)/cos_in*G;
				alpha_L.mult(brdf);
				if(s==1)
					alpha_L.mult(cos_out/(p));
				else
					alpha_L.mult(cos_out/(p*(1-q)));
				brdf=sample.brdf;
			}
			
			lightPath.add(new PathVertice(hitRecord, new Spectrum(alpha_L), G,p_forw, p_backw, s));
			
			wasSpecular = hitRecord.material.hasSpecularReflection();			
			Vector3f sampleDir=new Vector3f(sample.w);
			sampleDir.normalize();
			cos_out=Math.abs(sampleDir.dot(hitRecord.normal));	
			p=sample.p;
			r=new Ray(hitRecord.position, sampleDir);
			prevPos=hitRecord.position;
			s++;
		}
		
		return lightPath;
	}

	private ArrayList<PathVertice> generateEyePath(Ray r) {
		
		ArrayList<PathVertice> eyePath=new ArrayList<PathVertice>();
		boolean wasSpecular = false;
		
		int t=0;
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
			cos_in=Math.abs(hitRecord.normal.dot(hitRecord.w));
	
			float[][] samples=sampler.makeSamples(1, 2);
			ShadingSample sample=hitRecord.material.getShadingSample(hitRecord, samples[0]);
			
			Vector3f sampleDir=new Vector3f(sample.w);
			sampleDir.normalize();
			
			if(t>0){
				G=cos_out*cos_in/(d2);
				alpha_E.mult(brdf);
				if(t==2)
					alpha_E.mult(cos_out/(p));
				else
					alpha_E.mult(cos_out/(p*(1-q)));
				if(wasSpecular){
					p_forw = 1;
					wasSpecular = false;
				}
				else
					p_forw=p/cos_out*G;
				if(hitRecord.material.hasSpecularReflection())
					p_backw = 1;
				else
					p_backw=hitRecord.material.getPobability(sampleDir,hitRecord.w,hitRecord.normal)/cos_in*G;
			}
			if(t==0){
				G=cos_in/d2;
				hitRecord.p=cos_in/d2;
				p_forw=hitRecord.p;
				if(hitRecord.material.hasSpecularReflection())
					p_backw = 1;
				else
					p_backw=hitRecord.material.getPobability(sampleDir,hitRecord.w,hitRecord.normal)/cos_in*G;
			}
							
			
			brdf=sample.brdf;
			cos_out=Math.abs(sampleDir.dot(hitRecord.normal));
	
			p=sample.p;
			wasSpecular = hitRecord.material.hasSpecularReflection();
									
			eyePath.add(new PathVertice(hitRecord, new Spectrum(alpha_E), G,p_forw, p_backw, t));
			r=new Ray(hitRecord.position, sampleDir);
			prevPos=hitRecord.position;
			t++;
		}
		
		return eyePath;
	}

	private ArrayList<PathVertice> generatePath(HitRecord hitRecord, int k, Spectrum brdf, 
		Spectrum alpha, ShadingSample sample, float G, float p_backw ) {
		ArrayList<PathVertice> path=new ArrayList<PathVertice>();
		float p_forw = hitRecord.p, q=0.5f, p=1, cos_in = 1, cos_out = 1;
		boolean wasSpecular = false;
		while(russionRouletteStop(k)<q){
			path.add(new PathVertice(hitRecord, new Spectrum(alpha), G,p_forw, p_backw, k));	
			wasSpecular = hitRecord.material.hasSpecularReflection();			
			Vector3f sampleDir=new Vector3f(sample.w);
			sampleDir.normalize();
			cos_out=Math.abs(sampleDir.dot(hitRecord.normal));	
			p=sample.p;
			Ray r=new Ray(hitRecord.position, sampleDir);
			Vector3f prevPos = hitRecord.position;
			k++;
			
			hitRecord = root.intersect(r);
			if(hitRecord==null)
				break;
			float[][] samples=sampler.makeSamples(1, 2);
			sample=hitRecord.material.getShadingSample(hitRecord, samples[0]);
			cos_in=Math.abs(hitRecord.normal.dot(hitRecord.w));
			float d2=StaticVecmath.dist2(prevPos, hitRecord.position);
			G=cos_out*cos_in/(d2);
//				if(wasSpecular){
//					p_forw=p;
//					wasSpecular = false;
//				} else
				p_forw=p/cos_out*G;  
//				if(hitRecord.material.hasSpecularReflection())
//					p_backw = hitRecord.material.getPobability(sample.w,hitRecord.w,hitRecord.normal);
//				else
				p_backw=hitRecord.material.getPobability(sample.w,hitRecord.w,hitRecord.normal)/cos_in*G;
			alpha.mult(brdf);
			if(k>1)
				alpha.mult(cos_out/(p*(1-q)));
			else
				alpha.mult(cos_out/(p));
			brdf=sample.brdf;
		}	
		
		return path;
	}

	private void computeLightImage(ArrayList<PathVertice> lightPath, Ray r) {
		Vector3f camPos = r.origin;
		for(PathVertice lightVert : lightPath){
			Vector3f dir=StaticVecmath.sub(lightVert.hitRecord.position, camPos);
			float d2=dir.lengthSquared();
			dir.normalize();
			Ray visRay=new Ray(camPos, dir);
			HitRecord hit = root.intersect(visRay);
			int[] imgPos = scene.getCamera().getImagePos(visRay);
			if(hit==null||hit.t<0.001||StaticVecmath.sub(camPos, hit.position).lengthSquared()<d2-0.001)
				continue;
			if(lightVert.hitRecord.material.hasSpecularReflection())
				continue;
			int s = lightVert.k;
			if(s==0)
				continue;
			dir.negate();
			Vector3f lookAt = new Vector3f(scene.getCamera().getLookAt());
			lookAt.normalize();
			float cosCam = Math.abs(lookAt.dot(dir));
			float cosLight = Math.abs(lightVert.hitRecord.normal.dot(dir));
			if(cosCam<=delta||cosLight<=delta)
				continue;
			float p_s = cosLight/d2;
			float p_t=lightVert.hitRecord.material.getPobability(lightVert.hitRecord.w,dir,lightVert.hitRecord.normal);

			float G = cosCam*cosLight/d2;
			p_t=p_t*G/cosLight;

			Spectrum brdf;
			if(s==0)
				brdf=lightVert.hitRecord.material.evaluateEmission(lightVert.hitRecord, dir);
			else
				brdf=lightVert.hitRecord.material.evaluateBRDF(lightVert.hitRecord, lightVert.hitRecord.w, dir);
			dir.negate();

			Spectrum c = new Spectrum(brdf);
			c.mult(G);
			
			c.mult(lightVert.alpha);
			float w=computeWeightOneEye(s,lightPath,p_s, p_t);
			c.mult(w);
			lightImage.addSample(imgPos[0], imgPos[1], c);
		}

	}

	private float computeWeightNoLight(int t, ArrayList<PathVertice> eyePath,
			ArrayList<PathVertice> lightPath) {
		float v_prod;
		float p_t=lightPath.get(0).hitRecord.p;
		if(t==0)
			return 1.f;

		v_prod=p_t/(eyePath.get(t).p_forw);
		
		float v_sum=v_prod;

		for(int i=t;i>1;i--){
			v_prod*=eyePath.get(i).p_backw/(eyePath.get(i-1).p_forw);
			v_sum+=v_prod;
		}
		
		return 1.f/(v_sum);
	}
	
	private float computeWeightOneEye(int s, ArrayList<PathVertice> lightPath, float p_s, float p_t) {
		float u_prod;
		u_prod=p_s/(lightPath.get(s).p_forw);
		
		float u_sum=u_prod;
		
		for(int i=s;i>1;i--){
			u_prod*=lightPath.get(i).p_backw/(lightPath.get(i-1).p_forw);
			u_sum+=u_prod;
		}
		return 1.f/(u_sum+p_t+1);
	}


	private float computeWeight(int s, int t, ArrayList<PathVertice> eyePath,
			ArrayList<PathVertice> lightPath, float p_s, float p_t, float g) {
		float u_prod;
		u_prod=p_s/(lightPath.get(s).p_forw);

		float v_prod;
		v_prod=p_t/eyePath.get(t).p_forw;
		
		float u_sum=u_prod;
		float v_sum=v_prod;
		
		for(int i=s;i>1;i--){
			u_prod*=lightPath.get(i).p_backw/(lightPath.get(i-1).p_forw);
			u_sum+=u_prod;
		}

		for(int i=t;i>1;i--){
			v_prod*=eyePath.get(i).p_backw/(eyePath.get(i-1).p_forw);
			v_sum+=v_prod;
		}
		
		return 1.f/(v_sum+u_sum+1.f);
	}


	private float russionRouletteStop(int k) {
		if(k<2)
			return 0;
		else{
			return (float) Math.random();
		}
	}


	public float[][] makePixelSamples(Sampler sampler, int n) {
		return sampler.makeSamples(n, 2);
	}

}
