package ironfurnaces.gui;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import ironfurnaces.IronFurnaces;
import ironfurnaces.IronFurnacesClient;
import ironfurnaces.container.BlockIronFurnaceScreenHandlerBase;
import ironfurnaces.init.Reference;
import ironfurnaces.tileentity.BlockIronFurnaceTileBase;
import ironfurnaces.util.StringHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.Optional;

public abstract class BlockIronFurnaceScreenBase<T extends BlockIronFurnaceScreenHandlerBase> extends HandledScreen<T> {

    public static Identifier GUI;
    public static final Identifier WIDGETS = new Identifier(Reference.MOD_ID + ":" + "textures/gui/widgets.png");
    PlayerInventory playerInv;
    Text name;
    /** The X size of the inventory window in pixels. */
    protected int xSize = 176;
    /** The Y size of the inventory window in pixels. */
    protected int ySize = 166;

    public boolean add_button;
    public boolean sub_button;

    public BlockIronFurnaceScreenBase(T handler, PlayerInventory inv, Text name, Identifier gui) {
        super(handler, inv, name);
        playerInv = inv;
        this.name = name;
        this.GUI = gui;
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        super.render(matrices, mouseX, mouseY, delta);
        this.drawMouseoverTooltip(matrices, mouseX, mouseY);
    }


    @Override
    protected void drawForeground(MatrixStack matrices, int mouseX, int mouseY) {
        //drawString(Minecraft.getInstance().fontRenderer, "Energy: " + container.getEnergy(), 10, 10, 0xffffff);
        this.client.textRenderer.draw(matrices, this.playerInv.getDisplayName(), 7, this.ySize - 93, 4210752);
        this.client.textRenderer.draw(matrices, name, 7 + this.xSize / 2 - this.client.textRenderer.getWidth(name) / 2, 6, 4210752);

        if (showInventoryButtons(handler).get() && getRedstoneMode(handler).get() == 4) {
            int comSub = getComSub(handler).get();
            int i = comSub > 9 ? 28 : 31;
            this.client.textRenderer.draw(matrices, comSub + "", i - 42, 90, 4210752);
        }
        int actualMouseX = mouseX - ((this.width - this.xSize) / 2);
        int actualMouseY = mouseY - ((this.height - this.ySize) / 2);

        this.addTooltips(matrices, actualMouseX, actualMouseY);

    }

    private static Optional<Boolean> getAutoInput(ScreenHandler handler) {
        if (handler instanceof BlockIronFurnaceScreenHandlerBase) {
            BlockPos pos = ((BlockIronFurnaceScreenHandlerBase) handler).getPos();
            World world = ((BlockIronFurnaceScreenHandlerBase) handler).getWorld();
            return pos != null ? Optional.of(((BlockIronFurnaceTileBase) world.getBlockEntity(pos)).getAutoInput() == 1) : Optional.empty();
        } else {
            return Optional.empty();
        }
    }

    private static Optional<Boolean> getAutoOutput(ScreenHandler handler) {
        if (handler instanceof BlockIronFurnaceScreenHandlerBase) {
            BlockPos pos = ((BlockIronFurnaceScreenHandlerBase) handler).getPos();
            World world = ((BlockIronFurnaceScreenHandlerBase) handler).getWorld();
            return pos != null ? Optional.of(((BlockIronFurnaceTileBase) world.getBlockEntity(pos)).getAutoOutput() == 1) : Optional.empty();
        } else {
            return Optional.empty();
        }
    }

    private static Optional<Boolean> showInventoryButtons(ScreenHandler handler) {
        if (handler instanceof BlockIronFurnaceScreenHandlerBase) {
            BlockPos pos = ((BlockIronFurnaceScreenHandlerBase) handler).getPos();
            World world = ((BlockIronFurnaceScreenHandlerBase) handler).getWorld();
            return pos != null ? Optional.of(((BlockIronFurnaceTileBase) world.getBlockEntity(pos)).getShowButtons() == 1) : Optional.empty();
        } else {
            return Optional.empty();
        }
    }

    private static Optional<Integer> getRedstoneMode(ScreenHandler handler) {
        if (handler instanceof BlockIronFurnaceScreenHandlerBase) {
            BlockPos pos = ((BlockIronFurnaceScreenHandlerBase) handler).getPos();
            World world = ((BlockIronFurnaceScreenHandlerBase) handler).getWorld();
            return pos != null ? Optional.of(((BlockIronFurnaceTileBase) world.getBlockEntity(pos)).getRedstoneSetting()) : Optional.empty();
        } else {
            return Optional.empty();
        }
    }

