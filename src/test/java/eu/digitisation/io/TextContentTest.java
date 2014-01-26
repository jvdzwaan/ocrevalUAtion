/*
 * Copyright (C) 2013 Universidad de Alicante
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package eu.digitisation.io;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author carrasco@ua.es
 */
public class TextContentTest {

    public TextContentTest() {
    }

    /**
     * Test of toString method, of class TextContent.
     *
     * @throws java.net.URISyntaxException
     * @throws eu.digitisation.io.WarningException
     */
    @Test
    public void testToString() throws URISyntaxException, WarningException {
        System.out.println("toString");
        URL resourceUrl = getClass().getResource("/UnicodeCharEquivalences.txt");
        File file = new File(resourceUrl.toURI());
        CharFilter filter = null;//new CharFilter(file);
        String s = "hola   " + "\n\t" + " y\u2028 de todo\n";
        TextContent instance = new TextContent(s, filter);
        String expResult = "hola y de todo";
        String result = instance.toString();
        assertEquals(expResult.length(), result.length());
        assertEquals(expResult, result);
    }
}
