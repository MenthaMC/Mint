/*
 * This file is licensed under the MIT license.
 * Origin : Leaves (https://github.com/LeavesMC/Leaves)
 */

package org.leavesmc.leaves.plugin;

import java.util.Set;

import org.jetbrains.annotations.NotNull;

public interface FeatureManager {
    @NotNull
    Set<String> getAvailableFeatures();

    boolean isFeatureAvailable(@NotNull String feature);
}
