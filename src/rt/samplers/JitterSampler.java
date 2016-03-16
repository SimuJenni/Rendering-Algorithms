package rt.samplers;

import java.util.Random;

import rt.Sampler;

public class JitterSampler implements Sampler {
	
	private Random random;

	public JitterSampler() {
		random = new Random();
	}

	@Override
	public float[][] makeSamples(int n, int d) {
		assert(d==2);
		int numPerDim = (int) Math.pow(n,1.f/d);
		float cellSize = 1.f/numPerDim;
		float[][] samples = new float[n][d];
		for(int i=0; i<n; i++){
			for(int j=0; j<d; j++){
				int t = i%numPerDim;
				if(j==0)
					samples[i][j]=random.nextFloat()*cellSize+i%numPerDim*cellSize;
				else
					samples[i][j]=random.nextFloat()*cellSize+i/numPerDim*cellSize;
			}
		}
		return samples;
	}

}
