package rt.integrators;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import rt.Film;
import rt.Integrator;
import rt.IntegratorFactory;
import rt.Scene;
import rt.Spectrum;
import rt.films.BoxFilterFilm;
import rt.tonemappers.ClampTonemapper;

public class BDPathTracingIntegratorFactory implements IntegratorFactory {

	private ArrayList<BDPathTracingIntegrator> integrators = new ArrayList<>();
	private Scene scene;
	private BoxFilterFilm lightImage;

	public BDPathTracingIntegratorFactory(Scene scene) {
		this.scene = scene;
		this.lightImage = new BoxFilterFilm(scene.getFilm().getWidth(), scene.getFilm().getHeight());
	}

	@Override
	public Integrator make(Scene scene) {
		BDPathTracingIntegrator integrator = new BDPathTracingIntegrator(scene, lightImage);
		integrators.add(integrator);
		return integrator;
	}

	@Override
	public void prepareScene(Scene scene) {
		// TODO Auto-generated method stub

	}
	
	public void writeLightImage(String path){	
		
		BufferedImage img = new BufferedImage(lightImage.getWidth(), lightImage.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
		
		Spectrum[][] lightImg = lightImage.getImage();

		for(int i=0; i<lightImage.getWidth(); i++)
		{
			for(int j=0; j<lightImage.getHeight(); j++)
			{
				// Clamping
				Spectrum s = lightImg[i][j];
				s.clamp(0,1);
				img.setRGB(i, lightImage.getHeight()-1-j, ((int)(255.f*s.r) << 16) | ((int)(255.f*s.g) << 8) | ((int)(255.f*s.b)));
			}
		}
				
		try
		{	
			ImageIO.write(img, "png", new File(path+".png"));
			System.out.println("Wrote light image to: \n " + path);
		} catch (IOException e) {System.out.println("Could not write image to \n"+ path);}
	}

	public void addLightImage(Film film){
		film.addLightImage(lightImage);
	}

	@Override
	public void finish() {
		writeLightImage("../output/testscenes/lightimage");
		addLightImage(scene.getFilm());
	}

}