package no.feide.mellon.jaas.principals;

import java.util.Iterator;

import javax.security.auth.Subject;

import com.sun.security.auth.PrincipalComparator;


/**
 * A special principal for the eduPersonScopedAffiliation attribute in FEIDE
 * <br><br>
 * Note that no subject will have any ScopedAffiliationPrincipals, only MoriaPrincipals.
 * An ScopedAffiliationPrincipal implies a MoriaPrincipal with <code>attributeName</code> "eduPersonScopedAffiliation"
 * and the sufficient attribute value. See the <code>implies</code> method.
 * 
 * ScopedAffiliationPrincipals are only used in the policy file. In the policy file it cannot
 * be replaced by the corresponding MoriaPrincipal if you want to make use of wildcards. 
 * 
 * @author Rikke Amilde Løvlid
 */
public class ScopedAffiliationPrincipal extends MoriaPrincipal implements PrincipalComparator{

	/**
	 * @param name	the attribute value of the eduPersonScopedAffiliation attribute in FEIDE.
	 * 				It is on the form x@y where x may be replaced by a wildcard.
	 */
	public ScopedAffiliationPrincipal(String name){
		super("eduPersonScopedAffiliation", name);
	}
	
	/**
	 * ScopedAffiliationPrincipal implies the Subject subject if the subject has a MoriaPrincipal
	 * with attributeName "eduPersonScopedAffiliation" (this.attributeName is "eduPersonScopedAffiliation") 
	 * and an attributeValue which is implied by the attributeValue belonging to this ScopedAffiliationPrincipal.
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
	 * @param string1	this objects attribute value. It is on the form a@b
	 * @param string2	the attribute value we vant to know wether is implied by string1 or not. It is on the form c@d
	 * 
	 * The "a" in string1 may be a wildcard, "*". 
	 * string1 implies string2 if they are equal or "a" is a wildcard and b and d are equal.
	 * 
	 * @return whether string1 implies string2
	 */
	public boolean implies(String string1, String string2){
		if(string1.indexOf("*")!=0){
			return string1.equals(string2);
		}
		if(string1.indexOf("@")==1 && 
				string1.substring(2).equals(string2.substring(string2.indexOf("@")+1))){
			return true;
		}
		return false;
		
	}
}
