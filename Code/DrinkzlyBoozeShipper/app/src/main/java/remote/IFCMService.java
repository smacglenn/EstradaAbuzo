package remote;

import io.reactivex.Observable;
import model.FCMResponse;
import model.FCMSendData;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface IFCMService {
    @Headers({
            "Content-Type:application/json",
            "Authorization:key=AAAAy1JbAxI:APA91bFypqPE1vTSt59-VahjyOy40dD5bWMJArWGpzBvp3o3fcefI98MJK_S3F_4F9KQp-UtK-DoyUB6V6Fvpd7Aae3_HBFlOam-z33aIPiielNZsig7KoBMUYcbA_td276c-IALrnWe"
    })
    @POST("fcm/send")
    Observable<FCMResponse> sendNotification(@Body FCMSendData body);
}
