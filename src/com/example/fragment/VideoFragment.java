package com.example.fragment;

import java.io.File;
import java.io.IOException;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import com.example.adapter.MyListAdapter;
import com.example.adapter.MyVideoAdapter;
import com.example.mediaactivity.R;
import com.example.msg.audioMsg;
import com.example.msg.videoMsg;

import android.R.integer;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract.CommonDataKinds.Relation;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout.LayoutParams;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.GestureDetector.OnDoubleTapListener;
import android.view.InputDevice;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnGenericMotionListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Toast;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class VideoFragment extends Fragment {

	private Button btprevious;
	private Button btplay;
	private Button btstop;
	private Button btnext;
	private Button btmute;
	private Button btmode;
	private SurfaceView sv_video;
	private SurfaceHolder surfaceHolder;
	private MediaPlayer mediaPlayer;
	private int currentPosition = 0;
	private String path;
	private int backPosition = 0;
	private int cpos = 0;
	private ListView vlv;
	private ArrayList<videoMsg> videoList = new ArrayList<videoMsg>();
	private AudioManager audioManager;
	private int currentSount;
	private TextView vvl;
	private SeekBar vpb;
	private TextView vnowtime;
	private TextView vtotaltime;
	private boolean mute;
	private LinearLayout lvba;
	private LinearLayout lvbu, llM;
	private ImageView sb;
	private int volume;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.video_activity, container, false);
		sv_video = (SurfaceView) view.findViewById(R.id.sv_video);
		surfaceHolder = sv_video.getHolder();
		surfaceHolder.addCallback(new Callback() {

			@Override
			public void surfaceCreated(SurfaceHolder holder) {
				mediaPlayer.setDisplay(sv_video.getHolder());
			}

			@Override
			public void surfaceChanged(SurfaceHolder holder, int format,
					int width, int height) {
			}

			@Override
			public void surfaceDestroyed(SurfaceHolder holder) {
				// 必须要释放掉 否则每次隐藏surface都有问题
				mediaPlayer.release();
				mediaPlayer = null;
			}
		});
		llM = (LinearLayout) getActivity().findViewById(R.id.linearLayout);
		sb = (ImageView) getActivity().findViewById(R.id.scrollbar);
		vlv = (ListView) view.findViewById(R.id.vlv);
		vlv.setAdapter(new MyVideoAdapter(getActivity(), getVideos()));
		vlv.setOnItemClickListener(new MyOnItemClik());
		init(view);
		initBroadcast();
		return view;
	}

	private void init(View view) {
		btstop = (Button) view.findViewById(R.id.vstop);
		btplay = (Button) view.findViewById(R.id.vplay);
		btnext = (Button) view.findViewById(R.id.vnext);
		btprevious = (Button) view.findViewById(R.id.vprevious);
		btmute = (Button) view.findViewById(R.id.vmute);
		btmode = (Button) view.findViewById(R.id.vmode);
		btmode.setOnClickListener(new MyOnClickListener());
		btplay.setOnClickListener(new MyOnClickListener());
		btnext.setOnClickListener(new MyOnClickListener());
		btprevious.setOnClickListener(new MyOnClickListener());
		btmute.setOnClickListener(new MyOnClickListener());
		btstop.setOnClickListener(new MyOnClickListener());
		vnowtime = (TextView) view.findViewById(R.id.vnowtime);
		vtotaltime = (TextView) view.findViewById(R.id.vtotaltime);
		vpb = (SeekBar) view.findViewById(R.id.vprogressbar);
		vpb.setOnSeekBarChangeListener(new MyProgressBarListener());
		vpb.setEnabled(false);
		lvba = (LinearLayout) view.findViewById(R.id.vll_bar);
		lvbu = (LinearLayout) view.findViewById(R.id.vll_button);
		vvl = (TextView) view.findViewById(R.id.vvolume);
		audioManager = (AudioManager) getActivity().getSystemService(
				getActivity().AUDIO_SERVICE);
		currentSount = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
		formatVolume(INIT);
		sv_video.setOnGenericMotionListener(new vScrollVolume());
		lvba.setOnGenericMotionListener(new vScrollVolume());
		lvbu.setOnGenericMotionListener(new vScrollVolume());
		sv_video.setOnTouchListener(new OnTouchListener() {

			private int sh;
			private int lh;
			private float DownX;
			private float DownY;
			private long currentMS;

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					DownX = event.getX();
					DownY = event.getY();
					currentMS = System.currentTimeMillis();
					currentPosition = mediaPlayer.getCurrentPosition();
					//每次重新开始循环
					handler2.removeCallbacks(runnable2);
					lvbu.setVisibility(View.VISIBLE);
					lvba.setVisibility(View.VISIBLE);
					sh = sv_video.getHeight();
					lh = lvbu.getHeight() + lvba.getHeight();
					break;

				case MotionEvent.ACTION_UP:
					handler2.postDelayed(runnable2, 5000);
					break;

				default:
					break;
				}
				return true;

			}
		});
	}

	public static final int INIT = 0; // 初始音量
	public static final int RAISE = 1;// 提升
	public static final int LOW = 2;// 降低

	public void formatVolume(int mode) {
		// 只取5-15之间到声音来设置百分比
		switch (mode) {
		case INIT:
			currentSount = audioManager
					.getStreamVolume(AudioManager.STREAM_MUSIC);
			if (currentSount > 5) {
				volume = (currentSount - 5) * 10;
				if (volume > 100) {
					vvl.setText("100%");
					volume = 100;
				} else {
					vvl.setText(volume + "%");
				}
			} else {
				vvl.setText("0%");
			}
			break;
		case RAISE:
			audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
					AudioManager.ADJUST_RAISE, 0);
			if (audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) <= 5) {
				audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 6, 0);
			}
			volume = volume + 10;
			if (volume >= 100) {
				vvl.setText("100%");
				volume = 100;
			} else {
				vvl.setText(volume + "%");
			}
			break;
		case LOW:
			audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
					AudioManager.ADJUST_LOWER, 0);
			if (audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) <= 5) {
				audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);
			}
			volume = volume - 10;
			if (volume <= 0) {
				vvl.setText("0%");
				volume = 0;
				audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);
			} else {
				vvl.setText(volume + "%");
			}
			break;
		default:
			break;
		}
	}

	public class vScrollVolume implements OnGenericMotionListener {

		@Override
		public boolean onGenericMotion(View v, MotionEvent event) {
			if (0 != (event.getSource() & InputDevice.SOURCE_CLASS_POINTER)) {
				switch (event.getAction()) {
				// process the scroll wheel movement...处理滚轮事件
				case MotionEvent.ACTION_SCROLL:
					audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
					btmute.setBackgroundResource(R.drawable.sound);
					mute = false;
					// 获得垂直坐标上的滚动方向,也就是滚轮向下滚
					if (event.getAxisValue(MotionEvent.AXIS_VSCROLL) < 0.0f) {
						formatVolume(LOW);
					}
					// 获得垂直坐标上的滚动方向,也就是滚轮向上滚
					else {
						formatVolume(RAISE);
					}
					return true;
				}
			}
			// switch (event.getButtonState()) {
			// case MotionEvent.BUTTON_PRIMARY:
			// screen(false);
			// return true;
			// }
			return false;
		}
	}

	/**
	 * 寻找音乐
	 * 
	 * */
	private String fPath = "mnt/sda/sda1"; // USB路径
	private boolean isplayed;
	private videoMsg videomsg;

	public ArrayList<videoMsg> getVideos() {
		// 递归寻找指定目录
		try {
			File path = new File(fPath);
			seekFiles(path);
		} catch (Exception e) {
			e.printStackTrace();
		}
		// 通过手机媒体中心寻找
		try {
			Cursor cursor = getActivity().getContentResolver().query(
					MediaStore.Video.Media.EXTERNAL_CONTENT_URI, null, null,
					null, null);
			cursor.moveToFirst();
			for (int i = 0; i < cursor.getCount(); i++) {
				String title = cursor.getString(cursor
						.getColumnIndexOrThrow(MediaStore.Video.Media.TITLE));
				String data = cursor.getString(cursor
						.getColumnIndex(MediaStore.Video.Media.DATA));
				videomsg = new videoMsg(title, data);
				videoList.add(videomsg);
				cursor.moveToNext();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		// 排序
		Collections.sort(videoList, new Comparator<videoMsg>() {
			@Override
			public int compare(videoMsg lhs, videoMsg rhs) {
				return lhs.getName().compareTo(rhs.getName());
			}

		});
		return videoList;
	}

	/**
	 * 递归搜索全部子文件夹
	 * 
	 * */
	public void seekFiles(File file) {
		try {
			// listfiles()列出所有的子文件夹的数组
			File[] files = file.listFiles();
			for (File f : files) {
				if (!f.isDirectory()) {
					// if (f.getName().contains(".mp3")) {
					if (getMimeType(f.getName()).equals("video")) {
						// 去除重复
						String name = f.getName().substring(0,
								(f.getName()).indexOf("."));
						videomsg = new videoMsg(name, f.getPath());
						videoList.add(videomsg);
					}
				} else {
					seekFiles(f);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/***
	 * 判断文件是音audio频或视频video
	 * 
	 * */
	public static String getMimeType(String fileName) {
		FileNameMap fileNameMap = URLConnection.getFileNameMap();
		String type = fileNameMap.getContentTypeFor(fileName);
		type = type.substring(0, type.indexOf("/"));
		return type;
	}

	public String getpath(int cpos) {
		return videoList.get(cpos).getPath();
	}

	public class MyOnItemClik implements OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			stopVideo();
			cpos = position;
			play(getpath(cpos));
		}

	}

	// private Boolean single = false;
	private int mode = 0;
	public static final int FINISH = 0; // 初始音量
	public static final int SINGLE = 1;// 提升
	public static final int LIST = 2;// 降低

	public class MyOnClickListener implements OnClickListener {

		private boolean pause;

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.vplay:
				if (mediaPlayer.isPlaying()) {
					pasueVideo();
					isplayed = false;
					pause = true;
				} else if (pause) {
					mediaPlayer.start();
					btplay.setBackgroundResource(R.drawable.pause);
					pause = false;
				} else {
					play(getpath(cpos));
				}
				break;
			case R.id.vstop:
				stopVideo();
				break;
			case R.id.vprevious:
				stopVideo();
				cpos = cpos - 1;
				if (cpos < 0) {
					cpos = 0;
					play(getpath(0));
				} else {
					play(getpath(cpos));
				}

				break;
			case R.id.vnext:
				stopVideo();
				cpos = cpos + 1;
				if (cpos >= videoList.size()) {
					cpos = 0;
					play(getpath(0));
				} else {
					play(getpath(cpos));
				}
				break;
			case R.id.vmute:
				if (!mute) {
					btmute.setBackgroundResource(R.drawable.mute);
					currentSount = audioManager
							.getStreamVolume(audioManager.STREAM_MUSIC);
					audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0,
							0);
					mute = true;
				} else {
					audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
							currentSount, 0);
					btmute.setBackgroundResource(R.drawable.sound);
					mute = false;
				}
				break;
			case R.id.vmode:
				if (mode == 0) {
					btmode.setText("单循");
					mode = SINGLE;
				} else if (mode == 1) {
					btmode.setText("顺序");
					mode = LIST;
				} else if (mode == 2) {
					btmode.setText("播停");
					mode = FINISH;
				}
				break;
			default:
				break;
			}
		}
	}

	public void stop() {
		stopVideo();
		// pasueVideo();
		screen(false);
	}

	public void screen(boolean isScreen) {
		// RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams)
		// sv_video
		// .getLayoutParams();
		if (isScreen) {
			llM.setVisibility(View.GONE);
			sb.setVisibility(View.GONE);
			vlv.setVisibility(View.GONE);
			lvbu.setVisibility(View.GONE);
			lvba.setVisibility(View.GONE);
			// hideNavigationBar();
			sv_video.setVisibility(View.VISIBLE);
			// lp.width = RelativeLayout.LayoutParams.MATCH_PARENT;
			// lp.height = RelativeLayout.LayoutParams.MATCH_PARENT;
			// sv_video.setLayoutParams(lp);
			getActivity().setRequestedOrientation(
					ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		} else {
			isplayed = false;
			llM.setVisibility(View.VISIBLE);
			sb.setVisibility(View.VISIBLE);
			vlv.setVisibility(View.VISIBLE);
			lvbu.setVisibility(View.GONE);
			lvba.setVisibility(View.GONE);
			sv_video.setVisibility(View.GONE);
			// lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, 0);
			// lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT, 0);
			// lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 0);
			// sv_video.setLayoutParams(lp);
			getActivity().setRequestedOrientation(
					ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		}
	}

	public void play(String path) {
		if (mediaPlayer == null) {
			mediaPlayer = new MediaPlayer();
			mediaPlayer.setOnErrorListener(new OnErrorListener() {

				@Override
				public boolean onError(MediaPlayer mp, int what, int extra) {
					mp.reset();
					return false;
				}
			});
			mediaPlayer.setOnCompletionListener(new MyOnCompletion());
		}
		mediaPlayer.reset();
		mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
		try {
			mediaPlayer.setDataSource(path);
		} catch (IllegalArgumentException | SecurityException
				| IllegalStateException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			mediaPlayer.prepare();
		} catch (IllegalStateException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		mediaPlayer.setOnPreparedListener(new OnPreparedListener() {

			@Override
			public void onPrepared(MediaPlayer mp) {
				// TODO Auto-generated method stub
				mediaPlayer.start();
			}
		});
		btplay.setBackgroundResource(R.drawable.pause);
		vpb.setEnabled(true);
		vtotaltime.setText(ShowTime(mediaPlayer.getDuration()));
		vlv.setItemChecked(cpos, true);
		vlv.setSelection(cpos);
		StrartbarUpdate();
		isplayed = true;
		screen(true);
	}

	public class MyOnCompletion implements OnCompletionListener {

		@Override
		public void onCompletion(MediaPlayer mp) {
			// stopVideo();
			if (mode == LIST) {
				cpos = cpos + 1;
				if (cpos < videoList.size()) {
					play(getpath(cpos));
				} else {
					cpos = 0;
					play(getpath(0));
				}
			} else if (mode == SINGLE) {
				play(getpath(cpos));

			} else if (mode == FINISH) {
				mediaPlayer.stop();
				isplayed = false;
				vpb.setProgress(0);
				vnowtime.setText((ShowTime(0)));
				handler.removeCallbacks(runnable);
				btplay.setBackgroundResource(R.drawable.play);
			}
		}
	}

	public void replay() {
		if (isplayed) {
			if (!mediaPlayer.isPlaying()) {
				path = getpath(cpos);
				mediaPlayer.start();
				btplay.setBackgroundResource(R.drawable.pause);
			}
		}
	}

	public void pasueVideo() {
		if (mediaPlayer != null && mediaPlayer.isPlaying()) {
			mediaPlayer.pause();
			btplay.setBackgroundResource(R.drawable.play);
		}

	}

	public void stopVideo() {
		if (mediaPlayer != null && mediaPlayer.isPlaying()) {
			mediaPlayer.stop();
			isplayed = false;
			vpb.setProgress(0);
			vnowtime.setText((ShowTime(0)));
			handler.removeCallbacks(runnable);
			btplay.setBackgroundResource(R.drawable.play);
		}
	}

	public void resetVideo() {
		mediaPlayer.seekTo(0);
		mediaPlayer.start();
	}

	/**
	 * 播放进度条
	 * 
	 * */
	public class MyProgressBarListener implements OnSeekBarChangeListener {

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {
			if (fromUser == true) {
				mediaPlayer.seekTo(progress);
				vnowtime.setText(ShowTime(progress));
			}
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub

		}
	}

	/**
	 * 时间转化
	 * 
	 * */
	public String ShowTime(int time) {
		time /= 1000;
		int minute = time / 60;
		int second = time % 60;
		minute %= 60;
		return String.format("%02d:%02d", minute, second);
	}

	/**
	 * 设置歌曲进度条
	 * 
	 * */
	Handler handler = new Handler();

	public void StrartbarUpdate() {
		handler.post(runnable);
	}

	Runnable runnable = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			int CurrentPosition = mediaPlayer.getCurrentPosition();
			vnowtime.setText(ShowTime(CurrentPosition));
			int mMax = mediaPlayer.getDuration();
			vpb.setMax(mMax);
			vpb.setProgress(CurrentPosition);
			handler.postDelayed(runnable, 100);
		}
	};
	/**
	 * 插拔USB广播监听
	 * 
	 * */
	private UsbStatesReceiver usbStatesReceiver;

	private void initBroadcast() {
		usbStatesReceiver = new UsbStatesReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_MEDIA_CHECKING);
		filter.addAction(Intent.ACTION_MEDIA_MOUNTED);
		filter.addAction(Intent.ACTION_MEDIA_EJECT);
		filter.addAction(Intent.ACTION_MEDIA_REMOVED);
		filter.addDataScheme("file");
		getActivity().registerReceiver(usbStatesReceiver, filter);
	}

	private class UsbStatesReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(Intent.ACTION_MEDIA_MOUNTED)
					|| intent.getAction().equals(Intent.ACTION_MEDIA_CHECKING)) {
				USBInsert();
			} else {
				USBOut();
			}
		}
	}

	private void USBInsert() {
		// 重新获取名称和路径
		vlv.setAdapter(new MyVideoAdapter(getActivity(), getVideos()));
		if (mediaPlayer == null) {
			mediaPlayer = new MediaPlayer();
			// mediaPlayer.setOnCompletionListener(new MyOnCompletion());
		}
		cpos = 0;
		vpb.setProgress(0);
		vnowtime.setText(ShowTime(0));
		vtotaltime.setText(ShowTime(0));
		handler.removeCallbacks(runnable);
	}

	private void USBOut() {
		if (mediaPlayer != null && mediaPlayer.isPlaying()) {
			mediaPlayer.stop();
			mediaPlayer.release();
			mediaPlayer = null;
		}
		vpb.setEnabled(false);
		vlv.setItemChecked(cpos, false);
		// 清空列表
		videoList.clear();
		btplay.setBackgroundResource(R.drawable.play);
		vpb.setProgress(0);
		vtotaltime.setText(ShowTime(0));
		vnowtime.setText(ShowTime(0));
		handler.removeCallbacks(runnable);
		screen(false);
	}

	Handler handler2 = new Handler();

	Runnable runnable2 = new Runnable() {

		@Override
		public void run() {
			lvbu.setVisibility(View.GONE);
			lvba.setVisibility(View.GONE);
			// hideNavigationBar();
			// handler2.post(runnable2);
		}
	};
}
