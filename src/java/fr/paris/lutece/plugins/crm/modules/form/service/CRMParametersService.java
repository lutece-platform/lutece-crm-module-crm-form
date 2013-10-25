package fr.paris.lutece.plugins.crm.modules.form.service;

import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;

import fr.paris.lutece.plugins.crm.modules.form.business.CRMLocalParameters;
import fr.paris.lutece.plugins.crm.modules.form.util.Constants;
import fr.paris.lutece.plugins.form.business.Form;
import fr.paris.lutece.portal.service.spring.SpringContextService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;

/**
 * 
 * CRMParametersService
 *
 */
public class CRMParametersService implements ICRMParametersService {

	//The MAP containing associations between form and crm parameters
	private HashMap<String , CRMLocalParameters> _mapLocalParameters;
	private static final String CRM_ENABLED_LOCAL_CRM_PARAMETERS="crm-form.enabledLocalCrmParameters";
	private boolean _bEnabledLocalCrmParameters ;
		
	
	/**
     * {@inheritDoc}
     */
    @Override
	public  String getIdTypeDemande(HttpServletRequest request, Form form )
	{
		
    	String strIdTypeDemande=null;
		
		if(!isEnabledLocalCrmParameters())
		{
			strIdTypeDemande =request.getParameter( Constants.PARAM_ID_DEMAND_TYPE );
			
		}
		else if ( form !=null && _mapLocalParameters.containsKey(Integer.toString(form.getIdForm())))
		{
			CRMLocalParameters crmLocalParameters=_mapLocalParameters.get(Integer.toString(form.getIdForm()));
			strIdTypeDemande=crmLocalParameters.getIdDemandeType();
		}
		
		
		return strIdTypeDemande;
		
	}
	
    /**
     * {@inheritDoc}
     */
    @Override
	public String  getCrmWebAppCode(HttpServletRequest request, Form form )
	
	{
    	
		String strCrmWebAppCode=null;
		if(!isEnabledLocalCrmParameters())
		{
			strCrmWebAppCode=  request.getParameter( Constants.PARAM_CRM_WEBB_APP_CODE );
			
		}
		else if ( form !=null && _mapLocalParameters.containsKey(Integer.toString(form.getIdForm())))
		{
			CRMLocalParameters crmLocalParameters=_mapLocalParameters.get(Integer.toString(form.getIdForm()));
			
			strCrmWebAppCode=crmLocalParameters.getCRMWebAppCode();
		}
		return strCrmWebAppCode;
		
	}
	
	
	
    /**
     * {@inheritDoc}
     */
    @Override
	public boolean isEnabledLocalCrmParameters()
	{
		
		
		return _bEnabledLocalCrmParameters;
		
	}
	
    /**
     * {@inheritDoc}
     */
    @Override
    public void  init()
	 {
    	_bEnabledLocalCrmParameters=AppPropertiesService.getPropertyBoolean(CRM_ENABLED_LOCAL_CRM_PARAMETERS, false);
		 _mapLocalParameters=new HashMap<String, CRMLocalParameters>( );
		 List<CRMLocalParameters> listLocalParameters = SpringContextService.getBeansOfType( CRMLocalParameters.class );
		 for(CRMLocalParameters crmLocalParameters:listLocalParameters)
		 {
			 if( crmLocalParameters !=null && !StringUtils.isEmpty(crmLocalParameters.getIdForm() )  )
			 {
				 _mapLocalParameters.put( crmLocalParameters.getIdForm(), crmLocalParameters);
			 }
		 }
	}
	 
}
