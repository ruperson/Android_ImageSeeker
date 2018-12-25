/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ru.ifmo.ctddev.vanyan.imageseeker.utilities;

import android.net.Uri;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class NetworkUtils {
    static OkHttpClient client = new OkHttpClient();
    public static String getResponseFromHttpUrl(String searchQuery) {
        Uri builtUri = Uri.parse("https://api.unsplash.com/search/photos/").buildUpon()
                .appendQueryParameter("query", searchQuery)
                .appendQueryParameter("per_page", "50")
                .appendQueryParameter("client_id", "SUPADUPASECRETKEY")
                .build();

        Request request = new Request.Builder().get().url(builtUri.toString()).build();
        String contentAsString = null;
        try {
            Response response = client.newCall(request).execute();
            contentAsString = response.body().string();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return contentAsString;
    }
}