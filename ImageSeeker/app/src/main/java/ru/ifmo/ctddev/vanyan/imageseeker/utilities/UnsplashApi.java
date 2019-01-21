package ru.ifmo.ctddev.vanyan.imageseeker.utilities;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface UnsplashApi {
    //! Insert you unsplash key here
    @GET("search/photos/?per_page=50&client_id=SUPADUPASECRETKEY")
    Call<DeserializeDataJson> getContributors(@Query("query") String name);

}

