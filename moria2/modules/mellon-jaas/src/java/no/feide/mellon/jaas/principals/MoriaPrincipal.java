package no.feide.mellon.jaas.principals;

import java.security.Principal;

/**
 * This is the general Principal class.
 * 
 * @author Rikke Amilde Løvlid
 */

public class MoriaPrincipal implements Principal{

	protected String attributeName;
	protected String attributeValue;
	
	
	/**
	 * @param name	the principal name that are used in the policy file,
	 * 				has to have the format attributeName:attributeValue,
	 * 				ex: eduPersonAffiliation:student
	 */
	public MoriaPrincipal(String name){
		if(name == null){
			throw new NullPointerException("illegal null input");
		}
		if(name.indexOf(":")!=-1){	
			this.attributeName = name.substring(0, name.indexOf(":"));
			this.attributeValue = name.substring(name.indexOf(":")+1);
		}
	}
	/**
	 * @param attributeName		the FEIDE attribute name
	 * @param attributeValue	the FEIDE attribute value
	 */
	public MoriaPrincipal(String attributeName, String attributeValue){
		if(attributeName == null || attributeValue == null){
			throw new NullPointerException("illegal null input");
		}
		this.attributeName = attributeName;
		this.attributeValue = attributeValue;
	}
	
	/**
	 * @return attributeName:attributeValue
	 */
	public String getName(){
		return attributeName + ":" + attributeValue;
	}
	
	public String getAttributeName(){
		return attributeName;
	}
	
	public String getAttributeValue(){
		return attributeValue;
	}
	
	/**
	 * @return MoriaPrincipal: attributeName:attributeValue
	 */
	public String toString(){
		return ("MoriaPrincipal: " + this.getName());
	}
	
	/**
	 * @param o			the object we want to compare to <code>this</code>
	 * @return true		if <code>this</code> is the same object as <code>o</code> or
	 * 					<code>this</code> and <code>o</code> have the same
	 * 					<code>attributeName</code> and </code>attributeValue</code>.
	 * @return false 	otherwise 
	 */
	public boolean equals(Object o){
		if(o == null){
			return false;
		}
		if(this == o){
			return true;
		}
		if(!(o instanceof MoriaPrincipal)){
			return false;
		}
		return this.getName().equals(((MoriaPrincipal)o).getName());
	}
	
	public int hashCode(){
		return this.getName().hashCode();
	}
	
	
}
