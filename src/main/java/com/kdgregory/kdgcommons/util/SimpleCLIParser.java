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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.kdgregory.kdgcommons.lang.StringUtil;


/**
 *  A simplified command-line processor. Extracts options from an argument list,
 *  with optional parameters, leaving the rest of the arguments for the caller.
 *  Will also generate an invocation message that lists the available options
 *  (and optionally exits with error code 1).
 *  <p>
 *  This class can be used on its own, but is normally subclassed. The subclass is
 *  responsible for defining the set of options and presenting a constructor that
 *  just takes the command-line argument vector. The subclass also normally defines
 *  an <code>enum</code> that corresponds to the available options, as a shorthand
 *  for accessing the option values.
 *  <p>
 *  The {@link SimpleCLIParser.OptionDefinition} nested class is how the caller
 *  defines legal options. Options have two forms:
 *  <p>
 *  <ul>
 *  <li> <strong> Enable/disable </strong>
 *  <p>  The option always has a value, either enabled or disabled. The user can
 *       specify distinct strings to enable or disable it, and there is a default
 *       value.
 *  <li> <strong> Parameterized </strong>
 *  <p>  The option takes zero or more parameters. These parameters are specified
 *       as separate command-line arguments (eg: "<code>--opt param</code>"), or as
 *       embedded parameters (eg: "<code>--opt=param</code>"). Parameterized options
 *       may be repeated (eg: "<code>--opt param1 --opt param2</code>"), and embedded
 *       parameters may also be repeated (eg: "<code>--opt param1,param2</code>"). A
 *       parameterized option is considered "enabled" if it appears in the argument
 *       list, with or without parmaeters.
 *  </ul>
 *  <p>
 *  The non-option command-line arguments are available in a number of ways. They can
 *  be retrieved all at once using the {@link #getParameters} method, or one at a time
 *  using the {@link #shift} method.
 *  <p>
 *  Design note: this class is intended for internal use in a narrow phase of
 *  program operation. As a result, it makes no attempt to protect its internal
 *  state. If you wish to modify the option data after construction, have at it
 *  (but beware that some lists are unmodifiable as implementation choices).
 *  <p>
 *  Note also that this class does not attempt to perform validation. The caller
 *  is responsible for ensuring that all required options are present, and that
 *  option parameters are valid.
 *
 *  @since 1.0.8
 */
public class SimpleCLIParser
{
    private List<OptionDefinition> optionDefs;
    private Map<Object,OptionDefinition> defsByKey = new HashMap<Object,OptionDefinition>();
    private Map<String,OptionDefinition> defsByStr = new HashMap<String,OptionDefinition>();

    private Map<Object,Option> options = new LinkedHashMap<Object,Option>();
    private List<String> nonOptions = new ArrayList<String>();
    private Iterator<String> shiftArgs;


    /**
     *  Processes the supplied list of arguments into options, option params,
     *  and non-options. Subclasses will normally expose a constructor that
     *  just accepts an argument vector, and hide the option definitions.
     *
     *  @param argv         The argument vector passed to <code>main()</code>.
     *  @param optionDefs   Definitions for all expected options.
     */
    public SimpleCLIParser(String[] argv, OptionDefinition... optionDefs)
    {
        this.optionDefs = Arrays.asList(optionDefs);
        buildOptionLookups();
        processArgs(argv);
        this.shiftArgs = nonOptions.iterator();
    }


    private void buildOptionLookups()
    {
        for (OptionDefinition option : optionDefs)
        {
            defsByKey.put(option.key, option);
            defsByStr.put(option.enableVal, option);
            defsByStr.put(option.disableVal, option);
        }
    }


    private void processArgs(String[] argv)
    {
        Iterator<String> itx = Arrays.asList(argv).iterator();
        while (itx.hasNext())
        {
            String arg = itx.next();
            if (! tryAsOption(arg, itx))
            {
                nonOptions.add(arg);
            }
        }
    }


    private boolean tryAsOption(String arg, Iterator<String> itx)
    {
        String argSansParam = arg.contains("=")
                            ? arg.substring(0, arg.indexOf("="))
                            : arg;

        OptionDefinition def = defsByStr.get(argSansParam);
        if (def == null)
            return false;

        Option opt = options.get(def.key);
        if (opt == null)
        {
            opt = new Option();
            options.put(def.key, opt);
        }

        opt.isEnabled = argSansParam.equals(def.enableVal);

        if (arg.contains("="))
        {
            String params = arg.substring(arg.indexOf("=") + 1);
            for (String param : params.split(","))
            {
                opt.addValue(param);
            }
        }
        else
        {
            for (int ii = 0 ; ii < def.numParams ; ii++)
            {
                opt.addValue(itx.next());
            }
        }

        return true;
    }


//----------------------------------------------------------------------------
//  Public Methods
//----------------------------------------------------------------------------

    /**
     *  Returns a single option definition, by key, <code>null</code> if
     *  there's no such definition.
     */
    public OptionDefinition getDefinition(Object key)
    {
        return defsByKey.get(key);
    }


    /**
     *  Returns the complete list of option definitions. This may be used to
     *  display information about the options (although {@link #getHelp} is
     *  usually a better choice.
     */
    public List<OptionDefinition> getAllDefinitions()
    {
        return optionDefs;
    }


