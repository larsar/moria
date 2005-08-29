package no.feide.mellon.jaas.loginmodules;


import java.util.Map;
import java.util.*;

import java.security.Principal;
import javax.security.auth.Subject;
import javax.security.auth.callback.*;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

import no.feide.mellon.jaas.callbackhandlers.*;
import no.feide.mellon.jaas.principals.*;
import no.feide.mellon.v2_1.*;
import no.feide.moria.webservices.v2_1.Attribute;

/**
 * @author Rikke Amilde Løvlid
 */
public class MoriaLoginModule implements LoginModule{
	
	private Subject subject;
	private CallbackHandler callbackHandler;
	private Map sharedState;
	private Map options;
	
	private boolean loginSucceeded;
	private boolean commitSucceeded;
	
	private String username;
	private ArrayList tmpPrincipals;
	private ArrayList tmpPublicCredentials;
	
	private boolean debug;
	
	private final static String[] ATTRIBUTE_NAMES = {"eduPersonAffiliation", "eduPersonScopedAffiliation", "eduPersonPrimaryAffiliation"};
	private String endpoint;
	private String service_username;
	private String service_password;

	public MoriaLoginModule(){
		tmpPrincipals = new ArrayList();
		tmpPublicCredentials = new ArrayList();
		loginSucceeded = false;
		commitSucceeded = false;
		debug = false;
	}
	
	/**
	 * Initialize this <code>LoginModule</code>
	 * <br><br>
	 * @param subject 			the <code>Subject</code> to be authenticated.
	 * @param callbackHandler 	a <code>CallbackHandler</code> for communicating with the user,
	 * 							(promting for username and password)
	 * @param sharedState 		state shared with the other <code>LoginModule</code>
	 * @param options 			options specified in the login <code>Configuration</code> for this
	 * 							particular <code>LoginModule</code>
	 */
	public void initialize(Subject subject, CallbackHandler callbackHandler, Map sharedState, Map options){
		this.subject = subject;
		this.callbackHandler = callbackHandler;
		this.sharedState = sharedState;
		this.options = options;
		
		if(options.containsKey("debug")){
			debug = "true".equalsIgnoreCase((String)options.get("debug"));	
		}
		
		endpoint = (String)options.get("endpoint");
		service_username = (String)options.get("service_username");
		service_password = (String)options.get("service_password");
		
		if(debug){
			System.out.println("\t[MoriaLoginModule] initialize");
		}
	}
	
	/**
	 * The login method is called to authenticate the user.
	 * 
	 * @return true 	if the authentication succeedes (no exception is thrown).
	 *
	 * @throws FailedLoginException if authentication failes.
	 * @throws LoginException 		if this <code>LoginModule</code> is unable to perform the uthentication.
	 */
	public boolean login() throws LoginException{
		
		if(debug){
			System.out.println("\t[MoriaLoginModule] login");
		}
		
		if(callbackHandler == null){
			throw new LoginException("Error: no CallbackHandler available");
		}
		
		try{
			Callback[] callbacks = new Callback[2];
			callbacks[0] = new NameCallback("username: ");
			callbacks[1] = new PasswordCallback("password: ", false);
		
			callbackHandler.handle(callbacks);
			
			username = ((NameCallback)callbacks[0]).getName();

			char[] password = ((PasswordCallback)callbacks[1]).getPassword();
			
			((PasswordCallback)callbacks[1]).clearPassword();
			callbacks[0] = null;
			callbacks[1] = null;
		
			if(debug){
				System.out.println("\t[MoriaLoginModule] " + "user entered user name: " + username);
				System.out.print("\t[MoriaLoginModule] " + "user entered password: ");
				for(int i=0; i<password.length; i++){
					System.out.print(password[i]);
				}
				System.out.println();
			}
			
			loginSucceeded = validate(username, password);
			
			if(loginSucceeded){
				if(debug){
					System.out.println("\t[MoriaLoginModule] authentication succeeded");
				}
				return true;
			}
			else{
				if(debug){
					System.out.println("\t[MoriaLoginModule] authentication failed");
				}
				username = null;
				throw new FailedLoginException("Authentication failed");
			}
		}
		catch(LoginException le){
			loginSucceeded = false;
			throw le;
		}
		catch(Exception e){
			loginSucceeded = false;
			throw new LoginException(e.getMessage());
		}
		
	}
	
