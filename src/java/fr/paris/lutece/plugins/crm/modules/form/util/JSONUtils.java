package fr.paris.lutece.plugins.crm.modules.form.util;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpSession;

import fr.paris.lutece.plugins.form.service.upload.FormAsynchronousUploadHandler;
import fr.paris.lutece.plugins.genericattributes.business.GenericAttributeError;
import fr.paris.lutece.plugins.genericattributes.business.Entry;
import fr.paris.lutece.plugins.genericattributes.business.EntryHome;
import fr.paris.lutece.plugins.genericattributes.business.Response;
import fr.paris.lutece.plugins.genericattributes.business.Field;
import fr.paris.lutece.plugins.genericattributes.business.FieldHome;
import fr.paris.lutece.plugins.genericattributes.service.entrytype.IEntryTypeService;
import fr.paris.lutece.plugins.genericattributes.util.GenericAttributesUtils;
import fr.paris.lutece.plugins.genericattributes.service.entrytype.EntryTypeServiceManager;
import fr.paris.lutece.plugins.blobstore.service.BlobStoreFileItem;
import fr.paris.lutece.portal.service.util.AppLogService;
import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;

import fr.paris.lutece.portal.business.file.File;

public final class JSONUtils {
	
	public static final String JSON_KEY_RESPONSE = "response";
	public static final String JSON_KEY_ID_FORM = "id_form";
	public static final String JSON_KEY_ID_ENTRY = "id_entry";
    public static final String JSON_KEY_ID_RESPONSE = "id_response";
    public static final String JSON_KEY_ID_FIELD = "id_field";
    public static final String JSON_KEY_VALUE_RESPONSE = "value_response";
    public static final String JSON_KEY_FILE_NAME = "file_name";
    public static final String JSON_KEY_FILE_EXTENSION = "file_extension";
    public static final String JSON_KEY_ERROR_MESSAGE = "error_message";
    public static final String JSON_KEY_MANDATORY_ERROR = "mandatory_error";
    public static final String JSON_KEY_TITLE_QUESTION = "title_question";
    public static final String JSON_KEY_FORM_ERROR = "form_error";
    public static final String JSON_KEY_MIME_TYPE = "mime_type";
   
   
 
	
	/**
     * Empty constructor
     */
    private JSONUtils( )
    {
        // nothing
    }
    /**
     * Builds the json string for the response map
     * @param mapResponse the response map
     * @param nIdForm the id form
     * @param session the session
     * @return the json string
     */
    public static String buildJson( Map<Integer, List<Response>> mapResponse, int nIdForm, HttpSession session )
    {
        JSONObject jsonResponses = new JSONObject( );

        for ( List<Response> listResponse : mapResponse.values( ) )
        {
            for ( Response response : listResponse )
            {
                jsonResponses.accumulate( JSON_KEY_RESPONSE, buildJson( response, session ));
            }
        }

        jsonResponses.element( JSON_KEY_ID_FORM, nIdForm );

        return jsonResponses.toString( );
    }
    
    /**
     * Builds the json string for the {@link Response}
     * @param response the response
     * @param session the session
     * @return the json string
     */
    public static JSONObject buildJson( Response response, HttpSession session )
    {
        JSONObject jsonResponse = new JSONObject( );
        jsonResponse.element( JSON_KEY_ID_ENTRY, response.getEntry( ).getIdEntry( ) );
        jsonResponse.element( JSON_KEY_ID_RESPONSE, response.getIdResponse( ) );

        if ( response.getField( ) != null )
        {
            jsonResponse.element( JSON_KEY_ID_FIELD, response.getField( ).getIdField( ) );
        }

        if ( ( response.getResponseValue( ) != null ) && ( response.getFile( ) != null ) )
        {
            jsonResponse.element( JSON_KEY_VALUE_RESPONSE, response.getResponseValue( ) );
        }
        else
        {
            // file specific data
            if ( ( response.getFile( ) != null ) && StringUtils.isNotBlank( response.getFile( ).getTitle( ) ) )
            {
                jsonResponse.element( JSON_KEY_FILE_NAME, response.getFile( ).getTitle( ) );
                jsonResponse.element( JSON_KEY_FILE_EXTENSION,
                        FilenameUtils.getExtension( response.getFile( ).getTitle( ) ) );
                jsonResponse.element( JSON_KEY_MIME_TYPE, response.getFile( ).getMimeType( ) );

                List<FileItem> listFileItems = FormAsynchronousUploadHandler.getHandler( ).getListUploadedFiles(
                		IEntryTypeService.PREFIX_ATTRIBUTE +Integer.toString( response.getEntry( ).getIdEntry( ) ), session );

                if ( ( listFileItems != null ) && !listFileItems.isEmpty( ) )
                {
                    for ( FileItem fileItem : listFileItems )
                    {
                        if ( fileItem instanceof BlobStoreFileItem
                                && fileItem.getName( ).equals( response.getFile( ).getTitle( ) ) )
                        {
                            jsonResponse.accumulate( BlobStoreFileItem.JSON_KEY_FILE_METADATA_BLOB_ID,
                                    ( (BlobStoreFileItem) fileItem ).getBlobId( ) );

                            break;
                        }
                    }
                }
            }
        }

        // form error
        if ( response.getEntry( ).getError( ) != null )
        {
            jsonResponse.element( JSON_KEY_FORM_ERROR, buildJson( response.getEntry( ).getError( ) ) );
        }

        return jsonResponse;
    }
    /**
     * Builds json form {@link GenericAttributeError}
     * @param formError {@link GenericAttributeError}
     * @return json string
     */
    public static String buildJson( GenericAttributeError formError )
    {
        JSONObject jsonError = new JSONObject( );

        jsonError.element( JSON_KEY_ERROR_MESSAGE,
                StringUtils.isNotBlank( formError.getErrorMessage( ) ) ? formError.getErrorMessage( )
                        : StringUtils.EMPTY );
        jsonError.element( JSON_KEY_MANDATORY_ERROR, formError.isMandatoryError( ) );
        jsonError.element( JSON_KEY_TITLE_QUESTION, formError.getTitleQuestion( ) );

        return jsonError.toString( );
    }
    
