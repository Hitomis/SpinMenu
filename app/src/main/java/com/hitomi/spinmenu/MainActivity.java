package com.hitomi.spinmenu;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v7.app.AppCompatActivity;

import com.hitomi.smlibrary.SpinMenu;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private SpinMenu spinMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        spinMenu = (SpinMenu) findViewById(R.id.spin_menu);

        final List<Fragment> fragmentList = new ArrayList<>();
        fragmentList.add(Fragment1.newInstance());
        fragmentList.add(Fragment2.newInstance());
        fragmentList.add(Fragment3.newInstance());
        fragmentList.add(Fragment4.newInstance());
        fragmentList.add(Fragment5.newInstance());
        fragmentList.add(Fragment6.newInstance());

        FragmentPagerAdapter fragmentPagerAdapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                return fragmentList.get(position);
            }

            @Override
            public int getCount() {
                return fragmentList.size();
            }
        };
        spinMenu.setFragmentAdapter(fragmentPagerAdapter);

        List<String> hintStrList = new ArrayList<>();
        hintStrList.add("热门信息");
        hintStrList.add("实时新闻");
        hintStrList.add("我的论坛");
        hintStrList.add("走走看看");
        hintStrList.add("我的信息");
        hintStrList.add("系统设置");
        spinMenu.setHintTextList(hintStrList);
    }
}
