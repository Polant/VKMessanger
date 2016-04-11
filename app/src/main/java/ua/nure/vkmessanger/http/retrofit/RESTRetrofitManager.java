package ua.nure.vkmessanger.http.retrofit;

import android.content.Context;
import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import ua.nure.vkmessanger.AccessTokenManager;
import ua.nure.vkmessanger.http.RESTInterface;
import ua.nure.vkmessanger.http.model.CustomResponse;
import ua.nure.vkmessanger.http.model.RequestResult;
import ua.nure.vkmessanger.model.Attachment;
import ua.nure.vkmessanger.model.Message;
import ua.nure.vkmessanger.model.UserDialog;
import ua.nure.vkmessanger.model.WallPost;

/**
 * Класс-обертка для выполнения Http-запросов с помощью Retrofit.
 */
public class RESTRetrofitManager implements RESTInterface {

    private static final String RETROFIT_MANAGER_LOG_TAG = "RETROFIT_MANAGER_LOG";

    private static final String VK_API_VERSION = "5.38";

    private static final int CONNECT_TIMEOUT = 5000;
    private static final int WRITE_TIMEOUT = 5000;
    private static final int READ_TIMEOUT = 5000;

    private static final OkHttpClient CLIENT = new OkHttpClient.Builder()
            .connectTimeout(CONNECT_TIMEOUT, TimeUnit.MILLISECONDS)
            .writeTimeout(WRITE_TIMEOUT, TimeUnit.MILLISECONDS)
            .readTimeout(READ_TIMEOUT, TimeUnit.MILLISECONDS)
            .build();

