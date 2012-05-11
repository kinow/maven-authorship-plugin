/*
 * The MIT License
 *
 * Copyright (c) <2012> <Bruno P. Kinoshita>
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package net.sf.authorship.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.authorship.model.Author;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.validator.routines.EmailValidator;
import org.apache.commons.validator.routines.UrlValidator;

/**
 * Utility methods for retrieving authors from text tokens.
 * 
 * @author Bruno P. Kinoshita - http://www.kinoshita.eti.br
 * @since 0.1
 */
public final class AuthorUtil {

    private AuthorUtil() {
        super();
    }

    public static final Author getAuthorFromAuthorJavadocAnnotation(String value) {
        value = StringUtils.trim(value);
        Author author = null;
        for (Production production : PRODUCTIONS) {
            author = production.produce(value);
            if (author != null) {
                break;
            }
        }
        return author;
    }

    private static boolean isEmail(String input) {
        return EmailValidator.getInstance().isValid(input);
    }
    
    private static boolean isUrl(String input) {
        return UrlValidator.getInstance().isValid(input);
    }
    
    // e-mail
    private static final Production EmailOnly = new Production() {
        public Author produce(String input) {
            Author author = null;
            if (EmailValidator.getInstance().isValid(input)) {
                author = new Author(null, null, input, null);
            }
            return author;
        }
    };

    private final static Pattern NAME_EMAIL1 = Pattern.compile("(.*)\\s?-\\s?(.*)");
    
    // name - e-mail or url
    private final static Production NameEmail1 = new Production() {
        public Author produce(String input) {
            Author author = null;
            Matcher matcher = NAME_EMAIL1.matcher(input);
            if(matcher.matches()) {
                author = new Author();
                author.setName(StringUtils.trim(matcher.group(1)));
                final String tokenValue = StringUtils.trim(matcher.group(2));
                if(isEmail(tokenValue)) {
                    author.setEmail(tokenValue);
                } else if(isUrl(tokenValue)) {
                    author.setUrl(tokenValue);
                }
            }
            return author;
        }
    };
    
    private final static Pattern NAME_EMAIL2 = Pattern.compile("(.*)\\s?\\(\\s?(.*)\\s?\\)\\s?");
    
    // name (e-mail or url)
    private final static Production NameEmail2 = new Production() {
        public Author produce(String input) {
            Author author = null;
            Matcher matcher = NAME_EMAIL2.matcher(input);
            if(matcher.matches()) {
                author = new Author();
                author.setName(StringUtils.trim(matcher.group(1)));
                final String tokenValue = StringUtils.trim(matcher.group(2));
                if(isEmail(tokenValue)) {
                    author.setEmail(tokenValue);
                } else if(isUrl(tokenValue)) {
                    author.setUrl(tokenValue);
                }
            }
            return author;
        }
    };
    
    private final static Pattern NAME_EMAIL3 = Pattern.compile("(.*)\\s?\\<\\s?(.*)\\s?\\>\\s?");
    
    // name <e-mail or url>
    private final static Production NameEmail3 = new Production() {
        public Author produce(String input) {
            Author author = null;
            Matcher matcher = NAME_EMAIL3.matcher(input);
            if(matcher.matches()) {
                author = new Author();
                author.setName(StringUtils.trim(matcher.group(1)));
                final String tokenValue = StringUtils.trim(matcher.group(2));
                if(isEmail(tokenValue)) {
                    author.setEmail(tokenValue);
                } else if(isUrl(tokenValue)) {
                    author.setUrl(tokenValue);
                }
            }
            return author;
        }
    };
    
    // name
    private final static Production Name = new Production() {
        public Author produce(String input) {
            Author author = new Author(null, input, null, null);
            return author;
        }
    };

    private static final Production[] PRODUCTIONS = new Production[] {
        EmailOnly, NameEmail1, NameEmail2, NameEmail3
        ,Name // this must be the last
    };
}

/**
 * A production that produces authors if the given input matches with its
 * programmed implementation.
 * 
 * @author Bruno P. Kinoshita - http://www.kinoshita.eti.br
 * @since 0.1
 */
interface Production {

    /**
     * Produces an Author from an input if matches the implementation of this
     * production.
     * 
     * @param input
     *            Author javadoc annotation value.
     * @return an Author if it matches its production implementation.
     */
    Author produce(String input);
}
