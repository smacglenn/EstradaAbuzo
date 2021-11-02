package model.eventbus;

import model.BranchModel;

public class BranchSelectEvent {
    private BranchModel branchModel;

    public BranchSelectEvent(BranchModel branchModel) {
        this.branchModel = branchModel;
    }

    public BranchModel getBranchModel() {
        return branchModel;
    }

    public void setBranchModel(BranchModel branchModel) {
        this.branchModel = branchModel;
    }
}
