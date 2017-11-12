package com.ebanswers.rxdownload;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;

import com.callanna.rxload.RxLoad;
import com.callanna.rxload.db.DownLoadStatus;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;

public class DownLoadActivity extends AppCompatActivity {
    private List<AppInfo> appInfos = new ArrayList<>();
    private RecyclerView recyclerView;
    private AppInfoAdapter appInfoAdapter;

    public static void start(Context context) {
        Intent intent = new Intent(context, DownLoadActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_down_load);
        recyclerView = (RecyclerView) findViewById(R.id.list_app);
        appInfoAdapter = new AppInfoAdapter(appInfos);
        recyclerView.setAdapter(appInfoAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        initData();
    }

    private void initData() {

        appInfos.add(new AppInfo("悦动圈", "http://f2.market.xiaomi.com/download/AppStore/0fdde45866fcb4d662db1f504b9bb482423d44d3f/com.yuedong.sport.apk", R.mipmap.a1));
        appInfos.add(new AppInfo("爱奇艺", "http://f1.market.xiaomi.com/download/AppStore/0dec385a7f95248ac3f2a63405581b9d5fae8c328/com.qiyi.video.apk", R.mipmap.a2));
        appInfos.add(new AppInfo("Keep", "http://f5.market.mi-img.com/download/AppStore/0b77c0475dc26490b288373bbdd83760e8938a92d/com.gotokeep.keep.apk", R.mipmap.a3));
        appInfos.add(new AppInfo("高德地图", "http://amapdownload.autonavi.com/down6/C021100011760/Amap_V8.1.6.2187_android_C021100011760_(Build1709051022).apk", R.mipmap.a4));
        appInfos.add(new AppInfo("ofo共享单车", "http://f4.market.xiaomi.com/download/AppStore/09992547581001a40bd7cc26e07ac13519042ef5d/so.ofo.labofo.apk", R.mipmap.a5));
        appInfos.add(new AppInfo("滴滴出行", "http://f1.market.xiaomi.com/download/AppStore/0739d545b94364e433299fa2a2ff3d82a12eff8bb/com.sdu.didi.psnger.apk", R.mipmap.a6));
        appInfos.add(new AppInfo("携程旅行", "http://f2.market.xiaomi.com/download/AppStore/0dcfb5a2525af70b00b9e9bd99a34182784412ccc/ctrip.android.view.apk", R.mipmap.a7));
        appInfos.add(new AppInfo("QQ", " http://f4.market.mi-img.com/download/AppStore/0ef745dfdef0157c80cd3eb64ecd36a9f17403341/com.tencent.mobileqq.apk", R.mipmap.a8));
        appInfos.add(new AppInfo("微信", " http://f4.market.mi-img.com/download/AppStore/065fc45210ea8e141fe49042b4cfd4480fe41af7f/com.tencent.mm.apk", R.mipmap.a9));
        appInfos.add(new AppInfo("微博", "http://f5.market.mi-img.com/download/AppStore/09baca46e8a13404a29cd051dbd83bf72ea6c4082/com.sina.weibo.apk", R.mipmap.a10));

    }


    public class AppInfoAdapter extends BaseQuickAdapter<AppInfo, BaseViewHolder> {

        public AppInfoAdapter(@Nullable List<AppInfo> data) {
            super(R.layout.item_appinfo, data);
        }

        @Override
        protected void convert(final BaseViewHolder helper, final AppInfo item) {
            helper.setText(R.id.tv_name, item.getName())
                    .setImageResource(R.id.imv_app, item.getImage())
                    .setProgress(R.id.progress, 0)
                    .setText(R.id.tv_progress_status, " ")
                    .setText(R.id.tv_progress_size, "");
            final Button btn_start = helper.getView(R.id.btn_start);
            btn_start.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (btn_start.getText().equals("继续")) {
                        RxLoad.getInstance().start(item.getUrl());
                    } else {
                        RxLoad.getInstance().pause(item.getUrl());
                    }
                }
            });
            final Button btn_delete = helper.getView(R.id.btn_delete);
            btn_delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (btn_delete.getText().equals("下载")) {
                        RxLoad.getInstance().download(item.getUrl());
                    } else {
                        RxLoad.getInstance().delete(item.getUrl());
                    }
                }
            });
            RxLoad.getInstance().getDownStatus(item.getUrl()).subscribe(new Consumer<DownLoadStatus>() {
                @Override
                public void accept(@NonNull DownLoadStatus downLoadStatus) throws Exception {
                    helper.setText(R.id.tv_progress_status, downLoadStatus.getStringStatus())
                            .setText(R.id.tv_progress_size, downLoadStatus.getFormatStatusString())
                            .setProgress(R.id.progress, (int) downLoadStatus.getPercentNumber());
                    switch (downLoadStatus.getStatus()) {
                        case DownLoadStatus.NORMAL:
                        case DownLoadStatus.PREPAREING:
                        case DownLoadStatus.WAITING:
                        case DownLoadStatus.STARTED:
                            helper.setText(R.id.btn_delete, "取消");
                            helper.setText(R.id.btn_start, "暂停");
                            helper.setVisible(R.id.btn_start, true);
                            break;
                        case DownLoadStatus.PAUSED:
                            helper.setVisible(R.id.btn_start, true);
                            helper.setText(R.id.btn_delete, "取消");
                            helper.setText(R.id.btn_start, "继续");
                            break;
                        case DownLoadStatus.COMPLETED:
                            helper.setVisible(R.id.btn_start, false);
                            helper.setVisible(R.id.btn_delete,false);
                            break;
                        case DownLoadStatus.FAILED:
                            helper.setVisible(R.id.btn_delete,true);
                            helper.setVisible(R.id.btn_start, false);
                            helper.setText(R.id.btn_delete, "下载");
                            break;
                    }
                }
            });
        }
    }
}
