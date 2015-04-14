package rt.intersectables;

import javax.vecmath.*;

import rt.HitRecord;
import rt.Intersectable;
import rt.Ray;
import rt.accelerator.BoundingBox;

/**
 * Defines a triangle by referring back to a {@link Mesh}
 * and its vertex and index arrays. 
 */
public class MeshTriangle implements Intersectable {

	private Mesh mesh;
	private int index, v0,v1,v2;
	private float x0,x1,x2,y0,y1,y2,z0,z1,z2;
	float vertices[];
	
	/**
	 * Make a triangle.
	 * 
	 * @param mesh the mesh storing the vertex and index arrays
	 * @param index the index of the triangle in the mesh
	 */
	public MeshTriangle(Mesh mesh, int index)
	{
		this.mesh = mesh;
		this.index = index;	
		vertices = mesh.vertices;
		
		// Access the triangle vertices as follows (same for the normals):		
		// 1. Get three vertex indices for triangle
		v0 = mesh.indices[index*3];
		v1 = mesh.indices[index*3+1];
		v2 = mesh.indices[index*3+2];
		
		// 2. Access x,y,z coordinates for each vertex
		x0 = vertices[v0*3];
		x1 = vertices[v1*3];
		x2 = vertices[v2*3];
		y0 = vertices[v0*3+1];
		y1 = vertices[v1*3+1];
		y2 = vertices[v2*3+1];
		z0 = vertices[v0*3+2];
		z1 = vertices[v1*3+2];
		z2 = vertices[v2*3+2];
	}
	
	public HitRecord intersect(Ray r)
	{				
		Vector3f vert1=new Vector3f(x0,y0,z0);
		Vector3f vert2=new Vector3f(x1,y1,z1);
		Vector3f vert3=new Vector3f(x2,y2,z2);
		
		Vector3f a=new Vector3f(vert1);
		a.sub(vert2);
		Vector3f b=new Vector3f(vert1);
		b.sub(vert3);
		Vector3f c=new Vector3f(r.direction);
		Vector3f d=new Vector3f(vert1);
		d.sub(r.origin);
		Matrix3f m=new Matrix3f();
		m.setColumn(0, a);
		m.setColumn(1, b);
		m.setColumn(2, c);
		float detM=m.determinant();
		
		Matrix3f tmp=new Matrix3f(m);
		tmp.setColumn(0, d);
		float beta=tmp.determinant()/detM;
		
		tmp=new Matrix3f(m);
		tmp.setColumn(1, d);
		float gamma=tmp.determinant()/detM;
		
		tmp=new Matrix3f(m);
		tmp.setColumn(2, d);
		float t=tmp.determinant()/detM;

		if(beta+gamma>1||beta+gamma<0||beta>1||gamma>1||beta<0||gamma<0)
			return null;
		else{
			// Hit position
			Vector3f position = new Vector3f(r.direction);
			position.scaleAdd(t, r.origin);
			
			// Hit normal
			float alpha=1-beta-gamma;
			float normals[] = mesh.normals;
			Vector3f n1=new Vector3f(normals[v0*3],normals[v0*3+1],normals[v0*3+2]);
			Vector3f n2=new Vector3f(normals[v1*3],normals[v1*3+1],normals[v1*3+2]);
			Vector3f n3=new Vector3f(normals[v2*3],normals[v2*3+1],normals[v2*3+2]);
			n1.scale(alpha);
			n2.scale(beta);
			n3.scale(gamma);


			Vector3f retNormal = new Vector3f(n1);
			retNormal.add(n2);
			retNormal.add(n3);
			retNormal.normalize();
			
			// Incident direction, convention is that it points away from the hit position
			Vector3f wIn = new Vector3f(r.direction);
			wIn.negate();
			
			HitRecord hit=new HitRecord(t, position, retNormal, wIn, null, mesh.material, 0.f, 0.f);
			return hit;
		}	
	}

	@Override
	public BoundingBox getBoundingBox() {
		float xmin=Math.min(Math.min(x0, x1), x2);
		float ymin=Math.min(Math.min(y0, y1), y2);
		float zmin=Math.min(Math.min(z0, z1), z2);
		float xmax=Math.max(Math.max(x0, x1), x2);
		float ymax=Math.max(Math.max(y0, y1), y2);
		float zmax=Math.max(Math.max(z0, z1), z2);
		return new BoundingBox(xmin,ymin,zmin,xmax,ymax,zmax);
	}
	
}
