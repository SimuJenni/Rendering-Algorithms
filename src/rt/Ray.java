package rt;

import javax.vecmath.*;

/**
 * A ray represented by an origin and a direction.
 */
public class Ray {

	public Vector3f origin;
	public Vector3f direction;
	public int recursionDepth;
	
	public Ray(Vector3f origin, Vector3f direction)
	{
		this.origin = new Vector3f(origin); 
		this.direction = new Vector3f(direction);
		this.recursionDepth=5;
		shiftOriginInDir();
	}

	private void shiftOriginInDir() {
		Vector3f shiftedOrig=new Vector3f(this.direction);
		shiftedOrig.normalize();
		shiftedOrig.scaleAdd( 0.00001f, origin);
		this.origin=shiftedOrig;
	}
}
