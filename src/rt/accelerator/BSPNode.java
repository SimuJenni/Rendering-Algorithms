package rt.accelerator;

import java.util.ArrayList;
import java.util.Iterator;

import javax.vecmath.Vector3f;

import rt.HitRecord;
import rt.Intersectable;
import rt.Ray;
import rt.accelerator.BSPAccelerator.Axis;
import rt.accelerator.BSPAccelerator.SplitPlane;
import rt.intersectables.IntersectableList;

public class BSPNode {
	private float plane_pos;  // split plane position
	private Axis axis;       // partitioning axis (x,y, or z)
	public BSPNode left, right;
	private boolean isLeaf;
	private IntersectableList intersectables;
	private BoundingBox box;
	public SplitPlane splitPlane;
	
	public BSPNode(SplitPlane s, IntersectableList intersectables, BoundingBox box) {
		super();
		this.plane_pos = s.distance;
		this.axis = s.axis;
		this.intersectables = intersectables;
		this.isLeaf=false;
		this.box=box;
		this.splitPlane=s;
	}
	
	public BSPNode(IntersectableList intersectables, BoundingBox box) {
		super();
		this.intersectables = intersectables;
		this.isLeaf=true;
		this.box=box;
	}

	public HitRecord intersect(Ray r) {
		if(this.isLeaf){
			HitRecord hitRecord = null;
			float t = Float.MAX_VALUE;
			
			// Intersect all objects in group, return closest hit
			Iterator<Intersectable> it = this.intersectables.iterator();
			while(it.hasNext())
			{
				Intersectable o = it.next();
				HitRecord tmp = o.intersect(r);
				if(tmp!=null && tmp.t<t)
				{
					t = tmp.t;
					hitRecord = tmp;
				}
			}
			return hitRecord;
		}
		else{
			HitRecord hit1,hit2;
			hit1=this.left.intersect(r);
			hit2=this.right.intersect(r);
			if(hit2==null||hit1!=null&&hit1.t<hit2.t)
				return hit1;
			else
				return hit2;
		}
	}

	public BoundingBox getBox() {
		if(this.box==null){
			box=intersectables.getBoundingBox();
			return box;
		}
		else
			return box;
	}

	public boolean isLeaf() {
		return this.isLeaf;
	}

	public boolean isBelow(Vector3f e) {
		boolean result=false;
		switch(axis){
		case X:
			result = e.x<=this.plane_pos;
			break;
		case Y:
			result = e.y<=this.plane_pos;
			break;
		case Z:
			result = e.z<=this.plane_pos;
			break;
		}
		return result;
	}

}
