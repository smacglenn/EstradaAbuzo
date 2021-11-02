package callback;

import java.util.List;

import model.BestDealsModel;

public interface IBestDealCallbackListener {
    void onListBestDealsLoadSuccess(List<BestDealsModel> bestDealsModels);
    void onListBestDealsLoadFailed(String message);
}
