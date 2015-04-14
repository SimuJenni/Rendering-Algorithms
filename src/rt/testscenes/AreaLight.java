package rt.testscenes;

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
import rt.materials.Glossy;
import rt.materials.Reflective;
import rt.materials.Refractive;
import rt.samplers.*;
import rt.tonemappers.*;

public class AreaLight extends Scene {

	public AreaLight()
	{
		
		// List of objects
		IntersectableList objects = new IntersectableList();	
		// Output file name
		outputFilename = new String("../output/testscenes/AreaLight");
		
		// Image width and height in pixels
		width = 256;
		height = 256;
		
		// Number of samples per pixel
		SPP = 256;
		
		// Specify which camera, film, and tonemapper to use
		camera = new FixedCamera(width, height);
		
		Vector3f eye = new Vector3f(0.f, 0.f, 3.0f);
		Vector3f lookAt = new Vector3f(0.f, 0.f, 0.f);
		Vector3f up = new Vector3f(0.f, 1.f, 0.f);
		camera =new PinholeCamera(eye, lookAt, up, 80, 1, width, height);
		
		film = new BoxFilterFilm(width, height);
		tonemapper = new ClampTonemapper();
		
		// Specify which integrator and sampler to use
		integratorFactory = new AreaLightIntegratorFactory();
//		integratorFactory = new DebugIntegratorFactory();


//		samplerFactory = new OneSamplerFactory();
		samplerFactory = new RandomSamplerFactory();

		
		// Define the root object (an intersectable) of the scene
		// A box
		CSGPlane p1 = new CSGPlane(new Vector3f(1.2f, 0.f, 0.f), 1.f);
		CSGPlane p2 = new CSGPlane(new Vector3f(-1.2f, 0.f, 0.f), 1.f);
		CSGPlane p3 = new CSGPlane(new Vector3f(0.f, 1.f, 0.f), 1.f);
		CSGPlane p4 = new CSGPlane(new Vector3f(0.f, -1.f, 0.f), 1.f);
		CSGPlane p5 = new CSGPlane(new Vector3f(0.f, 0.f, 1.f), 1.f);
		CSGCube cube = new CSGCube(); 
		CSGSphere sphere1 = new CSGSphere(new Vector3f(0.f, -0.4f, 1f), 0.4f);
		
//		sphere1.material=new Glossy(5,new Spectrum(1,1,1), new Spectrum(1,1,1));
		
		CSGNode n1 = new CSGNode(p1, p2, CSGNode.OperationType.ADD);
		CSGNode n2 = new CSGNode(p3, p4, CSGNode.OperationType.ADD);
		CSGNode n3 = new CSGNode(n2, p5, CSGNode.OperationType.ADD);
		CSGNode n4 = new CSGNode(n3, n1, CSGNode.OperationType.ADD);
		CSGNode n5 = new CSGNode(sphere1, n4, CSGNode.OperationType.ADD); 
		objects.add(n5);
		root=new BSPAccelerator(objects);
		root=objects;
		// Light source
		Vector3f bottomLeft = new Vector3f(-0.7f, 0.9f, 0.9f);
		Vector3f right = new Vector3f(.25f, 0.f, 0.f);
		Vector3f top = new Vector3f(0f, 0.f, .25f);

		RectangleLight rectangleLight1 = new RectangleLight(bottomLeft, right, top, new Spectrum(35.f, 50f, 70f));
		
		objects.add(rectangleLight1);

		lightList = new LightList();
		lightList.add(rectangleLight1);
		
		bottomLeft = new Vector3f(0.5f, 0.9f, 1.5f);
		right = new Vector3f(.3f, 0.f, 0.f);
		top = new Vector3f(0f, 0.f, .3f);

		RectangleLight rectangleLight2 = new RectangleLight(bottomLeft, right, top, new Spectrum(35f, 65f, 50f));
		objects.add(rectangleLight2);
		lightList.add(rectangleLight2);
	}
}
