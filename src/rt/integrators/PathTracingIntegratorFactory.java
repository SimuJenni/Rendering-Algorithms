package rt.integrators;

import rt.Integrator;
import rt.IntegratorFactory;
import rt.Scene;

public class PathTracingIntegratorFactory implements IntegratorFactory {

	@Override
	public Integrator make(Scene scene) {
		// TODO Auto-generated method stub
		return new PathTracingIntegrator(scene);
	}

	@Override
	public void prepareScene(Scene scene) {
		// TODO Auto-generated method stub

	}

	@Override
	public void finish() {
		// TODO Auto-generated method stub
		
	}

}
