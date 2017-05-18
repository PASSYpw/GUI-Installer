package pw.passy.installer

import javafx.concurrent.Task
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.stage.Stage
import org.binaryone.jutils.io.FileUtils
import org.binaryone.jutils.io.IOUtils
import org.ini4j.Ini
import pw.passy.installer.controller.InstallingController
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.net.URL
import javax.rmi.CORBA.Util

class Installer(val nginxPort:Int, val mySqlPort:Int, val phpPort:Int, val mysqlPass:String, val stage:Stage, val path:String) {


    private var task:Task<Void>? = null

    init {
        val loader = FXMLLoader()
        val parent:Parent = loader.load(javaClass.getResourceAsStream("/InstallingGui.fxml"))
        val controller:InstallingController = loader.getController()
        stage.scene = Scene(parent)

        install(controller)
    }
    private fun install(con:InstallingController) {
        task = object : Task<Void>() {
            @Throws(Exception::class)
            override fun call(): Void? {

                val dir = File(path)

                if(dir.exists()) {
                    if(!dir.isDirectory || dir.listFiles().isNotEmpty()) {

                        updateMessage("Failed, target folder is not a folder or is not empty")
                        updateProgress(100,100)

                        return null
                    }
                } else {
                    if(!dir.mkdir()) {
                        updateMessage("Failed, could not create target Folder!")
                        updateProgress(100,100)
                        return null
                    }
                }
                updateProgress(5,100)
                updateMessage("Downloading Components...")
                Utils.downloadFile(URL("https://api.liz3.net/storage/passy/services.zip"), File(dir, "all.zip"))


                updateProgress(40,100)
                updateMessage("Unzipping Components...")
                Utils.unZip(File(dir, "all.zip").absolutePath, File(dir, "bin").absolutePath)

                updateProgress(55,100)
                updateMessage("Downloading PASSY Platform...")
                Utils.downloadFile(URL("https://api.liz3.net/storage/passy/passy.zip"), File(dir, "passy.zip"))

                updateProgress(70,100)
                updateMessage("Unzipping PASSY Platform...")
                Utils.unZip(File(dir, "passy.zip").absolutePath, File(dir,"web").absolutePath)


                updateProgress(80,100)
                updateMessage("Setting up Configs...")

                var mysql = String(IOUtils.convertStreamToByteArray(javaClass.getResourceAsStream("/configs/my.ini")))
                mysql = mysql.replace("%%%MYSQLPORT%%%", mySqlPort.toString())
                mysql = mysql.replace("%%%MYSQLROOT%%%", File(dir, "bin/mysql").absolutePath.replace("\\","/"))
                mysql = mysql.replace("%%%MYSQLDATA%%%", File(dir, "bin/mysql/data").absolutePath.replace("\\","/"))
                FileUtils.writeFile(mysql, File(dir, "bin/mysql/my.ini").absolutePath)
                File(dir, "bin/mysql/data").mkdir()
                var nginx = String(IOUtils.convertStreamToByteArray(javaClass.getResourceAsStream("/configs/nginx.conf")))
                nginx = nginx.replace("%%%WEBROOT%%%", File(dir, "web").absolutePath)
                nginx = nginx.replace("%%%WEBPORT%%%", nginxPort.toString())
                nginx = nginx.replace("%%%PHPPORT%%%", phpPort.toString())
                FileUtils.writeFile(nginx, File(dir, "bin/nginx/conf/nginx.conf").absolutePath)

                var php = String(IOUtils.convertStreamToByteArray(javaClass.getResourceAsStream("/configs/php.ini")))
                php = php.replace("%%%EXTENSIONDIR%%%", File(dir, "bin/php/ext").absolutePath)
                FileUtils.writeFile(php, File(dir, "bin/php/php.ini").absolutePath)

                var passyConf = String(IOUtils.convertStreamToByteArray(javaClass.getResourceAsStream("/configs/config.inc.php")))
                passyConf = passyConf.replace("%%%PASSWORD%%%", mysqlPass)
                FileUtils.writeFile(passyConf, File(dir, "web/config.inc.php").absolutePath)

                val config = File(dir, "info.ini")
                config.createNewFile()
                val ini = Ini(config)
                val sec = ini.add("data")
                sec.put("php", phpPort.toString())
                sec.put("home", dir.absolutePath)
                ini.store()

                updateProgress(92,100)
                updateMessage("Downloading Client...")
                Utils.downloadFile(URL("https://api.liz3.net/storage/passy/client.jar"), File(dir, "Client.jar"))

                updateProgress(100,100)
                updateMessage("Finished!")
                con.contBtn.setOnAction {

                    Thread(Runnable {

                        val pb = ProcessBuilder("java", "-jar", File(dir, "Client.jar").absolutePath, "-first", mysqlPass)
                        val p = pb.start()

                        val inStream = p.inputStream
                        val r = BufferedReader(InputStreamReader(inStream))

                        do {
                            val line = r.readLine()
                            if(line == null) break


                            println(line)
                        }while (true)


                    }).start()
                }
                con.contBtn.isDisable = false


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