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

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import io.reactivex.Notification;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.schedulers.Timed;


/**
 * Filtering Observables
 */
public class Simple11Fragment extends Fragment {
    Button btn_smaple1, btn_smaple2, btn_smaple3, btn_smaple4, btn_smaple5, btn_smaple6;
    TextView tx_console;

    public static Simple11Fragment newInstance() {
        Simple11Fragment fragment = new Simple11Fragment();
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
        View view = inflater.inflate(R.layout.fragment_simple11, container, false);
        btn_smaple1 = view.findViewById(R.id.btn_act1);
        btn_smaple2 = view.findViewById(R.id.btn_act2);
        btn_smaple3 = view.findViewById(R.id.btn_act3);
        btn_smaple4 = view.findViewById(R.id.btn_act4);
        btn_smaple5 = view.findViewById(R.id.btn_act5);
        btn_smaple6 = view.findViewById(R.id.btn_act6);
        tx_console = view.findViewById(R.id.tv_console);


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


    private void print(final String tag) {
        Log.d("duanyl", tag);
        tx_console.post(new Runnable() {
            @Override
            public void run() {
                tx_console.append("\n" + tag);
            }
        });

    }
    private Observer observer = new Observer<Integer>() {
        @Override
        public void onSubscribe(Disposable d) {
            print("onSubscribe ");
        }

        @Override
        public void onNext(Integer integer) {
            print("onNext "+integer);
        }

        @Override
        public void onError(Throwable e) {
            print("onError "+e.getMessage());
        }

        @Override
        public void onComplete() {
            print("onComplete " );
        }
    };
    private Observable getObservable(final boolean isError){
        return  Observable.just(1,2,3,4,5)
                .doOnSubscribe(new Consumer<Disposable>() {
                    @Override
                    public void accept(Disposable disposable) throws Exception {
                        print("doOnSubscribe ");
                    }
                })
                .doOnEach(new Consumer<Notification<Integer>>() {
                    @Override
                    public void accept(Notification<Integer> integerNotification) throws Exception {
                        print("doOnEach :"+integerNotification);
                    }
                }).doAfterNext(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        print("doAfterNext : "+integer  );
                        if(isError && integer == 3){
                            throw new Exception("There is a Error!!");
                        }
                    }
                }).doAfterTerminate(new Action() {
                    @Override
                    public void run() throws Exception {
                        print("doAfterTerminate : "  );
                    }
                }).doOnComplete(new Action() {
                    @Override
                    public void run() throws Exception {
                        print("doOnComplete : "  );
                    }
                }).doFinally(new Action() {
                    @Override
                    public void run() throws Exception {
                        print("doFinally : "  );
                    }
                }).doOnDispose(new Action() {
                    @Override
                    public void run() throws Exception {
                        print("doOnDispose : "  );
                    }
                }).doOnTerminate(new Action() {
                    @Override
                    public void run() throws Exception {
                        print("doOnTerminate : "  );
                    }
                }).onTerminateDetach()
                .doOnError(new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        print("doOnError : "  );
                    }
                }).doOnLifecycle(new Consumer<Disposable>() {
                    @Override
                    public void accept(Disposable disposable) throws Exception {
                        print("doOnLifecycle : accept"  );
                    }
                }, new Action() {
                    @Override
                    public void run() throws Exception {
                        print("doOnLifecycle Action : "  );
                    }
                });
    }
    private void doAct1() {
        //需要引入RxJava 1.0
        //-------------buffer operator------
        tx_console.setText("Do");
        getObservable(false).subscribe(observer);
        Log.d("duanyl", "doAct1: =================");
        getObservable(true).subscribe(observer);

        /**
          doOnSubscribe
          doOnLifecycle : accept
          onSubscribe
          doOnEach :OnNextNotification[1]
          onNext 1
          doAfterNext : 1
          doOnEach :OnNextNotification[2]
          onNext 2
          doAfterNext : 2
          doOnEach :OnNextNotification[3]
          onNext 3
          doAfterNext : 3
          doOnEach :OnNextNotification[4]
          onNext 4
          doAfterNext : 4
          doOnEach :OnNextNotification[5]
          onNext 5
          doAfterNext : 5
          doOnEach :OnCompleteNotification
          doOnComplete :
          doOnTerminate :
          onComplete
          doFinally :
          doAfterTerminate :


          doOnSubscribe
          doOnLifecycle : accept
          onSubscribe
          doOnEach :OnNextNotification[1]
          onNext 1
          doAfterNext : 1
          doOnEach :OnNextNotification[2]
          onNext 2
          doAfterNext : 2
          doOnEach :OnNextNotification[3]
          onNext 3
          doAfterNext : 3
          doOnTerminate :
          doOnError :
          onError There is a Error!!
          doFinally :
          doAfterTerminate :
         */
    }


    private void doAct2() {
        //-------------sample operator------
        tx_console.setText("materialize");
        Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> e) throws Exception {
                e.onNext("aaaa");
                e.onNext("bbbb");
                e.onNext("cccc");
                e.onComplete();
            }
        }).materialize().subscribe(new Consumer<Notification<String>>() {
            @Override
            public void accept(Notification<String> stringNotification) throws Exception {
                print("materialize:"+stringNotification +"--->getValue:"+stringNotification.getValue()
                        +"--->isOnComplete:"+stringNotification.isOnComplete()
                        +"--->isOnError:"+stringNotification.isOnError() );
            }
        });
        Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> e) throws Exception {
                e.onNext("aaaa");
                e.onNext("bbbb");
                e.onNext("cccc");
                e.onComplete();
            }
        }).materialize()
                .map(new Function<Notification<String>, Notification<String>>() {
            @Override
            public Notification<String> apply(Notification<String> stringNotification) throws Exception {
                return stringNotification;
            }
        }).dematerialize().subscribe(new Consumer<Object>() {
            @Override
            public void accept(Object o) throws Exception {
                print("materialize:"+o.toString());
            }
        });
        print("serialize " );
        Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> e) throws Exception {
                print("create "+Thread.currentThread().getName());
                e.onNext("aaaa");
                e.onNext("bbbb");
                e.onNext("cccc");
                e.onComplete();
            }
        }).serialize()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .delay(3000, TimeUnit.MILLISECONDS).subscribe(new Consumer<String>() {
            @Override
            public void accept(String s) throws Exception {
                print("delay "+Thread.currentThread().getName());
            }
        });
