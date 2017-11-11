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

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class Simple4Fragment extends Fragment {
    Button btn_smaple1,btn_smaple2,btn_smaple3,btn_smaple4,btn_smaple5,btn_smaple6;
    TextView tx_console;
    public static Simple4Fragment newInstance( ) {
        Simple4Fragment fragment = new Simple4Fragment();
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
       View view = inflater.inflate(R.layout.fragment_simple4, container, false);
        btn_smaple1 =  view.findViewById(R.id.btn_act1);
        btn_smaple2 =  view.findViewById(R.id.btn_act2);
        btn_smaple3 =  view.findViewById(R.id.btn_act3);
        btn_smaple4 =  view.findViewById(R.id.btn_act4);
        btn_smaple5 =  view.findViewById(R.id.btn_act5);
        btn_smaple6 =  view.findViewById(R.id.btn_act6);
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


        btn_smaple6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doAct6();
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
    private void doAct1() {
        //-------------OOM
        tx_console.setText("");
        Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> e) throws Exception {
                int i = 0;
                while (true){
                    e.onNext("data:"+(i++));
                }
            }
        }) .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.newThread())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String s) throws Exception {
                        Thread.sleep(2000);
                        printThread(s);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(@NonNull Throwable throwable) throws Exception {
                        printThread(throwable.getMessage());
                    }
                });

    }
    Subscription subscription1;
    private void doAct2() {
        //DROP---------------分三次打印第一次0...49,第二次50...99...第三次100...127
        if(subscription1 == null) {
            tx_console.setText("DROP");
            Flowable.create(new FlowableOnSubscribe<String>() {
                @Override
                public void subscribe(@NonNull FlowableEmitter<String> e) throws Exception {
                    int i = 0;
                    while (true) {
                        e.onNext("data:" + (i++));
                        if (i == 1000) {
                            e.onComplete();
                            return;
                        }
                    }
                }
            }, BackpressureStrategy.DROP)//超出缓冲池的数据丢弃，丢弃最新的，保留原始的
                    .subscribeOn(Schedulers.computation())
                    .observeOn(Schedulers.newThread())
                    .subscribe(new Subscriber<String>() {
                        @Override
                        public void onSubscribe(Subscription s) {
                            subscription1 = s;
                            subscription1.request(50);
                        }

                        @Override
                        public void onNext(String s) {
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            printThread(s);
                        }

                        @Override
                        public void onError(Throwable t) {

                        }

                        @Override
                        public void onComplete() {

                        }
                    });
        }else{
            subscription1.request(50);
        }
    }
    Subscription s1;
    private void doAct6() {
        //LATEST----------打印分三次打印第一次0...49,第二次50...99...第三次100...127,999
        //与DROP的区别在于会打印最后一次数据
        if(s1 == null) {
            tx_console.setText("LATEST");

            Flowable.create(new FlowableOnSubscribe<String>() {
                @Override
                public void subscribe(@NonNull FlowableEmitter<String> e) throws Exception {
                    int i = 0;
                    while (true) {
                        e.onNext("data:" + (i++));
                        if (i == 1000) {
                            e.onComplete();
                            return;
                        }
                    }
                }
            }, BackpressureStrategy.LATEST)//超出缓冲池的数据的时候，丢弃之前的，保留最新的
                    .subscribeOn(Schedulers.computation())
                    .observeOn(Schedulers.newThread())
                    .subscribe(new Subscriber<String>() {
                        @Override
                        public void onSubscribe(Subscription s) {
                            s1 = s;
                            s1.request(50);
                        }

                        @Override
                        public void onNext(String s) {
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            printThread(s);
                        }

                        @Override
                        public void onError(Throwable t) {

                        }

                        @Override
                        public void onComplete() {

                        }
                    });
        }else{
            s1.request(50);
        }
    }
    @Override
    public void onPause() {
        super.onPause();
        if(subscription != null) {
            subscription.cancel();
        }
        if(subscription1 != null) {
            subscription1.cancel();
        }
        if(s1 != null) {
            s1.cancel();
        }

    }
    Subscription subscription;

    private void doAct3() {
        tx_console.setText("MISSING");
        Flowable.create(new FlowableOnSubscribe<String>() {
            @Override
            public void subscribe(@NonNull FlowableEmitter<String> e) throws Exception {
                int i = 0;
                while (true){
                    e.onNext("data:"+(i++));
                }
            }
        }, BackpressureStrategy.MISSING)//没有缓冲池，接收第一个数据以后，后面的都丢弃
                .subscribeOn(Schedulers.computation())
                .observeOn(Schedulers.newThread())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(@NonNull String s) throws Exception {
                        printThread(s);
                    }
                });
    }


    private void doAct4() {
        //Buffer策略------OOM
        tx_console.setText("BUFFER");

        Flowable.create(new FlowableOnSubscribe<String>() {
            @Override
            public void subscribe(@NonNull FlowableEmitter<String> e) throws Exception {
                int i = 0;
                while (true){
                    e.onNext("data:"+(i++));
                }
            }
        }, BackpressureStrategy.BUFFER)// 缓冲池的数据128个，默认的策略
                .subscribeOn(Schedulers.computation())
                .observeOn(Schedulers.newThread())
                .subscribe(new Subscriber<String>() {
                    @Override
                    public void onSubscribe(Subscription s) {
                        subscription = s;
                        subscription.request(256);
                    }

                    @Override
                    public void onNext(String s) {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        printThread(s);

                    }

                    @Override
                    public void onError(Throwable t) {
                        printThread(t.getMessage());
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }
    private void doAct5() {
        //ERROR------报错---MissingBackpressureException: create: could not emit value due to lack of requests
        tx_console.setText("ERROR");

        Flowable.create(new FlowableOnSubscribe<String>() {
            @Override
            public void subscribe(@NonNull FlowableEmitter<String> e) throws Exception {
                int i = 0;
                while (true){
                    e.onNext("data:"+(i++));
                }
            }
        }, BackpressureStrategy.ERROR)//超出缓冲池的数据128个时，报Error
                .subscribeOn(Schedulers.computation())
                .observeOn(Schedulers.newThread())
                .subscribe(new Subscriber<String>() {
                    @Override
                    public void onSubscribe(Subscription s) {
                        subscription = s;
                        subscription.request(128);
                    }

                    @Override
                    public void onNext(String s) {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        printThread(s);

                    }

                    @Override
                    public void onError(Throwable t) {
                        printThread(t.getMessage());
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

}
