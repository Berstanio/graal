/*
 * Copyright (c) 2018, 2020, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * The Universal Permissive License (UPL), Version 1.0
 *
 * Subject to the condition set forth below, permission is hereby granted to any
 * person obtaining a copy of this software, associated documentation and/or
 * data (collectively the "Software"), free of charge and under any and all
 * copyright rights in the Software, and any and all patent rights owned or
 * freely licensable by each licensor hereunder covering either (i) the
 * unmodified Software as contributed to or provided by such licensor, or (ii)
 * the Larger Works (as defined below), to deal in both
 *
 * (a) the Software, and
 *
 * (b) any piece of software and/or hardware listed in the lrgrwrks.txt file if
 * one is included with the Software each a "Larger Work" to which the Software
 * is contributed by such licensors),
 *
 * without restriction, including without limitation the rights to copy, create
 * derivative works of, display, perform, and distribute the Software and make,
 * use, sell, offer for sale, import, export, have made, and have sold the
 * Software and the Larger Work(s), and to sublicense the foregoing rights on
 * either these or other terms.
 *
 * This license is subject to the following condition:
 *
 * The above copyright notice and either this complete permission notice or at a
 * minimum a reference to the UPL must be included in all copies or substantial
 * portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.oracle.truffle.regex;

import java.util.Arrays;
import java.util.Objects;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.regex.result.RegexResult;
import com.oracle.truffle.regex.tregex.parser.flavors.PythonFlavor;
import com.oracle.truffle.regex.tregex.parser.flavors.RegexFlavor;
import com.oracle.truffle.regex.tregex.parser.flavors.RubyFlavor;
import com.oracle.truffle.regex.tregex.string.Encodings;

/**
 * These options define how TRegex should interpret a given parsing request.
 * <p>
 * Available options:
 * <ul>
 * <li><b>Flavor</b>: specifies the regex dialect to use. Possible values:
 * <ul>
 * <li><b>ECMAScript</b>: ECMAScript/JavaScript syntax (default).</li>
 * <li><b>PythonStr</b>: regular Python 3 syntax.</li>
 * <li><b>PythonBytes</b> Python 3 syntax, but for {@code bytes}-objects.</li>
 * <li><b>Ruby</b>: ruby syntax.</li>
 * </ul>
 * </li>
 * <li><b>Encoding</b>: specifies the string encoding to match against. Possible values:
 * <ul>
 * <li><b>UTF-8</b></li>
 * <li><b>UTF-16</b></li>
 * <li><b>UTF-32</b></li>
 * <li><b>LATIN-1</b></li>
 * <li><b>BYTES</b> (equivalent to LATIN-1)</li>
 * </ul>
 * </li>
 * <li><b>Validate</b>: don't generate a regex matcher object, just check the regex for syntax
 * errors.</li>
 * <li><b>U180EWhitespace</b>: treat 0x180E MONGOLIAN VOWEL SEPARATOR as part of {@code \s}. This is
 * a legacy feature for languages using a Unicode standard older than 6.3, such as ECMAScript 6 and
 * older.</li>
 * <li><b>UTF16ExplodeAstralSymbols</b>: generate one DFA states per (16 bit) {@code char} instead
 * of per-codepoint. This may improve performance in certain scenarios, but increases the likelihood
 * of DFA state explosion.</li>
 * <li><b>AlwaysEager</b>: do not generate any lazy regex matchers (lazy in the sense that they may
 * lazily compute properties of a {@link RegexResult}).</li>
 * <li><b>RegressionTestMode</b>: exercise all supported regex matcher variants, and check if they
 * produce the same results.</li>
 * <li><b>DumpAutomata</b>: dump all generated parser trees, NFA, and DFA to disk. This will
 * generate debugging dumps of most relevant data structures in JSON, GraphViz and LaTex
 * format.</li>
 * <li><b>StepExecution</b>: dump tracing information about all DFA matcher runs.</li>
 * </ul>
 * All options except {@code Flavor} and {@code Encoding} are boolean and {@code false} by default.
 */
public final class RegexOptions {

