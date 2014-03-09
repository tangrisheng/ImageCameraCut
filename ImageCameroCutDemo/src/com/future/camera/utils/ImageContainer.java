package com.future.camera.utils;

import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Map;
import android.graphics.Bitmap;

/**
 * 
 *  Class Name: ImageContainer
 *  Function:
 *  
 *     Modifications:   
 *  
 *  @author tianqingsong  DateTime 2012-8-31 下午3:59:37    
 *  @version 1.0
 */
public class ImageContainer {
	private Map<String, SoftReference<Bitmap>> localImageCache = new HashMap<String, SoftReference<Bitmap>>();
	private static ImageContainer mImageContainer = null;

	static public ImageContainer instance() {
		if (null == mImageContainer) {
			mImageContainer = new ImageContainer();
		}

		return mImageContainer;
	}
	
	public void putBitmap2Container(String key,Bitmap bmp) {
		if (key == null || bmp == null) {
			return;
		}
		
		localImageCache.put(key,new SoftReference<Bitmap>(bmp));
	}
	
	public Bitmap getBitmapFromContainer(String key) {
		if (key == null) {
			return null;
		}
		
		SoftReference<Bitmap> softReference = localImageCache.get(key);
		return softReference.get();
	} 

	public void removeBitmapFromContainer(String key) {
		localImageCache.remove(key);
		return;
	}
}
