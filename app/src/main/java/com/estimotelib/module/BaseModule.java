package com.estimotelib.module;

import android.content.Context;
import android.util.Log;

import com.estimotelib.api.API;
import com.estimotelib.api.EstimoteLibApi;
import com.estimotelib.interfaces.ICallbackHandler;
import com.estimotelib.serverutility.NetworkManager;
import com.google.gson.Gson;

import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public abstract class BaseModule implements API {
    protected Gson mGson;
    protected NetworkManager mNetworkManager;
    protected EstimoteLibApi mEstimoteLibApi;
    protected Context mContext;

    public BaseModule(Context context) {
        mContext = context;
        mNetworkManager = NetworkManager.getInstance(context);
        mEstimoteLibApi = mNetworkManager.getmPykupzAPI();
        mGson = new Gson();
    }

    protected String getErrorMessage(final String errorResponse) {

        String errorMsg = "";
        try{
            JSONObject errorJson = new JSONObject(errorResponse);
            errorMsg = errorJson.optString("message");
        }catch (JSONException e) {
            e.printStackTrace();
        }
        return errorMsg;

    }

    protected void parseResponseRetrofit(final String response,
                                         Class classRef,
                                         ICallbackHandler iCallbackHandler) {
       try {
            Object json = new JSONTokener(response).nextValue();
            if (json instanceof JSONObject){
                Object responseObject = mGson.fromJson(response, classRef);
                iCallbackHandler.response(responseObject);
            }else {
                iCallbackHandler.response(response);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    protected Callback makeNetworkCall(final String apiName) {
        Callback callback = new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    final String processedResponse = readResponse(response.body());
                    if (processedResponse != null) {
                        Log.e("processedResponse", processedResponse);
                        onSuccessResponse(processedResponse);
                    }
                } else {
                    onFailureResponse(response.message());
                    Log.e("error message", apiName+": "+response.message());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                onFailureResponse(t.getMessage());
            }
        };

        return callback;
    }

    private String readResponse(ResponseBody response) {

        try {
            final InputStream inputStream = response.byteStream();
            final String responseStr = readInputStream(inputStream);
            return responseStr;
        } catch (Exception e) {
            return null;
        }
    }

    private String readInputStream(InputStream responseStream) {
        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new InputStreamReader(responseStream, HTTP.UTF_8), 8);
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            bufferedReader = new BufferedReader(new InputStreamReader(responseStream));
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }
            return stringBuilder.toString();
        } catch (Exception e) {
            onFailureResponse("Failed");
            return null;
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                    responseStream.close();
                } catch (IOException e) {
                    onFailureResponse("Failed");
                    return null;
                }
            }
        }
    }

    protected abstract void onSuccessResponse(String response);

    protected abstract void onFailureResponse(String errorMsg);
}
