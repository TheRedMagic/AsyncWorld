package gg.undefinedaquatic.asyncWorld.driver

import gg.undefinedaquatic.asyncWorld.format.WorldFormat
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.StreamTagVisitor
import net.minecraft.world.level.ChunkPos
import net.minecraft.world.level.chunk.storage.RegionFile
import net.minecraft.world.level.chunk.storage.RegionStorageInfo

interface WorldDriver {

    fun readRegion(chunkPos: ChunkPos): ByteArray?

    fun writeRegion(chunkPos: ChunkPos, chunkData: ByteArray?)

    fun readEntity(chunkPos: ChunkPos): ByteArray?

    fun writeEntity(chunkPos: ChunkPos, chunkData: ByteArray?)

    fun getRegionFile(chunkcoordintpair: ChunkPos): RegionFile

    fun scanChunk(chunkPos: ChunkPos, visitor: StreamTagVisitor)

    fun info(): RegionStorageInfo

    fun close()

    fun flush()

}