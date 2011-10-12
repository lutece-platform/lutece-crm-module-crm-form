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
package fr.paris.lutece.plugins.crm.modules.form.service;

import fr.paris.lutece.plugins.crm.modules.form.service.signrequest.CRMRequestAuthenticatorService;
import fr.paris.lutece.plugins.crm.modules.form.util.Constants;
import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;
import fr.paris.lutece.util.httpaccess.HttpAccess;
import fr.paris.lutece.util.httpaccess.HttpAccessException;
import fr.paris.lutece.util.url.UrlItem;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 *
 * CRMWebServices
 *
 */
public final class CRMWebServices
{
    /**
     * CRMWebServices
     */
    private CRMWebServices(  )
    {
    }

    /**
     * This method calls Rest WS to create a new demand and gets the id demand
     * @param strIdDemandType id of demand type
     * @param strUserGuid login of user auth
     * @param strIdStatusCRM id of CRM status
     * @param strStatusText status text
     * @param strData data
     * @return id demand
     * @throws HttpAccessException the exception if there is a problem
     */
    public static String sendDemandCreateByUserGuid( String strIdDemandType, String strUserGuid, String strIdStatusCRM,
        String strStatusText, String strData ) throws HttpAccessException
    {
        String strUrl = AppPropertiesService.getProperty( Constants.PROPERTY_WEBSERVICE_CRM_REST_URL ) +
            Constants.URL_REST_DEMAND_CREATE_BY_USER_GUID;

        // List parameters to post
        Map<String, String> params = new HashMap<String, String>(  );
        params.put( Constants.PARAM_ID_DEMAND_TYPE, strIdDemandType );
        params.put( Constants.PARAM_USER_GUID, strUserGuid );
        params.put( Constants.PARAM_ID_STATUS_CRM, strIdStatusCRM );
        params.put( Constants.PARAM_STATUS_TEXT, strStatusText );
        params.put( Constants.PARAM_DEMAND_DATA, strData );

        // List elements to include to the signature
        List<String> listElements = new ArrayList<String>(  );
        listElements.add( strIdDemandType );
        listElements.add( strUserGuid );
        listElements.add( strIdStatusCRM );
        listElements.add( strStatusText );
        listElements.add( strData );

        return sendDemandAction( strUrl, params, listElements );
    }

    /**
     * This method calls Rest WS to create a new demand and get id demand
     * @param strIdDemandType id of demand type
     * @param strIdCRMUser the id crm user
     * @param strIdStatusCRM id of CRM status
     * @param strStatusText status text
     * @param strData data
     * @return id demand
     * @throws HttpAccessException the exception if there is a problem
     */
    public static String sendDemandCreateByIdCRMUser( String strIdDemandType, String strIdCRMUser,
        String strIdStatusCRM, String strStatusText, String strData )
        throws HttpAccessException
    {
        String strUrl = AppPropertiesService.getProperty( Constants.PROPERTY_WEBSERVICE_CRM_REST_URL ) +
            Constants.URL_REST_DEMAND_CREATE_BY_ID_CRM_USER;

        // List parameters to post
        Map<String, String> params = new HashMap<String, String>(  );
        params.put( Constants.PARAM_ID_DEMAND_TYPE, strIdDemandType );
        params.put( Constants.PARAM_ID_CRM_USER, strIdCRMUser );
        params.put( Constants.PARAM_ID_STATUS_CRM, strIdStatusCRM );
        params.put( Constants.PARAM_STATUS_TEXT, strStatusText );
        params.put( Constants.PARAM_DEMAND_DATA, strData );

        // List elements to include to the signature
        List<String> listElements = new ArrayList<String>(  );
        listElements.add( strIdDemandType );
        listElements.add( strIdCRMUser );
        listElements.add( strIdStatusCRM );

        return sendDemandAction( strUrl, params, listElements );
    }

