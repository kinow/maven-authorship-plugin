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

import org.tmatesoft.svn.core.wc.SVNRevision;

/**
 * Options for SVN strategy for finding authors.
 * 
 * @author Bruno P. Kinoshita - http://www.kinoshita.eti.br
 * @since 0.1
 */
public class SvnOptions {

    private SVNRevision pegRevision;
    private SVNRevision startRevision;
    private SVNRevision endRevision;
    long limit;
    String path;

    /**
     * 
     */
    public SvnOptions() {
        pegRevision = SVNRevision.create(0L);
        startRevision = SVNRevision.create(0L);
        endRevision = SVNRevision.HEAD;
        limit = 999999L;
        path = "";
    }

    /**
     * @param pegRevision
     * @param startRevision
     * @param endRevision
     * @param limit
     * @param path
     */
    public SvnOptions(SVNRevision pegRevision, SVNRevision startRevision,
            SVNRevision endRevision, long limit, String path) {
        super();
        this.pegRevision = pegRevision;
        this.startRevision = startRevision;
        this.endRevision = endRevision;
        this.limit = limit;
        this.path = path;
    }

    /**
     * @return the pegRevision
     */
    public SVNRevision getPegRevision() {
        return pegRevision;
    }

    /**
     * @param pegRevision
     *            the pegRevision to set
     */
    public void setPegRevision(SVNRevision pegRevision) {
        this.pegRevision = pegRevision;
    }

    /**
     * @return the startRevision
     */
    public SVNRevision getStartRevision() {
        return startRevision;
    }

    /**
     * @param startRevision
     *            the startRevision to set
     */
    public void setStartRevision(SVNRevision startRevision) {
        this.startRevision = startRevision;
    }

    /**
     * @return the endRevision
     */
    public SVNRevision getEndRevision() {
        return endRevision;
    }

    /**
     * @param endRevision
     *            the endRevision to set
     */
    public void setEndRevision(SVNRevision endRevision) {
        this.endRevision = endRevision;
    }

    /**
     * @return the limit
     */
    public long getLimit() {
        return limit;
    }

    /**
     * @param limit
     *            the limit to set
     */
    public void setLimit(long limit) {
        this.limit = limit;
    }

    /**
     * @return the path
     */
    public String getPath() {
        return path;
    }

    /**
     * @param path
     *            the path to set
     */
    public void setPath(String path) {
        this.path = path;
    }

}
