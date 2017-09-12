package com.callanna.rxdownload.db;

import android.content.ContentValues;
import android.database.Cursor;

import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Function;

/**
 * Created by Callanna on 2017/7/16.
 */

public class DownLoadBean {
    private int id = -1;
    private String url ="";
    private String saveName ="";
    private String savePath ="";
    private String tempPath = "";
    private String lmfPath = "";
    private DownLoadStatus status;
    private String lastModify ="";
    private boolean isSupportRange;
    private boolean isChanged;

    public DownLoadBean(String url, String saveName, String savePath) {
        this.url = url;
        this.saveName = saveName;
        this.savePath = savePath;
    }

    public DownLoadBean(int id, String url, String saveName, String savePath, String tPath,String lPath, DownLoadStatus status,String lastModify,boolean isSupportRange,boolean isChanged) {
        this.id = id;
        this.url = url;
        this.saveName = saveName;
        this.savePath = savePath;
        this.tempPath = tPath;
        this.lmfPath = lPath;
        this.status = status;
        this.lastModify = lastModify;
        this.isSupportRange = isSupportRange;
        this.isChanged = isChanged;
    }

    public DownLoadBean(String url) {
        this.url = url;
        this.status = new DownLoadStatus(DownLoadStatus.NORMAL);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getSaveName() {
        return saveName;
    }
    public String getFileName() {
        if ((saveName != null) && (saveName.length() > 0)) {
            int dot = saveName.lastIndexOf('.');
            if ((dot >-1) && (dot < (saveName.length()))) {
                return saveName.substring(0, dot);
            }
        }
        return saveName;
    }
    public String getFileExtensionName() {
        if ((saveName != null) && (saveName.length() > 0)) {
            int dot = saveName.lastIndexOf('.');
            if ((dot >-1) && (dot < (saveName.length() - 1))) {
                return saveName.substring(dot + 1);
            }
        }
        return saveName;
    }
    public void setSaveName(String saveName) {
        this.saveName = saveName;
    }

    public String getSavePath() {
        return savePath;
    }

    public void setSavePath(String savePath) {
        this.savePath = savePath;
    }

    public String getTempPath() {
        return tempPath;
    }

    public void setTempPath(String tempPath) {
        this.tempPath = tempPath;
    }

    public String getLmfPath() {
        return lmfPath;
    }

    public void setLmfPath(String lmfPath) {
        this.lmfPath = lmfPath;
    }

    public DownLoadStatus getStatus() {
        return status;
    }

    public void setStatus(DownLoadStatus status) {
        this.status = status;
    }

    public boolean getIsSupportRange() {
        return isSupportRange;
    }

    public void setIsSupportRange(boolean isSupportRange) {
        this.isSupportRange = isSupportRange;
    }

    public boolean isChanged() {
        return isChanged;
    }

    public void setChanged(boolean changed) {
        isChanged = changed;
    }

    public static final Function<Cursor, DownLoadBean> MAPPER = new Function<Cursor, DownLoadBean>() {
        @Override
        public DownLoadBean apply(@NonNull Cursor cursor) throws Exception {
            int id = Db.getInt(cursor,Db.DownLoadTable.COLUMN_ID);
            String url =  Db.getString(cursor,Db.DownLoadTable.COLUMN_URL);
            String name =  Db.getString(cursor,Db.DownLoadTable.COLUMN_SAVE_NAME);
            String path =  Db.getString(cursor,Db.DownLoadTable.COLUMN_SAVE_PATH);
            String tpath =  Db.getString(cursor,Db.DownLoadTable.COLUMN_TEMP_PATH);
            String lpath =  Db.getString(cursor,Db.DownLoadTable.COLUMN_LMDF_PATH);
            int flag = Db.getInt(cursor,Db.DownLoadTable.COLUMN_DOWNLOAD_FLAG);
            long  downloadsize = Db.getInt(cursor,Db.DownLoadTable.COLUMN_DOWNLOAD_SIZE);
            long  totlesize = Db.getInt(cursor,Db.DownLoadTable.COLUMN_TOTAL_SIZE);
            String LastModify = Db.getString(cursor,Db.DownLoadTable.COLUMN_LastModify);
            boolean isRange = Db.getInt(cursor,Db.DownLoadTable.COLUMN_RANGE)==1 ;
            boolean isChanged = Db.getInt(cursor,Db.DownLoadTable.COLUMN_CHENGED)==1 ;
            return new DownLoadBean(id,url,name,path,tpath,lpath,new DownLoadStatus(flag,downloadsize,totlesize),LastModify,isRange,isChanged);
        }
    };

    public void setLastModify(String s) {
       this.lastModify = s;
    }

    public String getLastModify() {
        return lastModify;
    }

    public static final class Builder {
        private final ContentValues values = new ContentValues();

        public Builder id(long id) {
            values.put(Db.DownLoadTable.COLUMN_ID, id);
            return this;
        }

        public Builder saveName(String name) {
            values.put(Db.DownLoadTable.COLUMN_SAVE_NAME, name);
            return this;
        }

        public Builder savePath(String path) {
            values.put(Db.DownLoadTable.COLUMN_SAVE_PATH, path);
            return this;
        }
        public Builder tempPath(String path) {
            values.put(Db.DownLoadTable.COLUMN_TEMP_PATH, path);
            return this;
        }
        public Builder lmfPath(String path) {
            values.put(Db.DownLoadTable.COLUMN_LMDF_PATH, path);
            return this;
        }
        public Builder status(int status){
            values.put(Db.DownLoadTable.COLUMN_DOWNLOAD_FLAG, status);
            return this;
        }
        public Builder downSize(int size){
            values.put(Db.DownLoadTable.COLUMN_DOWNLOAD_SIZE, size);
            return this;
        }
        public Builder totalSize(int size){
            values.put(Db.DownLoadTable.COLUMN_TOTAL_SIZE, size);
            return this;
        }
        public Builder  lastModify(String lastModify){
            values.put(Db.DownLoadTable.COLUMN_LastModify, lastModify);
            return this;
        }
        public Builder isSupportRange(boolean isRange){
            values.put(Db.DownLoadTable.COLUMN_RANGE, isRange);
            return this;
        }
        public Builder get(DownLoadBean bean){
            if(bean.getId() !=-1) {
                //values.put(Db.DownLoadTable.COLUMN_ID, bean.getId());
            }
            values.put(Db.DownLoadTable.COLUMN_URL, bean.getUrl());
            values.put(Db.DownLoadTable.COLUMN_SAVE_NAME, bean.getSaveName());
            values.put(Db.DownLoadTable.COLUMN_SAVE_PATH, bean.getSavePath());
            values.put(Db.DownLoadTable.COLUMN_TEMP_PATH, bean.getTempPath());
            values.put(Db.DownLoadTable.COLUMN_LMDF_PATH, bean.getLmfPath());
            values.put(Db.DownLoadTable.COLUMN_DOWNLOAD_FLAG, bean.getStatus().getStatus());
            values.put(Db.DownLoadTable.COLUMN_DOWNLOAD_SIZE,bean.getStatus().getDownloadSize());
            values.put(Db.DownLoadTable.COLUMN_TOTAL_SIZE, bean.getStatus().getTotalSize());
            values.put(Db.DownLoadTable.COLUMN_LastModify, bean.getLastModify());
            values.put(Db.DownLoadTable.COLUMN_RANGE, bean.getIsSupportRange());
            values.put(Db.DownLoadTable.COLUMN_CHENGED, bean.isChanged());
            return this;
        }

        public ContentValues build() {
            return values; // TODO defensive copy?
        }
    }
}
