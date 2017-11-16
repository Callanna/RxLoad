package com.callanna.rxload.db;

import android.content.Context;
import android.database.Cursor;

import com.squareup.sqlbrite2.BriteDatabase;
import com.squareup.sqlbrite2.SqlBrite;

import java.util.LinkedList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by Callanna on 2017/7/16.
 */

public class DBManager {
    private static final String QUERY_URL =
            "SELECT *  FROM " + Db.DownLoadTable.TABLE_NAME + " WHERE " + Db.DownLoadTable.COLUMN_URL + " = ?";

    private static final String QUERY_ALL =
            "SELECT *  FROM " + Db.DownLoadTable.TABLE_NAME ;

    private static final String QUERY_STATUS =
            "SELECT *  FROM " + Db.DownLoadTable.TABLE_NAME + " WHERE " + Db.DownLoadTable.COLUMN_DOWNLOAD_FLAG + " = ?";

    private volatile static DBManager singleton;
    private DBHelper mDbOpenHelper;
    private BriteDatabase db;
    private SqlBrite sqlBrite;



    private DBManager(Context context) {
        mDbOpenHelper = new DBHelper(context);
        sqlBrite =  new SqlBrite.Builder().build();
        db = sqlBrite.wrapDatabaseHelper(mDbOpenHelper, Schedulers.io());
        db.setLoggingEnabled(false);
    }

    public static DBManager getSingleton(Context context) {
        if (singleton == null) {
            synchronized (DBManager.class) {
                if (singleton == null) {
                    singleton = new DBManager(context);
                }
            }
        }
        return singleton;
    }

    public synchronized Observable<List<DownLoadBean>> searchDownloadByAll(){
       return db.createQuery(Db.DownLoadTable.TABLE_NAME,QUERY_ALL)
                .mapToList(DownLoadBean.MAPPER);
    }
    public synchronized Observable<List<DownLoadBean>>  searchStatus(int status) {
        return db.createQuery(Db.DownLoadTable.TABLE_NAME,QUERY_STATUS, String.valueOf(status))
                .mapToList(DownLoadBean.MAPPER) ;
    }
    public synchronized Observable<List<DownLoadBean>>  searchDownloadByStatus(int status){
        List<DownLoadBean> downLoadBeanList = new LinkedList<>();
        Cursor cursor = db.query(QUERY_STATUS,status+"");
        while (cursor.moveToNext()){
            String url = cursor.getString(cursor.getColumnIndex(Db.DownLoadTable.COLUMN_URL));
            DownLoadBean downLoadBean = new DownLoadBean(url);
            DownLoadStatus downLoadStatus = new DownLoadStatus();
            downLoadBean.setId(cursor.getInt(cursor.getColumnIndex(Db.DownLoadTable.COLUMN_ID)));
            downLoadStatus.setStatus(cursor.getInt(cursor.getColumnIndex(Db.DownLoadTable.COLUMN_DOWNLOAD_FLAG)));
            downLoadStatus.setDownloadSize(cursor.getInt(cursor.getColumnIndex(Db.DownLoadTable.COLUMN_DOWNLOAD_SIZE)));
            downLoadStatus.setTotalSize(cursor.getInt(cursor.getColumnIndex(Db.DownLoadTable.COLUMN_TOTAL_SIZE)));
            downLoadBean.setStatus(downLoadStatus);
            downLoadBean.setSaveName(cursor.getString(cursor.getColumnIndex(Db.DownLoadTable.COLUMN_SAVE_NAME)));
            downLoadBean.setIsSupportRange(cursor.getInt(cursor.getColumnIndex(Db.DownLoadTable.COLUMN_RANGE))==1);
            downLoadBean.setChanged(cursor.getInt(cursor.getColumnIndex(Db.DownLoadTable.COLUMN_CHENGED)) == 1);
            downLoadBean.setLastModify(cursor.getString(cursor.getColumnIndex(Db.DownLoadTable.COLUMN_LastModify)));
            downLoadBean.setSavePath(cursor.getString(cursor.getColumnIndex(Db.DownLoadTable.COLUMN_SAVE_PATH)));
            downLoadBean.setTempPath(cursor.getString(cursor.getColumnIndex(Db.DownLoadTable.COLUMN_TEMP_PATH)));
            downLoadBean.setLmfPath(cursor.getString(cursor.getColumnIndex(Db.DownLoadTable.COLUMN_LMDF_PATH)));
            downLoadBeanList.add(downLoadBean);
        }
        cursor.close();
        db.close();
        return   Observable.just(downLoadBeanList);
    }
    public synchronized Observable<DownLoadBean>  searchDownloadByUrl(String url){
        return  db.createQuery(Db.DownLoadTable.TABLE_NAME,QUERY_URL,url)
                .mapToOneOrDefault(DownLoadBean.MAPPER,new DownLoadBean(url)) ;
    }
    public synchronized DownLoadBean  searchByUrl(String url){
        DownLoadBean downLoadBean = null;
        Cursor cursor = db.query(QUERY_URL,url);
        if(cursor.getCount() >0){
            downLoadBean = new DownLoadBean(url);
            DownLoadStatus downLoadStatus = new DownLoadStatus();
            cursor.moveToFirst();
            downLoadBean.setId(cursor.getInt(cursor.getColumnIndex(Db.DownLoadTable.COLUMN_ID)));
            downLoadStatus.setStatus(cursor.getInt(cursor.getColumnIndex(Db.DownLoadTable.COLUMN_DOWNLOAD_FLAG)));
            downLoadStatus.setDownloadSize(cursor.getInt(cursor.getColumnIndex(Db.DownLoadTable.COLUMN_DOWNLOAD_SIZE)));
            downLoadStatus.setTotalSize(cursor.getInt(cursor.getColumnIndex(Db.DownLoadTable.COLUMN_TOTAL_SIZE)));
            downLoadBean.setStatus(downLoadStatus);
            downLoadBean.setUrl(cursor.getString(cursor.getColumnIndex(Db.DownLoadTable.COLUMN_URL)));

            downLoadBean.setSaveName(cursor.getString(cursor.getColumnIndex(Db.DownLoadTable.COLUMN_SAVE_NAME)));

            downLoadBean.setIsSupportRange(cursor.getInt(cursor.getColumnIndex(Db.DownLoadTable.COLUMN_RANGE))==1);

            downLoadBean.setChanged(cursor.getInt(cursor.getColumnIndex(Db.DownLoadTable.COLUMN_CHENGED)) == 1);

            downLoadBean.setLastModify(cursor.getString(cursor.getColumnIndex(Db.DownLoadTable.COLUMN_LastModify)));

            downLoadBean.setSavePath(cursor.getString(cursor.getColumnIndex(Db.DownLoadTable.COLUMN_SAVE_PATH)));

            downLoadBean.setTempPath(cursor.getString(cursor.getColumnIndex(Db.DownLoadTable.COLUMN_TEMP_PATH)));
            downLoadBean.setLmfPath(cursor.getString(cursor.getColumnIndex(Db.DownLoadTable.COLUMN_LMDF_PATH)));

        }
        cursor.close();
        db.close();
        return   downLoadBean;
    }

