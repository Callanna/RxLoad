package com.callanna.rxdownload;

import android.text.TextUtils;
import android.util.Log;

import org.reactivestreams.Publisher;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.net.ConnectException;
import java.net.ProtocolException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.reactivex.Flowable;
import io.reactivex.FlowableTransformer;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;
import io.reactivex.functions.BiPredicate;
import okhttp3.internal.http.HttpHeaders;
import retrofit2.HttpException;
import retrofit2.Response;

import static java.lang.String.format;
import static java.util.Locale.getDefault;
import static java.util.TimeZone.getTimeZone;

/**
 * Author: Season(ssseasonnn@gmail.com)
 * Date: 2016/11/2
 * Time: 09:07
 * 工具类
 */
public class Utils {
    private static final String TAG = "RxDownLoad";
    private static final CharSequence CACHE = ".cache";
    private static final CharSequence TMP_SUFFIX = ".tmp";
    private static final CharSequence LMF_SUFFIX = ".lmf";
    public static final String REQUEST_RETRY_HINT = "Request";
    public static final String NORMAL_RETRY_HINT = "Normal download";
    public static final String RANGE_RETRY_HINT = "Range %d";
    public static final String RETRY_HINT = "%s get [%s] error, now retry [%d] times";
    public static boolean DEBUG = true;

    public static void setDebug(boolean flag) {
        DEBUG = flag;
    }

    public static void log(String message) {
        if (empty(message)) return;
        if (DEBUG) {
            Log.d(TAG, message);
        }
    }

    public static void log(String message, Object... args) {
        log(format(getDefault(), message+": %s", args));
    }

    public static void log(Throwable throwable) {
        Log.e(TAG, throwable.getMessage());
    }

    public static String formatStr(String str, Object... args) {
        return format(getDefault(), str+"  %s", args);
    }

    public static boolean empty(String string) {
        return TextUtils.isEmpty(string);
    }

    /**
     * convert long to GMT string
     *
     * @param lastModify long
     * @return String
     */
    public static String longToGMT(long lastModify) {
        Date d = new Date(lastModify);
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        sdf.setTimeZone(getTimeZone("GMT"));
        return sdf.format(d);
    }

    /**
     * convert GMT string to long
     *
     * @param GMT String
     * @return long
     * @throws ParseException
     */
    public static long GMTToLong(String GMT) throws ParseException {
        if (GMT == null || "".equals(GMT)) {
            return new Date().getTime();
        }
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        sdf.setTimeZone(getTimeZone("GMT"));
        Date date = sdf.parse(GMT);
        return date.getTime();
    }

    public static void close(Closeable closeable) throws IOException {
        if (closeable != null) {
            closeable.close();
        }
    }

    public static void closeQuietly(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (RuntimeException rethrown) {
                throw rethrown;
            } catch (Exception ignored) {
            }
        }
    }

    public static String lastModify(Response<?> response) {
        String last = response.headers().get("Last-Modified");
        log("------->lastModify :"+last);
        return last;
    }

    public static long contentLength(Response<?> response) {
        return HttpHeaders.contentLength(response.headers());
    }

    public static String fileName(String url, Response<?> response) {
        String fileName = contentDisposition(response);
        if (empty(fileName)) {
            fileName = url.substring(url.lastIndexOf('/') + 1);
        }
        if (fileName.startsWith("\"")) {
            fileName = fileName.substring(1);
        }
        if (fileName.endsWith("\"")) {
            fileName = fileName.substring(0, fileName.length() - 1);
        }
        return fileName;
    }

    public static String contentDisposition(Response<?> response) {
        String disposition = response.headers().get("Content-Disposition");
        if (empty(disposition)) {
            return "";
        }
        Matcher m = Pattern.compile(".*filename=(.*)").matcher(disposition.toLowerCase());
        if (m.find()) {
            return m.group(1);
        } else {
            return "";
        }
    }

    public static boolean isChunked(Response<?> response) {
        return "chunked".equals(transferEncoding(response));
    }

    public static boolean notSupportRange(Response<?> response) {
        return (TextUtils.isEmpty(contentRange(response)) && !TextUtils.equals(acceptRanges(response), "bytes")) || contentLength(response) == -1 ||
                isChunked(response);
    }



    public static void mkdirs(String... paths) {
        for (String each : paths) {
            File file = new File(each);
            if (file.exists() && file.isDirectory()) {
            } else {
               file.mkdirs();
            }
        }
    }


    private static String transferEncoding(Response<?> response) {
        return response.headers().get("Transfer-Encoding");
    }

    private static String contentRange(Response<?> response) {
        Log.d("duanyl", "contentRange: "+response.headers().get("Content-Range"));
        return response.headers().get("Content-Range");
    }

    private static String acceptRanges(Response<?> response) {
        Log.d("duanyl", "acceptRanges: "+ response.headers().get("Accept-Ranges"));
        return response.headers().get("Accept-Ranges");
    }



    public static <U> ObservableTransformer<U, U> retry(final String hint, final int retryCount) {
        return new ObservableTransformer<U, U>() {
            @Override
            public ObservableSource<U> apply(Observable<U> upstream) {
                return upstream.retry(new BiPredicate<Integer, Throwable>() {
                    @Override
                    public boolean test(Integer integer, Throwable throwable) throws Exception {
                        return retry(hint, retryCount, integer, throwable);
                    }
                });
            }
        };
    }

    public static <U> FlowableTransformer<U, U> retry2(final String hint, final int retryCount) {
        return new FlowableTransformer<U, U>() {
            @Override
            public Publisher<U> apply(Flowable<U> upstream) {
                return upstream.retry(new BiPredicate<Integer, Throwable>() {
                    @Override
                    public boolean test(Integer integer, Throwable throwable) throws Exception {
                        return retry(hint, retryCount, integer, throwable);
                    }
                });
            }
        };
    }

    public static Boolean retry(String hint, int maxRetryCount, Integer integer, Throwable throwable) {
        if (throwable instanceof ProtocolException) {
            if (integer < maxRetryCount + 1) {
                log(RETRY_HINT, hint, "ProtocolException", integer);
                return true;
            }
            return false;
        } else if (throwable instanceof UnknownHostException) {
            if (integer < maxRetryCount + 1) {
                log(RETRY_HINT, hint, "UnknownHostException", integer);
                return true;
            }
            return false;
        } else if (throwable instanceof HttpException) {
            if (integer < maxRetryCount + 1) {
                log(RETRY_HINT, hint, "HttpException", integer);
                return true;
            }
            return false;
        } else if (throwable instanceof SocketTimeoutException) {
            if (integer < maxRetryCount + 1) {
                log(RETRY_HINT, hint, "SocketTimeoutException", integer);
                return true;
            }
            return false;
        } else if (throwable instanceof ConnectException) {
            if (integer < maxRetryCount + 1) {
                log(RETRY_HINT, hint, "ConnectException", integer);
                return true;
            }
            return false;
        } else if (throwable instanceof SocketException) {
            if (integer < maxRetryCount + 1) {
                log(RETRY_HINT, hint, "SocketException", integer);
                return true;
            }
            return false;
        } else {
            if (integer < maxRetryCount + 1) {
                log(RETRY_HINT, hint, " Exception", integer);
                return true;
            }
            return false;
        }
    }

}