    public static RetrofitAPI getRetrofit() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.vk.com/method/")
                .client(CLIENT)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        return retrofit.create(RetrofitAPI.class);
    }

    private Context mContext;

    public RESTRetrofitManager(Context context) {
        this.mContext = context;
    }

    @Override
    public CustomResponse loadUserDialogs() {
        RetrofitAPI api = getRetrofit();
        Call<JsonElement> retrofitCall = api.userDialogs(VK_API_VERSION,
                USER_DIALOGS_DEFAULT_REQUEST_COUNT,
                AccessTokenManager.getAccessToken(mContext));

        CustomResponse customResponseResult = new CustomResponse();
        try {
            Response<JsonElement> retrofitResponse = retrofitCall.execute();
            if (retrofitResponse.isSuccessful()) {
                Log.d(RETROFIT_MANAGER_LOG_TAG, "request SUCCESSFUL");

                List<UserDialog> dialogs = new ArrayList<>();

                JsonArray jsonItemsArray = retrofitResponse.body().getAsJsonObject()
                        .getAsJsonObject("response")
                        .getAsJsonArray("items");

                for (int i = 0; i < jsonItemsArray.size(); i++) {
                    JsonObject dialogJSON = jsonItemsArray.get(i).getAsJsonObject().get("message").getAsJsonObject();
                    JsonElement chatIdJsonElement = dialogJSON.get("chat_id");
                    int chatId = chatIdJsonElement == null ? 0 : chatIdJsonElement.getAsInt();

                    dialogs.add(new UserDialog(
                            chatId,
                            dialogJSON.get("user_id").getAsInt(),
                            dialogJSON.get("body").getAsString()
                    ));
                }
                Log.d(RETROFIT_MANAGER_LOG_TAG, String.format("Dialogs count == %d", dialogs.size()));

                customResponseResult.setRequestResult(RequestResult.SUCCESS)
                        .setAnswer(dialogs);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return customResponseResult;
    }

    @Override
    public CustomResponse loadSelectedDialogById(int dialogId, int offsetCount) {
        RetrofitAPI api = getRetrofit();
        Call<JsonElement> retrofitCall = api.dialogHistory(VK_API_VERSION,
                dialogId, offsetCount, DIALOG_MESSAGES_DEFAULT_REQUEST_COUNT, AccessTokenManager.getAccessToken(mContext));

        CustomResponse customResponseResult = new CustomResponse();
        try {
            //Sync request.
            Response<JsonElement> retrofitResponse = retrofitCall.execute();

            if (retrofitResponse.isSuccessful()) {
                JsonObject responseBody = retrofitResponse.body().getAsJsonObject();
                try {
                    if (!responseBody.has("response")) {
                        return new CustomResponse();    //ResultResponse.ERROR by default.
                    }
                }catch (NullPointerException ignored) { }    //Не понимаю, как, но иногда вылетает.

                List<Message> messages = new ArrayList<>();

                JsonArray jsonItemsArray = responseBody.getAsJsonObject("response").getAsJsonArray("items");
                for (int i = 0; i < jsonItemsArray.size(); i++) {
                    JsonObject messageJSON = jsonItemsArray.get(i).getAsJsonObject();

                    //Читаю данные о сообщении.
                    int messageId = messageJSON.get("id").getAsInt();
                    boolean isMessageFromMe = messageJSON.get("out").getAsInt() == MESSAGE_WAS_SEND_FROM_ME;
                    boolean isRead = messageJSON.get("read_state").getAsInt() == MESSAGE_WAS_READ;
                    String messageBody = messageJSON.get("body").getAsString();
                    Date date = new Date(messageJSON.get("date").getAsLong());

                    //Читаю данные о вложениях сообщений.
                    Attachment[] attachments = null;
                    if (messageJSON.has("attachments")){

                        JsonArray attachmentsJSONArray = messageJSON.get("attachments").getAsJsonArray();
                        attachments = new Attachment[attachmentsJSONArray.size()];

                        for (int j = 0; j < attachmentsJSONArray.size(); j++) {
                            JsonObject attachmentItemJson = attachmentsJSONArray.get(j).getAsJsonObject();

                            String attachmentItemType = attachmentItemJson.get("type").getAsString();
                            //TODO: пока что добавляю только записи на стене, но потом сделать и другое.
                            if (attachmentItemType.equals(Attachment.TYPE_WALL_POST)){

                                JsonObject wallPostJSONObject = attachmentItemJson.get("wall").getAsJsonObject();

                                int postId = wallPostJSONObject.get("id").getAsInt();
                                int authorId = wallPostJSONObject.get("from_id").getAsInt();
                                int wallOwnerId = wallPostJSONObject.get("to_id").getAsInt();
                                Date postCreatedDate = new Date(wallPostJSONObject.get("date").getAsLong());
                                String postText = wallPostJSONObject.get("text").getAsString();
                                String postType = wallPostJSONObject.get("post_type").getAsString();

                                attachments[j] = new Attachment(
                                        attachmentItemType,
                                        new WallPost(
                                                postId,
                                                authorId,
                                                wallOwnerId,
                                                postCreatedDate,
                                                postText,
                                                postType));

                                //TODO: сделать еще парсинг вложенных в запись на стене Attachments.
                            }
                        }

                    }
                    messages.add(new Message(messageId, isMessageFromMe, isRead, messageBody, date, attachments));
                }
                Log.d(RETROFIT_MANAGER_LOG_TAG, String.format("Messages count == %d", messages.size()));

                customResponseResult.setRequestResult(RequestResult.SUCCESS)
                        .setAnswer(messages);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return customResponseResult;
    }

    @Override
    public CustomResponse sendMessageTo(String message, int peerId) {
        RetrofitAPI api = getRetrofit();
        Call<JsonElement> retrofitCall = api.sendMessage(VK_API_VERSION, peerId, message, AccessTokenManager.getAccessToken(mContext));

        CustomResponse customResponseResult = new CustomResponse();
        try {
            Response<JsonElement> retrofitResponse = retrofitCall.execute();
            JsonObject responseObject = retrofitResponse.body().getAsJsonObject();

            if (responseObject.has("response")) {
                int messageId = responseObject.get("response").getAsInt();
                Date date = new Date();
                Attachment[] attachments = null;

                //TODO: 3-й параметр(isRead) false - под вопросом.
                customResponseResult.setRequestResult(RequestResult.SUCCESS)
                        .setAnswer(new Message(messageId, true, false, message, date, attachments));
            }
            else {
                if (responseObject.has("error")) {
                    JsonElement errorObject = responseObject.get("error");
                    customResponseResult.setAnswer(errorObject.getAsJsonObject().get("error_code").getAsInt());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return customResponseResult;
    }
}