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

package net.sf.kdgcommons.util;

import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;


public class TestRedactor extends TestCase
{
    public void testSimpleRedactor() throws Exception
    {
        Redactor redactor = new Redactor(false, "\\d{3}-\\d{2}-\\d{4}", "\\d{9}", "A*B");

        List<List<String>> testcases = Arrays.asList(
            Arrays.asList(  (String)null,           (String)null),
            Arrays.asList(  "",                     ""),
            Arrays.asList(  "leave me alone",       "leave me alone"),
            Arrays.asList(  "Believe IT!",          "REDACTEDelieve IT!"),
            Arrays.asList(  "BABs!",                "REDACTEDREDACTEDs!"),
            Arrays.asList(  "X123456789X",          "XREDACTEDX"),
            Arrays.asList(  "X123-45-6789X",        "XREDACTEDX"),
            Arrays.asList(  "X123-456789X",         "X123-456789X")
        );

        for (List<String> testcase : testcases)
        {
            assertEquals(testcase.get(0), testcase.get(1), redactor.apply(testcase.get(0)));
        }
    }


    public void testSmartRedactor() throws Exception
    {
        Redactor redactor = new Redactor("\\d{3}-\\d{2}-\\d{4}", "\\d{9}", "A*B");

        List<List<String>> testcases = Arrays.asList(
            Arrays.asList(  (String)null,           (String)null),
            Arrays.asList(  "",                     ""),
            Arrays.asList(  "leave me alone",       "leave me alone"),
            Arrays.asList(  "Believe IT!",          "Xelieve IT!"),
            Arrays.asList(  "BABs!",                "XXXs!"),
            Arrays.asList(  "X123456789X",          "X#########X"),
            Arrays.asList(  "X123-45-6789X",        "X###-##-####X"),
            Arrays.asList(  "X123-456789X",         "X123-456789X")
        );

        for (List<String> testcase : testcases)
        {
            assertEquals(testcase.get(0), testcase.get(1), redactor.apply(testcase.get(0)));
        }
    }
}