    private static final int U180E_WHITESPACE = 1;
    public static final String U180E_WHITESPACE_NAME = "U180EWhitespace";
    private static final int REGRESSION_TEST_MODE = 1 << 1;
    public static final String REGRESSION_TEST_MODE_NAME = "RegressionTestMode";
    private static final int DUMP_AUTOMATA = 1 << 2;
    public static final String DUMP_AUTOMATA_NAME = "DumpAutomata";
    private static final int STEP_EXECUTION = 1 << 3;
    public static final String STEP_EXECUTION_NAME = "StepExecution";
    private static final int ALWAYS_EAGER = 1 << 4;
    public static final String ALWAYS_EAGER_NAME = "AlwaysEager";
    private static final int UTF_16_EXPLODE_ASTRAL_SYMBOLS = 1 << 5;
    public static final String UTF_16_EXPLODE_ASTRAL_SYMBOLS_NAME = "UTF16ExplodeAstralSymbols";
    private static final int VALIDATE = 1 << 6;
    public static final String VALIDATE_NAME = "Validate";

    public static final String FLAVOR_NAME = "Flavor";
    public static final String FLAVOR_PYTHON_STR = "PythonStr";
    public static final String FLAVOR_PYTHON_BYTES = "PythonBytes";
    public static final String FLAVOR_RUBY = "Ruby";
    public static final String FLAVOR_ECMASCRIPT = "ECMAScript";
    private static final String[] FLAVOR_OPTIONS = {FLAVOR_PYTHON_STR, FLAVOR_PYTHON_BYTES, FLAVOR_RUBY, FLAVOR_ECMASCRIPT};

    public static final String ENCODING_NAME = "Encoding";

    private static final String FEATURE_SET_NAME = "FeatureSet";

    public static final RegexOptions DEFAULT = new RegexOptions(0, null, Encodings.UTF_16_RAW);

    private final int options;
    private final RegexFlavor flavor;
    private final Encodings.Encoding encoding;

    private RegexOptions(int options, RegexFlavor flavor, Encodings.Encoding encoding) {
        this.options = options;
        this.flavor = flavor;
        this.encoding = encoding;
    }

    public static Builder builder() {
        return new Builder();
    }

    @TruffleBoundary
    public static RegexOptions parse(String optionsString) throws RegexSyntaxException {
        int options = 0;
        RegexFlavor flavor = null;
        for (String propValue : optionsString.split(",")) {
            if (propValue.isEmpty()) {
                continue;
            }
            int eqlPos = propValue.indexOf('=');
            if (eqlPos < 0) {
                throw optionsSyntaxError(optionsString, propValue + " is not in form 'key=value'");
            }
            String key = propValue.substring(0, eqlPos);
            String value = propValue.substring(eqlPos + 1);
            switch (key) {
                case U180E_WHITESPACE_NAME:
                    options = parseBooleanOption(optionsString, options, key, value, U180E_WHITESPACE);
                    break;
                case REGRESSION_TEST_MODE_NAME:
                    options = parseBooleanOption(optionsString, options, key, value, REGRESSION_TEST_MODE);
                    break;
                case DUMP_AUTOMATA_NAME:
                    options = parseBooleanOption(optionsString, options, key, value, DUMP_AUTOMATA);
                    break;
                case STEP_EXECUTION_NAME:
                    options = parseBooleanOption(optionsString, options, key, value, STEP_EXECUTION);
                    break;
                case ALWAYS_EAGER_NAME:
                    options = parseBooleanOption(optionsString, options, key, value, ALWAYS_EAGER);
                    break;
                case UTF_16_EXPLODE_ASTRAL_SYMBOLS_NAME:
                    options = parseBooleanOption(optionsString, options, key, value, UTF_16_EXPLODE_ASTRAL_SYMBOLS);
                    break;
                case FLAVOR_NAME:
                    flavor = parseFlavor(optionsString, value);
                    break;
                case FEATURE_SET_NAME:
                    // deprecated
                    break;
                default:
                    throw optionsSyntaxError(optionsString, "unexpected option " + key);
            }
        }
        return new RegexOptions(options, flavor, Encodings.UTF_16_RAW);
    }

    private static int parseBooleanOption(String optionsString, int options, String key, String value, int flag) throws RegexSyntaxException {
        if (value.equals("true")) {
            return options | flag;
        } else if (!value.equals("false")) {
            throw optionsSyntaxErrorUnexpectedValue(optionsString, key, value, "true", "false");
        }
        return options;
    }

