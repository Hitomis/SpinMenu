# SpinMenu
[![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-SpinMenu-green.svg?style=true)](https://android-arsenal.com/details/1/4396)



轮盘样式的 Fragment 菜单选择控件。有没有很炫？

# Preview

<img src="preview/menu_cyclic.gif"/><img src="preview/menu_slop.gif"/>


# Usage

导入 smlibrary module, 或者直接拷贝 com.hitomi.smlibrary 包下所有 java 文件到您的项目中

布局文件中：

        <com.hitomi.smlibrary.SpinMenu
            android:id="@+id/spin_menu"
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            app:hint_text_color="#FFFFFF"
            app:hint_text_size="14sp"
            app:scale_ratio="0.36"
            tools:context="com.hitomi.spinmenu.MainActivity">

            <FrameLayout
                android:layout_width="match_parent"
                android:layout_height="match_parent"
                android:background="#333a4a"></FrameLayout>

        </com.hitomi.smlibrary.SpinMenu>

如果您觉得背景太空洞，可以在 SpinMenu 中嵌套其它布局，来绘制您自己的背景

Activity 中：

    spinMenu = (SpinMenu) findViewById(R.id.spin_menu);

    // 设置页面标题
    List<String> hintStrList = new ArrayList<>();
    hintStrList.add("热门信息");
    hintStrList.add("实时新闻");
    hintStrList.add("我的论坛");
    hintStrList.add("我的信息");
    hintStrList.add("走走看看");
    hintStrList.add("阅读空间");
    hintStrList.add("听听唱唱");
    hintStrList.add("系统设置");

    spinMenu.setHintTextStrList(hintStrList);
    spinMenu.setHintTextColor(Color.parseColor("#FFFFFF"));
    spinMenu.setHintTextSize(14);

    // 设置启动手势开启菜单
    spinMenu.setEnableGesture(true);

    // 设置页面适配器
    final List<Fragment> fragmentList = new ArrayList<>();
    fragmentList.add(Fragment1.newInstance());
    fragmentList.add(Fragment2.newInstance());
    fragmentList.add(Fragment3.newInstance());
    fragmentList.add(Fragment4.newInstance());
    fragmentList.add(Fragment5.newInstance());
    fragmentList.add(Fragment6.newInstance());
    fragmentList.add(Fragment7.newInstance());
    fragmentList.add(Fragment8.newInstance());
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

    // 设置菜单状态改变时的监听器
    spinMenu.setOnSpinMenuStateChangeListener(new OnSpinMenuStateChangeListener() {
        @Override
        public void onMenuOpened() {
            Toast.makeText(MainActivity.this, "SpinMenu opened", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onMenuClosed() {
            Toast.makeText(MainActivity.this, "SpinMenu closed", Toast.LENGTH_SHORT).show();
        }
    });

# Attributes

    <attr name="scale_ratio" format="float" />
    支持页面缩放大小自定义，建议取值在3.0到5.0之间

    <attr name="hint_text_color" format="color"/>
    支持页面标题文字颜色自定义

    <attr name="hint_text_size" format="dimension"/>
    支持页面标题文字大小自定义

#Licence

MIT
 


