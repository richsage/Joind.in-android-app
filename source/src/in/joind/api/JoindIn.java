package in.joind.api;

import java.util.ArrayList;
import java.util.Map;

import in.joind.model.Event;
import in.joind.model.EventCollectionResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.QueryMap;
import retrofit2.http.Url;

public interface JoindIn {
    @GET("events")
    Call<EventCollectionResponse<ArrayList<Event>>> events(@QueryMap Map<String, String> queryParams);

    @GET()
    Call<EventCollectionResponse<ArrayList<Event>>> eventsByUri(@Url String url);
}
