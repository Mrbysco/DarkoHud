package shadows.darkohud;

import dev.shadowsoffire.placebo.network.MessageHelper;
import dev.shadowsoffire.placebo.network.MessageProvider;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SeedMessage {

	private long seed;

	public SeedMessage(long seed) {
		this.seed = seed;
	}

	public static class Provider implements MessageProvider<SeedMessage> {
		@Override
		public Class<?> getMsgClass() {
			return SeedMessage.class;
		}

		@Override
		public void write(SeedMessage msg, FriendlyByteBuf buf) {
			buf.writeLong(msg.seed);
		}

		@Override
		public SeedMessage read(FriendlyByteBuf buf) {
			return new SeedMessage(buf.readLong());
		}

		@Override
		public void handle(SeedMessage msg, Supplier<NetworkEvent.Context> ctx) {
			MessageHelper.handlePacket(() -> {
				HudRenderer.seed = msg.seed;
				HudRenderer.hasSeed = true;
			}, ctx);
		}
	}

}
