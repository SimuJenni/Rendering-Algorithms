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
		outputFilename = new String("../output/testscenes/CornellBox");
				
		// Specify pixel sampler to be used
		samplerFactory = new RandomSamplerFactory();
		
		// Samples per pixel
		SPP = 32;
		outputFilename = outputFilename + " " + String.format("%d", SPP) + "SPP";
		
		// Make camera and film
		Vector3f eye = new Vector3f(278.f,273.f,-800.f);
		Vector3f lookAt = new Vector3f(278.f,273.f,0.f);
		Vector3f up = new Vector3f(0.f,1.f,0.f);
		int width = 400;
		int height = 400;
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
		integratorFactory = new BDPathTracingIntegratorFactory(this);

		
		// List of objects
		IntersectableList objects = new IntersectableList();	
		

		// Left, red wall
//		Rectangle left = new Rectangle(new Point3f(550f, 0, 0f), new Point3f(550f, 0, 559.2f), new Point3f(550, 548.8f, 559.2f));
		Rectangle left = new Rectangle(new Vector3f(550f, 0, 0f), new Vector3f(0, 0, 559.2f), new Vector3f(0, 548.8f, 0));	

		left.material = red;
		objects.add(left);
		
		// Floor
//		Rectangle floor = new Rectangle(new Point3f(555.f,0,0), new Point3f(0,0,0), new Point3f(0,0,555.f));
		Rectangle floor = new Rectangle(new Vector3f(555f, 0, 0f), new Vector3f(-555f, 0, 0), new Vector3f(0, 0, 555.f));	

		floor.material = gray;
		objects.add(floor);

		// Roof
//		Rectangle roof = new Rectangle(new Point3f(556, 548.8f, 0), new Point3f(556, 548.8f, 559.2f), new Point3f(0, 548.8f, 559.2f));
		Rectangle roof = new Rectangle(new Vector3f(556, 548.8f, 0), new Vector3f(0, 0, 559.2f), new Vector3f(-556.f, 0, 0));	

		roof.material = gray;
		objects.add(roof);

		// Back, white wall
//		Rectangle back = new Rectangle(new Point3f(549.6f, 0, 559.2f), new Point3f(0, 0, 559.2f), new Point3f(0, 548.8f, 559.2f));
		Rectangle back = new Rectangle(new Vector3f(549.6f, 0, 559.2f), new Vector3f(-549.6f, 0, 0), new Vector3f(0, 548.8f, 0));	
		back.material = gray;
		objects.add(back);

		// Right, green wall
//		Rectangle right = new Rectangle(new Point3f(0, 0, 559.2f), new Point3f(0, 0, 0), new Point3f(0, 548.8f, 0f));
		Rectangle right = new Rectangle(new Vector3f(0, 0, 559.2f), new Vector3f(0, 0, -559.2f), new Vector3f(0, 548.8f, 0));	
		right.material = green;
		objects.add(right);
		
		// Add boxes
		Intersectable box = new Cube();
		
		// Small box
		Matrix4f t = new Matrix4f();
		t.setIdentity();
		t.setScale(82.5f);
		Matrix4f rot = new Matrix4f();
//		rot.rotY((float) Math.toRadians(-16.616f));
		rot.rotY((float) Math.toRadians(-36.616f));

		t.mul(rot);
		Matrix4f trans = new Matrix4f();
		trans.setIdentity();
		trans.setTranslation(new Vector3f(185, 83.5f, 169));
		
		t.mul(trans, t);
		Instance smallBox = new Instance(box, t);
		smallBox.material = new Diffuse(new Spectrum(.5f));
//		smallBox.material = new Reflective();
//		smallBox.material = new Refractive(1.3f);

		objects.add(smallBox);
		
		// Big box
		t = new Matrix4f();
		t.setIdentity();

		t.setScale(82.5f);
		t.m11 = 166.5f;
		rot.rotY((float) Math.toRadians(-72.766f));
//		rot.rotY((float) Math.toRadians(90));
		
		t.mul(rot);
		trans = new Matrix4f();
		trans.setIdentity();
		trans.setTranslation(new Vector3f(368, 167.5f, 351));
//		trans.setTranslation(new Vector3f(300, 167.5f, 400));

		t.mul(trans, t);
		Instance bigBox = new Instance(box, t);
		bigBox.material = new Diffuse(new Spectrum(.5f));
//		bigBox.material = new Refractive(1.3f);

		objects.add(bigBox);
		
		// sphere
		Sphere sphere = new Sphere(new Vector3f(400, 80.5f, 100), 75f);
		sphere.material = new Diffuse(new Spectrum(0.8f, 0.8f, 0.8f));
		sphere.material = new Refractive(1.3f);

		objects.add(sphere);
	
		// Light source
		Spectrum emission = new Spectrum(40, 35,30);
		emission.mult(500000.f);

		RectangleLight rectangleLight = new RectangleLight(new Vector3f(343, 548.6f, 227), new Vector3f(0, 0, 105), new Vector3f(-130, 0, 0), emission);
		objects.add(rectangleLight);
		
		// Connect objects to root
		root = objects;
				
		// List of lights
		lightList = new LightList();
		lightList.add(rectangleLight);
	}
	
	public void finish()
	{
		if(integratorFactory instanceof BDPathTracingIntegratorFactory)
		{
			((BDPathTracingIntegratorFactory)integratorFactory).writeLightImage("../output/testscenes/lightimage");
			((BDPathTracingIntegratorFactory)integratorFactory).addLightImage(film);
		}
	}
}
