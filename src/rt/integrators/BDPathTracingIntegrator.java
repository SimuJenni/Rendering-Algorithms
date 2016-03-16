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
import rt.samplers.JitterSampler;
import rt.samplers.RandomSampler;

public class BDPathTracingIntegrator implements Integrator {
	LightList lightList;
	Intersectable root;
	RandomSampler sampler;
	final float delta = 0.0000001f;
	private BoxFilterFilm lightImage;
	private Scene scene;
	private float sigma_t = 0.0008f, q = 0.5f;
	private JitterSampler jitterSampler;
	private int sampleCount = 0;
	private float[][] lightSamples;

	public BDPathTracingIntegrator(Scene scene, BoxFilterFilm lightImage) {
		this.scene = scene;
		this.lightList = scene.getLightList();
		this.root = scene.getIntersectable();
		this.sampler = new RandomSampler();
		this.jitterSampler = new JitterSampler();
		this.lightImage = lightImage;
	}

	public Spectrum integrate(Ray r) {

		// Init light-path
		float[][] samples = sampler.makeSamples(3, 2);
		HitRecord lightSample = lightList.getRandomLight().sample(
				lightSamples[sampleCount]);
		sampleCount++;
		lightSample.p *= 1.f / lightList.size();
		// lightSample.makeTangentFrame(lightSample.normal);
		ShadingSample sampleLight = lightSample.material.getEmissionSample(
				lightSample, samples[1]);
		Spectrum alpha_init = new Spectrum(1, 1, 1);
		alpha_init.mult(1.f / lightSample.p);
		ArrayList<PathVertice> lightPath = generatePath(lightSample, 0,
				sampleLight.emission, alpha_init, sampleLight, 1, 0);

		computeLightImage(lightPath, r);

		// Init eye-path
		ShadowHandler shadowHandle = new ShadowHandler(r, 0);
		if (shadowHandle.checkShadowed(root)) {
			return new Spectrum();
		}
		HitRecord hit = shadowHandle.shadowHit;

		ShadingSample sample = hit.material.getShadingSample(hit, samples[2]);
		hit.p = shadowHandle.cosIn / shadowHandle.d2;
		Spectrum brdf = rayMarch(r, hit, sample.brdf);
		ArrayList<PathVertice> eyePath = generatePath(hit, 0, brdf,
				new Spectrum(1, 1, 1), sample, shadowHandle.G,
				hit.material.getPobability(sample.w, hit.w, hit.normal)
						/ shadowHandle.d2);

		Spectrum outgoing = new Spectrum(0);
		Spectrum c = new Spectrum(0);
		int tSamp = 0;
		int sSamp = 0;

		for (PathVertice eyeVert : eyePath) {
			int t = eyeVert.k;
			// if(t!=tSamp)
			// continue;
			if (eyeVert.hitRecord.material.hasSpecularReflection())
				continue;

			c = eyeVert.hitRecord.material.evaluateEmission(eyeVert.hitRecord,
					eyeVert.hitRecord.w);
			if (c != null) {
				c.mult(eyeVert.alpha);
				float w = computeWeightNoLight(t, eyePath, lightPath);
				c.mult(w);
				outgoing.add(c);
			}

			for (PathVertice lightVert : lightPath) {
				if (lightVert.hitRecord.material.hasSpecularReflection())
					continue;
				int s = lightVert.k;
				// if(s!=sSamp)
				// continue;
				Vector3f dir = StaticVecmath.sub(lightVert.hitRecord.position,
						eyeVert.hitRecord.position);
				float d2 = dir.lengthSquared();
				dir.normalize();
				shadowHandle = new ShadowHandler(eyeVert.hitRecord, dir, d2);
				if (shadowHandle.checkShadowed(root)) {
					continue;
				}

				float p_s = eyeVert.hitRecord.material.getPobability(
						eyeVert.hitRecord.w, dir, eyeVert.hitRecord.normal);
				dir.negate();
				float p_t = lightVert.hitRecord.material.getPobability(
						lightVert.hitRecord.w, dir, lightVert.hitRecord.normal);

				p_t = p_t / shadowHandle.cosOut * shadowHandle.G;
				p_s = p_s / shadowHandle.cosIn * shadowHandle.G;

				Spectrum brdf_light;
				if (s == 0)
					brdf_light = lightVert.hitRecord.material.evaluateEmission(
							lightVert.hitRecord, dir);
				else
					brdf_light = lightVert.hitRecord.material.evaluateBRDF(
							lightVert.hitRecord, lightVert.hitRecord.w, dir);
				dir.negate();
				Spectrum brdf_eye = eyeVert.hitRecord.material.evaluateBRDF(
						eyeVert.hitRecord, dir, eyeVert.hitRecord.w);

				c = new Spectrum(brdf_eye);
				c.mult(brdf_light);
				c.mult(shadowHandle.G);

				c.mult(eyeVert.alpha);
				c.mult(lightVert.alpha);
				float w = computeWeight(s, t, eyePath, lightPath, p_s, p_t,
						shadowHandle.G);
				assert (!Float.isNaN(w));

				c.mult(w);
				outgoing.add(new Spectrum(c));
			}
		}
		return outgoing;

	}

