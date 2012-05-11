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

import java.util.LinkedHashSet;
import java.util.Set;

import net.sf.authorship.model.Author;
import net.sf.authorship.util.AuthorshipException;

import org.apache.commons.lang.StringUtils;
import org.tmatesoft.svn.core.ISVNLogEntryHandler;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.svn.SVNRepositoryFactoryImpl;
import org.tmatesoft.svn.core.wc.ISVNOptions;
import org.tmatesoft.svn.core.wc.SVNLogClient;
import org.tmatesoft.svn.core.wc.SVNWCUtil;

/**
 * SVN Strategy for finding authors. It uses a Java SVN API to list the commit 
 * log and find authors' ids.
 * 
 * @author Bruno P. Kinoshita - http://www.kinoshita.eti.br
 * @since 0.1
 */
public class SvnStrategy implements AuthorshipStrategy<Author> {

    public enum PROTOCOL {
        SVN, HTTP_HTTPS
    };

    private String readOnlyUrl;
    private SvnOptions svnOptions;

    private final SVNLogClient logClient;

    /**
     * Svn Strategy constructor.
     * 
     * @param protocol enum for protocol type
     * @param readOnlyUrl read only SVN url
     * @param svnOptions SVN options for connecting using Java SVN API
     */
    public SvnStrategy(PROTOCOL protocol, String readOnlyUrl, SvnOptions svnOptions) {
        if(protocol == PROTOCOL.SVN) {
            SVNRepositoryFactoryImpl.setup();
        } else if (protocol == PROTOCOL.HTTP_HTTPS) {
            DAVRepositoryFactory.setup();
        }
        this.readOnlyUrl = readOnlyUrl;
        this.svnOptions = svnOptions;

        // Auth manager
        ISVNAuthenticationManager authManager = SVNWCUtil.createDefaultAuthenticationManager();

        // Options
        boolean readonly = true;
        ISVNOptions options = SVNWCUtil.createDefaultOptions(readonly);
        logClient = new SVNLogClient(authManager, options);
    }

    /* (non-Javadoc)
     * @see net.sf.authorship.strategies.AuthorshipStrategy#getAuthors()
     */
    public Set<Author> getAuthors() {
        final Set<Author> authors = new LinkedHashSet<Author>();
        final ISVNLogEntryHandler authorsSvnLogHandler = new ISVNLogEntryHandler() {
            /**
             * This method only grabs the author name from the SVN log entry
             */
            public void handleLogEntry(SVNLogEntry logEntry) throws SVNException {
                final String authorId = logEntry.getAuthor();
                if(StringUtils.isNotBlank(authorId)) {
                    Author author = new Author();
                    author.setId(authorId);
                    authors.add(author);
                }
            }
        };
        
        final SVNURL url;
        try {
            url = SVNURL
                .parseURIDecoded(this.readOnlyUrl);
        } catch (SVNException svne) {
            throw new AuthorshipException("Invalid SVN url ["+this.readOnlyUrl+"]", svne);
        }
        
        try {
            logClient.doLog(url, new String[] { this.svnOptions.getPath() },
                this.svnOptions.getPegRevision(),
                this.svnOptions.getStartRevision(),
                this.svnOptions.getEndRevision(), Boolean.FALSE, // stopOnCopy
                Boolean.TRUE, // discoverChangedPaths
                this.svnOptions.getLimit(), authorsSvnLogHandler);
        } catch (SVNException svne) {
            throw new AuthorshipException("Failed to read commit log", svne);
        }
        
        return authors;
    }

}
