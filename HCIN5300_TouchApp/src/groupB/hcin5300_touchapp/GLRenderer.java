package groupB.hcin5300_touchapp;

import groupB.hcin5300_touchapp.AboutScreen;

import java.io.FileWriter;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.Calendar;
import java.util.Vector;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.Matrix;
import android.os.Environment;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.WindowManager;

public class GLRenderer implements Renderer {
	
	private static final String LOGTAG = "GLRenderer";

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

	boolean elementSelected = false;
	int elementNo = -1;
	int currLevel = 1;
	int textureIndx = 0;	
	int texCount = 12;
	String[] fileName = {"periodic_table4",
			"ag1", "ag2", "ag3", "ag4", "ag5", //Ag texture range [1-5]
			"pb1", "pb2", "pb3", "pb4", "pb5", //Pb texture range [6-10]
			"selection"};
	Vector<int[]> IDs;
	final int AG1 = 1;
	final int AG2 = 2;
	final int AG3 = 3;
	final int AG4 = 4;
	final int AG5 = 5;
	final int PB1 = 6;
	final int PB2 = 7;
	final int PB3 = 8;
	final int PB4 = 9;
	final int PB5 = 10;
	
	// Our screenresolution
	public static float mScreenWidth; // = 1280;
	public static float mScreenHeight; // = 768;

	// Misc
	Context mContext;
	
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
	
    // Log File Variables
    String participant = AboutScreen.message;
    String filename = participant.replace(" ", "");
	//Calendar cal = Calendar.getInstance();
	
	public GLRenderer(Context c)
	{
		mContext = c;
		
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
		//touch coordinates increase from top to left (unlike texture coordinates)
		ag = new TouchCoords(0.575f, 0.44f, 0.628f, 0.55f);
		pb = new TouchCoords(0.729f, 0.546f, 0.782f, 0.656f);
		l1 = new TouchCoords(0.045f, 0.84f, 0.159f, 0.93f);
		l2 = new TouchCoords(0.197f, 0.84f, 0.311f, 0.93f);
		l3 = new TouchCoords(0.349f, 0.84f, 0.463f, 0.93f);
		l4 = new TouchCoords(0.695f, 0.84f, 0.809f, 0.93f);
		l5 = new TouchCoords(0.847f, 0.84f, 0.961f, 0.93f);
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
		float l = pb.l;
		float t = pb.tt;
		float r = pb.r;
		float b = pb.tb;
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
	}
	
	@Override
	public void onDrawFrame(GL10 unused) {
		
		// Get the current time
    	long now = System.currentTimeMillis();   	
    	if((now - lastTouch) >= touchOffset)
    		touchEnabled = true;
		
		// Render our example
		Render(mtrxProjectionAndView);		
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
        		IDs.get(11)[0]);
	    
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
		
