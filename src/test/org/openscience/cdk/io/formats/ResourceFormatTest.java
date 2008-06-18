/* $RCSfile: $
 * $Author$
 * $Date$
 * $Revision$
 *
 * Copyright (C) 2006-2007  Egon Willighagen <egonw@users.sf.net>
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
package org.openscience.cdk.io.formats;

import org.junit.Assert;
import org.junit.Test;

abstract public class ResourceFormatTest {
    
    private IResourceFormat resourceFormat;
    
    @Test public void testResourceFormatSet() {
        Assert.assertNotNull(resourceFormat);
    }

    @Test public void testMIMEType() {
        Assert.assertNotNull(resourceFormat.getMIMEType());
        Assert.assertNotSame(0, resourceFormat.getMIMEType().length());
    }
    
    @Test public void testFormatName() {
        Assert.assertNotNull(resourceFormat.getFormatName());
        Assert.assertNotSame(0, resourceFormat.getFormatName().length());
    }
    
    @Test public void testPrefNameExt() {
        Assert.assertNotNull(resourceFormat.getPreferredNameExtension());
        Assert.assertNotSame(0, resourceFormat.getPreferredNameExtension().length());
    }
    
    @Test public void testNameExt() {
        Assert.assertNotNull(resourceFormat.getNameExtensions());
        Assert.assertNotSame(0, resourceFormat.getNameExtensions().length);
    }
    
    @Test public void testIsXML() {
        Assert.assertNotNull(resourceFormat.isXMLBased());
    }
    
}
