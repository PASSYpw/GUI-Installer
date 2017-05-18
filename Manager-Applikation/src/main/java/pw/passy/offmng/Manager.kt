package pw.passy.offmng

import org.ini4j.Ini
import java.io.*
import java.nio.Buffer

/**
 * Created by liz3 on 17.05.17.
 */
class Manager(val args:Array<String>) {

    var homeFolder = ""
    var firstStart = false
    var mysqlPass = ""
    var phpPort = 0
    var services:Triple<Process?, Process?, Process?>? = null
    init {


    for(current:String in args) {
            if(current.equals("-home")) {
                val index = args.indexOf(current) + 1
                homeFolder = args[index]
            }
            if(current.equals("first")) {
                val index = args.indexOf(current) + 1
                mysqlPass = args[index]
                firstStart = true

            }

        }
        if(!firstStart) loadValuesFromConfig()
        val workerThread = Thread(Runnable {
            bootstrap()
        })
        workerThread.name = "PASSY ROOT Worker"
        workerThread.priority = Thread.MAX_PRIORITY
        workerThread.start()
    }
    fun restart() {
        services!!.first!!.destroy()
        services!!.second!!.destroy()
        services!!.third!!.destroy()

        services = bootServices()
    }
    private fun bootstrap() {

        if(homeFolder.equals("")) homeFolder = File(".").canonicalPath
        services = bootServices()
        println("PHP: " + services!!.first)
        println("MySQL: " + services!!.second)
        println("NGINX: " + services!!.third)
        Thread(Runnable {
            Controller.manager = this
            GuiManager.start()
        }).start()

    }
    private fun bootServices(): Triple<Process?, Process?, Process?> {

        var phpProcess:Process? = null
        var mysqlProcess:Process? = null
        var nginxProcess:Process? = null
        //PHP
        try {
            val phpBuilder = ProcessBuilder(File(homeFolder, "bin/php/php-cgi.exe").absolutePath,
                    "-b","127.0.0.1:$phpPort", "-c",
                    "php.ini")
            phpBuilder.directory(File(homeFolder, "bin/php"))

            val phpThread = Thread(Runnable {
                phpProcess = phpBuilder.start()
                read(phpProcess!!.inputStream, "PHP-POOL")
            })
            phpThread.name = "PASSY PHP Worker Thread"
            phpThread.start()
            println("PHP Worker Thread started")

            val mysqlBuilder = ProcessBuilder(File(homeFolder, "bin/mysql/bin/mysqld.exe").absolutePath)
            mysqlBuilder.directory(File(homeFolder, "bin/mysql"))
            val sqlThread = Thread(Runnable {
                if(firstStart) {
                    Runtime.getRuntime().exec(File(homeFolder, "bin/mysql/bin/mysqld.exe").absolutePath + "--initialize-insecure")
                    println("Is first run")
                    Thread.sleep(7000)
                    mysqlProcess = mysqlBuilder.start()
                    read(mysqlProcess!!.inputStream, "MYSQL-Server")
                    Thread.sleep(7000)
                    val passSetterBP = ProcessBuilder(File(homeFolder, "bin/mysql/bin/mysqladmin.exe").absolutePath, "-u","root","password", mysqlPass)
                    passSetterBP.directory(File(homeFolder, "bin/mysql"))
                    passSetterBP.start()
                    val creatorBP = ProcessBuilder(File(homeFolder, "bin/mysql/bin/mysql.exe").absolutePath, "-u","root","-p")
                    creatorBP.directory(File(homeFolder, "bin/mysql"))
                    val process = creatorBP.start()
                    val writer = OutputStreamWriter(process.outputStream)
                    Thread.sleep(1250)
                    writer.write("$mysqlPass\n")
                    writer.flush()
                    Thread.sleep(1250)
                    writer.write("CREATE DATABASE `passy`\n")
                    writer.close()
                    process.destroy()
                } else {
                    mysqlProcess = mysqlBuilder.start()
                    read(mysqlProcess!!.inputStream, "MYSQL-Server")
                }
            })
            sqlThread.name = "PASSY MySql Worker Thread"
            sqlThread.start()
            println("MySQL Worker Thread started")


            val nginxBuilder = ProcessBuilder(File(homeFolder, "bin/nginx/nginx.exe").absolutePath)
            nginxBuilder.directory(File(homeFolder, "bin/nginx"))
            val nginxThread = Thread(Runnable {
                nginxProcess = nginxBuilder.start()
                read(nginxProcess!!.inputStream, "Nginx")
            })
            nginxThread.name = "PASSY NGINX Worker Thread"
            nginxThread.start()
            println("Nginx Worker Thread started")
        }catch (e:Exception) {
            e.printStackTrace()
        }

        Thread.sleep(10000)

        println("Returning Processes")
        return Triple(phpProcess, mysqlProcess, nginxProcess)
    }
    private fun loadValuesFromConfig() : Boolean {
        val path = {
            if(homeFolder.equals("")) {
                //val path will be the local folder
               val x =  File(".").canonicalPath
                homeFolder = x
                x
            } else {
                //val path will be homeFolder
                homeFolder
            }
        }

        val f = Ini(File(path.invoke(),"info.ini"))

        if(!f.containsKey("data")) return false

        val sec = f["data"]

        if(!sec!!.containsKey("php")) return false

        phpPort = sec.get("php")!!.toInt()

        return true
    }

    private fun read(inStream:InputStream, name:String) {

        Thread(Runnable {

            val reader = BufferedReader(InputStreamReader(inStream))
            do {
                val l = reader.readLine() ?: break

                println("[$name]: $l")

            }while (true)
        }).start()
    }
}