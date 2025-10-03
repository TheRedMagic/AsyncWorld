package gg.undefinedaquatic.asyncWorld

import gg.undefinedaquatic.asyncWorld.driver.WorldDriver
import gg.undefinedaquatic.asyncWorld.format.WorldFormat
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.level.ChunkPos

class StorageInfo(
    val driver: WorldDriver,
    val format: WorldFormat,
) {

    val isEntity = !driver.isRegion

    fun read(chunkPos: ChunkPos): CompoundTag? {
        return format.read(chunkPos, if (isEntity) driver.readEntity(chunkPos) else driver.readRegion(chunkPos))
    }

    fun write(chunkPos: ChunkPos, chunkData: CompoundTag?) {
        if (isEntity) {
            driver.writeEntity(chunkPos, format.write(chunkPos, chunkData))
        } else {
            driver.writeRegion(chunkPos, format.write(chunkPos, chunkData))
        }
    }

}