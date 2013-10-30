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
package fr.paris.lutece.plugins.crm.modules.form.util;


/**
 *
 * Constants
 *
 */
public final class Constants
{
    // CONSTANTS
    public static final String SEPARATOR = ",";
    public static final String INVALID_ID = "-1";
    public static final String UNDERSCORE = "_";

    // PARAMETERS
    public static final String PARAM_ID_DEMAND_TYPE = "id_demand_type";
    public static final String PARAM_ID_DEMAND = "id_demand";
    public static final String PARAM_USER_GUID = "user_guid";
    public static final String PARAM_ID_STATUS_CRM = "id_status_crm";
    public static final String PARAM_STATUS_TEXT = "status_text";
    public static final String PARAM_DEMAND_DATA = "demand_data";
    public static final String PARAM_ID_BLOB = "id_blob";
    public static final String PARAM_ID_CRM_USER = "id_crm_user";
    public static final String PARAMETER_URL_RETURN = "url_return";
    public static final String PARAMETER_ACTION_NAME = "action";
    public static final String PARAM_CRM_WEBB_APP_CODE = "crm_web_app_code";


    //SESSION ATTRIBUTE
    public static final String SESSION_ATTRIBUTE_ID_DEMAND_PARAMS = "FORM_ID_DEMAND";
    public static final String SESSION_ATTRIBUTE_USER_GUID_PARAMS = "FORM_USER_GUID";
    public static final String SESSION_ATTRIBUTE_DEMAND_DATA_PARAMS = "FORM_DEMAND_DATA";
    public static final String SESSION_ATTRIBUTE_DEMAND_CRM_WEBB_APP_CODE_PARAMS= "FORM_CRM_WEB_APP_CODE";
    
    // JSON KEY
    public static final String JSON_KEY_FORM = "form";
    public static final String JSON_KEY_SUBFORMS = "subforms";

    // CRM STATUS TEXT
    public static final String PROPERTY_CRM_STATUS_TEXT_NEW = "module.crm.form.crm.status.text.new";
    public static final String PROPERTY_CRM_STATUS_TEXT_MODIF = "module.crm.form.crm.status.text.modif";
    public static final String PROPERTY_CRM_STATUS_TEXT_VALIDATE = "module.crm.form.crm.status.text.validate";
    public static final String PROPERTY_MESSAGE_STOP_ACCESS_DENIED = "module.crm.form.message.stop.accessDenied";
    public static final String PROPERTY_MESSAGE_INFO_REMOVE_DEMAND = "module.crm.form.message.info.removeDemand";
    public static final String PROPERTY_MESSAGE_ERROR_CALLING_WS = "module.crm.form.message.error.calling.ws";

  
    // ACTIONS
    public static final String ACTION_DO_REMOVE_DRAFT = "do_remove_draft";
    public static final String ACTION_REMOVE_DRAFT = "remove_draft";

    /**
     * Constants
     */
    private Constants(  )
    {
    }
}
