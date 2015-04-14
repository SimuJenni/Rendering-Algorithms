package rt.intersectables;

import java.util.ArrayList;

import javax.vecmath.Vector3f;

import rt.Material;
import rt.Ray;
import rt.intersectables.CSGNode.OperationType;

public class CSGUnitCylinder extends CSGSolid {

	private CSGNode root;
	
	public CSGUnitCylinder(Material material){
		CSGCylinder cylinder = new CSGCylinder(material);
		cylinder.material = material;
		
		CSGPlane upperPlane = new CSGPlane(new Vector3f(0,0,1),-1.f, material);
		CSGPlane lowerPlane = new CSGPlane(new Vector3f(0,0,-1),0, material);
		
		CSGNode node = new CSGNode(cylinder, upperPlane, CSGNode.OperationType.INTERSECT);
		root = new CSGNode(node,lowerPlane,CSGNode.OperationType.INTERSECT);
	}
	
	@Override
	ArrayList<IntervalBoundary> getIntervalBoundaries(Ray r) {
		return root.getIntervalBoundaries(r);
	}

}
