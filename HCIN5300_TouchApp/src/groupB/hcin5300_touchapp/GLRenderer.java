package groupB.hcin5300_touchapp;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.Vector;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.RectF;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.Matrix;
import android.view.Display;
import android.view.MotionEvent;
import android.view.WindowManager;

public class GLRenderer implements Renderer {

	// Our matrices
	private final float[] mtrxProjection = new float[16];
	private final float[] mtrxView = new float[16];
	private final float[] mtrxProjectionAndView = new float[16];
	
	// Geometric variables
	public static float vertices[];
	public static short indices[];
	public static float uvs[];
	public FloatBuffer vertexBuffer;
	public ShortBuffer drawListBuffer;
	public FloatBuffer uvBuffer;
	
	//test buffers
	public static float tv[];
	public FloatBuffer tvb;
	
	int mPositionHandle;
	int mTexCoordLoc;
	int mtrxhandle;
	int mSamplerLoc;

	int textureIndx = 0;
	int texCount = 11;
	String[] fileName = {"periodic_table4",
			"ag1", "ag2", "ag3", "ag4", "ag5",
			"pb1", "pb2", "pb3", "pb4", "pb5"};
	Vector<int[]> IDs;
	
	// Our screenresolution
	float	mScreenWidth; // = 1280;
	float	mScreenHeight; // = 768;

	// Misc
	Context mContext;
	long mLastTime;
	int mProgram;
	
	//touch timer
	boolean touchEnabled = true;
	long lastTouch = -1;
	long touchOffset = 250; //in milliseconds
	
	//touch coordinates
	TouchCoords ag;
	TouchCoords pb;
	TouchCoords l1;
	TouchCoords l2;
	TouchCoords l3;
	TouchCoords l4;
	TouchCoords l5;
	
	public GLRenderer(Context c)
	{
		mContext = c;
		mLastTime = System.currentTimeMillis() + 100;
		
		WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		mScreenWidth = size.x;
		mScreenHeight = size.y;
		
		initTouchCoords();
		loadBuffers();
		
		//test 
		//testBuffers();
	}
	
	public void initTouchCoords()
	{
		ag = new TouchCoords(0.575f, 0.56f, 0.628f, 0.45f);
		pb = new TouchCoords(0.727f, 0.462f, 0.780f, 0.352f);
		l1 = new TouchCoords(0.045f, 0.160f, 0.159f, 0.07f);
		l2 = new TouchCoords(0.197f, 0.160f, 0.311f, 0.07f);
		l3 = new TouchCoords(0.349f, 0.160f, 0.463f, 0.07f);
		l4 = new TouchCoords(0.695f, 0.160f, 0.809f, 0.07f);
		l5 = new TouchCoords(0.847f, 0.160f, 0.961f, 0.07f);
	}
	
	public void loadBuffers()
	{
		vertices = new float[] {
				0.0f, mScreenHeight, 0.0f,
				0.0f, 0.0f, 0.0f,
				mScreenWidth, 0.0f, 0.0f,
				mScreenWidth, mScreenHeight, 0.0f };
		indices = new short[] {0, 1, 2, 0, 2, 3}; 
		uvs = new float[] {
				0.0f, 0.0f,
				0.0f, 1.0f,
				1.0f, 1.0f,			
				1.0f, 0.0f			
	    };
		
	}
	
	//test function
	public void testBuffers()
	{
		float l = mScreenWidth*0.847f;
		float t = mScreenHeight*0.160f;
		float r = mScreenWidth*0.961f;
		float b = mScreenHeight*0.07f;
		tv = new float[] {
				l, t, 0.0f,
				l, b, 0.0f,
				r, b, 0.0f,
				r, t, 0.0f };
		// The vertex buffer.
		ByteBuffer bb = ByteBuffer.allocateDirect(tv.length * 4);
		bb.order(ByteOrder.nativeOrder());
		tvb = bb.asFloatBuffer();
		tvb.put(tv);
		tvb.position(0);
	}
	
	public void onPause()
	{
		/* Do stuff to pause the renderer */
	}
	
	public void onResume()
	{
		/* Do stuff to resume the renderer */
		mLastTime = System.currentTimeMillis();
	}
	
