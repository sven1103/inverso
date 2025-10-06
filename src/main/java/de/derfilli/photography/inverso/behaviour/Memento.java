package de.derfilli.photography.inverso.behaviour;

import java.time.Instant;

/**
 * <b><interface short description - 1 Line!></b>
 *
 * <p><More detailed description - When to use, what it solves, etc.></p>
 *
 * @since <version tag>
 */
public interface Memento {

  Instant snapshotTime();

  String originatorName();
}
