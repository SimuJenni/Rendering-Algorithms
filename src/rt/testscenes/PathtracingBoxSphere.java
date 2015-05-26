package rt.testscenes;

import javax.vecmath.*;

import rt.*;
import rt.intersectables.*;
import rt.tonemappers.*;
import rt.integrators.*;
import rt.lightsources.*;
import rt.materials.*;
import rt.samplers.*;
import rt.cameras.*;
import rt.films.*;

public class PathtracingBoxSphere extends Scene {
	
	public PathtracingBoxSphere()
	{	
		outputFilename = new String("../output/testscenes/PathtracingBoxSphere");
				
		// Specify pixel sampler to be used
		samplerFactory = new RandomSamplerFactory();
		
		// Samples per pixel
		SPP = 256;
		outputFilename = outputFilename + " " + String.format("%d", SPP) + "SPP";
		
		// Make camera and film
		Vector3f eye = new Vector3f(-3.f,1.f,4.f);
		Vector3f lookAt = new Vector3f(0.f,1.f,0.f);
		Vector3f up = new Vector3f(0.f,1.f,0.f);
		float fov = 60.f;
		int width = 300;
		int height = 300;
		float aspect = (float)width/(float)height;
		camera = new PinholeCamera(eye, lookAt, up, fov, aspect, width, height);
		film = new BoxFilterFilm(width, height);						
		tonemapper = new ClampTonemapper();
		
		// Specify integrator to be used
		integratorFactory = new BDPathTracingIntegratorFactory(this);
		
		// List of objects
		IntersectableList objects = new IntersectableList();	
		
		Sphere sphere = new Sphere(new Vector3f(-.3f,-.1f,1.f), .5f);
		sphere.material = new Diffuse(new Spectrum(1.f, 1.f, 1.f));
		sphere.material = new Refractive(1.1f);
//		sphere.material = new Reflective();

		objects.add(sphere);

		// Right, red wall
		Rectangle rectangle = new Rectangle(new Vector3f(2.f, -.75f, 2.f), new Vector3f(0.f, 4.f, 0.f), new Vector3f(0.f, 0.f, -4.f));
		rectangle.material = new Diffuse(new Spectrum(0.8f, 0.f, 0.f));
		objects.add(rectangle);
	
		// Bottom
		rectangle = new Rectangle(new Vector3f(-2.f, -.75f, 2.f), new Vector3f(4.f, 0.f, 0.f), new Vector3f(0.f, 0.f, -4.f));
		rectangle.material = new Diffuse(new Spectrum(0.8f, 0.8f, 0.8f));
		objects.add(rectangle);

		// Top
		rectangle = new Rectangle(new Vector3f(-2.f, 3.25f, 2.f), new Vector3f(0.f, 0.f, -4.f), new Vector3f(4.f, 0.f, 0.f));
		rectangle.material = new Diffuse(new Spectrum(0.8f, 0.8f, 0.8f));
		objects.add(rectangle);
		
		// Left
		rectangle = new Rectangle(new Vector3f(-2.f, -.75f, -2.f), new Vector3f(4.f, 0.f, 0.f), new Vector3f(0.f, 4.f, 0.f));
		rectangle.material = new Diffuse(new Spectrum(0.f, 0.8f, 0.f));
		objects.add(rectangle);
		
		// Light source
		Vector3f bottomLeft = new Vector3f(.8f, 3.f, .8f);
		Vector3f right = new Vector3f(0.f, 0.f, -.2f);
		Vector3f top = new Vector3f(.2f, 0.f, 0.f);
		RectangleLight rectangleLight = new RectangleLight(bottomLeft, right, top, new Spectrum(100.f, 100.f, 120.f));
		objects.add(rectangleLight);
		
		// Light source
		Vector3f bottomLeft2 = new Vector3f(-0.8f, 3.f, -0.8f);
		RectangleLight rectangleLight2 = new RectangleLight(bottomLeft2, right, top, new Spectrum(100.f, 120.f, 100.f));
		objects.add(rectangleLight2);
		
		// Connect objects to root
		root = objects;
				
		// List of lights
		lightList = new LightList();
		lightList.add(rectangleLight);
		lightList.add(rectangleLight2);

	}
	
}
