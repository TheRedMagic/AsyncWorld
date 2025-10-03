package gg.undefinedaquatic.asyncWorld.format

import net.minecraft.nbt.CompoundTag
import net.minecraft.world.level.ChunkPos

interface WorldFormat {

    fun read(byteArray: ByteArray?): CompoundTag?

    fun write(chunkPos: ChunkPos, chunkData: CompoundTag?): ByteArray?

}