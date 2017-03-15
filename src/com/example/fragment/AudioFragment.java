package com.example.fragment;

import java.io.File;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import com.example.adapter.MyListAdapter;
import com.example.mediaactivity.R;
import com.example.msg.audioMsg;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.drm.DrmStore.Playback;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.view.InputDevice;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnGenericMotionListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class AudioFragment extends Fragment {
	private ArrayList<audioMsg> audioList = new ArrayList<audioMsg>();
	private MediaPlayer mediaPlayer;
	private int currentPos = 0;
	private ListView lv;
	private AudioManager audioManager;
	private SeekBar pb;
	// private SeekBar vb;
	private Button btnprevious, btnplay, btnnext, btnmute, btnmode, btnstop;
	private TextView nowtime;
	private TextView totaltime;
	private int currentSount;
	private int randomPos;
	private boolean mute = false;
	private TextView vl;
	private int volume;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.audio_activity, container, false);
		lv = (ListView) view.findViewById(R.id.lv);
		pb = (SeekBar) view.findViewById(R.id.progressbar);
		// vb = (SeekBar) view.findViewById(R.id.volumebar);
		InitButton(view);
		lv.setAdapter(new MyListAdapter(getActivity(), getMusics()));
		lv.setOnItemClickListener(new MyOnItemClik());
		mediaPlayer = new MediaPlayer();
		mediaPlayer.setOnCompletionListener(new MyOnCompletionListener());
		Initseekbar();
		initBroadcast();
		LinearLayout lba = (LinearLayout) view.findViewById(R.id.ll_bar);
		LinearLayout lbu = (LinearLayout) view.findViewById(R.id.ll_button);
		lba.setOnGenericMotionListener(new ScrollVolume());
		lbu.setOnGenericMotionListener(new ScrollVolume());
		return view;
	}

	public MediaPlayer getmMediaPlayer() {
		return mediaPlayer;
	}

	private void InitButton(View view) {
		// TODO Auto-generated method stub
		btnprevious = (Button) view.findViewById(R.id.previous);
		btnplay = (Button) view.findViewById(R.id.play);
		btnstop = (Button) view.findViewById(R.id.stop);
		btnnext = (Button) view.findViewById(R.id.next);
		btnmute = (Button) view.findViewById(R.id.mute);
		btnmode = (Button) view.findViewById(R.id.mode);
		nowtime = (TextView) view.findViewById(R.id.nowtime);
		totaltime = (TextView) view.findViewById(R.id.totaltime);
		vl = (TextView) view.findViewById(R.id.volume);
		btnprevious.setOnClickListener(new MyOnClickListener());
		btnplay.setOnClickListener(new MyOnClickListener());
		btnstop.setOnClickListener(new MyOnClickListener());
		btnnext.setOnClickListener(new MyOnClickListener());
		btnmute.setOnClickListener(new MyOnClickListener());
		btnmode.setOnClickListener(new MyOnClickListener());
	}

	/**
	 * ����������
	 * 
	 * */
	public void Initseekbar() {
		audioManager = (AudioManager) getActivity().getSystemService(
				getActivity().AUDIO_SERVICE);
		// int MaxSound = audioManager
		// .getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		// vb.setMax(MaxSound);
		// vb.setProgress(currentSount);
		pb.setOnSeekBarChangeListener(new MyProgressBarListener());
		// vb.setOnSeekBarChangeListener(new MyVolumeBarListener());
		pb.setEnabled(false);
		formatVolume(INIT);
	}

	public static final int INIT = 0; // ��ʼ����
	public static final int RAISE = 1;// ����
	public static final int LOW = 2;// ����

	/**
	 * 
	 * ���������óɰٷֱ�ģʽ
	 * */
	public void formatVolume(int mode) {
		// ֻȡ5-15֮�䵽���������ðٷֱ�
		switch (mode) {
		case INIT:
			currentSount = audioManager
					.getStreamVolume(AudioManager.STREAM_MUSIC);
			if (currentSount > 5) {
				volume = (currentSount - 5) * 10;
				if (volume > 100) {
					vl.setText("100%");
					volume = 100;
				} else {
					vl.setText(volume + "%");
				}
			} else {
				vl.setText("0%");
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
				vl.setText("100%");
				volume = 100;
			} else {
				vl.setText(volume + "%");
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
				vl.setText("0%");
				volume = 0;
				audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);
			} else {
				vl.setText(volume + "%");
			}
			break;
		default:
			break;
		}
	}

	/**
	 * Ѱ������
	 * 
	 * */
	private String fPath = "mnt/sda/sda1"; // USB·��
	private boolean isplayed;
	private audioMsg audioMsg;

	public ArrayList<audioMsg> getMusics() {
		// �ݹ�Ѱ��ָ��Ŀ¼
		try {
			File path = new File(fPath);
			seekFiles(path);
		} catch (Exception e) {
			e.printStackTrace();
		}
		// ͨ���ֻ�ý������Ѱ��
		try {
			Cursor cursor = getActivity().getContentResolver().query(
					MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null,
					null, null);
			cursor.moveToFirst();
			for (int i = 0; i < cursor.getCount(); i++) {
				String title = cursor.getString(cursor
						.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
				String data = cursor.getString(cursor
						.getColumnIndex(MediaStore.Audio.Media.DATA));
				audioMsg = new audioMsg(title, data);
				audioList.add(audioMsg);
				cursor.moveToNext();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		// ����
		Collections.sort(audioList, new Comparator<audioMsg>() {
			@Override
			public int compare(audioMsg lhs, audioMsg rhs) {
				return lhs.getName().compareTo(rhs.getName());
			}

		});
		return audioList;
	}

	/**
	 * �ݹ�����ȫ�����ļ���
	 * 
	 * */
	public void seekFiles(File file) {
		try {
			// listfiles()�г����е����ļ��е�����
			File[] files = file.listFiles();
			for (File f : files) {
				if (!f.isDirectory()) {
					// if (f.getName().contains(".mp3")) {
					if (getMimeType(f.getName()).equals("audio")) {
						// ȥ���ظ�
						String name = f.getName().substring(0,
								f.getName().indexOf("."));
						audioMsg = new audioMsg(name, f.getPath());
						audioList.add(audioMsg);
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
	 * �ж��ļ�����audioƵ����Ƶvideo
	 * 
	 * */
	public static String getMimeType(String fileName) {
		FileNameMap fileNameMap = URLConnection.getFileNameMap();
		String type = fileNameMap.getContentTypeFor(fileName);
		type = type.substring(0, type.indexOf("/"));
		return type;
	}

	/**
	 * ���Ÿ���
	 * 
	 * */
	public void Play(int pos) {
		try {
			mediaPlayer.reset();
			mediaPlayer.setDataSource(audioList.get(pos).getPath());
			mediaPlayer.prepare();
			mediaPlayer.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
		totaltime.setText(ShowTime(mediaPlayer.getDuration()));
		pb.setEnabled(true);
		btnplay.setBackgroundResource(R.drawable.pause);
		lv.setItemChecked(pos, true);
		lv.setSelection(pos);
		StrartbarUpdate();
		isplayed = true;
	}

	public static final int ALL = 0;
	public static final int SINGLE = 1;
	public static final int RANDOM = 2;
	private int mode = ALL;

	/**
	 * ����������ϲ�����һ��
	 * 
	 * **/
	public class MyOnCompletionListener implements OnCompletionListener {
		@Override
		public void onCompletion(MediaPlayer mp) {
			if (mode == ALL) {
				if (currentPos < audioList.size() - 1) {
					currentPos = currentPos + 1;
					Play(currentPos);
				} else {
					currentPos = 0;
					Play(currentPos);
				}
			} else if (mode == SINGLE) {
				Play(currentPos);
			} else if (mode == RANDOM) {
				randomPos = (int) (Math.random() * audioList.size());
				randomCheck(randomPos);
			}
		}
	}

	/**
	 * ��������¼�
	 * 
	 * **/
	public class MyOnItemClik implements OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			if (!mediaPlayer.isPlaying()) {
				Play(position);
				currentPos = position;
			} else
			// {
			// mediaPlayer.stop();
			if (currentPos != position) {
				Play(position);
				currentPos = position;
			}
			// }
		}
	}

	/**
	 * ���Ž�����
	 * 
	 * */
	public class MyProgressBarListener implements OnSeekBarChangeListener {

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {
			if (fromUser == true) {
				if (!mediaPlayer.isPlaying()) {
					Play(currentPos);
				}
				mediaPlayer.seekTo(progress);
				nowtime.setText(ShowTime(progress));
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
	 * ������
	 * 
	 * */
	public class MyVolumeBarListener implements OnSeekBarChangeListener {

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {
			if (fromUser) {
				int SeekPosition = seekBar.getProgress();
				audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
						SeekPosition, 0);
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
	 * ��������¼�
	 * 
	 * */
	private boolean pause;

	public class MyOnClickListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.previous:
				if (mode == ALL || mode == SINGLE) {
					if (currentPos > 0) {
						currentPos = currentPos - 1;
						Play(currentPos);
					} else {
						currentPos = 0;
						Play(currentPos);
					}
				} else if (mode == RANDOM) {
					randomPos = (int) (Math.random() * audioList.size());
					randomCheck(randomPos);
				}
				break;
			case R.id.play:
				if (mediaPlayer.isPlaying()) {
					mediaPlayer.pause();
					isplayed = false;
					pause = true;
					btnplay.setBackgroundResource(R.drawable.play);
				} else if (pause) {
					mediaPlayer.start();
					btnplay.setBackgroundResource(R.drawable.pause);
					pause = false;
				} else if (!mediaPlayer.isPlaying()) {
					Play(currentPos);
				}
				break;
			case R.id.stop:
				isplayed = false;
				stop();
				break;
			case R.id.next:
				if (mode == ALL || mode == SINGLE) {
					if (currentPos < audioList.size() - 1) {
						currentPos = currentPos + 1;
						Play(currentPos);
					} else {
						currentPos = 0;
						Play(currentPos);
					}
				} else if (mode == RANDOM) {
					randomPos = (int) (Math.random() * audioList.size());
					randomCheck(randomPos);
				}
				break;
			case R.id.mute:
				if (!mute) {
					btnmute.setBackgroundResource(R.drawable.mute);
					currentSount = audioManager
							.getStreamVolume(audioManager.STREAM_MUSIC);
					audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0,
							0);
					// vb.setProgress(0);
					mute = true;
				} else {
					btnmute.setBackgroundResource(R.drawable.sound);
					audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
							currentSount, 0);
					// vb.setProgress(currentSount);
					mute = false;
				}
				break;
			case R.id.mode:
				if (mode == ALL) {
					mode = SINGLE;
					btnmode.setText("��ѭ");
				} else if (mode == SINGLE) {
					mode = RANDOM;
					btnmode.setText("���");
				} else if (mode == RANDOM) {
					mode = ALL;
					btnmode.setText("˳��");
				}
				break;
			default:
				break;
			}

		}
	}

	public void replay() {
		if (isplayed) {
			if (!mediaPlayer.isPlaying()) {
				mediaPlayer.start();
				btnplay.setBackgroundResource(R.drawable.pause);
				pause = false;
			}
		}
	}

	public void pause() {
		if (mediaPlayer.isPlaying()) {
			mediaPlayer.pause();
			pause = true;
			btnplay.setBackgroundResource(R.drawable.play);
		}
	}

	public void stop() {
		// TODO Auto-generated method stub
		if (mediaPlayer.isPlaying()) {
			mediaPlayer.stop();
		}
		btnplay.setBackgroundResource(R.drawable.play);
		pb.setProgress(0);
		nowtime.setText(ShowTime(0));
		handler.removeCallbacks(runnable);
	}

	/**
	 * ���������
	 * 
	 * */
	public void randomCheck(int randomPos) {
		if (randomPos == currentPos) {
			randomPos = (int) (Math.random() * audioList.size());
			randomCheck(randomPos);
		} else {
			Play(randomPos);
			currentPos = randomPos;
		}
	}

	/**
	 * ʱ��ת��
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
	 * ���ø���������
	 * 
	 * */
	Handler handler = new Handler();

	public void StrartbarUpdate() {
		if (mediaPlayer != null) {
			handler.post(runnable);
		}
	}

	Runnable runnable = new Runnable() {
		@Override
		public void run() {
			// TODO Auto-generated method stub
			int CurrentPosition = mediaPlayer.getCurrentPosition();
			nowtime.setText(ShowTime(CurrentPosition));
			int mMax = mediaPlayer.getDuration();
			pb.setMax(mMax);
			pb.setProgress(CurrentPosition);
			handler.postDelayed(runnable, 100);
		}
	};

	/**
	 * ���USB�㲥����
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
		lv.setAdapter(new MyListAdapter(getActivity(), getMusics()));
		if (mediaPlayer == null) {
			mediaPlayer = new MediaPlayer();
			mediaPlayer.setOnCompletionListener(new MyOnCompletionListener());
		}
		currentPos = 0;
		pb.setProgress(0);
		nowtime.setText(ShowTime(0));
		totaltime.setText(ShowTime(0));
		handler.removeCallbacks(runnable);
	}

	private void USBOut() {
		if (mediaPlayer.isPlaying()) {
			mediaPlayer.stop();
		}
		mediaPlayer.release();
		mediaPlayer = null;
		pb.setEnabled(false);
		lv.setItemChecked(currentPos, false);
		// �б����
		audioList.clear();
		btnplay.setBackgroundResource(R.drawable.play);
		pb.setProgress(0);
		totaltime.setText(ShowTime(0));
		nowtime.setText(ShowTime(0));
		handler.removeCallbacks(runnable);
	}

	/**
	 * �ֻ�����������
	 * 
	 * */
	public class ScrollVolume implements OnGenericMotionListener {

		@Override
		public boolean onGenericMotion(View v, MotionEvent event) {
			if (0 != (event.getSource() & InputDevice.SOURCE_CLASS_POINTER)) {
				switch (event.getAction()) {
				// process the scroll wheel movement...��������¼�
				case MotionEvent.ACTION_SCROLL:
					btnmute.setBackgroundResource(R.drawable.sound);
					audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
					mute = false;
					// ��ô�ֱ�����ϵĹ�������,Ҳ���ǹ������¹�
					if (event.getAxisValue(MotionEvent.AXIS_VSCROLL) < 0.0f) {
						formatVolume(LOW);
					}
					// ��ô�ֱ�����ϵĹ�������,Ҳ���ǹ������Ϲ�
					else {
						formatVolume(RAISE);
					}
					return true;
				}
			}
			return false;
		}
	}

	@Override
	public void onDestroy() {
		if (mediaPlayer.isPlaying()) {
			mediaPlayer.stop();
		}
		mediaPlayer.release();
		mediaPlayer = null;
		super.onDestroy();
	}
}
