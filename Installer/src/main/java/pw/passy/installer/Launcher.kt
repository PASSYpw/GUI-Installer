package pw.passy.installer

import javafx.application.Application
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.stage.Stage
import pw.passy.installer.controller.StartController

class Launcher : Application() {
    override fun start(primaryStage: Stage?) {

        val loader = FXMLLoader()
        val parent = loader.load<Parent>(javaClass.getResourceAsStream("/StartGui.fxml"))
        val controller:StartController = loader.getController()
        controller.cnlBtn.setOnAction {
            System.exit(0)
        }
        val scene = Scene(parent)
        primaryStage!!.title = "PASSY Installer"
        primaryStage.scene = scene
        primaryStage.isResizable = false
        primaryStage.centerOnScreen()
        primaryStage.sizeToScene()
        primaryStage.centerOnScreen()
        primaryStage.show()
        val manager = InstallationManager(primaryStage)

        controller.contBtn.setOnAction {
            manager.prepare()
            manager.next()
        }
    }


    companion object {
        fun start() {
            launch(Launcher::class.java)
        }
    }
}