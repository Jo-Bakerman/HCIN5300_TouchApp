package groupB.hcin5300_touchapp.app;

import java.util.Vector;

import groupB.hcin5300_touchapp.R.id;
import groupB.hcin5300_touchapp.R.layout;
import groupB.hcin5300_touchapp.SampleAppMenu.SampleAppMenu;
import groupB.hcin5300_touchapp.SampleAppMenu.SampleAppMenuInterface;
import groupB.hcin5300_touchapp.R;
import groupB.hcin5300_touchapp.SampleAppMenu.SampleAppMenuGroup;
import groupB.hcin5300_touchapp.utils.Texture;
import groupB.hcin5300_touchapp.utils.LoadingDialogHandler;
import groupB.hcin5300_touchapp.utils.SampleApplicationGLView;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class MainActivity extends Activity {
	
	private static final String LOGTAG = "MainActivity";

	// Our OpenGL view:
    private SampleApplicationGLView mGlView;
    // Our renderer:
    private GLRenderer mRenderer;
    
    private RelativeLayout mUILayout;
    private LoadingDialogHandler loadingDialogHandler = new LoadingDialogHandler(
            this);
    // The textures we will use for rendering:
    private Vector<Texture> mTextures;
    boolean mIsDroidDevice = false;
    
 
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        Log.d(LOGTAG, "onCreate");
        super.onCreate(savedInstanceState);
        
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        startLoadingAnimation();
        // Load any sample specific textures:
        mTextures = new Vector<Texture>();
        loadTextures();

        mIsDroidDevice = android.os.Build.MODEL.toLowerCase().startsWith(
            "droid");    
        
        initApplication();
    }
    
    private void loadTextures()
    {
    	mTextures.add(Texture.loadTextureFromApk("periodic_table4.jpg",
                getAssets())); // 0
    	
    	//Ag
//    	mTextures.add(Texture.loadTextureFromApk("periodic_table4.jpg",
//                getAssets())); // 1
//    	
//    	mTextures.add(Texture.loadTextureFromApk("periodic_table4.jpg",
//                getAssets())); // 2
//    	
//    	mTextures.add(Texture.loadTextureFromApk("periodic_table4.jpg",
//                getAssets())); // 3
//    	
//    	mTextures.add(Texture.loadTextureFromApk("periodic_table4.jpg",
//                getAssets())); // 4
//    	
//    	mTextures.add(Texture.loadTextureFromApk("periodic_table4.jpg",
//                getAssets())); // 5
    	
    	//Pb
//    	mTextures.add(Texture.loadTextureFromApk("periodic_table4.jpg",
//                getAssets())); // 6
//    	
//    	mTextures.add(Texture.loadTextureFromApk("periodic_table4.jpg",
//                getAssets())); // 7
//    	
//    	mTextures.add(Texture.loadTextureFromApk("periodic_table4.jpg",
//                getAssets())); // 8
//    	
//    	mTextures.add(Texture.loadTextureFromApk("periodic_table4.jpg",
//                getAssets())); // 9
//    	
//    	mTextures.add(Texture.loadTextureFromApk("periodic_table4.jpg",
//                getAssets())); // 10
        
    	
        mTextures.add(Texture.loadTextureFromApk("selection.png",
                getAssets())); // 11
    }
    
    protected void onResume()
    {
        Log.d(LOGTAG, "onResume");
        super.onResume(); 

        // Resume the GL view:
        if (mGlView != null)
        {
            mGlView.setVisibility(View.VISIBLE);
            mGlView.onResume();
        }    
    }
    
    @Override
    public void onConfigurationChanged(Configuration config)
    {
        Log.d(LOGTAG, "onConfigurationChanged");
        super.onConfigurationChanged(config);
    }
    
    @Override
    protected void onPause()
    {
        Log.d(LOGTAG, "onPause");
        
        // finish will reset the app when home button is pressed
        finish();
        
        super.onPause();
    }
    
    @Override
    protected void onDestroy()
    {
        Log.d(LOGTAG, "onDestroy");
        super.onDestroy();           
        
        // Unload texture:
        mTextures.clear();
        mTextures = null;
        
        System.gc();
    }
    
    private void startLoadingAnimation()
    {
        LayoutInflater inflater = LayoutInflater.from(this);
        mUILayout = (RelativeLayout) inflater.inflate(R.layout.camera_overlay,
            null, false);
        
        mUILayout.setVisibility(View.VISIBLE);
        mUILayout.setBackgroundColor(Color.BLACK);
        
        // Gets a reference to the loading dialog
        loadingDialogHandler.mLoadingDialogContainer = mUILayout
            .findViewById(R.id.loading_indicator);
        
        // Shows the loading indicator at start
        loadingDialogHandler
            .sendEmptyMessage(LoadingDialogHandler.SHOW_LOADING_DIALOG);
        
        // Adds the inflated layout to the view
        addContentView(mUILayout, new LayoutParams(LayoutParams.MATCH_PARENT,
            LayoutParams.MATCH_PARENT));
    }
    
    private void initApplication()
    {
        // Create OpenGL ES view:
        int depthSize = 16;
        int stencilSize = 0;
        boolean translucent = true;
        
        mGlView = new SampleApplicationGLView(this);
        mGlView.init(translucent, depthSize, stencilSize);
        
        mRenderer = new GLRenderer(this);
        mRenderer.setTextures(mTextures);
        mGlView.setRenderer(mRenderer);
        
        mRenderer.mIsActive = true;
        
        // Now add the GL surface view. It is important
        // that the OpenGL ES surface view gets added
        // BEFORE the camera is started and video
        // background is configured.
        addContentView(mGlView, new LayoutParams(LayoutParams.MATCH_PARENT,
            LayoutParams.MATCH_PARENT));
        
        // Sets the UILayout to be drawn in front of the camera
        mUILayout.bringToFront();
        
        // Hides the Loading Dialog
        loadingDialogHandler
            .sendEmptyMessage(LoadingDialogHandler.HIDE_LOADING_DIALOG);
        
        // Sets the layout background to transparent
        mUILayout.setBackgroundColor(Color.TRANSPARENT);

    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
    	mRenderer.processTouchEvent(event);
        return true;
    }
       
    private void showToast(String text)
    {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

}
