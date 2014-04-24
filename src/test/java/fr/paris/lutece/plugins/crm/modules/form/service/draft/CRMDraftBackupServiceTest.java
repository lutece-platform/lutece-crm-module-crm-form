/*
 * Copyright (c) 2002-2014, Mairie de Paris
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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.paris.lutece.plugins.blobstore.service.IBlobStoreService;
import fr.paris.lutece.plugins.crm.modules.form.util.Constants;
import fr.paris.lutece.plugins.form.utils.JSONUtils;
import fr.paris.lutece.plugins.genericattributes.business.Entry;
import fr.paris.lutece.plugins.genericattributes.business.EntryType;
import fr.paris.lutece.plugins.genericattributes.business.Response;
import fr.paris.lutece.test.LuteceTestCase;
import fr.paris.lutece.test.MokeHttpSession;


public class CRMDraftBackupServiceTest extends LuteceTestCase
{
    public void testSaveResponses( )
    {
        CRMDraftBackupService backupService = new CRMDraftBackupService( );
        IBlobStoreService blobStore = new InMemoryBlobStoreService( );
        backupService.setBlobStoreService( blobStore );

        // create an entry
        String strKey = blobStore.store( new byte[0] );

        int nIdForm = 1;

        Map<Integer, List<Response>> mapResponses = buildResponseMap( );
        MokeHttpSession session = new MokeHttpSession( );
        session.setAttribute( Constants.SESSION_ATTRIBUTE_DEMAND_DATA_PARAMS, strKey );

        byte[] bShouldStore = JSONUtils.buildJson( mapResponses, nIdForm, session.getId( ) ).getBytes( );

        backupService.saveResponses( mapResponses, nIdForm, session );

        // get the blob to check values
        byte[] bStored = blobStore.getBlob( strKey );

        assertTrue( Arrays.equals( bShouldStore, bStored ) );
    }

    private Map<Integer, List<Response>> buildResponseMap( )
    {
        Map<Integer, List<Response>> mapResponses = new HashMap<Integer, List<Response>>( );

        // create a minimal test
        EntryType entryType = new EntryType( );
        entryType.setBeanName( "form.entryTypeText" );

        Response response1 = new Response( );
        Entry entry1 = new Entry( );
        entry1.setIdEntry( 1 );
        entry1.setEntryType( entryType );
        response1.setEntry( entry1 );
        response1.setResponseValue( "value1" );
        mapResponses.put( Integer.valueOf( 1 ), Collections.singletonList( response1 ) );

        Response response2 = new Response( );
        Entry entry2 = new Entry( );
        entry2.setIdEntry( 2 );
        entry2.setEntryType( entryType );
        response2.setEntry( entry2 );
        response2.setResponseValue( "value2" );
        mapResponses.put( Integer.valueOf( 2 ), Collections.singletonList( response2 ) );

        return mapResponses;
    }
}
