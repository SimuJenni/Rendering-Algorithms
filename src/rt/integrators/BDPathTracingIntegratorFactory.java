package rt.integrators;

import rt.Integrator;
import rt.IntegratorFactory;
import rt.Scene;

public class BDPathTracingIntegratorFactory implements IntegratorFactory {

	@Override
	public Integrator make(Scene scene) {
		// TODO Auto-generated method stub
		return new BDPathTracingIntegrator(scene);

	}

	@Override
	public void prepareScene(Scene scene) {
		// TODO Auto-generated method stub

	}

}