    private static Optional<Integer> getComSub(ScreenHandler handler) {
        if (handler instanceof BlockIronFurnaceScreenHandlerBase) {
            BlockPos pos = ((BlockIronFurnaceScreenHandlerBase) handler).getPos();
            World world = ((BlockIronFurnaceScreenHandlerBase) handler).getWorld();
            return pos != null ? Optional.of(((BlockIronFurnaceTileBase) world.getBlockEntity(pos)).getRedstoneComSub()) : Optional.empty();
        } else {
            return Optional.empty();
        }
    }

    public static Text getTooltip(BlockIronFurnaceTileBase te, int index)
    {
        switch (te.furnaceSettings.get(index))
        {
            case 1:
                return new TranslatableText("tooltip." + Reference.MOD_ID + ".gui_input");
            case 2:
                return new TranslatableText("tooltip." + Reference.MOD_ID + ".gui_output");
            case 3:
                return new TranslatableText("tooltip." + Reference.MOD_ID + ".gui_input_output");
            case 4:
                return new TranslatableText("tooltip." + Reference.MOD_ID + ".gui_fuel");
            default:
                return new TranslatableText("tooltip." + Reference.MOD_ID + ".gui_none");
        }
    }

    private static Optional<Text> getTooltip(ScreenHandler handler, int index) {
        if (handler instanceof BlockIronFurnaceScreenHandlerBase) {
            BlockPos pos = ((BlockIronFurnaceScreenHandlerBase) handler).getPos();
            World world = ((BlockIronFurnaceScreenHandlerBase) handler).getWorld();
            return pos != null ? Optional.of(getTooltip((BlockIronFurnaceTileBase) world.getBlockEntity(pos), index)) : Optional.empty();
        } else {
            return Optional.empty();
        }
    }

    private static Optional<Integer> getSettingTop(ScreenHandler handler) {
        if (handler instanceof BlockIronFurnaceScreenHandlerBase) {
            BlockPos pos = ((BlockIronFurnaceScreenHandlerBase) handler).getPos();
            World world = ((BlockIronFurnaceScreenHandlerBase) handler).getWorld();
            return pos != null ? Optional.of(((BlockIronFurnaceTileBase) world.getBlockEntity(pos)).getSettingTop()) : Optional.empty();
        } else {
            return Optional.empty();
        }
    }

    private static Optional<Integer> getSettingBottom(ScreenHandler handler) {
        if (handler instanceof BlockIronFurnaceScreenHandlerBase) {
            BlockPos pos = ((BlockIronFurnaceScreenHandlerBase) handler).getPos();
            World world = ((BlockIronFurnaceScreenHandlerBase) handler).getWorld();
            return pos != null ? Optional.of(((BlockIronFurnaceTileBase) world.getBlockEntity(pos)).getSettingBottom()) : Optional.empty();
        } else {
            return Optional.empty();
        }
    }

    private static Optional<Integer> getSettingFront(ScreenHandler handler) {
        if (handler instanceof BlockIronFurnaceScreenHandlerBase) {
            BlockPos pos = ((BlockIronFurnaceScreenHandlerBase) handler).getPos();
            World world = ((BlockIronFurnaceScreenHandlerBase) handler).getWorld();
            return pos != null ? Optional.of(((BlockIronFurnaceTileBase) world.getBlockEntity(pos)).getSettingFront()) : Optional.empty();
        } else {
            return Optional.empty();
        }
    }

    private static Optional<Integer> getSettingBack(ScreenHandler handler) {
        if (handler instanceof BlockIronFurnaceScreenHandlerBase) {
            BlockPos pos = ((BlockIronFurnaceScreenHandlerBase) handler).getPos();
            World world = ((BlockIronFurnaceScreenHandlerBase) handler).getWorld();
            return pos != null ? Optional.of(((BlockIronFurnaceTileBase) world.getBlockEntity(pos)).getSettingBack()) : Optional.empty();
        } else {
            return Optional.empty();
        }
    }

    private static Optional<Integer> getSettingLeft(ScreenHandler handler) {
        if (handler instanceof BlockIronFurnaceScreenHandlerBase) {
            BlockPos pos = ((BlockIronFurnaceScreenHandlerBase) handler).getPos();
            World world = ((BlockIronFurnaceScreenHandlerBase) handler).getWorld();
            return pos != null ? Optional.of(((BlockIronFurnaceTileBase) world.getBlockEntity(pos)).getSettingLeft()) : Optional.empty();
        } else {
            return Optional.empty();
        }
    }

