package com.example.drinksproject.rmi.shared;

import com.example.drinksproject.model.Branch;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface BranchService extends Remote {
    List<Branch> getAllBranches() throws RemoteException;
}
