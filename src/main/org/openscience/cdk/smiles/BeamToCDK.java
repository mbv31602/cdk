package org.openscience.cdk.smiles;

import org.openscience.cdk.annotations.TestClass;
import org.openscience.cdk.annotations.TestMethod;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.interfaces.IChemObjectBuilder;
import org.openscience.cdk.interfaces.IPseudoAtom;
import org.openscience.cdk.interfaces.IStereoElement;
import org.openscience.cdk.stereo.DoubleBondStereochemistry;
import org.openscience.cdk.stereo.TetrahedralChirality;
import uk.ac.ebi.beam.Graph;
import uk.ac.ebi.beam.Atom;
import uk.ac.ebi.beam.Bond;
import uk.ac.ebi.beam.Configuration;
import uk.ac.ebi.beam.Edge;
import uk.ac.ebi.beam.Element;

import static org.openscience.cdk.CDKConstants.ISAROMATIC;
import static org.openscience.cdk.interfaces.IDoubleBondStereochemistry.Conformation;
import static org.openscience.cdk.interfaces.ITetrahedralChirality.Stereo;
import static uk.ac.ebi.beam.Configuration.Type.Tetrahedral;

/**
 * Convert the Beam toolkit object model to the CDK. Currently the aromatic
 * bonds from SMILES are loaded as singly bonded {@link IBond}s with the {@link
 * org.openscience.cdk.CDKConstants#ISAROMATIC} flag set.
 *
 * <blockquote><pre>
 * IChemObjectBuilder builder = SilentChemObjectBuilder.getInstance();
 * ChemicalGraph      g       = ChemicalGraph.fromSmiles("CCO");
 *
 * BeamToCDK          g2c     = new BeamToCDK(builder);
 *
 * // make sure the Beam notation is expanded - this converts organic
 * // subset atoms with inferred hydrogen counts to atoms with a
 * // set implicit hydrogen property
 * IAtomContainer    ac       = g2c.toAtomContainer(Functions.expand(g));
 * </pre></blockquote>
 *
 * @author John May
 * @cdk.module smiles
 * @see <a href="http://johnmay.github.io/beam">Beam SMILES Toolkit</a>
 */
@TestClass("org.openscience.cdk.smiles.BeamToCDKTest")
final class BeamToCDK {

    /** The builder used to create the CDK objects. */
    private final IChemObjectBuilder builder;

    /**
     * Create a new converter for the Beam SMILES toolkit. The converter needs
     * an {@link IChemObjectBuilder}. Currently the 'cdk-silent' builder will
     * give the best performance.
     *
     * @param builder chem object builder
     */
    BeamToCDK(IChemObjectBuilder builder) {
        this.builder = builder;
    }

    /**
     * Convert a Beam ChemicalGraph to a CDK IAtomContainer.
     *
     * @param g Beam graph instance
     * @return the CDK {@link IAtomContainer} for the input
     * @throws IllegalArgumentException the Beam graph was not 'expanded' - and
     *                                  contained organic subset atoms. If this
     *                                  happens use the Beam Functions.expand()
     *                                  to
     */
    @TestMethod("benzene,imidazole")
    IAtomContainer toAtomContainer(Graph g) {

        IAtomContainer ac = builder.newInstance(IAtomContainer.class,
                                                0, 0, 0, 0);
        IAtom[] atoms = new IAtom[g.order()];
        IBond[] bonds = new IBond[g.size()];

        int j = 0; // bond index

        for (int i = 0; i < g.order(); i++)
            atoms[i] = toCDKAtom(g.atom(i), g.implHCount(i));
        for (Edge e : g.edges())
            bonds[j++] = toCDKBond(e, atoms);

        // atom-centric stereo-specification (only tetrahedral ATM)
        for (int u = 0; u < g.order(); u++) {

            Configuration c = g.configurationOf(u);
            if (c.type() == Tetrahedral) {

                IStereoElement se = newTetrahedral(u, g.neighbors(u), atoms, c);

                // currently null if implicit H or lone pair
                if (se != null)
                    ac.addStereoElement(se);
            }
        }

        ac.setAtoms(atoms);
        ac.setBonds(bonds);

        // use directional bonds to assign bond-based stereo-specification
        addDoubleBondStereochemistry(g, ac);

        return ac;
    }

    /**
     * Adds double-bond conformations ({@link DoubleBondStereochemistry}) to the
     * atom-container.
     *
     * @param g  Beam graph object (for directional bonds)
     * @param ac The atom-container built from the Beam graph
     */
    private void addDoubleBondStereochemistry(Graph g, IAtomContainer ac) {

        for (final Edge e : g.edges()) {

            if (e.bond() != Bond.DOUBLE)
                continue;

            int u = e.either();
            int v = e.other(u);

            // find a directional bond for either end
            Edge first = findDirectionalEdge(g, u);
            Edge second = findDirectionalEdge(g, v);

            // if either atom is not incident to a directional label there
            // is no configuration
            if (first == null || second == null)
                continue;

            // if the directions (relative to the double bond) are the
            // same then they are on the same side - otherwise they
            // are opposite
            Conformation conformation = first.bond(u) == second.bond(v)
                                        ? Conformation.TOGETHER
                                        : Conformation.OPPOSITE;

            // get the stereo bond and build up the ligands for the
            // stereo-element - linear search could be improved with
            // map or API change to double bond element
            IBond db = ac.getBond(ac.getAtom(u), ac.getAtom(v));

            IBond[] ligands = new IBond[]{
                    ac.getBond(ac.getAtom(u),
                               ac.getAtom(first.other(u))),
                    ac.getBond(ac.getAtom(v),
                               ac.getAtom(second.other(v)))
            };

            ac.addStereoElement(new DoubleBondStereochemistry(db,
                                                              ligands,
                                                              conformation));

        }
    }