	/**
	 * This method is called if the LoginContext's overall authentication succeeded.
	 * 
	 * @return true 	if this loginmodules own login attempt succeded and commit succeeds,
	 * @return false 	if the login attempt failed.
	 * 
	 * @throws LoginException if the commit attempt failes. 
	 */
	public boolean commit() throws LoginException{
		if(debug){
			System.out.println("\t[MoriaLoginModule] commit");
		}
		
		if(loginSucceeded){
			if(subject.isReadOnly()){
				throw new LoginException("Subject is Readonly");
			}
			
			try{
				if(debug){
					Iterator it = tmpPrincipals.iterator();
					while(it.hasNext()){
						System.out.println("\t[MoriaLoginModule] Principal: " + it.next().toString());
					}
				}		
				subject.getPrincipals().addAll(tmpPrincipals);
				subject.getPublicCredentials().addAll(tmpPublicCredentials);
				
				tmpPrincipals.clear();
				tmpPublicCredentials.clear();
				
				if(callbackHandler instanceof PassiveCallbackHandler){
					((PassiveCallbackHandler)callbackHandler).clearPassword();
				}
				
				commitSucceeded = true;
				
				return true;
			}
			catch(Exception ex){
				throw new LoginException(ex.getMessage());
			}
		}
		else{
			tmpPrincipals.clear();
			tmpPublicCredentials.clear();
			return false;
		}
	}
	
	/**
	 * This method is called if the LoginContext's overall authentication failed.
	 * 
	 * @return false 	if this loginmodules own login attempts failed
	 * @return true 	otherwise			
	 * 
	 * @throws LoginException if the abort failes
	 */
	public boolean abort() throws LoginException{
		if(debug){
			System.out.println("\t[MoriaLoginModule] abort");
		}
		
		if(!loginSucceeded){
			return false;
		}
		else if(loginSucceeded && !commitSucceeded){
			loginSucceeded = false;
			username = null;
			tmpPrincipals.clear();
			tmpPublicCredentials.clear();
			
			if(callbackHandler instanceof PassiveCallbackHandler){
				((PassiveCallbackHandler)callbackHandler).clearPassword();
			}
		}
		else{
			logout();
		}
		
		return true;
	}
	
	/**
	 * This method removes the principals that was added by the <code>commit</code> method.
	 * 
	 * @return true in all cases.
	 * 
	 * @throws LoginException if the logout fails.
	 */
	public boolean logout() throws LoginException{
		if(debug){
			System.out.println("\t[MoriaLoginModule] logout");
		}
		
		tmpPrincipals.clear();
		tmpPublicCredentials.clear();
		
		if(callbackHandler instanceof PassiveCallbackHandler){
			((PassiveCallbackHandler)callbackHandler).clearPassword();
		}
		
		Iterator it = subject.getPrincipals().iterator();
		while(it.hasNext()){
			Principal p = (Principal)it.next();
			if(debug){
				System.out.println("\t[MoriaLoginModule] removing principal " + p.toString());
			}
			it.remove();
		}
		
		it = subject.getPublicCredentials().iterator();
		while(it.hasNext()){
			Object c = it.next();
			if(debug){
				System.out.println("\t[MoriaLoginModule] removing credential " + c.toString());
			}
			it.remove();
		}
	
		return true;
	}
	
	/**
	 * This method checks the username and password and gets the users attribute values.
	 * 
	 * @param username
	 * @param password
	 * 
	 * @return true 	if username and password are correct and false if there are som exception or the
	 * 					username and/or password not is correct.
	 */
	private boolean validate(String username, char[] password){
		try{
			Moria service = new Moria(endpoint, service_username, service_password);
		
			Attribute[] attributes = service.directNonInteractiveAuthentication(ATTRIBUTE_NAMES, username, new String(password));
			
			for(int i=0; i<attributes.length; i++){
				Attribute att = attributes[i];
				for(int j=0; j<att.getValues().length; j++){
					tmpPrincipals.add(new MoriaPrincipal(att.getName(), att.getValues()[j]));
				}
			}
			tmpPublicCredentials.add(username);
			
			return true;
		}
		catch(Exception ex){
			System.err.println("Error: " + ex.getMessage());
			ex.printStackTrace();
			return false;
		}	
	}
}
