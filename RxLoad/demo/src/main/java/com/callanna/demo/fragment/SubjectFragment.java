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

        source.subscribe(getObserver("First","BehaviorSubject"));
        // it will get 1, 2, 3, 4 and onComplete

        source.onNext(1);
        source.onNext(2);
        source.onNext(3);

        /*
         * it will emit 3(last emitted), 4 and onComplete for second observer also.
         */
        source.subscribe(getObserver("Second","BehaviorSubject"));

        source.onNext(4);
        source.onComplete();
         /*
         * it will emit nothing .
         */
        source.subscribe(getObserver("Third","BehaviorSubject"));
/**
 *
  BehaviorSubject-->First onSubscribe
  BehaviorSubject-->First onNext : value :1
  BehaviorSubject-->First onNext : value :2
  BehaviorSubject-->First onNext : value :3
  BehaviorSubject-->Second:onSubscribe
  BehaviorSubject-->Second onNext : value :3
  BehaviorSubject-->First onNext : value :4
  BehaviorSubject-->Second onNext : value :4
  BehaviorSubject-->First onComplete
  BehaviorSubject-->Second onComplete
  BehaviorSubject-->Third:onSubscribe
  BehaviorSubject-->Third onComplete
 */
    }
    /* An AsyncSubject emits the last value (and only the last value) emitted by the source
         * Observable, and only after that source Observable completes. (If the source Observable
         * does not emit any values, the AsyncSubject also completes without emitting any values.)
         */
    private void doAct2() {
        tx_console.setText("----------AsyncSubject----------");
        AsyncSubject<Integer> source = AsyncSubject.create();

        source.subscribe(getObserver("First","AsyncSubject"));
        // it will emit only 4 and onComplete

        source.onNext(1);
        source.onNext(2);
        source.onNext(3);

        /*
         * it will emit 4 and onComplete for second observer also.
         */
        source.subscribe(getObserver("Second","AsyncSubject"));

        source.onNext(4);
        source.onComplete();
         /*
         * it will emit nothing.
         */
        source.subscribe(getObserver("Third","AsyncSubject"));
        /**
         *
           AsyncSubject-->First onSubscribe
           AsyncSubject-->Second:onSubscribe
           AsyncSubject-->First onNext : value :4
           AsyncSubject-->First onComplete
           AsyncSubject-->Second onNext : value :4
           AsyncSubject-->Second onComplete
           AsyncSubject-->Third:onSubscribe
           AsyncSubject-->Third onNext : value :4
           AsyncSubject-->Third onComplete
         */
    }
    /* PublishSubject emits to an observer only those items that are emitted
        * by the source Observable, subsequent to the time of the subscription.
        */
    private void doAct3() {
        tx_console.setText("----------PublishSubject----------");
        PublishSubject<Integer> source = PublishSubject.create();

        source.subscribe(getObserver("First","PublishSubject"));
        // it will get 1, 2, 3, 4 and onComplete

        source.onNext(1);
        source.onNext(2);
        source.onNext(3);
        /*
         * it will emit 4 and onComplete for second observer also.
         */
        source.subscribe(getObserver("Second","PublishSubject"));

        source.onNext(4);
        source.onComplete();
         /*
         * it will emit nothing.
         */
        source.subscribe(getObserver("Third","PublishSubject"));
        /**
         * PublishSubject-->First onSubscribe
           PublishSubject-->First onNext : value :1
           PublishSubject-->First onNext : value :2
           PublishSubject-->First onNext : value :3
           PublishSubject-->Second:onSubscribe
           PublishSubject-->First onNext : value :4
           PublishSubject-->Second onNext : value :4
           PublishSubject-->First onComplete
           PublishSubject-->Second onComplete
           PublishSubject-->Third:onSubscribe
           PublishSubject-->Third onComplete
         */
    }

    /* ReplaySubject emits to any observer all of the items that were emitted
     * by the source Observable, regardless of when the observer subscribes.
     */
    private void doAct4() {
        tx_console.setText("----------ReplaySubject----------");
        ReplaySubject<Integer> source = ReplaySubject.create();

        source.subscribe(getObserver("First","ReplaySubject")); // it will get 1, 2, 3, 4

        source.onNext(1);
        source.onNext(2);
        source.onNext(3);
          /*
         * it will emit 1, 2, 3, 4 for second observer also as we have used replay
         */
        source.subscribe(getObserver("Second","ReplaySubject"));
        source.onNext(4);
        source.onComplete();

        /*
         * it will emit 1, 2, 3, 4 for second observer also as we have used replay
         */
        source.subscribe(getObserver("Third","ReplaySubject"));
        /**
         * ReplaySubject-->First onSubscribe
           ReplaySubject-->First onNext : value :1
           ReplaySubject-->First onNext : value :2
           ReplaySubject-->First onNext : value :3
           ReplaySubject-->Second:onSubscribe
           ReplaySubject-->Second onNext : value :1
           ReplaySubject-->Second onNext : value :2
           ReplaySubject-->Second onNext : value :3
           ReplaySubject-->First onNext : value :4
           ReplaySubject-->Second onNext : value :4
           ReplaySubject-->First onComplete
           ReplaySubject-->Second onComplete
           ReplaySubject-->Third:onSubscribe
           ReplaySubject-->Third onNext : value :1
           ReplaySubject-->Third onNext : value :2
           ReplaySubject-->Third onNext : value :3
           ReplaySubject-->Third onNext : value :4
           ReplaySubject-->Third onComplete
         */

    }
    private Observer<Integer> getObserver(final String id,final String tag) {
        return new Observer<Integer>() {

            @Override
            public void onSubscribe(Disposable d) {
               print(tag+"-->"+id+"  onSubscribe");
            }

            @Override
            public void onNext(Integer value) {
                print(tag+"-->"+id+" onNext : value :"+value);

            }

            @Override
            public void onError(Throwable e) {
                print(tag+"-->"+id+"  onError :  "  + e.getMessage());

            }

            @Override
            public void onComplete() {
                print(tag+"-->"+id+"  onComplete  "  );
            }
        };
    }


}
