/* $Revision$ $Author$ $Date$
 *
 * Copyright (C) 2004-2007  The Chemistry Development Kit (CDK) project
 * 
 * Contact: cdk-devel@slists.sourceforge.net
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 * All we ask is that proper credit is given for our work, which includes
 * - but is not limited to - adding the above copyright notice to the beginning
 * of your source code files, and to any copyright notice that you may distribute
 * with programs based on this work.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package org.openscience.cdk.io;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openscience.cdk.*;
import org.openscience.cdk.aromaticity.CDKHueckelAromaticityDetector;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IChemObjectBuilder;
import org.openscience.cdk.smiles.SmilesParser;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;

import java.io.IOException;
import java.io.StringWriter;

/**
 * TestCase for the writer MOL2 writer.
 *
 * @cdk.module test-io
 * @see Mol2Writer
 */
public class Mol2WriterTest extends ChemObjectIOTest {

    private static IChemObjectBuilder builder;

    @BeforeClass
    public static void setup() {
        builder = DefaultChemObjectBuilder.getInstance();
        setChemObjectIO(new Mol2Writer());
    }

    @Test
    public void testAccepts() throws Exception {
        Mol2Writer writer = new Mol2Writer();
        Assert.assertTrue(writer.accepts(ChemFile.class));
        Assert.assertTrue(writer.accepts(ChemModel.class));
        Assert.assertTrue(writer.accepts(MoleculeSet.class));
        Assert.assertTrue(writer.accepts(AtomContainerSet.class));
    }

    /**
     * @throws CDKException
     * @throws IOException
     * @cdk.bug 2675188
     */
    @Test
    public void testWriter1() throws CDKException, IOException {
        SmilesParser sp = new SmilesParser(builder);
        IAtomContainer molecule = sp.parseSmiles("C([H])([H])([H])([H])");

        StringWriter swriter = new StringWriter();
        Mol2Writer writer = new Mol2Writer(swriter);
        writer.write(molecule);
        writer.close();
        Assert.assertTrue(swriter.getBuffer().toString().indexOf("1 C1 0.000 0.000 0.000 C.3") > 0);
        Assert.assertTrue(swriter.getBuffer().toString().indexOf("1 2 1 1") > 0);
    }

    @Test
    public void testWriter2() throws CDKException, IOException {
        SmilesParser sp = new SmilesParser(builder);
        IAtomContainer molecule = sp.parseSmiles("c1ccccc1C=O");
        CDKHueckelAromaticityDetector.detectAromaticity(molecule);

        StringWriter swriter = new StringWriter();
        Mol2Writer writer = new Mol2Writer(swriter);
        writer.write(molecule);
        writer.close();

        Assert.assertTrue(swriter.getBuffer().toString().indexOf("1 C1 0.000 0.000 0.000 C.ar") > 0);
        Assert.assertTrue(swriter.getBuffer().toString().indexOf("8 O8 0.000 0.000 0.000 O.2") > 0);
        Assert.assertTrue(swriter.getBuffer().toString().indexOf("7 C7 0.000 0.000 0.000 C.2") > 0);
        Assert.assertTrue(swriter.getBuffer().toString().indexOf("1 2 1 ar") > 0);
        Assert.assertTrue(swriter.getBuffer().toString().indexOf("8 8 7 2") > 0);


    }
}