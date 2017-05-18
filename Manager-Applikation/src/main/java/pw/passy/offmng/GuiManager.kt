package pw.passy.offmng

import javafx.application.Application
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.stage.Stage

/**
 * Created by liz3 on 17.05.17.
 */
class GuiManager : Application(){

    var stage:Stage? = null
    var controller: Controller? = null

    override fun start(primaryStage: Stage?) {

        val loader = FXMLLoader()
        val parent:Parent = loader.load(javaClass.getResourceAsStream("/Gui.fxml"))
        this.controller = loader.getController()
        this.stage = primaryStage
        primaryStage!!.scene = Scene(parent)
        primaryStage.sizeToScene()
        primaryStage.isResizable = false
        primaryStage.centerOnScreen()
        controller!!.init()
        primaryStage.setOnCloseRequest {

        }
        primaryStage.show()

    }
    companion object {
        public fun start() {
            launch(GuiManager::class.java)
        }
    }
}