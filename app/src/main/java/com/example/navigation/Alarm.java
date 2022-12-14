package com.example.navigation;

import android.util.Log;

import com.skt.Tmap.TMapPoint;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

public class Alarm {

    public ArrayList<TMapPoint> saveRoutePoint;

    public Alarm(ArrayList<TMapPoint> saveRoutePoint){
        this.saveRoutePoint = saveRoutePoint;
    }

    public boolean accident() throws IOException {

        StringBuilder urlBuilder = new StringBuilder("http://www.utic.go.kr/guide/imsOpenData.do"); /*URL*/
        urlBuilder.append("?" + URLEncoder.encode("key","UTF-8") + "=klJXl3fne6niJzEYZ3YC3MenAHxXJBvZaWQElXlDZKTI5HaNd8ZMWb9vv0XIsTQ"); /*Service Key*/
//        urlBuilder.append("?" + URLEncoder.encode("key","UTF-8") + "=joqQEZdJV6jtlHRXFxCXZZ15xTpzfxMQIQmcK0ElMAe3deCmWv83I8Z93MoVVs"); /*Service Key*/
        URL url = new URL(urlBuilder.toString());
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Content-type", "text/xml;charset=UTF-8");
        Log.e("Response code: ", conn.getResponseCode()+"");
        BufferedReader rd;
        if(conn.getResponseCode() >= 200 && conn.getResponseCode() <= 300) {
            rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        } else {
            rd = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
        }
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = rd.readLine()) != null) {
            sb.append(line);
        }
        rd.close();
        conn.disconnect();
        Log.e("?????????", sb.toString());

        ArrayList<Object> list_x = new ArrayList<Object>();
        ArrayList<Object> list_y = new ArrayList<Object>();
        List<HashMap<String, String>> list = null;
        try {
            list = getResultMap(sb.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }

//        for(Map<String,String> tmpMap : list) {
//            Log.e("??????", String.valueOf(list.size()));
//            list_x.add(tmpMap.get("locationDataX"));
//            list_y.add(tmpMap.get("locationDataY"));
//        }
        list_x.add(127.30567976);
        list_y.add(36.37959235);

        Log.e("x??????", list_x.get(0).toString());
        Log.e("y??????", list_y.get(0).toString());

        Log.e("???????????????", ""+saveRoutePoint.size());
        return passAccident(Double.parseDouble(list_x.get(0).toString()), Double.parseDouble(list_y.get(0).toString()));
    }

    public boolean passAccident(double x, double y){
        for(TMapPoint p : saveRoutePoint){
            Log.e("????????? ??? x", Double.toString(p.getLongitude()));
            Log.e("????????? ??? y", Double.toString(p.getLatitude()));
            if((x-0.0001) < p.getLongitude() && (x+0.0001) > p.getLongitude() && (y-0.0001) < p.getLatitude() && (y+0.0001) > p.getLatitude()){
                Log.e("??????", "??????");
                return true;
            }
        }
        return false;
    }

    public static List<HashMap<String, String>> getResultMap(String data) throws Exception {

        //???????????? ????????? map??? ??????????????????.
        List<HashMap<String, String>> resultMap = new LinkedList<HashMap<String, String>>();

        InputSource is = new InputSource(new StringReader(data));

        //Document ???????????? xml???????????? ???????????????.
        Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(is);

        //xPath ???????????? ????????? ????????????.
        XPath xpath = XPathFactory.newInstance().newXPath();

        //xPath??? ???????????? ?????? node????????? ???????????? ???????????????.
        NodeList nodeList = (NodeList) xpath.compile("/result/record").evaluate(document, XPathConstants.NODESET);
        int nodeListCount = nodeList.getLength();
        for (int i = 0; i < nodeListCount; i++) {
            NodeList childNode = nodeList.item(i).getChildNodes();
            HashMap<String, String> nodeMap = new HashMap<String, String>();
            int childNodeCount = childNode.getLength();
            for (int j = 0; j < childNodeCount; j++) {
                nodeMap.put(childNode.item(j).getNodeName(), childNode.item(j).getTextContent());
            }
            resultMap.add(nodeMap);
        }
        return resultMap;
    }



}
