package model.eventbus;

import java.util.List;

import model.BranchModel;

public interface IBranchCallbackListener {
    void onBranchLoadSuccess(List<BranchModel> branchModelList);
    void onBranchLoadFailed(String message);
}
