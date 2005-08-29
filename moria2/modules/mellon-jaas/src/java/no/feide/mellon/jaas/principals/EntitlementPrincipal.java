package no.feide.mellon.jaas.principals;

import java.util.Iterator;

import javax.security.auth.Subject;

import com.sun.security.auth.PrincipalComparator;


/**
 * A special principal for the eduPersonEntitlement attribute in FEIDE
 * <br><br>
 * Note that no subject will have any EntitlementPrincipals, only MoriaPrincipals.
 * An EntitlementPrincipal implies a MoriaPrincipal with <code>attributeName</code> "eduPersonEntitlement"
 * and the sufficient attribute value. See the <code>implies</code> method.
 * 
 * EntitlementPrincipals are only used in the policy file. In the policy file it cannot
 * be replaced by the corresponding MoriaPrincipal if you want to make use of wildcards. 
 * 
 * @author Rikke Amilde Løvlid
 */
public class EntitlementPrincipal extends MoriaPrincipal implements PrincipalComparator{

	/**
	 * @param name	the attribute value of the eduPersonEntitlement attribute in FEIDE,
	 * 				may contain wildcards.
	 */
	public EntitlementPrincipal(String name){
		super("eduPersonEntitlement", name);
	}
	
	/**
	 * This EntitlementPrincipal implies the Subject subject if the subject has a MoriaPrincipal
	 * with attributeName "eduPersonEntitlement" (this.attributeName is "eduPersonEntitlement") 
	 * and an attributeValue which is implied by the attributeValue belonging to this EntitlementPrincipal.
	 */
	public boolean implies(Subject subject){
		Iterator it = subject.getPrincipals(MoriaPrincipal.class).iterator();
		while(it.hasNext()){
			MoriaPrincipal mp = (MoriaPrincipal)it.next();
			if(mp.getAttributeName().equals(this.attributeName) && implies(this.attributeValue, mp.getAttributeValue())){
				return true;
			}	
		}
		return false;
	}
	
	/**
	 * @param string1
	 * @param string2
	 * 
	 * A wildcard, "*", in <code>string1</code> can match any substring of <code>string2</code> 
	 * that does not contain any ':'. The wildcards have to be between two ":".
	 * 
	 * @return whether string1 implies string 2
	 */
	public boolean implies(String string1, String string2){
		/**
		 * <code>string1</code> impies <code>string2</code> if they are equal.
		 */
		//if(string1.equals(string2) ||
		//		(string1.equals("*") && string2.indexOf(":")==-1)){
		if(string1.equals(string2)){
			return true;
		}
		/**
		 * <code>string1</code>: a:*:b <BR>
		 * <code>string2</code>: c:d:e <BR>
		 * <code>string1</code> implies <code>string2</code> if there are c,d,e so that 
		 * a implies c (here equivalent with a equals c), b implies e and d does not contain any ':'.
		 */
		//index1 is the position of the first "*".
		int index1 = string1.indexOf("*");
	
		//The "*" mentioned is the first occurence of "*" in string1.
		if(index1!=-1 &&
				//string1 starts with the "*" (string1: *:..) or 
				//the "*" follows a ":" and the substring befor "*" is the same in both string1 and string2 (a equals c)
				(index1==0 || 
						(string1.charAt(index1-1)==':') && string1.substring(0,index1).equals(string2.substring(0,index1))) &&
		        //string1 ends with "*" and part e does not exist or
				//the "*" is followd by a ":" and b implies e.
				((index1==string1.length()-1 && string2.indexOf(":", index1)==-1) ||
						(string1.charAt(index1+1)==':' && implies(string1.substring(index1+2), string2.substring(string2.indexOf(":", index1)+1))))){
			return true;
		}
		return false;
	}
}
