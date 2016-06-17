package in.joind.api;

import android.content.Context;

import in.joind.R;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class APIService {
    private JoindIn api;

    public APIService(Context context) {
        String baseUrl = context.getString(R.string.apiURL);

        api = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(JoindIn.class);
    }

    public JoindIn getAPI() {
        return api;
    }
}
