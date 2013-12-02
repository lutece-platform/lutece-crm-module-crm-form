/*
 * Copyright (c) 2002-2013, Mairie de Paris
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

import fr.paris.lutece.plugins.crm.modules.form.service.CRMParametersService;
import fr.paris.lutece.plugins.crm.modules.form.service.ICRMParametersService;
import fr.paris.lutece.plugins.crm.modules.form.util.Constants;
import fr.paris.lutece.plugins.crmclient.service.ICRMClientService;
import fr.paris.lutece.plugins.crmclient.service.authenticator.IAuthenticatorService;
import fr.paris.lutece.plugins.crmclient.util.CRMException;
import fr.paris.lutece.plugins.crmclient.util.CrmClientConstants;
import fr.paris.lutece.plugins.form.business.Form;
import fr.paris.lutece.plugins.form.business.FormSubmit;
import fr.paris.lutece.plugins.form.service.draft.DraftBackupService;
import fr.paris.lutece.plugins.form.service.upload.FormAsynchronousUploadHandler;
import fr.paris.lutece.plugins.form.utils.FormUtils;
import fr.paris.lutece.plugins.form.utils.JSONUtils;
import fr.paris.lutece.plugins.genericattributes.business.Response;
import fr.paris.lutece.portal.service.blobstore.BlobStoreFileItem;
import fr.paris.lutece.portal.service.blobstore.BlobStoreService;
import fr.paris.lutece.portal.service.blobstore.NoSuchBlobException;
import fr.paris.lutece.portal.service.i18n.I18nService;
import fr.paris.lutece.portal.service.message.SiteMessage;
import fr.paris.lutece.portal.service.message.SiteMessageException;
import fr.paris.lutece.portal.service.message.SiteMessageService;
import fr.paris.lutece.portal.service.security.LuteceUser;
import fr.paris.lutece.portal.service.security.SecurityService;
import fr.paris.lutece.portal.service.util.AppException;
import fr.paris.lutece.portal.service.util.AppLogService;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import net.sf.json.JSONObject;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;


/**
 * CRM Draft Backup Service
 */
public class CRMDraftBackupService implements DraftBackupService
{
    private static Logger _logger = Logger.getLogger( "lutece.crm" );
    private BlobStoreService _blobStoreService;
    @Inject
    private ICRMParametersService _crmParametersService;
    @Inject
    private ICRMClientService _crmClientService;
    @Inject
    private IAuthenticatorService _authenticatorService;