    /**
     * Get the list of blob id from a JSON string
     * @param strJSON The JSON to get blob id from
     * @param nIdEntry The id of the entry to get blob id from, or
     *            {@link FormUtils#CONSTANT_ID_NULL} to get blob id from any
     *            entry
     * @return The list of blob id, or an empty list if no blob id was found
     *         in the JSON string
     */
    public static List<String> getBlobIds( String strJSON, int nIdEntry )
    {
        List<String> listBlobIds = new ArrayList<String>( );
        JSONObject jsonObject = JSONObject.fromObject( strJSON );

        try
        {
            JSON jsonResponses = (JSON) jsonObject.get( JSON_KEY_RESPONSE );

            if ( ( jsonResponses != null ) && !jsonResponses.isEmpty( ) )
            {
                if ( jsonResponses.isArray( ) )
                {
                    // array
                    for ( JSONObject jsonResponse : ( (Collection<JSONObject>) ( (JSONArray) jsonResponses ) ) )
                    {
                        if ( ( ( nIdEntry == GenericAttributesUtils.CONSTANT_ID_NULL ) || ( nIdEntry == jsonResponse
                                .getInt( JSON_KEY_ID_ENTRY ) ) )
                                && jsonResponse.containsKey( BlobStoreFileItem.JSON_KEY_FILE_METADATA_BLOB_ID ) )
                        {
                            listBlobIds.addAll( getFileMetadataBlobIdsFromJson( jsonResponse ) );
                        }
                    }
                }
                else
                {
                    // only one response ?
                    JSONObject jsonResponse = (JSONObject) jsonResponses;

                    if ( ( ( nIdEntry == jsonResponse.getInt( JSON_KEY_ID_ENTRY ) ) || ( nIdEntry == GenericAttributesUtils.CONSTANT_ID_NULL ) )
                            && jsonResponse.containsKey( BlobStoreFileItem.JSON_KEY_FILE_METADATA_BLOB_ID ) )
                    {
                        listBlobIds.addAll( getFileMetadataBlobIdsFromJson( jsonResponse ) );
                    }
                }
            }
        }
        catch ( JSONException jsonEx )
        {
            // nothing to do - response might no be present
        }

        return listBlobIds;
    }
    
    /**
     * Get the list of blob id from a JSON string
     * @param strJSON The JSON to get blob id from
     * @return The list of blob id, or an empty list if no blob id was found
     *         in the JSON string
     */
    public static List<String> getBlobIds( String strJSON )
    {
        return getBlobIds( strJSON, GenericAttributesUtils.CONSTANT_ID_NULL );
    }

   
    /**
     * Gets blobs id for files metadata
     * @param strJSONFields the strJSONFields
     * @param field the field
     * @return blob ids found, empty list otherwise.
     */
    private static Collection<String> getFileMetadataBlobIdsFromJson( JSONObject json )
    {
        Object oMetadata = json.get( BlobStoreFileItem.JSON_KEY_FILE_METADATA_BLOB_ID );

        if ( oMetadata == null )
        {
            return Collections.emptyList( );
        }

        if ( oMetadata instanceof JSONArray )
        {
            return (Collection<String>) oMetadata;
        }

        return Collections.singletonList( oMetadata.toString( ) );
    }
    
