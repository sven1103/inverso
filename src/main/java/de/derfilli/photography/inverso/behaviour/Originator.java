package de.derfilli.photography.inverso.behaviour;

/**
 * <b><interface short description - 1 Line!></b>
 *
 * <p><More detailed description - When to use, what it solves, etc.></p>
 *
 * @since <version tag>
 */
public interface Originator<M extends Memento> {

  M createMemento();

  void restoreMemento(M memento);
}
