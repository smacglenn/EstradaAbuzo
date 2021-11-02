package callback;

import java.util.List;

import model.CategoryModel;
import model.OrderModel;

public interface IOrderCallbackListener {
    void onOrderLoadSuccess(List<OrderModel> orderModelList);
    void onOrderLoadFailed(String message);

}