    /**
     * This method calls Rest WS to update a demand
     * @param strIdDemand id of demand
     * @param strIdStatusCRM id of CRM status
     * @param strStatusText status text
     * @param strData data
     * @throws HttpAccessException the exception if there is a problem
     */
    public static void sendDemandUpdate( String strIdDemand, String strIdStatusCRM, String strStatusText, String strData )
        throws HttpAccessException
    {
        String strUrl = AppPropertiesService.getProperty( Constants.PROPERTY_WEBSERVICE_CRM_REST_URL ) +
            Constants.URL_REST_DEMAND_UPDATE;

        // List parameters to post
        Map<String, String> params = new HashMap<String, String>(  );
        params.put( Constants.PARAM_ID_DEMAND, strIdDemand );
        params.put( Constants.PARAM_ID_STATUS_CRM, strIdStatusCRM );
        params.put( Constants.PARAM_STATUS_TEXT, strStatusText );
        params.put( Constants.PARAM_DEMAND_DATA, strData );

        // List elements to include to the signature
        List<String> listElements = new ArrayList<String>(  );
        listElements.add( strIdDemand );
        listElements.add( strIdStatusCRM );

        sendDemandAction( strUrl, params, listElements );
    }

    /**
     * This method calls Rest WS to delete a demand
     * @param strIdDemand the id of the demand
     * @throws HttpAccessException the exception if there is a problem
     */
    public static void sendDemandDelete( String strIdDemand )
        throws HttpAccessException
    {
        String strUrl = AppPropertiesService.getProperty( Constants.PROPERTY_WEBSERVICE_CRM_REST_URL ) +
            Constants.URL_REST_DEMAND_DELETE;

        // List parameters to post
        Map<String, String> params = new HashMap<String, String>(  );
        params.put( Constants.PARAM_ID_DEMAND, strIdDemand );

        // List elements to include to the signature
        List<String> listElements = new ArrayList<String>(  );
        listElements.add( strIdDemand );

        sendDemandAction( strUrl, params, listElements );
    }

    /**
     * This method calls Rest WS to get the user guid from a given id demand
     * @param strIdDemand the id demand
     * @return the user guid
     * @throws HttpAccessException the exception if there is a problem
     */
    public static String getUserGuidFromIdDemand( String strIdDemand )
        throws HttpAccessException
    {
        StringBuilder sbUrl = new StringBuilder( AppPropertiesService.getProperty( 
                    Constants.PROPERTY_WEBSERVICE_CRM_REST_URL ) );
        sbUrl.append( Constants.URL_REST_DEMAND );
        sbUrl.append( strIdDemand );
        sbUrl.append( Constants.URL_REST_DEMAND_USER_GUID );

        UrlItem url = new UrlItem( sbUrl.toString(  ) );
        url.addParameter( Constants.PARAM_ID_DEMAND, strIdDemand );

        // List elements to include to the signature
        List<String> listElements = new ArrayList<String>(  );
        listElements.add( strIdDemand );

        String strResponse = StringUtils.EMPTY;

        try
        {
            HttpAccess httpAccess = new HttpAccess(  );
            strResponse = httpAccess.doGet( url.getUrl(  ),
                    CRMRequestAuthenticatorService.getRequestAuthenticatorForWS(  ), listElements );
        }
        catch ( HttpAccessException e )
        {
            String strError = "CRMWebServices - Error connecting to '" + url.getUrl(  ) + "' : ";
            AppLogService.error( strError + e.getMessage(  ), e );
            throw new HttpAccessException( strError, e );
        }

        return strResponse;
    }

    /**
     * This method calls Rest WS to do an action
     * @param strUrl the url
     * @param params the params to pass in the post
     * @param listElements the list of elements to include in the signature
     * @return the response as a string
     * @throws HttpAccessException the exception if there is a problem
     */
    private static String sendDemandAction( String strUrl, Map<String, String> params, List<String> listElements )
        throws HttpAccessException
    {
        String strResponse = StringUtils.EMPTY;

        try
        {
            HttpAccess httpAccess = new HttpAccess(  );
            strResponse = httpAccess.doPost( strUrl, params,
                    CRMRequestAuthenticatorService.getRequestAuthenticatorForWS(  ), listElements );
        }
        catch ( HttpAccessException e )
        {
            String strError = "CRMWebServices - Error connecting to '" + strUrl + "' : ";
            AppLogService.error( strError + e.getMessage(  ), e );
            throw new HttpAccessException( strError, e );
        }

        return strResponse;
    }
}
