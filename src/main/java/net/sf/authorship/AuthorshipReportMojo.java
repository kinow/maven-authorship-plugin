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
package net.sf.authorship;

import java.io.File;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;

import net.sf.authorship.model.Author;
import net.sf.authorship.strategies.AuthorshipStrategy;
import net.sf.authorship.strategies.GitStrategy;
import net.sf.authorship.strategies.JavaSourceStrategy;
import net.sf.authorship.strategies.SvnOptions;
import net.sf.authorship.strategies.SvnStrategy;
import net.sf.authorship.strategies.SvnStrategy.PROTOCOL;
import net.sf.authorship.util.AuthorshipException;

import org.apache.commons.validator.routines.EmailValidator;
import org.apache.maven.doxia.sink.Sink;
import org.apache.maven.doxia.siterenderer.Renderer;
import org.apache.maven.model.Developer;
import org.apache.maven.model.Scm;
import org.apache.maven.project.MavenProject;
import org.apache.maven.reporting.AbstractMavenReport;
import org.apache.maven.reporting.MavenReportException;
import org.apache.maven.scm.manager.NoSuchScmProviderException;
import org.apache.maven.scm.manager.ScmManager;
import org.apache.maven.scm.provider.ScmProvider;
import org.apache.maven.scm.repository.ScmRepositoryException;
import org.eclipse.jgit.lib.Constants;

/**
 * Process authors from SCM, Java Sources and pom.xml and 
 * generates a Authorship Report.
 * 
 * @goal authorship
 * @phase process-sources
 */
public class AuthorshipReportMojo extends AbstractMavenReport {
    /**
     * The Maven project.
     * 
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;

    /**
     * SCM Manager component to be injected.
     * 
     * @component
     */
    private ScmManager scmManager;
    
    /**
     * <i>Maven Internal</i>: The Doxia Site Renderer.
     * 
     * @component
     * @required
     * @readonly
     */
    private Renderer siteRenderer;
    
    /**
     * The output directory for the report.
     * 
     * @parameter default-value="${project.reporting.outputDirectory}/authorship"
     * @required
     */
    private File outputDirectory;

    private final static String SCM_PROVIDER_SVN_TYPE = "svn";
    private final static String GIT_PROVIDER_SVN_TYPE = "git";

//    public void execute() throws MojoExecutionException {
//        getLog().info("pom.xml authors: " + this.getPomAuthors());
//        getLog().info("SCM authors: " + this.getScmAuthors());
//        getLog().info("Java source authors: " + this.getJavaSourceAuthors());
//    }
    
    /**
     * @return list of authors from SCM.
     */
    private Set<Author> getScmAuthors() {
        Set<Author> authors = new LinkedHashSet<Author>();
        final Scm scm = project.getScm();
        if(scm != null) {
            final String connection = project.getScm().getConnection();
            if(connection == null) {
              getLog().info("Empty <connection> in pom.xml SCM information (<scm> XML tag).");  
            } else {
                ScmProvider scmProvider;
                try {
                    scmProvider = scmManager.getProviderByUrl(connection);
                    AuthorshipStrategy<Author> strategy = null;
                    if(SCM_PROVIDER_SVN_TYPE.equals(scmProvider.getScmType())) {
                        final String svnUrl = this.getSvnUrl(connection);
                        final PROTOCOL protocol = this.geSvnProtocol(connection);
                        final SvnOptions options = new SvnOptions(); // TODO: map from mojo config
                        strategy = new SvnStrategy(protocol, svnUrl, options); // FIXME 
                    } else if(GIT_PROVIDER_SVN_TYPE.equals(scmProvider.getScmType())) {
                        final String folder = new File(System.getProperty("java.io.tmpdir"), Long.toString(System.nanoTime())).getAbsolutePath();
                        // FIXME: avoid exceptions with index out of bound
                        strategy = new GitStrategy(connection.substring(connection.indexOf("scm:git:")+8, connection.length()), folder, Constants.HEAD, null);
                    }
                    if(strategy != null) {
                        authors = strategy.getAuthors();
                    }
                } catch (ScmRepositoryException e) {
                    getLog().warn("Failed to retrieve authorship from <scm>: " + e.getMessage(), e);
                } catch (NoSuchScmProviderException e) {
                    getLog().warn("Failed to retrieve authorship from <scm>: " + e.getMessage(), e);
                } catch (AuthorshipException ae) {
                    getLog().warn("Failed to retrieve authorship from <scm>: " + ae.getMessage(), ae);
                }
            }
        } else {
            getLog().info("No project SCM information found. Skipping authorship from SCM (<scm> XML tag info).");
        }
        return authors;
    }
    
