package com.example.activity;

import java.util.ArrayList;
import java.util.List;

import com.example.adapter.FragmentAdapter;
import com.example.fragment.AudioFragment;
import com.example.fragment.VideoFragment;
import com.example.mediaactivity.R;

import android.app.Activity;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.DisplayMetrics;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class MainActivity extends FragmentActivity {

	private TextView videoL;
	private TextView audioL;
	private ImageView imageview;
	private int bmpW;
	private int currIndex;
	private int offset;
	private ViewPager vp;
	private List<Fragment> fragments;
	private long firstTime;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 隐藏标题栏
		getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		// 隐藏手机状态栏
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_main);
		audioL = (TextView) findViewById(R.id.audiolayout);
		audioL.setOnClickListener(new MyOnClickListener());
		videoL = (TextView) findViewById(R.id.videolayout);
		videoL.setOnClickListener(new MyOnClickListener());
		audioL.setTextColor(getResources().getColor(R.color.choice_bg_color));
		init();
	}

	public void InitImage() {
		imageview = (ImageView) findViewById(R.id.scrollbar);
		// 获取图片宽度
		bmpW = BitmapFactory.decodeResource(getResources(), R.drawable.bar)
				.getWidth();
		// 获取屏幕宽度
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		int screenW = dm.widthPixels;
		// //让滚动条宽度为屏幕的一半
		// LinearLayout.LayoutParams layoutParams = new
		// LinearLayout.LayoutParams(
		// LinearLayout.LayoutParams.MATCH_PARENT,
		// LinearLayout.LayoutParams.WRAP_CONTENT);
		// layoutParams.width =screenW/2;
		// imageview.setLayoutParams(layoutParams);

		offset = (screenW / fragments.size() - bmpW) / 2;
		// imgageview设置平移，使下划线平移到初始位置（平移一个offset）
		Matrix matrix = new Matrix();
		matrix.postTranslate(offset, 0);
		imageview.setImageMatrix(matrix);
	}

	private void init() {
		fragments = new ArrayList<Fragment>();
		fragments.add(new AudioFragment());
		fragments.add(new VideoFragment());
		InitImage();
		FragmentAdapter mAdapter = new FragmentAdapter(
				getSupportFragmentManager(), fragments);
		vp = (ViewPager) findViewById(R.id.viewPager);
		vp.setAdapter(mAdapter);
		vp.setCurrentItem(0);
		vp.setOnPageChangeListener(new MyOnPageChangeListener());
	}

	public class MyOnPageChangeListener implements OnPageChangeListener {
		private int one = offset * 2 + bmpW;

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onPageScrollStateChanged(int arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onPageSelected(int arg0) {
			// TODO Auto-generated method stub
			Animation animation = new TranslateAnimation(currIndex * one, arg0
					* one, 0, 0);// 平移动画
			currIndex = arg0;
			stopandreplay(currIndex);
			animation.setFillAfter(true);// 动画终止时停留在最后一帧，不然会回到没有执行前的状态
			animation.setDuration(200);// 动画持续时间0.2秒
			imageview.startAnimation(animation);// 是用ImageView来显示动画的
		}
	}

	public class MyOnClickListener implements android.view.View.OnClickListener {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.audiolayout:
				vp.setCurrentItem(0);
				stopandreplay(0);
				break;
			case R.id.videolayout:
				vp.setCurrentItem(1);
				stopandreplay(1);
				break;
			default:
				break;
			}
		}
	}

	/**
	 * 切换页面时
	 * 
	 * */
	public void stopandreplay(int i) {
		switch (i) {
		case 0:
			((AudioFragment) fragments.get(0)).formatVolume(0);
			((AudioFragment) fragments.get(0)).replay();
			((VideoFragment) fragments.get(1)).pasueVideo();
			audioL.setTextColor(getResources()
					.getColor(R.color.choice_bg_color));
			videoL.setTextColor(getResources().getColor(R.color.black));
			break;
		case 1:
			((VideoFragment) fragments.get(1)).formatVolume(0);
			((VideoFragment) fragments.get(1)).replay();
			((AudioFragment) fragments.get(0)).pause();
			videoL.setTextColor(getResources()
					.getColor(R.color.choice_bg_color));
			audioL.setTextColor(getResources().getColor(R.color.black));

			break;
		default:
			break;
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {

		case KeyEvent.KEYCODE_VOLUME_DOWN:
			((AudioFragment) fragments.get(0)).formatVolume(2);
			((VideoFragment) fragments.get(1)).formatVolume(2);
			return true;

		case KeyEvent.KEYCODE_VOLUME_UP:
			((AudioFragment) fragments.get(0)).formatVolume(1);
			((VideoFragment) fragments.get(1)).formatVolume(1);
			return true;
		}
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			((VideoFragment) fragments.get(1)).stop();
			long secondTime = System.currentTimeMillis();
			if (secondTime - firstTime > 1000) {// 如果两次按键时间间隔大于1000毫秒，则不退出
				Toast.makeText(this, "再按一次返回键退出", Toast.LENGTH_SHORT).show();
				firstTime = secondTime;// 更新firstTime
				return true;
			} else {
				System.exit(0);// 否则退出程序
			}

			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	@Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus && Build.VERSION.SDK_INT >= 19) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }
}
