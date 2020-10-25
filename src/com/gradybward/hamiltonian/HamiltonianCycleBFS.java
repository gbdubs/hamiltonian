package com.gradybward.hamiltonian;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiPredicate;

/**
 * Uses double-DFS to find Hamiltonian cycles in small (V <= 63) undirected graphs.
 * 
 * <p>
 * This solver uses a simple approach. Imagine a path as a "word", built up of sequential characters
 * representing the nodes of the path. Each word has a few properties:
 * 
 * <ul>
 * <li>StartAndEnd (SE): A start and end (the order of which doesn't really matter)
 * <li>Elements: A collection of characters/vertexes in the word
 * <li>Path: An ordering of the characters from the start to the end through each character
 * 
 * <p>
 * We build up a dictionary of valid words, storing and using these properties to create new words
 * from the words we know at the beginning. If N=NumberOfNodes, this solver builds up valid words up
 * to length CEIL(1 + N/2), and then looks for paths that share the same start and end points, but
 * have fully different elements in between.
 * 
 * <p>
 * This is an exponentially space intensive algorithm. In the worst case of a fully connected graph,
 * this could take O((N/2)!) bytes of memory! Yikes! To save (constant factors of) space, we use
 * bit-strings to represent sets (limiting this solver to sets with <= 63 elements), and we use
 * bytes to record directions (since there are always 63 or fewer elements), and only record one
 * path of the two paths that we could record in any situation.
 * 
 * <p>
 * This algorithm is at its best in situations where we have a relatively sparse graph (E < 4V).
 */
class HamiltonianCycleBFS implements HamiltonianCycleSolver {

  @Override
  public <T> Optional<List<T>> findHamiltonianCycle(List<T> elements,
      BiPredicate<T, T> adjacencyFn) {
    Map<Byte, Collection<Byte>> adjacent = new HashMap<>();
    for (byte i = 0; i < elements.size(); i++) {
      if (i >= 64) {
        throw new RuntimeException("This solver uses a long bitstring to record element sets. "
            + "Sizes Greater than 63 will cause undefined behavior.");
      }
      adjacent.put(i, new HashSet<>());
      for (byte j = 0; j < elements.size(); j++) {
        if (adjacencyFn.test(elements.get(i), elements.get(j))) {
          adjacent.get(i).add(j);
        }
      }
      if (adjacent.get(i).size() < 2) {
        return Optional.empty(); // A cycle requires every vertex to have 2+ edges.
      }
    }
    Optional<byte[]> idxes = new Solver(adjacent).calculate();
    if (idxes.isPresent()) {
      List<T> result = new ArrayList<>();
      for (byte i : idxes.get()) {
        result.add(elements.get(i));
      }
      return Optional.of(result);
    }
    return Optional.empty();
  }

  private static class Solver {
    private final Map<Byte, Collection<Byte>> adjacent;
    private final HashMap<Integer, Map<Long, Map<Long, byte[]>>> lengthToStartAndEndToElementsToPaths;
    private final long completeBS;
    private final int n;
    private int longestPathsAreOfLength;

    private Solver(Map<Byte, Collection<Byte>> adjacent) {
      this.adjacent = adjacent;
      lengthToStartAndEndToElementsToPaths = new HashMap<>();
      Map<Long, Map<Long, byte[]>> startAndEndToElementsToPaths = new HashMap<>();
      for (byte a : adjacent.keySet()) {
        for (byte b : adjacent.get(a)) {
          if (a < b && a != b) {
            long startAndEnd = set(set(0, a), b);
            startAndEndToElementsToPaths.putIfAbsent(startAndEnd, new HashMap<>());
            startAndEndToElementsToPaths.get(startAndEnd).put(startAndEnd, new byte[] { a, b });
          }
        }
      }
      lengthToStartAndEndToElementsToPaths.put(2, startAndEndToElementsToPaths);
      n = adjacent.size();
      long bs = 0;
      for (int i = 0; i < n; i++) {
        bs = set(bs, i);
      }
      completeBS = bs;
      longestPathsAreOfLength = 2;
    }

    public Optional<byte[]> calculate() {
      int requirePathsOfLength = (int) Math.ceil((2.0 + n) / 2);
      while (longestPathsAreOfLength <= requirePathsOfLength) {
        addOneLinkToEveryPathOfLongestLength();
      }
      return getCompletePathFromTwoPartialPaths();
    }

