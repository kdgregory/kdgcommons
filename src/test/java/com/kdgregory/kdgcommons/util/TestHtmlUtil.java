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

import java.util.Map;
import java.util.TreeMap;

import org.junit.Test;
import static org.junit.Assert.*;


public class TestHtmlUtil
{
    @Test
    public void testEscape() throws Exception
    {
        assertEquals("", HtmlUtil.escape(null));
        assertEquals("foo", HtmlUtil.escape("foo"));
        assertEquals("foo&amp;&lt;&gt;&#39;&quot;bar",
                     HtmlUtil.escape("foo&<>\'\"bar"));
        assertEquals("f&#xf6;o&#x2738;",
                     HtmlUtil.escape("f\u00f6o\u2738"));
    }


    @Test
    public void testUnescape() throws Exception
    {
        assertEquals("", HtmlUtil.unescape(null));

        assertSame("foo", HtmlUtil.unescape("foo"));

        assertEquals("foo&<>\'\"bar",
                     HtmlUtil.unescape("foo&amp;&lt;&gt;&apos;&quot;bar"));

        assertEquals("fooAbar", HtmlUtil.unescape("foo&#65;bar"));
        assertEquals("fooAbar", HtmlUtil.unescape("foo&#x41;bar"));

        assertEquals("\u00A0", HtmlUtil.unescape("&nbsp;"));

        assertEquals("foo&unknown;bar", HtmlUtil.unescape("foo&unknown;bar"));
        assertEquals("foo&;bar", HtmlUtil.unescape("foo&;bar"));
        assertEquals("foo&bar", HtmlUtil.unescape("foo&bar"));
    }


    @Test
    public void testAppendAttribute() throws Exception
    {
        StringBuilder buf1 = new StringBuilder();
        HtmlUtil.appendAttribute(buf1, "foo", "bar");
        assertEquals(" foo='bar'", buf1.toString());

        StringBuilder buf2 = new StringBuilder();
        HtmlUtil.appendAttribute(buf2, "foo", "b'a\"r");
        assertEquals(" foo='b&#39;a&quot;r'", buf2.toString());

        StringBuilder buf3 = new StringBuilder();
        HtmlUtil.appendAttribute(buf3, "foo", null);
        assertEquals(" foo=''", buf3.toString());
    }


    @Test
    public void testAppendOptionalAttribute() throws Exception
    {
        StringBuilder buf1 = new StringBuilder();
        HtmlUtil.appendOptionalAttribute(buf1, "foo", "bar");
        assertEquals(" foo='bar'", buf1.toString());

        StringBuilder buf2 = new StringBuilder();
        HtmlUtil.appendOptionalAttribute(buf2, "foo", "");
        assertEquals("", buf2.toString());

        StringBuilder buf3 = new StringBuilder();
        HtmlUtil.appendOptionalAttribute(buf3, "foo", null);
        assertEquals("", buf3.toString());
    }


    @Test
    public void testBuildQueryStringZeroParameters() throws Exception
    {
        Map<String,String> params = new TreeMap<String,String>();

        assertEquals("", HtmlUtil.buildQueryString(params, false));
    }


    @Test
    public void testBuildQueryStringOneParameter() throws Exception
    {
        Map<String,String> params = new TreeMap<String,String>();
        params.put("argle", "bargle");

        assertEquals("argle=bargle", HtmlUtil.buildQueryString(params, false));
    }


    @Test
    public void testBuildQueryStringTwoParameters() throws Exception
    {
        Map<String,String> params = new TreeMap<String,String>();
        params.put("argle", "bargle");
        params.put("foo", "bar");

        assertEquals("argle=bargle&foo=bar", HtmlUtil.buildQueryString(params, false));
    }


    @Test
    public void testBuildQueryStringIncludeEmpty() throws Exception
    {
        Map<String,String> params = new TreeMap<String,String>();
        params.put("argle", "bargle");
        params.put("foo", "");

        assertEquals("argle=bargle&foo=", HtmlUtil.buildQueryString(params, false));
    }


    @Test
    public void testBuildQueryStringIgnoreEmpty() throws Exception
    {
        Map<String,String> params = new TreeMap<String,String>();
        params.put("argle", "bargle");
        params.put("foo", "");

        assertEquals("argle=bargle", HtmlUtil.buildQueryString(params, true));
    }


    @Test
    public void testBuildQueryStringIncludeNull() throws Exception
    {
        Map<String,String> params = new TreeMap<String,String>();
        params.put("argle", "bargle");
        params.put("foo", null);

        assertEquals("argle=bargle&foo=", HtmlUtil.buildQueryString(params, false));
    }


    @Test
    public void testBuildQueryStringIgnoreNull() throws Exception
    {
        Map<String,String> params = new TreeMap<String,String>();
        params.put("argle", "bargle");
        params.put("foo", null);

        assertEquals("argle=bargle", HtmlUtil.buildQueryString(params, true));
    }


