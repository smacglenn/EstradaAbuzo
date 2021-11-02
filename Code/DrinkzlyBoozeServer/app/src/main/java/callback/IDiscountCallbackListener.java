package callback;

import java.util.List;

import model.DiscountModel;

public interface IDiscountCallbackListener {
    void onListDiscountLoadSuccess(List<DiscountModel> discountModelList);
    void onListDiscountLoadFailed(String message);
}
