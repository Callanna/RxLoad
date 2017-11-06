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

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.observables.GroupedObservable;
import io.reactivex.schedulers.Schedulers;

/**
 * Transforming Observables
 */
public class Simple7Fragment extends Fragment {
    Button btn_smaple1, btn_smaple2, btn_smaple3, btn_smaple4, btn_smaple5, btn_smaple6;
    TextView tx_console;


    public static Simple7Fragment newInstance() {
        Simple7Fragment fragment = new Simple7Fragment();
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
        View view = inflater.inflate(R.layout.fragment_simple7, container, false);
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

    private Observer<String> getStringObserver(final String tag) {
        return new Observer<String>() {

            @Override
            public void onSubscribe(Disposable d) {
                print(tag + "-->String onSubscribe");
            }

            @Override
            public void onNext(String value) {
                print(tag + "-->String onNext : value :" + value);

            }

            @Override
            public void onError(Throwable e) {
                print(tag + "-->String onError :  " + e.getMessage());

            }

            @Override
            public void onComplete() {
                print(tag + "-->String onComplete  ");
            }
        };
    }

    private Observer<List<String>> getStringListObserver(final String tag) {
        return new Observer<List<String>>() {

            @Override
            public void onSubscribe(Disposable d) {
                print(tag + "-->StringList onSubscribe");
            }

            @Override
            public void onNext(List<String> value) {
                print(tag + "-->StringList onNext-----------" );
                for(int i = 0;i <value.size();i++ ) {
                    print(tag + "-->StringList onNext   value :"+value.get(i));
                }

            }

            @Override
            public void onError(Throwable e) {
                print(tag + "-->StringList onError :  " + e.getMessage());

            }

            @Override
            public void onComplete() {
                print(tag + "-->StringList onComplete  ");
            }
        };
    }


    private void doAct1() {
        //-------------buffer operator------
        tx_console.setText("buffer");
        String[] arr = new String[]{"a", "b", "c", "d", "e"};
        Observable.fromArray(arr)
                .buffer(3,1)
                .subscribe(getStringListObserver("buffer"));
        // 3 means,  每创建一个list的长度 （it takes max of three from its start index and create list）
        // 1 means, 每创建一个list,开始下表跳过的个数（it jumps one step every time）
        //输出
        // a,b,c
        // b,c,d
        // c,d,e
        // d,e
        Observable.fromArray(arr)
                .buffer(2)//默认skip 和count相同
                .subscribe(getStringListObserver("buffer"));
        //输出
        // a,b,
        // c,d,
        // e
    }
    private void doAct2() {
        //-------------Map operator------
        tx_console.setText("Map");
        Observable.just(1,2,3,4,5)
                .map(new Function<Integer, String>() {
                    @Override
                    public String apply(Integer integer) throws Exception {
                        return integer+".xxxxx";
                    }
                }).subscribe(new Consumer<String>() {
            @Override
            public void accept(String s) throws Exception {
                print(s);
            }
        });


    }


    private void doAct3() {
        //-------------FlatMap operator------
        tx_console.setText("FlatMap");
        Observable.just(1,2,3,4,5)
                .flatMap(new Function<Integer, ObservableSource<String>>() {
                    @Override
                    public ObservableSource<String> apply(final Integer integer) throws Exception {

                        return Observable.create(new ObservableOnSubscribe<String>() {
                            @Override
                            public void subscribe(ObservableEmitter<String> e) throws Exception {
                                e.onNext(integer+".x");
                                e.onNext(integer+".xx");
                                e.onNext(integer+".xxx");
                                e.onNext(integer+".xxxx");
                                e.onComplete();
                            }
                        });
                    }
                }).subscribe(new Consumer<String>() {
            @Override
            public void accept(String s) throws Exception {
                print("accept:"+s);
            }
        });
        Observable.just(1,2,3,4,5).flatMap(new Function<Integer, ObservableSource<Integer>>() {
            @Override
            public ObservableSource<Integer> apply(Integer integer) throws Exception {
                return Observable.just(integer * 2);
            }
        }, new BiFunction<Integer, Integer, Integer>() {
            @Override
            public Integer apply(Integer integer, Integer integer2) throws Exception {
                return integer + integer2;
            }
        }).subscribe(new Consumer<Integer>() {
            @Override
            public void accept(Integer integer) throws Exception {
                print("accept:"+integer);
            }
        });

    }


    private void doAct4() {

        //-------------GroupBy operator------
        tx_console.setText("GroupBy");
        String[] arr = new String[]{"aaa", "bb", "ccc", "dd", "eee"};
        Observable.fromArray(arr).groupBy(new Function<String, Boolean>() {
            @Override
            public Boolean apply(String s) throws Exception {
                return s.length()== 3;
            }
        }).subscribe(new Consumer<GroupedObservable<Boolean, String>>() {
            @Override
            public void accept(final GroupedObservable<Boolean, String> booleanStringGroupedObservable) throws Exception {
                if(booleanStringGroupedObservable.getKey()) {
                    booleanStringGroupedObservable.toList().subscribe(new Consumer<List<String>>() {
                        @Override
                        public void accept(List<String> strings) throws Exception {
                            print("key=" + booleanStringGroupedObservable.getKey() + ",val=" + strings);
                        }
                    });
                }else{
                    booleanStringGroupedObservable.subscribe(new Consumer<String>() {
                        @Override
                        public void accept(String s) throws Exception {
                            print("key=" + booleanStringGroupedObservable.getKey() + ",val=" + s+s);
                        }
                    });
                }
            }
        });
    }

    private void doAct5() {
        // -------------Scan
        tx_console.setText("Scan  ");
        Observable.just(1,2,3,4,5).scan(new BiFunction<Integer, Integer, Integer>() {
            @Override
            public Integer apply(Integer integer, Integer integer2) throws Exception {
                return integer+integer2;
            }
        }).subscribe(new Consumer<Integer>() {
            @Override
            public void accept(Integer integer) throws Exception {
                print(integer+"");
            }
        });
        Observable.fromArray(new String[]{"a","b","c","d","e"}).scan(new BiFunction<String, String, String>() {
            @Override
            public String apply(String s, String s2) throws Exception {
                return s+s2;
            }
        }).subscribe(getStringObserver("Scan"));
    }

    private void doAct6() {
        //------------Window
        tx_console.setText("Window ");
        Observable.interval(300, TimeUnit.MILLISECONDS).take(50)
                .window(5, TimeUnit.SECONDS)
                .subscribe(new Consumer<Observable<Long>>() {
                    @Override
                    public void accept(Observable<Long> longObservable) throws Exception {
                        print("Window accept---------");
                        longObservable.subscribe(new Consumer<Long>() {
                            @Override
                            public void accept(Long aLong) throws Exception {
                                print(aLong+"");
                            }
                        });
                    }
                });
    }
}
