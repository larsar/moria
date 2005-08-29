package no.feide.mellon.jaas.application;

import java.security.AccessControlException;

/**
 * @author Rikke Amilde Løvlid.
 * 
 * This is how you run it:
 * java -Djava.security.auth.login.config==jaas.config \
 * 		-Djava.security.manager \
 * 		-Djava.security.policy==jaas.policy \
 * 		Login \
 * 		Demo \
 *		args
 *
 * As you see this application need to be spesified as the argument of the Login class.
 */
public class Demo{
	
	public static void main(String[] args){
		try{
			System.out.println("\t\tYour user.home property value is: " + System.getProperty("user.home"));
		}
		catch(AccessControlException ace){
			System.out.println("\t\tYou don't have permission to read the user.home value");
		}
		try{
			System.out.println("\t\tYour java.home property value is: " + System.getProperty("java.home"));
		}
		catch(AccessControlException ace){
			System.out.println("\t\tYou don't have permission to read the java.home value");
		}
	}
}
