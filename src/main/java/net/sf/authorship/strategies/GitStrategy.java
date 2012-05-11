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

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import net.sf.authorship.model.Author;
import net.sf.authorship.util.AuthorshipException;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryCache;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.util.FS;

/**
 * 
 * @author Bruno P. Kinoshita - http://www.kinoshita.eti.br
 * @since 0.1
 */
public class GitStrategy implements AuthorshipStrategy<Author> {

    private String readOnlyUrl;
    private String folder;
    private String fromRevision;
    private String toRevision;
    
    private static final  Logger LOGGER = Logger.getLogger(GitStrategy.class.getCanonicalName());

    public GitStrategy(String readOnlyUrl, String folder, String fromRevision,
            String toRevision) {
        super();
        this.readOnlyUrl = readOnlyUrl;
        this.folder = folder;
        this.fromRevision = fromRevision;
        this.toRevision = toRevision;
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.sf.authorship.strategies.AuthorshipStrategy#getAuthors()
     */
    public Set<Author> getAuthors() throws AuthorshipException {

        try {
            this.cloneRepository(this.readOnlyUrl, this.folder);
        } catch (IOException ioe) {
            throw new AuthorshipException("Failed to clone git repository ["
                    + this.readOnlyUrl + "]", ioe);
        }
        
        final Set<Author> authorEmails = new HashSet<Author>();
        final File directory = new File(this.folder);
        try {
            final Repository repository;
            try {
                repository = RepositoryCache.open(
                        RepositoryCache.FileKey.lenient(directory, FS.DETECTED),
                        true);
            } catch (IOException ioe) {
                throw new AuthorshipException("Failed to open git repository ["
                        + this.readOnlyUrl + "] cloned into local repository ["
                        + this.folder + "]", ioe);
            }
            
            final RevWalk walk;
            try {
                walk = new RevWalk(repository);
                if(StringUtils.isNotBlank(this.fromRevision)) {
                    this.fromRevision = Constants.HEAD;
                }
                ObjectId revId = repository.resolve(this.fromRevision);
                RevCommit root = walk.parseCommit(revId);
                walk.markStart(root);
                if(StringUtils.isNotBlank(this.toRevision)) {
                    ObjectId to = repository.resolve(this.toRevision);
                    RevCommit end = walk.parseCommit(to);
                    walk.markUninteresting(end);
                }
            } catch (IOException ioe) {
                throw new AuthorshipException("Failed to analyse revisions from git repository ["+this.folder+"]: " + ioe.getMessage(), ioe);
            }
            
            for (RevCommit commit : walk) {
                Author author = new Author(null, commit.getAuthorIdent().getName(), commit.getAuthorIdent().getEmailAddress(), null);
                authorEmails.add(author);
            }
            walk.dispose();
        } finally {
            try {
                if(!directory.delete()) {
                    directory.deleteOnExit();
                }
            } catch (RuntimeException re) {
                LOGGER.warning(re.getMessage());
            }
        }

        return authorEmails;
    }

    /**
     * Clones remote repository into local folder.
     * 
     * @param readOnlyUrl
     *            read only Git repository URL.
     * @param folder
     *            local repository folder.
     */
    private void cloneRepository(String readOnlyUrl, String folder)
            throws IOException {
        Git.cloneRepository().setURI(readOnlyUrl)
                .setDirectory(new File(folder)).call();
    }

}
