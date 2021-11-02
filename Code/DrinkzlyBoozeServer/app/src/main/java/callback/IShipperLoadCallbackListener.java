package callback;

import android.app.AlertDialog;
import android.widget.Button;
import android.widget.RadioButton;

import java.util.List;

import model.OrderModel;
import model.ShipperModel;

public interface IShipperLoadCallbackListener {
    void onShipperLoadSuccess(List<ShipperModel> shipperModelList);
    void onShipperLoadSuccess(int pos, OrderModel orderModel, List<ShipperModel> shipperModels,
                              AlertDialog dialog,
                              Button btn_ok, Button btn_cancel,
                              RadioButton rdi_shipping,RadioButton rdi_shipped,RadioButton rdi_cancelled,RadioButton rdi_delete,RadioButton rdi_restore_placed);
    void onShipperLoadFailed(String message);
}
