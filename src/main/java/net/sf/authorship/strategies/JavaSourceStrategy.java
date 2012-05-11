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
package net.sf.authorship.strategies;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.authorship.model.Author;
import net.sf.authorship.util.AuthorUtil;
import net.sf.authorship.util.AuthorshipException;

import org.apache.tools.ant.DirectoryScanner;

/**
 * 
 * @author Bruno P. Kinoshita - http://www.kinoshita.eti.br
 * @since 0.1
 */
public class JavaSourceStrategy implements AuthorshipStrategy<Author> {

    private static final  Logger LOGGER = Logger.getLogger(AuthorshipStrategy.class.getCanonicalName());
    
    /**
     * Pattern for finding Java Source files.
     */
    private static final String JAVA_SOURCES_PATTERN = "**/*.java";
    
    private static final Pattern JAVA_AUTHOR_TAG_REGEX = Pattern.compile("\\s?\\*\\s?@author\\s+(.*)");
    
    private final String folder;
    
    public JavaSourceStrategy(String folder) {
        super();
        this.folder = folder;
    }
    
    /* (non-Javadoc)
     * @see net.sf.authorship.strategies.AuthorshipStrategy#getAuthors()
     */
    public Set<Author> getAuthors() throws AuthorshipException {
        final String[] javaSources;
        try {
            javaSources = this.scan(folder, JAVA_SOURCES_PATTERN);
        } catch (IOException ioe) {
            throw new AuthorshipException("Failed to read java sources folder ["+this.folder+"]", ioe);
        }
        final Set<Author> authors = new LinkedHashSet<Author>();
        if(javaSources != null && javaSources.length > 0) {
            for(String javaSource : javaSources) {
                FileReader reader = null;
                BufferedReader bufferedReader = null;
                try {
                    reader = new FileReader(new File(this.folder, javaSource));
                    bufferedReader = new BufferedReader(reader);
                    
                    String line = null;
                    while((line = bufferedReader.readLine()) != null) {
                        Matcher matcher = JAVA_AUTHOR_TAG_REGEX.matcher(line);
                        if(matcher.lookingAt()) {
                            String authorTagValue = matcher.group(1);
                            Author author = AuthorUtil.getAuthorFromAuthorJavadocAnnotation(authorTagValue);
                            if(author != null && author.isValid()) {
                                authors.add(author);
                            } 
                        }
                    }
                } catch (Exception e) {
                    
                } finally {
                    if(bufferedReader != null) {
                        try { 
                            bufferedReader.close();
                        } catch(Exception e) {
                            LOGGER.warning(e.getMessage());
                        }
                    }
                    if(reader != null) {
                        try { 
                            reader.close();
                        } catch(Exception e) {
                            LOGGER.warning(e.getMessage());
                        }
                    }
                }
            }
        }
        return authors;
    }
    
    private String[] scan(String folder, final String includes) throws IOException {
        final DirectoryScanner ds = new DirectoryScanner();
        ds.setBasedir(folder);
        ds.setIncludes(new String[]{includes});
        ds.scan();
        return ds.getIncludedFiles();
    }

}
