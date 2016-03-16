package rt.materials;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.vecmath.Matrix3f;
import javax.vecmath.Point2f;
import javax.vecmath.Vector3f;

import rt.HitRecord;
import rt.Material;
import rt.Spectrum;
import rt.Material.ShadingSample;


public class TextureMaterial implements Material {

	private BufferedImage texture;
	private BufferedImage bumpMap;
	private Spectrum kd;

	public TextureMaterial(String textureFile, String bumpMapFile) {
		kd = new Spectrum(.5f);
		kd.mult((float) (1/Math.PI));
		try {
			if(textureFile!=null)
				texture = ImageIO.read(new File(textureFile));
			if (bumpMapFile != null)
				bumpMap = ImageIO.read(new File(bumpMapFile));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public TextureMaterial(String textureFileName) {
		this(textureFileName, null);
	}


	public TextureMaterial(Object object, String bumpFile, Spectrum s) {
		this(null, bumpFile);
		this.kd = s;
	}

	private Spectrum biLinearColor(float xCor, float yCor, BufferedImage texIm) {
		int w=texIm.getWidth();
		int h=texIm.getHeight();
		xCor = xCor*(w-1);
		yCor = yCor*(h-1);
		float u1=(float) Math.floor(xCor);
		float u2=(float) Math.ceil(xCor);
		float v1=(float) Math.floor(yCor);
		float v2=(float) Math.ceil(yCor);
		
		if(u1==u2&&u1>0)
			if(u1>0)
				u1 = u1-1;
			else
				u2 = u2+1;
		if(v1==v2&&v1>0)
			if(v1>0)
				v1 = v1-1;
			else
				v2 = v2+1;
		
		if(u2>=w)
			u2=w-1;
		if(v2>=h)
			v2=h-1;
		int r11 =(texIm.getRGB((int)u1,h-1-(int)v1)>>16)&0x0ff;
		int g11=(texIm.getRGB((int)u1,h-1-(int)v1)>>8) &0x0ff;
		int b11=(texIm.getRGB((int)u1,h-1-(int)v1))    &0x0ff;
		int r12 =(texIm.getRGB((int)u1,h-1-(int)v2)>>16)&0x0ff;
		int g12=(texIm.getRGB((int)u1,h-1-(int)v2)>>8) &0x0ff;
		int b12=(texIm.getRGB((int)u1,h-1-(int)v2))    &0x0ff;
		int r21 =(texIm.getRGB((int)u2,h-1-(int)v1)>>16)&0x0ff;
		int g21=(texIm.getRGB((int)u2,h-1-(int)v1)>>8) &0x0ff;
		int b21=(texIm.getRGB((int)u2,h-1-(int)v1))    &0x0ff;
		int r22 =(texIm.getRGB((int)u2,h-1-(int)v2)>>16)&0x0ff;
		int g22=(texIm.getRGB((int)u2,h-1-(int)v2)>>8) &0x0ff;
		int b22=(texIm.getRGB((int)u2,h-1-(int)v2))    &0x0ff;
		
		
		float wu=(xCor-u1)/(u2-u1);
		float wv=(yCor-v1)/(v2-v1);
		
		float cbr=(1-wu)*r11+wu*r21;
		float ctr=(1-wu)*r12+wu*r22;
		float cr=Math.max(cbr*(1-wv)+ctr*wv,0);
		
		float cbg=(1-wu)*g11+wu*g21;
		float ctg=(1-wu)*g12+wu*g22;
		float cg=Math.max(cbg*(1-wv)+ctg*wv,0);
		
		float cbb=(1-wu)*b11+wu*b21;
		float ctb=(1-wu)*b12+wu*b22;
		float cb=Math.max(cbb*(1-wv)+ctb*wv,0);
		
		assert(!Float.isNaN(cr)&&!Float.isNaN(cg)&&!Float.isNaN(cb));

		if(cr==Float.NaN||cg==Float.NaN||cb==Float.NaN)
			return new Spectrum();
		cr = Math.max(cr, 0);
		cg = Math.max(cr, 0);
		cb = Math.max(cr, 0);

		Spectrum rgbSpec = new Spectrum(cr/255,cg/255,cb/255);
		rgbSpec.mult(new Spectrum(1/(float)Math.PI));
		
		return rgbSpec;
	}
	
	@Override
	public Spectrum evaluateBRDF(HitRecord hitRecord, Vector3f wOut,
			Vector3f wIn) {
		if(texture!=null)
			return biLinearColor(hitRecord.u, hitRecord.v, texture);
		else
			return kd;
	}

	@Override
	public Spectrum evaluateEmission(HitRecord hitRecord, Vector3f wOut) {
		return null;
	}

	@Override
	public boolean hasSpecularReflection() {
		return false;
	}

	@Override
	public ShadingSample evaluateSpecularReflection(HitRecord hitRecord) {
		return null;
	}

	@Override
	public boolean hasSpecularRefraction() {
		return false;
	}

	@Override
	public ShadingSample evaluateSpecularRefraction(HitRecord hitRecord) {
		return null;
	}

	public ShadingSample getShadingSample(HitRecord hitRecord, float[] sample)
	{
		evaluateBumpMap(hitRecord);
		float psi1=sample[0];
		float psi2=sample[1];
		psi1=(float) Math.sqrt(psi1);
		psi2=(float) (Math.PI*2*psi2);
		
		Vector3f dir=new Vector3f((float)Math.cos(psi2)*psi1,
				(float)Math.sin(psi2)*psi1,(float)Math.sqrt(1-sample[0]));
		dir=hitRecord.transformToTangentSpace(dir);
		dir.normalize();
		float p=(float) Math.abs((dir.dot(hitRecord.normal)/Math.PI));
		Spectrum brdf=evaluateBRDF(hitRecord,hitRecord.getNormalizedDirection(),dir);
	
		return new ShadingSample(brdf,new Spectrum(0.f, 0.f, 0.f),dir,hasSpecularReflection(),p);	
	}

	@Override
	public ShadingSample getEmissionSample(HitRecord hitRecord, float[] sample) {
		return null;
	}

	@Override
	public boolean castsShadows() {
		return true;
	}

	public void evaluateBumpMap(HitRecord hitRecord) {
		if (bumpMap != null) {
			hitRecord.makeTangentFrame(hitRecord.normal);
			Spectrum spec = biLinearColor(hitRecord.u, hitRecord.v, bumpMap);
			spec.mult((float) Math.PI);
			Vector3f n = new Vector3f(spec.r, spec.g, spec.b);
			n.scale(2);
			n.sub(new Vector3f(1,1,1));
			hitRecord.transformToTangentSpace(n);
			assert(!Float.isNaN(n.x)&&!Float.isNaN(n.y)&&!Float.isNaN(n.z));
			if(!Float.isNaN(n.x)&&!Float.isNaN(n.y)&&!Float.isNaN(n.z))
				hitRecord.normal = n;
		}
	}

	@Override
	public float getPobability(Vector3f inDir, Vector3f outDir, Vector3f normal) {
		return (float) Math.abs((outDir.dot(normal)/Math.PI));
	}

}