    /**
     * @param connection
     * @return
     */
    private PROTOCOL geSvnProtocol(String connection) {
        if(connection.indexOf("scm:svn:http:") >= 0) {
            return PROTOCOL.HTTP_HTTPS;
        } else {
            return PROTOCOL.SVN;
        }
    }

    /**
     * @param connection
     * @return
     */
    private String getSvnUrl(String connection) {
        // TBD: find a better way to avoid exceptions like index out of bound
        return connection.substring(connection.indexOf("scm:svn:")+8, connection.length());
    }

    /**
     * @return list of authors from Java source files.
     */
    private Set<Author> getJavaSourceAuthors() {
        Set<Author> authors = new LinkedHashSet<Author>();
        final List<?> srcs = this.getCompileSourceRoots();
        if(srcs != null && srcs.size() > 0) {
            Iterator<?> iterator = srcs.iterator();
            while(iterator.hasNext()) {
                final String src = (String) iterator.next();
                final JavaSourceStrategy strategy = new JavaSourceStrategy(src);
                try {
                    authors = strategy.getAuthors();
                } catch (AuthorshipException ae) {
                    getLog().warn("Failed to retrieve authors from [" + src + "]", ae);
                }
            }
        } else {
            getLog().info("No Java sources directory found. Skipping authorship from Java sources (@author tags).");
        }
        return authors;
    }
    
    /**
     * @return list of authors from pom.xml.
     */
    private Set<Author> getPomAuthors() {
        // TBD: This could be replaced by a iterator and transformer from [functor]
        final Set<Author> authors = new LinkedHashSet<Author>();
        final List<?> developers = project.getDevelopers();
        if(developers != null && developers.size() > 0) {
            Iterator<?> iterator = developers.iterator();
            while(iterator.hasNext()) {
                Developer developer = (Developer) iterator.next();
                Author author = new Author();
                author.setId(developer.getId());
                author.setName(developer.getName());
                final String email = developer.getEmail();
                if(EmailValidator.getInstance().isValid(email)) {
                    author.setEmail(developer.getEmail());
                } else {
                    author.setEmail(this.getPomEmail(email));
                }
                author.setUrl(developer.getUrl());
                authors.add(author);
            }
        } else {
            getLog().info("No authors found in pom.xml. Skipping authorship from pom.xml (<author> XML tags).");
        }
        return authors;
    }
    
    /**
     * TBD: move this method to some helper class. It could even be a procedure using 
     * [functor].
     * 
     * @param email raw email from pom.xml that could not be validated using commons email validator
     * @return formatted email from pom.xml.
     */
    private String getPomEmail(String email) {
        try {
            email = email.replaceAll("(?i)\\s+AT", "@"); // (?i) is for case insensitive
            email = email.replaceAll("(?i)_AT_", "@");
            email = email.replaceAll("(?i)\\s+DOT", ".");
            email = email.replaceAll("(?i)_DOT_", ".");
            email = email.replaceAll("\\s", ""); 
        } catch (Throwable t) {
            getLog().warn("Failed to retrieve e-mail from pom.xml author ["+email+"]: " + t.getMessage(), t);
        }
        return email;
    }

    /* (non-Javadoc)
     * @see org.apache.maven.reporting.MavenReport#getDescription(java.util.Locale)
     */
    public String getDescription(Locale locale) {
        return getBundle(locale).getString( "report.authorship.description" );
    }

