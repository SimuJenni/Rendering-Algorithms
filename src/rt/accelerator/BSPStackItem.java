package rt.accelerator;

public class BSPStackItem {

	BSPNode node;
	public float tmin, tmax;

	public BSPStackItem(BSPNode node, float t1, float t2) {
		super();
		this.node = node;
		this.tmin = t1;
		this.tmax = t2;
	}
}
