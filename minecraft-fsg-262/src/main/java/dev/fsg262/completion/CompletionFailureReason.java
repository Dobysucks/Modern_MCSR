package dev.fsg262.completion;

/**
 * Explicit failure reasons for the completion validator. Each reason maps to
 * a specific point in the intended speedrun progression.
 */
public enum CompletionFailureReason {
    NO_OVERWORLD_RESOURCE_ROUTE,
    NO_LAVA_ACCESS,
    NO_PORTAL_ROUTE,
    NO_BASTION,
    BAD_BASTION_LOOT,
    NO_FORTRESS,
    BAD_FORTRESS_ROUTE,
    INSUFFICIENT_BLAZE_PROGRESSION,
    INSUFFICIENT_PEARL_PROGRESSION,
    STRONGHOLD_NOT_VERIFIED,
    END_NOT_VERIFIED,
    RNG_NOT_VERIFIED,
    NO_FOOD_ROUTE,
    NO_IGNITION_ROUTE
}
