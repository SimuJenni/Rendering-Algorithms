package rt.integrators;

import rt.Integrator;
import rt.IntegratorFactory;
import rt.Scene;

public class AreaLightIntegratorFactory implements IntegratorFactory {

	@Override
	public Integrator make(Scene scene) {
		// TODO Auto-generated method stub
		return new AreaLightIntegrator(scene);
	}

	@Override
	public void prepareScene(Scene scene) {
		// TODO Auto-generated method stub

	}

}
