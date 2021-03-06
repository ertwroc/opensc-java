/***********************************************************
 * $Id$
 * 
 * PKCS#15 cryptographic provider of the opensc project.
 * http://www.opensc-project.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Created: 26.12.2007
 * 
 ***********************************************************/

package org.opensc.pkcs15.token.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;

import org.opensc.pkcs15.token.DF;
import org.opensc.pkcs15.token.DFAcl;
import org.opensc.pkcs15.token.EF;
import org.opensc.pkcs15.token.EFAcl;
import org.opensc.pkcs15.token.MF;
import org.opensc.pkcs15.token.PathHelper;
import org.opensc.pkcs15.token.Token;
import org.opensc.pkcs15.token.TokenFile;
import org.opensc.pkcs15.token.TokenFileAcl;
import org.opensc.pkcs15.token.TokenPath;
import org.opensc.pkcs15.util.Util;

/**
 * @author wglas
 *
 */
public class SoftwareToken implements Token {

    private static final String MF_PATH_STRING = "3F00";
    private File directory;
    private File currentFile;
    private File mfFile;
    private TokenPath currentPath;
    
    private static File appendToFile(final File file, final int relPath)
    {
        return new File (file,String.format(Locale.US,"%04X",relPath));
    }
    
    /**
     * @param directory
     */
    public SoftwareToken(File directory) {
        super();
        this.directory = directory;
        this.mfFile = new File(this.directory,MF_PATH_STRING);
        this.currentFile = this.mfFile;
        this.currentPath = PathHelper.MF_PATH;
        
        if (!this.mfFile.exists())
            this.mfFile.mkdir();
    }
    
    /* (non-Javadoc)
     * @see org.opensc.pkcs15.token.Token#reset()
     */
    @Override
    public void reset() throws IOException {
        
        this.currentFile = this.mfFile;
        this.currentPath = PathHelper.MF_PATH;
        
        if (this.mfFile.exists())
           Util.rmdirRecursive(this.mfFile);
        
        this.mfFile.mkdir();
    }

    /* (non-Javadoc)
     * @see org.opensc.pkcs15.token.Token#close()
     */
    @Override
    public void close() throws IOException {
        
    }

    /* (non-Javadoc)
     * @see org.opensc.pkcs15.token.Token#createEF(int, org.opensc.pkcs15.token.EFAcl)
     */
    @Override
    public EF createEF(int path, long size, EFAcl acl) throws IOException {
        
        File file = appendToFile(this.currentFile,path);
        
        if (!file.createNewFile())
            throw new IOException("Cannot create file ["+file.getCanonicalPath()+"].");
        
        TokenPath efPath = new TokenPath(this.currentPath,path);
        
        return new EF(efPath,file.length(),acl);
    }

    /* (non-Javadoc)
     * @see org.opensc.pkcs15.token.Token#createDF(int, org.opensc.pkcs15.token.DFAcl)
     */
    @Override
    public DF createDF(int path, long size, DFAcl acl) throws IOException {
        
       File file = appendToFile(this.currentFile,path);
        
        if (!file.mkdir())
            throw new IOException("Cannot create directory ["+file.getCanonicalPath()+"].");
        
        TokenPath dfPath = new TokenPath(this.currentPath,path);
        
        return new DF(dfPath,file.length(),acl);
    }

    /* (non-Javadoc)
     * @see org.opensc.pkcs15.token.Token#deleteDF(int)
     */
    @Override
    public void deleteDF(int path) throws IOException {
        
        File file = appendToFile(this.currentFile,path);
        
        if (!file.isDirectory())
            throw new IOException("File ["+file.getCanonicalPath()+"] is not a directory.");
        
        if (!file.delete())
            throw new IOException("Cannot delete directory ["+file.getCanonicalPath()+"].");
        
    }

    /* (non-Javadoc)
     * @see org.opensc.pkcs15.token.Token#deleteEF(int)
     */
    @Override
    public void deleteEF(int path) throws IOException {
        
        File file = appendToFile(this.currentFile,path);
        
        if (!file.isFile())
            throw new IOException("File ["+file.getCanonicalPath()+"] is not an oridnary file.");
        
        if (!file.delete())
            throw new IOException("Cannot delete file ["+file.getCanonicalPath()+"].");
        
    }

