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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 *  Applies redactions to a string, replacing sensitive content (as specified
 *  by regex) with something else. Redactions can either be "smart", replacing
 *  letters and numbers but leaving punctuation (this is useful to verify the
 *  format of input) or "simple", replacing everything (of any length) with the
 *  word "REDACTED".
 *  <p>
 *  Example: if you have the redaction pattern <code>\d{3}-\d{2}-\d{4}</code>,
 *  you can replace formatted social security numbers: "123-45-6789" becomes
 *  "###-##-####" with a smart redactor, "REDACTED" with a simple redactor. Of
 *  course, if you're concerned about SSNs, you should also use the pattern
 *  <code>\d{9}</code>; the redactor lets you specify multiple patterns.
 *  <p>
 *  Note: the redactor makes use of capturing groups to do its work. Your regexes
 *  should not use groups internally.
 *
 *  @since 1.0.18
 */
public class Redactor
{
    private List<Pattern> patterns = new ArrayList<Pattern>();
    private String replaceWith;


    /**
     *  Creates a smart redactor.
     */
    public Redactor(String... regexes)
    {
        this(true, regexes);
    }


    /**
     *  Creates a redactor that may be smart or simple, depending on the passed
     *  <code>isSmart</code> value.
     */
    public Redactor(boolean isSmart, String... regexes)
    {
        if (! isSmart)
            replaceWith = "REDACTED";

        for (String regex : regexes)
        {
            patterns.add(Pattern.compile("(" + regex + ")"));
        }
    }


    /**
     *  Applies the redactor to the provided string.
     */
    public String apply(String text)
    {
        if (text == null)
            return text;

        for (Pattern pattern : patterns)
        {
            StringBuffer sb = new StringBuffer(text.length() * 2);
            Matcher matcher = pattern.matcher(text);
            while (matcher.find())
            {
                String found = matcher.group(1);
                String replacement = (replaceWith != null)
                                   ? replaceWith
                                   : found.replaceAll("\\d", "#").replaceAll("[A-Za-z]", "X");
                matcher.appendReplacement(sb, replacement);
            }
            if (sb.length() > 0)
            {
                matcher.appendTail(sb);
                text = sb.toString();
            }
        }

        return text;
    }
}
