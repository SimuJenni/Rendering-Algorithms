package rt;

/**
 * Stores a spectrum of color values. In this implementation, we work with RGB colors.
 */
public class Spectrum {

	@Override
	public String toString() {
		return "Spectrum [r=" + r + ", g=" + g + ", b=" + b + "]";
	}

	public float r, g, b;
	private float max = Float.MAX_VALUE;
	
	public Spectrum()
	{
		r = 0.f;
		g = 0.f;
		b = 0.f;
	}
	
	public Spectrum(float r, float g, float b)
	{
		this.r = r;
		this.g = g;
		this.b = b;
	}
	
	public Spectrum(Spectrum s)
	{
		this.r = s.r;
		this.g = s.g;
		this.b = s.b;
	}
	
	public Spectrum(float f) {
		this.r = f;
		this.g = f;
		this.b = f;	}

	public void mult(float t)
	{
		r = r*t;
		g = g*t;
		b = b*t;
	}
	
	public void mult(Spectrum s)
	{
		r = r*s.r;
		g = g*s.g;
		b = b*s.b;
	}
	
	public void add(Spectrum s)
	{
		r = r+s.r;
		g = g+s.g;
		b = b+s.b;
	}
	
	public void clamp(float min, float max)
	{
		r = Math.min(max,  r);
		r = Math.max(min, r);
		g = Math.min(max,  g);
		g = Math.max(min, g);
		b = Math.min(max,  b);
		b = Math.max(min, b);
	}

	public void sqrt() {
		r=(float) Math.sqrt(r);
		g=(float) Math.sqrt(g);
		b=(float) Math.sqrt(b);
	}

	public void divide(float t) {
		r = r/t;
		g = g/t;
		b = b/t;	
	}

	public void sub(Spectrum s) {
		r = r-s.r;
		g = g-s.g;
		b = b-s.b;		
	}

	public void divide(Spectrum s) {
		r = r/s.r;
		g = g/s.g;
		b = b/s.b;		
	}

	public void add(float s) {
		r = r+s;
		g = g+s;
		b = b+s;		
	}

	public void nonNeg() {
//		assert(!Float.isNaN(r));
//		assert(!Float.isNaN(g));
//		assert(!Float.isNaN(b));
//		assert(r>=0||g>=0||b>=0);

		if(r==Float.NaN)
			r=0;
		if(b==Float.NaN)
			b=0;
		if(g==Float.NaN)
			g=0;
		r = Math.max(r, 0);
		b = Math.max(b, 0);	
		g = Math.max(g, 0);	
//		r = Math.min(r, max);
//		b = Math.min(b, max);	
//		g = Math.min(g, max);	
	}
	
}
