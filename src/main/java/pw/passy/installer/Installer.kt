package pw.passy.installer

import javafx.concurrent.Task
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.stage.Stage
import pw.passy.installer.controller.InstallingController

class Installer(val apachePort:Int, val mySqlPort:Int, val mysqlPass:String, val stage:Stage, path:String) {

    private var task:Task<Void>? = null

    init {
        val loader = FXMLLoader()
        val parent:Parent = loader.load(javaClass.getResourceAsStream("/InstallingGui.fxml"))
        val controller:InstallingController = loader.getController()
        stage.scene = Scene(parent)

        install(controller)
    }

    private fun cancel() {

    }
    private fun install(con:InstallingController) {
        task = object : Task<Void>() {
            @Throws(Exception::class)
            override fun call(): Void? {



                return null
            }
        }
        con.progressBar.progressProperty().bind((task as Task<Void>).progressProperty())
        con.infoLabel.textProperty().bind((task as Task<Void>).messageProperty())
        val thread = Thread(task)
        thread.isDaemon = true
        thread.start()
    }
}