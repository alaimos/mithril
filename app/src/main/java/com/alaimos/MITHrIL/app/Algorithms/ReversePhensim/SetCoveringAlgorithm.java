package com.alaimos.MITHrIL.app.Algorithms.ReversePhensim;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntRBTreeSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class SetCoveringAlgorithm {

    private static final Logger log = LoggerFactory.getLogger(SetCoveringAlgorithm.class);

    private final IntSet universe;

    private final List<IntSet> subsets;

    @Contract("_, _ -> new")
    public static @NotNull SetCoveringAlgorithm of(@NotNull IntSet universe, @NotNull List<IntSet> subsets) {
        return new SetCoveringAlgorithm(universe, subsets);
    }

    public SetCoveringAlgorithm(@NotNull IntSet universe, @NotNull List<IntSet> subsets) {
        this.universe = removeUncoveredUniverseElements(universe, subsets);
        this.subsets  = removeUncoveredSubsetsElements(this.universe, subsets);
    }

    public @NotNull Collection<IntSet> run() {
        var solutionTree = new SolutionTree();
        var stack = new Stack<SolutionTreeNode>();
        var maxLevel = 0;
        var x = 0;
        stack.push(solutionTree.root);
        while (!stack.empty()) {
            var currentNode = stack.pop();
            if ((x++ % 100) == 0) log.debug("Iteration: {} - Stack size: {}", x, stack.size());
            var covered = currentNode.getCovered();
            if (covered != null && covered.equals(universe)) {
                solutionTree.addSolution(currentNode);
                continue;
            }
            if (currentNode.level + 1 > solutionTree.solutionMinSize) continue;
            var greedyOptimalSubsets = findGreedyOptimalSubsets(covered);
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
        log.debug("Iteration: {} - Stack size: {}", x, 0);
        return solutionTree.solutions;
    }

    private @NotNull IntSet removeUncoveredUniverseElements(@NotNull IntSet universe, @NotNull List<IntSet> subsets) {
        var coverable = new IntOpenHashSet();
        for (var subset : subsets) {
            coverable.addAll(subset);
        }
        var newUniverse = new IntOpenHashSet(universe);
        newUniverse.retainAll(coverable);
        return newUniverse;
    }

    private @NotNull List<IntSet> removeUncoveredSubsetsElements(
            @NotNull IntSet universe, @NotNull List<IntSet> subsets
    ) {
        var newSubsets = new ArrayList<IntSet>();
        for (var subset : subsets) {
            var newSubset = new IntOpenHashSet(subset);
            newSubset.retainAll(universe);
            newSubsets.add(newSubset);
        }
        return newSubsets;
    }

    private @NotNull IntArrayList findGreedyOptimalSubsets(IntSet covered) {
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

    private int countUncovered(@NotNull IntSet subset, IntSet covered) {
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

    private class SolutionTreeNode {

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

        public @Nullable IntSet getCovered() {
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

        public @NotNull IntSet getSolution() {
            var solution = new IntRBTreeSet();
            var currentNode = this;
            while (currentNode.selectedIndex != -1) {
                solution.add(currentNode.selectedIndex);
                currentNode = currentNode.parent;
            }
            return solution;
        }

    }

    private class SolutionTree {

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
}
