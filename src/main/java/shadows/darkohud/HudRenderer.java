package shadows.darkohud;

import java.util.Iterator;
import java.util.Locale;
import java.util.Random;
import java.util.stream.StreamSupport;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biomes;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class HudRenderer {

	public static long seed = 0;
	public static boolean hasSeed = false;

	@SubscribeEvent
	public void render(RenderGuiOverlayEvent.Post e) {
		if (!e.getOverlay().id().equals(new ResourceLocation("hotbar"))) return;
		Minecraft mc = Minecraft.getInstance();
		if (mc.screen != null) return;
		Player p = mc.player;
		if (p == null) return;
		GuiGraphics guiGraphics = e.getGuiGraphics();
		Iterable<ItemStack> items = p.getAllSlots();
		int amount = (int) StreamSupport.stream(items.spliterator(), false).filter(s -> !s.isEmpty()).count();
		int y = mc.getWindow().getGuiScaledHeight() / 2;
		Iterator<ItemStack> it = items.iterator();
		for (int i = 0; i < amount; i++) {
			ItemStack s = it.next();
			if (s.isEmpty()) {
				i--;
				continue;
			}
			renderItem(guiGraphics, s, 2, y - amount * 8 + 16 * (amount - i) - 8);
		}

		Level level = mc.level;
		if(level == null) return;
		long day = level.getGameTime() / 24000;
		String time = timeToString(level.dayTime());
		ResourceLocation biome = level.getBiome(p.blockPosition()).unwrapKey().map(ResourceKey::location).orElse(new ResourceLocation("unknown"));
		String biomeUnloc = "biome." + biome.getNamespace() + "." + biome.getPath();
		MutableComponent biomeTxt = Component.translatable(biomeUnloc);
		if (isSlimeChunk(level, p.blockPosition())) biomeTxt = biomeTxt.withStyle(ChatFormatting.DARK_GREEN);
		MutableComponent txt = Component.translatable("Day %s (%s) %s", Component.translatable("%s", day), Component.translatable("%s", time).withStyle(ChatFormatting.GREEN), biomeTxt);
		PoseStack matrix = new PoseStack();
		float scale = 1;
		int width = mc.getWindow().getGuiScaledWidth();
		int barWidth = p.getOffhandItem().isEmpty() ? 91 : 121;
		if (width / 2 - barWidth < mc.font.width(txt)) {
			scale = (width / 2F - barWidth) / mc.font.width(txt);
		}
		matrix.scale(scale, scale, 1);

		guiGraphics.drawString(mc.font, txt, (int)(1 / scale), (int)((mc.getWindow().getGuiScaledHeight() - mc.font.lineHeight * scale) / scale), 0xFFFFFF, true);
	}

	private void drawItemText(Font font, ItemStack stack, int x, int y, String txt) {
		if (!stack.isEmpty()) {
			PoseStack poseStack = new PoseStack();
			if (stack.getCount() != 1 || txt != null) {
				String s = txt == null ? String.valueOf(stack.getCount()) : txt;
				poseStack.translate(0.0F, 0.0F, 200.0F);
				MultiBufferSource.BufferSource irendertypebuffer$impl = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
				font.drawInBatch(s, (float) (x + 19 - 2 - font.width(s)), (float) (y + 6 + 3), 16777215, true, poseStack.last().pose(), irendertypebuffer$impl, Font.DisplayMode.NORMAL, 0, 15728880);
				irendertypebuffer$impl.endBatch();
			}
		}
	}

	private void renderItem(GuiGraphics guiGraphics, ItemStack stack, int x, int y) {
		guiGraphics.renderItem(stack, x, y);
		if (stack.isDamageableItem()) {
			int dmg = stack.getDamageValue();
			int max = stack.getMaxDamage();
			String msg = "" + (max - dmg);
			Font font = Minecraft.getInstance().font;
			drawItemText(font, stack, x + 2 + font.width(msg), y - 4, msg);
		}
		if (stack.getCount() > 1) {
			drawItemText(Minecraft.getInstance().font, stack, x, y, null);
		}
	}

	//https://github.com/Lunatrius/InGame-Info-XML/blob/master/src/main/java/com/github/lunatrius/ingameinfo/tag/TagTime.java#L51
	private String timeToString(long time) {
		long hour = (time / 1000 + 6) % 24;
		final long minute = (time % 1000) * 60 / 1000;
		String ampm = "AM";
		if (hour >= 12) {
			hour -= 12;
			ampm = "PM";
		}
		if (hour == 0) {
			hour += 12;
		}
		return String.format(Locale.ENGLISH, "%02d:%02d %s", hour, minute, ampm);
	}

	//https://github.com/Lunatrius/LunatriusCore/blob/master/src/main/java/com/github/lunatrius/core/world/chunk/ChunkHelper.java
	private static final Random RANDOM = new Random();

	public static boolean isSlimeChunk(final long seed, final BlockPos pos) {
		final int x = pos.getX() >> 4;
		final int z = pos.getZ() >> 4;
		RANDOM.setSeed(seed + (long) (x * x * 4987142) + (long) (x * 5947611) + (long) (z * z) * 4392871L + (long) (z * 389711) ^ 987234911L);
		return RANDOM.nextInt(10) == 0;
	}

	public boolean isSlimeChunk(Level world, BlockPos pos) {
		return hasSeed && isSlimeChunk(seed, pos) || Biomes.SWAMP.equals(world.getBiome(pos).unwrapKey().orElse(null)) && pos.getY() > 50 && pos.getY() < 70;
	}

}
