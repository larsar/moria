package no.feide.moria.service;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.HashMap;

public interface AuthenticationIF extends Remote {
    
    public String requestSession(String[] attributes, String prefix, String postfix) throws RemoteException;
    public String requestSession(String prefix, String postfix) throws RemoteException;
    public HashMap getAttributes(String id) throws RemoteException;

}
