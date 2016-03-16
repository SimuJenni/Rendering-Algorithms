package test1;

import static org.junit.Assert.*;

import javax.vecmath.Vector3f;

import org.junit.*;

import rt.HitRecord;
import rt.Material.ShadingSample;
import rt.Ray;
import rt.Spectrum;
import rt.intersectables.Plane;
import rt.intersectables.Rectangle;
import rt.lightsources.RectangleLight;
import rt.samplers.JitterSampler;

public class unitTest1 {

	private static Vector3f rayDir, rayOrigin, normal;

	@Before
	public void setUp() {
		rayOrigin = new Vector3f(0, 0, 10);
		rayDir = new Vector3f(0, 0, -1);
	}

	@Test
	public void testPlaneHit1() {
		normal = new Vector3f(0, 0, 1);
		Plane plane = new Plane(normal, 0);
		HitRecord hit = plane.intersect(new Ray(rayOrigin, rayDir));
		assertEquals (hit.position, new Vector3f(0, 0, 0));
		assertEquals (hit.normal, normal);
		assertEquals (hit.w,normal);
		assertEquals(hit.t1.dot(hit.t2),0,0.0001);
		assertEquals(hit.t1.dot(hit.normal),0, 0.0001);
	}
	
	@Test
	public void testPlaneHit2() {
		normal = new Vector3f(0, 1, 1);
		Plane plane = new Plane(new Vector3f(normal), 0);
		normal.normalize();
		HitRecord hit = plane.intersect(new Ray(rayOrigin, rayDir));
		assertEquals (hit.normal.length(), 1f, 0.0001);
		assertEquals (hit.position, new Vector3f(0, 0, 0));
		assertEquals (hit.normal, normal);
		Vector3f negRayDir = new Vector3f(rayDir);
		negRayDir.negate();
		assertEquals (hit.w,negRayDir);
		assertEquals(hit.t1.dot(hit.t2),0f,0.0001);
		assertEquals(hit.t1.dot(hit.normal),0f, 0.0001);
	}
	
	@Test
	public void testRecthit() {
		Vector3f bottomLeft = new Vector3f(-1,-1,0), right = new Vector3f(2,0,0),
				up = new Vector3f(0,2,0);
		normal = new Vector3f(0, 0, 1);
		Rectangle rect = new Rectangle(bottomLeft, right, up);
		normal.normalize();
		HitRecord hit = rect.intersect(new Ray(rayOrigin, rayDir));
		assertEquals (hit.normal.length(), 1, 0.0001);
		assertEquals (hit.position, new Vector3f(0, 0, 0));
		assertEquals (hit.normal, normal);
		Vector3f negRayDir = new Vector3f(rayDir);
		negRayDir.negate();
		assertEquals (hit.w,negRayDir);
		assertEquals(hit.t1.dot(hit.t2),0.f,0.0001);
		assertEquals(hit.t1.dot(hit.normal),0.f, 0.0001);
		assertEquals(rect.getArea(),4.f,0.0001);
	}
	
	@Test
	public void testRecthit2() {
		Vector3f bottomLeft = new Vector3f(-1,-1,-1), right = new Vector3f(2,0,2),
				up = new Vector3f(0,2,0);
		normal = new Vector3f(-1, 0, 1);
		Rectangle rect = new Rectangle(bottomLeft, right, up);
		normal.normalize();
		HitRecord hit = rect.intersect(new Ray(rayOrigin, rayDir));
		assertEquals (hit.normal.length(), 1.f, 0.0001);
		assertEquals (hit.position, new Vector3f(0, 0, 0));
		assertEquals (hit.normal, normal);
		assertEquals(hit.w.length(),1f,0.0001);
		Vector3f negRayDir = new Vector3f(rayDir);
		negRayDir.negate();
		assertEquals (hit.w,negRayDir);
		assertEquals(hit.t1.dot(hit.t2),0.f,0.0001);
		assertEquals(hit.t1.dot(hit.normal),0.f, 0.0001);
	}
	
	@Test
	public void testRecthit3() {
		Vector3f bottomLeft = new Vector3f(0.001f,0,0), right = new Vector3f(2,0,2),
				up = new Vector3f(0,2,0);
		Rectangle rect = new Rectangle(bottomLeft, right, up);
		HitRecord hit = rect.intersect(new Ray(rayOrigin, rayDir));
		assertNull (hit);
	}
	
	@Test
	public void testJitterSampler(){
		JitterSampler sampler = new JitterSampler();
		float[][] samples = sampler.makeSamples(4,2);
		assertTrue(samples[0][0]<=0.5f);
		assertTrue(samples[0][1]<=0.5f);
		assertTrue(samples[3][0]>=0.5f);
		assertTrue(samples[3][1]>=0.5f);
	}

	@Test
	public void testRectLightSample(){
		Vector3f bottomLeft = new Vector3f(-1,-1,0), right = new Vector3f(2,0,0),
				up = new Vector3f(0,2,0);
		RectangleLight rLight = new RectangleLight(bottomLeft, right, up, new Spectrum(1,1,1));
		HitRecord lSamp = rLight.sample(new float[]{1f,1f});
		assertEquals(lSamp.position,new Vector3f(1,1,0));
		lSamp = rLight.sample(new float[]{0f,0f});
		assertEquals(lSamp.position,bottomLeft);
		assertEquals(lSamp.p, 1/4.f, 0.0001);
		assertEquals(lSamp.normal, new Vector3f(0,0,1));
	}
	
	@Test
	public void testRectLightSampleDirection(){
		normal = new Vector3f(0, 0, 1);
		Vector3f bottomLeft = new Vector3f(-1,-1,0), right = new Vector3f(2,0,0),
				up = new Vector3f(0,2,0);
		RectangleLight rLight = new RectangleLight(bottomLeft, right, up, new Spectrum(1,1,1));
		HitRecord lSamp = rLight.sample(new float[]{0.5f,0.5f});
		ShadingSample s = lSamp.material.getEmissionSample(lSamp, new float[]{0f,0f});
		assertEquals(s.w, normal);
		assertEquals(s.w.length(),1f,0.0001);
		assertTrue(s.w.dot(normal)>=0);
		assertTrue(s.p<=1/Math.PI);
		s = lSamp.material.getEmissionSample(lSamp, new float[]{0.592f,0.573f});
		assertTrue(s.w.dot(normal)>=0);
		assertEquals(s.w.length(),1f,0.0001);
		assertTrue(s.p<=1/Math.PI);
	}
	
}
