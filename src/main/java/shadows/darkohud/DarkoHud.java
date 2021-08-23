package shadows.darkohud;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.network.FMLNetworkConstants;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import shadows.placebo.util.NetworkUtils;

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
		NetworkUtils.registerMessage(CHANNEL, 0, new SeedMessage(0));
		ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.DISPLAYTEST, () -> Pair.of(() -> FMLNetworkConstants.IGNORESERVERONLY, (a, b) -> true));
		MinecraftForge.EVENT_BUS.addListener(this::login);
	}

	public void login(EntityJoinWorldEvent e) {
		if (e.getEntity() instanceof ServerPlayerEntity) {
			ServerPlayerEntity p = (ServerPlayerEntity) e.getEntity();
			NetworkUtils.sendTo(CHANNEL, new SeedMessage(p.getLevel().getSeed()), p);
		}
	}

}