	@Override
	public void onDrawFrame(GL10 unused) {
		
		// Get the current time
    	long now = System.currentTimeMillis();
    	
    	// We should make sure we are valid and sane
    	if (mLastTime > now) return;
    	
    	if((now - lastTouch) >= touchOffset)
    		touchEnabled = true;
		
		// Render our example
		Render(mtrxProjectionAndView);
		
		// Save the current time to see how long it took :).
        mLastTime = now;		
	}
	
	private void Render(float[] m) {
		
		// clear Screen and Depth Buffer, we have set the clear color as black.
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        
        GLES20.glDisable(GLES20.GL_CULL_FACE);
        
        // get handle to vertex shader's vPosition member
	    mPositionHandle = GLES20.glGetAttribLocation(riGraphicTools.sp_Image, "vPosition");
	    // Get handle to texture coordinates location
	    mTexCoordLoc = GLES20.glGetAttribLocation(riGraphicTools.sp_Image, "a_texCoord" );
	    // Get handle to shape's transformation matrix
        mtrxhandle = GLES20.glGetUniformLocation(riGraphicTools.sp_Image, "uMVPMatrix");       
        // Get handle to textures locations
        mSamplerLoc = GLES20.glGetUniformLocation (riGraphicTools.sp_Image, "s_texture" );
        
        //test
        //DrawTestCoords(m);
	    
	    // Enable generic vertex attribute array
	    GLES20.glEnableVertexAttribArray(mPositionHandle);
	    // Prepare the triangle coordinate data
	    GLES20.glVertexAttribPointer(mPositionHandle, 3,
	                                 GLES20.GL_FLOAT, false,
	                                 0, vertexBuffer);
	    // Enable generic vertex attribute array
	    GLES20.glEnableVertexAttribArray ( mTexCoordLoc );
	    
	    // Prepare the texturecoordinates
	    GLES20.glVertexAttribPointer ( mTexCoordLoc, 2, GLES20.GL_FLOAT,
                false, 
                0, uvBuffer); 
	    
	    GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
	    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 
        		IDs.get(textureIndx)[0]);
	    
        // Apply the projection and view transformation
        GLES20.glUniformMatrix4fv(mtrxhandle, 1, false, m, 0);       
             