    /**
     *  Returns a help message that describes the various options. This is
     *  normally reported for invalid output.
     */
    public String getHelp()
    {
        StringBuilder sb = new StringBuilder(1024);
        for (OptionDefinition def : optionDefs)
        {
            sb.append("\n");
            if (def.type == OptionDefinition.Type.BINARY)
            {
                sb.append("    ").append(def.enableVal);
                if (def.enableByDefault)
                    sb.append(" (default)");
                sb.append("\n");
                if (! StringUtil.isEmpty(def.disableVal))
                {
                    sb.append("    ").append(def.disableVal);
                    if (! def.enableByDefault)
                        sb.append(" (default)");
                    sb.append("\n");
                }
            }
            else
            {
                sb.append("    ").append(def.enableVal);
                for (int ii = 0 ; ii < def.numParams ; ii++)
                {
                    if (ii == 0)
                    {
                        sb.append("=PARAM").append(ii + 1);
                    }
                    else
                    {
                        sb.append(",PARAM").append(ii + 1);
                    }
                    sb.append("\n");
                }
            }
            sb.append("\n    ").append(def.description).append("\n");
        }
        return sb.toString();
    }


    /**
     *  Indicates whether the option identified by the passed key is enabled
     *  or not. For options that take parameters, the option is enabled if it
     *  appears on the command-line (regardless of the number of parameters).
     *
     *  @throws IllegalArgumentException if given a key that does not match
     *          any definition.
     */
    public boolean isOptionEnabled(Object key)
    {
        Option opt = options.get(key);
        if (opt != null)
            return opt.isEnabled;

        OptionDefinition def = defsByKey.get(key);
        if (def != null)
            return def.enableByDefault;

        throw new IllegalArgumentException("unknown option key: " + key);
    }


    /**
     *  Returns the keys for all specified options, in the order that they
     *  appear on the command line.
     */
    public List<Object> getOptions()
    {
        return new ArrayList<Object>(options.keySet());
    }


    /**
     *  For options that take parameters, returns the parameters. Will return
     *  an empty list if the option is not present (even if the option is an
     *  enable/disable type).
     */
    public List<String> getOptionValues(Object key)
    {
        Option opt = options.get(key);
        if ((opt == null) || (opt.values == null))
            return Collections.emptyList();

        return opt.values;
    }


    /**
     *  Returns the complete list of non-option parameters. You can call this
     *  as many times as desired, regardless of calls to {@link #shift}. The
     *  returned list is array-backed.
     */
    public List<String> getParameters()
    {
        return nonOptions;
    }


    /**
     *  Returns the next non-option parameter, <code>null</code> if there are
     *  none remaining. This method iterates through the list of parameters
     *  once, and may not be reset.
     */
    public String shift()
    {
        if (shiftArgs.hasNext())
            return shiftArgs.next();
        else
            return null;
    }


//----------------------------------------------------------------------------
//  Supporting Classes
//----------------------------------------------------------------------------

    /**
     *  This class defines a single command-line option.
     */
    public static class OptionDefinition
    {
        public enum Type { BINARY, PARAMETERIZED }

        private Type type;
        private Object key;
        private String enableVal;
        private String disableVal;
        private boolean enableByDefault;
        private int numParams;
        private String description;

        /**
         *  Constructor for an option that has separate "enable" and "disable"
         *  strings. Such an option cannot take parameters.
         *
         *  @param  key             A key used to retrieve this option's value
         *                          from the parsed command line. For readability,
         *                          an <code>enum</code> value is the best choice.
         *  @param  enableVal       The command-line string that enables the option.
         *                          May be null/blank, if the option is enabled by
         *                          default.
         *  @param  disableVal      The command-line string that disables the option.
         *                          May be null/blank, if the option is disabled by
         *                          default.
         *  @param  enableByDefault If <code>true</code>, the option will be enabled
         *                          unless explicitly disabled; if <code>false</code>,
         *                          disabled unless specifically enabled.
         *  @param  description     A description of the option.
         */
        public OptionDefinition(Object key, String enableVal, String disableVal, boolean enableByDefault, String description)
        {
            this.type = Type.BINARY;
            this.key = key;
            this.enableVal = enableVal;
            this.disableVal = disableVal;
            this.enableByDefault = enableByDefault;
            this.numParams = 0;
            this.description = description;
        }


        /**
         *  Constructor for an option that takes zero or more parameters. Such
         *  options are disabled by default; they must be explicitly specified
         *  on the command-line.
         *  <p>
         *  Parameters may be specified in one of two ways: either as subsequent
         *  arguments on the command line, or in the form :<code>OPTION=PARAM1,...</code>".
         *  At present, there is no support for an option that has a name that
         *  includes an "=".
         *
         *  @param  key             A key used to retrieve this option's value
         *                          from the parsed command line. For readability,
         *                          an <code>enum</code> value is the best choice.
         *  @param  enableVal       The command-line string that enables the option.
         *  @param  numParams       The number of parameters that this option takes.
         *                          This number of non-option command-line arguments
         *                          will be shifted from the argument list.
         *  @param  description     A description of the option.
         */
        public OptionDefinition(Object key, String enableVal, int numParams, String description)
        {
            this.type = Type.PARAMETERIZED;
            this.key = key;
            this.enableVal = enableVal;
            this.enableByDefault = false;
            this.numParams = numParams;
            this.description = description;
        }

        public Type getType()
        {
            return type;
        }

        public Object getKey()
        {
            return key;
        }

        public String getEnableVal()
        {
            return enableVal;
        }

        public String getDisableVal()
        {
            return disableVal;
        }

        public boolean isEnableByDefault()
        {
            return enableByDefault;
        }

        public int getNumParams()
        {
            return numParams;
        }

        public String getDescription()
        {
            return description;
        }
    }


    /**
     *  This class holds an option as specified on the command line. At present it'
     *  s only used internally, so no need for accessors.
     */
    private static class Option
    {
        public List<String> values;
        public boolean isEnabled;

        public void addValue(String param)
        {
            if (values == null)
                values = new ArrayList<String>();
            values.add(param);
        }
    }
}
