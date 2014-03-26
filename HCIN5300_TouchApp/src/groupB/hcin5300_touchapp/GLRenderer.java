package groupB.hcin5300_touchapp;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
 
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Rect;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.view.Display;
import android.view.MotionEvent;
import android.view.WindowManager;

public class GLRenderer implements Renderer {
	
	// Our matrices
    private final float[] mtrxProjection = new float[16];
    private final float[] mtrxView = new float[16];
    private final float[] mtrxProjectionAndView = new float[16];
    
    int mPositionHandle;
    int mTexCoordLoc;
    int mtrxhandle;
    int mSamplerLoc;
 
    // Our screenresolution
    int mScreenWidth;
    int mScreenHeight;
 
    // Misc
    Context mContext;
    long mLastTime;
    int mProgram;
    
    //public Rect image;
    
    //level textures
    int currLevel = -1;
    int[] textures;
    int texCount = 6;
    
    // virtual button coordinates
    public Rect agC = new Rect(21, 33, 35, 19);
    public Rect pbC = new Rect(63, 18, 77, 3);
    
    public Rect l1C = new Rect(-119, -49, -91, -59);
    public Rect l2C = new Rect(-86, -49, -58, -59);
    public Rect l3C = new Rect(-53, -49, -25, -59);
    public Rect l4C = new Rect(57, -50, 85, -60);
    public Rect l5C = new Rect(91, -50, 119, -60);
    
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
    	
    	loadTextures();   	
    }
    
    public void initRendering()
    {
    	mPositionHandle = GLES20.glGetAttribLocation(riGraphicTools.sp_Image, "vPosition");
        mTexCoordLoc = GLES20.glGetAttribLocation(riGraphicTools.sp_Image, "a_texCoord" );
        mtrxhandle = GLES20.glGetUniformLocation(riGraphicTools.sp_Image, "uMVPMatrix");
        mSamplerLoc = GLES20.glGetUniformLocation (riGraphicTools.sp_Image, "s_texture" );
    }
    
    public void loadTextures()
    {
    	textures = new int[texCount];
    	textures[0] = mContext.getResources().getIdentifier
    			("drawable/periodic_table4", null, mContext.getPackageName());
    	textures[1] = mContext.getResources().getIdentifier
    			("drawable/ic_launcher", null, mContext.getPackageName());
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
 
        // Get the amount of time the last frame took.
        long elapsed = now - mLastTime;
 
        // Update our example
 
        // Render our example
        initRendering();
        Render(mtrxProjectionAndView);
 
        // Save the current time to see how long it took <img src="http://androidblog.reindustries.com/wp-includes/images/smilies/icon_smile.gif" alt=":)" class="wp-smiley"> .
        mLastTime = now;
 
    }
 
    private void Render(float[] m) {
 
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT); 
        GLES20.glUniformMatrix4fv(mtrxhandle, 1, false, m, 0);

        RenderImage();
    	RenderVirtualButtons();
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
 
        // Set the clear color to black
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1);
 
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
    
    public void RenderImage()
    {
    	RenderRectangle(0, mScreenHeight, mScreenWidth, 0, 0);
    }
    
    public void RenderVirtualButtons()
    {
    	RenderRectangle(0, mScreenHeight/2, mScreenWidth/2, 0, 1);
    }
        
    public void RenderRectangle(int l, int t, int r, int b, int id)
    {
    	Rect image = new Rect();	
    	image.left = l;
    	image.top = t;
    	image.right = r;
    	image.bottom = b;
    		
    	// We have to create the vertices of our triangle.
        float[] vertices = new float[]
        	{0.0f, t, 0.0f,
             0.0f, 0.0f, 0.0f,
             r, 0.0f, 0.0f,
             r, t, 0.0f,
           };
 
        short[] indices = new short[] {0, 1, 2, 0, 2, 3}; // The order of vertexrendering.
 
        // The vertex buffer.
        ByteBuffer bbv = ByteBuffer.allocateDirect(vertices.length * 4);
        bbv.order(ByteOrder.nativeOrder());
        FloatBuffer vertexBuffer = bbv.asFloatBuffer();
        vertexBuffer.put(vertices);
        vertexBuffer.position(0);
 
        // initialize byte buffer for the draw list
        ByteBuffer dlb = ByteBuffer.allocateDirect(indices.length * 2);
        dlb.order(ByteOrder.nativeOrder());
        ShortBuffer drawListBuffer = dlb.asShortBuffer();
        drawListBuffer.put(indices);
        drawListBuffer.position(0);
        
     // Create our UV coordinates.
        float[] uvs = new float[] {
                0.0f, 0.0f,
                0.0f, 1.0f,
                1.0f, 1.0f,
                1.0f, 0.0f
        };
 
        // The texture buffer
        ByteBuffer bb = ByteBuffer.allocateDirect(uvs.length * 4);
        bb.order(ByteOrder.nativeOrder());
        FloatBuffer uvBuffer = bb.asFloatBuffer();
        uvBuffer.put(uvs);
        uvBuffer.position(0);
     
        ChangeTexture(textures[id]);
    	
    	GLES20.glEnableVertexAttribArray(mPositionHandle);
        GLES20.glVertexAttribPointer(mPositionHandle, 3,
                                     GLES20.GL_FLOAT, false,
                                     0, vertexBuffer);       
        GLES20.glEnableVertexAttribArray ( mTexCoordLoc );
        GLES20.glVertexAttribPointer ( mTexCoordLoc, 2, GLES20.GL_FLOAT,
                false,
                0, uvBuffer);      
        GLES20.glUniform1i ( mSamplerLoc, 0);
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, indices.length,
                GLES20.GL_UNSIGNED_SHORT, drawListBuffer);
 
        GLES20.glDisableVertexAttribArray(mPositionHandle);
        GLES20.glDisableVertexAttribArray(mTexCoordLoc);
    }   
    
    public void ChangeTexture(int id)
    { 
        // Generate Textures, if more needed, alter these numbers.
        int[] texturenames = new int[1];
        GLES20.glGenTextures(1, texturenames, 0);
        
        // Temporary create a bitmap
        Bitmap bmp = BitmapFactory.decodeResource(mContext.getResources(), id);
 
        // Bind texture to texturename
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texturenames[0]);
 
        // Set filtering
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
 
        // Load the bitmap into the bound texture.
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bmp, 0);
 
        // We are done using the bitmap so we should recycle it.
        bmp.recycle();
    }
    
    public void TranslateSprite()
    {
//        vertices = new float[]
//            {image.left, image.top, 0.0f,
//        image.left, image.bottom, 0.0f,
//        image.right, image.bottom, 0.0f,
//        image.right, image.top, 0.0f,
//                               };
//        // The vertex buffer.
//        ByteBuffer bb = ByteBuffer.allocateDirect(vertices.length * 4);
//        bb.order(ByteOrder.nativeOrder());
//        vertexBuffer = bb.asFloatBuffer();
//        vertexBuffer.put(vertices);
//        vertexBuffer.position(0);
    }
    
    public void processTouchEvent(MotionEvent event)
    {
//        // Get the half of screen value
//        int screenhalf = (int) (mScreenWidth / 2);
//        if(event.getX()<screenhalf)
//        {
//            image.left -= 10;
//            image.right -= 10;
//        }
//        else
//        {
//            image.left += 10;
//            image.right += 10;
//        }
//     
//        // Update the new data.
//        TranslateSprite();
    }
}
