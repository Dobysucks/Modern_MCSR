package dev.fsg262.atum;

import dev.fsg262.search.AcceptedSeed;

/**
 * Integration boundary for Atum. The client-side adapter must connect this
 * contract to Atum's actual reset event; this interface is not cosmetic.
 */
public interface AtumResetBridge {
    void injectAcceptedSeed(AcceptedSeed acceptedSeed);
}