    /* (non-Javadoc)
     * @see org.apache.maven.reporting.MavenReport#getName(java.util.Locale)
     */
    public String getName(Locale locale) {
        return "Authorship Report";
    }
    
    /**
     * Gets the resource bundle for the report text.
     * 
     * @param locale
     *            The locale for the report, must not be <code>null</code>.
     * @return The resource bundle for the requested locale.
     */
    private ResourceBundle getBundle(Locale locale) {
        return ResourceBundle.getBundle("authorship-report", locale);
    }

    /* (non-Javadoc)
     * @see org.apache.maven.reporting.MavenReport#getOutputName()
     */
    public String getOutputName() {
        return "authorship/index";
    }
    
    /**
     * Returns the compileSourceRoots for the currently executing project.
     */
    @SuppressWarnings( "unchecked" )
    private List<String> getCompileSourceRoots()
    {
        return project.getExecutionProject().getCompileSourceRoots();
    }

    /* (non-Javadoc)
     * @see org.apache.maven.reporting.AbstractMavenReport#executeReport(java.util.Locale)
     */
    @Override
    public void executeReport(Locale locale) throws MavenReportException {
        // Step 0: Checking pom availability
        if ( "pom".equals( project.getPackaging() ) ) {
            getLog().info( "Skipping pom project" );
            return;
        }
        
        if ( outputDirectory == null ) {
            outputDirectory = new File(this.outputDirectory, "authorship"); 
            return;
        } 
        
        if(! outputDirectory.exists()) {
            if(!outputDirectory.mkdirs()) {
                getLog().error("Couldn't create authoring report directory ["+outputDirectory.getAbsolutePath()+"]");
                return;
            }
        }
        
        // Step 1: Analyze the project
        final Set<Author> pomAuthors = this.getPomAuthors();
        final Set<Author> scmAuthors = this.getScmAuthors();
        final Set<Author> srcAuthors = this.getJavaSourceAuthors();
        
        // Step 2: Create sink and bundle
        Sink sink = getSink();
        ResourceBundle bundle = getBundle(locale);
        
        // Step 3: Generate the report
        AuthorshipReportView reportView = new AuthorshipReportView();
        getLog().debug("Generating authorship report...");
        reportView.generateReport(pomAuthors, scmAuthors, srcAuthors, sink, bundle );
    }

    /* (non-Javadoc)
     * @see org.apache.maven.reporting.AbstractMavenReport#getOutputDirectory()
     */
    @Override
    protected String getOutputDirectory() {
        return outputDirectory.getAbsolutePath();
    }
    
    /* (non-Javadoc)
     * @see org.apache.maven.reporting.AbstractMavenReport#setReportOutputDirectory(java.io.File)
     */
    @Override
    public void setReportOutputDirectory(File reportOutputDirectory) {
        if ((reportOutputDirectory != null)
                && (!reportOutputDirectory.getAbsolutePath().endsWith(
                        "authorship"))) {
            this.outputDirectory = new File(reportOutputDirectory, "authorship");
        } else {
            this.outputDirectory = reportOutputDirectory;
        }
    }
    
    /**
     * @param outputDirectory the outputDirectory to set
     */
    public void setOutputDirectory(File outputDirectory) {
        this.outputDirectory = outputDirectory;
    }
    
    /**
     * @param siteRenderer the siteRenderer to set
     */
    public void setSiteRenderer(Renderer siteRenderer) {
        this.siteRenderer = siteRenderer;
    }
    
    /**
     * @param project the project to set
     */
    public void setProject(MavenProject project) {
        this.project = project;
    }

    /* (non-Javadoc)
     * @see org.apache.maven.reporting.AbstractMavenReport#getProject()
     */
    @Override
    protected MavenProject getProject() {
        return project;
    }

    /* (non-Javadoc)
     * @see org.apache.maven.reporting.AbstractMavenReport#getSiteRenderer()
     */
    @Override
    protected Renderer getSiteRenderer() {
        return siteRenderer;
    }
    
}