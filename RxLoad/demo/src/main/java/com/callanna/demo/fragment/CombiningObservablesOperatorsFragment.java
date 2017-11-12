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
import io.reactivex.schedulers.Schedulers;

/**
 * Combining Observables Operators
 */
public class CombiningObservablesOperatorsFragment extends Fragment {
    Button btn_smaple1, btn_smaple2, btn_smaple3, btn_smaple4, btn_smaple5, btn_smaple6;
    TextView tx_console;

    public static CombiningObservablesOperatorsFragment newInstance() {
        CombiningObservablesOperatorsFragment fragment = new CombiningObservablesOperatorsFragment();
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
/**
 * combineLatest accept:10 * 100 = 1000
   combineLatest accept:10 * 101 = 1010
   combineLatest accept:10 * 102 = 1020
   combineLatest accept:10 * 103 = 1030
   combineLatest accept:10 * 104 = 1040
   combineLatest accept:10 * 105 = 1050
   combineLatest accept:10 * 106 = 1060
   combineLatest accept:10 * 107 = 1070
   combineLatest accept:10 * 108 = 1080
   combineLatest accept:10 * 109 = 1090
 */
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
/**
 *  withLatestFrom accept:4+109=113
   withLatestFrom accept:5+109=114
   withLatestFrom accept:6+109=115
   withLatestFrom accept:7+109=116
   withLatestFrom accept:8+109=117
   withLatestFrom accept:9+109=118
   withLatestFrom accept:10+109=119
 */
    }


    private void doAct3() {
        //-------------join operator------
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
                }).subscribeOn(Schedulers.computation())
                .subscribe(new Consumer<String>() {
            @Override
            public void accept(String s) throws Exception {
                print("join accept:"+s);
            }
        });
/**
 * join accept:1+100=101
   join accept:2+100=102
  join accept:3+100=103
  join accept:4+100=104
  join accept:5+100=105
  join accept:6+100=106
  join accept:7+100=107
  join accept:8+100=108
  join accept:9+100=109
   join accept:10+100=110
 */

    }

    private void doAct4() {

        //-------------merge operator------
        tx_console.setText("merge");

        Observable.just(11).merge(getFirstObservable(),getSecondObservable()).subscribe(new Consumer<Long>() {
            @Override
            public void accept(Long s) throws Exception {
                print("merge accept:"+s);//1,2,...10,100,101,...,109
            }
        });
        getFirstObservable()
                .mergeWith(getSecondObservable()).subscribe(new Consumer<Long>() {
            @Override
            public void accept(Long s) throws Exception {
                print("mergeWith accept:"+s);//1,2,...10,100,101,...,109
            }
        });

        Observable.concat(getFirstObservable(),getSecondObservable()).subscribe(new Consumer<Long>() {
            @Override
            public void accept(Long aLong) throws Exception {
                print("concat accept"+aLong);//1,2,...10,100,101,...,109
            }
        });


    }

    private void doAct5() {
        tx_console.setText("switchMap");
        getFirstObservable().switchMap(new Function<Long, ObservableSource<String>>() {
            @Override
            public ObservableSource<String> apply(Long s) throws Exception {
                return Observable.just(s+"*="+s*s);
            }
        }).subscribe(new Consumer<String>() {
            @Override
            public void accept(String s) throws Exception {
                print("switchMap accept:"+s);// 1,4,9,16,25,36,49,64,81,100
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
    /**
     * zip accept:1 * 100=100
       zip accept:2 * 101=202
       zip accept:3 * 102=306
       zip accept:4 * 103=412
       zip accept:5 * 104=520
       zip accept:6 * 105=630
       zip accept:7 * 106=742
       zip accept:8 * 107=856
       zip accept:9 * 108=972
       zip accept:10 * 109=1090
       zip 4 accept:1 + 100 + 1 + 100 = 11001100
       zip list accept:1100110011001100
       zip 4 accept:2 + 101 + 2 + 101 = 21012101
       zip list accept:2101210121012101
       zip 4 accept:3 + 102 + 3 + 102 = 31023102
       zip list accept:3102310231023102
       zip 4 accept:4 + 103 + 4 + 103 = 41034103
       zip list accept:4103410341034103
       zip 4 accept:5 + 104 + 5 + 104 = 51045104
       zip list accept:5104510451045104
       zip 4 accept:6 + 105 + 6 + 105 = 61056105
       zip list accept:6105610561056105
       zip 4 accept:7 + 106 + 7 + 106 = 71067106
       zip list accept:7106710671067106
       zip 4 accept:8 + 107 + 8 + 107 = 81078107
       zip list accept:8107810781078107
       zip 4 accept:9 + 108 + 9 + 108 = 91089108
       zip list accept:9108910891089108
       zip 4 accept:10 + 109 + 10 + 109 = 1010910109
       zip list accept:10109101091010910109
     */
}
