package com.gradybward.hamiltonian;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

/**
 * Uses a basic DFS to solve the Hamiltonian cycle problem.
 * 
 * <p>
 * Uses the program execution stack as the queue, with a maximum stack size of O(N).
 */
final class HamiltonianCycleDFS implements HamiltonianCycleSolver {

  @Override
  public <T> Optional<List<T>> findHamiltonianCycle(List<T> elements,
      BiPredicate<T, T> adjacencyFn) {
    Map<Byte, T> translation = new HashMap<>();
    Map<Byte, List<Byte>> adjacent = new HashMap<>();
    for (int i = 0; i < elements.size(); i++) {
      translation.put((byte) i, elements.get(i));
      List<Byte> adj = new ArrayList<>();
      for (int j = 0; j < elements.size(); j++) {
        if (adjacencyFn.test(elements.get(i), elements.get(j))) {
          adj.add((byte) j);
        }
      }
      adjacent.put((byte) i, adj);
    }
    // We only need traverse from the 0th node, since all Hamiltonian cycles will include it!
    Optional<List<Byte>> result = new Solver(adjacent, (byte) 0).getHamiltonianCycle();
    return result.map(r -> r.stream().map(translation::get).collect(Collectors.toList()));
  }

  private static final class Solver {
    private final Map<Byte, List<Byte>> adjacent;
    private final int n;
    private final boolean[] seen;
    private final List<Byte> inOrder;

    private Solver(Map<Byte, List<Byte>> adjacent, Byte initial) {
      this.adjacent = adjacent;
      n = adjacent.size();
      seen = new boolean[n];
      inOrder = new ArrayList<>();
      seen[initial] = true;
      inOrder.add(initial);
    }

    private Optional<List<Byte>> getHamiltonianCycle() {
      if (inOrder.size() == n) {
        if (adjacent.get(inOrder.get(0)).contains(inOrder.get(n - 1))) {
          return Optional.of(inOrder);
        }
        return Optional.empty();
      }
      for (byte adj : adjacent.get(inOrder.get(inOrder.size() - 1))) {
        if (seen[adj]) {
          continue;
        }
        seen[adj] = true;
        inOrder.add(adj);
        Optional<List<Byte>> traversal = getHamiltonianCycle();
        if (traversal.isPresent()) {
          return traversal;
        }
        inOrder.remove(inOrder.size() - 1);
        seen[adj] = false;
      }
      return Optional.empty();
    }
  }
}
