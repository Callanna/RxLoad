package com.callanna.demo.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.callanna.demo.R;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

import static io.reactivex.Observable.create;

/**
 * 三个情况 ，看看RxJava观察者，被观察者执行任务的线程Schedulers
 */
public class Simple2Fragment extends Fragment {
    Button btn_smaple1,btn_smaple2,btn_smaple3,btn_smaple4,btn_smaple5;
    TextView tx_console;
    public static Simple2Fragment newInstance( ) {
        Simple2Fragment fragment = new Simple2Fragment();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
       View view = inflater.inflate(R.layout.fragment_simple2, container, false);

        btn_smaple1 =  view.findViewById(R.id.btn_act1);
        btn_smaple2 =  view.findViewById(R.id.btn_act2);
        btn_smaple3 =  view.findViewById(R.id.btn_act3);
        btn_smaple4 =  view.findViewById(R.id.btn_act4);
        btn_smaple5 =  view.findViewById(R.id.btn_act5);
        tx_console =   view.findViewById(R.id.tv_console);


        btn_smaple1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doAct1();
            }
        });


        btn_smaple2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doAct2();
            }
        });
        btn_smaple3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doAct3();
            }
        });

        btn_smaple4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doAct4();
            }
        });

        btn_smaple5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doAct5();
            }
        });
        return view;
    }
    private void printThread(final String tag){
        final String th = Thread.currentThread().getName();
        Log.d("duanyl", tag+":-------"+th);
        tx_console.post(new Runnable() {
            @Override
            public void run() {
                tx_console.append("\n"+tag+":-------"+th);
            }
        });
    }
    private ObservableOnSubscribe<String> observableOnSubscribe = new ObservableOnSubscribe<String>() {
        @Override
        public void subscribe(@NonNull ObservableEmitter<String> e) throws Exception {
            printThread("subscribe");
            e.onNext("Observable  数据data");
            e.onComplete();
        }
    };
    private Function<String, ObservableSource<String>> function = new Function<String, ObservableSource<String>>() {
        @Override
        public ObservableSource<String> apply(@NonNull String s) throws Exception {
            printThread("Observable flatMap"+s+":");
            return Observable.just(s);
        }
    };
    private Consumer<String> consumer = new Consumer<String>() {
        @Override
        public void accept(@NonNull String s) throws Exception {
            printThread("Observer onNext"+s+":");
        }
    };

    public void doAct1( ) {
        //-------------------------- 1   默认主线程-----------------
        tx_console.setText("");
        printThread("1 默认主线程 ");

        create(observableOnSubscribe)
                .flatMap(function)
                .subscribe(consumer);

        //---------------------------2   指定 Observable 的调度器---------------
        printThread("2  指定 Observable 的调度器");
        create(observableOnSubscribe)
                .flatMap(function)
                .subscribeOn(Schedulers.newThread())
                .subscribe(consumer);

        //---------------------------3    默认新线程---------------
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                printThread("  默认新线程");
                create(observableOnSubscribe).flatMap(function).subscribe(consumer);
            }
        }).start();

        //---------------------------    新线程 指定  Observable ，Observer的调度器---------------

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                printThread("    新线程 指定  Observable ，Observer的调度器");
                create(observableOnSubscribe).flatMap(function)
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread()).subscribe(consumer);
            }
        }).start();
        Observable.timer(4,TimeUnit.SECONDS)
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(@NonNull Long aLong) throws Exception {
                        printThread("    timer 调度器");
                    }
                });

    }

    public void doAct2( ) {
        //---------------------------1   同时指定多个 Observable 的调度器---------------
        printThread("3   同时指定多个 Observable 的调度");
        create(observableOnSubscribe)
                .flatMap(function)
                .subscribeOn(Schedulers.newThread())//Observable new 线程发射数据
                .subscribeOn(Schedulers.io())
                .subscribeOn(Schedulers.computation())
                .subscribe(consumer);//最终observer在new 线程接收数据

        //---------------------------2   同时指定多个 Observable 的调度器---------------
        Observable.timer(2,TimeUnit.SECONDS)
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(@NonNull Long aLong) throws Exception {
                        printThread("2   同时指定多个 Observable ,observer 的调度器");
                        create(observableOnSubscribe).flatMap(function)
                                .subscribeOn(Schedulers.newThread())
                                .subscribeOn(Schedulers.io())
                                .subscribeOn(Schedulers.computation())//最终 Observable在 new 线程发射数据
                                .observeOn(Schedulers.newThread())
                                .observeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(consumer);//最终observer 在 main 线程接收数据
                    }
                });
    }

    public void doAct3() {
        //---------------------------     调用Observable 的操作符---------------
        tx_console.setText("");
        printThread("     调用Observable 的操作符");
        create(observableOnSubscribe)//io线程发送数据
                .flatMap(function)//io线程接收数据
                .subscribeOn(Schedulers.io())//指定io线程发送数据
                .flatMap(function)//io线程接收数据
                .observeOn(AndroidSchedulers.mainThread())//切换mian线程接收数据
                .flatMap(function)//mian线程接收数据
                .delay(2, TimeUnit.SECONDS)//delay操作符在Computation线程中接受数据
                .subscribe(consumer);//目标Observer 在Computation线程中接受数据

    }



    public void doAct4() {
        tx_console.setText("");
        //---------------------------    切换 Observable ，Observer的调度器---------------
        printThread("    切换 Observable ，Observer的调度器");
        create(observableOnSubscribe)
                .flatMap(function)//new 1 线程接收数据
                .subscribeOn(Schedulers.newThread())//new 1线程发送数据
                .flatMap(function)//new 1 线程接收数据
                .subscribeOn(Schedulers.io())//设置无效
                .observeOn(Schedulers.newThread())//切换new 2线程接收数据
                .flatMap(function)//new 2 线程接收数据
                .subscribeOn(Schedulers.computation())//设置无效
                .flatMap(function)//new 2 线程接收数据
                .observeOn(Schedulers.io())//切换io线程接收数据
                .subscribe(consumer);//目标Observer 在io线程中接受数据

    }
    public void doAct5() {
        tx_console.setText("");
        //---------------------------  对比  切换 Observable trampoline调度器---------------
        printThread("    切换 Observable io调度器");
        Observable.just("1","2","3","4","5")
                .subscribeOn(Schedulers.newThread())
                .flatMap(function)
                .observeOn(Schedulers.io())
                .flatMap(function)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(consumer);
        Observable.timer(3,TimeUnit.SECONDS)
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(@NonNull Long aLong) throws Exception {
                        printThread("    切换 Observable trampoline调度器");
                        Observable.just("1","2","3","4","5")
                                .subscribeOn(Schedulers.newThread())
                                .flatMap(function)
                                .observeOn(Schedulers.trampoline())
                                .flatMap(function)
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(consumer);
                    }
                });

    }
}