    private void addOneLinkToEveryPathOfLongestLength() {
      Map<Long, Map<Long, byte[]>> startAndEndToElementsToPaths = lengthToStartAndEndToElementsToPaths
          .get(longestPathsAreOfLength);
      Map<Long, Map<Long, byte[]>> newLengthMap = new HashMap<>();
      for (Long startAndEnd : startAndEndToElementsToPaths.keySet()) {
        Map<Long, byte[]> elementsToPath = startAndEndToElementsToPaths.get(startAndEnd);
        for (byte startOrEndIndex = 0; startOrEndIndex < n; startOrEndIndex++) {
          if (!isSet(startAndEnd, startOrEndIndex)) {
            continue;
          }
          for (Long elements : elementsToPath.keySet()) {
            byte[] path = elementsToPath.get(elements);
            for (byte adjacentToStartOrEnd : adjacent.get(startOrEndIndex)) {
              if (isSet(elements, adjacentToStartOrEnd)) {
                continue;
              }
              byte newElement = adjacentToStartOrEnd;
              byte[] newPath = createNewPathAppendingNewElementToOneSide(path, startOrEndIndex,
                  newElement);
              long resultStartAndEnd = set(set(0, newPath[0]), newPath[newPath.length - 1]);
              newLengthMap.putIfAbsent(resultStartAndEnd, new HashMap<>());
              newLengthMap.get(resultStartAndEnd).put(set(elements, newElement), newPath);
            }
          }
        }
      }
      longestPathsAreOfLength++;
      lengthToStartAndEndToElementsToPaths.put(longestPathsAreOfLength, newLengthMap);
    }

    private byte[] createNewPathAppendingNewElementToOneSide(byte[] path, byte from, byte to) {
      byte[] result = new byte[path.length + 1];
      int offset;
      if (path[0] == from) {
        result[0] = to;
        offset = 1;
      } else if (path[path.length - 1] == from) {
        result[path.length] = to;
        offset = 0;
      } else {
        throw new RuntimeException(
            String.format("Expected path %s to be terminated on one side by %s.",
                pathToString(path), (int) from));
      }
      for (int i = 0; i < path.length; i++) {
        result[offset + i] = path[i];
      }
      return result;
    }

    private Optional<byte[]> getCompletePathFromTwoPartialPaths() {
      int l1 = (n + 2) / 2;
      int l2 = (n + 2) - l1;
      for (Long startAndEnd : lengthToStartAndEndToElementsToPaths.get(l1).keySet()) {
        Map<Long, byte[]> elementsToPath1 = lengthToStartAndEndToElementsToPaths.get(l1)
            .get(startAndEnd);
        Map<Long, byte[]> elementsToPath2 = lengthToStartAndEndToElementsToPaths.get(l2)
            .get(startAndEnd);
        // Though L2 lengths are computed, start-ends might not have any paths with length = L2.
        if (elementsToPath2 == null) {
          continue;
        }
        for (long elements1 : elementsToPath1.keySet()) {
          for (long elements2 : elementsToPath2.keySet()) {
            if (areCompleteWithStartAndEnd(elements1, elements2, startAndEnd)) {
              byte[] pathA = elementsToPath1.get(elements1);
              byte[] pathB = elementsToPath2.get(elements2);
              byte[] result = new byte[n];
              int i = 0;
              for (; i < pathA.length; i++) {
                result[i] = pathA[i];
              }
              // Don't include the first or last element from the second array, these are start +
              // end.
              if (result[i - 1] == pathB[0]) {
                for (int j = 1; j < pathB.length - 1; j++) {
                  result[i + j - 1] = pathB[j];
                }
              } else {
                for (int j = pathB.length - 2; j > 0; j--) {
                  result[i + pathB.length - 2 - j] = pathB[j];
                }
              }
              return Optional.of(result);
            }
          }
        }
      }
      return Optional.empty();
    }

    private boolean areCompleteWithStartAndEnd(long pathA, long pathB, long se) {
      return isComplete(pathA | pathB) && (pathA & pathB) == se;
    }

    private static boolean isSet(long bs, byte location) {
      return (bs & (1 << location)) != 0;
    }

    private static long set(long bs, int location) {
      return bs | (1L << location);
    }

    private boolean isComplete(long bs) {
      return bs == completeBS;
    }

    @Override
    public String toString() {
      StringBuilder result = new StringBuilder();
      result.append("ADJACENT \n" + adjacent.toString() + "\n");
      result.append(String.format("N = %s\n", n));
      result.append(String.format("COMPLETE = %s\n", Long.toBinaryString(completeBS)));
      for (int i = 2; i <= longestPathsAreOfLength; i++) {
        result.append(String.format("  Of Length %s\n", i));
        Map<Long, Map<Long, byte[]>> startAndEndToElementsToPaths = lengthToStartAndEndToElementsToPaths
            .get(i);
        for (Long startAndEnd : startAndEndToElementsToPaths.keySet()) {
          result.append("  ").append(bitString(startAndEnd)).append("\n");
          for (Long elements : startAndEndToElementsToPaths.get(startAndEnd).keySet()) {
            byte[] path = startAndEndToElementsToPaths.get(startAndEnd).get(elements);
            result.append("    ");
            result.append(bitString(elements));
            result.append(" ");
            result.append(pathToString(path));
            result.append("\n");
          }
        }
      }
      return result.toString();
    }

    private static String pathToString(byte[] directions) {
      int[] result = new int[directions.length];
      for (int i = 0; i < directions.length; i++) {
        result[i] = directions[i];
      }
      return Arrays.toString(result);
    }

    private String bitString(long i) {
      return String.format("%" + n + "s", Long.toBinaryString(i)).replace(' ', '0');
    }
  }
}
