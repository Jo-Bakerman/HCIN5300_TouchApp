package groupB.hcin5300_touchapp.app;

import groupB.hcin5300_touchapp.utils.Vector3D;
import groupB.hcin5300_touchapp.utils.CubeShaders;
import groupB.hcin5300_touchapp.utils.SampleUtils;
import groupB.hcin5300_touchapp.utils.Texture;
import groupB.hcin5300_touchapp.utils.TexturedRectangle;

import java.util.Vector;
 
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.WindowManager;

public class GLRenderer implements GLSurfaceView.Renderer {
	
	private static final String LOGTAG = "GLRenderer";      
    public boolean mIsActive = false;    
    private MainActivity mActivity;   
    private Vector<Texture> mTextures;
    
    public boolean elementSelected = false;
    int elementIndex = -1;
    int currLevel = 0;
	
 // OpenGL ES 2.0 specific (3D model):
    private int shaderProgramID = 0;
    private int vertexHandle = 0;
    private int normalHandle = 0;
    private int textureCoordHandle = 0;
    private int mvpMatrixHandle = 0;
    private int texSampler2DHandle = 0;
 
    // Our screenresolution
    int mScreenWidth;
    int mScreenHeight;
 
    // virtual button coordinates
    public Rect agC = new Rect(21, 33, 35, 19);
    public Rect pbC = new Rect(63, 18, 77, 3);
    
    public Rect l1C = new Rect(-119, -49, -91, -59);
    public Rect l2C = new Rect(-86, -49, -58, -59);
    public Rect l3C = new Rect(-53, -49, -25, -59);
    public Rect l4C = new Rect(57, -50, 85, -60);
    public Rect l5C = new Rect(91, -50, 119, -60);
    
