package fr.paris.lutece.plugins.crm.modules.form.business;

/**
 * 
 * CRMLocalParameters
 *
 */
public class CRMLocalParameters {


	private String _strIdForm;
	private String _strIdDemandeType;
	private String _strCRMWebAppCode;
	
	/**
	 * 
	 * @return the form id
	 */
	public String getIdForm() {
		return _strIdForm;
	}
	/**
	 * 
	 * @param strIdForm the form id
	 */
	public void setIdForm(String  strIdForm) {
			_strIdForm = strIdForm;
	}
	/**
	 * 
	 * @return the demande type id
	 */
	public String getIdDemandeType() {
		return _strIdDemandeType;
	}
	/**
	 * 
	 * @param strIdDemandeType the demande type id
	 */
	public void setIdDemandeType(String strIdDemandeType) {
			_strIdDemandeType = strIdDemandeType;
	}
	/**
	 * 
	 * @return the code of the webapp crm 
	 */
	public String getCRMWebAppCode() {
		return _strCRMWebAppCode;
	}
	/**
	 * 
	 * @param strCRMWebAppCode the code of the webapp crm 
	 */
	public void setCRMWebAppCode(String strCRMWebAppCode) {
			_strCRMWebAppCode = strCRMWebAppCode;
	}
	

}
