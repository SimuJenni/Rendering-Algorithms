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

public class GlossySphere extends Scene {

	public GlossySphere()
	{
		
		// List of objects
		IntersectableList objects = new IntersectableList();	
		// Output file name
		outputFilename = new String("../output/basicscenes/glossySphere");
		
		// Image width and height in pixels
		width = 512;
		height = 512;
		
		// Number of samples per pixel
		SPP = 5;
		
		// Specify which camera, film, and tonemapper to use
		
		Vector3f eye = new Vector3f(0.8f, 0.5f, 2.0f);
		Vector3f lookAt = new Vector3f(0.f, 0.f, 0.f);
		Vector3f up = new Vector3f(0.f, 1.f, 0.f);
		camera =new PinholeCamera(eye, lookAt, up, 80, 1, width, height);
		
		film = new BoxFilterFilm(width, height);
		tonemapper = new ClampTonemapper();
		
		// Specify which integrator and sampler to use
//		integratorFactory = new PointLightIntegratorFactory();
		integratorFactory = new WhittedIntegratorFactory();

//		samplerFactory = new OneSamplerFactory();
		samplerFactory = new RandomSamplerFactory();
		
		// Add objects
		Mesh mesh;
		try	{
		mesh = ObjReader.read("../obj/teapot.obj", 1.f);
		} catch(IOException e) 
		{
			System.out.printf("Could not read .obj file\n");
			return;
		}
		
		
		Glossy material=new Glossy(10,new Spectrum(1,1,1),new Spectrum(1,1,1));

		Sphere sphere = new Sphere(new Vector3f(0.f, 0f, 0f), 0.8f);
		sphere.material=material;
		mesh.material=material;
		
//		objects.add(mesh);	
		objects.add(sphere);

		
		root=objects;
		
		// Light sources
		LightGeometry pointLight = new PointLight(new Vector3f(0f, -.5f, 1.6f), new Spectrum(3.f, 4.f, 6.f));
		LightGeometry pointLight2 = new PointLight(new Vector3f(-1.6f, 0f, .8f), new Spectrum(7.f, 4.f, 3.f));
		LightGeometry pointLight3 = new PointLight(new Vector3f(1f, 1.f, 0f), new Spectrum(3.f, 6.f, 4.f));

		lightList = new LightList();
		lightList.add(pointLight);
		lightList.add(pointLight2);
		lightList.add(pointLight3);

	}
}
