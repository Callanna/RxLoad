package com.callanna.demo.fragment;

import android.os.Bundle;
import android.os.Handler;
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

import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

/**
 * 描述了四个情景，来形象化的表达RxJava 的观察者模式
 */
public class SimpleFragment extends Fragment {
    Button btn_smaple1,btn_smaple2,btn_smaple3,btn_smaple4;
    TextView tx_console;
    public static SimpleFragment newInstance( ) {
        SimpleFragment fragment = new SimpleFragment();
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
       View view = inflater.inflate(R.layout.fragment_simple, container, false);
        btn_smaple1 =  view.findViewById(R.id.btn_act1);
        btn_smaple2 =  view.findViewById(R.id.btn_act2);
        btn_smaple3 =  view.findViewById(R.id.btn_act3);
        btn_smaple4 =  view.findViewById(R.id.btn_act4);
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
        return view;
    }

    private void doAct3() {
        //情景三   顾客下单---薯条，鸡排，西瓜汁---但是中途顾客不满意，没有退款就离开，但是厨师不知道依然做完三道菜
        tx_console.setText("doAct3 Current Thread:"+Thread.currentThread().getName());
        Observable.just("土豆","鸡肉","西瓜")
                .map(new Function<String, String>() {
                    @Override
                    public String apply(@NonNull   String s) throws Exception {
                        //to do some prepare,such as:打鸡蛋 wipe egg,洗青菜 wash vegetable,..

                        if(s.equals("土豆")){
                            s = "cook土豆-----薯条";
                        }else if(s.equals("鸡肉")){
                            s = "cook鸡肉-----炸鸡排";
                        }else{
                            s = "cook西瓜-----西瓜汁";
                        }
                        final String msg = "Observable 厨师:"+s;
                        Log.d("duanyl", msg);
                        tx_console.post(new Runnable() {
                            @Override
                            public void run() {
                                tx_console.append("\n "+msg);
                            }
                        });
                        return s;
                    }
                })
                .doOnSubscribe(new Consumer<Disposable>() {
                    @Override
                    public void accept(@NonNull Disposable disposable) throws Exception {
                        Log.d("duanyl", " 接单 收款" );
                        tx_console.post(new Runnable() {
                            @Override
                            public void run() {
                                tx_console.append("\n  接单 收款" );
                            }
                        });
                    }
                })
                .doOnError(new Consumer<Throwable>() {
                    @Override
                    public void accept(@NonNull Throwable throwable) throws Exception {
                        //上菜
                        Log.d("duanyl", " 退款" );
                        tx_console.post(new Runnable() {
                            @Override
                            public void run() {
                                tx_console.append("\n  退款" );
                            }
                        });
                    }
                })
                .doOnComplete(new Action() {
                    @Override
                    public void run() throws Exception {
                        Log.d("duanyl", " 上菜" );
                        tx_console.post(new Runnable() {
                            @Override
                            public void run() {
                                tx_console.append("\n    上菜" );
                            }
                        });
                    }
                })
                .flatMap(new Function<String, ObservableSource<String>>() {
                    @Override
                    public ObservableSource<String> apply(@NonNull final  String s) throws Exception {
                        return Observable.create(new ObservableOnSubscribe<String>() {
                            @Override
                            public void subscribe(@NonNull ObservableEmitter<String> e) throws Exception {
                                e.onNext(s.substring(s.indexOf("-----")));
                                e.onComplete();
                            }
                        });
                    }
                })
                .subscribeOn(Schedulers.io())//在厨房
                .observeOn(AndroidSchedulers.mainThread())//在餐厅
                .subscribe(new Observer<String>() {
                    Disposable disposable;
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        disposable = d;
                    }

                    @Override
                    public void onNext(@NonNull final String s) {
                        Log.d("duanyl", "Observer 顾客 onNext:  eat " + s);
                        tx_console.post(new Runnable() {
                            @Override
                            public void run() {
                                tx_console.append("\nObserver 顾客 onNext:  eat " + s);
                            }
                        });
                        Log.d("duanyl", "Observer 顾客 离开");
                        tx_console.post(new Runnable() {
                            @Override
                            public void run() {
                                tx_console.append("\nObserver 顾客 离开");
                            }
                        });
                        disposable.dispose();
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {

                    }

                    @Override
                    public void onComplete() {
                        Log.d("duanyl", "Observer 顾客 离开");
                        tx_console.post(new Runnable() {
                            @Override
                            public void run() {
                                tx_console.append("\nObserver 顾客 离开");
                            }
                        });
                    }
                });
    }
    //情景二   顾客下单---薯条，鸡排，西瓜汁---但是中途厨师烫伤，顾客知道后，退款离开
    private void doAct2() {
        tx_console.setText("doAct2 Current Thread:"+Thread.currentThread().getName());
        Observable.just("土豆","鸡肉","西瓜")
                .map(new Function<String, String>() {
                    @Override
                    public String apply(@NonNull   String s) throws Exception {
                        //to do some prepare,such as:打鸡蛋 wipe egg,洗青菜 wash vegetable,..

                        if(s.equals("土豆")){
                            s = "cook土豆-----薯条";
                        }else if(s.equals("鸡肉")){
                            s = "cook鸡肉-----炸鸡排";
                        }else{
                            s = "cook西瓜-----西瓜汁";
                        }

                        final String msg = "Observable 厨师:"+s;
                        Log.d("duanyl", msg);
                        tx_console.post(new Runnable() {
                            @Override
                            public void run() {
                                tx_console.append("\n "+msg);
                            }
                        });
                        return s;
                    }
                })
                .flatMap(new Function<String, ObservableSource<String>>() {
                    @Override
                    public ObservableSource<String> apply(@NonNull final  String s) throws Exception {
                        return Observable.create(new ObservableOnSubscribe<String>() {
                            @Override
                            public void subscribe(@NonNull ObservableEmitter<String> e) throws Exception {
                                e.onNext(s.substring(s.indexOf("-----")));
                                if(s.contains("鸡肉")){
                                    e.onError(new Throwable("厨师烫伤"));
                                }
                                e.onComplete();
                            }
                        });
                    }
                })
                .doOnSubscribe(new Consumer<Disposable>() {
                    @Override
                    public void accept(@NonNull Disposable disposable) throws Exception {
                        Log.d("duanyl", " 接单 收款" );
                        tx_console.post(new Runnable() {
                            @Override
                            public void run() {
                                tx_console.append("\n  接单 收款" );
                            }
                        });
                    }
                })
                .doOnError(new Consumer<Throwable>() {
                    @Override
                    public void accept(@NonNull Throwable throwable) throws Exception {
                        //上菜
                        Log.d("duanyl", " 退款" );
                        tx_console.post(new Runnable() {
                            @Override
                            public void run() {
                                tx_console.append("\n  退款" );
                            }
                        });
                    }
                })
                .doOnComplete(new Action() {
                    @Override
                    public void run() throws Exception {
                        Log.d("duanyl", " 上菜" );
                        tx_console.post(new Runnable() {
                            @Override
                            public void run() {
                                tx_console.append("\n    上菜" );
                            }
                        });
                    }
                })
                .subscribeOn(Schedulers.io())//在厨房
                .observeOn(AndroidSchedulers.mainThread())//在餐厅
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(@NonNull final String s) throws Exception {
                        Log.d("duanyl", "Observer 顾客 onNext:  eat " + s);
                        tx_console.post(new Runnable() {
                            @Override
                            public void run() {
                                tx_console.append("\nObserver 顾客 onNext:  eat " + s);
                            }
                        });
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(@NonNull final Throwable throwable) throws Exception {
                        Log.d("duanyl", "Observer 顾客 onError: 知道" + throwable.getMessage());
                        tx_console.post(new Runnable() {
                            @Override
                            public void run() {
                                tx_console.append("\nObserver 顾客 onError: 知道" + throwable.getMessage());
                            }
                        });
                    }
                }, new Action() {
                    @Override
                    public void run() throws Exception {
                        Log.d("duanyl", "Observer 顾客  : 吃完" );
                        tx_console.post(new Runnable() {
                            @Override
                            public void run() {
                                tx_console.append("\nObserver 顾客  : 吃完"  );
                            }
                        });
                    }
                });
    }
    //情景一  顾客下单---西红柿鸡蛋炒面
    public void doAct1(){
        tx_console.setText("Current Thread:"+Thread.currentThread().getName());
        Observable.just("noodles","egg","vegetable")
                .map(new Function<String, String>() {
                    @Override
                    public String apply(@NonNull final String s) throws Exception {
                        //to do some prepare,such as:打鸡蛋 wipe egg,洗青菜 wash vegetable,..

                        Log.d("duanyl", "Observable 厨师: to do some prepare "+s );
                        tx_console.post(new Runnable() {
                            @Override
                            public void run() {
                                tx_console.append("\nObservable 厨师: to do some prepare "+s);
                            }
                        });
                        return s;
                    }
                })//using reduce to add all the String
                .reduce(new BiFunction<String, String, String>() {
                    @Override
                    public String apply(@NonNull final String s, @NonNull final String s2) throws Exception {
                        //cook

                        Log.d("duanyl", "Observable 厨师: to cook" +s+"," +s2);
                        tx_console.post(new Runnable() {
                            @Override
                            public void run() {
                                tx_console.append("\nObservable 厨师: to cook"+s+"," +s2);
                            }
                        });
                        return s+"," +s2;
                    }
               })
                .flatMapObservable(new Function<String, ObservableSource<String>>() {
                    @Override
                    public ObservableSource<String> apply(@NonNull String s) throws Exception {

                        return Observable.just(s+" +盘子---鸡蛋西红柿炒面)");
                    }
                })
                .doAfterTerminate(new Action() {
                    @Override
                    public void run() throws Exception {
                        //上菜
                        Log.d("duanyl", " 上菜" );
                        tx_console.post(new Runnable() {
                            @Override
                            public void run() {
                                tx_console.append("\n  上菜" );
                            }
                        });
                    }
                })
                .subscribeOn(Schedulers.io())//在厨房
                .observeOn(AndroidSchedulers.mainThread())//在餐厅
                 .subscribe(new Observer<String>() {
                     @Override
                     public void onSubscribe(@NonNull Disposable d) {
                         Log.d("duanyl", " 下单" );
                         tx_console.post(new Runnable() {
                             @Override
                             public void run() {
                                 tx_console.append("\n  下单" );
                             }
                         });
                     }

                     @Override
                     public void onNext(@NonNull final String s) {
                         Log.d("duanyl", "Observer 顾客 onNext:  eat "+s );
                        tx_console.post(new Runnable() {
                            @Override
                            public void run() {
                                tx_console.append("\nObserver 顾客 onNext:  eat "+s );
                            }
                        });
                     }

                     @Override
                     public void onError(@NonNull final Throwable e) {
                         Log.d("duanyl", "Observer 顾客 onError: 知道"+e.getMessage() );
                        tx_console.post(new Runnable() {
                            @Override
                            public void run() {
                                tx_console.append("\nObserver 顾客 onError: 知道"+e.getMessage()  );
                            }
                        });
                     }

                     @Override
                     public void onComplete() {
                         Log.d("duanyl", "Observer 顾客 onComplete: 吃完付钱" );
                        tx_console.post(new Runnable() {
                            @Override
                            public void run() {
                                tx_console.append("\nObserver 顾客 onComplete: 吃完付钱"  );
                            }
                        });
                     }
                 });
   }
    //情景四 顾客下单---"薯条","炸鸡排","西瓜汁","紫米粥"----并且顾客要求吃完一道在上一道菜
    public void doAct4(){
        tx_console.setText("doAct4 Current Thread:"+Thread.currentThread().getName());
        Flowable.just("土豆","鸡肉","紫米","西瓜")
                .map(new Function<String, String>() {
                    @Override
                    public String apply(@NonNull   String s) throws Exception {
                        //to do some prepare,such as:打鸡蛋 wipe egg,洗青菜 wash vegetable,..

                        if(s.equals("土豆")){
                            s = "cook土豆-----薯条";
                        }else if(s.equals("鸡肉")){
                            s = "cook鸡肉-----炸鸡排";
                        }else if(s.equals("西瓜")){
                            s = "cook西瓜-----西瓜汁";
                        }else{
                            s = "cook紫米-----紫米粥";
                        }

                        final String msg = "Observable 厨师:"+s;
                        Log.d("duanyl", msg);
                        tx_console.post(new Runnable() {
                            @Override
                            public void run() {
                                tx_console.append("\n "+msg);
                            }
                        });
                        return s;
                    }
                })

                .doOnSubscribe(new Consumer<Subscription>() {
                    @Override
                    public void accept(@NonNull Subscription subscription) throws Exception {
                        Log.d("duanyl", " 接单 收款" );
                        tx_console.post(new Runnable() {
                            @Override
                            public void run() {
                                tx_console.append("\n  接单 收款" );
                            }
                        });
                    }
                })
                .doOnError(new Consumer<Throwable>() {
                    @Override
                    public void accept(@NonNull Throwable throwable) throws Exception {
                        //上菜
                        Log.d("duanyl", " 退款" );
                        tx_console.post(new Runnable() {
                            @Override
                            public void run() {
                                tx_console.append("\n  退款" );
                            }
                        });
                    }
                })
                .doOnComplete(new Action() {
                    @Override
                    public void run() throws Exception {
                        Log.d("duanyl", " 上菜" );
                        tx_console.post(new Runnable() {
                            @Override
                            public void run() {
                                tx_console.append("\n    上菜" );
                            }
                        });
                    }
                })
                .onBackpressureBuffer()
                .subscribeOn(Schedulers.io())//在厨房
                .observeOn(AndroidSchedulers.mainThread())//在餐厅
                .subscribe(new Subscriber<String>() {
                    Subscription subscription;
                    @Override
                    public void onSubscribe(Subscription s) {
                        subscription = s;
                        subscription.request(1);
                    }

                    @Override
                    public void onNext(final String s) {
                        Log.d("duanyl", "Observer 顾客 onNext:  eat " + s);
                        tx_console.post(new Runnable() {
                            @Override
                            public void run() {
                                tx_console.append("\nObserver 顾客 onNext:  eat " + s);
                            }
                        });
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Log.d("duanyl", "Observer 顾客 onNext:  吃完了 " + s);
                                tx_console.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        tx_console.append("\nObserver 顾客 onNext:  吃完了 " + s);
                                    }
                                });
                                subscription.request(1);
                            }
                        },5000);
                    }

                    @Override
                    public void onError(Throwable t) {

                    }

                    @Override
                    public void onComplete() {
                        Log.d("duanyl", "Observer 顾客 onComplete: 菜全部上完" );
                        tx_console.post(new Runnable() {
                            @Override
                            public void run() {
                                tx_console.append("\nObserver 顾客 onComplete: 菜全部上完"  );
                            }
                        });
                    }
                });
    }


}
