package dev.fsg262.mixin;

public final class McsrPiglinBrutePolicy {
    private McsrPiglinBrutePolicy() {}

    public static boolean shouldBlock(boolean mcsrWorld, boolean disablePiglinBrutes) {
        return mcsrWorld && disablePiglinBrutes;
    }
}
