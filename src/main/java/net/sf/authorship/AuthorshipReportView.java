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

import java.util.ResourceBundle;
import java.util.Set;

import net.sf.authorship.model.Author;

import org.apache.commons.lang.StringUtils;
import org.apache.maven.doxia.sink.Sink;

/**
 * 
 * @author Bruno P. Kinoshita - http://www.kinoshita.eti.br
 * @since 0.1
 */
public class AuthorshipReportView {

    /**
     * Generates Authorship report.
     * 
     * TODO: use resource bundle and locale
     * 
     * @param pomAuthors
     * @param scmAuthors
     * @param srcAuthors
     * @param sink
     * @param bundle
     */
    public void generateReport(Set<Author> pomAuthors, Set<Author> scmAuthors,
            Set<Author> srcAuthors, Sink sink, ResourceBundle bundle) {
        sink.head();
        sink.title();
        sink.text("Authorship report"); 
        sink.title_();
        sink.head_();
        sink.body();
        
        sink.section1();
        sink.sectionTitle1();
        sink.text("Authorship report");
        sink.sectionTitle1_();
        
        sink.section2();
        sink.sectionTitle2();
        sink.text("pom.xml authors");
        sink.sectionTitle2_();
        
        generateDivergenceTable(pomAuthors, scmAuthors, srcAuthors, sink);
        
        sink.section2();
        sink.sectionTitle2();
        sink.text("pom.xml authors");
        sink.sectionTitle2_();
        
        if(pomAuthors != null && pomAuthors.size() > 0) {
            generateAuthorsTable(pomAuthors, sink);
        } else {
            sink.paragraph();
            sink.text("No authors found in pom.xml");
            sink.paragraph_();
            sink.horizontalRule();
        }
        
        sink.section2_();
        sink.section2();
        sink.sectionTitle2();
        sink.text("SCM authors");
        sink.sectionTitle2_();
        
        if(scmAuthors != null && scmAuthors.size() > 0) {
            generateAuthorsTable(scmAuthors, sink);
        } else {
            sink.paragraph();
            sink.text("No authors in project SCM");
            sink.paragraph_();
            sink.horizontalRule();
        }
        
        sink.section2_();
        sink.section2();
        sink.sectionTitle2();
        sink.text("Java source files authors");
        sink.sectionTitle2_();
        
        if(scmAuthors != null && scmAuthors.size() > 0) {
            generateAuthorsTable(srcAuthors, sink);
        } else {
            sink.paragraph();
            sink.text("No authors in project Java source files");
            sink.paragraph_();
            sink.horizontalRule();
        }
        
        sink.section2_();
        
        sink.section1_();
        
        sink.body_();
        sink.flush();
        sink.close();
    }

    /**
     * @param pomAuthors
     * @param scmAuthors
     * @param srcAuthors
     * @param sink
     */
    private void generateDivergenceTable(Set<Author> pomAuthors,
            Set<Author> scmAuthors, Set<Author> srcAuthors, Sink sink) {
        
        sink.paragraph();
        sink.bold();
        sink.text("This table lists the authors NOT found in pom.xml. " +
        		"It uses what was found by looking at the SCM commit " +
        		"log and at the Java source files (based on the @author tag)");
        sink.bold_();
        sink.paragraph_();
        sink.horizontalRule();
        
        sink.table();
        sink.tableRow();
        sink.tableCell();
        sink.tableCell_();
        sink.tableCell();
        sink.bold();
        sink.text("Author");
        sink.bold_();
        sink.tableCell_();
        sink.tableCell();
        sink.bold();
        sink.text("Present in SCM?");
        sink.bold_();
        sink.tableCell_();
        sink.tableCell();
        sink.bold();
        sink.text("Present in Java source files?");
        sink.bold_();
        sink.tableCell_();
        sink.tableRow_();
        
        int count = 1;
        // FIXME: we could merge the three data structures into one, and simplify it
        for(Author author : scmAuthors) {
            if(notInPom(author, pomAuthors)) {
                sink.tableRow();
                sink.tableCell();
                sink.text(Integer.toString(count));
                sink.tableCell_();
                sink.tableCell();
                sink.text(getPomAuthorValue(author));
                sink.tableCell_();
                sink.tableCell();
                sink.text(getAuthorPresentInSCM(author, scmAuthors));
                sink.tableCell_();
                sink.tableCell();
                sink.text(getAuthorPresentInJavaSources(author, srcAuthors));
                sink.tableCell_();
                sink.tableRow_();
                ++count;
            }
        }
        
        for(Author author : srcAuthors) {
            if(notInPom(author, pomAuthors)) {
                sink.tableRow();
                sink.tableCell();
                sink.text(Integer.toString(count));
                sink.tableCell_();
                sink.tableCell();
                sink.text(getPomAuthorValue(author));
                sink.tableCell_();
                sink.tableCell();
                sink.text(getAuthorPresentInSCM(author, scmAuthors));
                sink.tableCell_();
                sink.tableCell();
                sink.text(getAuthorPresentInJavaSources(author, srcAuthors));
                sink.tableCell_();
                sink.tableRow_();
                ++count;
            }
        }
        
        sink.table_();
    }

