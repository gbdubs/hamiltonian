package com.gradybward.hamiltonian;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/** Tests basic correctness of the Hamiltonian Algorithms implemented in this package. */
@RunWith(Parameterized.class)
public class HamiltonianCycleTest {

  private final HamiltonianCycleSolver solver;
  private final String graphName;
  private final int[][] adjacencyList;
  private final boolean cycleExists;

  public HamiltonianCycleTest(HamiltonianCycleSolver solver, String graphName,
      int[][] adjacencyList, boolean cycleExists) {
    this.solver = solver;
    this.graphName = graphName;
    this.adjacencyList = adjacencyList;
    this.cycleExists = cycleExists;
  }

  @Test
  public void runTest() {
    assertTrue(
        String.format("%s %s\n%s", solver.getClass(), graphName,
            Arrays.deepToString(adjacencyList)),
        solver.findHamiltonianCycle(elements(adjacencyList), (a, b) -> {
          for (int c : adjacencyList[a]) {
            if (c == b) return true;
          }
          return false;
        }).isPresent() == cycleExists);
  }

  private List<Integer> elements(int[][] a) {
    List<Integer> result = new ArrayList<>();
    for (int i = 0; i < a.length; i++) {
      result.add(i);
    }
    return result;
  }

  private static int[][] semiConnectedGraph = new int[][] { /* 0 */ new int[] { 1, 2 },
      /* 1 */ new int[] { 0, 2 }, /* 2 */ new int[] { 0, 1 }, /* 3 */ new int[] {} };

  private static int[][] bidirectionalLoop = new int[][] { /* 0 */ new int[] { 3, 1 },
      /* 1 */ new int[] { 0, 2 }, /* 2 */ new int[] { 1, 3 }, /* 3 */ new int[] { 2, 0 } };

  private static int[][] distinctConnectedComponents = new int[][] { /* 0 */ new int[] { 1 },
      /* 1 */ new int[] { 0 }, /* 2 */ new int[] { 3 }, /* 3 */ new int[] { 2 } };

  private static int[][] notebookExamplePage55 = new int[][] { /* 0 */ new int[] { 1, 5, 6 },
      /* 1 */ new int[] { 0, 2, 5 }, /* 2 */ new int[] { 1, 3, 4 }, /* 3 */ new int[] { 2, 4, 5 },
      /* 4 */ new int[] { 2, 3, 7 }, /* 5 */ new int[] { 0, 3, 6 }, /* 6 */ new int[] { 0, 5, 7 },
      /* 7 */ new int[] { 4, 6 } };

  private static int[][] bowtie = new int[][] { /* 0 */ new int[] { 1, 2, 3, 4 },
      /* 1 */ new int[] { 0, 1 }, /* 2 */ new int[] { 0, 2 }, /* 3 */ new int[] { 0, 4 },
      /* 4 */ new int[] { 0, 3 } };

  // https://math.stackexchange.com/questions/2485496
  private static int[][] stackExchangeGraph = new int[][] { /* 0 */ new int[] { 1, 4 },
      /* 1 */ new int[] { 0, 2, 3, 5, 6 }, /* 2 */ new int[] { 1, 4, 5, 6 },
      /* 3 */ new int[] { 1, 4 }, /* 4 */ new int[] { 0, 2, 3, 5 }, /* 5 */ new int[] { 2, 4, 6 },
      /* 6 */ new int[] { 1, 2, 5 } };

  private static int[][] petersonsGraph = new int[][] { /* 0 */ new int[] { 4, 1, 5 },
      /* 1 */ new int[] { 0, 2, 6 }, /* 2 */ new int[] { 1, 3, 7 }, /* 3 */ new int[] { 2, 4, 8 },
      /* 4 */ new int[] { 3, 0, 9 }, /* 5 */ new int[] { 0, 7, 8 }, /* 6 */ new int[] { 1, 8, 9 },
      /* 7 */ new int[] { 2, 9, 5 }, /* 8 */ new int[] { 3, 5, 6 }, /* 9 */ new int[] { 4, 6, 7 } };

  private static int[][] notPetersonsGraph = new int[][] { /* 0 */ new int[] { 4, 1, 5, 6 },
      /* 1 */ new int[] { 0, 2, 6 }, /* 2 */ new int[] { 1, 3, 7 }, /* 3 */ new int[] { 2, 4, 8 },
      /* 4 */ new int[] { 3, 0, 9 }, /* 5 */ new int[] { 0, 7, 8 },
      /* 6 */ new int[] { 1, 8, 9, 0 }, /* 7 */ new int[] { 2, 9, 5 },
      /* 8 */ new int[] { 3, 5, 6 }, /* 9 */ new int[] { 4, 6, 7 } };

  private static int[][] createLoopOfSize(int s) {
    int[][] r = new int[s][2];
    for (int i = 0; i < s; i++) {
      r[i][0] = (i - 1 + s) % s;
      r[i][1] = (i + 1 + s) % s;
    }
    return r;
  }

  private static int[][] createPerfectlyConnectedGraph(int s) {
    int[][] r = new int[s][s - 1];
    for (int i = 0; i < s; i++) {
      for (int j = 0; j < s - 1; j++) {
        r[i][j] = j >= i ? j + 1 : j;
      }
    }
    return r;
  }

  @Parameterized.Parameters
  public static List<Object[]> testCases() {
    List<Object[]> expectations = new ArrayList<>();

    expectations.add(new Object[] { "SemiConnected", semiConnectedGraph, false });
    expectations.add(new Object[] { "Loop4", bidirectionalLoop, true });
    expectations.add(new Object[] { "Disconnected", distinctConnectedComponents, false });
    expectations.add(new Object[] { "NotebookPage55", notebookExamplePage55, true });
    expectations.add(new Object[] { "Bowtie", bowtie, false });
    expectations.add(new Object[] { "StackExchangeGraph", stackExchangeGraph, false });
    expectations.add(new Object[] { "PetersonsGraph", petersonsGraph, false });
    expectations.add(new Object[] { "NotPetersonsGraph", notPetersonsGraph, true });
    expectations.add(new Object[] { "12Loop", createLoopOfSize(12), true });
    expectations.add(new Object[] { "63Loop", createLoopOfSize(63), true });
    expectations.add(new Object[] { "Perfect10", createPerfectlyConnectedGraph(10), true });

    List<HamiltonianCycleSolver> solvers = new ArrayList<>();
    solvers.add(new HamiltonianCycleBFS());
    solvers.add(new HamiltonianCycleDFS());

    List<Object[]> result = new ArrayList<>();
    for (Object[] o : expectations) {
      for (Object s : solvers) {
        result.add(new Object[] { s, o[0], o[1], o[2] });
      }
    }
    return result;
  }
}
