package gg.undefinedaquatic.asyncWorld.driver

import net.minecraft.nbt.StreamTagVisitor
import net.minecraft.world.level.ChunkPos

interface WorldDriver {

    fun readRegion(chunkPos: ChunkPos): ByteArray?

    fun writeRegion(chunkPos: ChunkPos, chunkData: ByteArray?)

    fun readEntity(chunkPos: ChunkPos): ByteArray?

    fun writeEntity(chunkPos: ChunkPos, chunkData: ByteArray?)

    fun scanChunk(chunkPos: ChunkPos, visitor: StreamTagVisitor)

    val isRegion: Boolean

    fun close()

    fun flush()

}