    private static Optional<Integer> getSettingRight(ScreenHandler handler) {
        if (handler instanceof BlockIronFurnaceScreenHandlerBase) {
            BlockPos pos = ((BlockIronFurnaceScreenHandlerBase) handler).getPos();
            World world = ((BlockIronFurnaceScreenHandlerBase) handler).getWorld();
            return pos != null ? Optional.of(((BlockIronFurnaceTileBase) world.getBlockEntity(pos)).getSettingRight()) : Optional.empty();
        } else {
            return Optional.empty();
        }
    }

    private static Optional<Integer> getIndexFront(ScreenHandler handler) {
        if (handler instanceof BlockIronFurnaceScreenHandlerBase) {
            BlockPos pos = ((BlockIronFurnaceScreenHandlerBase) handler).getPos();
            World world = ((BlockIronFurnaceScreenHandlerBase) handler).getWorld();
            return pos != null ? Optional.of(((BlockIronFurnaceTileBase) world.getBlockEntity(pos)).getIndexFront()) : Optional.empty();
        } else {
            return Optional.empty();
        }
    }

    private static Optional<Integer> getIndexBack(ScreenHandler handler) {
        if (handler instanceof BlockIronFurnaceScreenHandlerBase) {
            BlockPos pos = ((BlockIronFurnaceScreenHandlerBase) handler).getPos();
            World world = ((BlockIronFurnaceScreenHandlerBase) handler).getWorld();
            return pos != null ? Optional.of(((BlockIronFurnaceTileBase) world.getBlockEntity(pos)).getIndexBack()) : Optional.empty();
        } else {
            return Optional.empty();
        }
    }

    private static Optional<Integer> getIndexLeft(ScreenHandler handler) {
        if (handler instanceof BlockIronFurnaceScreenHandlerBase) {
            BlockPos pos = ((BlockIronFurnaceScreenHandlerBase) handler).getPos();
            World world = ((BlockIronFurnaceScreenHandlerBase) handler).getWorld();
            return pos != null ? Optional.of(((BlockIronFurnaceTileBase) world.getBlockEntity(pos)).getIndexLeft()) : Optional.empty();
        } else {
            return Optional.empty();
        }
    }

    private static Optional<Integer> getIndexRight(ScreenHandler handler) {
        if (handler instanceof BlockIronFurnaceScreenHandlerBase) {
            BlockPos pos = ((BlockIronFurnaceScreenHandlerBase) handler).getPos();
            World world = ((BlockIronFurnaceScreenHandlerBase) handler).getWorld();
            return pos != null ? Optional.of(((BlockIronFurnaceTileBase) world.getBlockEntity(pos)).getIndexRight()) : Optional.empty();
        } else {
            return Optional.empty();
        }
    }

    private static Optional<BlockPos> getBlockPos(ScreenHandler handler) {
        if (handler instanceof BlockIronFurnaceScreenHandlerBase) {
            BlockPos pos = ((BlockIronFurnaceScreenHandlerBase) handler).getPos();
            World world = ((BlockIronFurnaceScreenHandlerBase) handler).getWorld();
            return pos != null ? Optional.of(((BlockIronFurnaceTileBase) world.getBlockEntity(pos)).getPos()) : Optional.empty();
        } else {
            return Optional.empty();
        }
    }