    private static RegexFlavor parseFlavor(String optionsString, String value) throws RegexSyntaxException {
        switch (value) {
            case FLAVOR_PYTHON_STR:
                return PythonFlavor.STR_INSTANCE;
            case FLAVOR_PYTHON_BYTES:
                return PythonFlavor.BYTES_INSTANCE;
            case FLAVOR_RUBY:
                return RubyFlavor.INSTANCE;
            case FLAVOR_ECMASCRIPT:
                return null;
            default:
                throw optionsSyntaxErrorUnexpectedValue(optionsString, FLAVOR_NAME, value, FLAVOR_PYTHON_STR, FLAVOR_PYTHON_BYTES, FLAVOR_RUBY, FLAVOR_ECMASCRIPT);
        }
    }

    private static RegexSyntaxException optionsSyntaxErrorUnexpectedValue(String optionsString, String key, String value, String... expectedValues) {
        return optionsSyntaxError(optionsString, String.format("unexpected value '%s' for option '%s', expected one of %s", value, key, Arrays.toString(expectedValues)));
    }

    private static RegexSyntaxException optionsSyntaxError(String optionsString, String msg) {
        return new RegexSyntaxException(String.format("Invalid options syntax in '%s': %s", optionsString, msg));
    }

    private boolean isBitSet(int bit) {
        return (options & bit) != 0;
    }

    public boolean isU180EWhitespace() {
        return isBitSet(U180E_WHITESPACE);
    }

    public boolean isRegressionTestMode() {
        return isBitSet(REGRESSION_TEST_MODE);
    }

    /**
     * Produce ASTs and automata in JSON, DOT (GraphViz) and LaTeX formats.
     */
    public boolean isDumpAutomata() {
        return isBitSet(DUMP_AUTOMATA);
    }

    /**
     * Trace the execution of automata in JSON files.
     */
    public boolean isStepExecution() {
        return isBitSet(STEP_EXECUTION);
    }

    /**
     * Always match capture groups eagerly.
     */
    public boolean isAlwaysEager() {
        return isBitSet(ALWAYS_EAGER);
    }

    /**
     * Explode astral symbols ({@code 0x10000 - 0x10FFFF}) into sub-automata where every state
     * matches one {@code char} as opposed to one code point.
     */
    public boolean isUTF16ExplodeAstralSymbols() {
        return isBitSet(UTF_16_EXPLODE_ASTRAL_SYMBOLS);
    }

    /**
     * Do not generate an actual regular expression matcher, just check the given regular expression
     * for syntax errors.
     */
    public boolean isValidate() {
        return isBitSet(VALIDATE);
    }

    public RegexFlavor getFlavor() {
        return flavor;
    }

    public Encodings.Encoding getEncoding() {
        return encoding;
    }