    public GLRenderer(MainActivity activity)
    {
        mActivity = activity;       
        
        WindowManager wm = (WindowManager) mActivity.getSystemService(Context.WINDOW_SERVICE);
    	Display display = wm.getDefaultDisplay();
    	Point size = new Point();
    	display.getSize(size);
    	mScreenWidth = size.x;
    	mScreenHeight = size.y;
    }
    
    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config)
    {
        Log.d(LOGTAG, "GLRenderer.onSurfaceCreated");
        
        // Call function to initialize rendering:
        initRendering();
    }
    
    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height)
    {
        Log.d(LOGTAG, "GLRenderer.onSurfaceChanged");
    }
    
    @Override
    public void onDrawFrame(GL10 gl)
    {
        if (!mIsActive)
            return;
        
        // Call our function to render content
        renderFrame();              
    }
    
    private void initRendering()
    {
        Log.d(LOGTAG, "VirtualButtonsRenderer.initRendering");
        
        // Define clear color
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        
        // Now generate the OpenGL texture objects and add settings
        for (Texture t : mTextures)
        {
            GLES20.glGenTextures(1, t.mTextureID, 0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, t.mTextureID[0]);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA,
                t.mWidth, t.mHeight, 0, GLES20.GL_RGBA,
                GLES20.GL_UNSIGNED_BYTE, t.mData);
        }
        
        shaderProgramID = SampleUtils.createProgramFromShaderSrc(
            CubeShaders.CUBE_MESH_VERTEX_SHADER,
            CubeShaders.CUBE_MESH_FRAGMENT_SHADER);
        
        vertexHandle = GLES20.glGetAttribLocation(shaderProgramID,
            "vertexPosition");
        normalHandle = GLES20.glGetAttribLocation(shaderProgramID,
            "vertexNormal");
        textureCoordHandle = GLES20.glGetAttribLocation(shaderProgramID,
            "vertexTexCoord");
        mvpMatrixHandle = GLES20.glGetUniformLocation(shaderProgramID,
            "modelViewProjectionMatrix");
        texSampler2DHandle = GLES20.glGetUniformLocation(shaderProgramID,
            "texSampler2D");            
    }
    
    private void renderFrame()
    {
        // Clear color and depth buffer
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        
        
        //GLES20.glEnable(GLES20.GL_CULL_FACE);
        //GLES20.glCullFace(GLES20.GL_BACK);      
        GLES20.glDisable(GLES20.GL_CULL_FACE);                   
        
        RenderBackground();
        if(!elementSelected)                  	
        	applyElementsFrame();        
    
        SampleUtils.checkGLError("GLRenderer renderFrame");

        GLES20.glDisable(GLES20.GL_BLEND);
        GLES20.glDisable(GLES20.GL_DEPTH_TEST);       
    }
    
    public void RenderBackground()
    {
    	TexturedRectangle meshObj = new TexturedRectangle();
		Texture meshTex = null;
		Vector3D tr = new Vector3D(0.0f, 0.0f, 0.0f);
		Vector3D sc = new Vector3D(mScreenWidth, mScreenHeight, 0.0f);
		
    	if(elementSelected) 
    		meshTex = mTextures.get(LoadLevelBackground());
    	
    	else //render the periodic table
	    	meshTex = mTextures.get(0);

    	//draw the background
    	StartDraw(tr, sc, meshObj, meshTex);
    }
    
    public void StartDraw(Vector3D tr, Vector3D sc, 
    		TexturedRectangle meshObj, Texture meshTex)
    {
        float[] modelViewMatrix = new float[16];
        
        final float eyeX = 0.0f;
        final float eyeY = 0.0f;
        final float eyeZ = 1.5f;
        final float lookX = 0.0f;
        final float lookY = 0.0f;
        final float lookZ = -5.0f;    
        final float upX = 0.0f;
        final float upY = 1.0f;
        final float upZ = 0.0f;
          
        Matrix.setLookAtM(modelViewMatrix, 0, eyeX, eyeY, eyeZ, 
        		lookX, lookY, lookZ, upX, upY, upZ);       
        Matrix.translateM(modelViewMatrix, 0, 
    				tr.x, tr.y, tr.z);       
        Matrix.scaleM(modelViewMatrix, 0, 
            		sc.x, sc.y, sc.z);   
            
        GLES20.glUseProgram(shaderProgramID);
        GLES20.glVertexAttribPointer(vertexHandle, 3, GLES20.GL_FLOAT,
                	false, 0, meshObj.getVertices());
        GLES20.glVertexAttribPointer(normalHandle, 3, GLES20.GL_FLOAT,
                	false, 0, meshObj.getNormals());
        GLES20.glVertexAttribPointer(textureCoordHandle, 2,
                	GLES20.GL_FLOAT, false, 0, meshObj.getTexCoords());
            
        GLES20.glEnableVertexAttribArray(vertexHandle);
        GLES20.glEnableVertexAttribArray(normalHandle);
        GLES20.glEnableVertexAttribArray(textureCoordHandle);
    	
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 
        		meshTex.mTextureID[0]);   	
    	
        GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false,
            modelViewMatrix, 0);
        GLES20.glUniform1i(texSampler2DHandle, 0);
        GLES20.glDrawElements(GLES20.GL_TRIANGLES,
        	6, GLES20.GL_UNSIGNED_SHORT,
            meshObj.getIndices());
    }
    
    public int LoadLevelBackground()
    {
    	int texIndx = 0;
    	switch(currLevel)
    	{
    	case 1: 
    		if(elementIndex == 0) //Ag  	
    		{
    			texIndx = 1;
    		}
    		else //Pb
    		{
    			texIndx = 6;
    		}  			
    		break;
    	case 2: 
    		if(elementIndex == 0) //Ag  	
    		{
    			texIndx = 2;
    		}
    		else //Pb
    		{
    			texIndx = 7;
    		}
    		break;
    	case 3: 
    		if(elementIndex == 0) //Ag  	
    		{
    			texIndx = 3;
    		}
    		else //Pb
    		{
    			texIndx = 8;
    		}
    		break;
    	case 4:
    		if(elementIndex == 0) //Ag
    		{
    			texIndx = 4;
    		}
    		else //Pb
    		{
    			texIndx = 9;
    		}
    		break;
    	case 5:
    		if(elementIndex == 0) //Ag
    		{
    			texIndx = 5;
    		}
    		else //Pb
    		{
    			texIndx = 10;
    		}
    		break;
    	}
    	return texIndx;
    }
    
    public void applyElementsFrame()
    {
    	TexturedRectangle meshObj1 = new TexturedRectangle();
    	TexturedRectangle meshObj2 = new TexturedRectangle();
		Texture meshTex = mTextures.get(1);
		Vector3D tr1 = new Vector3D(0.0f, 0.0f, 0.0f);
		Vector3D tr2 = new Vector3D(0.0f, 0.0f, 0.0f);
		Vector3D sc = new Vector3D(mScreenWidth, mScreenHeight, 0.0f);
		
		StartDraw(tr1, sc, meshObj1, meshTex);
		StartDraw(tr2, sc, meshObj2, meshTex);
    }
      
    public void setTextures(Vector<Texture> textures)
    {
        mTextures = textures;       
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