    /**
     * Utility for find the first directional edge incident to a vertex. If
     * there are no directional labels then null is returned.
     *
     * @param g graph from Beam
     * @param u the vertex for which to find
     * @return first directional edge (or null if none)
     */
    private Edge findDirectionalEdge(Graph g, int u) {
        for (Edge e : g.edges(u)) {
            Bond b = e.bond();
            if (b == Bond.UP || b == Bond.DOWN)
                return e;
        }
        return null;
    }

    /**
     * Creates a tetrahedral element for the given configuration. Currently only
     * tetrahedral centres with 4 explicit atoms are handled.
     *
     * @param u     central atom
     * @param vs    neighboring atom indices (in order)
     * @param atoms array of the CDK atoms (pre-converted)
     * @param c     the configuration of the neighbors (vs) for the order they
     *              are given
     * @return tetrahedral stereo element for addition to an atom container
     */
    private IStereoElement newTetrahedral(int u, int[] vs, IAtom[] atoms,
                                          Configuration c) {

        // no way to handle tetrahedral configurations with implicit
        // hydrogen or lone pair at the moment
        if (vs.length != 4)
            return null;

        // @TH1/@TH2 = anti-clockwise and clockwise respectively
        Stereo stereo = c == Configuration.TH1 ? Stereo.ANTI_CLOCKWISE
                                               : Stereo.CLOCKWISE;

        return new TetrahedralChirality(atoms[u],
                                        new IAtom[]{
                                                atoms[vs[0]],
                                                atoms[vs[1]],
                                                atoms[vs[2]],
                                                atoms[vs[3]]
                                        },
                                        stereo);
    }

    /**
     * Create a new CDK {@link IAtom} from the Beam Atom.
     *
     * @param beamAtom an Atom from the Beam ChemicalGraph
     * @param hCount   hydrogen count for the atom                
     * @return the CDK atom to have it's properties set
     */
    @TestMethod("methaneAtom,waterAtom,oxidanide,azaniumAtom")
    IAtom toCDKAtom(Atom beamAtom, int hCount) {

        IAtom cdkAtom = newCDKAtom(beamAtom);

        cdkAtom.setImplicitHydrogenCount(hCount);
        cdkAtom.setFormalCharge(beamAtom.charge());

        if (beamAtom.isotope() >= 0)
            cdkAtom.setMassNumber(beamAtom.isotope());

        if (beamAtom.aromatic())
            cdkAtom.setFlag(ISAROMATIC, true);

        // don't load for now - serious speed hit due to creating hashmap for
        // each atom
        // if (beamAtom.atomClass() != 0)
        //      cdkAtom.setProperty("smi:atomClass", beamAtom.atomClass());

        return cdkAtom;
    }

    /**
     * Create a new CDK {@link IAtom} from the Beam Atom. If the element is
     * unknown (i.e. '*') then an pseudo atom is created.
     *
     * @param atom an Atom from the BEam ChemicalGraph
     * @return the CDK atom to have it's properties set
     */
    @TestMethod("newUnknownAtom,newCarbonAtom,newNitrogenAtom")
    IAtom newCDKAtom(Atom atom) {
        Element element = atom.element();
        boolean unknown = element == Element.Unknown;
        return unknown ? builder.newInstance(IPseudoAtom.class,
                                             element.symbol())
                       : builder.newInstance(IAtom.class,
                                             element.symbol());
    }

    /**
     * Convert an edge from the Beam Graph to a CDK bond. Note -
     * currently aromatic bonds are set to SINGLE and then.
     *
     * @param edge  the Beam edge to convert
     * @param atoms the already converted atoms
     * @return new bond instance
     */
    @TestMethod("singleBondEdge,aromaticBondEdge,doubleBondEdge")
    IBond toCDKBond(Edge edge, IAtom[] atoms) {

        int u = edge.either();
        int v = edge.other(u);

        IBond bond = builder.newInstance(IBond.class,
                                         atoms[u],
                                         atoms[v],
                                         toCDKBondOrder(edge));

        if (edge.bond() == Bond.AROMATIC
                || (atoms[u].getFlag(ISAROMATIC) && atoms[v].getFlag(ISAROMATIC))) {
            bond.setFlag(ISAROMATIC, true);
            atoms[u].setFlag(ISAROMATIC, true);
            atoms[v].setFlag(ISAROMATIC, true);
        }

        return bond;
    }

    /**
     * Convert bond label on the edge to a CDK bond order - there is no aromatic
     * bond order and as such this is currently set 'SINGLE' with the aromatic
     * flag to be set.
     *
     * @param edge beam edge
     * @return CDK bond order for the edge type
     * @throws IllegalArgumentException the bond was a 'DOT' - should not be
     *                                  loaded but the exception is there
     *                                  in-case
     */
    private IBond.Order toCDKBondOrder(Edge edge) {
        switch (edge.bond()) {
            case SINGLE:
            case UP:
            case DOWN:
            case IMPLICIT:  // single/aromatic - aromatic ~ single atm.
            case AROMATIC:  // we will also set the flag
                return IBond.Order.SINGLE;
            case DOUBLE:
                return IBond.Order.DOUBLE;
            case TRIPLE:
                return IBond.Order.TRIPLE;
            case QUADRUPLE:
                return IBond.Order.QUADRUPLE;
            default:
                throw new IllegalArgumentException("Edge label " + edge
                        .bond() + "cannot be converted to a CDK bond order");
        }
    }


}