    /**
     * Set the blobstore service of this draft backup service
     * @param blobStoreService The blobstore service
     */
    public void setBlobStoreService( BlobStoreService blobStoreService )
    {
        _blobStoreService = blobStoreService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean preProcessRequest( HttpServletRequest request, Form form ) throws SiteMessageException
    {
        if ( !isRequestAuthenticated( request, form ) )
        {

            SiteMessageService.setMessage( request, Constants.PROPERTY_MESSAGE_STOP_ACCESS_DENIED,
                    SiteMessage.TYPE_STOP );

        }

        // handle delete actions
        if ( draftAction( request ) )
        {
            return true;
        }

        //update session attributes 
        if ( !StringUtils.isEmpty( request.getParameter( Constants.PARAM_ID_DEMAND ) ) )
        {
            updateSessionAttributes( request, form );
        }

        // create if the draft does not exist
        else if ( !existsDraft( request, form ) )
        {
            create( request, form );
        }

        restore( request );

        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void saveDraft( HttpServletRequest request, Form form ) throws SiteMessageException
    {
        if ( _logger.isDebugEnabled( ) )
        {
            _logger.debug( "Saving Draft ..." );
        }

        HttpSession session = request.getSession( true );

        saveResponses( FormUtils.getResponses( session ), form.getIdForm( ), session );

        updateCRMStatus( request );
    }

    /**
     * Updates the CRM status to "in progress".
     * @param request the request
     */
    private void updateCRMStatus( HttpServletRequest request )
    {
        HttpSession session = request.getSession( );

        // get draft blob id
        String strKey = (String) session.getAttribute( Constants.SESSION_ATTRIBUTE_DEMAND_DATA_PARAMS );
        String strIdDemand = (String) session.getAttribute( Constants.SESSION_ATTRIBUTE_ID_DEMAND_PARAMS );
        String strCrmWebAppCode = (String) session
                .getAttribute( Constants.SESSION_ATTRIBUTE_DEMAND_CRM_WEBB_APP_CODE_PARAMS );
        String strStatusText = I18nService.getLocalizedString( Constants.PROPERTY_CRM_STATUS_TEXT_MODIF,
                request.getLocale( ) );

        if ( StringUtils.isNotBlank( strKey ) )
        {
            try
            {
                _crmClientService.sendUpdateDemand( strIdDemand, strStatusText, strCrmWebAppCode,
                        CrmClientConstants.CRM_STATUS_DRAFT, strKey );

            }
            catch ( CRMException e )
            {
                AppLogService.error( e );
            }
        }
        else
        {
            _logger.error( "No draft found" );
        }
    }

    /**
     * Saves the responses
     * @param mapResponses map response
     * @param nIdForm the id form
     * @param session the session
     */
    public void saveResponses( Map<Integer, List<Response>> mapResponses, int nIdForm, HttpSession session )
    {
        // get draft blob id
        String strKey = (String) session.getAttribute( Constants.SESSION_ATTRIBUTE_DEMAND_DATA_PARAMS );

        if ( StringUtils.isNotBlank( strKey ) )
        {
            storeFiles( mapResponses, session );

            String strJsonResponse = JSONUtils.buildJson( mapResponses, nIdForm, session.getId( ) );
            _blobStoreService.update( strKey, strJsonResponse.getBytes( ) );
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void saveDraft( HttpServletRequest request, FormSubmit formSubmit )
    {
        if ( _logger.isDebugEnabled( ) )
        {
            _logger.debug( "Saving formsubmit ..." );
        }

        HttpSession session = request.getSession( true );

        // build the map response
        Map<Integer, List<Response>> mapResponses = new HashMap<Integer, List<Response>>( );

        for ( Response response : formSubmit.getListResponse( ) )
        {
            int nIdEntry = response.getEntry( ).getIdEntry( );
            List<Response> listResponseEntry = mapResponses.get( nIdEntry );

            if ( listResponseEntry == null )
            {
                listResponseEntry = new ArrayList<Response>( );
                mapResponses.put( nIdEntry, listResponseEntry );
            }

            listResponseEntry.add( response );
        }

        saveResponses( mapResponses, formSubmit.getForm( ).getIdForm( ), session );

        updateCRMStatus( request );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void validateDraft( HttpServletRequest request, Form form )
    {
        if ( _logger.isDebugEnabled( ) )
        {
            _logger.debug( "Validating Draft ..." );
        }

        HttpSession session = request.getSession( true );

        String strKey = (String) session.getAttribute( Constants.SESSION_ATTRIBUTE_DEMAND_DATA_PARAMS );
        String strDemandId = (String) session.getAttribute( Constants.SESSION_ATTRIBUTE_ID_DEMAND_PARAMS );
        String strCrmWebAppCode = (String) session
                .getAttribute( Constants.SESSION_ATTRIBUTE_DEMAND_CRM_WEBB_APP_CODE_PARAMS );

        if ( StringUtils.isNotBlank( strKey ) )
        {
            String strStatusText = I18nService.getLocalizedString( Constants.PROPERTY_CRM_STATUS_TEXT_VALIDATE,
                    request.getLocale( ) );

            try
            {
                _crmClientService.sendUpdateDemand( strDemandId, strStatusText, strCrmWebAppCode,
                        CrmClientConstants.CRM_STATUS_VALIDATED, strKey );

            }
            catch ( CRMException e )
            {
                AppLogService.error( e );
            }
            byte[] dataForm = _blobStoreService.getBlob( strKey );

            if ( dataForm != null )
            {
                String strDataForm = new String( dataForm );
                deleteFiles( strDataForm );
            }

            _blobStoreService.delete( strKey );

            // Remove session attributes
            removeSessionAttributes( session );

        }
    }

    /**
     * Check if the draft exists
     * @param request the HTTP request
     * @param form The form
     * @return true if the draft already exists, false otherwise
     */
    private boolean existsDraft( HttpServletRequest request, Form form )
    {

        if ( request.getParameter( Constants.PARAM_DEMAND_DATA ) != null )
        {
            return true;
        }

        return false;

    }

    /**
     * Stores all file for the subform to BlobStore and replaces
     * {@link FileItem} by {@link BlobStoreFileItem}
     * @param mapResponses the map of <id_entry,Responses>
     * @param session the session
     */
    private void storeFiles( Map<Integer, List<Response>> mapResponses, HttpSession session )
    {
        for ( Entry<Integer, List<Response>> entryMap : mapResponses.entrySet( ) )
        {
            int nIdEntry = entryMap.getKey( );
            String strIdEntry = Integer.toString( nIdEntry );
            List<FileItem> uploadedFiles = FormAsynchronousUploadHandler.getHandler( ).getFileItems( strIdEntry,
                    session.getId( ) );

            if ( uploadedFiles != null )
            {
                List<FileItem> listBlobStoreFileItems = new ArrayList<FileItem>( );

                for ( int nIndex = 0; nIndex < uploadedFiles.size( ); nIndex++ )
                {
                    FileItem fileItem = uploadedFiles.get( nIndex );
                    String strFileName = fileItem.getName( );

                    if ( !( fileItem instanceof BlobStoreFileItem ) )
                    {
                        // file is not blobstored yet
                        String strFileBlobId;
                        InputStream is = null;

                        try
                        {
                            is = fileItem.getInputStream( );
                            strFileBlobId = _blobStoreService.storeInputStream( is );
                        }
                        catch ( IOException e1 )
                        {
                            IOUtils.closeQuietly( is );
                            _logger.error( e1.getMessage( ), e1 );
                            throw new AppException( e1.getMessage( ), e1 );
                        }

                        String strJSON = BlobStoreFileItem.buildFileMetadata( strFileName, fileItem.getSize( ),
                                strFileBlobId, fileItem.getContentType( ) );

                        if ( _logger.isDebugEnabled( ) )
                        {
                            _logger.debug( "Storing " + fileItem.getName( ) + " with : " + strJSON );
                        }

                        String strFileMetadataBlobId = _blobStoreService.store( strJSON.getBytes( ) );

                        try
                        {
                            BlobStoreFileItem blobStoreFileItem = new BlobStoreFileItem( strFileMetadataBlobId,
                                    _blobStoreService );
                            listBlobStoreFileItems.add( blobStoreFileItem );
                        }
                        catch ( NoSuchBlobException nsbe )
                        {
                            // nothing to do, blob is deleted and draft is not up to date.
                            if ( _logger.isDebugEnabled( ) )
                            {
                                _logger.debug( nsbe.getMessage( ) );
                            }
                        }
                        catch ( Exception e )
                        {
                            _logger.error( "Unable to create new BlobStoreFileItem " + e.getMessage( ), e );
                            throw new AppException( e.getMessage( ), e );
                        }
                    }
                    else
                    {
                        // nothing to do
                        listBlobStoreFileItems.add( fileItem );
                    }
                }

                // replace current file list with the new one
                uploadedFiles.clear( );
                uploadedFiles.addAll( listBlobStoreFileItems );
            }
        }
    }

    /**
     * Create a draft
     * @param request the HTTP request
     * @param form the form
     */
    private void create( HttpServletRequest request, Form form )
    {
        HttpSession session = request.getSession( true );

        String strDemandType = _crmParametersService.getIdTypeDemande( request, form );
        String strCrmWebAppCode = _crmParametersService.getCrmWebAppCode( request, form );

        if ( StringUtils.isNotBlank( strDemandType ) )
        {
            JSONObject json = new JSONObject( );
            json.element( JSONUtils.JSON_KEY_ID_FORM, form.getIdForm( ) );

            // the data is only the key - no need to store any other data
            String strData = _blobStoreService.store( json.toString( ).getBytes( ) );

            try
            {
                // save user info and demand to CRM
                String strIdCRMUser = request.getParameter( Constants.PARAM_ID_CRM_USER );
                String strUserGuid = StringUtils.EMPTY;
                String strIdDemand = StringUtils.EMPTY;

                if ( StringUtils.isBlank( strIdCRMUser ) && SecurityService.isAuthenticationEnable( ) )
                {
                    LuteceUser user = SecurityService.getInstance( ).getRemoteUser( request );

                    if ( user != null )
                    {
                        strUserGuid = user.getName( );
                    }
                }

                String strStatusText = I18nService.getLocalizedString( Constants.PROPERTY_CRM_STATUS_TEXT_NEW,
                        request.getLocale( ) );

                if ( StringUtils.isNotBlank( strUserGuid ) )
                {
                    strIdDemand = _crmClientService.sendCreateDemandByUserGuid( strDemandType, strUserGuid,
                            CrmClientConstants.CRM_STATUS_DRAFT, strStatusText, strData, strCrmWebAppCode );

                }
                else if ( StringUtils.isNotBlank( strIdCRMUser ) )
                {
                    strIdDemand = _crmClientService.sendCreateDemandByIdCRMUser( strDemandType, strIdCRMUser,
                            CrmClientConstants.CRM_STATUS_DRAFT, strStatusText, strData, strCrmWebAppCode );
                }

                if ( StringUtils.isNotBlank( strIdDemand ) && !Constants.INVALID_ID.equals( strIdDemand ) )
                {

                    try
                    {
                        strUserGuid = _crmClientService.getUserGuidFromIdDemand( strIdDemand, strCrmWebAppCode );
                    }
                    catch ( CRMException ex )
                    {
                        _logger.error( "Error calling WebService : " + ex.getMessage( ), ex );
                    }
                    updateSessionAttributes( session, strIdDemand, strData, strUserGuid, strCrmWebAppCode );
                }
                else
                {
                    throw new Exception( "Invalid ID demand" );
                }
            }
            catch ( Exception e )
            {
                _logger.error( "Error calling WebService : " + e.getMessage( ), e );

                // Remove the blob created previously
                _blobStoreService.delete( strData );
            }
        }
    }

    /**
     * Restore a draft
     * @param request the HTTP request
     */
    private void restore( HttpServletRequest request )
    {
        if ( _logger.isDebugEnabled( ) )
        {
            _logger.debug( "Restoring Draft ..." );
        }

        HttpSession session = request.getSession( true );

        String strData = ( (String) session.getAttribute( Constants.SESSION_ATTRIBUTE_DEMAND_DATA_PARAMS ) );

        if ( StringUtils.isNotBlank( strData ) )
        {
            byte[] dataForm = _blobStoreService.getBlob( strData );

            if ( dataForm != null )
            {
                String strDataForm = new String( dataForm );

                if ( StringUtils.isNotBlank( strDataForm ) )
                {
                    // bind responses to session if jsonresponse has content - use default otherwise.
                    Map<Integer, List<Response>> mapResponses = JSONUtils.buildListResponses( strDataForm,
                            request.getLocale( ), session );

                    if ( mapResponses != null )
                    {
                        if ( _logger.isDebugEnabled( ) )
                        {
                            _logger.debug( "Found reponses - restoring form" );
                        }

                        FormUtils.restoreResponses( session, mapResponses );

                        for ( Entry<Integer, List<Response>> entryMap : mapResponses.entrySet( ) )
                        {
                            int nIdEntry = entryMap.getKey( );
                            List<String> listBlobIds = JSONUtils.getBlobIds( strDataForm, nIdEntry );

                            if ( ( listBlobIds != null ) && !listBlobIds.isEmpty( ) )
                            {
                                for ( String strBlobId : listBlobIds )
                                {
                                    FileItem fileItem;

                                    try
                                    {
                                        fileItem = new BlobStoreFileItem( strBlobId, _blobStoreService );
                                        FormAsynchronousUploadHandler.getHandler( ).addFileItemToUploadedFile(
                                                fileItem, Integer.toString( nIdEntry ), session );
                                    }
                                    catch ( NoSuchBlobException nsbe )
                                    {
                                        // file might be deleted
                                        _logger.debug( nsbe.getMessage( ) );
                                    }
                                    catch ( Exception e )
                                    {
                                        throw new AppException( "Unable to parse JSON file metadata for blob id "
                                                + strBlobId + " : " + e.getMessage( ), e );
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        else
        {
            AppLogService.error( "No blob id found for the current session" );
        }
    }

    /**
     * Delete a draft
     * @param request the HTTP request
     * @return <code>true</code> if an error occurs, <code>false</code>
     *         otherwise
     */
    private boolean delete( HttpServletRequest request )
    {
        if ( _logger.isDebugEnabled( ) )
        {
            _logger.debug( "Deleting Draft ..." );
        }

        boolean bHasError = false;

        String strIdDemand = request.getParameter( Constants.PARAM_ID_DEMAND );
        String strData = request.getParameter( Constants.PARAM_DEMAND_DATA );
        String strCrmWebAppCode = request.getParameter( Constants.PARAM_CRM_WEBB_APP_CODE );

        if ( StringUtils.isNotBlank( strIdDemand ) && StringUtils.isNumeric( strIdDemand )
                && StringUtils.isNotBlank( strData ) )
        {
            try
            {
                // Delete the demand in CRM
                _crmClientService.sendDeleteDemand( strIdDemand, strCrmWebAppCode );

                byte[] dataForm = _blobStoreService.getBlob( strData );

                if ( dataForm != null )
                {
                    String strDataForm = new String( dataForm );
                    deleteFiles( strDataForm );
                }

                // Delete the demand in Blobstore
                _blobStoreService.delete( strData );
            }
            catch ( CRMException ex )
            {
                _logger.error( "Error deleting draft : " + ex.getMessage( ), ex );
                bHasError = true;
            }
        }
        else
        {
            bHasError = true;
        }

        return bHasError;
    }

    /**
     * Removes all files stored in blobstore for the current subform and
     * strJsonFields.
     * @param strDataForm the data form
     */
    private void deleteFiles( String strDataForm )
    {
        List<String> listBlobIds = JSONUtils.getBlobIds( strDataForm );

        if ( ( listBlobIds != null ) && !listBlobIds.isEmpty( ) )
        {
            for ( String strBlobId : listBlobIds )
            {
                FileItem fileItem;

                try
                {
                    fileItem = new BlobStoreFileItem( strBlobId, _blobStoreService );

                    if ( _logger.isDebugEnabled( ) )
                    {
                        _logger.debug( "Removing file " + fileItem.getName( ) );
                    }

                    fileItem.delete( );
                }
                catch ( NoSuchBlobException nsbe )
                {
                    // file might be deleted
                    if ( _logger.isDebugEnabled( ) )
                    {
                        _logger.debug( nsbe.getMessage( ) );
                    }
                }
                catch ( Exception e )
                {
                    throw new AppException( "Unable to parse JSON file metadata for blob id " + strBlobId + " : "
                            + e.getMessage( ), e );
                }
            }
        }
    }

    /**
     * Do draft action
     * @param request the HTTP request
     * @return true if there is an draft action, false otherwise
     * @throws SiteMessageException message exception if remove draft
     */
    private boolean draftAction( HttpServletRequest request ) throws SiteMessageException
    {
        String strAction = request.getParameter( Constants.PARAMETER_ACTION_NAME );

        if ( StringUtils.isNotBlank( strAction ) )
        {
            if ( Constants.ACTION_DO_REMOVE_DRAFT.equals( strAction ) )
            {
                doRemoveDraft( request );
            }
            else if ( Constants.ACTION_REMOVE_DRAFT.equals( strAction ) )
            {
                removeDraft( request );
            }

            return true;
        }

        return false;
    }

    /**
     * Do remove a demand by calling the DraftBackUpService
     * @param request The HTTP request
     */
    private void doRemoveDraft( HttpServletRequest request )
    {
        delete( request );
    }

    /**
     * Remove a draft and display a message saying the draft has or not been
     * deleted
     * @param request The HTTP request
     * @throws SiteMessageException the message exception
     */
    private void removeDraft( HttpServletRequest request ) throws SiteMessageException
    {
        String strUrlReturn = request.getParameter( Constants.PARAMETER_URL_RETURN );

        if ( StringUtils.isNotBlank( strUrlReturn ) )
        {
            if ( delete( request ) )
            {
                SiteMessageService.setMessage( request, Constants.PROPERTY_MESSAGE_ERROR_CALLING_WS,
                        SiteMessage.TYPE_ERROR, strUrlReturn );
            }
            else
            {
                SiteMessageService.setMessage( request, Constants.PROPERTY_MESSAGE_INFO_REMOVE_DEMAND,
                        SiteMessage.TYPE_INFO, strUrlReturn );
            }
        }
    }

    /**
     * Check if the request is authenticated
     * @param request the HTTP request
     * @return true if it is authenticated, false otherwise
     */
    private boolean isRequestAuthenticated( HttpServletRequest request, Form form )
    {
        boolean bIsAuthenticated = true;
        String strDemandType = _crmParametersService.getIdTypeDemande( request, form );
        String strCrmWebAppCode = _crmParametersService.getCrmWebAppCode( request, form );
        String strDemand = request.getParameter( Constants.PARAM_ID_DEMAND );
        String strAction = request.getParameter( Constants.PARAMETER_ACTION_NAME );
        if ( StringUtils.isNotBlank( strAction ) && Constants.ACTION_DO_REMOVE_DRAFT.equals( strAction ) )
        {

            bIsAuthenticated = _authenticatorService.getRequestAuthenticatorForWs( strCrmWebAppCode )
                    .isRequestAuthenticated( request );
        }
        else if ( !_crmParametersService.isEnabledLocalCrmParameters( )
                && ( StringUtils.isNotBlank( strDemandType ) || StringUtils.isNotBlank( strDemand ) )
                || ( StringUtils.isNotBlank( strAction ) && Constants.ACTION_REMOVE_DRAFT.equals( strAction ) ) )
        {
            bIsAuthenticated = _authenticatorService.getRequestAuthenticatorForUrl( strCrmWebAppCode )
                    .isRequestAuthenticated( request );
        }

        return bIsAuthenticated;
    }

    /**
     * Update session attributes
     * @param request the HTTP request
     * @param form the form object
     * 
     */
    private void updateSessionAttributes( HttpServletRequest request, Form form )
    {
        HttpSession session = request.getSession( true );
        String strIdDemand = request.getParameter( Constants.PARAM_ID_DEMAND );
        String strCrmWebAppCode = _crmParametersService.getCrmWebAppCode( request, form );
        String strUserGuid = null;
        try
        {
            strUserGuid = _crmClientService.getUserGuidFromIdDemand( strIdDemand, strCrmWebAppCode );
        }
        catch ( CRMException ex )
        {
            _logger.error( "Error calling WebService : " + ex.getMessage( ), ex );
        }

        String strDemandData = request.getParameter( Constants.PARAM_DEMAND_DATA );
        updateSessionAttributes( session, strIdDemand, strDemandData, strUserGuid, strCrmWebAppCode );
    }

    /**
     * Update session attributes
     * @param session the Http session
     * @param strIdDemand the demand id
     * @param strDemandData the demad data
     * @param strUserGuid the user guid
     * @param strCrmWebAppCode the web app code
     */
    private void updateSessionAttributes( HttpSession session, String strIdDemand, String strDemandData,
            String strUserGuid, String strCrmWebAppCode )
    {
        if ( !StringUtils.isEmpty( strIdDemand ) )
        {
            session.setAttribute( Constants.SESSION_ATTRIBUTE_ID_DEMAND_PARAMS, strIdDemand );
        }
        if ( !StringUtils.isEmpty( strDemandData ) )
        {
            session.setAttribute( Constants.SESSION_ATTRIBUTE_DEMAND_DATA_PARAMS, strDemandData );
        }
        if ( !StringUtils.isEmpty( strUserGuid ) )
        {
            session.setAttribute( Constants.SESSION_ATTRIBUTE_USER_GUID_PARAMS, strUserGuid );
        }
        if ( !StringUtils.isEmpty( strCrmWebAppCode ) )
        {
            session.setAttribute( Constants.SESSION_ATTRIBUTE_DEMAND_CRM_WEBB_APP_CODE_PARAMS, strCrmWebAppCode );
        }
    }

    /**
     * Remove the session attributes
     * @param session the session
     */
    private void removeSessionAttributes( HttpSession session )
    {
        session.removeAttribute( Constants.SESSION_ATTRIBUTE_ID_DEMAND_PARAMS );
        session.removeAttribute( Constants.SESSION_ATTRIBUTE_DEMAND_DATA_PARAMS );
        session.removeAttribute( Constants.SESSION_ATTRIBUTE_DEMAND_CRM_WEBB_APP_CODE_PARAMS );
        session.removeAttribute( Constants.SESSION_ATTRIBUTE_USER_GUID_PARAMS );
    }

    /**
     * set _crmParametersService
     * @param _crmParametersService _crmParametersService
     */
    public void setCrmParametersService( CRMParametersService crmParametersService )
    {
        this._crmParametersService = crmParametersService;
    }

}
