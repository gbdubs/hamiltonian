package com.gradybward.hamiltonian;

import java.util.List;
import java.util.Optional;
import java.util.function.BiPredicate;

/**
 * An algorithm to find a hamiltonian cycle within a graph, if such a cycle exists.
 * 
 * <p>
 * A Hamiltonian Cycle is a graph path that starts and ends at the same element, and visits every
 * node on the graph exactly once. Determining if a Hamiltonian Cycle exists is an NP-Complete
 * problem, but one that is tractable at small N.
 * 
 * <p>
 * Backing algorithms of this interface must be at most 63 elements large.
 */
public interface HamiltonianCycleSolver {

  <T> Optional<List<T>> findHamiltonianCycle(List<T> elements, BiPredicate<T, T> adjacencyFn);

  public static HamiltonianCycleSolver DFS() {
    return new HamiltonianCycleDFS();
  }

  public static HamiltonianCycleSolver BFS() {
    return new HamiltonianCycleBFS();
  }
}