    private void addTooltips(MatrixStack matrix, int mouseX, int mouseY) {

        if (!showInventoryButtons(handler).get()) {
            if (mouseX >= -20 && mouseX <= 0 && mouseY >= 4 && mouseY <= 26) {
                this.renderTooltip(matrix, new TranslatableText("tooltip." + Reference.MOD_ID + ".gui_open"), mouseX, mouseY);
            }
        } else {
            if (mouseX >= -13 && mouseX <= 0 && mouseY >= 4 && mouseY <= 26) {
                this.renderTooltip(matrix, StringHelper.getShiftInfoGui(), mouseX, mouseY);
            } else if (mouseX >= -47 && mouseX <= -34 && mouseY >= 12 && mouseY <= 25) {
                List<Text> list = Lists.newArrayList();
                list.add(new TranslatableText("tooltip." + Reference.MOD_ID + ".gui_auto_input"));
                list.add(new LiteralText("" + getAutoInput(handler).get()));
                this.renderTooltip(matrix, list, mouseX, mouseY);
            } else if (mouseX >= -29 && mouseX <= -16 && mouseY >= 12 && mouseY <= 25) {
                List<Text> list = Lists.newArrayList();
                list.add(new TranslatableText("tooltip." + Reference.MOD_ID + ".gui_auto_output"));
                list.add(new LiteralText("" + getAutoOutput(handler).get()));
                this.renderTooltip(matrix, list, mouseX, mouseY);
            } else if (mouseX >= -32 && mouseX <= -23 && mouseY >= 31 && mouseY <= 40) {
                List<Text> list = Lists.newArrayList();
                list.add(new TranslatableText("tooltip." + Reference.MOD_ID + ".gui_top"));
                list.add(getTooltip(handler, 1).get());
                this.renderTooltip(matrix, list, mouseX, mouseY);
            } else if (mouseX >= -32 && mouseX <= -23 && mouseY >= 55 && mouseY <= 64) {
                List<Text> list = Lists.newArrayList();
                list.add(new TranslatableText("tooltip." + Reference.MOD_ID + ".gui_bottom"));
                list.add(getTooltip(handler, 0).get());
                this.renderTooltip(matrix, list, mouseX, mouseY);
            } else if (mouseX >= -32 && mouseX <= -23 && mouseY >= 43 && mouseY <= 52) {
                List<Text> list = Lists.newArrayList();
                if (IronFurnacesClient.isShiftKeyDown()) {
                    list.add(new TranslatableText("tooltip." + Reference.MOD_ID + ".gui_reset"));
                } else {
                    list.add(new TranslatableText("tooltip." + Reference.MOD_ID + ".gui_front"));
                    list.add(getTooltip(handler, getIndexFront(handler).get()).get());
                }
                this.renderTooltip(matrix, list, mouseX, mouseY);
            } else if (mouseX >= -44 && mouseX <= -35 && mouseY >= 43 && mouseY <= 52) {
                List<Text> list = Lists.newArrayList();
                list.add(new TranslatableText("tooltip." + Reference.MOD_ID + ".gui_left"));
                list.add(getTooltip(handler, getIndexLeft(handler).get()).get());
                this.renderTooltip(matrix, list, mouseX, mouseY);
            } else if (mouseX >= -20 && mouseX <= -11 && mouseY >= 43 && mouseY <= 52) {
                List<Text> list = Lists.newArrayList();
                list.add(new TranslatableText("tooltip." + Reference.MOD_ID + ".gui_right"));
                list.add(getTooltip(handler, getIndexRight(handler).get()).get());
                this.renderTooltip(matrix, list, mouseX, mouseY);
            } else if (mouseX >= -20 && mouseX <= -11 && mouseY >= 55 && mouseY <= 64) {
                List<Text> list = Lists.newArrayList();
                list.add(new TranslatableText("tooltip." + Reference.MOD_ID + ".gui_back"));
                list.add(getTooltip(handler, getIndexBack(handler).get()).get());
                this.renderTooltip(matrix, list, mouseX, mouseY);
            } else if (mouseX >= -47 && mouseX <= -34 && mouseY >= 70 && mouseY <= 83) {
                List<Text> list = Lists.newArrayList();
                list.add(new TranslatableText("tooltip." + Reference.MOD_ID + ".gui_redstone_ignored"));
                this.renderTooltip(matrix, list, mouseX, mouseY);
            } else if (mouseX >= -31 && mouseX <= -18 && mouseY >= 70 && mouseY <= 83) {
                List<Text> list = Lists.newArrayList();
                if (IronFurnacesClient.isShiftKeyDown()) {
                    list.add(new TranslatableText("tooltip." + Reference.MOD_ID + ".gui_redstone_low"));
                } else {
                    list.add(new TranslatableText("tooltip." + Reference.MOD_ID + ".gui_redstone_high"));
                }
                this.renderTooltip(matrix, list, mouseX, mouseY);
            } else if (mouseX >= -15 && mouseX <= -2 && mouseY >= 70 && mouseY <= 83) {
                List<Text> list = Lists.newArrayList();
                list.add(new TranslatableText("tooltip." + Reference.MOD_ID + ".gui_redstone_comparator"));
                this.renderTooltip(matrix, list, mouseX, mouseY);
            } else if (mouseX >= -47 && mouseX <= -34 && mouseY >= 86 && mouseY <= 99) {
                List<Text> list = Lists.newArrayList();
                list.add(new TranslatableText("tooltip." + Reference.MOD_ID + ".gui_redstone_comparator_sub"));
                this.renderTooltip(matrix, list, mouseX, mouseY);
            }

        }
    }

