package com.samplekit.utils;

import android.util.ArrayMap;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.util.List;
import java.util.Map;

public class GsonUtils {

    private static GsonBuilder newBuilder() {
        final GsonBuilder gsonBuilder = new GsonBuilder();
        try {
            final Class<?> toNumberPolicyClass = Class.forName("com.google.gson.ToNumberPolicy");
            final Object longOrDouble = toNumberPolicyClass.getField("LONG_OR_DOUBLE").get(null);
            final Class<?> toNumberStrategyClass = Class.forName("com.google.gson.ToNumberStrategy");
            GsonBuilder.class.getMethod("setObjectToNumberStrategy", toNumberStrategyClass).invoke(gsonBuilder, longOrDouble);
//            gsonBuilder.setObjectToNumberStrategy(ToNumberPolicy.LONG_OR_DOUBLE)
        } catch (Exception ignored) {
            Log.w("GsonUtils", "gson version requires 2.8.9 or above");
        }
        gsonBuilder.disableHtmlEscaping();
        return gsonBuilder;
    }

    private static Gson gson = newBuilder().create();
    private static Gson gsonPretty = newBuilder().setPrettyPrinting().create();

    public static String toJson(Object src) {
        return gson.toJson(src);
    }

    public static String toPrettyJson(Object src) {
        return gsonPretty.toJson(src);
    }

    public static <T> Map<String, T> toMap(String json) {
        try {
            if (json != null) {
                return gson.fromJson(json, ArrayMap.class);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public static <T> T toObject(String json, Class<T> clazz) {
        try {
            if (json != null) {
                return gson.fromJson(json, clazz);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static <T> List<T> toList(String json) {
        try {
            if (json != null) {
                return gson.fromJson(json, new TypeToken<List<T>>() {
                }.getType());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
