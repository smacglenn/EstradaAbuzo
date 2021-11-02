package callback;

import java.util.List;

import model.OrderTransactionModel;

public interface ITransactionsCallbackListener {
    void onTransactionsLoadSuccess(List<OrderTransactionModel> orderTransactionModelList);
    void onTransactionsLoadFailed(String message);
}