		// Write to File   
        try
        {
	        // Write Header - Date/Time, Participant Name, Test Type
        	// File location : Public Downloads folder
        	//cal = cal.getInstance();
        	Calendar cal = Calendar.getInstance();
        	String dateTime = cal.getTime().toString();
        	//dateTime += ":" + Integer.toString(cal.get(Calendar.SECOND));
        	String fileHead = dateTime + "\n" + participant + "\n" + "Touch Test";
        	
        	FileWriter fw = new FileWriter(Environment.getExternalStoragePublicDirectory
        			(Environment.DIRECTORY_DOWNLOADS).toString() + "/" + filename + ".txt", true);
	        fw.append( fileHead + "\n");
	        fw.close();
	        //Log.d("FileWriter","File was Created");
        } catch (Exception e) {
            Log.e("FileWriter","Did Not Create File " + filename );
        }
	}
	
	public void processTouchEvent(MotionEvent event)
	{
		float tx = event.getX();
		float ty = event.getY();
		
		if(touchEnabled){
			
			if(elementSelected)
			{
				if(elementNo > 0){
				//check if any of the buttons is pressed
				int buttonSelected = getPressedButton(tx, ty);
				switch(buttonSelected) //determine which level is selected
				{
				case 1:
					textureIndx = elementNo == 1 ? AG1 : PB1;
					if(currLevel != 1)
		    			addLogEntry();
					currLevel = 1;
					break;
				case 2: 
					textureIndx = elementNo == 1 ? AG2 : PB2;
					if(currLevel != 2)
		    			addLogEntry();
					currLevel = 2;
					break;
				case 3:
					textureIndx = elementNo == 1 ? AG3 : PB3;
					if(currLevel != 3)
		    			addLogEntry();
					currLevel = 3;
					break;
				case 4: 
					textureIndx = elementNo == 1 ? AG4 : PB4;
					if(currLevel != 4)
		    			addLogEntry();
					currLevel = 4;
					break;
				case 5:
					textureIndx = elementNo == 1 ? AG5 : PB5;
					if(currLevel != 5)
		    			addLogEntry();
					currLevel = 5;
					break;
				}
				}
			}
			else //check for element selection
			{
				if(IsPressed(ag, tx, ty)) //Ag is selected
				{
					elementNo = 1;
					currLevel = 1;
					// Add Selected Element to File
	    			try
	    	        {
	    	        	FileWriter fw = new FileWriter
	    	        			(Environment.getExternalStoragePublicDirectory
	    	        					(Environment.DIRECTORY_DOWNLOADS).toString() + "/" + filename + ".txt", true);
	    		        fw.append("Ag\n");
	    		        fw.append("-----\n");
	    		        fw.close();
	    		        //Log.d("FileWriter","File was Appended");
	    	        } catch (Exception e) {
	    	        	Log.e("FileWriter","Did Not Create File2");
	    	        }
					textureIndx = AG1;
					elementSelected = true;	
				}
				else
				{
					if(IsPressed(pb, tx, ty)) //Pb is selected
					{
						elementNo = 2;
						currLevel = 1;
						// Add Selected Element to File
		    			try
		    	        {
		    	        	FileWriter fw = new FileWriter
		    	        			(Environment.getExternalStoragePublicDirectory
		    	        					(Environment.DIRECTORY_DOWNLOADS).toString() + "/" + filename + ".txt", true);
		    		        fw.append("Pb\n");
		    		        fw.append("-----\n");
		    		        fw.close();
		    		        //Log.d("FileWriter","File was Appended");
		    	        } catch (Exception e) {
		    	        	Log.e("FileWriter","Did Not Create File2");
		    	        }
						textureIndx = PB1;
						elementSelected = true;
					}
				}
			}
			
			lastTouch = System.currentTimeMillis();
			touchEnabled = false;
		}
	}
	
	public int getPressedButton(float tx, float ty)
	{
		if(IsPressed(l1, tx, ty))
			return 1;
		if(IsPressed(l2, tx, ty))
			return 2;
		if(IsPressed(l3, tx, ty))
			return 3;
		if(IsPressed(l4, tx, ty))
			return 4;
		if(IsPressed(l5, tx, ty))
			return 5;
		
		return 0; //nothing is pressed
	}
	
	public boolean IsPressed(TouchCoords q, float tx, float ty)
	{		
		float l = q.l;
		float t = q.t;
		float r = q.r;
		float b = q.b;
		
		if(l<=tx && tx<=r && t<=ty && ty<=b)
			return true;
		
		return false;
	}
	
	public void SetupImage()
	{
		// The texture buffer
		ByteBuffer tb = ByteBuffer.allocateDirect(uvs.length * 4);
		tb.order(ByteOrder.nativeOrder());
		uvBuffer = tb.asFloatBuffer();
		uvBuffer.put(uvs);
		uvBuffer.position(0);	
		
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
	
	private void addLogEntry()
    {
    	try
        {
			// **** Stores Previous Level & End Time of Prev Level
			Calendar cal = Calendar.getInstance();
			String dateTime = cal.getTime().toString();
			String newLine = dateTime + ", " + "Touch" + ", " + participant + ", " + Integer.toString(currLevel) + "\n";
			
        	FileWriter fw = new FileWriter(Environment.getExternalStoragePublicDirectory
        			(Environment.DIRECTORY_DOWNLOADS).toString() + "/" + filename + ".txt", true);
	        fw.append(newLine);
	        fw.close();
        } catch (Exception e) {
        	Log.e("FileWriter","Did Not Create File2");
        }
    }
}
