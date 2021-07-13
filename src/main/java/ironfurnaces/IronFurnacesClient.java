package ironfurnaces;

import ironfurnaces.init.Reference;
import ironfurnaces.tileentity.BlockIronFurnaceTileBase;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.glfw.GLFW;

public class IronFurnacesClient implements ClientModInitializer {



    @Override
    public void onInitializeClient() {
        Reference.initClient();



    }

    public static boolean isShiftKeyDown() {
        return isKeyDown(GLFW.GLFW_KEY_LEFT_SHIFT) || isKeyDown(GLFW.GLFW_KEY_RIGHT_SHIFT);
    }
    public static boolean isKeyDown(int glfw) {
        InputUtil.Key key = InputUtil.Type.KEYSYM.createFromCode(glfw);
        int keyCode = key.getCode();
        if (keyCode != InputUtil.UNKNOWN_KEY.getCode()) {
            long windowHandle = MinecraftClient.getInstance().getWindow().getHandle();
            try {
                if (key.getCategory() == InputUtil.Type.KEYSYM) {
                    return InputUtil.isKeyPressed(windowHandle, keyCode);
                } /**else if (key.getType() == InputMappings.Type.MOUSE) {
                 return GLFW.glfwGetMouseButton(windowHandle, keyCode) == GLFW.GLFW_PRESS;
                 }**/
            } catch (Exception ignored) {
            }
        }
        return false;
    }



}
