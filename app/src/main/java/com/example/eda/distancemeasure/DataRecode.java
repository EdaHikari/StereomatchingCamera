package com.example.eda.distancemeasure;

import android.content.Context;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint2f;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public class DataRecode {

    File mFile;

    DataRecode(Context context, String filename){
        mFile = new File(context.getExternalFilesDir(null), filename+".xml");
    }
    public void loadData(String dataName){
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            //ファイルの読み込み
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(mFile);

            //要素の読み込み
            Element element = doc.getDocumentElement();
            //System.out.println("Node: " + element.getNodeName());
            //System.out.println("code: " + element.getAttribute("code"));

            NodeList nodeList = element.getChildNodes();
            for(int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if(node.getNodeType() == Node.ELEMENT_NODE) {
                    Element name = (Element)node;
                    System.out.println(name.getNodeName() + ": " + name.getTextContent());
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void saveData(String dataName,Mat mat){
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.newDocument();

            //データの形式
            Element info = doc.createElement("info2");
            info.setAttribute("code", "dataName");
            doc.appendChild(info);

            Element rows = doc.createElement("rows");
            rows.appendChild(doc.createTextNode("Taro Yamada"));
            info.appendChild(rows);

            Element cols = doc.createElement("cols");
            cols.appendChild(doc.createTextNode("Taro Yamada"));
            info.appendChild(cols);

            Element type = doc.createElement("type");
            type.appendChild(doc.createTextNode("Taro Yamada"));
            info.appendChild(type);

            //データの中身
            Element matData = doc.createElement("matData");
            matData.appendChild(doc.createTextNode(mat.dump()));
            info.appendChild( matData);
            //ファイルの形式
            TransformerFactory tfFactory = TransformerFactory.newInstance();
            Transformer tf = tfFactory.newTransformer();

            tf.setOutputProperty("indent", "yes");
            tf.setOutputProperty("encoding", "UTF-8");

            tf.transform(new DOMSource(doc), new StreamResult(mFile));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void saveData(MatOfPoint2f mat2f){
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.newDocument();

            //データの形式
            Element info = doc.createElement("info");
            info.setAttribute("code", "dataName");
            doc.appendChild(info);

            Element rows = doc.createElement("rows");
            rows.appendChild(doc.createTextNode("Taro Yamada"));
            info.appendChild(rows);

            Element cols = doc.createElement("cols");
            cols.appendChild(doc.createTextNode("Taro Yamada"));
            info.appendChild(cols);

            Element type = doc.createElement("type");
            type.appendChild(doc.createTextNode("Taro Yamada"));
            info.appendChild(type);

            //データの中身
            Element matData = doc.createElement("matData");
            matData.appendChild(doc.createTextNode(mat2f.dump()));
            info.appendChild( matData);
            //ファイルの形式
            TransformerFactory tfFactory = TransformerFactory.newInstance();
            Transformer tf = tfFactory.newTransformer();

            tf.setOutputProperty("indent", "yes");
            tf.setOutputProperty("encoding", "UTF-8");

            tf.transform(new DOMSource(doc), new StreamResult(mFile));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
