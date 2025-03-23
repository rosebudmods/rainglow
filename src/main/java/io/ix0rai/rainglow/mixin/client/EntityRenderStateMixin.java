package io.ix0rai.rainglow.mixin.client;

import io.ix0rai.rainglow.data.EntityRenderStateTracker;
import net.minecraft.class_10017;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.UUID;

@Mixin(class_10017.class)
public class EntityRenderStateMixin implements EntityRenderStateTracker {
    @Unique
    private UUID entityUuid;

    @Override
    public void rainglow$setEntity(Entity entity) {
        if (entity != null) {
            this.entityUuid = entity.getUuid();
        }
    }

    // TODO: This could be used to just get the Entity as well as the UUID but just saving the UUID is better for long term memory usage

    @Override
    public UUID rainglow$getEntityUuid() {
        return this.entityUuid;
    }
}