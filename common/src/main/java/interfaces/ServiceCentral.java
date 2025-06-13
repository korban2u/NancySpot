package interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;


public interface ServiceCentral extends Remote {


    boolean enregistrerServiceBD(ServiceBD serviceBD) throws RemoteException;


    boolean enregistrerServiceProxy(ServiceProxy serviceProxy) throws RemoteException;


    boolean supprimerService(String serviceType) throws RemoteException;


    String getEtatServices() throws RemoteException;
}