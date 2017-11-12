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

import io.reactivex.Completable;
import io.reactivex.CompletableEmitter;
import io.reactivex.CompletableObserver;
import io.reactivex.CompletableOnSubscribe;
import io.reactivex.CompletableSource;
import io.reactivex.Maybe;
import io.reactivex.MaybeEmitter;
import io.reactivex.MaybeObserver;
import io.reactivex.MaybeOnSubscribe;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleObserver;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;

/**
 * Completable/Maybe/Single
 */
public class SingleFragment extends Fragment {
    Button btn_smaple1,btn_smaple2,btn_smaple3,btn_smaple4,btn_smaple5,btn_smaple6;
    TextView tx_console;
    public static SingleFragment newInstance( ) {
        SingleFragment fragment = new SingleFragment();
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
       View view = inflater.inflate(R.layout.fragment_simple5, container, false);
        btn_smaple1 =  view.findViewById(R.id.btn_act1);
        btn_smaple2 =  view.findViewById(R.id.btn_act2);
        btn_smaple3 =  view.findViewById(R.id.btn_act3);
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


        return view;
    }



    private void print(final String tag){
        Log.d("duanyl", tag );
        tx_console.post(new Runnable() {
            @Override
            public void run() {
                tx_console.append("\n"+tag );
            }
        });
    }
    private void doAct1() {
        //-------------Completable
        tx_console.setText("Completable");
        Completable.create(new CompletableOnSubscribe() {
            @Override
            public void subscribe(@NonNull CompletableEmitter e) throws Exception {
                print("Completable0  subscribe");
                e.onComplete();
            }
        }).subscribe(new CompletableObserver() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {
                print("Completable0  onSubscribe");
            }

            @Override
            public void onComplete() {
                print("Completable0  onComplete");
            }

            @Override
            public void onError(@NonNull Throwable e) {
                print("Completable0  onError");
            }
        });

        Completable.unsafeCreate(new CompletableSource() {
            @Override
            public void subscribe(@NonNull CompletableObserver cs) {
                print("Completable1  subscribe");
                cs.onComplete();
            }
        }).subscribe(new CompletableObserver() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {
                print("Completable1  onSubscribe");
            }

            @Override
            public void onComplete() {
               print("Completable1  onComplete");
            }

            @Override
            public void onError(@NonNull Throwable e) {
                print("Completable1  onError");
            }
        });

        Completable.complete().subscribe(new Action() {
            @Override
            public void run() throws Exception {
                print("Completable2  complete");
            }
        });
        Completable.timer(1, TimeUnit.SECONDS).subscribe(new Action() {
            @Override
            public void run() throws Exception {
                print("Completable3  timer");
            }
        });

    }

    private void doAct2() {
        tx_console.setText("Single");
        Single.create(new SingleOnSubscribe<String>() {
            @Override
            public void subscribe(@NonNull SingleEmitter<String> e) throws Exception {
                e.onSuccess("----Single 11");
                e.onSuccess("----Single 111");
                e.onSuccess("----Single 1111");
            }
        }).subscribe(new SingleObserver<String>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {
                print("Single1    onSubscribe"  );
            }

            @Override
            public void onSuccess(@NonNull String s) {
                print("Single1    onSuccess" +s);
            }

            @Override
            public void onError(@NonNull Throwable e) {

            }
        });
        Single.just(10)
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(@NonNull Integer integer) throws Exception {
                        print("Single2  just onSuccess"+integer);
                    }
                });
    }

    private void doAct3() {
        tx_console.setText("Maybe");
        Maybe.create(new MaybeOnSubscribe<String>() {
            @Override
            public void subscribe(@NonNull MaybeEmitter<String> e) throws Exception {
                print("Maybe0    subscribe" );
                e.onComplete();
                e.onSuccess("maybe one");
                e.onSuccess("maybe two");
                e.onSuccess("maybe three");

            }
        }).subscribe(new MaybeObserver<String>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {
                print("Maybe0    onSubscribe" );
            }

            @Override
            public void onSuccess(@NonNull String s) {
                print("Maybe0    onSuccess" +s);
            }

            @Override
            public void onError(@NonNull Throwable e) {
                print("Maybe0    onError"  );
            }

            @Override
            public void onComplete() {
                print("Maybe0    onComplete"  );
            }
        });
        Maybe.just(true).subscribe(new MaybeObserver<Boolean>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {
                print("Maybe1  just onSubscribe" );
            }

            @Override
            public void onSuccess(@NonNull Boolean aBoolean) {
                print("Maybe1  just onSuccess"+aBoolean );
            }

            @Override
            public void onError(@NonNull Throwable e) {
                print("Maybe1  just onError"  );
            }

            @Override
            public void onComplete() {
                print("Maybe1  just onComplete"  );
            }
        });
    }


}
