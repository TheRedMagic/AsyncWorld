package gg.undefinedaquatic.asyncWorld.driver.world

import net.minecraft.world.level.ChunkPos
import net.minecraft.world.level.chunk.storage.RegionFile
import net.minecraft.world.level.chunk.storage.RegionFileVersion
import net.minecraft.world.level.chunk.storage.RegionStorageInfo
import java.nio.ByteBuffer
import java.nio.file.Path

class DriverRegionFile(
    val storage: DriverRegionFileStorage,
    info: RegionStorageInfo,
    path: Path,
    externalFileDir: Path,
    version: RegionFileVersion,
) : RegionFile(info,path,externalFileDir, version,false) {

    override fun write(chunkPos: ChunkPos, chunkData: ByteBuffer) {
        storage.write(chunkPos, chunkData.array())
    }
}