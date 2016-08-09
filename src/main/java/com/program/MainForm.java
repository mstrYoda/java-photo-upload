package com.program;


import javax.swing.*;
import java.awt.dnd.DropTarget;

/**
 * Created by macbook on 9.08.2016.
 */
public class MainForm extends JFrame{
    private JTextField textField1;
    private JPanel jPanel;
    private JLabel lblDosya;

    public MainForm(){

        super("Foto Upload");
        setSize(400,400);
        setContentPane(jPanel);
        setVisible(true);


        DragListener listener = new DragListener(lblDosya,textField1);
        new DropTarget(this,listener);


    }
}
