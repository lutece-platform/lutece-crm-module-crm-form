/*
 * Copyright (c) 2002-2011, Mairie de Paris
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  1. Redistributions of source code must retain the above copyright notice
 *     and the following disclaimer.
 *
 *  2. Redistributions in binary form must reproduce the above copyright notice
 *     and the following disclaimer in the documentation and/or other materials
 *     provided with the distribution.
 *
 *  3. Neither the name of 'Mairie de Paris' nor 'Lutece' nor the names of its
 *     contributors may be used to endorse or promote products derived from
 *     this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * License 1.0
 */
package fr.paris.lutece.plugins.crm.modules.form.service.draft;

import fr.paris.lutece.portal.service.blobstore.BlobStoreService;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


public class InMemoryBlobStoreService implements BlobStoreService
{
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private Map<String, byte[]> _blobStore = new HashMap<String, byte[]>(  );

    public void delete( String strKey )
    {
        _blobStore.remove( strKey );
    }

    public byte[] getBlob( String strKey )
    {
        return _blobStore.get( strKey );
    }

    public InputStream getBlobInputStream( String strKey )
    {
        byte[] bValue = getBlob( strKey );

        if ( bValue != null )
        {
            return IOUtils.toInputStream( new String( bValue ) );
        }

        return null;
    }

    public String getBlobUrl( String strKey )
    {
        throw new UnsupportedOperationException(  );
    }

    public String getFileUrl( String strKey )
    {
        throw new UnsupportedOperationException(  );
    }

    public String getName(  )
    {
        return "InMemoryBlobStore - for testing purpose ONLY";
    }

    public void setName( String strName )
    {
        // nothing
    }

    public String store( byte[] blob )
    {
        String strKey = generateKey(  );
        _blobStore.put( strKey, blob );

        return strKey;
    }

    public String storeInputStream( InputStream inputStream )
    {
        try
        {
            return store( IOUtils.toByteArray( inputStream ) );
        }
        catch ( IOException e )
        {
            throw new RuntimeException( e );
        }
    }

    public void update( String strKey, byte[] blob )
    {
        _blobStore.put( strKey, blob );
    }

    public void updateInputStream( String strKey, InputStream inputStream )
    {
        try
        {
            update( strKey, IOUtils.toByteArray( inputStream ) );
        }
        catch ( IOException e )
        {
            throw new RuntimeException( e );
        }
    }

    /**
     * Generates an id
     * @return the id
     */
    private String generateKey(  )
    {
        return UUID.randomUUID(  ).toString(  );
    }
}
