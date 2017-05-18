package pw.passy.offmng;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXRadioButton;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import kotlin.Triple;

public class Controller {

    @FXML
    public JFXButton stopBtn;

    @FXML
    public JFXButton restartBtn;

    @FXML
    public JFXRadioButton nginxState;

    @FXML
    public JFXRadioButton phpState;

    @FXML
    public JFXRadioButton mysqlState;

    @FXML
    public JFXButton helpBtn;

    @FXML
    public Label nginxLabel;

    @FXML
    public Label phpLabel;

    @FXML
    public Label mysqlLabel;

    public static Manager manager;

    public void tray() {


    }
    public void init() {



        stopBtn.setOnAction(event -> {

            Triple<Process, Process, Process> services = manager.getServices();
            services.component1().destroy();
            services.component2().destroy();
            services.component3().destroy();

            System.exit(0);
        });
        restartBtn.setOnAction(event -> manager.restart());


        new Thread(() -> {

            while (true) {
                Triple<Process, Process, Process> services = manager.getServices();

                nginxState.setSelected(services.component3().isAlive());
                phpState.setSelected(services.component1().isAlive());
                mysqlState.setSelected(services.component2().isAlive());
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

}
