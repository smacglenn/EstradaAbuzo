package callback;

import java.util.List;

import model.ShippingOrderModel;

public interface IShippingOrderCallbackListener {
    void onShippingOrderLoadSuccess(List<ShippingOrderModel> shippingOrderModelList);
    void onShippingOrderLoadFailed(String message);
}
