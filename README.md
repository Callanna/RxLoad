# RxLoad
一个使用RxJava2,Sqlibite实现的加载库，加载网络，本地的文件（doc，xls，pdf），图片，网页。也可以利用他实现基本的下载文件功能，查看下载进度。
* 支持文件多线程下载，断点续传下载。
* 支持根据url文件下载地址查看下载进度。
* 支持加载office文档（.doc,.xls文件）
* 支持加载pdf文档（线只能加载小的文件，大的文件建议使用mupdf）
* 支持打开网页，内置JS脚本提高加载速度。

## Smaple
**1.文件下载
in code:

```Java
    // RxLoad初始化
     RxLoad.init(MainActivity.this)
                .downloadPath(Environment.getExternalStoragePublicDirectory("download").getPath())
                .maxDownloadNumber(3)
                .maxThread(3);
     //即使没有下载过，下载的时候也会收到下载进度变化通知              
     RxLoad.getLoadInfo(url).subscribe(
                new Consumer<LoadInfo>() {
                    @Override
                    public void accept(@NonNull LoadInfo downLoadStatus) throws Exception {
                        Log.d("duanyl", "onNext: flag:" + downLoadStatus.getStatus() + ",-->" + downLoadStatus.getFormatDownloadSize() + ",percent ：" + downLoadStatus.getPercentNumber());
                        tv_download.setText(downLoadStatus.getStringStatus() + ",   " + downLoadStatus.getFormatStatusString() + "    ,下载进度：" + downLoadStatus.getPercent());
                    }
                }
        );

```

*-----单任务-----*
效果图：

![demo1](https://raw.githubusercontent.com/Callanna/RxLoad/master/Res/signle.gif)  
in code:

```Java

        .....
         //下载文件
         RxLoad.getInstance().download(url)).subscribe(
                new Consumer<LoadInfo>() {
                    @Override
                    public void accept(@NonNull LoadInfo downLoadStatus) throws Exception {
                        Log.d("duanyl", "onNext: flag:" + downLoadStatus.getStatus() + ",-->" + downLoadStatus.getFormatDownloadSize() + ",percent ：" + downLoadStatus.getPercentNumber());
                        tv_download.setText(downLoadStatus.getStringStatus() + ",   " + downLoadStatus.getFormatStatusString() + "    ,下载进度：" + downLoadStatus.getPercent());
                    }
                }
        );
        //暂停下载
       RxLoad.getInstance().pause(url);
      //取消下载
       RxLoad.getInstance().delete(url);
```
**1.文件下载-----多任务**
效果图：

![demo1](https://raw.githubusercontent.com/Callanna/RxLoad/master/Res/mutile.gif)  


```java
//所有文件下载监听
RxLoad.getInstance().getDownLoading().subscribe(new Observer<List<LoadInfo>>() {
          ...
            @Override
            public void onNext(@NonNull List<LoadInfo> downLoadBeens) {
                int waiting = 0,pause=0,starting = 0,comeplete = 0,fail = 0 ;
                for (LoadInfo bean:downLoadBeens ) {
                    switch (bean.getStatus()){
                        case DownLoadStatus.NORMAL:
                        case DownLoadStatus.PREPAREING:
                        case DownLoadStatus.WAITING:
                            waiting ++;
                            break;
                        case DownLoadStatus.STARTED:
                            starting++;
                            break;
                        case DownLoadStatus.PAUSED:
                            pause++;
                            break;
                        case DownLoadStatus.COMPLETED:
                            comeplete++;
                            break;
                        case DownLoadStatus.FAILED:
                            fail++;
                            break;
                    }
                }
                log("等待中:"+waiting+",下载中："+starting+"" +"已暂停："+pause+",已完成："+comeplete+"，下载失败："+fail);
                tv_download.setText("等待中："+waiting+",下载中："+starting+"\n" +
                        "已暂停："+pause+",已完成："+comeplete+"，下载失败："+fail);
            }
          ...
        });
       List<String> urls = new ArrayList<>();
        for (AppInfo appinfo : appInfoList) {
            urls.add(appinfo.getUrl());
        }
        //下载一组文件
        RxLoad.getInstance().download(urls);
        //全部暂停
        RxLoad.getInstance().pauseAll();
       //全部开始
        RxLoad.getInstance().startAll();
       //全部取消
        RxLoad.getInstance().deleteAll();
```
## 加载文件，支持本地，在线文档----
效果图
![demo1](https://raw.githubusercontent.com/Callanna/RxLoad/master/Res/word.gif)  ![demo1](https://raw.githubusercontent.com/Callanna/RxLoad/master/Res/excel.gif)  ![demo1](https://raw.githubusercontent.com/Callanna/RxLoad/master/Res/pdf.gif)  
in codes
```java
 打开的Word文档,Excel文档,打开PDF文档
 RxLoad.openFile(getApplication(), name);


 打开Assets目录下载的Word文档,Excel文档,PDF文档
 RxLoad.openFileFromAssets(getApplication(), filename);

 将文件加载在指定的WebView,
 RxLoad.loadFile(url,webview);
 ```
 ## 加载图片
 ```java
  将图片加载在指定的ImageView,
 RxLoad.loadImage(url,imageview);
 获取图片Drawable
 RxLoad.loadImageAsDrawable(context,imgUrl)

```
## 网页加载
![demo1](https://raw.githubusercontent.com/Callanna/RxLoad/master/Res/youku.gif)  
```java
 //打开网页
  RxLoad.loadWeb(context, url, isPcClient);
```