    /**
     * Builds {@link GenericAttributeError} from json string
     * @param strJson json string
     * @return the {@link GenericAttributeError}
     */
    public static GenericAttributeError buildFormError( String strJson )
    {
        JSONObject jsonObject = JSONObject.fromObject( strJson );
        GenericAttributeError formError = new GenericAttributeError( );
        formError.setErrorMessage( jsonObject.getString( JSON_KEY_ERROR_MESSAGE ) );
        formError.setMandatoryError( jsonObject.getBoolean( JSON_KEY_MANDATORY_ERROR ) );
        formError.setTitleQuestion( jsonObject.getString( JSON_KEY_TITLE_QUESTION ) );

        return formError;
    }
    /**
     * Builds the response
     * @param json the json
     * @param locale the locale
     * @param session the session
     * @return response the response
     */
    private static Response buildResponse( JSONObject json, Locale locale, HttpSession session )
    {
        Response response = new Response( );
        response.setIdResponse( json.getInt( JSON_KEY_ID_RESPONSE ) );

        Entry entry = EntryHome.findByPrimaryKey( json.getInt( JSON_KEY_ID_ENTRY ) );
        response.setEntry( entry );

        if ( json.containsKey( JSON_KEY_FORM_ERROR ) )
        {
            response.getEntry( ).setError( buildFormError( json.getString( JSON_KEY_FORM_ERROR ) ) );
        }

        if ( json.containsKey( JSON_KEY_VALUE_RESPONSE ) && !json.containsKey( JSON_KEY_FILE_NAME ) )
        {
            response.setResponseValue( json.getString( JSON_KEY_VALUE_RESPONSE ) );
        }

        if ( json.containsKey( JSON_KEY_ID_FIELD ) )
        {
            Field field = FieldHome.findByPrimaryKey( json.getInt( JSON_KEY_ID_FIELD ) );
            response.setField( field );
        }

        // file specific
        boolean bIsFile = false;

        if ( json.containsKey( JSON_KEY_FILE_NAME ) )
        {
            File file = null;

            try
            {
                file = new File( );
                file.setTitle( json.getString( JSON_KEY_FILE_NAME ) );
                file.setMimeType( json.getString( JSON_KEY_MIME_TYPE ) );
            }
            catch ( JSONException e )
            {
                AppLogService.error( e.getMessage( ), e );
            }

            response.setFile( file );
            bIsFile = true;
        }

        if ( !bIsFile && ( response.getResponseValue( ) != null ) )
        {
            // if the entry is not a file, we can set the string value
            // data entry as specific behavior
            EntryTypeServiceManager.getEntryTypeService( entry ).setResponseToStringValue( entry, response, locale );
        }

        return response;
    }

    
    /**
     * Builds the responses list - null if {@link #JSON_KEY_RESPONSE} is
     * missing.
     * @param strJSON the json
     * @param locale the locale
     * @param session the session
     * @return the responses list - null if {@link #JSON_KEY_RESPONSE} is
     *         missing
     */
    @SuppressWarnings( "unchecked" )
    public static Map<Integer, List<Response>> buildListResponses( String strJSON, Locale locale, HttpSession session )
    {
        Map<Integer, List<Response>> mapResponses;
        JSONObject jsonObject = JSONObject.fromObject( strJSON );

        try
        {
            JSON jsonResponses = (JSON) jsonObject.get( JSON_KEY_RESPONSE );

            if ( ( jsonResponses != null ) && !jsonResponses.isEmpty( ) )
            {
                // there is at least one result
                mapResponses = new HashMap<Integer, List<Response>>( );

                if ( jsonResponses.isArray( ) )
                {
                    // array
                    for ( JSONObject jsonResponse : ( (Collection<JSONObject>) ( (JSONArray) jsonResponses ) ) )
                    {
                        Response response = buildResponse( jsonResponse, locale, session );
                        List<Response> listResponses = mapResponses.get( response.getEntry( ).getIdEntry( ) );

                        if ( listResponses == null )
                        {
                            listResponses = new ArrayList<Response>( );
                            mapResponses.put( response.getEntry( ).getIdEntry( ), listResponses );
                        }

                        listResponses.add( response );
                    }
                }
                else
                {
                    // only one response ?
                    JSONObject jsonResponse = (JSONObject) jsonResponses;

                    Response response = buildResponse( jsonResponse, locale, session );

                    List<Response> listResponses = new ArrayList<Response>( );
                    listResponses.add( response );
                    mapResponses.put( response.getEntry( ).getIdEntry( ), listResponses );
                }
            }
            else
            {
                // nothing to do - no response found
                mapResponses = null;
            }
        }
        catch ( JSONException jsonEx )
        {
            // nothing to do - response might no be present
            mapResponses = null;
        }

        return mapResponses;
    }
    
   

}