    /* (non-Javadoc)
     * @see org.opensc.pkcs15.token.Token#getCurrentFile()
     */
    @Override
    public TokenFile getCurrentFile() throws IOException {
        
        boolean r = this.currentFile.canRead();
        boolean w = this.currentFile.canWrite();
        boolean pw = this.currentFile.getParentFile().canWrite();
        
        if (this.currentFile.equals(this.mfFile))
            return new MF(this.currentPath,
                    this.currentFile.length(),
                    TokenFileAcl.AC_ALWAYS,
                    w ? TokenFileAcl.AC_ALWAYS : TokenFileAcl.AC_NEVER,
                    w ? TokenFileAcl.AC_ALWAYS : TokenFileAcl.AC_NEVER,
                    w ? TokenFileAcl.AC_ALWAYS : TokenFileAcl.AC_NEVER,
                    w ? TokenFileAcl.AC_ALWAYS : TokenFileAcl.AC_NEVER,
                    w ? TokenFileAcl.AC_ALWAYS : TokenFileAcl.AC_NEVER,
                    pw ? TokenFileAcl.AC_ALWAYS : TokenFileAcl.AC_NEVER,
                    w ? TokenFileAcl.AC_ALWAYS : TokenFileAcl.AC_NEVER,
                    w ? TokenFileAcl.AC_ALWAYS : TokenFileAcl.AC_NEVER,
                    w ? TokenFileAcl.AC_ALWAYS : TokenFileAcl.AC_NEVER);
        
        if (this.currentFile.isDirectory())
            return new DF(this.currentPath,
                    this.currentFile.length(),
                    TokenFileAcl.AC_ALWAYS,
                    w ? TokenFileAcl.AC_ALWAYS : TokenFileAcl.AC_NEVER,
                    w ? TokenFileAcl.AC_ALWAYS : TokenFileAcl.AC_NEVER,
                    w ? TokenFileAcl.AC_ALWAYS : TokenFileAcl.AC_NEVER,
                    w ? TokenFileAcl.AC_ALWAYS : TokenFileAcl.AC_NEVER,
                    w ? TokenFileAcl.AC_ALWAYS : TokenFileAcl.AC_NEVER,
                    pw ? TokenFileAcl.AC_ALWAYS : TokenFileAcl.AC_NEVER,
                    w ? TokenFileAcl.AC_ALWAYS : TokenFileAcl.AC_NEVER);
        
        return new EF(this.currentPath,
                this.currentFile.length(),
                r ? TokenFileAcl.AC_ALWAYS : TokenFileAcl.AC_NEVER,
                w ? TokenFileAcl.AC_ALWAYS : TokenFileAcl.AC_NEVER,
                w ? TokenFileAcl.AC_ALWAYS : TokenFileAcl.AC_NEVER,
                w ? TokenFileAcl.AC_ALWAYS : TokenFileAcl.AC_NEVER,
                w ? TokenFileAcl.AC_ALWAYS : TokenFileAcl.AC_NEVER,
                w ? TokenFileAcl.AC_ALWAYS : TokenFileAcl.AC_NEVER,
                w ? TokenFileAcl.AC_ALWAYS : TokenFileAcl.AC_NEVER,
                pw ? TokenFileAcl.AC_ALWAYS : TokenFileAcl.AC_NEVER,
                w ? TokenFileAcl.AC_ALWAYS : TokenFileAcl.AC_NEVER);
        
    }

    /* (non-Javadoc)
     * @see org.opensc.pkcs15.token.Token#readEFData()
     */
    @Override
    public InputStream readEFData() throws IOException {
        
        return new FileInputStream(this.currentFile);
    }

    /* (non-Javadoc)
     * @see org.opensc.pkcs15.token.Token#select(int)
     */
    @Override
    public TokenFile select(int path) throws IOException {
        
        File file = appendToFile(this.currentFile,path);
        
        if (!file.exists()) return null;
        
        this.currentFile = file;
        this.currentPath = new TokenPath(this.currentPath,path);
        
        return this.getCurrentFile();
    }

    /* (non-Javadoc)
     * @see org.opensc.pkcs15.token.Token#selectDF(int)
     */
    @Override
    public DF selectDF(int path) throws IOException {
        
        File file = appendToFile(this.currentFile,path);
        
        if (!file.exists())
            return null;
        
        if (!file.isDirectory())
            throw new IOException("File ["+file.getCanonicalPath()+"] is not a directory.");
        
        this.currentFile = file;
        this.currentPath = new TokenPath(this.currentPath,path);
        
        return (DF)this.getCurrentFile();
    }

    /* (non-Javadoc)
     * @see org.opensc.pkcs15.token.Token#selectParentDF()
     */
    @Override
    public DF selectParentDF() throws IOException {
        
        File file = this.currentFile.getParentFile();
        
        if (!file.isDirectory())
            throw new IOException("File ["+file.getCanonicalPath()+"] is not a directory.");
        
        this.currentFile = file;
        this.currentPath = this.currentPath.getParent();
        
        return (DF)this.getCurrentFile();
    }

    /* (non-Javadoc)
     * @see org.opensc.pkcs15.token.Token#selectEF(int)
     */
    @Override
    public EF selectEF(int path) throws IOException {
        
        File file = appendToFile(this.currentFile,path);
        
        if (!file.exists())
            return null;
        
        if (!file.isFile())
            throw new IOException("File ["+file.getCanonicalPath()+"] is not an oridinary file.");
        
        this.currentFile = file;
        this.currentPath = new TokenPath(this.currentPath,path);
        
        return (EF)this.getCurrentFile();
    }

    /* (non-Javadoc)
     * @see org.opensc.pkcs15.token.Token#selectMF()
     */
    @Override
    public MF selectMF() throws IOException {
        
        this.currentFile = this.mfFile;
        this.currentPath = PathHelper.MF_PATH;
        return (MF)this.getCurrentFile();
    }

    /* (non-Javadoc)
     * @see org.opensc.pkcs15.token.Token#writeEFData()
     */
    @Override
    public OutputStream writeEFData() throws IOException {
        
        if (!this.currentFile.delete())
            throw new IOException("Cannot delete EF ["+this.currentFile+"] before writing.");
        
        return new FileOutputStream(this.currentFile);
    }

}
