package groupB.hcin5300_touchapp;

public class TouchCoords {

	public float l;
	public float t;
	public float r;
	public float b;
	
	//test coords
	public float tt;
	public float tb;
	
	public TouchCoords(float l, float t, float r, float b)
	{
		this.l = l*GLRenderer.mScreenWidth;
		this.t = t*GLRenderer.mScreenHeight;
		this.r = r*GLRenderer.mScreenWidth;
		this.b = b*GLRenderer.mScreenHeight;
		
		tt = GLRenderer.mScreenHeight - this.t;
		tb = GLRenderer.mScreenHeight - this.b;
	}
}