        // Set the sampler texture unit to 0, where we have saved the texture.
        GLES20.glUniform1i ( mSamplerLoc, 0);
        // Draw the triangle
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, indices.length,
                GLES20.GL_UNSIGNED_SHORT, drawListBuffer);
        
        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle);
        GLES20.glDisableVertexAttribArray(mTexCoordLoc);              
        
        GLES20.glDisable(GLES20.GL_BLEND);
        GLES20.glDisable(GLES20.GL_DEPTH_TEST);
	}
	
	//test function
	public void DrawTestCoords(float[] m)
	{
	    // Enable generic vertex attribute array
	    GLES20.glEnableVertexAttribArray(mPositionHandle);
	    // Prepare the triangle coordinate data
	    GLES20.glVertexAttribPointer(mPositionHandle, 3,
	                                 GLES20.GL_FLOAT, false,
	                                 0, tvb);
	    // Enable generic vertex attribute array
	    GLES20.glEnableVertexAttribArray ( mTexCoordLoc );
	    
	    // Prepare the texturecoordinates
	    GLES20.glVertexAttribPointer ( mTexCoordLoc, 2, GLES20.GL_FLOAT,
                false, 
                0, uvBuffer);
	    // Get handle to textures locations
        mSamplerLoc = GLES20.glGetUniformLocation (riGraphicTools.sp_Image, "s_texture" ); 
	    
	    GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
	    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 
        		IDs.get(1)[0]);
	    
        // Apply the projection and view transformation
        GLES20.glUniformMatrix4fv(mtrxhandle, 1, false, m, 0);       
             
        // Set the sampler texture unit to 0, where we have saved the texture.
        GLES20.glUniform1i ( mSamplerLoc, 0);
        // Draw the triangle
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, indices.length,
                GLES20.GL_UNSIGNED_SHORT, drawListBuffer);
        
     // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle);
        GLES20.glDisableVertexAttribArray(mTexCoordLoc);
	}

	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		
		// We need to know the current width and height.
		mScreenWidth = width;
		mScreenHeight = height;
		
		// Redo the Viewport, making it fullscreen.
		GLES20.glViewport(0, 0, (int)mScreenWidth, (int)mScreenHeight);
		
		// Clear our matrices
	    for(int i=0;i<16;i++)
	    {
	    	mtrxProjection[i] = 0.0f;
	    	mtrxView[i] = 0.0f;
	    	mtrxProjectionAndView[i] = 0.0f;
	    }
	    
	    // Setup our screen width and height for normal sprite translation.
	    Matrix.orthoM(mtrxProjection, 0, 0f, mScreenWidth, 0.0f, mScreenHeight, 0, 50);
	    
	    // Set the camera position (View matrix)
        Matrix.setLookAtM(mtrxView, 0, 0f, 0f, 1f, 0f, 0f, 0f, 0f, 1.0f, 0.0f);

        // Calculate the projection and view transformation
        Matrix.multiplyMM(mtrxProjectionAndView, 0, mtrxProjection, 0, mtrxView, 0);
	}

	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		IDs = new Vector<int[]>();
		for(int j=0;j<texCount;++j)
		{
			IDs.add(new int[1]);
		}
		// Create the triangles
		SetupTriangle();
		// Create the image information
		SetupImage();
		
		// Set the clear color to black
		GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
		
		//textures
		for(int o=0;o<texCount;++o)
		{
			GLES20.glGenTextures(1, IDs.get(o), 0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, IDs.get(o)[0]);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            int id = mContext.getResources().getIdentifier("drawable/"+
					fileName[o], null, mContext.getPackageName());
            Bitmap bmp = BitmapFactory.decodeResource(mContext.getResources(), id);
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bmp, 0);
    		bmp.recycle();
		}    

	    // Create the shaders, solid color
	    int vertexShader = riGraphicTools.loadShader(GLES20.GL_VERTEX_SHADER, riGraphicTools.vs_SolidColor);
	    int fragmentShader = riGraphicTools.loadShader(GLES20.GL_FRAGMENT_SHADER, riGraphicTools.fs_SolidColor);

	    riGraphicTools.sp_SolidColor = GLES20.glCreateProgram();             // create empty OpenGL ES Program
	    GLES20.glAttachShader(riGraphicTools.sp_SolidColor, vertexShader);   // add the vertex shader to program
	    GLES20.glAttachShader(riGraphicTools.sp_SolidColor, fragmentShader); // add the fragment shader to program
	    GLES20.glLinkProgram(riGraphicTools.sp_SolidColor);                  // creates OpenGL ES program executables
	    
	    // Create the shaders, images
	    vertexShader = riGraphicTools.loadShader(GLES20.GL_VERTEX_SHADER, riGraphicTools.vs_Image);
	    fragmentShader = riGraphicTools.loadShader(GLES20.GL_FRAGMENT_SHADER, riGraphicTools.fs_Image);

	    riGraphicTools.sp_Image = GLES20.glCreateProgram();             // create empty OpenGL ES Program
	    GLES20.glAttachShader(riGraphicTools.sp_Image, vertexShader);   // add the vertex shader to program
	    GLES20.glAttachShader(riGraphicTools.sp_Image, fragmentShader); // add the fragment shader to program
	    GLES20.glLinkProgram(riGraphicTools.sp_Image);                  // creates OpenGL ES program executables	    
	    
	    // Set our shader programm
		GLES20.glUseProgram(riGraphicTools.sp_Image);
	}
	
	public void processTouchEvent(MotionEvent event)
	{
		float tx = event.getX();
		float ty = event.getY();
		
		if(touchEnabled){
			//textureIndx = 1 - textureIndx;
			lastTouch = System.currentTimeMillis();
			touchEnabled = false;
		}
	}
	
	public void SetupImage()
	{
		// The texture buffer
		ByteBuffer bb = ByteBuffer.allocateDirect(uvs.length * 4);
		bb.order(ByteOrder.nativeOrder());
		uvBuffer = bb.asFloatBuffer();
		uvBuffer.put(uvs);
		uvBuffer.position(0);	
	}

	public void SetupTriangle()
	{
		// The vertex buffer.
		ByteBuffer bb = ByteBuffer.allocateDirect(vertices.length * 4);
		bb.order(ByteOrder.nativeOrder());
		vertexBuffer = bb.asFloatBuffer();
		vertexBuffer.put(vertices);
		vertexBuffer.position(0);
		
		// initialize byte buffer for the draw list
		ByteBuffer dlb = ByteBuffer.allocateDirect(indices.length * 2);
		dlb.order(ByteOrder.nativeOrder());
		drawListBuffer = dlb.asShortBuffer();
		drawListBuffer.put(indices);
		drawListBuffer.position(0);
	}
}