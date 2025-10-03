package gg.undefinedaquatic.asyncWorld.driver

import gg.undefinedaquatic.asyncWorld.StorageInfo
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.StreamTagVisitor
import net.minecraft.world.level.ChunkPos
import net.minecraft.world.level.chunk.storage.RegionFile
import net.minecraft.world.level.chunk.storage.RegionFileStorage
import net.minecraft.world.level.chunk.storage.RegionStorageInfo
import java.nio.file.Path

class DriverRegionFileStorage(
    info: RegionStorageInfo,
    folder: Path,
    sync: Boolean,
    val storageInfo: StorageInfo
) : RegionFileStorage(
    info,
    folder,
    sync
) {

    override fun read(chunkPos: ChunkPos): CompoundTag? {
        return storageInfo.read(chunkPos)
    }

    override fun write(chunkPos: ChunkPos, chunkData: CompoundTag?) {
        storageInfo.write(chunkPos, chunkData)
    }

    override fun getRegionFile(chunkcoordintpair: ChunkPos): RegionFile {
        return super.getRegionFile(chunkcoordintpair)
    }

    override fun scanChunk(chunkPos: ChunkPos, visitor: StreamTagVisitor) {
        super.scanChunk(chunkPos, visitor)
    }

    override fun info(): RegionStorageInfo {
        return super.info()
    }

    override fun close() {
        super.close()
    }

    override fun flush() {
        super.flush()
    }
}