package pw.passy.installer

import javafx.concurrent.Task
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.stage.Stage
import org.binaryone.jutils.io.FileUtils
import org.binaryone.jutils.io.IOUtils
import pw.passy.installer.controller.InstallingController
import java.io.File
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
                Utils.downloadFile(URL(""), File(dir, "all.zip"))


                updateProgress(40,100)
                updateMessage("Unzipping Components...")
                Utils.unZip(File(dir, "all.zip").absolutePath, File(dir, "bin").absolutePath)


                updateProgress(60,100)
                updateMessage("Setting up Configs...")
                //MYSQL
                var mysql = String(IOUtils.convertStreamToByteArray(javaClass.getResourceAsStream("/configs/my.ini")))
                mysql = mysql.replace("%%%MYSQLPORT%%%", mySqlPort.toString())
                FileUtils.writeFile(mysql, File(dir, "bin/mysql/my.ini").absolutePath)

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

                Runtime.getRuntime().exec(File(dir, "bin/mysql/bin/mysqladmin.exe").absolutePath + " -u root $mysqlPass")



                /*
  To Replace:
  %%%EXTENSIONDIR%%% in php.ini

  %%%WEBROOT%%% in nginx.conf
  %%%WEBPORT%%% in nginx.conf
  %%%PHPPORT%%% in nginx.conf

   %%%MYSQLPORT%%% in my.ini

   %%%PASSWORD%%% in config.inc.php

   */
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