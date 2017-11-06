package com.callanna.demo;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.callanna.demo.fragment.Simple2Fragment;
import com.callanna.demo.fragment.Simple3Fragment;
import com.callanna.demo.fragment.Simple4Fragment;
import com.callanna.demo.fragment.Simple5Fragment;
import com.callanna.demo.fragment.Simple6Fragment;
import com.callanna.demo.fragment.Simple7Fragment;
import com.callanna.demo.fragment.Simple8Fragment;
import com.callanna.demo.fragment.SimpleFragment;
import com.cvlib.indicator.MagicIndicator;
import com.cvlib.indicator.ViewPagerHelper;
import com.cvlib.indicator.buildins.commonnavigator.CommonNavigator;
import com.cvlib.indicator.buildins.commonnavigator.abs.CommonNavigatorAdapter;
import com.cvlib.indicator.buildins.commonnavigator.abs.IPagerIndicator;
import com.cvlib.indicator.buildins.commonnavigator.abs.IPagerTitleView;
import com.cvlib.indicator.buildins.commonnavigator.indicators.WrapPagerIndicator;
import com.cvlib.indicator.buildins.commonnavigator.titles.SimplePagerTitleView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OperatorsActivity extends AppCompatActivity {
    private static final String[] CHANNELS = new String[]{"厨师与顾客","Schduler调度器", "模式一", "模式二", "模式三,四,五", "模式六","变换操作符","过滤操作符"};
    private List<String> mDataList = Arrays.asList(CHANNELS);
    private OperatorsPagerAdapter mPagerAdapter = new OperatorsPagerAdapter();

    private ViewPager mViewPager;
    public static void start(Context context){
        Intent intent = new Intent(context,OperatorsActivity.class);
        context.startActivity(intent);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_operators);

        mViewPager = (ViewPager) findViewById(R.id.view_pager);
        mViewPager.setAdapter(mPagerAdapter);
        initData();
        initMagicIndicator();
    }

    private void initData() {
        List<Fragment> data = new ArrayList<>();
        data.add(SimpleFragment.newInstance());
        data.add(Simple2Fragment.newInstance());
        data.add(Simple3Fragment.newInstance());
        data.add(Simple4Fragment.newInstance());
        data.add(Simple5Fragment.newInstance());
        data.add(Simple6Fragment.newInstance());
        data.add(Simple7Fragment.newInstance());
        data.add(Simple8Fragment.newInstance());
        mPagerAdapter.setFragments(data);
    }


    private void initMagicIndicator() {
        MagicIndicator magicIndicator = (MagicIndicator) findViewById(R.id.magic_indicator);
        magicIndicator.setBackgroundColor(Color.WHITE);
        CommonNavigator commonNavigator = new CommonNavigator(this);
        commonNavigator.setScrollPivotX(0.35f);
        commonNavigator.setAdapter(new CommonNavigatorAdapter() {
            @Override
            public int getCount() {
                return mDataList == null ? 0 : mDataList.size();
            }

            @Override
            public IPagerTitleView getTitleView(Context context, final int index) {
                SimplePagerTitleView simplePagerTitleView = new SimplePagerTitleView(context);
                simplePagerTitleView.setText(mDataList.get(index));
                simplePagerTitleView.setNormalColor(Color.parseColor("#333333"));
                simplePagerTitleView.setSelectedColor(Color.parseColor("#e94220"));
                simplePagerTitleView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mViewPager.setCurrentItem(index);
                    }
                });
                return simplePagerTitleView;
            }

            @Override
            public IPagerIndicator getIndicator(Context context) {
                WrapPagerIndicator indicator = new WrapPagerIndicator(context);
                indicator.setFillColor(Color.parseColor("#ebe4e3"));
                return indicator;
            }
        });
        magicIndicator.setNavigator(commonNavigator);
        ViewPagerHelper.bind(magicIndicator, mViewPager);
    }

    class OperatorsPagerAdapter extends FragmentStatePagerAdapter {
        private List<Fragment> fragments = new ArrayList<>();
        public OperatorsPagerAdapter() {
            super(OperatorsActivity.this.getSupportFragmentManager());
        }

        public void setFragments(List<Fragment> fragments) {
            this.fragments.addAll(fragments);
            notifyDataSetChanged();
        }
        public void addFragments( Fragment  fragment ) {
            this.fragments.add (fragment );
            notifyDataSetChanged();
        }
        public OperatorsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }
    }
}
