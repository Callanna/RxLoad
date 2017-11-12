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
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Predicate;

/**
 * Bool Observables Operators
 */
public class BoolOperatorsFragment extends Fragment {
    Button btn_smaple1, btn_smaple2, btn_smaple3, btn_smaple4, btn_smaple5, btn_smaple6;
    TextView tx_console;

    public static BoolOperatorsFragment newInstance() {
        BoolOperatorsFragment fragment = new BoolOperatorsFragment();
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
        View view = inflater.inflate(R.layout.fragment_simple10, container, false);
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
        tx_console.setText("all");
        Observable.fromArray(new String[]{"aaa","abb","ac","ad"})
                .all(new Predicate<String>() {
                    @Override
                    public boolean test(String s) throws Exception {
                        return s.contains("a");
                    }
                }).subscribe(new Consumer<Boolean>() {
            @Override
            public void accept(Boolean aBoolean) throws Exception {
                print("all accept:"+aBoolean);//true
            }
        });


    }
    private void doAct2() {
        //-------------sample operator------
        tx_console.setText("any");
        Observable.fromArray(new String[]{"aaa","abb","ac","ad"})
                .any(new Predicate<String>() {
                    @Override
                    public boolean test(String s) throws Exception {
                        return s.contains("e");
                    }
                }).subscribe(new Consumer<Boolean>() {
            @Override
            public void accept(Boolean aBoolean) throws Exception {
                print("any accept:"+aBoolean);//false
            }
        });
    }


    private void doAct3() {
        //-------------FlatMap operator------
        tx_console.setText("contains");
        Observable.fromArray(new String[]{"aaa","abb","ac","ad"})
                .contains("aaa").subscribe(new Consumer<Boolean>() {
            @Override
            public void accept(Boolean aBoolean) throws Exception {
                print("contains accept:"+aBoolean);//true
            }
        });
    }

    String[] arr = new String[]{"aaa", "bb", "ccc", "dd", "eee","fff","gg"};
    private void doAct4() {
        //-------------Last operator------
        tx_console.setText("defaultIfEmpty");
        Observable.fromArray(new String[]{})

                .defaultIfEmpty("--").subscribe(new Consumer<String>() {
            @Override
            public void accept(String s) throws Exception {
                print("defaultIfEmpty accept:"+s);//aaa,abb,ac,ad
            }
        });
    }

    private void doAct5() {
        // -------------Until
        tx_console.setText("equals");
        Observable.sequenceEqual( Observable.intervalRange(100,10,0,0, TimeUnit.MILLISECONDS),
                Observable.intervalRange(100,10,0,0, TimeUnit.MILLISECONDS)).subscribe(new Consumer<Boolean>() {
            @Override
            public void accept(Boolean aBoolean) throws Exception {
                print("sequenceEqual accept:"+aBoolean);//true
            }
        });

    }

    private void doAct6() {
        //------------While
        tx_console.setText("amb ");
        Observable.ambArray(
                Observable.intervalRange(100,10,300,0, TimeUnit.MILLISECONDS),
                Observable.intervalRange(0,10,0,0, TimeUnit.MILLISECONDS)).subscribe(
                new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        print("1 amb accpet :"+aLong);//0,1,2,3,4,5,6,7,8,9
                    }
                }
        );
        Observable.intervalRange(100,10,0,0, TimeUnit.MILLISECONDS)
                .ambWith(Observable.intervalRange(0,10,1000,0, TimeUnit.MILLISECONDS))
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        print( "2 amb accpet :"+aLong);//100,101,102,103,104,105,106,107,108,109
                    }
                });
    }
}
