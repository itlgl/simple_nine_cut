package com.lgl.ninecut;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	private TextView tv_noimg;
	private GridView gv_img;
	private ImageAdapter adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		initView();
	}

	private void initView() {
		tv_noimg = (TextView) findViewById(R.id.iv_noimg);
		gv_img = (GridView) findViewById(R.id.gv_img);

		gv_img.setFastScrollEnabled(true);
		adapter = new ImageAdapter(this);
		gv_img.setAdapter(adapter);
		gv_img.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				checkPic(adapter.getItem(position));
			}
		});
	}

	private File picFile;

	private void checkPic(String path) {
		picFile = new File(path);
		if(!picFile.exists()) {
			Toast.makeText(this, "图片不存在", Toast.LENGTH_SHORT).show();
			return;
		}
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(path, options);
		System.out.println("outWidth=" + options.outWidth);
		System.out.println("outHeight=" + options.outHeight);
		if (options.outWidth != options.outHeight) {
			Toast.makeText(this, "切割的图片必须是正方形的，先裁剪完再来切割吧", Toast.LENGTH_SHORT)
					.show();

			File fileDir = new File(Environment.getExternalStorageDirectory()
					.getAbsolutePath() + "/nine_cut");
			if (!fileDir.exists()) {
				fileDir.mkdirs();
			}
			picFile = new File(fileDir, "temp");

			Intent intent = new Intent("com.android.camera.action.CROP");
			intent.setDataAndType(Uri.fromFile(new File(path)), "image/*");
//			intent.setData(Uri.fromFile(new File(path)));
			intent.putExtra("crop", "true");
			intent.putExtra("aspectX", 1);
			intent.putExtra("aspectY", 1);
			intent.putExtra("return-data", false);
			intent.putExtra("output", Uri.fromFile(picFile));
			startActivityForResult(intent, 100);
		} else {
			cutPic(picFile);
		}
	}

	private void cutPic(final File picture) {
		new Thread() {
			public void run() {
				Bitmap srcBitmap = BitmapFactory.decodeFile(picture
						.getAbsolutePath());
				int width = srcBitmap.getWidth();
				int height = srcBitmap.getHeight();
				if (width < height) {
					height = width;
				} else {
					width = height;
				}
				Bitmap[] nineBitmap = new Bitmap[9];
				int pieceWidth = width / 3;
				int pieceHeight = height / 3;
				for (int i = 0; i < 3; i++) {
					for (int j = 0; j < 3; j++) {
						int x = j * pieceWidth;
						int y = i * pieceHeight;
						nineBitmap[i * 3 + j] = Bitmap.createBitmap(srcBitmap,
								x, y, pieceWidth, pieceHeight);
					}
				}

				// 把九张图片存到指定的路径
				final File fileDir = new File(Environment
						.getExternalStorageDirectory().getAbsolutePath()
						+ "/nine_cut/" + UUID.randomUUID());
				if (!fileDir.exists()) {
					fileDir.mkdirs();
				}
				for (int i = 8; i >= 0; i--) {
					String fileName = (i + 1) + ".png";
					File pic = new File(fileDir, fileName);
					try {
						FileOutputStream fos = new FileOutputStream(pic);
						nineBitmap[i].compress(CompressFormat.PNG, 100, fos);
						nineBitmap[i].recycle();
						fos.flush();
						fos.close();
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}

					// 将图片加入到图库
					try {
						MediaStore.Images.Media.insertImage(
								MainActivity.this.getContentResolver(),
								pic.getAbsolutePath(), fileName, null);
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					}
				}
				MainActivity.this.sendBroadcast(new Intent(
						Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri
								.fromFile(fileDir)));

				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						Toast.makeText(MainActivity.this,
								"图片已经存到" + fileDir.getAbsolutePath() + "下",
								Toast.LENGTH_SHORT).show();
					}
				});
			}
		}.start();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == 100 && resultCode == RESULT_OK) {
			cutPic(picFile);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		loadImage();
	}

	private void loadImage() {
		new Thread() {
			public void run() {
				adapter.addAll(Util.getGalleryPhotos(MainActivity.this));
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						adapter.notifyDataSetChanged();
						if (adapter.isEmpty()) {
							tv_noimg.setVisibility(View.VISIBLE);
						} else {
							tv_noimg.setVisibility(View.GONE);
							// String s = "file://" + adapter.getItem(0);
							// ImageLoader.getInstance().displayImage(s,
							// iv_icon);
							// ImageLoader.getInstance().displayImage(s,
							// iv_bottom);
						}
					}
				});
			}
		}.start();
	}
}
