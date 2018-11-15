package slaclone.util;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.*;
import java.net.URLEncoder;

/**
 * HTTP Method and other utils.
 * @author Rei Kambayashi
 */
public class HttpUtil {
    /**
     * Get string-message from http-server.
     * @param urlTo Url to get message
     * @return Returned message from server
     */
    public static String get(String urlTo){
        CloseableHttpClient client = HttpClients.createDefault();
        HttpGet get = new HttpGet(urlTo);
        get.setHeader("Content-type",  "charset=UTF-8");
        try {
            CloseableHttpResponse response = client.execute(get);
            String ret = EntityUtils.toString(response.getEntity());
           // System.out.println(ret);
            return ret;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Get file from http-server.
     * @param urlTo Url to get file
     * @return Returned file from server
     */
    public static File getFile(String urlTo){
        CloseableHttpClient client = HttpClients.createDefault();
        HttpGet get = new HttpGet(urlTo);
        get.setHeader("Content-type", "charset=UTF-8");
        try {
            CloseableHttpResponse response = client.execute(get);
            String[] filee = urlTo.split("/");
            if(!new File("./tmpFile").exists()){
                new File("./tmpFile").mkdirs();
            }
            File file = new File("./tmpFile/" + filee[filee.length - 1]);
            BufferedOutputStream writer = new BufferedOutputStream(new FileOutputStream(file));
            byte[] bytes = new byte[1024];
            int l = 0;
            while ((l = response.getEntity().getContent().read(bytes)) > 0){
                writer.write(bytes,0,l);
            }
            writer.flush();
            writer.close();
            return file;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Percent-encode utf8-encoded url.
     * @param url Text to encode
     * @return Percent-encoded url
     */
    public static String urlEncode(String url){
        try {
            return URLEncoder.encode(url,"UTF-8").replaceAll("[+]","%20");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return url;
        }
    }

    /**
     * Post parameter to http-server.
     * @param parameter Parameter text to post
     * @param urlTo Url to post parameter
     * @param contentType Parameter's Content-Type
     * @return Returned message from server
     */
    public static String post(String parameter,String urlTo,String contentType){
        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost post = new HttpPost(urlTo);
        post.setHeader("Content-type", contentType + "; charset=UTF-8");
        if(parameter != null)post.setEntity(new StringEntity(parameter, "UTF-8"));
        try {
            CloseableHttpResponse response = client.execute(post);
            String ret = EntityUtils.toString(response.getEntity());
         //   System.out.println(ret);
            return ret;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * Put parameter to http-server.
     * @param data Parameter text to put
     * @param urlTo Url to put parameter
     * @param contentType Parameter's Content-Type
     * @return Returned message from server
     */
    public static String put(String data,String urlTo,String contentType){
        CloseableHttpClient client = HttpClients.createDefault();
        HttpPut put = new HttpPut(urlTo);
        put.setHeader("Content-type", contentType + "; charset=UTF-8");
        if(data != null)put.setEntity(new StringEntity(data, "UTF-8"));
        try {
            CloseableHttpResponse response = client.execute(put);
            String ret = EntityUtils.toString(response.getEntity());
          //  System.out.println(ret);
            return ret;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
