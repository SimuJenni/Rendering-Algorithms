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

public class spheresEverywhere extends Scene {

	public spheresEverywhere()
	{
		
		// List of objects
		IntersectableList objects = new IntersectableList();	
		// Output file name
		outputFilename = new String("../output/basicscenes/SpheresOverSpheres");
		
		// Image width and height in pixels
		width = 512;
		height = 512;
		
		// Number of samples per pixel
		SPP = 5;
		
		// Specify which camera, film, and tonemapper to use
		
		Vector3f eye = new Vector3f(0.f, 0.f, 3.0f);
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
		for(int i=0;i<10;i++){
			for(int j=0;j<10;j++){
				for(int k=0;k<10;k++){
					Sphere s=new Sphere(new Vector3f((-5+i)*2,(-5+j)*2,-k*2),0.8f);
					objects.add(s);
				}
			}
		}

		BSPAccelerator spheres=new BSPAccelerator(objects);
			
		root=spheres;
//		root=objects;
		
		// Light sources
		LightGeometry pointLight1 = new PointLight(new Vector3f(-.8f, 0.8f, 3f), new Spectrum(3.f, 4.f, 6.f));
		LightGeometry pointLight2 = new PointLight(new Vector3f(0.8f, -0.5f, 2.8f), new Spectrum(7.f, 4.f, 3.f));
		LightGeometry pointLight3 = new PointLight(new Vector3f(0f, 0, 3.f), new Spectrum(0, 6.f, 2.f));

		lightList = new LightList();
		lightList.add(pointLight1);
		lightList.add(pointLight2);
		lightList.add(pointLight3);

	}
}
