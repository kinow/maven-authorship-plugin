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
package net.sf.authorship.model;

/**
 * The author returned by Strategies.
 * 
 * @author Bruno P. Kinoshita - http://www.kinoshita.eti.br
 * @since 0.1
 */
public class Author {

    private String id;
    private String name;
    private String email;
    private String url;

    /**
     * Default constructor. 
     */
    public Author() {
        super();
    }

    /**
     * @param name
     * @param email
     */
    public Author(String id, String name, String email, String url) {
        super();
        this.id = id;
        this.name = name;
        this.email = email;
        this.url = url;
    }

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }
    
    /**
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
    }
    
    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name
     *            the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the email
     */
    public String getEmail() {
        return email;
    }

    /**
     * @param email
     *            the email to set
     */
    public void setEmail(String email) {
        this.email = email;
    }
    
    /**
     * @return the url
     */
    public String getUrl() {
        return url;
    }
    
    /**
     * @param url the url to set
     */
    public void setUrl(String url) {
        this.url = url;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if(obj != null) {
            if(obj instanceof Author) {
                Author that = (Author)obj;
                boolean equals = this.getName() != null ? this.getName().equals(that.getName()) : this.getName() == that.getName();
                equals = this.getEmail() != null ? this.getEmail().equals(that.getEmail()) : this.getEmail() == that.getEmail();
                equals = this.getUrl() != null ? this.getUrl().equals(that.getUrl()) : this.getUrl() == that.getUrl();
                equals = this.getId() != null ? this.getId().equals(that.getId()) : this.getId() == that.getId();
                return equals;
            }
        }
        return Boolean.FALSE;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return ("GitAuthor<"+this.getId()+", "+this.getName()+", "+this.getEmail()+", "+this.getUrl()).hashCode();
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "<"+this.getId()+", "+this.getName()+", "+this.getEmail()+", "+this.getUrl()+">";
    }

    /**
     * @return
     */
    public boolean isValid() {
        return this.id != null || this.name != null || this.email != null || this.url != null;
    }

}
