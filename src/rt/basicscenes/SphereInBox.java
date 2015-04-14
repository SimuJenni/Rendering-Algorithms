package rt.basicscenes;

import java.io.IOException;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;

import rt.*;
import rt.accelerator.BSPAccelerator;
import rt.cameras.*;
import rt.films.*;
import rt.integrators.*;
import rt.intersectables.*;
import rt.lightsources.*;
import rt.materials.Blinn;
import rt.materials.Diffuse;
import rt.materials.Reflective;
import rt.materials.Refractive;
import rt.samplers.*;
import rt.tonemappers.*;

public class SphereInBox extends Scene {

	public SphereInBox()
	{
		
		// List of objects
		IntersectableList objects = new IntersectableList();	
		// Output file name
		outputFilename = new String("../output/basicscenes/Box");
		
		// Image width and height in pixels
		width = 512;
		height = 512;
		
		// Number of samples per pixel
		SPP = 5;
		
		// Specify which camera, film, and tonemapper to use
		camera = new FixedCamera(width, height);
		
		Vector3f eye = new Vector3f(0.f, 0.f, 3.0f);
		Vector3f lookAt = new Vector3f(0.f, 0.f, 0.f);
		Vector3f up = new Vector3f(0.f, 1.f, 0.f);
		camera =new PinholeCamera(eye, lookAt, up, 80, 1, width, height);
		
		film = new BoxFilterFilm(width, height);
		tonemapper = new ClampTonemapper();
		
		// Specify which integrator and sampler to use
		integratorFactory = new PointLightIntegratorFactory();
		integratorFactory = new WhittedIntegratorFactory();

//		samplerFactory = new OneSamplerFactory();
		samplerFactory = new RandomSamplerFactory();

		
		// Define the root object (an intersectable) of the scene
		// A box
		CSGPlane p1 = new CSGPlane(new Vector3f(1.f, 0.f, 0.f), 1.f);
		CSGPlane p2 = new CSGPlane(new Vector3f(-1.f, 0.f, 0.f), 1.f);
		CSGPlane p3 = new CSGPlane(new Vector3f(0.f, 1.f, 0.f), 1.f);
		CSGPlane p4 = new CSGPlane(new Vector3f(0.f, -1.f, 0.f), 1.f);
		CSGPlane p5 = new CSGPlane(new Vector3f(0.f, 0.f, 1.f), 1.f);
		CSGSphere sphere1 = new CSGSphere(new Vector3f(0.f, 0.f, 0.f), 0.8f);
		sphere1.material=new Blinn(new Spectrum(0.1f, 0.1f, 0.1f), new Spectrum(.4f, .4f, .4f), 50.f);

		CSGCylinder cylinder = new CSGCylinder(new Vector3f(0.f, 1.f, 0.f), 0.4f);
		CSGTwoSidedInfiniteCone cone = new CSGTwoSidedInfiniteCone(new Vector3f(0.f, 1.f, 0.f), 26.f);
//		cone.material=new Blinn(new Spectrum(0f, 0.f, 0.1f), new Spectrum(.6f, .6f, .6f), 80.f);
		cone.material=new Reflective(new Spectrum(0.1f, 0.1f, .1f));
		cylinder.material=new Reflective();
		
		CSGTwoSidedInfiniteCone cone2 = new CSGTwoSidedInfiniteCone(new Vector3f(0.f, 1.f, 0.f), 30.f);
		CSGNode tmpCone = new CSGNode(cone2, new CSGPlane(new Vector3f(0.f, -1.f, 0.f), 0.f), CSGNode.OperationType.INTERSECT);
		CSGNode halfCone = new CSGNode(tmpCone, sphere1, CSGNode.OperationType.INTERSECT);

		Matrix4f Mcone=new Matrix4f();
		Mcone.setIdentity();
		Mcone.m03=0.5f;
		Mcone.m13=-1.3f;
		Mcone.m23=1.5f;
		CSGInstance movedCone = new CSGInstance(halfCone,Mcone);

		CSGSphere sphere = new CSGSphere(new Vector3f(0.f, 0.f, 0.f), 0.8f);
		sphere.material=new Refractive(1.1f);
		CSGNode tmpfig1 = new CSGNode(sphere, cone, CSGNode.OperationType.INTERSECT);
		CSGNode fig = new CSGNode(tmpfig1, cylinder, CSGNode.OperationType.INTERSECT);
		fig = new CSGNode(fig, new CSGPlane(new Vector3f(1.f, 0.f, 0.f), 0.f), CSGNode.OperationType.INTERSECT);
		
		Matrix4f M=new Matrix4f();
		M.setIdentity();
		M.setScale(0.6f);
		M.m00=0.4f;
		M.m03=-0.5f;
		M.m23=1.6f;
		CSGInstance inst=new CSGInstance(sphere,M);
		
		CSGNode n1 = new CSGNode(p1, p2, CSGNode.OperationType.ADD);
		CSGNode n2 = new CSGNode(p3, p4, CSGNode.OperationType.ADD);
		CSGNode n3 = new CSGNode(n2, p5, CSGNode.OperationType.ADD);
		CSGNode n4 = new CSGNode(n3, n1, CSGNode.OperationType.ADD);
		CSGNode n5 = new CSGNode(inst, n4, CSGNode.OperationType.ADD); 
		CSGNode n6 = new CSGNode(n5, cylinder, CSGNode.OperationType.ADD);
		CSGNode n7 = new CSGNode(n6, cone, CSGNode.OperationType.ADD);
		objects.add(new CSGNode(n7, movedCone, CSGNode.OperationType.ADD));
		// Add objects
		Mesh mesh;
		try	{
		mesh = ObjReader.read("../obj/teapot.obj", 1.f);
		} catch(IOException e) 
		{
			System.out.printf("Could not read .obj file\n");
			return;
		}
		Matrix4f t = new Matrix4f();
		t.setIdentity();
		
		// Instance one
		t.setScale(0.3f);
		t.setTranslation(new Vector3f(0.45f, -0.4f, 1.65f));
		Matrix4f rot = new Matrix4f();
		rot.setIdentity();
		rot.rotY((float)Math.toRadians(180.f));
		t.mul(rot);
//		mesh.material=new Diffuse(new Spectrum(0.1f,0.4f,0.2f));
		mesh.material=new Blinn(new Spectrum(0.1f,0.3f,0.2f), new Spectrum(.1f, .1f, .1f), 50.f);

		Instance instance = new Instance(mesh, t);
		objects.add(instance);	
		
		root=objects;
		root=new BSPAccelerator(objects);
		
		// Light sources
		LightGeometry pointLight = new PointLight(new Vector3f(-.8f, 0.8f, 3f), new Spectrum(3.f, 4.f, 6.f));
		LightGeometry pointLight2 = new PointLight(new Vector3f(0.8f, -0.5f, 2.8f), new Spectrum(7.f, 4.f, 3.f));

		lightList = new LightList();
		lightList.add(pointLight);
		lightList.add(pointLight2);

	}
}
