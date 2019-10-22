package com.redeban.payment.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Size;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Represents a JSON model used in the redeban Api.
 */
public abstract class RedebanJsonModel {

    @NonNull
    public abstract Map<String, Object> toMap();

    @NonNull
    public abstract JSONObject toJson();

    @Override
    public String toString() {
        return this.toJson().toString();
    }

    static void putRedebanJsonModelMapIfNotNull(
            @NonNull Map<String, Object> upperLevelMap,
            @NonNull @Size(min = 1) String key,
            @Nullable RedebanJsonModel jsonModel) {
        if (jsonModel == null) {
            return;
        }
        upperLevelMap.put(key, jsonModel.toMap());
    }


    static void putRedebanJsonModelIfNotNull(
            @NonNull JSONObject jsonObject,
            @NonNull @Size(min = 1) String key,
            @Nullable RedebanJsonModel jsonModel) {
        if (jsonModel == null) {
            return;
        }

        try {
            jsonObject.put(key, jsonModel.toJson());
        } catch (JSONException ignored) {
        }
    }

    static void putRedebanJsonModelListIfNotNull(
            @NonNull Map<String, Object> upperLevelMap,
            @NonNull @Size(min = 1) String key,
            @Nullable List<? extends RedebanJsonModel> jsonModelList) {
        if (jsonModelList == null) {
            return;
        }

        List<Map<String, Object>> mapList = new ArrayList<>();
        for (int i = 0; i < jsonModelList.size(); i++) {
            mapList.add(jsonModelList.get(i).toMap());
        }
        upperLevelMap.put(key, mapList);
    }


    static void putRedebanJsonModelListIfNotNull(
            @NonNull JSONObject jsonObject,
            @NonNull @Size(min = 1) String key,
            @Nullable List<? extends RedebanJsonModel> jsonModelList) {
        if (jsonModelList == null) {
            return;
        }

        try {
            JSONArray array = new JSONArray();
            for (RedebanJsonModel model : jsonModelList) {
                array.put(model.toJson());
            }
            jsonObject.put(key, array);
        } catch (JSONException ignored) {
        }
    }
}

