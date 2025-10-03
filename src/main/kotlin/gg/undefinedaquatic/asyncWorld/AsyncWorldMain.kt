package gg.undefinedaquatic.asyncWorld

import gg.undefinedaquatic.asyncWorld.driver.custom.SQLLiteDriver
import gg.undefinedaquatic.asyncWorld.format.AnvilFormat
import gg.undefinedaquatic.asyncWorld.loader.WorldUtil.createWorldAsync
import org.bukkit.Bukkit
import org.bukkit.WorldCreator
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import java.io.File

class AsyncWorldMain : JavaPlugin() {

    companion object {
        lateinit var INSTANCE: AsyncWorldMain
            private set
    }

    override fun onLoad() {
        INSTANCE = this
    }

    override fun onDisable() {

    }

    override fun onEnable() {
        val file = dataFolder.resolve("worlddb.db")
        getCommand("testing")!!.setExecutor(TestingCommand(file))
    }

}

class TestingCommand(
    val file: File
) : CommandExecutor {

    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): Boolean {

        Bukkit.getScheduler().runTaskAsynchronously(AsyncWorldMain.INSTANCE, Runnable {
            val world = WorldCreator("testing")
                .createWorldAsync(StorageInfo(SQLLiteDriver(file, true), AnvilFormat()))

            (sender as Player).teleportAsync(world!!.spawnLocation)
        })
        return true
    }

}