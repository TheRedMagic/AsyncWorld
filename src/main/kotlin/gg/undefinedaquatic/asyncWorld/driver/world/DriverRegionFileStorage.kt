package gg.undefinedaquatic.asyncWorld.driver.world

import gg.undefinedaquatic.asyncWorld.StorageInfo
import io.papermc.paper.configuration.GlobalConfiguration
import net.minecraft.FileUtil
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.StreamTagVisitor
import net.minecraft.world.level.ChunkPos
import net.minecraft.world.level.chunk.storage.RegionFile
import net.minecraft.world.level.chunk.storage.RegionFileStorage
import net.minecraft.world.level.chunk.storage.RegionFileVersion
import net.minecraft.world.level.chunk.storage.RegionStorageInfo
import java.nio.file.Path

class DriverRegionFileStorage(
    val info: RegionStorageInfo,
    val folder: Path,
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

    fun write(chunkPos: ChunkPos, byteBuf: ByteArray) {
        storageInfo.driver.writeRegion(chunkPos, byteBuf)
    }

    override fun getRegionFile(chunkPos: ChunkPos): RegionFile {
        synchronized(this) {
            val key = ChunkPos.asLong(chunkPos.x shr 5, chunkPos.z shr 5)
            var ret = this.regionCache.getAndMoveToFirst(key)
            if (ret != null) {
                return ret
            } else {
                if (this.regionCache.size >= GlobalConfiguration.get().misc.regionFileCacheSize) {
                    (this.regionCache.removeLast() as RegionFile).close()
                }

                val regionPath = this.folder.resolve(getRegionFileName(chunkPos.x, chunkPos.z))
                //this.createRegionFile(key)
                FileUtil.createDirectoriesSafe(this.folder)
                ret = DriverRegionFile(this, this.info, regionPath, this.folder, RegionFileVersion.getCompressionFormat())
                this.regionCache.putAndMoveToFirst(key, ret)
                return ret
            }
        }
    }

    private fun getRegionFileName(chunkX: Int, chunkZ: Int): String {
        return "r." + (chunkX shr 5) + "." + (chunkZ shr 5) + ".mca"
    }

//    private fun createRegionFile(position: Long) {
//        synchronized(this.nonExistingRegionFiles) {
//            this.nonExistingRegionFiles.remove(position)
//        }
//    }

    override fun scanChunk(chunkPos: ChunkPos, visitor: StreamTagVisitor) {
        storageInfo.driver.scanChunk(chunkPos, visitor)
    }

    override fun close() {
        storageInfo.driver.close()
    }

    override fun flush() {
        storageInfo.driver.flush()
    }

}