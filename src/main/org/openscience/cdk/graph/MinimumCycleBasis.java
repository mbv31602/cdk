package org.openscience.cdk.graph;

import org.openscience.cdk.annotations.TestClass;
import org.openscience.cdk.annotations.TestMethod;

import static org.openscience.cdk.graph.InitialCycles.Cycle;

/**
 * Compute the minimum cycle basis (MCB) of a graph. A cycle basis is a set of
 * cycles that can be combined to generate all the cycles of a graph. The MCB is
 * the basis of minimum weight (length). As an example, there are three cycle
 * bases of <a href="http://en.wikipedia.org/wiki/Naphthalene"><i>naphthalene</i></a>,
 * one basis with the two rings of size six and two bases with either ring of
 * size six and the perimeter ring of size 10. The weights of each basis are 12
 * (6+6), 16 (6+10), 16 (6+10). The basis of the two six member rings has
 * minimum weight (12) and is thus the MCB of <i>naphthalene</i>.<p/>
 *
 * The Smallest Set of Smallest Rings (SSSR) is normally used interchangeably
 * with MCB although traditionally their meaning was different {@cdk.cite
 * Berger04}. Although the MCB can be used to generate all other cycles of a
 * graph it may not be unique. A compound with a bridged ring system, such as <a
 * href="http://bit.ly/11sBxYr"><i>3-quinuclidinol</i></a>, has three equally
 * valid minimum cycle bases. From any two six member rings the third ring can
 * be generated by &oplus;-sum (xoring) their edges. Due there being more then
 * one MCB it should not be used as a <i>unique</i> descriptor. The smallest
 * (but potentially exponential) <i>unique</i> set of short cycles is the union
 * of all minimum cycle bases. This set is known as the {@link RelevantCycles}.
 *
 * @author John May
 * @cdk.module core
 * @cdk.keyword SSSR
 * @cdk.keyword Smallest Set of Smallest Rings
 * @cdk.keyword MCB
 * @cdk.keyword Minimum Cycle Basis
 * @cdk.keyword Cycle
 * @cdk.keyword Ring
 * @cdk.keyword Ring Perception
 * @see RelevantCycles
 * @see org.openscience.cdk.ringsearch.SSSRFinder#findSSSR()
 * @see <a href="http://en.wikipedia.org/wiki/Cycle_space">Cycle Basis</a>
 */
@TestClass("org.openscience.cdk.graph.MinimumCycleBasisTest")
public final class MinimumCycleBasis {

    /** The minimum cycle basis. */
    private final GreedyBasis basis;

    /**
     * Generate the minimum cycle basis for a graph.
     *
     * @param graph undirected adjacency list
     * @see org.openscience.cdk.ringsearch.RingSearch#fused()
     * @see GraphUtil#subgraph(int[][], int[])
     */
    @TestMethod("noGraph")
    public MinimumCycleBasis(final int[][] graph) {
        this(new InitialCycles(graph));
    }

    /**
     * Generate the minimum cycle basis from a precomputed set of initial
     * cycles.
     *
     * @param initial set of initial cycles.
     * @throws NullPointerException null InitialCycles provided
     */
    @TestMethod("noInitialCycles")
    MinimumCycleBasis(final InitialCycles initial) {

        if (initial == null)
            throw new NullPointerException("no InitialCycles provided");

        this.basis = new GreedyBasis(initial.numberOfCycles(),
                                     initial.numberOfEdges());

        // processing by size add cycles which are independent of smaller cycles
        for (final Cycle cycle : initial.cycles()) {
            if (basis.isIndependent(cycle))
                basis.add(cycle);
        }
    }

    /**
     * The paths of all cycles in the minimum cycle basis.
     *
     * @return array of vertex paths
     */
    @TestMethod("paths_bicyclo,paths_napthalene,paths_anthracene," +
                        "paths_cyclophane_odd,paths_cyclophane_even")
    public int[][] paths() {
        final int[][] paths = new int[size()][0];
        int i = 0;
        for (final Cycle c : basis.members())
            paths[i++] = c.path();
        return paths;
    }

    /**
     * The number of the cycles in the minimum cycle basis.
     *
     * @return size of minimum cycle set
     */
    @TestMethod("size_bicyclo,size_napthalene,size_anthracene," +
                        "size_cyclophane_odd,size_cyclophane_even")
    public int size() {
        return basis.members().size();
    }

}