    public RegexOptions withEncoding(Encodings.Encoding newEnc) {
        return newEnc == encoding ? this : new RegexOptions(options, flavor, newEnc);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int hash = options;
        hash = prime * hash + Objects.hashCode(flavor);
        hash = prime * hash + encoding.hashCode();
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof RegexOptions)) {
            return false;
        }
        RegexOptions other = (RegexOptions) obj;
        return this.options == other.options && this.flavor == other.flavor && this.encoding == other.encoding;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (isU180EWhitespace()) {
            sb.append(U180E_WHITESPACE_NAME + "=true,");
        }
        if (isRegressionTestMode()) {
            sb.append(REGRESSION_TEST_MODE_NAME + "=true,");
        }
        if (isDumpAutomata()) {
            sb.append(DUMP_AUTOMATA_NAME + "=true,");
        }
        if (isStepExecution()) {
            sb.append(STEP_EXECUTION_NAME + "=true,");
        }
        if (isAlwaysEager()) {
            sb.append(ALWAYS_EAGER_NAME + "=true,");
        }
        if (flavor == PythonFlavor.STR_INSTANCE) {
            sb.append(FLAVOR_NAME + "=" + FLAVOR_PYTHON_STR + ",");
        } else if (flavor == PythonFlavor.BYTES_INSTANCE) {
            sb.append(FLAVOR_NAME + "=" + FLAVOR_PYTHON_BYTES + ",");
        } else if (flavor == RubyFlavor.INSTANCE) {
            sb.append(FLAVOR_NAME + "=" + FLAVOR_RUBY + ",");
        }
        return sb.toString();
    }

    public static final class Builder {

        private int options;
        private RegexFlavor flavor;
        private Encodings.Encoding encoding = Encodings.UTF_16_RAW;

        private Builder() {
            this.options = 0;
            this.flavor = null;
        }

        @TruffleBoundary
        public int parseOptions(String src) throws RegexSyntaxException {
            int i = 0;
            while (i < src.length()) {
                switch (src.charAt(i)) {
                    case 'A':
                        i = parseBooleanOption(src, i, ALWAYS_EAGER_NAME, ALWAYS_EAGER);
                        break;
                    case 'D':
                        i = parseBooleanOption(src, i, DUMP_AUTOMATA_NAME, DUMP_AUTOMATA);
                        break;
                    case 'E':
                        i = parseEncoding(src, i);
                        break;
                    case 'F':
                        i = parseFlavor(src, i);
                        break;
                    case 'R':
                        i = parseBooleanOption(src, i, REGRESSION_TEST_MODE_NAME, REGRESSION_TEST_MODE);
                        break;
                    case 'S':
                        i = parseBooleanOption(src, i, STEP_EXECUTION_NAME, STEP_EXECUTION);
                        break;
                    case 'U':
                        if (i + 1 >= src.length()) {
                            throw optionsSyntaxErrorUnexpectedKey(src, i);
                        }
                        switch (src.charAt(i + 1)) {
                            case '1':
                                i = parseBooleanOption(src, i, U180E_WHITESPACE_NAME, U180E_WHITESPACE);
                                break;
                            case 'T':
                                i = parseBooleanOption(src, i, UTF_16_EXPLODE_ASTRAL_SYMBOLS_NAME, UTF_16_EXPLODE_ASTRAL_SYMBOLS);
                                break;
                            default:
                                throw optionsSyntaxErrorUnexpectedKey(src, i);
                        }
                        break;
                    case 'V':
                        i = parseBooleanOption(src, i, VALIDATE_NAME, VALIDATE);
                        break;
                    case ',':
                        i++;
                        break;
                    case '/':
                        return i;
                    default:
                        throw optionsSyntaxErrorUnexpectedKey(src, i);
                }
            }
            return i;
        }

        private static int expectOptionName(String src, int i, String key) {
            if (!src.regionMatches(i, key, 0, key.length()) || src.charAt(i + key.length()) != '=') {
                throw optionsSyntaxErrorUnexpectedKey(src, i);
            }
            return i + key.length() + 1;
        }

        private static int expectValue(String src, int i, String value, String... expected) {
            if (!src.regionMatches(i, value, 0, value.length())) {
                throw optionsSyntaxErrorUnexpectedValue(src, i, expected);
            }
            return i + value.length();
        }

        private int parseBooleanOption(String src, int i, String key, int flag) throws RegexSyntaxException {
            int iVal = expectOptionName(src, i, key);
            if (src.regionMatches(iVal, "true", 0, "true".length())) {
                options |= flag;
                return iVal + "true".length();
            } else if (!src.regionMatches(iVal, "false", 0, "false".length())) {
                throw optionsSyntaxErrorUnexpectedValue(src, iVal, "true", "false");
            }
            return iVal + "false".length();
        }

        private int parseFlavor(String src, int i) throws RegexSyntaxException {
            int iVal = expectOptionName(src, i, FLAVOR_NAME);
            if (iVal >= src.length()) {
                throw optionsSyntaxErrorUnexpectedValue(src, iVal, FLAVOR_OPTIONS);
            }
            switch (src.charAt(iVal)) {
                case 'E':
                    flavor = null;
                    return expectValue(src, iVal, FLAVOR_ECMASCRIPT, FLAVOR_OPTIONS);
                case 'R':
                    flavor = RubyFlavor.INSTANCE;
                    return expectValue(src, iVal, FLAVOR_RUBY, FLAVOR_OPTIONS);
                case 'P':
                    if (iVal + 6 >= src.length()) {
                        throw optionsSyntaxErrorUnexpectedValue(src, iVal, FLAVOR_OPTIONS);
                    }
                    switch (src.charAt(iVal + 6)) {
                        case 'B':
                            flavor = PythonFlavor.BYTES_INSTANCE;
                            return expectValue(src, iVal, FLAVOR_PYTHON_BYTES, FLAVOR_OPTIONS);
                        case 'S':
                            flavor = PythonFlavor.STR_INSTANCE;
                            return expectValue(src, iVal, FLAVOR_PYTHON_STR, FLAVOR_OPTIONS);
                        default:
                            throw optionsSyntaxErrorUnexpectedValue(src, iVal, FLAVOR_OPTIONS);
                    }
                default:
                    throw optionsSyntaxErrorUnexpectedValue(src, iVal, FLAVOR_OPTIONS);
            }
        }

        private int parseEncoding(String src, int i) throws RegexSyntaxException {
            int iVal = expectOptionName(src, i, ENCODING_NAME);
            if (iVal >= src.length()) {
                throw optionsSyntaxErrorUnexpectedValue(src, iVal, Encodings.ALL_NAMES);
            }
            switch (src.charAt(iVal)) {
                case 'B':
                    encoding = Encodings.LATIN_1;
                    return expectValue(src, iVal, "BYTES", Encodings.ALL_NAMES);
                case 'L':
                    encoding = Encodings.LATIN_1;
                    return expectValue(src, iVal, Encodings.LATIN_1.getName(), Encodings.ALL_NAMES);
                case 'U':
                    if (iVal + 4 >= src.length()) {
                        throw optionsSyntaxErrorUnexpectedValue(src, iVal, FLAVOR_OPTIONS);
                    }
                    switch (src.charAt(iVal + 4)) {
                        case '8':
                            encoding = Encodings.UTF_8;
                            return expectValue(src, iVal, Encodings.UTF_8.getName(), Encodings.ALL_NAMES);
                        case '1':
                            encoding = Encodings.UTF_16;
                            return expectValue(src, iVal, Encodings.UTF_16.getName(), Encodings.ALL_NAMES);
                        case '3':
                            encoding = Encodings.UTF_32;
                            return expectValue(src, iVal, Encodings.UTF_32.getName(), Encodings.ALL_NAMES);
                        default:
                            throw optionsSyntaxErrorUnexpectedValue(src, iVal, Encodings.ALL_NAMES);
                    }
                default:
                    throw optionsSyntaxErrorUnexpectedValue(src, iVal, Encodings.ALL_NAMES);
            }
        }

        @TruffleBoundary
        private static RegexSyntaxException optionsSyntaxErrorUnexpectedKey(String src, int i) {
            int eqlPos = src.indexOf('=', i);
            return optionsSyntaxError(src, String.format("unexpected option '%s'", src.substring(i, eqlPos < 0 ? src.length() : eqlPos)));
        }

        @TruffleBoundary
        private static RegexSyntaxException optionsSyntaxErrorUnexpectedValue(String src, int i, String... expected) {
            int commaPos = src.indexOf(',', i);
            String value = src.substring(i, commaPos < 0 ? src.length() : commaPos);
            return optionsSyntaxError(src, String.format("unexpected value '%s', expected one of %s", value, Arrays.toString(expected)));
        }

        private boolean isBitSet(int bit) {
            return (options & bit) != 0;
        }

        public Builder u180eWhitespace(boolean enabled) {
            updateOption(enabled, U180E_WHITESPACE);
            return this;
        }

        public Builder regressionTestMode(boolean enabled) {
            updateOption(enabled, REGRESSION_TEST_MODE);
            return this;
        }

        public Builder dumpAutomata(boolean enabled) {
            updateOption(enabled, DUMP_AUTOMATA);
            return this;
        }

        public Builder stepExecution(boolean enabled) {
            updateOption(enabled, STEP_EXECUTION);
            return this;
        }

        public Builder alwaysEager(boolean enabled) {
            updateOption(enabled, ALWAYS_EAGER);
            return this;
        }

        public Builder utf16ExplodeAstralSymbols(boolean enabled) {
            updateOption(enabled, UTF_16_EXPLODE_ASTRAL_SYMBOLS);
            return this;
        }

        public boolean isUtf16ExplodeAstralSymbols() {
            return isBitSet(UTF_16_EXPLODE_ASTRAL_SYMBOLS);
        }

        public Builder flavor(@SuppressWarnings("hiding") RegexFlavor flavor) {
            this.flavor = flavor;
            return this;
        }

        public RegexFlavor getFlavor() {
            return flavor;
        }

        public Builder encoding(@SuppressWarnings("hiding") Encodings.Encoding encoding) {
            this.encoding = encoding;
            return this;
        }

        public Encodings.Encoding getEncoding() {
            return encoding;
        }

        public RegexOptions build() {
            return new RegexOptions(this.options, this.flavor, this.encoding);
        }

        private void updateOption(boolean enabled, int bitMask) {
            if (enabled) {
                this.options |= bitMask;
            } else {
                this.options &= ~bitMask;
            }
        }
    }
}
