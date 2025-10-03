package gg.undefinedaquatic.asyncWorld.format

import io.netty.buffer.ByteBufOutputStream
import io.netty.buffer.Unpooled
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtIo
import net.minecraft.world.level.ChunkPos
import net.minecraft.world.level.chunk.storage.RegionFileStorage.RegionFileSizeException
import java.io.ByteArrayInputStream
import java.io.DataInputStream

class AnvilFormat: WorldFormat {
    override fun read(chunkPos: ChunkPos, byteArray: ByteArray?): CompoundTag? {
        return DataInputStream(ByteArrayInputStream(byteArray ?: return null)).use { dataInputStream ->
            NbtIo.read(dataInputStream)
        }
    }

    override fun write(
        chunkPos: ChunkPos,
        chunkData: CompoundTag?
    ): ByteArray? {
        val chunkDataOutputStream = ByteBufOutputStream(Unpooled.buffer())

        try {
            NbtIo.write(chunkData, chunkDataOutputStream)
            chunkDataOutputStream.close()
        } catch (_: RegionFileSizeException) {
            println("Well shit....")
        }
        return chunkDataOutputStream.buffer().array()
    }
}