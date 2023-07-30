package org.phantazm.zombies.autosplits.packet;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.PacketByteBuf;
import org.jetbrains.annotations.NotNull;
import org.phantazm.messaging.serialization.DataWriter;

import java.util.Objects;

/**
 * A {@link DataWriter} that writes to a {@link PacketByteBuf}.
 */
public class PacketByteBufDataWriter implements DataWriter {

    private final PacketByteBuf packetByteBuf;

    /**
     * Creates a {@link PacketByteBufDataWriter}.
     *
     * @param packetByteBuf The {@link PacketByteBuf} to write to
     */
    public PacketByteBufDataWriter(@NotNull PacketByteBuf packetByteBuf) {
        this.packetByteBuf = Objects.requireNonNull(packetByteBuf, "packetByteBuf");
    }

    public PacketByteBufDataWriter() {
        this(PacketByteBufs.create());
    }

    @Override
    public void writeByte(byte data) {
        packetByteBuf.writeByte(data);
    }

    @Override
    public void writeInt(int data) {
        packetByteBuf.writeInt(data);
    }

    @Override
    public byte @NotNull [] toByteArray() {
        return packetByteBuf.array();
    }

    public @NotNull PacketByteBuf getBuf() {
        return packetByteBuf;
    }
}
