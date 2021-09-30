package shadows.darkohud;

import java.util.Iterator;
import java.util.Locale;
import java.util.Random;
import java.util.stream.StreamSupport;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biomes;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class HudRenderer {

	public static long seed = 0;
	public static boolean hasSeed = false;

	@SubscribeEvent
	public void render(RenderGameOverlayEvent.Post e) {
		if (e.getType() != ElementType.ALL) return;
		Minecraft mc = Minecraft.getInstance();
		if (mc.screen != null) return;
		PlayerEntity p = mc.player;
		if (p == null) return;
		ItemRenderer rdn = mc.getItemRenderer();
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
			renderItem(rdn, s, 2, y - amount * 8 + 16 * (amount - i) - 8);
		}

		World world = mc.level;
		long day = world.getGameTime() / 24000;
		String time = timeToString(world.dayTime());
		ResourceLocation biome = world.getBiomeName(p.blockPosition()).map(RegistryKey::location).orElse(new ResourceLocation("unknown"));
		String biomeUnloc = "biome." + biome.getNamespace() + "." + biome.getPath();
		IFormattableTextComponent biomeTxt = new TranslationTextComponent(biomeUnloc);
		if (isSlimeChunk(world, p.blockPosition())) biomeTxt = biomeTxt.withStyle(TextFormatting.DARK_GREEN);
		TranslationTextComponent txt = new TranslationTextComponent("Day %s (%s) %s", new TranslationTextComponent("%s", day), new TranslationTextComponent("%s", time).withStyle(TextFormatting.GREEN), biomeTxt);
		MatrixStack matrix = new MatrixStack();
		float scale = 1;
		int width = mc.getWindow().getGuiScaledWidth();
		int barWidth = p.getOffhandItem().isEmpty() ? 91 : 121;
		if (width / 2 - barWidth < mc.font.width(txt)) {
			scale = (width / 2F - barWidth) / mc.font.width(txt);
		}
		matrix.scale(scale, scale, 1);
		mc.font.drawShadow(matrix, txt, 1 / scale, (mc.getWindow().getGuiScaledHeight() - mc.font.lineHeight * scale) / scale, 0xFFFFFF);
	}

	private void drawItemText(FontRenderer font, ItemStack stack, int x, int y, String txt) {
		if (!stack.isEmpty()) {
			MatrixStack matrixstack = new MatrixStack();
			if (stack.getCount() != 1 || txt != null) {
				String s = txt == null ? String.valueOf(stack.getCount()) : txt;
				matrixstack.translate(0.0D, 0.0D, Minecraft.getInstance().getItemRenderer().blitOffset + 200);
				IRenderTypeBuffer.Impl irendertypebuffer$impl = IRenderTypeBuffer.immediate(Tessellator.getInstance().getBuilder());
				font.drawInBatch(s, (float) (x + 19 - 2 - font.width(s)), (float) (y + 6 + 3), 16777215, true, matrixstack.last().pose(), irendertypebuffer$impl, false, 0, 15728880);
				irendertypebuffer$impl.endBatch();
			}
		}
	}

	private void renderItem(ItemRenderer rdn, ItemStack stack, int x, int y) {
		rdn.renderGuiItem(stack, x, y);
		if (stack.isDamageableItem()) {
			int dmg = stack.getDamageValue();
			int max = stack.getMaxDamage();
			String msg = "" + (max - dmg);
			FontRenderer font = Minecraft.getInstance().font;
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

	public boolean isSlimeChunk(World world, BlockPos pos) {
		return hasSeed && isSlimeChunk(seed, pos) || Biomes.SWAMP.equals(world.getBiomeName(pos).orElse(null)) && pos.getY() > 50 && pos.getY() < 70;
	}

}
