package callback;

import java.util.List;

import model.BestDealsModel;
import model.MostPopularModel;

public interface IMostPopularCallbackListener {
    void onListMostPopularLoadSuccess(List<MostPopularModel> mostPopularModels);
    void onListMostPopularLoadFailed(String message);
}
