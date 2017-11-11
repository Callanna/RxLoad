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
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.Schedulers;

/**
 * Filtering Observables
 */
public class Simple8Fragment extends Fragment {
    Button btn_smaple1, btn_smaple2, btn_smaple3, btn_smaple4, btn_smaple5, btn_smaple6;
    TextView tx_console;

    public static Simple8Fragment newInstance() {
        Simple8Fragment fragment = new Simple8Fragment();
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
        View view = inflater.inflate(R.layout.fragment_simple8, container, false);
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

    private void doAct1() {
        //-------------buffer operator------
        tx_console.setText("debounce");
        String[] arr = new String[]{"a", "b", "c", "d", "e"};
        Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> emitter) throws Exception {
                // send events with simulated time wait
                emitter.onNext(1); // skip
                Thread.sleep(400);
                emitter.onNext(2); // deliver
                Thread.sleep(505);
                emitter.onNext(3); // skip
                Thread.sleep(100);
                emitter.onNext(4); // skip
                Thread.sleep(300);
                emitter.onNext(5); // skip
                Thread.sleep(410);
                emitter.onNext(6); // skip
                Thread.sleep(205);
                emitter.onNext(7);//deliver
                emitter.onComplete();
            }
        }).debounce(500,TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        print("accept:"+integer); //2,7
                    }
                });
        //输出结果；
//        11-07 13:21:23.676 5593-5593/com.callanna.demo D/duanyl: accept:2
//        11-07 13:21:24.696 5593-5593/com.callanna.demo D/duanyl: accept:7

        Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> emitter) throws Exception {
                // send events with simulated time wait
                emitter.onNext(1); // skip
                Thread.sleep(400);
                emitter.onNext(2); // deliver
                Thread.sleep(505);
                emitter.onNext(3); // skip
                Thread.sleep(100);
                emitter.onNext(4); // skip
                Thread.sleep(300);
                emitter.onNext(5); // skip
                Thread.sleep(410);
                emitter.onNext(6); // skip
                Thread.sleep(205);
                emitter.onNext(7);//deliver
                emitter.onComplete();
            }
        }).throttleWithTimeout(500,TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        print("accept:"+integer); //2,7
                    }
                });
    }
    private void doAct2() {
        //-------------sample operator------
        tx_console.setText("sample");

        Observable.intervalRange(1,20,0,300, TimeUnit.MILLISECONDS)
                .sample(1, TimeUnit.SECONDS, Schedulers.newThread())
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        print("sample Accept:"+aLong);
                    }
                });

        Observable.intervalRange(1,20,5000,300, TimeUnit.MILLISECONDS)
                .throttleFirst(1, TimeUnit.SECONDS, Schedulers.newThread())
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        print("throttleFirst Accept:"+aLong);
                    }
                });

        Observable.intervalRange(1,20,10000,300, TimeUnit.MILLISECONDS)
                .throttleLast(1, TimeUnit.SECONDS, Schedulers.newThread())
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        print("throttleLast Accept:"+aLong);
                    }
                });
