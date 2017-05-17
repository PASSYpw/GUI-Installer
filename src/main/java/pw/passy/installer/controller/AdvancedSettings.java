package pw.passy.installer.controller;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXTextField;
import javafx.fxml.FXML;

public class AdvancedSettings {

    @FXML
    public JFXButton contBtn;

    @FXML
    public JFXButton cnlBtn;

    @FXML
    public JFXButton backBtn;

    @FXML
    public JFXTextField apachePort;

    @FXML
    public JFXTextField mysqlPort;

    @FXML
    public JFXTextField instPath;

    @FXML
    public JFXCheckBox startwithSystem;

    @FXML
    public JFXButton pathBtn;
}
