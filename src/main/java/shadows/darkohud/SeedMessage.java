package shadows.darkohud;

import java.util.function.Supplier;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent.Context;
import shadows.placebo.util.NetworkUtils;
import shadows.placebo.util.NetworkUtils.MessageProvider;

public class SeedMessage extends MessageProvider<SeedMessage> {

	private long seed;

	public SeedMessage(long seed) {
		this.seed = seed;
	}

	@Override
	public void write(SeedMessage msg, PacketBuffer buf) {
		buf.writeLong(msg.seed);
	}

	@Override
	public SeedMessage read(PacketBuffer buf) {
		return new SeedMessage(buf.readLong());
	}

	@Override
	public void handle(SeedMessage msg, Supplier<Context> ctx) {
		NetworkUtils.handlePacket(() -> () -> {
			HudRenderer.seed = msg.seed;
			HudRenderer.hasSeed = true;
		}, ctx.get());
	}

}
