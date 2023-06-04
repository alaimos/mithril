package com.alaimos.MITHrIL.app.Algorithms;

import it.unimi.dsi.fastutil.ints.*;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;

public class SetCoveringAlgorithm {

    static class SolutionTreeNode {

        public int level = 0;
        public int selectedIndex = -1;
        public SolutionTreeNode parent = null;
        public List<SolutionTreeNode> children = new ArrayList<>();

        public SolutionTreeNode() {
        }

        public SolutionTreeNode(int level, int selectedIndex, SolutionTreeNode parent) {
            this.level         = level;
            this.selectedIndex = selectedIndex;
            this.parent        = parent;
        }

        public IntSet getCovered(List<IntSet> subsets) {
            if (level == 0) return null;
            if (parent.level == 0) return subsets.get(selectedIndex);
            var covered = new IntOpenHashSet();
            var currentNode = this;
            while (currentNode.selectedIndex != -1) {
                covered.addAll(subsets.get(currentNode.selectedIndex));
                currentNode = currentNode.parent;
            }
            return covered;
        }

        public IntSet getSolution() {
            var solution = new IntRBTreeSet();
            var currentNode = this;
            while (currentNode.selectedIndex != -1) {
                solution.add(currentNode.selectedIndex);
                currentNode = currentNode.parent;
            }
            return solution;
        }

    }

    static class SolutionTree {

        public SolutionTreeNode root = new SolutionTreeNode();

        public Collection<IntSet> solutions = new HashSet<>();

        public int solutionMinSize = Integer.MAX_VALUE;

        public void pruneBranchesToSolutionNode(@NotNull SolutionTreeNode solutionNode) {
            var currentNode = solutionNode;
            while (currentNode.selectedIndex != -1) {
                var childrenSize = currentNode.children.size();
                if (childrenSize > 1) break;
                currentNode.parent.children.remove(currentNode);
                currentNode = currentNode.parent;
            }
        }

        public void addSolution(@NotNull SolutionTreeNode solutionNode) {
            var solution = solutionNode.getSolution();
            pruneBranchesToSolutionNode(solutionNode);
            if (solutionNode.level > solutionMinSize) return;
            if (solutionNode.level < solutionMinSize) {
                solutionMinSize = solutionNode.level;
                solutions.clear();
            }
            solutions.add(solution);
        }
    }

    public static @NotNull Collection<IntSet> findSetCover(@NotNull IntSet universe, @NotNull List<IntSet> subsets) {
        universe = removeUncoveredUniverseElements(universe, subsets);
        subsets  = removeUncoveredSubsetsElements(universe, subsets);
        var solutionTree = new SolutionTree();
        var stack = new Stack<SolutionTreeNode>();
        var maxLevel = 0;
        var x = 0;
        stack.push(solutionTree.root);
        while (!stack.empty()) {
            var currentNode = stack.pop();
            System.out.print(stack.size() + " - " + ++x + "\r");
            var covered = currentNode.getCovered(subsets);
            if (covered != null && covered.equals(universe)) {
                solutionTree.addSolution(currentNode);
                continue;
            }
            if (currentNode.level + 1 > solutionTree.solutionMinSize) continue;
            var greedyOptimalSubsets = findGreedyOptimalSubsets(subsets, covered);
            var iterator = greedyOptimalSubsets.intIterator();
            while (iterator.hasNext()) {
                var i = iterator.nextInt();
                var newLevel = currentNode.level + 1;
                var newNode = new SolutionTreeNode(newLevel, i, currentNode);
                if (newLevel == solutionTree.solutionMinSize
                        && solutionTree.solutions.contains(newNode.getSolution())) {
                    continue;
                }
                maxLevel = Math.max(maxLevel, newNode.level);
                currentNode.children.add(newNode);
                stack.push(newNode);

            }
        }
        System.out.println();
        return solutionTree.solutions;
    }