//        11-07 15:53:57.386 7972-8005/com.callanna.demo D/duanyl: sample Accept:4
//        11-07 15:53:58.386 7972-8005/com.callanna.demo D/duanyl: sample Accept:7
//        11-07 15:53:59.386 7972-8005/com.callanna.demo D/duanyl: sample Accept:10
//        11-07 15:54:00.386 7972-8005/com.callanna.demo D/duanyl: sample Accept:14
//        11-07 15:54:01.386 7972-8005/com.callanna.demo D/duanyl: sample Accept:17
//        11-07 15:54:01.396 7972-8007/com.callanna.demo D/duanyl: throttleFirst Accept:1
//        11-07 15:54:02.596 7972-8007/com.callanna.demo D/duanyl: throttleFirst Accept:5
//        11-07 15:54:03.796 7972-8007/com.callanna.demo D/duanyl: throttleFirst Accept:9
//        11-07 15:54:04.996 7972-8007/com.callanna.demo D/duanyl: throttleFirst Accept:13
//        11-07 15:54:06.196 7972-8007/com.callanna.demo D/duanyl: throttleFirst Accept:17
//        11-07 15:54:07.396 7972-8008/com.callanna.demo D/duanyl: throttleLast Accept:4
//        11-07 15:54:08.396 7972-8008/com.callanna.demo D/duanyl: throttleLast Accept:7
//        11-07 15:54:09.396 7972-8008/com.callanna.demo D/duanyl: throttleLast Accept:10
//        11-07 15:54:10.396 7972-8008/com.callanna.demo D/duanyl: throttleLast Accept:14
//        11-07 15:54:11.396 7972-8008/com.callanna.demo D/duanyl: throttleLast Accept:17
    }


    private void doAct3() {
        //-------------FlatMap operator------
        tx_console.setText("first@take&skip");
        Observable.just(1,2,3)
                .first(0)
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer aInteger) throws Exception {
                        print("first Accept:"+aInteger);//1
                    }
                });
        Observable.intervalRange(10,10,0,300, TimeUnit.MILLISECONDS)
                .firstElement()
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        print("firstElement Accept:"+aLong);//10
                    }
                });

        Observable.intervalRange(10,10,0,300, TimeUnit.MILLISECONDS)
                .take(5)
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        print("take Accept:"+aLong);//10,11,12,13,14
                    }
                });
        Observable.intervalRange(100,10,3000,300, TimeUnit.MILLISECONDS)
                .skip(5)
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        print("skip Accept:"+aLong);//105,106,107,108,109
                    }
                });


    }

    String[] arr = new String[]{"aaa", "bb", "ccc", "dd", "eee","fff","gg"};
    private void doAct4() {

        //-------------Last operator------
        tx_console.setText("Last");
        Observable.fromArray(arr)
                .last("--")
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String aLong) throws Exception {
                        print("last Accept:"+aLong);//gg
                    }
                });
        Observable.fromArray(arr)
                .lastElement()
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String aLong) throws Exception {
                        print("lastElement Accept:"+aLong);//gg
                    }
                });

        Observable.fromArray(arr)
                .skipLast(3)
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String aLong) throws Exception {
                        print("skipLast Accept:"+aLong);
                        //"aaa", "bb", "ccc", "dd"
                    }
                });
        Observable.fromArray(arr)
                .take(3)
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String aLong) throws Exception {
                        print("take Accept:"+aLong);
                        //"aaa", "bb", "ccc",
                    }
                });
        Observable.fromArray(arr)
                .takeLast(3)
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String aLong) throws Exception {
                        print("takeLast Accept:"+aLong);
                        //"eee","fff","gg"
                    }
                });

    }

    private void doAct5() {
        // -------------Until
        tx_console.setText("Until  ");

        Observable.intervalRange(10,10,0,300, TimeUnit.MILLISECONDS)
                .doOnSubscribe(new Consumer<Disposable>() {
                    @Override
                    public void accept(Disposable disposable) throws Exception {
                        print("skipUntil  doOnSubscribe");
                    }
                })
                .skipUntil(Observable.timer(2000,TimeUnit.MILLISECONDS))
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        print("skipUntil Accept:"+aLong);
                    }
                });
//        11-07 14:26:35.776 6302-6302/com.callanna.demo D/duanyl: skipUntil  doOnSubscribe
//        11-07 14:26:37.876 6302-6324/com.callanna.demo D/duanyl: skipUntil Accept:17
//        11-07 14:26:38.176 6302-6324/com.callanna.demo D/duanyl: skipUntil Accept:18
//        11-07 14:26:38.476 6302-6324/com.callanna.demo D/duanyl: skipUntil Accept:19

        Observable.intervalRange(10,10,5,300, TimeUnit.MILLISECONDS)
                .doOnSubscribe(new Consumer<Disposable>() {
                    @Override
                    public void accept(Disposable disposable) throws Exception {
                        print("takeUntil  doOnSubscribe");
                    }
                })
                .takeUntil(Observable.timer(2000,TimeUnit.MILLISECONDS))
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        print("takeUntil Accept:"+aLong);
                    }
                });

