package com.callanna.rxload.reader;

import android.content.Context;

import com.callanna.rxload.reader.exception.XyException;
import com.callanna.rxload.reader.tool.FileUtils;
import com.callanna.rxload.reader.tool.HtmlPicturesManager;

import org.apache.poi.hssf.converter.ExcelToHtmlConverter;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.converter.WordToHtmlConverter;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.w3c.dom.Document;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import static com.callanna.rxload.Utils.log;

/**
 * 使用POI作为转换器实现转换功能
 */
public class PoiConverter  {
    private Context context;
    /**
     * 存放图片的路径
     */
    private final static String IMAGE_PATH = "image";

   // public void docToHtml(String sourceFileName, String targetFileName) {

    public PoiConverter(Context context){
        this.context=context;
    }


    public File docToHtmlFromSD(String sourceFileName ) {
        File sourceFile = null,targetFile = null;
        try {
            File pic_dir_zip = new File(FileUtils.getFilePath(context) , "Cache");
            if(!pic_dir_zip.exists()) {
                pic_dir_zip.mkdirs();
            }
            String  name = sourceFileName.substring(0,sourceFileName.indexOf("."));
              sourceFile = new File(pic_dir_zip, name +".doc");
              targetFile = new File(pic_dir_zip, name +".html");
            FileInputStream inputStream  = new FileInputStream(sourceFileName);
            sourceFile.createNewFile();
            FileUtils.writeFile(sourceFile, inputStream);
            docToHtml(sourceFile.getPath(),targetFile.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
         return targetFile;
    }
    public File docToHtmlFromAssets(String sourceFileName ) {
        File sourceFile = null,targetFile = null;
        try {
            File pic_dir_zip = new File(FileUtils.getFilePath(context) , "Cache");
            if(!pic_dir_zip.exists()) {
                pic_dir_zip.mkdirs();
            }
            String  name = sourceFileName.substring(0,sourceFileName.indexOf("."));
             sourceFile = new File(pic_dir_zip, name +".doc");
             targetFile = new File(pic_dir_zip, name +".html");
            InputStream inputStream= context.getResources().getAssets().open(sourceFileName);
            sourceFile.createNewFile();
            FileUtils.writeFile(sourceFile, inputStream);
            docToHtml(sourceFile.getPath(),targetFile.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
            log("docToHtmlFromAssets: error:"+e.getLocalizedMessage());
        }
        return targetFile;
    }
    public File xlsToHtmlFromAssets(String sourceFileName) {
        File sourceFile = null,targetFile = null;
        try {
            File pic_dir_zip = new File(FileUtils.getFilePath(context) , "Cache");
            if(!pic_dir_zip.exists()) {
                pic_dir_zip.mkdirs();
            }
            String  name = sourceFileName.substring(0,sourceFileName.indexOf("."));
              sourceFile = new File(pic_dir_zip, name +".xls");
              targetFile = new File(pic_dir_zip, name +".html");
            InputStream inputStream= context.getResources().getAssets().open(sourceFileName);
            sourceFile.createNewFile();
            FileUtils.writeFile(sourceFile, inputStream);
            xlsToHtml(sourceFile.getPath(),targetFile.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
            log("xlsToHtmlFromAssets: error:"+e.getLocalizedMessage());
        }
        return targetFile;
    }
    public File xlsToHtmlFromSD(String sourceFileName) {
        File sourceFile = null,targetFile = null;
        try {
            File pic_dir_zip = new File(FileUtils.getFilePath(context) , "Cache");
            if(!pic_dir_zip.exists()) {
                pic_dir_zip.mkdirs();
            }
            String  name = sourceFileName.substring(0,sourceFileName.indexOf("."));
            sourceFile = new File(pic_dir_zip, name +".xls");
            targetFile = new File(pic_dir_zip, name +".html");
            FileInputStream inputStream = new FileInputStream(sourceFileName);
            sourceFile.createNewFile();
            FileUtils.writeFile(sourceFile, inputStream);
            xlsToHtml(sourceFile.getPath(),targetFile.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return targetFile;
    }
      public void docToHtml(String sourceFileName, String targetFileName) {

        initFolder(targetFileName);
        String imagePathStr = initImageFolder(targetFileName);
        InputStream in=null;
        try {

            HWPFDocument wordDocument = new HWPFDocument(new FileInputStream(sourceFileName));
            Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            WordToHtmlConverter wordToHtmlConverter = new WordToHtmlConverter(document);
            HtmlPicturesManager picturesManager = new HtmlPicturesManager(imagePathStr,IMAGE_PATH);
            wordToHtmlConverter.setPicturesManager(picturesManager);
            wordToHtmlConverter.processDocument(wordDocument);

            Document htmlDocument = wordToHtmlConverter.getDocument();
            DOMSource domSource = new DOMSource(htmlDocument);
            StreamResult streamResult = new StreamResult(new File(targetFileName));

            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer serializer = tf.newTransformer();
            serializer.setOutputProperty(OutputKeys.ENCODING, "utf-8");
            serializer.setOutputProperty(OutputKeys.INDENT, "yes");
            serializer.setOutputProperty(OutputKeys.METHOD, "html");
            serializer.transform(domSource, streamResult);
        } catch (Exception e) {
            e.printStackTrace();
            throw new XyException("将doc文件转换为html时出错!",e);
        } finally {
            if(null!=in){
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void xlsToHtml(String sourceFileName, String targetFileName) {
        initFolder(targetFileName);
        try {

            Document doc = ExcelToHtmlConverter.process(new File(sourceFileName));
            DOMSource domSource = new DOMSource( doc );
            StreamResult streamResult = new StreamResult( new File(targetFileName) );
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer serializer = tf.newTransformer();
            serializer.setOutputProperty( OutputKeys.ENCODING, "UTF-8" );
            serializer.setOutputProperty( OutputKeys.INDENT, "yes" );
            serializer.setOutputProperty( OutputKeys.METHOD, "html" );
            serializer.transform( domSource, streamResult );
        }catch (Exception e) {
            e.printStackTrace();
            //log.error("将xls文件转换为html时出错", e);
            throw new XyException("将xls文件转换为html时出错!",e);
        }
    }

    /**
     * 初始化存放html文件的文件夹
     * @param targetFileName html文件的文件名
     */
    private void initFolder (String targetFileName){
        File targetFile = new File(targetFileName);
        if (targetFile.exists()) {
            targetFile.delete();
        }
        String targetPathStr = context.getFilesDir()+targetFileName.substring(0, targetFileName.lastIndexOf(File.separator));
        File targetPath = new File(targetPathStr);
        //如果文件夹不存在，则创建
        if (!targetPath.exists()) {
            targetPath.mkdirs();
        }
    }

    /**
     * 初始化存放图片的文件夹
     * @param htmlFileName html文件的文件名
     * @return 存放图片的文件夹路径
     */
    private String initImageFolder(String htmlFileName){
        String targetPathStr = htmlFileName.substring(0, htmlFileName.lastIndexOf(File.separator));
        //创建存放图片的文件夹
        String imagePathStr = targetPathStr + File.separator + IMAGE_PATH+ File.separator;
        File imagePath = new File(imagePathStr);
        if (imagePath.exists()) {
            imagePath.delete();
        }
        imagePath.mkdir();
        return imagePathStr;
    }


    /**
     * 读取doc文件的文本，不带格式
     * @param fileName 文件名
     * @return 文件的文本内容
     */
    public static String readTextForDoc(String fileName) {
        String text;
        try {
            FileInputStream in = new FileInputStream(fileName);
            WordExtractor wordExtractor = new WordExtractor(in);
            text = wordExtractor.getText();
        } catch (Exception e) {
            //log.error("读取文件出错:"+fileName, e);
            throw new XyException("读取文件出错", e);
        }
        return text;
    }
}
