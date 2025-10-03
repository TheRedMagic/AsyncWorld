package gg.undefinedaquatic.asyncWorld.driver.custom

import gg.undefinedaquatic.asyncWorld.driver.WorldDriver
import net.minecraft.nbt.StreamTagVisitor
import net.minecraft.world.level.ChunkPos
import net.minecraft.world.level.chunk.storage.RegionFile
import net.minecraft.world.level.chunk.storage.RegionStorageInfo

class SQLLiteDriver : WorldDriver {

    override fun readRegion(chunkPos: ChunkPos): ByteArray? {
        TODO("Not yet implemented")
    }

    override fun writeRegion(chunkPos: ChunkPos, chunkData: ByteArray?) {
        TODO("Not yet implemented")
    }

    override fun readEntity(chunkPos: ChunkPos): ByteArray? {
        TODO("Not yet implemented")
    }

    override fun writeEntity(chunkPos: ChunkPos, chunkData: ByteArray?) {
        TODO("Not yet implemented")
    }

    override fun getRegionFile(chunkcoordintpair: ChunkPos): RegionFile {
        TODO("Not yet implemented")
    }

    override fun scanChunk(chunkPos: ChunkPos, visitor: StreamTagVisitor) {
        TODO("Not yet implemented")
    }

    override fun info(): RegionStorageInfo {
        TODO("Not yet implemented")
    }

    override fun close() {
        TODO("Not yet implemented")
    }

    override fun flush() {
        TODO("Not yet implemented")
    }

}