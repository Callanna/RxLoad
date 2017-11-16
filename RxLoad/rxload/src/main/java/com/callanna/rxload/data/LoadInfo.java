package com.callanna.rxload.data;

import android.os.Parcel;
import android.os.Parcelable;

import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * Created by Callanna on 2017/11/14.
 */

public class LoadInfo  implements Parcelable {
    public static final int NORMAL = 0x00;      //未下载
    public static final int PREPAREING = 0x01;      //未下载
    public static final int WAITING = 0x02;     //等待中
    public static final int STARTED = 0x03;     //已开始下载
    public static final int PAUSED = 0x04;      //已暂停
    public static final int CANCELED = 0x05;    //已取消
    public static final int COMPLETED = 0x06;   //已完成
    public static final int FAILED = 0x07;      //下载失败

    public static final Parcelable.Creator<LoadInfo> CREATOR = new Parcelable.Creator<LoadInfo>() {
        @Override
        public LoadInfo createFromParcel(Parcel source) {
            return new LoadInfo(source);
        }

        @Override
        public LoadInfo[] newArray(int size) {
            return new LoadInfo[size];
        }
    };

    private int status = NORMAL;
    private String loadurl = "";

    private String saveName ="";

    private String savePath ="";

    private long downloadsize;

    private long totalsize;

    public LoadInfo() {
    }

    public LoadInfo(int flag) {
        status = flag;
    }

    public LoadInfo(long downloadSize, long totalSize) {
        this.downloadsize = downloadSize;
        this.totalsize = totalSize;
    }

    public LoadInfo(int flag, long downloadSize, long totalSize) {
        this.status = flag;
        this.downloadsize = downloadSize;
        this.totalsize = totalSize;
    }

    protected LoadInfo(Parcel in) {
        this.status = in.readInt();
        this.totalsize = in.readLong();
        this.downloadsize = in.readLong();
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getStatus() {
        return status;
    }
    public String getStringStatus() {
        String temp = "";
        switch (status){
            case NORMAL:
                temp = "未下载";
                break;
            case PREPAREING:
                temp= "准备下载中";
                break;
            case WAITING:
                temp= "等待下载中";
                break;
            case STARTED:
                temp = "下载开始";
                break;
            case PAUSED:
                temp= "下载已暂停";
                break;
            case CANCELED:
                temp = "下载已取消";
                break;
            case COMPLETED:
                temp= "下载已完成";
                break;
            case FAILED:
                temp="下载已失败";
                break;
        }
        return temp;
    }

    public long getTotalSize() {
        return totalsize;
    }

    public void setTotalSize(long totalSize) {
        this.totalsize = totalSize;
    }

    public long getDownloadSize() {
        return downloadsize;
    }

    public void setDownloadSize(long downloadSize) {
        this.downloadsize = downloadSize;
    }

    public void setLoadurl(String loadurl) {
        this.loadurl = loadurl;
    }

    public void setSaveName(String saveName) {
        this.saveName = saveName;
    }

    public void setSavePath(String savePath) {
        this.savePath = savePath;
    }

    public String getLoadurl() {
        return loadurl;
    }

    public String getSaveName() {
        return saveName;
    }

    public String getSavePath() {
        return savePath;
    }
    /**
     * 获得格式化的总Size
     *
     * @return example: 2KB , 10MB
     */
    public String getFormatTotalSize() {
        return formatSize(totalsize);
    }

    public String getFormatDownloadSize() {
        return formatSize(downloadsize);
    }

    /**
     * 获得格式化的状态字符串
     *
     * @return example: 2MB/36MB
     */
    public String getFormatStatusString() {
        return getFormatDownloadSize() + "/" + getFormatTotalSize();
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
    /**
     * 获得下载的百分比, 保留两位小数
     *
     * @return example: 5.25%
     */
    public String getPercent() {
        String percent;
        Double result;
        if (totalsize == 0L) {
            result = 0.0;
        } else {
            result = downloadsize * 1.0 / totalsize;
        }
        NumberFormat nf = NumberFormat.getPercentInstance();
        nf.setMinimumFractionDigits(2);//控制保留小数点后几位，2：表示保留2位小数点
        percent = nf.format(result);
        return percent;
    }

    /**
     * 获得下载的百分比数值
     *
     * @return example: 5%  will return 5, 10% will return 10.
     */
    public long getPercentNumber() {
        double result;
        if (totalsize == 0L) {
            result = 0.0;
        } else {
            result = downloadsize * 1.0 / totalsize;
        }
        return (long) (result * 100);
    }
    /**
     * Format file size to String
     *
     * @param size long
     * @return String
     */
    public  String formatSize(long size) {
        String hrSize;
        double b = size;
        double k = size / 1024.0;
        double m = ((size / 1024.0) / 1024.0);
        double g = (((size / 1024.0) / 1024.0) / 1024.0);
        double t = ((((size / 1024.0) / 1024.0) / 1024.0) / 1024.0);
        DecimalFormat dec = new DecimalFormat("0.00");
        if (t > 1) {
            hrSize = dec.format(t).concat(" TB");
        } else if (g > 1) {
            hrSize = dec.format(g).concat(" GB");
        } else if (m > 1) {
            hrSize = dec.format(m).concat(" MB");
        } else if (k > 1) {
            hrSize = dec.format(k).concat(" KB");
        } else {
            hrSize = dec.format(b).concat(" B");
        }
        return hrSize;
    }
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.status);
        dest.writeLong(this.totalsize);
        dest.writeLong(this.downloadsize);
    }

}