	private Spectrum rayMarch(Ray r, HitRecord hit, Spectrum brdf) {
		 Spectrum T = new Spectrum(1);
		 Spectrum L = new Spectrum();
		 Spectrum Lve = new Spectrum(0.00012f, 0.00012f, 0.00026f);
		
		 int numSteps = 20;
		 Vector3f dir = StaticVecmath.sub(hit.position, r.origin);
		 float dist = dir.length();
		 float ds = dist/numSteps;
		 for(int i=0;i<numSteps;i++){
		 Vector3f shadowStart = new Vector3f(dir);
		 shadowStart.scaleAdd((i+1)*ds/dist, r.origin);
		 Spectrum inScatter = inScatter(shadowStart);
		 Spectrum TmulLve = new Spectrum(Lve);
		 TmulLve.add(inScatter);
		 TmulLve.mult(T);
		 L.add(TmulLve);
		 T.mult(1-sigma_t*ds);
		 }
		 L.mult(ds);
		 T.mult(brdf);
		 L.add(T);
		 return L;
	}

	private Spectrum inScatter(Vector3f shadowStart) {
		Spectrum T = new Spectrum(1);
		int numSamples = 1;
		float sigma_s = 1.f;
		float[][] samples = sampler.makeSamples(3, 2);
		HitRecord lightSample = lightList.getRandomLight().sample(samples[0]);
		Vector3f dir = StaticVecmath.sub(shadowStart, lightSample.position);
		float dist = dir.length();
		float dt = dist / numSamples;
		dir.normalize();
		Ray visRay = new Ray(shadowStart, dir);
		HitRecord shadowHit = root.intersect(visRay);
		if (shadowHit == null
				|| shadowHit.t < 0.0001
				|| StaticVecmath.sub(shadowStart, shadowHit.position).length()
						/ dist < 0.99)
			return new Spectrum();

		for (int i = 0; i < numSamples; i++) {
			T.mult(1 - sigma_t * dt);
		}
		Spectrum Ld = new Spectrum(lightSample.material.evaluateEmission(
				lightSample, dir));
		Ld.mult(Math.max(dir.dot(lightSample.normal), 0));
		Ld.mult(1 / (dist * dist));
		Ld.mult(T);
		Ld.mult((float) (sigma_s / Math.PI));
		return Ld;
	}