    /**
     * @param author
     * @param pomAuthors 
     * @return
     */
    private boolean notInPom(Author author, Set<Author> pomAuthors) {
        boolean found = Boolean.TRUE;
        for(Author pomAuthor : pomAuthors) {
            if(author.getId()!=null && pomAuthor.getId()!=null && author.getId().equals(pomAuthor.getId())) {
                found = Boolean.FALSE;
                break;
            }
            if(author.getName()!=null && pomAuthor.getName()!=null && author.getName().equals(pomAuthor.getName())) {
                found = Boolean.FALSE;
                break;
            }
            if(author.getEmail()!=null && pomAuthor.getEmail()!=null && author.getEmail().equals(pomAuthor.getEmail())) {
                found = Boolean.FALSE;
                break;
            }
        }
        return found;
    }

    /**
     * @param author
     * @param scmAuthors
     * @return
     */
    private String getAuthorPresentInSCM(Author author, Set<Author> scmAuthors) {
        for(Author scmAuthor : scmAuthors) {
            if(author.getId()!=null && scmAuthor.getId()!=null && author.getId().equals(scmAuthor.getId())) {
                return "Yup";
            }
            if(author.getName()!=null && scmAuthor.getName()!=null && author.getName().equals(scmAuthor.getName())) {
                return "Yup";
            }
            if(author.getEmail()!=null && scmAuthor.getEmail()!=null && author.getEmail().equals(scmAuthor.getEmail())) {
                return "Yup";
            }
        }
        return "Nope";
    }

    /**
     * @param author
     * @param srcAuthors
     * @return
     */
    private String getAuthorPresentInJavaSources(Author author,
            Set<Author> srcAuthors) {
        for(Author srcAuthor : srcAuthors) {
            if(author.getId()!=null && srcAuthor.getId()!=null && author.getId().equals(srcAuthor.getId())) {
                return "Yup";
            }
            if(author.getName()!=null && srcAuthor.getName()!=null && author.getName().equals(srcAuthor.getName())) {
                return "Yup";
            }
            if(author.getEmail()!=null && srcAuthor.getEmail()!=null && author.getEmail().equals(srcAuthor.getEmail())) {
                return "Yup";
            }
        }
        return "Nope";
    }

    /**
     * @param author
     * @return
     */
    private String getPomAuthorValue(Author author) {
        StringBuilder authorValue = new StringBuilder();
        authorValue.append("id="+StringUtils.defaultIfBlank(author.getId(), "<empty>"));
        authorValue.append(", name="+StringUtils.defaultIfBlank(author.getName(), "<empty>"));
        authorValue.append(", email="+StringUtils.defaultIfBlank(author.getEmail(), "<empty>"));
        return authorValue.toString();
    }

    /**
     * @param authors
     * @param sink
     */
    private void generateAuthorsTable(Set<Author> authors, Sink sink) {
        sink.table();
        sink.tableRow();
        sink.tableCell();
        sink.bold();
        sink.text("Id");
        sink.bold_();
        sink.tableCell_();
        sink.tableCell();
        sink.bold();
        sink.text("Name");
        sink.bold_();
        sink.tableCell_();
        sink.tableCell();
        sink.bold();
        sink.text("E-mail");
        sink.bold_();
        sink.tableCell_();
        sink.tableCell();
        sink.bold();
        sink.text("URL");
        sink.bold_();
        sink.tableCell_();
        sink.tableRow_();
        
        
        for(Author author : authors) {
            sink.tableRow();
            sink.tableCell();
            sink.text(StringUtils.defaultIfBlank(author.getId(), ""));
            sink.tableCell_();
            sink.tableCell();
            sink.text(StringUtils.defaultIfBlank(author.getName(), ""));
            sink.tableCell_();
            sink.tableCell();
            sink.text(StringUtils.defaultIfBlank(author.getEmail(), ""));
            sink.tableCell_();
            sink.tableCell();
            sink.text(StringUtils.defaultIfBlank(author.getUrl(), ""));
            sink.tableCell_();
            sink.tableRow_();
        }
        sink.table_();
    }

}
