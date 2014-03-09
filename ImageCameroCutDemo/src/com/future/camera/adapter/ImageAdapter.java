/**
 * @(#)ImageAdapter.java   DateTime 2012-8-28 下午3:56:10 
 *
 * Copyright(c) 2012-later China future network
 * All rights reserved
 */
package com.future.camera.adapter;

import java.util.List;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;


 /**
 *  Class Name: ImageAdapter.java
 *  Function:
 *  
 *     Modifications:   
 *  
 *  @author tianqingsong  DateTime 2012-8-28 下午3:56:10    
 *  @version 1.0 
 */
public class ImageAdapter extends BaseAdapter {
	
	private List<Bitmap> mList;
	private Context mCtx;
	public ImageAdapter(Context ctx,List<Bitmap> list) {
		this.mCtx = ctx;
		mList = list;
	}

	/* (non-Javadoc)
	 * @see android.widget.Adapter#getCount()
	 */
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		if (mList == null) {
			return 0;
		}
		return mList.size();
	}

	/* (non-Javadoc)
	 * @see android.widget.Adapter#getItem(int)
	 */
	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		if (mList == null) {
			return null;
		}
		return mList.get(position);
	}

	/* (non-Javadoc)
	 * @see android.widget.Adapter#getItemId(int)
	 */
	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	/* (non-Javadoc)
	 * @see android.widget.Adapter#getView(int, android.view.View, android.view.ViewGroup)
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		if (mList == null) {
			return null;
		}
		ImageView localImageView = new ImageView(mCtx);
		localImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
		localImageView.setLayoutParams(new Gallery.LayoutParams(Gallery.LayoutParams.MATCH_PARENT, Gallery.LayoutParams.MATCH_PARENT));
		localImageView.setImageBitmap(mList.get(position));
		return localImageView;
	}
	
	public void notifyDataChanged(List<Bitmap> list) {
		mList = list;
		notifyDataSetChanged();
	}

}
