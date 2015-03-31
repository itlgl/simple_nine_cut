package com.lgl.ninecut;

import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.widget.Toast;

public class NineCutActivity extends Activity {

	String path;
	Bitmap bitmap;
	File picFile;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_nine_cut);

		path = getIntent().getStringExtra("path");
		picFile = new File(path);
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(path, options);
		System.out.println("outWidth=" + options.outWidth);
		System.out.println("outHeight=" + options.outHeight);
		if (options.outWidth != options.outHeight) {
			Toast.makeText(this, "切割的图片必须是正方形的，先裁剪完再来切割吧", Toast.LENGTH_SHORT)
					.show();

			// String picName = path.substring(path.lastIndexOf("/"));
			// if (TextUtils.isEmpty(picName)) {
			// picName = "tem";
			// }
			// File fileDir = new File(Environment.getExternalStorageDirectory()
			// .getAbsolutePath() + "/nine_cut/" + picName);
			// if(!fileDir.exists()) {
			// fileDir.mkdirs();
			// }
			File fileDir = new File(Environment.getExternalStorageDirectory()
					.getAbsolutePath() + "/nine_cut");
			if (!fileDir.exists()) {
				fileDir.mkdirs();
			}
			picFile = new File(fileDir, "temp");

			Intent intent = new Intent("com.android.camera.action.CROP");
			intent.setData(Uri.fromFile(new File(path)));
			intent.putExtra("aspectX", 1);
			intent.putExtra("aspectY", 1);
			intent.putExtra("return-data", false);
			intent.putExtra("output", Uri.fromFile(picFile));
		}
	}
}
