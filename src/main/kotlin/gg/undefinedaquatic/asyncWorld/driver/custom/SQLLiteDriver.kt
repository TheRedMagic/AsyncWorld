package gg.undefinedaquatic.asyncWorld.driver.custom

import gg.undefinedaquatic.asyncWorld.driver.WorldDriver
import net.minecraft.nbt.StreamTagVisitor
import net.minecraft.world.level.ChunkPos
import net.minecraft.world.level.chunk.storage.RegionFile
import net.minecraft.world.level.chunk.storage.RegionStorageInfo
import java.io.File
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException

class SQLLiteDriver(
    val file: File,
    override val isRegion: Boolean,
) : WorldDriver {

    private val connection: Connection

    init {
        // Make sure the parent directory exists
        if (!file.parentFile.exists()) {
            file.parentFile.mkdirs()
        }

        // Initialize the database connection
        connection = DriverManager.getConnection("jdbc:sqlite:${file.absolutePath}")

        // Create the necessary tables if they don't exist
        createTables()
    }

    private fun createTables() {
        connection.createStatement().use { statement ->
            // Create table for regions
            statement.execute("""
                CREATE TABLE IF NOT EXISTS regions (
                    chunk_pos INTEGER NOT NULL PRIMARY KEY,
                    data BLOB
                )
            """)

            // Create table for entities
            statement.execute("""
                CREATE TABLE IF NOT EXISTS entities (
                    chunk_pos INTEGER NOT NULL PRIMARY KEY,
                    data BLOB
                )
            """)
        }
    }

    override fun readRegion(chunkPos: ChunkPos): ByteArray? {
        try {
            connection.prepareStatement("SELECT data FROM regions WHERE chunk_pos = ?").use { ps ->
                ps.setLong(1, chunkPos.longKey)
                ps.executeQuery().use { rs ->
                    if (rs.next()) {
                        return rs.getBytes("data")
                    }
                }
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }
        return null
    }
    override fun writeRegion(chunkPos: ChunkPos, chunkData: ByteArray?) {
        try {
            if (chunkData == null) {
                // Delete the region data
                connection.prepareStatement("DELETE FROM regions WHERE chunk_pos = ?").use { ps ->
                    ps.setLong(1, chunkPos.longKey)
                    ps.executeUpdate()
                }
            } else {
                // Insert or update region data
                connection.prepareStatement(
                    "INSERT OR REPLACE INTO regions (chunk_pos, data) VALUES (?, ?)"
                ).use { ps ->
                    ps.setLong(1, chunkPos.longKey)
                    ps.setBytes(2, chunkData)
                    ps.executeUpdate()
                }
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }
    }

    override fun readEntity(chunkPos: ChunkPos): ByteArray? {
        try {
            connection.prepareStatement("SELECT data FROM entities WHERE chunk_pos = ?").use { ps ->
                ps.setLong(1, chunkPos.longKey)
                ps.executeQuery().use { rs ->
                    if (rs.next()) {
                        return rs.getBytes("data")
                    }
                }
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }
        return null
    }

    override fun writeEntity(chunkPos: ChunkPos, chunkData: ByteArray?) {
        try {
            if (chunkData == null) {
                // Delete the entity data
                connection.prepareStatement("DELETE FROM entities WHERE chunk_pos = ?").use { ps ->
                    ps.setLong(1, chunkPos.longKey)
                    ps.executeUpdate()
                }
            } else {
                // Insert or update entity data
                connection.prepareStatement(
                    "INSERT OR REPLACE INTO entities (chunk_pos, data) VALUES (?, ?)"
                ).use { ps ->
                    ps.setLong(1, chunkPos.longKey)
                    ps.setBytes(2, chunkData)
                    ps.executeUpdate()
                }
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }
    }

    override fun scanChunk(chunkPos: ChunkPos, visitor: StreamTagVisitor) {

    }

    override fun close() {

    }

    override fun flush() {

    }

}