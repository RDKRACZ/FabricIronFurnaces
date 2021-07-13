package ironfurnaces;

import ironfurnaces.init.Reference;
import ironfurnaces.tileentity.BlockIronFurnaceTileBase;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public class IronFurnaces implements ModInitializer {

    public static Identifier furnace_packet = new Identifier(Reference.MOD_ID, "furnace_settings");

    @Override
    public void onInitialize() {
        Reference.init();

        ServerPlayNetworking.registerGlobalReceiver(furnace_packet, (server, player, handler, buf, responseSender) -> {

            int x = buf.readInt();
            int y = buf.readInt();
            int z = buf.readInt();
            int index = buf.readInt();
            int set = buf.readInt();
            BlockPos pos = new BlockPos(x, y, z);
            server.execute(() -> {
                BlockEntity tee = player.world.getBlockEntity(pos);
                if (tee != null && tee instanceof BlockIronFurnaceTileBase) {
                    BlockIronFurnaceTileBase te = (BlockIronFurnaceTileBase) tee;

                    te.furnaceSettings.set(index, set);
                    te.getWorld().updateListeners(pos, te.getWorld().getBlockState(pos).getBlock().getDefaultState(), te.getWorld().getBlockState(pos), 2);

                }
            });
        });

    }


}
