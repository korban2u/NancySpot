package interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;


public interface ServiceProxy extends Remote {


    String getVelibData() throws RemoteException;


    String getIncidents() throws RemoteException;


    boolean ping() throws RemoteException;
}