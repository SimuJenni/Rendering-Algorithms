package rt;

import java.util.ArrayList;

/**
 * A list of light sources.
 */
public class LightList extends ArrayList<LightGeometry> {

	public LightGeometry getRandomLight() {
		float ran=(float) Math.random();
		int idx=Math.min((int) (this.size()*ran),this.size()-1);
		return this.get(idx);
	}
}