	private ArrayList<PathVertice> generatePath(HitRecord hitRecord, int k,
			Spectrum brdf, Spectrum alpha, ShadingSample sample, float G,
			float p_backw) {
		ArrayList<PathVertice> path = new ArrayList<PathVertice>();
		float p_forw = hitRecord.p, p = 1;
		path.add(new PathVertice(hitRecord, new Spectrum(alpha), G, p_forw,
				p_backw, k));

		while (russionRouletteStop(k) < 1 - q) {
			ShadowHandler shadowHandle = new ShadowHandler(hitRecord,new Vector3f(sample.w),0);
			if(shadowHandle.checkShadowed(root)){
				break;
			}
			p = sample.p;
			k++;

			boolean spec = hitRecord.material.hasSpecularRefraction();
			hitRecord = shadowHandle.shadowHit;
			float[][] samples = sampler.makeSamples(1, 2);
			sample = hitRecord.material.getShadingSample(hitRecord, samples[0]);
			if (shadowHandle.d2 <= 1e-8)
				break;
			if(spec)
				p_forw=p;
			else
			p_forw = p / shadowHandle.cosOut * shadowHandle.G;
			if(hitRecord.material.hasSpecularRefraction())
				p_backw=hitRecord.material.getPobability(sample.w,hitRecord.w,hitRecord.normal);
			else
			p_backw = hitRecord.material.getPobability(sample.w, hitRecord.w,
					hitRecord.normal) / shadowHandle.cosIn * shadowHandle.G;
			alpha.mult(brdf);

			if (k > 1)
				alpha.mult(shadowHandle.cosOut / (p * (1 - q)));
			else 
				alpha.mult(shadowHandle.cosOut / p);
			brdf = sample.brdf;
			path.add(new PathVertice(hitRecord, new Spectrum(alpha), shadowHandle.G, p_forw,
					p_backw, k));

		}

		return path;
	}

	private void computeLightImage(ArrayList<PathVertice> lightPath, Ray r) {
		Vector3f camPos = r.origin;
		for (PathVertice lightVert : lightPath) {
			int s = lightVert.k;
			if (s == 0)
				continue;
			if (lightVert.hitRecord.material.hasSpecularReflection())
				continue;

			Vector3f dir = StaticVecmath.sub(lightVert.hitRecord.position,
					camPos);
			float d2 = dir.lengthSquared();
			Ray visRay = new Ray(camPos, dir);
			ShadowHandler shadowHandle = new ShadowHandler(visRay, d2);
			if (shadowHandle.checkShadowed(root)) {
				continue;
			}

			float p_s = shadowHandle.cosIn / shadowHandle.d2;
			float p_t = lightVert.hitRecord.material.getPobability(
					lightVert.hitRecord.w, dir, lightVert.hitRecord.normal)
					/ d2;

			Spectrum brdf = lightVert.hitRecord.material.evaluateBRDF(
					lightVert.hitRecord, lightVert.hitRecord.w, dir);
			// brdf = rayMarch(visRay, hit, brdf);

			Spectrum c = new Spectrum(brdf);
			c.mult(shadowHandle.G);
			c.mult(lightVert.alpha);
			float w = computeWeightOneEye(s, lightPath, p_s, p_t);
			c.mult(w);
			int[] imgPos = scene.getCamera().getImagePos(visRay);
			lightImage.addSample(imgPos[0], imgPos[1], c);
		}

	}

	private float computeWeightNoLight(int t, ArrayList<PathVertice> eyePath,
			ArrayList<PathVertice> lightPath) {
		float p_t = lightPath.get(0).hitRecord.p;
		if (t == 0)
			return 1.f;
		float v_sum = computePsum(t, eyePath, p_t);

		return 1.f / (v_sum + 1);
	}

	private float computeWeightOneEye(int s, ArrayList<PathVertice> lightPath,
			float p_s, float p_t) {
		float u_sum = computePsum(s, lightPath, p_s);
		return 1.f / (u_sum + p_t + 1);
	}

	private float computeWeight(int s, int t, ArrayList<PathVertice> eyePath,
			ArrayList<PathVertice> lightPath, float p_s, float p_t, float g) {

		float u_sum = computePsum(s, lightPath, p_s);
		float v_sum = computePsum(t, eyePath, p_t);

		return 1.f / (v_sum + u_sum + 1.f);
	}

	private float computePsum(int k, ArrayList<PathVertice> path, float p) {
		float prod;
		prod = p / (path.get(k).p_forw);

		float sum = prod;

		for (int i = k; i > 1; i--) {
			prod *= path.get(i).p_backw / (path.get(i - 1).p_forw);
			sum += prod;
		}

		return sum;
	}

	private float russionRouletteStop(int k) {
		if (k < 1)
			return 0;
		else {
			return (float) Math.random();
		}
	}

	public float[][] makePixelSamples(Sampler sampler, int n) {
		sampleCount = 0;
		lightSamples = jitterSampler.makeSamples(n, 2);
		return jitterSampler.makeSamples(n, 2);
	}

}
