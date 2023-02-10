package com.example.chatbot.controller;

import javafx.fxml.FXML;
import javafx.scene.control.MenuBar;
import javafx.scene.control.TextArea;
import javafx.scene.input.InputMethodEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import com.example.chatbot.tools.methods;

public class chatBotController {

    public TextArea textSpace;
    public FlowPane chatPane;
    public MenuBar Menu;
    public AnchorPane chatting;
    public void initialize(){
        chatPane.setPrefWrapLength(1000);
    }


    public void sendMessage(MouseEvent mouseEvent) {
        double [] message = methods.calculateTextBoxSize(textSpace.getText(),textSpace.getFont(),"\n",chatPane.getMaxWidth(),10,textSpace.getBoundsInLocal().getHeight(),10);
        System.out.println("the calculated height is: "+ -message[1]);
        chatPane.setPrefHeight(chatPane.getHeight()-message[1]);//when sending messages, at first creating a new line.
        System.out.println("Current Pane height: "+chatPane.getPrefWrapLength());
    }

}
