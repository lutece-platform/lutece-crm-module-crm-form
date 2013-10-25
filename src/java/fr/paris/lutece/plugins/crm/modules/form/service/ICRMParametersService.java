package fr.paris.lutece.plugins.crm.modules.form.service;

import javax.servlet.http.HttpServletRequest;

import fr.paris.lutece.plugins.form.business.Form;

/**
 * 
 * ICRMParametersService
 *
 */
public interface ICRMParametersService {

	/**
	 * Return the crm demand type Id associated to the form
	 * @param request The HTTP request
	 * @param form the form
	 * @return the demand type Id associated to the form
	 */
	String getIdTypeDemande(HttpServletRequest request, Form form);
	/**
	 * Return the WebApp Code associated to the form. Use when multi crm web app can access to the form  
	 * @param request request The HTTP request
	 * @param form the form
	 * @return the WebApp Code associated to the form
	 */
	String getCrmWebAppCode(HttpServletRequest request, Form form);

	/**
	 * 
	 * @return true if the association between form and crm demand type id and crm web app code are record in the file crm-form_context.xml
	 */
	boolean isEnabledLocalCrmParameters( );
	
	/**
	 * init CRM Parameter Service
	 */
	void init( );

	}