//        11-07 14:26:35.776 6302-6302/com.callanna.demo D/duanyl: takeUntil  doOnSubscribe
//        11-07 14:26:35.786 6302-6326/com.callanna.demo D/duanyl: takeUntil Accept:10
//        11-07 14:26:36.086 6302-6326/com.callanna.demo D/duanyl: takeUntil Accept:11
//        11-07 14:26:36.386 6302-6326/com.callanna.demo D/duanyl: takeUntil Accept:12
//        11-07 14:26:36.676 6302-6326/com.callanna.demo D/duanyl: takeUntil Accept:13
//        11-07 14:26:36.986 6302-6326/com.callanna.demo D/duanyl: takeUntil Accept:14
//        11-07 14:26:37.276 6302-6326/com.callanna.demo D/duanyl: takeUntil Accept:15
//        11-07 14:26:37.576 6302-6326/com.callanna.demo D/duanyl: takeUntil Accept:16
    }

    private void doAct6() {
        //------------While
        tx_console.setText("While ");
        Observable.fromArray(arr)
                .doOnSubscribe(new Consumer<Disposable>() {
                    @Override
                    public void accept(Disposable disposable) throws Exception {
                        print("skipWhile  doOnSubscribe");
                    }
                })
                .skipWhile(new Predicate<String>() {
                    @Override
                    public boolean test(String s) throws Exception {
                        return s.equals("ccc");
                    }
                })
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String aLong) throws Exception {
                        print("skipWhile Accept:"+aLong);
                    }
                });
        Observable.fromArray(arr)
                .doOnSubscribe(new Consumer<Disposable>() {
                    @Override
                    public void accept(Disposable disposable) throws Exception {
                        print("takeWhile  doOnSubscribe");
                    }
                })
                .takeWhile(new Predicate<String>() {
                    @Override
                    public boolean test(String s) throws Exception {
                        return s.equals("ccc");
                    }
                })
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String aLong) throws Exception {
                        print("takeWhile Accept:"+aLong);
                    }
                });
//        11-07 14:29:07.806 6302-6302/com.callanna.demo D/duanyl: skipWhile  doOnSubscribe
//        11-07 14:29:07.806 6302-6302/com.callanna.demo D/duanyl: skipWhile Accept:aaa
//        11-07 14:29:07.806 6302-6302/com.callanna.demo D/duanyl: skipWhile Accept:bb
//        11-07 14:29:07.806 6302-6302/com.callanna.demo D/duanyl: skipWhile Accept:ccc
//        11-07 14:29:07.806 6302-6302/com.callanna.demo D/duanyl: skipWhile Accept:dd
//        11-07 14:29:07.806 6302-6302/com.callanna.demo D/duanyl: skipWhile Accept:eee
//        11-07 14:29:07.816 6302-6302/com.callanna.demo D/duanyl: skipWhile Accept:fff
//        11-07 14:29:07.816 6302-6302/com.callanna.demo D/duanyl: skipWhile Accept:gg
//        11-07 14:29:07.816 6302-6302/com.callanna.demo D/duanyl: takeWhile  doOnSubscribe
        Observable.fromArray(arr)
                .doOnSubscribe(new Consumer<Disposable>() {
                    @Override
                    public void accept(Disposable disposable) throws Exception {
                        print("skipWhile  doOnSubscribe");
                    }
                })
                .skipWhile(new Predicate<String>() {
                    @Override
                    public boolean test(String s) throws Exception {
                        return !s.equals("ccc");
                    }
                })
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String aLong) throws Exception {
                        print("skipWhile Accept:"+aLong);
                    }
                });

        Observable.fromArray(arr)
                .doOnSubscribe(new Consumer<Disposable>() {
                    @Override
                    public void accept(Disposable disposable) throws Exception {
                        print("takeWhile  doOnSubscribe");
                    }
                })
                .takeWhile(new Predicate<String>() {
                    @Override
                    public boolean test(String s) throws Exception {
                        return !s.equals("ccc");
                    }
                })
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String aLong) throws Exception {
                        print("takeWhile Accept:"+aLong);
                    }
                });

//        11-07 14:29:07.816 6302-6302/com.callanna.demo D/duanyl: skipWhile  doOnSubscribe
//        11-07 14:29:07.816 6302-6302/com.callanna.demo D/duanyl: skipWhile Accept:ccc
//        11-07 14:29:07.816 6302-6302/com.callanna.demo D/duanyl: skipWhile Accept:dd
//        11-07 14:29:07.816 6302-6302/com.callanna.demo D/duanyl: skipWhile Accept:eee
//        11-07 14:29:07.816 6302-6302/com.callanna.demo D/duanyl: skipWhile Accept:fff
//        11-07 14:29:07.816 6302-6302/com.callanna.demo D/duanyl: skipWhile Accept:gg
//        11-07 14:29:07.816 6302-6302/com.callanna.demo D/duanyl: takeWhile  doOnSubscribe
//        11-07 14:29:07.816 6302-6302/com.callanna.demo D/duanyl: takeWhile Accept:aaa
//        11-07 14:29:07.816 6302-6302/com.callanna.demo D/duanyl: takeWhile Accept:bb
    }
}
