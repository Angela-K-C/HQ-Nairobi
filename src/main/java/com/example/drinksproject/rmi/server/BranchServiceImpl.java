package com.example.drinksproject.rmi.server;

import com.example.drinksproject.dao.BranchDao;
import com.example.drinksproject.model.Branch;
import com.example.drinksproject.rmi.shared.BranchService;

import java.rmi.server.UnicastRemoteObject;
import java.rmi.RemoteException;
import java.util.List;

public class BranchServiceImpl extends UnicastRemoteObject implements BranchService {

    public BranchServiceImpl() throws RemoteException {
        super();
    }

    @Override
    public List<Branch> getAllBranches() throws RemoteException {
        return BranchDao.getAllBranches();
    }
}
