package rt.accelerator;

import java.util.Iterator;
import java.util.Stack;

import javax.vecmath.Vector3f;

import rt.HitRecord;
import rt.Intersectable;
import rt.Ray;
import rt.intersectables.Aggregate;
import rt.intersectables.IntersectableList;

public class BSPAccelerator implements Intersectable {
	private Aggregate a;
	private int MAX_HEIGHT;
	private final int MIN_PRIM=5;
	private BSPNode root;

	public BSPAccelerator(Aggregate a) {
		super();
		this.a = a;
		int height=0;
		MAX_HEIGHT=(int) (8+1.3*Math.log(a.numElements()));
		root=buildTree(a.getObjects(), a.getBoundingBox(), height);
	}

	@Override
	public HitRecord intersect(Ray r) {
		Vector3f d = r.direction;
		Vector3f e = r.origin;
		Stack<BSPStackItem> stack=new Stack<BSPStackItem>();
		HitRecord hit=null;
		BSPNode node,first,second;
		node=root;
		BSPStackItem s=node.getBox().intersectBox(r, node);
		if(s==null)
			return null;
		float isect=Float.MAX_VALUE;
		float tmin=s.tmin;
		float tmax=s.tmax;
		while(node!=null){
			if(isect<tmin)
				break;
			if(!node.isLeaf()){
				// Process interior node
				float tsplit=node.splitPlane.intersect(r);
				// Order children
				if(node.isBelow(e)){
					first=node.left;
					second=node.right;
				} else{
					first=node.right;
					second=node.left;
				}
				// Process children
				if(tsplit>tmax||tsplit<0||(tsplit==0&&node.splitPlane.pointsBelow(d))){
					node=first;
				} else
				if(tsplit<tmin||(tsplit==0&&!node.splitPlane.pointsAbove(d))){
					node=second;
				} else{
					node=first;
					BSPStackItem item=new BSPStackItem(second, tsplit, tmax); 
					stack.push(item);
					tmax=tsplit;
				}
			}
			else{
				HitRecord tmp = node.intersect(r);
				if(tmp!=null&&tmp.t<isect&&tmp.t>0){
					isect=tmp.t;
					hit=tmp;
				}
				if(stack.isEmpty())
					node=null;
				else{
					s=stack.pop();
					node=s.node;
					tmin=s.tmin;
					tmax=s.tmax;
				}
			}
		}
		return hit;
	}
	
	public BSPNode buildTree(IntersectableList intersectables, BoundingBox box, int height ){
		if(checkTermination(intersectables, height)){
			BSPNode leaf=new BSPNode(intersectables, box);
			return leaf;
		}
		SplitPlane s=findSplit(box);
		BSPNode node=new BSPNode(s, intersectables, box);
		BoundingBox boxAbove=box.boxAbove(s);
		BoundingBox boxBelow=box.boxBelow(s);
		IntersectableList objectsAbove=getObjectsAbove(s,intersectables);
		IntersectableList objectsBelow=getObjectsBelow(s,intersectables);
		node.right=buildTree(objectsAbove, boxAbove, height+1);
		node.left=buildTree(objectsBelow,boxBelow, height+1);
		return node;
	}

	private IntersectableList getObjectsBelow(SplitPlane s,
			IntersectableList intersectables) {
		IntersectableList objects=new IntersectableList();
		Iterator<Intersectable> it = intersectables.iterator();
		while(it.hasNext()){
			Intersectable obj=it.next();
			if(s.isBelow(obj.getBoundingBox()))
				objects.add(obj);
		}
		return objects;
	}

	private IntersectableList getObjectsAbove(SplitPlane s,
			IntersectableList intersectables) {
		IntersectableList objects=new IntersectableList();
		Iterator<Intersectable> it = intersectables.iterator();
		while(it.hasNext()){
			Intersectable obj=it.next();
			if(s.isAbove(obj.getBoundingBox()))
				objects.add(obj);
		}
		return objects;
	}

	private SplitPlane findSplit(BoundingBox box) {
		SplitPlane plane=null;
		float[] dim=box.getDimensions();
		int max=0;
		
		if(dim[0]>dim[1])
			max=0;
		else
			max=1;
		if(dim[2]>dim[max])
			max=2;
		
		switch(max){
			case 0:
				plane = new SplitPlane(Axis.X, box.x1+dim[0]/2);
				break;
			case 1:
				plane = new SplitPlane(Axis.Y, box.y1+dim[1]/2);
				break;
			case 2:
				plane = new SplitPlane(Axis.Z, box.z1+dim[2]/2);
				break;
		}
		
		return plane;
	}
	

	private boolean checkTermination(IntersectableList intersectables, int height) {
		return height>=this.MAX_HEIGHT||intersectables.numElements()<=this.MIN_PRIM;
	}
	
	
	public enum Axis {
	    X, Y, Z 
	}

	public class SplitPlane {
		@Override
		public String toString() {
			return "[axis=" + axis + ", distance=" + distance + "]";
		}

		Axis axis;
		float distance;
		
		public SplitPlane(Axis axis, float distance) {
			super();
			this.axis = axis;
			this.distance = distance;
		}
		
		public boolean pointsAbove(Vector3f d) {
			Vector3f dneg = new Vector3f(d);
			dneg.negate();
			return pointsBelow(dneg);
		}

		public boolean pointsBelow(Vector3f d) {
			boolean result=false;
			switch(axis){
			case X:
				result = d.x<0;
				break;
			case Y:
				result = d.y<0;
				break;
			case Z:
				result = d.z<0;
				break;
			}
			return result;
		}

		public float intersect(Ray r) {
			Vector3f d = r.direction;
			Vector3f e = r.origin;
			float t=Float.MAX_VALUE;
			switch(axis){
			case X:
				t=(distance-e.x)/d.x;
				break;
			case Y:
				t=(distance-e.y)/d.y;
				break;
			case Z:
				t=(distance-e.z)/d.z;
				break;
			}
			return t;
		}

		public boolean isAbove(BoundingBox box) {
			boolean isAbove=false;
			switch(axis){
				case X:
					isAbove = box.x2>=distance;
					break;
				case Y:
					isAbove = box.y2>=distance;
					break;
				case Z:
					isAbove = box.z2>=distance;
					break;
			}
			return isAbove;
		}

		public boolean isBelow(BoundingBox box) {
			boolean isBelow=false;
			switch(axis){
				case X:
					isBelow = box.x1<distance;
					break;
				case Y:
					isBelow = box.y1<distance;
					break;
				case Z:
					isBelow = box.z1<distance;
					break;
			}
			return isBelow;
		}
	}

	@Override
	public BoundingBox getBoundingBox() {
		return a.getBoundingBox();
	}

}
