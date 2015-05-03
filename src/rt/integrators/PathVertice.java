package rt.integrators;

import rt.HitRecord;
import rt.Material.ShadingSample;
import rt.Spectrum;

public class PathVertice {
	public Spectrum alpha;
	public HitRecord hitRecord; 
	public float G, p_forw, p_backw;
	public int k;

	public PathVertice(HitRecord hitRecord, Spectrum alpha, float G, float p_forw, float p_backw, int k) {
		this.hitRecord=hitRecord;
		this.alpha=alpha;
		this.p_forw=p_forw;
		this.p_backw=p_backw;
		this.k=k;
		this.G=G;
	}

}
