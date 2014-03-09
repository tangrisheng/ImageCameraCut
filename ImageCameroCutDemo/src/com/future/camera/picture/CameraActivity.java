/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.future.camera.picture;
import com.future.camera.R;
import com.future.camera.utils.ImageContainer;
import com.future.camera.utils.Util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.view.View;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

// ----------------------------------------------------------------------

public class CameraActivity extends Activity implements View.OnClickListener{
    private Preview mPreview;
    //准备一个Bitmap对象
    private Bitmap mBitmap = null; 
    private FrameLayout mPreviewLayout = null;
    private Button mCaptureBtn = null;
    Camera mCamera;
    Size mOptimalSize = null;
    @Override
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Hide the window title.
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        this.setContentView(R.layout.camera_layout);
        mPreviewLayout = (FrameLayout)findViewById(R.id.preview_layout);
        mCaptureBtn = (Button)findViewById(R.id.capture_btn);
        mCaptureBtn.setOnClickListener(this);
        
        mCamera = Camera.open();
        Camera.Parameters parameters=mCamera.getParameters();
        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        parameters.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
        parameters.set("rotation", 90);
        parameters.set("orientation", "portrait");
        parameters.setPictureFormat(PixelFormat.JPEG);  
        mCamera.setDisplayOrientation(90);
        mCamera.setParameters(parameters);
        // Create our Preview view and set it as the content of our activity.
        mPreview = new Preview(this);
        mPreviewLayout.addView(mPreview);

        //setContentView(mPreview);
    }
  //准备一个保存图片的PictureCallback对象
    public Camera.PictureCallback pictureCallback = new Camera.PictureCallback() {

            public void onPictureTaken(byte[] data, Camera camera) {
                    Log.i("yao","onPictureTaken");
                    if (data == null) {
                    	return;
                    }
                    
                    BitmapFactory.Options  options = new BitmapFactory.Options();
                    options.inJustDecodeBounds = false;
                    options.outWidth = (int)(mOptimalSize.width * 0.5f);
                    options.outWidth = (int)(mOptimalSize.height * 0.5f);
                    mBitmap = BitmapFactory.decodeByteArray(data, 0, data.length,options);
                    mBitmap = Util.rotate(mBitmap, 90);

                    String image_name = new SimpleDateFormat("yyyymmddhhMMssSSS").format(new Date());
                    ImageContainer.instance().putBitmap2Container(image_name, mBitmap);
                    Intent intent = new Intent();
                    intent.putExtra("image_name", image_name);
                    CameraActivity.this.setResult(Activity.RESULT_OK, intent);
                    CameraActivity.this.finish();
            }
            
            

    };
	
	 /* (non-Javadoc)
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 */
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.capture_btn:
			mCamera.takePicture(null, null, pictureCallback);
			break;
		default:
			break;
		}
	}
	
	// ----------------------------------------------------------------------

	class Preview extends SurfaceView implements SurfaceHolder.Callback {
	    SurfaceHolder mHolder;

	    Preview(Context context) {
	        super(context);

	        // Install a SurfaceHolder.Callback so we get notified when the
	        // underlying surface is created and destroyed.
	        mHolder = getHolder();
	        mHolder.addCallback(this);
	        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	    }

	    public void surfaceCreated(SurfaceHolder holder) {
	        // The Surface has been created, acquire the camera and tell it where
	        // to draw.
	        try {
	            mCamera.setPreviewDisplay(holder);
	        } catch (IOException exception) {
	            mCamera.release();
	            mCamera = null;
	            // TODO: add more exception handling logic here
	        }
	    }

	    public void surfaceDestroyed(SurfaceHolder holder) {
	        // Surface will be destroyed when we return, so stop the preview.
	        // Because the CameraDevice object is not a shared resource, it's very
	        // important to release it when the activity is paused.
	        mCamera.stopPreview();
	        mCamera.release();
	        mCamera = null;
	    }


	    private Size getOptimalPreviewSize(List<Size> sizes, int w, int h) {
	        final double ASPECT_TOLERANCE = 0.05;
	        double targetRatio = (double) w / h;
	        if (sizes == null) return null;

	        Size optimalSize = null;
	        double minDiff = Double.MAX_VALUE;

	        int targetHeight = h;

	        // Try to find an size match aspect ratio and size
	        for (Size size : sizes) {
	            double ratio = (double) size.width / size.height;
	            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
	            if (Math.abs(size.height - targetHeight) < minDiff) {
	                optimalSize = size;
	                minDiff = Math.abs(size.height - targetHeight);
	            }
	        }

	        // Cannot find the one match the aspect ratio, ignore the requirement
	        if (optimalSize == null) {
	            minDiff = Double.MAX_VALUE;
	            for (Size size : sizes) {
	                if (Math.abs(size.height - targetHeight) < minDiff) {
	                    optimalSize = size;
	                    minDiff = Math.abs(size.height - targetHeight);
	                }
	            }
	        }
	        return optimalSize;
	    }

	    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
	        // Now that the size is known, set up the camera parameters and begin
	        // the preview.
	        Camera.Parameters parameters = mCamera.getParameters();

	        List<Size> sizes = parameters.getSupportedPreviewSizes();
	        mOptimalSize = getOptimalPreviewSize(sizes, w, h);
	        parameters.setPreviewSize(mOptimalSize.width, mOptimalSize.height);

	        mCamera.setParameters(parameters);
	        mCamera.startPreview();
	    }

	}
}