    private static @NotNull IntSet removeUncoveredUniverseElements(
            @NotNull IntSet universe, @NotNull List<IntSet> subsets
    ) {
        var coverable = new IntOpenHashSet();
        for (var subset : subsets) {
            coverable.addAll(subset);
        }
        var newUniverse = new IntOpenHashSet(universe);
        newUniverse.retainAll(coverable);
        return newUniverse;
    }

    private static @NotNull List<IntSet> removeUncoveredSubsetsElements(
            @NotNull IntSet universe, @NotNull List<IntSet> subsets
    ) {
        var newSubsets = new ArrayList<IntSet>();
        for (var subset : subsets) {
            var newSubset = new IntOpenHashSet(subset);
            newSubset.retainAll(universe);
            if (!newSubset.isEmpty()) newSubsets.add(newSubset);
        }
        return newSubsets;
    }

    private static @NotNull IntArrayList findGreedyOptimalSubsets(@NotNull List<IntSet> subsets, IntSet covered) {
        var maxUncovered = -1;
        var optimalSubsets = new IntArrayList(subsets.size());
        for (var i = subsets.size(); i-- > 0; ) {
            var subset = subsets.get(i);
            var uncovered = countUncovered(subset, covered);
            if (uncovered < maxUncovered) continue;
            if (uncovered > maxUncovered) {
                maxUncovered = uncovered;
                optimalSubsets.clear();
            }
            optimalSubsets.add(i);
        }
        if (maxUncovered == 0) return IntArrayList.of();
        return optimalSubsets;
    }

    private static int countUncovered(@NotNull IntSet subset, IntSet covered) {
        if (covered == null || covered.size() == 0) return subset.size();
        var uncovered = subset.size();
        var iterator = subset.intIterator();
        while (iterator.hasNext()) {
            if (covered.contains(iterator.nextInt())) {
                --uncovered;
            }
        }
        return uncovered;
    }

    public static void main(String[] args) {
        IntSet universe = IntSets.fromTo(0, 8000);
        var subsets = new ArrayList<IntSet>();
        var random = new Random(1234);
        for (var i = 0; i < 10000; i++) {
            var subset = new IntOpenHashSet();
            var size = random.nextInt(3999) + 1;
            while (subset.size() < size) {
                subset.add(random.nextInt(8000));
            }
            subsets.add(subset);
        }

//        try (var r = new BufferedReader(new FileReader("/home/alaimos/Scaricati/rail507.txt"))) {
//            var elements = 0;
//            String line;
//            IntSet subset;
//            var first = true;
//            while ((line = r.readLine()) != null) {
//                String[] numbers = line.trim().split(" ");
//                if (first) {
//                    elements = Integer.parseInt(numbers[0]);
//                    universe = IntSets.fromTo(1, elements + 1);
//                    first    = false;
//                    continue;
//                }
//                subset = new IntOpenHashSet();
//                for (var i = 2; i < numbers.length; i++) {
//                    subset.add(Integer.parseInt(numbers[i]));
//                }
//                subsets.add(subset);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

//        IntSet universe = new IntOpenHashSet();
//        universe.add(1);
//        universe.add(2);
//        universe.add(3);
//        universe.add(4);
//        universe.add(5);
//        universe.add(6);
//
//        var subsets = new ArrayList<IntSet>();
//        var subset1 = new IntOpenHashSet();
//        subset1.add(1);
//        subset1.add(2);
//        subset1.add(3);
//        subsets.add(subset1);
//
//        var subset2 = new IntOpenHashSet();
//        subset2.add(4);
//        subset2.add(5);
//        subsets.add(subset2);
//
//        var subset3 = new IntOpenHashSet();
//        subset3.add(1);
//        subset3.add(4);
//        subsets.add(subset3);
//
//        var subset4 = new IntOpenHashSet();
//        subset4.add(2);
//        subset4.add(3);
//        subset4.add(5);
//        subsets.add(subset4);

        var solutions = findSetCover(universe, subsets);

        System.out.println("Set Cover Solutions:" + solutions.size());
//        for (Set<Integer> solution : solutions) {
//            System.out.println(solution);
//        }
    }
}
