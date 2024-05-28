package shadows.darkohud;

import dev.shadowsoffire.placebo.network.MessageHelper;
import dev.shadowsoffire.placebo.network.PacketDistro;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.network.NetworkConstants;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(DarkoHud.MODID)
public class DarkoHud {

	public static final String MODID = "darkohud";
	public static final Logger LOGGER = LogManager.getLogger(MODID);
	//Formatter::off
    public static final SimpleChannel CHANNEL = NetworkRegistry.ChannelBuilder
            .named(new ResourceLocation(MODID, MODID))
            .clientAcceptedVersions(s->true)
            .serverAcceptedVersions(s->true)
            .networkProtocolVersion(() -> "1.0.0")
            .simpleChannel();
    //Formatter::on

	public DarkoHud() {
		if (FMLEnvironment.dist == Dist.CLIENT) {
			MinecraftForge.EVENT_BUS.register(new HudRenderer());
		}
		MessageHelper.registerMessage(CHANNEL, 0, new SeedMessage.Provider());
		ModLoadingContext.get().registerExtensionPoint(IExtensionPoint.DisplayTest.class, () -> new IExtensionPoint.DisplayTest(() -> NetworkConstants.IGNORESERVERONLY, (a, b) -> true));
		MinecraftForge.EVENT_BUS.addListener(this::login);
	}

	public void login(EntityJoinLevelEvent e) {
		if (e.getEntity() instanceof ServerPlayer p) {
			PacketDistro.sendTo(CHANNEL, new SeedMessage(p.serverLevel().getSeed()), p);
		}
	}

}
