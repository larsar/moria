package no.feide.moria.service;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.HashMap;

public interface AuthenticationIF extends Remote {
    
    public String requestSession(String[] attributes, String prefix, String postfix) throws RemoteException;
    public String requestSession(String[] attributes, String prefix, String postfix, boolean denySSO) throws RemoteException;
    public HashMap getAttributes(String id) throws RemoteException;
    
    public String requestUserAuthentication(String id, String username, String password) throws RemoteException;
}
