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
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Timed;


/**
 * Observable Utility Operators
 */
public class ObservableUtilityOperatorsFragment extends Fragment {
    Button btn_smaple1, btn_smaple2, btn_smaple3, btn_smaple4, btn_smaple5, btn_smaple6;
    TextView tx_console;

    public static ObservableUtilityOperatorsFragment newInstance() {
        ObservableUtilityOperatorsFragment fragment = new ObservableUtilityOperatorsFragment();
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
            print("onNext " + integer);
        }

        @Override
        public void onError(Throwable e) {
            print("onError " + e.getMessage());
        }

        @Override
        public void onComplete() {
            print("onComplete ");
        }
    };

    private Observable getObservable(final boolean isError) {
        return Observable.just(1, 2, 3, 4, 5)
                .doOnSubscribe(new Consumer<Disposable>() {
                    @Override
                    public void accept(Disposable disposable) throws Exception {
                        print("doOnSubscribe ");
                    }
                })
                .doOnEach(new Consumer<Notification<Integer>>() {
                    @Override
                    public void accept(Notification<Integer> integerNotification) throws Exception {
                        print("doOnEach :" + integerNotification);
                    }
                }).doAfterNext(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        print("doAfterNext : " + integer);
                        if (isError && integer == 3) {
                            throw new Exception("There is a Error!!");
                        }
                    }
                }).doAfterTerminate(new Action() {
                    @Override
                    public void run() throws Exception {
                        print("doAfterTerminate : ");
                    }
                }).doOnComplete(new Action() {
                    @Override
                    public void run() throws Exception {
                        print("doOnComplete : ");
                    }
                }).doFinally(new Action() {
                    @Override
                    public void run() throws Exception {
                        print("doFinally : ");
                    }
                }).doOnDispose(new Action() {
                    @Override
                    public void run() throws Exception {
                        print("doOnDispose : ");
                    }
                }).doOnTerminate(new Action() {
                    @Override
                    public void run() throws Exception {
                        print("doOnTerminate : ");
                    }
                }).onTerminateDetach()
                .doOnError(new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        print("doOnError : ");
                    }
                }).doOnLifecycle(new Consumer<Disposable>() {
                    @Override
                    public void accept(Disposable disposable) throws Exception {
                        print("doOnLifecycle : accept");
                    }
                }, new Action() {
                    @Override
                    public void run() throws Exception {
                        print("doOnLifecycle Action : ");
                    }
                });
    }

    private void doAct1() {
        //需要引入RxJava 1.0
        //-------------Do" operator------
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
        //-------------materialize operator------
        tx_console.setText("materialize");

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
                        print("materialize:" + stringNotification + "--->getValue:" + stringNotification.getValue()
                                + "--->isOnComplete:" + stringNotification.isOnComplete()
                                + "--->isOnError:" + stringNotification.isOnError());
                        return stringNotification;
                    }
                }).dematerialize().subscribe(new Consumer<Object>() {
            @Override
            public void accept(Object o) throws Exception {
                print("dematerialize:" + o.toString());//
            }
        });
        /**
         * materialize:OnNextNotification[aaaa]--->getValue:aaaa--->isOnComplete:false--->isOnError:false
         materialize:OnNextNotification[bbbb]--->getValue:bbbb--->isOnComplete:false--->isOnError:false
         materialize:OnNextNotification[cccc]--->getValue:cccc--->isOnComplete:false--->isOnError:false
         materialize:OnCompleteNotification--->getValue:null--->isOnComplete:true--->isOnError:false
         dematerialize:aaaa
         dematerialize:bbbb
         dematerialize:cccc
         */
