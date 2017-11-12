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

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;

/**
 * Observable/Observer
 */
public class ObservableFragment extends Fragment {
    Button btn_smaple1, btn_smaple2,btn_smaple3, btn_smaple4;
    TextView tx_console;
    String[] arr = new String[]{"a", "b", "c", "d", "e"};
    List<String> list_str = Arrays.asList(arr);

    public static ObservableFragment newInstance() {
        ObservableFragment fragment = new ObservableFragment();
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
        View view = inflater.inflate(R.layout.fragment_simple3, container, false);
        btn_smaple1 = view.findViewById(R.id.btn_act1);
        btn_smaple2 = view.findViewById(R.id.btn_act2);
        btn_smaple3 = view.findViewById(R.id.btn_act3);
        btn_smaple4 = view.findViewById(R.id.btn_act4);
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
                toAct4();
            }
        });
        return view;
    }

    private void printThread(final String tag) {
        final String th = Thread.currentThread().getName();
        Log.d("duanyl", tag + ":-------" + th);
        tx_console.post(new Runnable() {
            @Override
            public void run() {
                tx_console.append("\n" + tag + ":-------" + th);
            }
        });
    }

    private Function<String, ObservableSource<String>> function = new Function<String, ObservableSource<String>>() {
        @Override
        public ObservableSource<String> apply(@NonNull String s) throws Exception {
            printThread("Observable flatMap" + s + ":");
            return Observable.just(s);
        }
    };
    private Consumer<Integer> consumer = new Consumer<Integer>() {
        @Override
        public void accept(@NonNull Integer s) throws Exception {
            printThread("Observer onNext" + s + ":");
        }
    };
    private Consumer<Long> consumer1 = new Consumer<Long>() {
        @Override
        public void accept(@NonNull Long s) throws Exception {
            printThread("Observer onNext" + s + ":");
        }
    };
    private Consumer<String> consumer2 = new Consumer<String>() {
        @Override
        public void accept(@NonNull String s) throws Exception {
            printThread("Observer onNext" + s + ":");
        }
    };
    ObservableEmitter observableEmitter;

    private void sendData() {
        if (observableEmitter != null) {
            for (int i = 0; i < 10; i++) {
                //数据发射源对象发射数据
                observableEmitter.onNext("Observable  数据data " + i);
            }
            observableEmitter.onComplete();
        }
    }

    private ObservableOnSubscribe<String> observableOnSubscribe = new ObservableOnSubscribe<String>() {
        @Override
        public void subscribe(@NonNull ObservableEmitter<String> e) throws Exception {
            printThread("subscribe");
            observableEmitter = e;
            observableEmitter.onNext("Observable  数据data");
            sendData();
        }
    };

    private void doAct1() {
        //create
        Observable.create(observableOnSubscribe).subscribe(consumer2);
        //defer
        String test="old data";
        final Observable observable=Observable.just(test);

        Observable observable_defer =  Observable.defer(new Callable<ObservableSource<String>>() {
            @Override
            public ObservableSource<String> call() throws Exception {
                return observable;
            }
        });
        test="new data";
        observable.subscribe(consumer2);
        observable_defer.subscribe(consumer2);

        //just
        Observable.just(1, 2, 3, 4, 5, 6, 7, 8, 9, 10).subscribe(consumer);
        //range
        Observable.range(0, 100).subscribe(consumer);


    }

    private void doAct2() {

        //from
        Observable.fromArray(arr).subscribe(consumer2);

        Observable.fromIterable(list_str).subscribe(consumer2);
        Observable.fromPublisher(new Publisher<String>() {
            @Override
            public void subscribe(Subscriber<? super String> s) {

            }
        });
        Observable.fromFuture(new Future<Integer>() {
            @Override
            public boolean cancel(boolean b) {
                return false;
            }

            @Override
            public boolean isCancelled() {
                return false;
            }

            @Override
            public boolean isDone() {
                return false;
            }

            @Override
            public Integer get() throws InterruptedException, ExecutionException {
                return 1;
            }

            @Override
            public Integer get(long l, @android.support.annotation.NonNull TimeUnit timeUnit) throws InterruptedException, ExecutionException, TimeoutException {
                return 1;
            }
        }, 5, TimeUnit.SECONDS).subscribe(consumer);
        Observable.fromCallable(new Callable<String>() {
            @Override
            public String call() throws Exception {
                return "from call";
            }
        }).subscribe(consumer2);


    }
    CompositeDisposable disposables = new CompositeDisposable();
    private void doAct3() {
        //3 秒后发射数字0
        disposables.add(Observable.timer(3,TimeUnit.SECONDS).subscribe(consumer1));
        //3 秒后发射数字0  数据在主线程发射
        disposables.add(Observable.timer(3,TimeUnit.SECONDS, AndroidSchedulers.mainThread()).subscribe(consumer1));
        //1秒后，每隔1秒发射从0发射，直到取消订阅
        disposables.add(Observable.interval(500,1000,TimeUnit.MILLISECONDS).subscribe(consumer1));
        //2秒后，每隔1秒发射从0发射，直到发射10个数据后停止
        disposables.add( Observable.intervalRange(10,10,2,1,TimeUnit.SECONDS).subscribe(consumer1));

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        disposables.clear();
    }

    public void toAct4(){
        //wrap
        Observable.wrap(new ObservableSource<String>() {
            @Override
            public void subscribe(@NonNull Observer<? super String> observer) {
                observer.onNext("haha");
            }
        }).subscribe(consumer2);
        //empty
        Observable.empty().subscribe(getObserver("Empty"));
        //nerver
        Observable.never().subscribe(getObserver("Never"));

        Observable.error(new TimeoutException()).subscribe(getObserver("Error"));

    }

    private Observer getObserver(final String tag){
       return new Observer<Object>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {
                printThread(tag+"  onSubscribe ");
            }

            @Override
            public void onNext(@NonNull Object o) {
                printThread(tag+" onNext "+o.toString());
            }

            @Override
            public void onError(@NonNull Throwable e) {
                printThread(tag+" onError "+e.getMessage());
            }

            @Override
            public void onComplete() {
                printThread(tag+" onComplete ");
            }
        };
    }
}
