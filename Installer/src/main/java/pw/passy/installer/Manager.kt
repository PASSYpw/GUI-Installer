package pw.passy.installer

import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.stage.DirectoryChooser
import javafx.stage.Stage
import pw.passy.installer.controller.AdvancedSettings
import pw.passy.installer.controller.PasswordController
import pw.passy.installer.controller.SettingsModeController
import java.io.File

class InstallationManager(val stage: Stage) {

    var advanced = false
    var currentGuiIndex: Int = 0
    var saves = HashMap<Int, Scene>()

    //preset defaults
    var nginxPort = 24480
    var mysqlPort = 24406
    var phpPort = 24488
    var startWithSystem = false
    var homeDir = ""
    var mysqlPass = ""


    fun prepare() {
        val home = System.getProperty("user.home")
        homeDir = File(home, ".passy").absolutePath

    }

    fun next() {
        if (saves.containsKey(currentGuiIndex + 1)) {
            currentGuiIndex++
            stage.scene = saves[currentGuiIndex]
            return
        }
        if (currentGuiIndex == 0 && !saves.containsKey(0)) saves.put(0, stage.scene)
        val loader = FXMLLoader()
        if (currentGuiIndex == 0) {
            val parent: Parent = loader.load(javaClass.getResourceAsStream("/SettingsGui.fxml"));
            val modeController: SettingsModeController = loader.getController()
            modeController.backBtn.setOnAction { back() }
            modeController.cnlBtn.setOnAction { System.exit(0) }
            modeController.contBtn.setOnAction { next() }
            modeController.simpleMode.selectedProperty().addListener({ _, _, newValue ->
                if (newValue == true) {
                    advanced = false
                    modeController.advMode.isSelected = false
                    if(saves.containsKey(2)) saves.remove(2)
                } else {
                    if (!modeController.advMode.isSelected) {
                        modeController.simpleMode.isSelected = true
                    }
                }
            })
            modeController.advMode.selectedProperty().addListener({ _, _, newValue ->
                if (newValue == true) {
                    advanced = true
                    modeController.simpleMode.isSelected = false
                } else {
                    if (!modeController.simpleMode.isSelected) {
                        modeController.advMode.isSelected = true
                    }
                }

            })
            modeController.simpleMode.isSelected = true
            stage.scene = Scene(parent)
        }
        if (currentGuiIndex == 1) {
            if (!advanced) {
                currentGuiIndex++
                next()
                return
            } else {
                val parent: Parent = loader.load(javaClass.getResourceAsStream("/AdvancedSettings.fxml"));
                val avdController: AdvancedSettings = loader.getController()
                avdController.backBtn.setOnAction { back() }
                avdController.contBtn.setOnAction {
                    val apacheP = avdController.apachePort.text
                    val sqlP = avdController.mysqlPort.text
                    val pP = avdController.phpPort.text
                    startWithSystem = avdController.startwithSystem.isSelected
                    if (apacheP == null || sqlP == null || pP == null) return@setOnAction

                    try {
                        if (apacheP.toInt() != 0 && apacheP.toInt() != nginxPort) nginxPort = apacheP.toInt()
                        if (sqlP.toInt() != 0 && sqlP.toInt() != mysqlPort) mysqlPort = sqlP.toInt()
                        if (pP.toInt() != 0 && pP.toInt() != phpPort) phpPort = pP.toInt()
                    } catch (e: Exception) {
                        return@setOnAction
                    }

                    next()
                }
                avdController.cnlBtn.setOnAction { System.exit(0) }
                avdController.pathBtn.setOnAction {
                    val dirStage = Stage()
                    val dirChooser = DirectoryChooser()
                    dirChooser.title = "Select Installation Path for PASSY"
                    val result: File? = dirChooser.showDialog(dirStage)
                    if (result != null) {
                        homeDir = result.absolutePath
                        avdController.instPath.text = homeDir
                    }
                }
                avdController.instPath.text = homeDir
                stage.scene = Scene(parent)
            }
        }
        if (currentGuiIndex == 2) {

            val parent: Parent = loader.load(javaClass.getResourceAsStream("/PasswordGui.fxml"));
            val c: PasswordController = loader.getController()
            c.backBtn.setOnAction { back() }
            c.cnlBtn.setOnAction { System.exit(0) }

            c.info.text = "Enter Passwords"
            c.contBtn.setOnAction {
                if (checkPasses(c.pass1.text, c.pass2.text).first) {
                    Installer(nginxPort, mysqlPort, phpPort, mysqlPass, stage, homeDir)
                }
            }
            c.pass1.setOnKeyReleased {
                val result = checkPasses(c.pass1.text, c.pass2.text)
                if (result.first) {
                    c.info.text = result.second
                    mysqlPass = c.pass2.text
                    return@setOnKeyReleased
                }
                c.info.text = result.second
            }
            c.pass2.setOnKeyReleased {
                val result = checkPasses(c.pass1.text, c.pass2.text)
                if (result.first) {
                    c.info.text = result.second
                    mysqlPass = c.pass2.text
                    return@setOnKeyReleased
                }
                c.info.text = result.second
            }
            stage.scene = Scene(parent)
        }
        currentGuiIndex++
        saves.put(currentGuiIndex, stage.scene)
    }

    fun back() {
        if(currentGuiIndex == 3 && !advanced) currentGuiIndex--
        if (saves.containsKey(currentGuiIndex - 1)) {
            currentGuiIndex--
            stage.scene = saves[currentGuiIndex]
        }

    }

    private fun checkPasses(pass1: String, pass2: String): Triple<Boolean, String, String> {

        if (pass1 != pass2) return Triple(false, "Passwords dont match!", "")

        if (pass1.length < 5) return Triple(false, "Password to Short!", "")

        return Triple(true, "Ok", "")
    }
}