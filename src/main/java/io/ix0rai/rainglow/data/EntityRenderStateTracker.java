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

    /**
     * Set the Rainbow Rendering for the associated entity
     * @param isRainbow boolean value to toggle the rainbow rendering
     */
    void rainglow$setRainbow(boolean isRainbow);

    /**
     * Get the Rainbow Rendering for the associated entity
     * @return boolean for entity should have the rainbow rendering
     */
    boolean rainglow$isRainbow();
}