    @Test
    public void testBuildQueryStringWithEncoding() throws Exception
    {
        Map<String,String> params = new TreeMap<String,String>();
        params.put("argle", "ba/ rgle");

        assertEquals("argle=ba%2F%20rgle", HtmlUtil.buildQueryString(params, true));
    }


    @Test
    public void testParseQueryStringNull() throws Exception
    {
        Map<String,String> params = HtmlUtil.parseQueryString(null, false);
        assertEquals(0, params.size());
    }


    @Test
    public void testParseQueryStringZeroParameters() throws Exception
    {
        Map<String,String> params = HtmlUtil.parseQueryString("", false);
        assertEquals(0, params.size());
    }


    @Test
    public void testParseQueryStringSingleParameter() throws Exception
    {
        Map<String,String> params = HtmlUtil.parseQueryString("foo=bar", false);
        assertEquals(1, params.size());
        assertEquals("bar", params.get("foo"));
    }


    @Test
    public void testParseQueryStringTwoParameters() throws Exception
    {
        Map<String,String> params = HtmlUtil.parseQueryString("foo=bar&argle=bargle", false);
        assertEquals(2, params.size());
        assertEquals("bar", params.get("foo"));
        assertEquals("bargle", params.get("argle"));
    }


    @Test
    public void testParseQueryStringIncludeEmptyParameter() throws Exception
    {
        Map<String,String> params = HtmlUtil.parseQueryString("foo=&argle=bargle", false);
        assertEquals(2, params.size());
        assertEquals("", params.get("foo"));
        assertEquals("bargle", params.get("argle"));
    }


    @Test
    public void testParseQueryStringIgnoreEmptyParameter() throws Exception
    {
        Map<String,String> params = HtmlUtil.parseQueryString("foo=&argle=bargle", true);
        assertEquals(1, params.size());
        assertEquals("bargle", params.get("argle"));
    }


    @Test
    public void testParseQueryStringWithEncodedParameter() throws Exception
    {
        Map<String,String> params = HtmlUtil.parseQueryString("foo=+%2F+&argle=bargle", true);
        assertEquals(2, params.size());
        assertEquals(" / ", params.get("foo"));
        assertEquals("bargle", params.get("argle"));
    }


    @Test
    public void testParseQueryStringWithLeadingQuestion() throws Exception
    {
        Map<String,String> params = HtmlUtil.parseQueryString("?foo=bar&argle=bargle", true);
        assertEquals(2, params.size());
        assertEquals("bar", params.get("foo"));
        assertEquals("bargle", params.get("argle"));
    }


    @Test
    public void testParseQueryStringWithLeadingQuestionAndZeroParams() throws Exception
    {
        Map<String,String> params = HtmlUtil.parseQueryString("?", true);
        assertEquals(0, params.size());
    }


    @Test
    public void testParseQueryStringFailWithInvalidParam() throws Exception
    {
        try
        {
            HtmlUtil.parseQueryString("foobar", true);
            fail("parsed param that had no =");
        }
        catch (RuntimeException e)
        {
            // success
        }
    }


    @Test
    public void testHtmlToTextNullAndEmpty() throws Exception
    {
        assertEquals("", HtmlUtil.htmlToText(null));
        assertEquals("", HtmlUtil.htmlToText(""));
    }


    @Test
    public void testHtmlToTextSimple() throws Exception
    {
        String input = "<html>this is some text</html>";
        assertEquals("this is some text", HtmlUtil.htmlToText(input));
    }


    @Test
    public void testHtmlToTextMultipleEmbeddedTags() throws Exception
    {
        String input = "<html>this <em>is</em> some <b>bold</b> text</html>";
        assertEquals("this is some bold text", HtmlUtil.htmlToText(input));
    }


    @Test
    public void testHtmlToTextParaAndBreakReplacement() throws Exception
    {
        String input = "<html>this is <p>a new<P> paragraph<br/>and a new line</html>";
        assertEquals("this is \na new\n paragraph\nand a new line", HtmlUtil.htmlToText(input));
    }


    @Test
    public void testHtmlToTextParaAndBreakReplacementWithAttributes() throws Exception
    {
        String input = "<html>this is <p class='foo'>a new paragraph<br class='bar'/>and a new line</html>";
        assertEquals("this is \na new paragraph\nand a new line", HtmlUtil.htmlToText(input));
    }


    @Test
    public void testHtmlToTextListItemReplacement() throws Exception
    {
        String input = "<html>this is <li>the first <li class='foo'>and the second</html>";
        assertEquals("this is \n* the first \n* and the second", HtmlUtil.htmlToText(input));
    }


    @Test
    public void testHtmlToTextWithUnclosedTag() throws Exception
    {
        String input = "<html>this is <lithe first and the second";
        assertEquals("this is ", HtmlUtil.htmlToText(input));
    }


    @Test
    public void testHtmlToTextExistingNewlinesRemoved() throws Exception
    {
        String input = "<html>this is\nthe first line\r\nthis is the second\n\rand this is the third";
        assertEquals("this is the first line this is the second and this is the third", HtmlUtil.htmlToText(input));
    }
}
