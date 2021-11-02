package com.example.androiddrinkzlyboozeclient.EEventBus;

import com.example.androiddrinkzlyboozeclient.Model.BranchModel;

public class MenuItemEvent {
    private boolean success;
    private BranchModel branchModel;

    public MenuItemEvent(boolean success, BranchModel branchModel) {
        this.success = success;
        this.branchModel = branchModel;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public BranchModel getBranchModel() {
        return branchModel;
    }

    public void setBranchModel(BranchModel branchModel) {
        this.branchModel = branchModel;
    }
}
