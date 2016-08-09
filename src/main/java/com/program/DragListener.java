package com.program;

import org.json.JSONObject;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;




/**
 * Created by macbook on 9.08.2016.
 */
public class DragListener implements DropTargetListener {

    JLabel imageLabel;
    JTextField textField;

    public DragListener(JLabel imgLabel ,JTextField txtField){
        imageLabel = imgLabel;
        textField = txtField;
    }

    @Override
    public void dragEnter(DropTargetDragEvent dtde) {

    }

    @Override
    public void dragOver(DropTargetDragEvent dtde) {

    }

    @Override
    public void dropActionChanged(DropTargetDragEvent dtde) {

    }

    @Override
    public void dragExit(DropTargetEvent dte) {

    }

    @Override
    public void drop(DropTargetDropEvent dtde) {
        dtde.acceptDrop(DnDConstants.ACTION_COPY);

        Transferable t = dtde.getTransferable();
        DataFlavor df[] = t.getTransferDataFlavors();

        for(DataFlavor f : df){
            try {
                if(f.isFlavorJavaFileListType()){
                    List<File> files = (List<File>)t.getTransferData(f);

                    for (File file:files){
                        displayImage(file.getPath());
                    }

                }
            }catch (Exception ex){
                System.out.println(ex.getMessage());

            }

        }

    }

    private void displayImage(String path) {
        BufferedImage buff = null ;

        try{
            buff = ImageIO.read(new File(path));


        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

        Image resized = buff.getScaledInstance(300,300,Image.SCALE_DEFAULT);

        ImageIcon icon = new ImageIcon(resized);
        imageLabel.setIcon(icon);
        imageLabel.setText("");

        File mFile = new File(path);
        BufferedInputStream stream = null;
        try {
            stream = new BufferedInputStream(new FileInputStream(mFile));
        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
        }

        int chunkSize = 255 * 1024;
        long size = mFile.length();
        long chunks = mFile.length() < chunkSize? 1: (mFile.length() / chunkSize);
        int chunkId = 0;

        String boundary = "****";
        String lineEnd = "\r\n";
        String twoHyphens = "--";

        StringBuilder result = new StringBuilder();

        for (chunkId = 0; chunkId < chunks; chunkId++) {

            try {
                URL url = new URL("http://uploads.im/api?upload");

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(20000 /* milliseconds */);
                conn.setConnectTimeout(20000 /* milliseconds */);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Connection", "Keep-Alive");
                conn.setRequestProperty("User-Agent","Mozilla/5.0");
                conn.setRequestProperty("Referer","www.google.com");
                conn.setRequestProperty("Content-Type","multipart/form-data;boundary="+boundary);
                conn.setDoOutput(true);

                DataOutputStream dos = new DataOutputStream(conn.getOutputStream());


                String param1 = ""+chunkId;
                String param2 = ""+chunks;
                String param3 = mFile.getName();

                // for every param
                dos.writeBytes("Content-Disposition: form-data; name=\"chunk\"" + lineEnd);
                dos.writeBytes("Content-Type: text/plain; charset=UTF-8" + lineEnd);
                dos.writeBytes("Content-Length: " + param1.length() + lineEnd);
                dos.writeBytes(lineEnd);
                dos.writeBytes(param1 + lineEnd);
                dos.writeBytes(twoHyphens + boundary + lineEnd);

                // Send parameter #chunks
                dos.writeBytes("Content-Disposition: form-data; name=\"chunks\"" + lineEnd);
                dos.writeBytes("Content-Type: text/plain; charset=UTF-8" + lineEnd);
                dos.writeBytes("Content-Length: " + param2.length() + lineEnd);
                dos.writeBytes(lineEnd);
                dos.writeBytes(param2 + lineEnd);
                dos.writeBytes(twoHyphens + boundary + lineEnd);


                // Send parameter #name
                dos.writeBytes("Content-Disposition: form-data; name=\"name\"" + lineEnd);
                dos.writeBytes("Content-Type: text/plain; charset=UTF-8" + lineEnd);
                dos.writeBytes("Content-Length: " + param3.length() + lineEnd);
                dos.writeBytes(lineEnd);
                dos.writeBytes(param3 + lineEnd);
                dos.writeBytes(twoHyphens + boundary + lineEnd);

                // Send parameter #file
                dos.writeBytes("Content-Disposition: form-data; name=\"file\";filename=\"" + param3 + "\"" + lineEnd); // filename is the Name of the File to be uploaded

                dos.writeBytes("Content-Type: image/jpeg" + lineEnd);
                dos.writeBytes(lineEnd);

                byte[] buffer = new byte[chunkSize];

                stream.read(buffer);

                // dos.write(buffer, 0, bufferSize);
                dos.write(buffer);

                dos.writeBytes(lineEnd);
                dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
                dos.flush();
                dos.close();

                BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String line;
                while ((line = rd.readLine()) != null) {
                    result.append(line);
                }
                rd.close();

            } catch (MalformedURLException e) {
                System.out.println("Malformed url exception : "+e.getMessage());
            } catch (IOException e) {
                System.out.println("IO Exception : " +e.getMessage());
            }
        }

        JSONObject obj = new JSONObject(result.toString());
        JSONObject data = obj.getJSONObject("data");
        String img_url = data.getString("img_url");

        textField.setText(img_url);

    }
}
