package com.callanna.demo.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.AndroidException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.callanna.demo.R;

import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.observables.GroupedObservable;
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


    }
    private void doAct2() {
        //-------------sample operator------
        tx_console.setText("sample");

        Observable.interval(300, TimeUnit.MILLISECONDS)
                .sample(1, TimeUnit.SECONDS, Schedulers.newThread())
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        print("Accept:"+aLong);
                    }
                });

        Observable.interval(300, TimeUnit.MILLISECONDS)
                .throttleFirst(1, TimeUnit.SECONDS, Schedulers.newThread())
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        print("Accept:"+aLong);
                    }
                });

        Observable.interval(300, TimeUnit.MILLISECONDS)
                .throttleLast(1, TimeUnit.SECONDS, Schedulers.newThread())
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        print("Accept:"+aLong);
                    }
                });


    }


    private void doAct3() {
        //-------------FlatMap operator------
        tx_console.setText("FlatMap");
        Observable.interval(300, TimeUnit.MILLISECONDS)
                .take(5)
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        print("Accept:"+aLong);
                    }
                });
        Observable.interval(300, TimeUnit.MILLISECONDS)
                .skip(5)
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        print("Accept:"+aLong);
                    }
                });
    }

    String[] arr = new String[]{"aaa", "bb", "ccc", "dd", "eee","fff","gg"};
    private void doAct4() {

        //-------------GroupBy operator------
        tx_console.setText("GroupBy");

        Observable.fromArray(arr)
                .skip(3)
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String aLong) throws Exception {
                        print("Accept:"+aLong);
                    }
                });
        Observable.fromArray(arr)
                .take(3)
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String aLong) throws Exception {
                        print("Accept:"+aLong);
                    }
                });

        Observable.fromArray(arr)
                .skipLast(3)
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String aLong) throws Exception {
                        print("Accept:"+aLong);
                    }
                });

        Observable.fromArray(arr)
                .takeLast(3)
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String aLong) throws Exception {
                        print("Accept:"+aLong);
                    }
                });

    }

    private void doAct5() {
        // -------------Scan
        tx_console.setText("Scan  ");
        Observable.fromArray(arr)
                .skipUntil(new ObservableSource<String>() {
                    @Override
                    public void subscribe(Observer<? super String> observer) {
                        observer.onNext("ccc");
                    }
                })
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String aLong) throws Exception {
                        print("Accept:"+aLong);
                    }
                });
        Observable.fromArray(arr)
                .takeUntil(new ObservableSource<String>() {
                    @Override
                    public void subscribe(Observer<? super String> observer) {
                        observer.onNext("ccc");
                    }
                })
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String aLong) throws Exception {
                        print("Accept:"+aLong);
                    }
                });
    }

    private void doAct6() {
        //------------Window
        tx_console.setText("Window ");


        Observable.fromArray(arr)
                .skipWhile(new Predicate<String>() {
                    @Override
                    public boolean test(String s) throws Exception {
                        return s.equals("ccc");
                    }
                })
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String aLong) throws Exception {
                        print("Accept:"+aLong);
                    }
                });
        Observable.fromArray(arr)
                .takeWhile(new Predicate<String>() {
                    @Override
                    public boolean test(String s) throws Exception {
                        return s.equals("ccc");
                    }
                })
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String aLong) throws Exception {
                        print("Accept:"+aLong);
                    }
                });
    }
}
