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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Function4;

/**
 * Filtering Observables
 */
public class Simple9Fragment extends Fragment {
    Button btn_smaple1, btn_smaple2, btn_smaple3, btn_smaple4, btn_smaple5, btn_smaple6;
    TextView tx_console;

    public static Simple9Fragment newInstance() {
        Simple9Fragment fragment = new Simple9Fragment();
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
        View view = inflater.inflate(R.layout.fragment_simple9, container, false);
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
        //需要引入RxJava 1.0
        //-------------buffer operator------
//        tx_console.setText("debounce");
//        String[] arr = new String[]{"a", "b", "c", "d", "e"};
//        String[] arr2 = new String[]{"-a", "-b", "-c", "-d", "-e"};
//        rx.Observable<String> obs_stu = rx.Observable.from(arr);
//        rx.Observable<Long> obs_time = rx.Observable.interval(1, TimeUnit.SECONDS);
//
//        Pattern2<String, Long> pattern = JoinObservable.from(obs_stu).and(obs_time);
//        Plan0<String> plan = pattern.then(new Func2<String, Long, String>() {
//            @Override
//            public String call(String s, Long aLong) {
//                return s+"---"+aLong;
//            }
//        });
//
//        JoinObservable
//                .when(plan)
//                .toObservable()
//                .subscribe(new Action1<String>() {
//                    @Override
//                    public void call(String s) {
//                        print(s);
//                    }
//                });

        Observable.combineLatest(getFirstObservable(), getSecondObservable(), new BiFunction<Long, Long, String>() {
            @Override
            public String apply(Long aLong, Long aLong2) throws Exception {
                return aLong +" * "+aLong2 +" = "+ aLong * aLong2;
            }
        }).subscribe(new Consumer<String>() {
            @Override
            public void accept(String s) throws Exception {
                print("combineLatest accept:"+s);
            }
        });

    }

    public Observable<Long> getFirstObservable(){
        return Observable.intervalRange(1,10,0,0, TimeUnit.MILLISECONDS);
    };
    public Observable<Long>  getSecondObservable(){
        return Observable.intervalRange(100,10,0,0, TimeUnit.MILLISECONDS);
    };

    private void doAct2() {
        //-------------sample operator------
        tx_console.setText("withLatestFrom");

        getFirstObservable()
          .withLatestFrom(getSecondObservable(), new BiFunction<Long, Long, String>() {
              @Override
              public String apply(Long aLong, Long aLong2) throws Exception {
                  return aLong +"+"+aLong2+"="+(aLong+aLong2);
              }
          }).subscribe(new Consumer<String>() {
            @Override
            public void accept(String s) throws Exception {
                print("withLatestFrom accept:"+s);
            }
        });

    }


    private void doAct3() {
        //-------------FlatMap operator------
        tx_console.setText("join");
        getFirstObservable()
                .join(getSecondObservable(), new Function<Long, ObservableSource<Long>>() {
                    @Override
                    public ObservableSource<Long> apply(Long aLong) throws Exception {
                        return Observable.just(aLong *2);
                    }
                }, new Function<Long, ObservableSource<Long>>() {
                    @Override
                    public ObservableSource<Long> apply(Long aLong) throws Exception {
                        return Observable.just(aLong *2);
                    }
                },new BiFunction<Long, Long, String>() {
                    @Override
                    public String apply(Long aLong, Long aLong2) throws Exception {
                        return aLong +"+"+aLong2+"="+(aLong+aLong2);
                    }
                }).subscribe(new Consumer<String>() {
            @Override
            public void accept(String s) throws Exception {
                print("join accept:"+s);
            }
        });


    }

    String[] arr = new String[]{"aaa", "bb", "ccc", "dd", "eee","fff","gg"};
    private void doAct4() {

        //-------------Last operator------
        tx_console.setText("merge");

        Observable.just(11).merge(getFirstObservable(),getSecondObservable()).subscribe(new Consumer<Long>() {
            @Override
            public void accept(Long s) throws Exception {
                print("merge accept:"+s);
            }
        });
        getFirstObservable()
                .mergeWith(getSecondObservable()).subscribe(new Consumer<Long>() {
            @Override
            public void accept(Long s) throws Exception {
                print("mergeWith accept:"+s);
            }
        });

        Observable.concat(getFirstObservable(),getSecondObservable()).subscribe(new Consumer<Long>() {
            @Override
            public void accept(Long aLong) throws Exception {
                print("concat accept"+aLong);
            }
        });


    }

    private void doAct5() {
        // -------------Until
        tx_console.setText("switchMap");
        getFirstObservable().switchMap(new Function<Long, ObservableSource<String>>() {
            @Override
            public ObservableSource<String> apply(Long s) throws Exception {
                return Observable.just(s+"*="+s*s);
            }
        }).subscribe(new Consumer<String>() {
            @Override
            public void accept(String s) throws Exception {
                print("switchMap accept:"+s);
            }
        });
    }

    private void doAct6() {
        //------------While
        tx_console.setText("While ");
        Observable.zip(getFirstObservable(), getSecondObservable(), new BiFunction<Long, Long, String>() {
            @Override
            public String apply(Long aLong, Long aLong2) throws Exception {
                return aLong+" * "+aLong2 +"="+aLong *aLong2;
            }
        }).subscribe(new Consumer<String>() {
            @Override
            public void accept(String s) throws Exception {
                print("zip accept:"+s);
            }
        });
        Observable.zip(getFirstObservable(), getSecondObservable(), getFirstObservable(), getSecondObservable(), new Function4<Long, Long, Long, Long, String>() {
            @Override
            public String apply(Long aLong, Long aLong2, Long aLong3, Long aLong4) throws Exception {
                return aLong+" + "+aLong2+" + "+aLong3+" + "+aLong4 +" = "+aLong +aLong2 +aLong3 +aLong4;
            }
        }).subscribe(new Consumer<String>() {
            @Override
            public void accept(String s) throws Exception {
                print("zip 4 accept:"+s);
            }
        });
        List<Observable<Long>> list = new ArrayList<>();
        list.add(getFirstObservable());
        list.add(getSecondObservable());
        list.add(getFirstObservable());
        list.add(getSecondObservable());
        list.add(getFirstObservable());
        list.add(getSecondObservable());
        list.add(getFirstObservable());
        list.add(getSecondObservable());

        Observable.zip(list, new Function<Object[], String>() {
            @Override
            public String apply(Object[] objects) throws Exception {
                String msg = "";
                for (int i = 0;i <objects.length;i++){
                    msg +=  objects[i] +"";
                }
                return msg;
            }
        }).subscribe(new Consumer<String>() {
            @Override
            public void accept(String o) throws Exception {
                print("zip list accept:"+o);
            }
        });
    }
}