/**
 * materialize:OnCompleteNotification--->getValue:null--->isOnComplete:true--->isOnError:false
 11-11 16:32:38.844 11090-11090/com.callanna.demo D/duanyl: materialize:OnNextNotification[aaaa]--->getValue:aaaa--->isOnComplete:false--->isOnError:false
 11-11 16:32:38.844 11090-11090/com.callanna.demo D/duanyl: materialize:OnNextNotification[bbbb]--->getValue:bbbb--->isOnComplete:false--->isOnError:false
 11-11 16:32:38.844 11090-11090/com.callanna.demo D/duanyl: materialize:OnNextNotification[cccc]--->getValue:cccc--->isOnComplete:false--->isOnError:false
 11-11 16:32:38.844 11090-11090/com.callanna.demo D/duanyl: materialize:OnCompleteNotification--->getValue:null--->isOnComplete:true--->isOnError:false
 11-11 16:32:38.844 11090-11090/com.callanna.demo D/duanyl: materialize:aaaa
 11-11 16:32:38.844 11090-11090/com.callanna.demo D/duanyl: materialize:bbbb
 11-11 16:32:38.844 11090-11090/com.callanna.demo D/duanyl: materialize:cccc
 11-11 16:32:38.844 11090-11090/com.callanna.demo D/duanyl: serialize
 11-11 16:32:38.854 11090-11111/com.callanna.demo D/duanyl: create RxCachedThreadScheduler-1
 11-11 16:32:41.904 11090-11112/com.callanna.demo D/duanyl: delay RxComputationThreadPool-1
 11-11 16:32:41.904 11090-11112/com.callanna.demo D/duanyl: delay RxComputationThreadPool-1
 11-11 16:32:41.904 11090-11112/com.callanna.demo D/duanyl: delay RxComputationThreadPool-1
 */
    }


    private void doAct3() {
        //-------------FlatMap operator------
        tx_console.setText("time");
        Observable.intervalRange(0,10,0,500,TimeUnit.MILLISECONDS)
                .timeInterval().subscribe(new Consumer<Timed<Long>>() {
            @Override
            public void accept(Timed<Long> longTimed) throws Exception {
                print("timeInterval---Timed--->"+longTimed.time());//0
            }
        });
        Observable.intervalRange(0,10,0,5500,TimeUnit.MILLISECONDS)
                .timeout(5000,TimeUnit.MILLISECONDS )
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        print("timeout---->"+aLong);//0
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        print("timeout----Throwable>"+throwable.getMessage());
                    }
                });
        Observable.intervalRange(0,10,0,5500,TimeUnit.MILLISECONDS)
                .timestamp()
                .subscribe(new Consumer<Timed<Long>>() {
                    @Override
                    public void accept(Timed<Long> longTimed) throws Exception {
                        print("timestamp---Timed--->"+longTimed.time());//1510388694052
                    }
                });
        /**
         * : timeInterval---Timed--->0
         11-11 16:24:54.044 10773-10801/com.callanna.demo D/duanyl: timeout---->0
         11-11 16:24:54.044 10773-10794/com.callanna.demo D/duanyl: timestamp---Timed--->1510388694052
         11-11 16:24:54.544 10773-10795/com.callanna.demo D/duanyl: timeInterval---Timed--->500
         11-11 16:24:55.034 10773-10795/com.callanna.demo D/duanyl: timeInterval---Timed--->500
         11-11 16:24:55.544 10773-10795/com.callanna.demo D/duanyl: timeInterval---Timed--->500
         11-11 16:24:56.034 10773-10795/com.callanna.demo D/duanyl: timeInterval---Timed--->500
         11-11 16:24:56.544 10773-10795/com.callanna.demo D/duanyl: timeInterval---Timed--->500
         11-11 16:24:57.034 10773-10795/com.callanna.demo D/duanyl: timeInterval---Timed--->500
         11-11 16:24:57.544 10773-10795/com.callanna.demo D/duanyl: timeInterval---Timed--->500
         11-11 16:24:58.034 10773-10795/com.callanna.demo D/duanyl: timeInterval---Timed--->500
         11-11 16:24:58.534 10773-10795/com.callanna.demo D/duanyl: timeInterval---Timed--->500
         11-11 16:24:59.044 10773-10796/com.callanna.demo D/duanyl: timeout----Throwable>null
         11-11 16:24:59.544 10773-10794/com.callanna.demo D/duanyl: timestamp---Timed--->1510388699553
         11-11 16:25:05.044 10773-10794/com.callanna.demo D/duanyl: timestamp---Timed--->1510388705053
         11-11 16:25:10.544 10773-10794/com.callanna.demo D/duanyl: timestamp---Timed--->1510388710553
         11-11 16:25:16.044 10773-10794/com.callanna.demo D/duanyl: timestamp---Timed--->1510388716053
         11-11 16:25:21.544 10773-10794/com.callanna.demo D/duanyl: timestamp---Timed--->1510388721553
         11-11 16:25:27.044 10773-10794/com.callanna.demo D/duanyl: timestamp---Timed--->1510388727053
         11-11 16:25:32.544 10773-10794/com.callanna.demo D/duanyl: timestamp---Timed--->1510388732553
         11-11 16:25:38.044 10773-10794/com.callanna.demo D/duanyl: timestamp---Timed--->1510388738053
         11-11 16:25:43.544 10773-10794/com.callanna.demo D/duanyl: timestamp---Timed--->1510388743553
         */
    }

    private void doAct4() {

        //-------------Last operator------
        tx_console.setText("using");
        Observable.using(new Callable<String>() {
            @Override
            public String call() throws Exception {
                return "hello";
            }
        }, new Function<String, ObservableSource<String>>() {
            @Override
            public ObservableSource<String> apply(String s) throws Exception {
                return Observable.just(s+"----》你好！");
            }
        }, new Consumer<String>() {
            @Override
            public void accept(String s) throws Exception {
                print("using----->"+s);//hello
            }
        }).subscribe(new Consumer<String>() {
            @Override
            public void accept(String s) throws Exception {
                print("using Consumer accept----->"+s);//hello----》你好！
            }
        });
    }

    private void doAct5() {
        // -------------Until
        tx_console.setText("to");

        String first = Observable.just("aaaa",2,3).blockingFirst().toString();
        print(""+first);//aaaa
        Iterable<String> stringIterable = Observable.just("1","2","3").blockingIterable();
        Iterator iterator = stringIterable.iterator();
        while (iterator.hasNext()){
            print(""+iterator.next());
        }
        //1,2,3


        Observable.just("1","2","3").toMap(new Function<String, String>() {
            @Override
            public String apply(String s) throws Exception {
                return s+"+"+s;
            }
        }).subscribe(new Consumer<Map<String, String>>() {
            @Override
            public void accept(Map<String, String> stringStringMap) throws Exception {
                print("toMap   "+stringStringMap );//{2+2=2, 3+3=3, 1+1=1}
            }
        });
        Observable.just(5,3,6,3,9,4)
                .toSortedList().subscribe(new Consumer<List<Integer>>() {
            @Override
            public void accept(List<Integer> integers) throws Exception {
                print("toSortedList"+integers);//[3, 3, 4, 5, 6, 9]
            }
        });
    }

    private void doAct6() {
        //------------While
        tx_console.setText("amb ");
        Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> e) throws Exception {
                e.onNext("2222");
                e.onError(new Throwable("Sorry!! an error occured sending the data"));
            }
        }).retry(3)
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String s) throws Exception {
                        print("retry--->" + s);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        print("retry--->throwable:" + throwable.getMessage());
                    }
                });
        /**
         * retry--->2222
         11-11 16:08:47.084 10700-10700/com.callanna.demo D/duanyl: retry--->2222
         11-11 16:08:47.084 10700-10700/com.callanna.demo D/duanyl: retry--->2222
         11-11 16:08:47.084 10700-10700/com.callanna.demo D/duanyl: retry--->2222
         11-11 16:08:47.084 10700-10700/com.callanna.demo D/duanyl: retry--->throwable:Sorry!! an error occured sending the data
         */
        print("cache ===========" );
        final AtomicBoolean shouldStop = new AtomicBoolean();
        Observable.timer(5000,TimeUnit.MILLISECONDS)
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        shouldStop.set(true);
                    }
                });
        Observable.intervalRange(0,10,0,500,TimeUnit.MILLISECONDS)
                .takeUntil(new Predicate<Long>() {
                    @Override
                    public boolean test(Long aLong) throws Exception {
                        return shouldStop.get();
                    }
                })
                .onTerminateDetach()
                .cache()
                .takeUntil(new Predicate<Long>() {
                    @Override
                    public boolean test(Long aLong) throws Exception {
                        return shouldStop.get();
                    }
                })
                .onTerminateDetach()
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        print("cache---->"+aLong);
                    }
                });


        Observable.just("1","2","3")
                .delay(1000,TimeUnit.MILLISECONDS)
                .cast(Integer.class)
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer val) throws Exception {
                        print("cast---->" + val);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        print(""+throwable.getMessage());
                    }
                });

    }
}
