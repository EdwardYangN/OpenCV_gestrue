package com.nan.opencv_gestrue;

import java.util.ArrayList;
import java.util.List;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
public class MainActivity extends Activity implements CvCameraViewListener2 {
	private TextView tvRes;
	private Button bt1, bt2;
	private ImageView iv1, iv2;
	public Bitmap bitm1, bitm2;
	private CameraBridgeViewBase mCamera;
	public Mat mrgba,mrgbaROI;
	char mflag = 0;
	static final char BU = 222;

	private void mErrorReport(String str){
		Toast.makeText(this, "Come up error at:"+str, Toast.LENGTH_SHORT).show();
	}
	
	private BaseLoaderCallback mCallback = new BaseLoaderCallback(this) {

		@Override
		public void onManagerConnected(int status) {
			// TODO Auto-generated method stub
			super.onManagerConnected(status);
			switch (status) {
			case BaseLoaderCallback.SUCCESS:
				mCamera.enableView();

				break;
			default:
				break;
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.activity_main);

		init();
		mCamera.setCvCameraViewListener(this);
		mCamera.setCameraIndex(CameraBridgeViewBase.CAMERA_ID_FRONT);
		///按钮按下时改变消息类型
		bt1.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mflag = BU;
			}
		});

	}
//句柄/接收到消息后根据消息判断与预先存好的图片的相似度
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			switch (msg.what) {
			case BU:
				Mat mat1 = new Mat();
				Mat mat3 = new Mat();
				Mat mat2 = mrgba.clone();
				if (mat2 == null) { // 判断mat2是否为空

				} else if (mat2.channels() > 1) {
					Imgproc.cvtColor(mat2, mat2, Imgproc.COLOR_BGR2GRAY);
				} else { // 判断mat2是为二值图像
					Utils.bitmapToMat(bitm1, mat1);//将bitmap格式转为Mat
					Utils.bitmapToMat(bitm2, mat3);//将bitmap格式转为Mat
					
			//		Core.bitwise_not(mat1, mat1);					
					//对文件Mat转成灰度图
					Imgproc.cvtColor(mat1, mat1, Imgproc.COLOR_BGR2GRAY);
					Imgproc.cvtColor(mat3, mat3, Imgproc.COLOR_BGR2GRAY);
					//改变文件大小与摄像头大小一致（像素）
					try {

						Imgproc.resize(mat1, mat1,
								new Size(mrgba.rows(), mrgba.cols()));
						Imgproc.resize(mat3, mat3,
								new Size(mrgba.rows(), mrgba.cols()));
						Imgproc.resize(mat2, mat2,
								new Size(mrgba.rows(), mrgba.cols()));
					} catch (Exception e) {
						mErrorReport("Resize");
					}
				}
					//比较两个Mat类型相似度
				try {
					if(comPareHist(mat1, mat2)<comPareHist(mat3, mat2)){
						if(comPareHist(mat3, mat2)>=0.58 && comPareHist(mat3, mat2)!=1.0){
							tvRes.setText("石头 ");
						}else
							tvRes.setText("STONE:"+comPareHist(mat3, mat2));
					}else{
						if(comPareHist(mat1, mat2)>=0.58 && comPareHist(mat1, mat2)!=1.0){
							tvRes.setText("布");
						}else
							tvRes.setText("BU："+comPareHist(mat1, mat2));
					}
				} catch (Exception e) {
					tvRes.setText("NULL");
				}
					
				break;
			default:
				break;
			}
		}

	};
//初始化/实例化控件，读入预先处理过的图片
	public void init() {
		bt1 = (Button) findViewById(R.id.bt1);
		tvRes = (TextView) findViewById(R.id.tvRes);
		iv1 = (ImageView) findViewById(R.id.iv1);
		mCamera = (CameraBridgeViewBase) findViewById(R.id.mCameraView);
		bitm1 = BitmapFactory.decodeResource(getResources(), R.drawable.bux1);
		bitm2 = BitmapFactory.decodeResource(getResources(), R.drawable.stonex1);
		iv1.setImageBitmap(bitm1);
	}
//比较两个团MAT类型变量相似度的函数
	public double comPareHist(Mat src, Mat dst) {
		double target=0;
		try {
			Log.d("ERROR", "comPareHist_1");
			src.convertTo(src, CvType.CV_32F);
			dst.convertTo(dst, CvType.CV_32F);
			Log.d("ERROR", "comPareHist_1.1");
			// 比较两个Mat类型的变量的相似度。
			target = Imgproc.compareHist(src, dst, Imgproc.CV_COMP_CORREL);
		} catch (Exception e) {
			mErrorReport("compareHist");
		}
		
		Log.d("ERROR", "comPareHist_2");
		return target;
	}

	@Override
	protected void onResume() {
		super.onResume();
		OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_9, this,
				mCallback);
	}

	@Override
	protected void onPause() {
		super.onPause();
		Log.d("onPause","1");
		mflag=0;
		if (mCamera != null) {
			mrgba.release();
			mCamera.disableView();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mflag=0;
		Log.d("onDestroy","1");
		if (mCamera != null) {
			mrgba.release();
			mCamera.disableView();
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		Builder alert = new AlertDialog.Builder(MainActivity.this);
		alert.setTitle("Close Notice").setMessage("Exit the Aplication ?");
		alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				mflag = 0;
				mrgba.release();
				MainActivity.this.finish();
			}
		}).setNegativeButton("Cancel", null).show();
		
		return true;
	}
	
	@Override
	public void onCameraViewStarted(int width, int height) {
		// mrgba = new Mat(width,height,CvType.CV_32F);
	}

	@Override
	public void onCameraViewStopped() {
		mrgba.release();
	}

	@Override
	public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
		Mat mrgbaS = inputFrame.rgba();
		mrgbaROI = mrgbaS.clone();
		mrgba=mrgbaS.adjustROI(0, -(mrgbaS.rows()-300),  -(mrgbaS.cols()/2-150), -(mrgbaS.cols()/2-150));//感兴趣区域
		// 取出肤色
		Imgproc.cvtColor(mrgba, mrgba, Imgproc.COLOR_BGR2YCrCb);
		Core.inRange(mrgba, new Scalar(0, 90, 150), new Scalar(255, 125, 170),
				mrgba);	//二值化，取出肤色
		Imgproc.medianBlur(mrgba, mrgba, 5);	//中值滤波		
		Imgproc.GaussianBlur(mrgba, mrgba, new Size(9, 9), 0, 0);//高斯滤波
		
		//画出轮廓************************
		Mat mask = mrgba.clone();
		List<MatOfPoint> contoursList = new ArrayList<MatOfPoint>();
		Mat hierarchy = new Mat();
		//寻找轮廓点
		try {
			Imgproc.findContours(mask, contoursList, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
		} catch (Exception e) {
			mErrorReport("findContours");
		}
		
		int contoursSize = contoursList.size();
		//画出轮廓
		for(int i=0;i<contoursSize;i++){
			Imgproc.drawContours(mrgbaROI, contoursList, i, new Scalar(255, 255, 0),3);
		}
		
		// 发送消息去执行handler里的程序
		switch(mflag){
		case BU:
			Message mess1 = new Message();
			mess1.what = BU;
			mHandler.sendMessage(mess1);
			break;
		default:
			break;
		}
		Core.bitwise_not(mrgba, mrgba);//取反
		//画个方框
		Core.rectangle(mrgbaROI, new Point(234, 0), new Point(234+mrgba.cols(),mrgba.rows()), new Scalar(0, 255, 0));
		return mrgbaROI;
	}

}