    @Override
    protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, this.GUI);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        int i = (this.width - this.backgroundWidth) / 2;
        int j = (this.height - this.backgroundHeight) / 2;
        this.drawTexture(matrices, i, j, 0, 0, this.backgroundWidth, this.backgroundHeight);
        int k;
        if (((BlockIronFurnaceScreenHandlerBase)this.handler).isBurning()) {
            k = ((BlockIronFurnaceScreenHandlerBase)this.handler).getFuelProgress();
            this.drawTexture(matrices, i + 56, j + 36 + 12 - k, 176, 12 - k, 14, k + 1);
        }

        k = ((BlockIronFurnaceScreenHandlerBase)this.handler).getCookProgress();
        this.drawTexture(matrices, i + 79, j + 34, 176, 14, k + 1, 16);

        RenderSystem.setShaderTexture(0, this.WIDGETS);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        int actualMouseX = mouseX - ((this.width - this.xSize) / 2);
        int actualMouseY = mouseY - ((this.height - this.ySize) / 2);

        this.addInventoryButtons(matrices, ((BlockIronFurnaceScreenHandlerBase) this.handler), actualMouseX, actualMouseY);
        this.addRedstoneButtons(matrices, ((BlockIronFurnaceScreenHandlerBase) this.handler), actualMouseX, actualMouseY);

    }

    private void addRedstoneButtons(MatrixStack matrix, BlockIronFurnaceScreenHandlerBase handler, int mouseX, int mouseY) {
        int guiLeft = (this.width - this.backgroundWidth) / 2;
        int guiTop = (this.height - this.backgroundHeight) / 2;
        if (showInventoryButtons(handler).get()) {
            this.blitRedstone(matrix);
            if (getRedstoneMode(handler).get() == 4) {
                int comSub = getComSub(handler).get();
                boolean flag = IronFurnacesClient.isShiftKeyDown();
                if (flag) {
                    if (comSub > 0) {
                        this.sub_button = true;
                        if (mouseX >= -31 && mouseX <= -18 && mouseY >= 86 && mouseY <= 99) {
                            this.drawTexture(matrix, guiLeft - 31, guiTop + 86, 14, 0, 14, 14);
                        } else {
                            this.drawTexture(matrix, guiLeft - 31, guiTop + 86, 0, 0, 14, 14);
                        }
                    } else {
                        this.sub_button = false;
                        this.drawTexture(matrix, guiLeft - 31, guiTop + 86, 28, 0, 14, 14);
                    }

                } else {
                    if (comSub < 15) {
                        this.add_button = true;
                        if (mouseX >= -31 && mouseX <= -18 && mouseY >= 86 && mouseY <= 99) {
                            this.drawTexture(matrix, guiLeft - 31, guiTop + 86, 14, 14, 14, 14);
                        } else {
                            this.drawTexture(matrix, guiLeft - 31, guiTop + 86, 0, 14, 14, 14);
                        }
                    } else {
                        this.add_button = false;
                        this.drawTexture(matrix, guiLeft - 31, guiTop + 86, 28, 14, 14, 14);

                    }
                }
            }
        }
    }

    private void addInventoryButtons(MatrixStack matrix, BlockIronFurnaceScreenHandlerBase container, int mouseX, int mouseY) {
        int guiLeft = (this.width - this.backgroundWidth) / 2;
        int guiTop = (this.height - this.backgroundHeight) / 2;
        if (!showInventoryButtons(container).get()) {
            this.drawTexture(matrix, guiLeft - 20, guiTop + 4, 0, 28, 23, 26);
        } else if (showInventoryButtons(container).get()) {
            this.drawTexture(matrix, guiLeft - 56, guiTop + 4, 0, 54, 59, 107);
            if (mouseX >= -47 && mouseX <= -34 && mouseY >= 12 && mouseY <= 25 || getAutoInput(container).get()) {
                this.drawTexture(matrix, guiLeft - 47, guiTop + 12, 0, 189, 14, 14);
            }
            if (mouseX >= -29 && mouseX <= -16 && mouseY >= 12 && mouseY <= 25 || getAutoOutput(container).get()) {
                this.drawTexture(matrix, guiLeft - 29, guiTop + 12, 14, 189, 14, 14);
            }
            this.blitIO(matrix);
        }


    }

    private void blitRedstone(MatrixStack matrix) {
        int guiLeft = (this.width - this.backgroundWidth) / 2;
        int guiTop = (this.height - this.backgroundHeight) / 2;
        boolean flag = IronFurnacesClient.isShiftKeyDown();
        if (flag) {
            this.drawTexture(matrix, guiLeft - 31, guiTop + 70, 84, 189, 14, 14);
        }
        int setting = getRedstoneMode(handler).get();
        if (setting == 0) {
            this.drawTexture(matrix, guiLeft - 47, guiTop + 70, 28, 189, 14, 14);
        } else if (setting == 1 && !flag) {
            this.drawTexture(matrix, guiLeft - 31, guiTop + 70, 42, 189, 14, 14);
        } else if (setting == 2) {
            this.drawTexture(matrix, guiLeft - 31, guiTop + 70, 98, 189, 14, 14);
        } else if (setting == 3) {
            this.drawTexture(matrix, guiLeft - 15, guiTop + 70, 56, 189, 14, 14);
        } else if (setting == 4) {
            this.drawTexture(matrix, guiLeft - 47, guiTop + 86, 70, 189, 14, 14);
        }

    }

    private void blitIO(MatrixStack matrix) {
        int guiLeft = (this.width - this.backgroundWidth) / 2;
        int guiTop = (this.height - this.backgroundHeight) / 2;
        int[] settings = new int[]{0, 0, 0, 0, 0, 0};
        int setting = getSettingTop(handler).get();
        if (setting == 1) {
            this.drawTexture(matrix, guiLeft - 32, guiTop + 31, 0, 161, 10, 10);
        } else if (setting == 2) {
            this.drawTexture(matrix, guiLeft - 32, guiTop + 31, 10, 161, 10, 10);
        } else if (setting == 3) {
            this.drawTexture(matrix, guiLeft - 32, guiTop + 31, 20, 161, 10, 10);
        } else if (setting == 4) {
            this.drawTexture(matrix, guiLeft - 32, guiTop + 31, 30, 161, 10, 10);
        }
        settings[1] = setting;

        setting = getSettingBottom(handler).get();
        if (setting == 1) {
            this.drawTexture(matrix, guiLeft - 32, guiTop + 55, 0, 161, 10, 10);
        } else if (setting == 2) {
            this.drawTexture(matrix, guiLeft - 32, guiTop + 55, 10, 161, 10, 10);
        } else if (setting == 3) {
            this.drawTexture(matrix, guiLeft - 32, guiTop + 55, 20, 161, 10, 10);
        } else if (setting == 4) {
            this.drawTexture(matrix, guiLeft - 32, guiTop + 55, 30, 161, 10, 10);
        }
        settings[0] = setting;
        setting = getSettingFront(handler).get();
        if (setting == 1) {
            this.drawTexture(matrix, guiLeft - 32, guiTop + 43, 0, 161, 10, 10);
        } else if (setting == 2) {
            this.drawTexture(matrix, guiLeft - 32, guiTop + 43, 10, 161, 10, 10);
        } else if (setting == 3) {
            this.drawTexture(matrix, guiLeft - 32, guiTop + 43, 20, 161, 10, 10);
        } else if (setting == 4) {
            this.drawTexture(matrix, guiLeft - 32, guiTop + 43, 30, 161, 10, 10);
        }
        settings[getIndexFront(handler).get()] = setting;
        setting = getSettingBack(handler).get();
        if (setting == 1) {
            this.drawTexture(matrix, guiLeft - 20, guiTop + 55, 0, 161, 10, 10);
        } else if (setting == 2) {
            this.drawTexture(matrix, guiLeft - 20, guiTop + 55, 10, 161, 10, 10);
        } else if (setting == 3) {
            this.drawTexture(matrix, guiLeft - 20, guiTop + 55, 20, 161, 10, 10);
        } else if (setting == 4) {
            this.drawTexture(matrix, guiLeft - 20, guiTop + 55, 30, 161, 10, 10);
        }
        settings[getIndexBack(handler).get()] = setting;
        setting = getSettingLeft(handler).get();
        if (setting == 1) {
            this.drawTexture(matrix, guiLeft - 44, guiTop + 43, 0, 161, 10, 10);
        } else if (setting == 2) {
            this.drawTexture(matrix, guiLeft - 44, guiTop + 43, 10, 161, 10, 10);
        } else if (setting == 3) {
            this.drawTexture(matrix, guiLeft - 44, guiTop + 43, 20, 161, 10, 10);
        } else if (setting == 4) {
            this.drawTexture(matrix, guiLeft - 44, guiTop + 43, 30, 161, 10, 10);
        }
        settings[getIndexLeft(handler).get()] = setting;
        setting = getSettingRight(handler).get();
        if (setting == 1) {
            this.drawTexture(matrix, guiLeft - 20, guiTop + 43, 0, 161, 10, 10);
        } else if (setting == 2) {
            this.drawTexture(matrix, guiLeft - 20, guiTop + 43, 10, 161, 10, 10);
        } else if (setting == 3) {
            this.drawTexture(matrix, guiLeft - 20, guiTop + 43, 20, 161, 10, 10);
        } else if (setting == 4) {
            this.drawTexture(matrix, guiLeft - 20, guiTop + 43, 30, 161, 10, 10);
        }
        settings[getIndexRight(handler).get()] = setting;
        boolean input = false;
        boolean output = false;
        boolean both = false;
        boolean fuel = false;
        for (int set : settings) {
            if (set == 1) {
                input = true;
            } else if (set == 2) {
                output = true;
            } else if (set == 3) {
                both = true;
            } else if (set == 4) {
                fuel = true;
            }
        }
        if (input || both) {
            this.drawTexture(matrix, guiLeft + 55, guiTop + 16, 0, 171, 18, 18);
        }
        if (output || both) {
            this.drawTexture(matrix, guiLeft + 111, guiTop + 30, 0, 203, 26, 26);
        }
        if (fuel) {
            this.drawTexture(matrix, guiLeft + 55, guiTop + 52, 18, 171, 18, 18);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        double actualMouseX = mouseX - ((this.width - this.xSize) / 2);
        double actualMouseY = mouseY - ((this.height - this.ySize) / 2);
        this.mouseClickedRedstoneButtons(actualMouseX, actualMouseY);
        this.mouseClickedInventoryButtons(button, this.handler, actualMouseX, actualMouseY);
        return super.mouseClicked(mouseX, mouseY, button);
    }

    public void sendServer(int index, int value)
    {
        PacketByteBuf buf = PacketByteBufs.create();
        BlockPos pos = getBlockPos(handler).get();
        buf.writeInt(pos.getX());
        buf.writeInt(pos.getY());
        buf.writeInt(pos.getZ());
        buf.writeInt(index);
        buf.writeInt(value);

        ClientPlayNetworking.send(IronFurnaces.furnace_packet, buf);
    }

    public void mouseClickedInventoryButtons(int button, BlockIronFurnaceScreenHandlerBase container, double mouseX, double mouseY) {
        boolean flag = button == GLFW.GLFW_MOUSE_BUTTON_2;
        if (!showInventoryButtons(container).get()) {
            if (mouseX >= -20 && mouseX <= 0 && mouseY >= 4 && mouseY <= 26) {
                sendServer(10, 1);
            }
        } else {
            if (mouseX >= -13 && mouseX <= 0 && mouseY >= 4 && mouseY <= 26) {
                sendServer(10, 0);
            } else if (mouseX >= -47 && mouseX <= -34 && mouseY >= 12 && mouseY <= 25) {
                if (!getAutoInput(container).get()) {
                    sendServer(6, 1);
                    MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 0.6F, 0.3F));
                } else {
                    sendServer(6, 0);
                    MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 0.6F, 0.3F));
                }

            } else if (mouseX >= -29 && mouseX <= -16 && mouseY >= 12 && mouseY <= 25) {
                if (!getAutoOutput(container).get()) {
                    sendServer(7, 1);
                    MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 0.6F, 0.3F));
                } else {
                    sendServer(7, 0);
                    MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 0.6F, 0.3F));
                }
            } else if (mouseX >= -32 && mouseX <= -23 && mouseY >= 31 && mouseY <= 40) {
                if (flag) {
                    sendToServerInverted(getSettingTop(container).get(), 1);
                } else {
                    sendToServer(getSettingTop(container).get(), 1);
                }
            } else if (mouseX >= -32 && mouseX <= -23 && mouseY >= 55 && mouseY <= 64) {
                if (flag) {
                    sendToServerInverted(getSettingBottom(container).get(), 0);
                } else {
                    sendToServer(getSettingBottom(container).get(), 0);
                }
            } else if (mouseX >= -32 && mouseX <= -23 && mouseY >= 43 && mouseY <= 52) {
                if (IronFurnacesClient.isShiftKeyDown()) {
                    sendServer(0, 0);
                    sendServer(1, 0);
                    sendServer(2, 0);
                    sendServer(3, 0);
                    sendServer(4, 0);
                    sendServer(5, 0);
                    MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 0.8F, 0.3F));
                } else {
                    if (flag) {
                        sendToServerInverted(getSettingFront(container).get(), getIndexFront(container).get());
                    } else {
                        sendToServer(getSettingFront(container).get(), getIndexFront(container).get());
                    }
                }
            } else if (mouseX >= -20 && mouseX <= -11 && mouseY >= 55 && mouseY <= 64) {
                if (flag) {
                    sendToServerInverted(getSettingBack(container).get(), getIndexBack(container).get());
                } else {
                    sendToServer(getSettingBack(container).get(), getIndexBack(container).get());
                }
            } else if (mouseX >= -44 && mouseX <= -35 && mouseY >= 43 && mouseY <= 52) {
                if (flag) {
                    sendToServerInverted(getSettingLeft(container).get(), getIndexLeft(container).get());
                } else {
                    sendToServer(getSettingLeft(container).get(), getIndexLeft(container).get());
                }
            } else if (mouseX >= -20 && mouseX <= -11 && mouseY >= 43 && mouseY <= 52) {
                if (flag) {
                    sendToServerInverted(getSettingRight(container).get(), getIndexRight(container).get());
                } else {
                    sendToServer(getSettingRight(container).get(), getIndexRight(container).get());
                }
            }
        }
    }

    private void sendToServer(int setting, int index) {
        MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 0.6F, 0.3F));
        if (setting <= 0) {
            sendServer(index, 1);
        } else if (setting == 1) {
            sendServer(index, 2);
        } else if (setting == 2) {
            sendServer(index, 3);
        } else if (setting == 3) {
            sendServer(index, 4);
        } else if (setting >= 4) {
            sendServer(index, 0);
        }
    }

    private void sendToServerInverted(int setting, int index) {
        MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 0.3F, 0.3F));
        if (setting <= 0) {
            sendServer(index, 4);
        } else if (setting == 1) {
            sendServer(index, 0);
        } else if (setting == 2) {
            sendServer(index, 1);
        } else if (setting == 3) {
            sendServer(index, 2);
        } else if (setting >= 4) {
            sendServer(index, 3);
        }
    }

    public void mouseClickedRedstoneButtons(double mouseX, double mouseY) {
        if (mouseX >= -31 && mouseX <= -18 && mouseY >= 86 && mouseY <= 99) {
            if (this.sub_button && IronFurnacesClient.isShiftKeyDown()) {
                sendServer(9, getComSub(handler).get() - 1);
                MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 0.3F, 0.3F));
            }
        }
        if (mouseX >= -31 && mouseX <= -18 && mouseY >= 86 && mouseY <= 99) {
            if (this.add_button && !IronFurnacesClient.isShiftKeyDown()) {
                sendServer(9, getComSub(handler).get() + 1);
                MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 0.6F, 0.3F));
            }
        }
        if (mouseX >= -47 && mouseX <= -34 && mouseY >= 70 && mouseY <= 83) {
            if (getRedstoneMode(handler).get() != 0) {
                sendServer(8, 0);
                MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 0.6F, 0.3F));
            }
        }

        if (mouseX >= -31 && mouseX <= -18 && mouseY >= 70 && mouseY <= 83) {
            if (getRedstoneMode(handler).get() != 1 && !IronFurnacesClient.isShiftKeyDown()) {
                sendServer(8, 1);
                MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 0.6F, 0.3F));
            }
            if (getRedstoneMode(handler).get() != 2 && IronFurnacesClient.isShiftKeyDown()) {
                sendServer(8, 2);
                MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 0.6F, 0.3F));
            }
        }

        if (mouseX >= -15 && mouseX <= -2 && mouseY >= 70 && mouseY <= 83) {
            if (getRedstoneMode(handler).get() != 3) {
                sendServer(8, 3);
                MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 0.6F, 0.3F));
            }
        }

        if (mouseX >= -47 && mouseX <= -34 && mouseY >= 86 && mouseY <= 99) {
            if (getRedstoneMode(handler).get() != 4) {
                sendServer(8, 4);
                MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 0.6F, 0.3F));
            }
        }
    }

}
