// Copyright Keith D Gregory
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.kdgregory.kdgcommons.util;

import org.junit.Test;
import static org.junit.Assert.*;


public class TestURLBuilder
{
    @Test
    public void testEmptyConstructor() throws Exception
    {
        URLBuilder builder = new URLBuilder();
        assertEquals("/", builder.toString());
    }


    @Test
    public void testPathConstructor() throws Exception
    {
        assertEquals("/",
                     new URLBuilder(null).toString());
        assertEquals("/foo",
                     new URLBuilder("/foo").toString());
        assertEquals("http://foo.example.com/bar",
                     new URLBuilder("http://foo.example.com/bar").toString());
    }



    @Test
    public void testFullConstructor() throws Exception
    {
        assertEquals("/",
                     new URLBuilder(null, null, null).toString());
        assertEquals("http://foo.example.com/",
                     new URLBuilder(null, "foo.example.com", null).toString());
        assertEquals("https://foo.example.com/",
                     new URLBuilder("HTTPS", "foo.example.com", null).toString());
        assertEquals("http://foo.example.com/bar.jsp",
                     new URLBuilder(null, "foo.example.com", "bar.jsp").toString());
        assertEquals("/bar.jsp",
                     new URLBuilder(null, null, "bar.jsp").toString());
    }


    @Test
    public void testAppendPath() throws Exception
    {
        URLBuilder builder = new URLBuilder();
        assertSame(builder, builder.appendPath("bar.jsp"));
        assertEquals("/bar.jsp", builder.toString());
    }


    @Test
    public void testAppendPathTwice() throws Exception
    {
        URLBuilder builder = new URLBuilder();
        assertSame(builder, builder.appendPath("foo"));
        assertSame(builder, builder.appendPath("bar.jsp"));
        assertEquals("/foo/bar.jsp", builder.toString());
    }


    @Test
    public void testAppendPathReserved() throws Exception
    {
        URLBuilder builder = new URLBuilder();
        assertSame(builder, builder.appendPath("f o"));
        assertEquals("/f%20o", builder.toString());
    }


    @Test
    public void testAppendParameter() throws Exception
    {
        URLBuilder builder = new URLBuilder();
        assertSame(builder, builder.appendParameter("foo", "bar"));
        assertEquals("/?foo=bar", builder.toString());
    }


    @Test
    public void testAppendSecondQueryParameter() throws Exception
    {
        URLBuilder builder = new URLBuilder();
        assertSame(builder, builder.appendParameter("foo", "bar"));
        assertSame(builder, builder.appendParameter("argle", "bargle"));
        assertEquals("/?foo=bar&argle=bargle", builder.toString());
    }


    @Test
    public void testAppendEscapedParameter() throws Exception
    {
        URLBuilder builder = new URLBuilder();
        assertSame(builder, builder.appendParameter("f o", "b/r"));
        assertEquals("/?f%20o=b%2Fr", builder.toString());
    }


    @Test
    public void testAppendNullParameter() throws Exception
    {
        URLBuilder builder1 = new URLBuilder();
        assertSame(builder1, builder1.appendParameter("foo", null));
        assertEquals("/?foo=", builder1.toString());

        URLBuilder builder2 = new URLBuilder();
        assertSame(builder2, builder2.appendOptionalParameter("foo", null));
        assertEquals("/", builder2.toString());
    }


    @Test
    public void testURLEncodeDecodeNoChange() throws Exception
    {
        assertEquals("foo", URLBuilder.urlEncode("foo"));

        assertEquals("foo", URLBuilder.urlDecode("foo"));
    }


    @Test
    public void testURLEncodeDecodeReservedChar() throws Exception
    {
        assertEquals("f%26o", URLBuilder.urlEncode("f&o"));

        assertEquals("f&o", URLBuilder.urlDecode("f%26o"));
    }


    @Test
    public void testURLEncodeDecodeNonAscii() throws Exception
    {
        assertEquals("F%C2%A2O", URLBuilder.urlEncode("f\u00A2o").toUpperCase());

        assertEquals("f\u00A2o", URLBuilder.urlDecode("f%C2%A2o"));
        assertEquals("f\u00A2o", URLBuilder.urlDecode("f%c2%a2o"));
    }


    @Test
    public void testURLEncodeDecodeSpace() throws Exception
    {
        assertEquals("f%20o", URLBuilder.urlEncode("f o"));

        assertEquals("f o", URLBuilder.urlDecode("f+o"));
        assertEquals("f o", URLBuilder.urlDecode("f%20o"));
    }


    @Test
    public void testURLEncodeDecodeNull() throws Exception
    {
        assertEquals("", URLBuilder.urlEncode(null));

        assertEquals("", URLBuilder.urlDecode(null));
    }
}
