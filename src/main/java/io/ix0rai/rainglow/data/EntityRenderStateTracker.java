package io.ix0rai.rainglow.data;

import net.minecraft.entity.Entity;

import java.util.UUID;

/**
 * @author A5ho9999
 * Interface to track entity data in render states
 */
public interface EntityRenderStateTracker {
    /**
     * Set the associated entity
     * @param entity The entity to associate with this render state
     */
    void rainglow$setEntity(Entity entity);

    /**
     * Get the associated entity UUID
     * @return The UUID of the associated entity, or null if not set
     */
    UUID rainglow$getEntityUuid();
}