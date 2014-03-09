package com.future.camera.ui;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.future.camera.adapter.ImageAdapter;
import com.future.camera.picture.CameraActivity;
import com.future.camera.picture.CropImageActivity;
import com.future.camera.utils.ImageContainer;
import com.future.camera.R;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

public class MainActivity extends Activity implements AdapterView.OnItemSelectedListener,
		AdapterView.OnItemClickListener,View.OnClickListener {

	private static final String TAG = "MainActivity";
	public static final int NONE = 0;
	public static final int CAPTURE_PICTURE = 1;// 调用相机拍照
	public static final int PICK_PICTURE = 2; // 从相册中选择图片
	public static final int RESULT_PICTURE = 3;// 剪切返回结果

	private final static int MENU_PICK_PIC = Menu.FIRST;
	private final static int MENU_CAPTURE_PIC = Menu.FIRST + 1;

	public static final String IMAGE_UNSPECIFIED = "image/*";
	private Gallery topGallery;
	private RadioGroup mRadioGroup;
	private ImageView mImageDisplayIv;
	private TextView mTipsTv;
	private List<Bitmap> mTopList;
	private ImageAdapter mImageAdapter;
	private int mIndex = 0;
	
	public static final String SD_IMAGES_PATH = "/sdcard/com.future.handlepicture/images/";
	public static final String DATA_IMAGES_PATH = "/data/data/com.future.handlepicture/images/";
	
	private String mImageDir = null;
	private String mImageName = null;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		init();
		loadData();
		mImageAdapter = new ImageAdapter(this, mTopList);
		topGallery.setAdapter(mImageAdapter);
	}

	private void init() {
		mTopList = new ArrayList<Bitmap>();
		mTipsTv = (TextView)findViewById(R.id.tips_tv);
		mTipsTv.setVisibility(View.VISIBLE);
		mRadioGroup = (RadioGroup)findViewById(R.id.radioGroup);
		mImageDisplayIv = (ImageView)findViewById(R.id.display_image);
		mImageDisplayIv.setOnClickListener(this);
		topGallery = (Gallery) findViewById(R.id.top_gallery);
		topGallery.setOnItemSelectedListener(this);
		topGallery.setOnItemClickListener(this);
	}

	private void loadData() {

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != Activity.RESULT_OK)
			return;
		
		switch (requestCode) {
		case CAPTURE_PICTURE:
			// 设置文件保存路径这里放在跟目录下
			//File picture = new File(Environment.getExternalStorageDirectory() + "/" + mImageName);
//			File picture = new File(mImageDir+ mImageName);
//			startPhotoZoom(Uri.fromFile(picture));
			String image_name = data.getStringExtra("image_name");
			cutPicture(image_name);
			break;
		case PICK_PICTURE:
			if (data == null) {
				return;
			}
			
			ContentResolver resolver = getContentResolver();
			Bitmap bmp = null;
			try {
				Uri uri = data.getData();
				bmp = MediaStore.Images.Media.getBitmap(resolver,uri);
				
				if (bmp != null) {
					ImageContainer.instance().putBitmap2Container(uri.toString(), bmp);
					cutPicture(uri.toString());
				}
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
			//startPhotoZoom(data.getData());
			break;
		case RESULT_PICTURE:
			//Bundle extras = data.getExtras();
			String result_image_name = data.getStringExtra("result_image_name");
			if (result_image_name != null) {
				if (mTipsTv.isShown()) {
					mTipsTv.setVisibility(View.GONE);
				}
				
				if (!topGallery.isShown()) {
					topGallery.setVisibility(View.VISIBLE);
				}
				
				if (!mRadioGroup.isShown()) {
					mRadioGroup.setVisibility(View.VISIBLE);
				}
				
				Bitmap photo = ImageContainer.instance().getBitmapFromContainer(result_image_name);
				ByteArrayOutputStream stream = new ByteArrayOutputStream();
				photo.compress(Bitmap.CompressFormat.JPEG, 75, stream);
				mTopList.add(photo);
				mImageAdapter.notifyDataSetChanged();
				RadioButton radioButton = new RadioButton(this);
	            radioButton.setId(mIndex);
	            radioButton.setClickable(false);
	            radioButton.setBackgroundResource(R.drawable.one_selector);
	            radioButton.setButtonDrawable(android.R.color.transparent);
	            mRadioGroup.addView(radioButton,mIndex);
	            mIndex++;
			}
			break;
		}

		super.onActivityResult(requestCode, resultCode, data);
	}

	public void startPhotoZoom(Uri uri) {
		Intent intent = new Intent("com.android.camera.action.CROP");
		intent.setDataAndType(uri, IMAGE_UNSPECIFIED);
		intent.putExtra("crop", "true");
		// aspectX aspectY 是宽高的比例
		intent.putExtra("aspectX", 1);
		intent.putExtra("aspectY", 1);
		// outputX outputY 是裁剪图片宽高
		intent.putExtra("outputX", 250);
		intent.putExtra("outputY", 250);
		intent.putExtra("return-data", true);
		startActivityForResult(intent, RESULT_PICTURE);
	}
	
	private void cutPicture(String key) {
		Intent intent = new Intent(this,CropImageActivity.class);
		intent.putExtra("image_key", key);
		startActivityForResult(intent, RESULT_PICTURE);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// 第一个参数：组ID 　第二个参数：菜单项ID　第三个参数：顺序号　第四个参数：菜单项上显示的内容
		menu.add(0, MENU_PICK_PIC, 0, "相册");
		menu.add(0, MENU_CAPTURE_PIC, 0, "拍照");
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_PICK_PIC:
			pickPicture();
			break;
		case MENU_CAPTURE_PIC:
			capturePicture();
			break;
		default:
			break;
		}
		return true;
	}

	private void pickPicture() {
//		Intent intent = new Intent(Intent.ACTION_PICK, null);
//		intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
//				IMAGE_UNSPECIFIED);
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.setType(IMAGE_UNSPECIFIED);
		startActivityForResult(intent, PICK_PICTURE);
	}

	private void capturePicture() {
		mImageDir = getImageDir();
		mImageName = getImageName();
//		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//		intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(Environment.getExternalStorageDirectory(), mImageName)));
		Intent intent = new Intent(this,CameraActivity.class);
		intent.putExtra("image_dir", mImageDir);
		intent.putExtra("image_name",  mImageName);
		startActivityForResult(intent, CAPTURE_PICTURE);
	}

	private String getImageDir() {
		String storagePath = null;
		String sdStatus = Environment.getExternalStorageState();
		if (sdStatus.equals(Environment.MEDIA_MOUNTED)) { // 检测sd是否可用
			storagePath = SD_IMAGES_PATH;
		} else {
			storagePath = DATA_IMAGES_PATH;
		}
		
		return storagePath;
	}
	
	private String getImageName() {
		return new SimpleDateFormat("yyyymmddhhMMssSSS").format(new Date()) + ".png";
	}
	
	@Override
	public void onItemSelected( AdapterView<?> adapter, View view,
	int position, long id) {
		// TODO Auto-generated method stub
		Log.v(TAG,"onItemSelected position = " + position + ",id = " + id);
		mRadioGroup.check(position);
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub

	}

	
	 /* (non-Javadoc)
	 * @see android.widget.AdapterView.OnItemClickListener#onItemClick(android.widget.AdapterView, android.view.View, int, long)
	 */
	@Override
	public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
		// TODO Auto-generated method stub
		mImageDisplayIv.setImageBitmap(mTopList.get(position));
		mImageDisplayIv.setVisibility(View.VISIBLE);
	}

	
	 /* (non-Javadoc)
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 */
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if (mImageDisplayIv.isShown()) {
			mImageDisplayIv.setVisibility(View.GONE);
		}
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		if (mImageDisplayIv.isShown()) {
			mImageDisplayIv.setVisibility(View.GONE);
			return;
		}
		super.onBackPressed();
	}
	
	
}