    public synchronized void add(DownLoadBean bean){
       db.insert(Db.DownLoadTable.TABLE_NAME,new DownLoadBean.Builder().get(bean).build());
    }

    public synchronized void update(DownLoadBean bean){
        db.update(Db.DownLoadTable.TABLE_NAME,new DownLoadBean.Builder().get(bean).build(),
                Db.DownLoadTable.COLUMN_ID +" = ? ", String.valueOf(bean.getId()));
    }
    public synchronized void updateStatusByUrl(String url, int flag){
        db.update(Db.DownLoadTable.TABLE_NAME,new DownLoadBean.Builder().status(flag).build(),
                Db.DownLoadTable.COLUMN_URL +" = ? ",url);
    }
    public synchronized void updateStatusByUrl(String url, DownLoadStatus flag){
        db.update(Db.DownLoadTable.TABLE_NAME,new DownLoadBean.Builder()
                        .status(flag.getStatus())
                        .downSize((int) flag.getDownloadSize())
                        .totalSize((int) flag.getTotalSize()).build(),
                Db.DownLoadTable.COLUMN_URL +" = ? ",url);
    }
    public synchronized void delete(String url){
        db.delete(Db.DownLoadTable.TABLE_NAME, Db.DownLoadTable.COLUMN_URL +" = ? ",url);
    }

    public synchronized void deleteWaiting(){
        db.delete(Db.DownLoadTable.TABLE_NAME, Db.DownLoadTable.COLUMN_DOWNLOAD_FLAG +" != ? ", String.valueOf(DownLoadStatus.COMPLETED));
    }

    public synchronized boolean recordNotExists(String url) {
       Long count = searchDownloadByUrl(url).observeOn(Schedulers.newThread())
                .count().blockingGet();
        return count>0;
    }

    public synchronized void clearStatusByUrl(String url) {
        db.update(Db.DownLoadTable.TABLE_NAME,new DownLoadBean.Builder()
                        .saveName("")
                        .savePath("")
                        .lmfPath("")
                        .tempPath("")
                        .status(0)
                        .downSize(0)
                        .totalSize(0).build(),
                Db.DownLoadTable.COLUMN_URL +" = ? ",url);
    }
}
