package com.example.adapter;

import java.util.List;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class FragmentAdapter extends FragmentPagerAdapter {
	private List<Fragment> fragments;

	public FragmentAdapter(FragmentManager fm, List<Fragment> list) {
		super(fm);
		this.fragments = list;
	}

	public Fragment getItem(int fragment) {
		return fragments.get(fragment);
	}

	public int getCount() {
		return fragments.size();
	}

}
