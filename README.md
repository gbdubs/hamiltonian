# Hamiltonian

Solvers for Hamiltonian Circuits in small graphs. Two supported algorithms:

* BFS - Not you'r grandparent's BFS: does some tricksy things with bitstrings to maintain small-memory sets, makes heavy use of bitwise operations, and does a double-sided search to reduce memory overhead. Fast for it's intended use case (V < 64), so no knights tour).
* DFS - A standard DFS, using the stack rather than any in memory data structure. Very memory light, but function-call heavy.

Given their tested runtime properties, the BFS is recommended for graphs with average degree <= 3.5, and DFS should be used elsewhere.

## Usage

```
import com.gradybward.hamiltonian.HamiltonianCycle;
...
interface Foo {
  public boolean isAdjacentTo(Foo other);
}
...
Optional<List<Foo>> getMeAllTheNodesInOrderISay(List<Foo> elements) {
  return HamiltonianCycleSolver.BFS().findHamiltonianCycle(elements, (elem1, elem2) -> elem1.isAdjacentTo(elem1));
}
```

## Notable Caveats to Use

* There is a pesky int to byte conversion faiulre happening for large graphs. I don't need to solve it today, and I want to go for a run instead. Feel free to find and fix it, and I'll send you a poem about your greatness.
* This does a pairwise lookup for the adjacency matrix calculation. Don't use an expensive function in there - it might be called O(V^2) times - I avoid doing this in a huristic (to suggest BFS/DFS) to avoid the overhead if your comparison fn is expensive. 
* Contributions welcome.

