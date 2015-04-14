package rt.materials;

import javax.vecmath.Vector3f;

import rt.HitRecord;
import rt.Material;
import rt.Spectrum;

public class XYZGrid extends Diffuse implements Material {
	
	private Spectrum tile,filling;
	private float thickness, tileSize;
	private Vector3f offset;

	public XYZGrid(Spectrum tile, Spectrum filling, float thickness,
			Vector3f offset) {
		this(tile,filling,thickness,offset,1f);

	}

	public XYZGrid(Spectrum tile, Spectrum filling, float thickness,
			Vector3f offset, float tileSize) {
		this.tile=tile;
		this.filling=filling;
		this.thickness=thickness;
		this.offset=offset;
		this.tileSize=tileSize;
	}

	@Override
	public Spectrum evaluateBRDF(HitRecord hitRecord, Vector3f wOut,
			Vector3f wIn) {
		Spectrum brdf=super.evaluateBRDF(hitRecord, wOut, wIn);
		Vector3f hit = new Vector3f(hitRecord.position);
		hit.add(offset);
		Vector3f dist=new Vector3f(hit.x%tileSize,hit.y%tileSize,hit.z%tileSize);
		dist.absolute();
		if (dist.x<thickness||dist.y<thickness||dist.z<thickness
				||dist.x>tileSize-thickness||dist.y>tileSize-thickness||dist.z>tileSize-thickness){
			brdf.mult(tile);
			return brdf;
		}
		else{
			brdf.mult(filling);
			return brdf;
		}
	}

}
