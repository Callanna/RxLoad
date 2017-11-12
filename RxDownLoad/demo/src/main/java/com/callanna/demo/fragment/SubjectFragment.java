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

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.AsyncSubject;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.ReplaySubject;
/**
 * BehaviorSubject/PublishSubject/AsyncSubject/ReplaySubject
 */
public class SubjectFragment extends Fragment {
    Button btn_smaple1,btn_smaple2,btn_smaple3 ,btn_smaple4;
    TextView tx_console;
    public static SubjectFragment newInstance( ) {
        SubjectFragment fragment = new SubjectFragment();
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
       View view = inflater.inflate(R.layout.fragment_simple6, container, false);
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



    private void print(final String tag){
        Log.d("duanyl", tag );
        tx_console.post(new Runnable() {
            @Override
            public void run() {
                tx_console.append("\n"+tag );
            }
        });
    }
    /* When an observer subscribes to a BehaviorSubject, it begins by emitting the item most
    * recently emitted by the source Observable (or a seed/default value if none has yet been
    * emitted) and then continues to emit any other items emitted later by the source Observable(s).
    * It is different from Async Subject as async emits the last value (and only the last value)
    * but the Behavior Subject emits the last and the subsequent values also.
    */
    private void doAct1() {
        tx_console.setText("-------BehaviorSubject-----");
        BehaviorSubject<Integer> source = BehaviorSubject.create();

        source.subscribe(getFirstObserver("BehaviorSubject"));
        // it will get 1, 2, 3, 4 and onComplete

        source.onNext(1);
        source.onNext(2);
        source.onNext(3);

        /*
         * it will emit 3(last emitted), 4 and onComplete for second observer also.
         */
        source.subscribe(getSecondObserver("BehaviorSubject"));

        source.onNext(4);
        source.onComplete();
         /*
         * it will emit nothing .
         */
        source.subscribe(getThirdObserver("BehaviorSubject"));

    }
    /* An AsyncSubject emits the last value (and only the last value) emitted by the source
         * Observable, and only after that source Observable completes. (If the source Observable
         * does not emit any values, the AsyncSubject also completes without emitting any values.)
         */
    private void doAct2() {
        tx_console.setText("----------AsyncSubject----------");
        AsyncSubject<Integer> source = AsyncSubject.create();

        source.subscribe(getFirstObserver("AsyncSubject"));
        // it will emit only 4 and onComplete

        source.onNext(1);
        source.onNext(2);
        source.onNext(3);

        /*
         * it will emit 4 and onComplete for second observer also.
         */
        source.subscribe(getSecondObserver("AsyncSubject"));

        source.onNext(4);
        source.onComplete();
         /*
         * it will emit nothing.
         */
        source.subscribe(getThirdObserver("AsyncSubject"));
    }
    /* PublishSubject emits to an observer only those items that are emitted
        * by the source Observable, subsequent to the time of the subscription.
        */
    private void doAct3() {
        tx_console.setText("----------PublishSubject----------");
        PublishSubject<Integer> source = PublishSubject.create();

        source.subscribe(getFirstObserver("PublishSubject"));
        // it will get 1, 2, 3, 4 and onComplete

        source.onNext(1);
        source.onNext(2);
        source.onNext(3);
        /*
         * it will emit 4 and onComplete for second observer also.
         */
        source.subscribe(getSecondObserver("PublishSubject"));

        source.onNext(4);
        source.onComplete();
         /*
         * it will emit nothing.
         */
        source.subscribe(getThirdObserver("PublishSubject"));
    }

    /* ReplaySubject emits to any observer all of the items that were emitted
     * by the source Observable, regardless of when the observer subscribes.
     */
    private void doAct4() {
        tx_console.setText("----------ReplaySubject----------");
        ReplaySubject<Integer> source = ReplaySubject.create();

        source.subscribe(getFirstObserver("ReplaySubject")); // it will get 1, 2, 3, 4

        source.onNext(1);
        source.onNext(2);
        source.onNext(3);
          /*
         * it will emit 1, 2, 3, 4 for second observer also as we have used replay
         */
        source.subscribe(getSecondObserver("ReplaySubject"));
        source.onNext(4);
        source.onComplete();

        /*
         * it will emit 1, 2, 3, 4 for second observer also as we have used replay
         */
        source.subscribe(getThirdObserver("ReplaySubject"));

    }
    private Observer<Integer> getFirstObserver(final String tag) {
        return new Observer<Integer>() {

            @Override
            public void onSubscribe(Disposable d) {
               print(tag+"-->First onSubscribe");
            }

            @Override
            public void onNext(Integer value) {
                print(tag+"-->First onNext : value :"+value);

            }

            @Override
            public void onError(Throwable e) {
                print(tag+"-->First onError :  "  + e.getMessage());

            }

            @Override
            public void onComplete() {
                print(tag+"-->First onComplete  "  );
            }
        };
    }

    private Observer<Integer> getSecondObserver(final String tag) {
        return  new Observer<Integer>() {

            @Override
            public void onSubscribe(Disposable d) {
                print(tag+"-->Second:onSubscribe");
            }

            @Override
            public void onNext(Integer value) {
                print(tag+"-->Second onNext : value :"+value);

            }

            @Override
            public void onError(Throwable e) {
                print(tag+"-->Second onError :  "  + e.getMessage());

            }

            @Override
            public void onComplete() {
                print(tag+"-->Second onComplete   "  );
            }
        };
    }
    private Observer<Integer> getThirdObserver(final String tag) {
        return  new Observer<Integer>() {

            @Override
            public void onSubscribe(Disposable d) {
                print(tag+"-->Third:onSubscribe");
            }

            @Override
            public void onNext(Integer value) {
                print(tag+"-->Third onNext : value :"+value);

            }

            @Override
            public void onError(Throwable e) {
                print(tag+"-->Third onError :  "  + e.getMessage());

            }

            @Override
            public void onComplete() {
                print(tag+"-->Third onComplete   "  );
            }
        };
    }

}