//        print("serialize ");
//
//        Observable observable = Observable.create(new ObservableOnSubscribe<String>() {
//            @Override
//            public void subscribe(ObservableEmitter<String> e) throws Exception {
//                emitter = e;
//                emitter.onNext("create subscribe first ");
//            }
//        });
//        observable.serialize().subscribe(new Consumer<String>() {
//            @Override
//            public void accept(String s) throws Exception {
//                print("  do serialize accept:" + s);
//            }
//        }, new Consumer<Throwable>() {
//            @Override
//            public void accept(Throwable throwable) throws Exception {
//                print("  do serialize throwable:" + throwable.getMessage());
//            }
//        });
//        Observable.intervalRange(0, 10, 0, 5, TimeUnit.MILLISECONDS).subscribe(new Consumer<Long>() {
//            @Override
//            public void accept(Long aLong) throws Exception {
//                emitter.onNext("intervalRange  send " + aLong);
//                if (aLong == 5) {
//                    emitter.onError(new Exception("intervalRange   ERROR"));
//                }
//            }
//        });
//        Observable.intervalRange(100, 10, 5, 5, TimeUnit.MILLISECONDS).subscribe(new Consumer<Long>() {
//            @Override
//            public void accept(Long aLong) throws Exception {
//                emitter.onNext("intervalRange2  send " + aLong);
//            }
//        });

    }


    private void doAct3() {
        //-------------time operator------
        tx_console.setText("time");
        Observable.intervalRange(0, 10, 0, 500, TimeUnit.MILLISECONDS)
                .timeInterval().subscribe(new Consumer<Timed<Long>>() {
            @Override
            public void accept(Timed<Long> longTimed) throws Exception {
                print("timeInterval---Timed--->" + longTimed.time());//0
            }
        });
        Observable.intervalRange(0, 10, 0, 5500, TimeUnit.MILLISECONDS)
                .timeout(5000, TimeUnit.MILLISECONDS)
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        print("timeout---->" + aLong);//0
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        print("timeout----Throwable>" + throwable.getMessage());
                    }
                });
        Observable.intervalRange(0, 10, 0, 5500, TimeUnit.MILLISECONDS)
                .timestamp()
                .subscribe(new Consumer<Timed<Long>>() {
                    @Override
                    public void accept(Timed<Long> longTimed) throws Exception {
                        print("timestamp---Timed--->" + longTimed.time());//1510388694052
                    }
                });
        /**
         * : timeInterval---Timed--->0
         11-11 16:24:54.044   timeout---->0
         11-11 16:24:54.044   timestamp---Timed--->1510388694052
         11-11 16:24:54.544   timeInterval---Timed--->500
         11-11 16:24:55.034   timeInterval---Timed--->500
         11-11 16:24:55.544   timeInterval---Timed--->500
         11-11 16:24:56.034   timeInterval---Timed--->500
         11-11 16:24:56.544   timeInterval---Timed--->500
         11-11 16:24:57.034   timeInterval---Timed--->500
         11-11 16:24:57.544   timeInterval---Timed--->500
         11-11 16:24:58.034   timeInterval---Timed--->500
         11-11 16:24:58.534   timeInterval---Timed--->500
         11-11 16:24:59.044   timeout----Throwable>null
         11-11 16:24:59.544   timestamp---Timed--->1510388699553
         11-11 16:25:05.044   timestamp---Timed--->1510388705053
         11-11 16:25:10.544   timestamp---Timed--->1510388710553
         11-11 16:25:16.044   timestamp---Timed--->1510388716053
         11-11 16:25:21.544   timestamp---Timed--->1510388721553
         11-11 16:25:27.044   timestamp---Timed--->1510388727053
         11-11 16:25:32.544   timestamp---Timed--->1510388732553
         11-11 16:25:38.044   timestamp---Timed--->1510388738053
         11-11 16:25:43.544   timestamp---Timed--->1510388743553
         */
    }

    private void doAct4() {

        //-------------using operator------
        tx_console.setText("using");
        Observable.using(new Callable<String>() {
            @Override
            public String call() throws Exception {
                return "hello";//----源数据
            }
        }, new Function<String, ObservableSource<String>>() {
            @Override
            public ObservableSource<String> apply(String s) throws Exception {
                return Observable.just(s + "----》你好！");//--------目标数据
            }
        }, new Consumer<String>() {
            @Override
            public void accept(String s) throws Exception {
                print("using----->" + s);//hello----收到源数据
                throw new Exception("源数据-----Error :" + s);
            }
        }).subscribe(new Consumer<String>() {
            @Override
            public void accept(String s) throws Exception {
                print("using Consumer accept----->" + s);//hello----》你好！
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {
                print("using Consumer throwable----->" + throwable.getMessage());
            }
        });
//        using Consumer accept----->hello----》你好！
//        using----->hello
//        using Consumer throwable----->源数据-----Error :hello
    }

    private void doAct5() {
        // -------------to
        tx_console.setText("to");

        String first = Observable.just("aaaa", 2, 3).blockingFirst().toString();
        print("" + first);//aaaa
        Iterable<String> stringIterable = Observable.just("1", "2", "3").blockingIterable();
        Iterator iterator = stringIterable.iterator();
        while (iterator.hasNext()) {
            print("" + iterator.next());
        }
        //1,2,3


        Observable.just("1", "2", "3").toMap(new Function<String, String>() {
            @Override
            public String apply(String s) throws Exception {
                return s + "+" + s;
            }
        }).subscribe(new Consumer<Map<String, String>>() {
            @Override
            public void accept(Map<String, String> stringStringMap) throws Exception {
                print("toMap   " + stringStringMap);//{2+2=2, 3+3=3, 1+1=1}
            }
        });
        Observable.just(5, 3, 6, 3, 9, 4)
                .toSortedList().subscribe(new Consumer<List<Integer>>() {
            @Override
            public void accept(List<Integer> integers) throws Exception {
                print("toSortedList" + integers);//[3, 3, 4, 5, 6, 9]
            }
        });
    }
    ObservableEmitter<String> emitter = null;

    private void doAct6() {
        //------------retry
        tx_console.setText("retry ");
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
         *
         retry--->2222
         retry--->2222
         retry--->2222
         retry--->2222
         retry--->throwable:Sorry!! an error occured sending the data
         */
        print("cache ===========");
        final AtomicBoolean shouldStop = new AtomicBoolean();
        shouldStop.set(false);
        Observable observable = Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> e) throws Exception {
                emitter = e;
                emitter.onNext("1-----onNext");

            }
        });
        Observable.intervalRange(0, 5, 100, 5, TimeUnit.MILLISECONDS).subscribe(new Consumer<Long>() {
            @Override
            public void accept(Long aLong) throws Exception {
                emitter.onNext("intervalRange  send " + aLong);
            }
        });

        observable.cache().subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String aLong) throws Exception {
                        print("no cache---->" + aLong);
                    }
                });
        observable.delay(2000,TimeUnit.MILLISECONDS).subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String aLong) throws Exception {
                        print(" cache---->" + aLong);
                    }
                });
        observable
                .delay(4000,TimeUnit.MILLISECONDS)
                .onTerminateDetach()
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String aLong) throws Exception {
                        print("onTerminateDetach cache---->" + aLong);
                    }
                });
/**
 *================================================no  cache ===========
  no cache---->1-----onNext
  cache---->intervalRange  send 0
  cache---->1-----onNext
  cache---->intervalRange  send 1
  cache---->intervalRange  send 2
  onTerminateDetach cache---->1-----onNext
  onTerminateDetach cache---->intervalRange  send 3
  onTerminateDetach cache---->intervalRange  send 4

 ===================================================cache===========
 : no cache---->1-----onNext
   cache---->1-----onNext
   onTerminateDetach cache---->1-----onNext
   onTerminateDetach cache---->intervalRange  send 0
   onTerminateDetach cache---->intervalRange  send 1
   onTerminateDetach cache---->intervalRange  send 2
   onTerminateDetach cache---->intervalRange  send 3
   onTerminateDetach cache---->intervalRange  send 4
 */


        //=========================cast===========

        Observable.just("1", "2", "3")
                .delay(8000, TimeUnit.MILLISECONDS)
                .cast(Integer.class)
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer val) throws Exception {
                        print("cast---->" + val);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        print("" + throwable.getMessage());//java.lang.String cannot be cast to java.lang.Integer
                    }
                });

    }
}
