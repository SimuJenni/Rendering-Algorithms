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

public class CornellBox extends Scene {
	
	public CornellBox()
	{	
		outputFilename = new String("../output/testscenes/CornellBox+Sphere");
				
		// Specify pixel sampler to be used
		samplerFactory = new RandomSamplerFactory();
		
		// Samples per pixel
		SPP = 512;
		outputFilename = outputFilename + " " + String.format("%d", SPP) + "SPP";
		
		// Make camera and film
		Vector3f eye = new Vector3f(278.f,273.f,-800.f);
		Vector3f lookAt = new Vector3f(278.f,273.f,0.f);
		Vector3f up = new Vector3f(0.f,1.f,0.f);
		int width = 256;
		int height = 256;
		float fov = 40;

		float aspect = (float)width/(float)height;
		camera = new PinholeCamera(eye, lookAt, up, fov, aspect, width, height);
		film = new BoxFilterFilm(width, height);						
		tonemapper = new ClampTonemapper();
		
		Material gray = new Diffuse(new Spectrum(.4f));
		Material green = new Diffuse(new Spectrum(0f, .5f, .0f));
		Material red = new Diffuse(new Spectrum(.5f, 0, 0));
		
		// Specify integrator to be used
		integratorFactory = new PathTracingIntegratorFactory();
		
		// List of objects
		IntersectableList objects = new IntersectableList();	
		

		// Left, red wall
		Rectangle left = new Rectangle(new Point3f(550f, 0, 0f), new Point3f(550f, 0, 559.2f), new Point3f(550, 548.8f, 559.2f));	
		left.material = red;
		objects.add(left);
		
		// Floor
		Rectangle floor = new Rectangle(new Point3f(555.f,0,0), new Point3f(0,0,0), new Point3f(0,0,555.f));
		floor.material = gray;
		objects.add(floor);

		// Roof
		Rectangle roof = new Rectangle(new Point3f(556, 548.8f, 0), new Point3f(556, 548.8f, 559.2f), new Point3f(0, 548.8f, 559.2f));
		roof.material = gray;
		objects.add(roof);

		// Back, white wall
		Rectangle back = new Rectangle(new Point3f(549.6f, 0, 559.2f), new Point3f(0, 0, 559.2f), new Point3f(0, 548.8f, 559.2f));
		back.material = gray;
		objects.add(back);

		// Right, green wall
		Rectangle right = new Rectangle(new Point3f(0, 0, 559.2f), new Point3f(0, 0, 0), new Point3f(0, 548.8f, 0f));
		right.material = green;
		objects.add(right);
		
		// Add boxes
		Intersectable box = new Cube();
		
		// Small box
		Matrix4f t = new Matrix4f();
		t.setIdentity();
		t.setScale(82.5f);
		Matrix4f rot = new Matrix4f();
		rot.rotY((float) Math.toRadians(-16.616f));
		t.mul(rot);
		Matrix4f trans = new Matrix4f();
		trans.setIdentity();
		trans.setTranslation(new Vector3f(185, 83.5f, 169));
		t.mul(trans, t);
		Instance smallBox = new Instance(box, t);
		smallBox.material = new Diffuse(new Spectrum(.5f));

		objects.add(smallBox);
		
		// Big box
		t = new Matrix4f();
		t.setIdentity();
		t.setScale(82.5f);
		t.m11 = 166.5f;
		rot.rotY((float) Math.toRadians(-72.766f));
		t.mul(rot);
		trans = new Matrix4f();
		trans.setIdentity();
		trans.setTranslation(new Vector3f(368, 167.5f, 351));
		t.mul(trans, t);
		Instance bigBox = new Instance(box, t);
		bigBox.material = new Diffuse(new Spectrum(.5f));
		objects.add(bigBox);
		
		// sphere
		Sphere sphere = new Sphere(new Vector3f(185, 300.5f, 169), 70f);
		sphere.material = new Diffuse(new Spectrum(0.8f, 0.8f, 0.8f));
		sphere.material = new Reflective();

		objects.add(sphere);
	
		// Light source
		Spectrum emission = new Spectrum(110, 105,95);
		emission.mult(3.f);

		RectangleLight rectangleLight = new RectangleLight(new Vector3f(343, 548.6f, 227), new Vector3f(0, 0, 105), new Vector3f(-130, 0, 0), emission);
		objects.add(rectangleLight);
		
		// Connect objects to root
		root = objects;
				
		// List of lights
		lightList = new LightList();
		lightList.add(rectangleLight);
	}
	
}
