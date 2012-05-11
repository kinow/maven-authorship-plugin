package net.sf.authorship;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.File;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.command.checkout.CheckOutScmResult;
import org.apache.maven.scm.manager.ScmManager;
import org.apache.maven.scm.provider.ScmProvider;
import org.apache.maven.scm.repository.ScmRepository;

/**
 * Goal which touches a timestamp file.
 * 
 * @goal touch
 * 
 * @phase process-sources
 */
public class AuthorshipMojo extends AbstractMojo {
    /**
     * The Maven project.
     *
     * @parameter expression="${project}"
     * @required
     * @<span id="high_3" class="searchterm3">read</span>only
     */
    private MavenProject project;
    
    /** SCM Manager component to be injected.
     * @component
     */
    private ScmManager scmManager;
    
    /** SCM working copy path.
     *  @parameter expression="${wcDirectory}"
     */
    private File wcDir;

    public void execute() throws MojoExecutionException, MojoFailureException {
        
      //get data from project
        String developerConnection = project.getScm().getDeveloperConnection();
 
        ScmProvider scmProvider;
        try {
            scmProvider = scmManager.getProviderByUrl(developerConnection);
            ScmRepository scmRepository = scmManager.makeScmRepository(developerConnection);
            CheckOutScmResult scmResult = scmProvider.checkOut(scmRepository, new ScmFileSet(wcDir));
            if (!scmResult.isSuccess()) {
                getLog().error(String.format("Fail to chckout artifact %s to %s", project.getArtifact(), wcDir));
            }
        } catch (Exception e) {
            throw new MojoExecutionException("Fail to checkout.", e);
        }
    }
}
