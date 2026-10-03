package dev.fsg262.world;

public interface McsrWorldDataAccess {
    boolean fsg262$isMcsrWorld();

    void fsg262$setMcsrWorldCreated(boolean isMcsrWorld);

    long fsg262$mcsrOverworldSeed();

    long fsg262$mcsrNetherSeed();

    boolean fsg262$disablePiglinBrutes();

    boolean fsg262$standardizedRng();

    void fsg262$setMcsrBarterIndex(int index);

    int fsg262$nextMcsrBarterIndex();

    void fsg262$setMcsrWorldSeeds(long overworldSeed, long netherSeed,
                                 boolean disablePiglinBrutes, boolean standardizedRng);
}
