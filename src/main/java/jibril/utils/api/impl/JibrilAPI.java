package jibril.utils.api.impl;

import com.github.natanbc.reliqua.Reliqua;
import com.github.natanbc.reliqua.request.PendingRequest;
import com.github.natanbc.reliqua.util.StatusCodeValidator;
import jibril.exported.JibrilExported;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class JibrilAPI extends Reliqua implements AutoCloseable {
    public static class Builder {
        private String baseApi;
        private OkHttpClient httpClient;
        private String token;
        private boolean trackCallSites;

        @CheckReturnValue
        @Nonnull
        public JibrilAPI build() {
            return new JibrilAPI(
                httpClient == null ? new OkHttpClient() : httpClient,
                token,
                baseApi,
                trackCallSites
            );
        }

        @CheckReturnValue
        @Nonnull
        public Builder setBaseApi(@Nullable String baseApi) {
            this.baseApi = baseApi;
            return this;
        }

        @CheckReturnValue
        @Nonnull
        public Builder setCallSiteTrackingEnabled(boolean enabled) {
            this.trackCallSites = enabled;
            return this;
        }

        @CheckReturnValue
        @Nonnull
        public Builder setHttpClient(@Nullable OkHttpClient httpClient) {
            this.httpClient = httpClient;
            return this;
        }

        @CheckReturnValue
        @Nonnull
        public Builder setToken(@Nullable String token) {
            this.token = token;
            return this;
        }
    }

    private static final Logger LOGGER = LoggerFactory.getLogger("JibrilAPI");
    private static final String USER_AGENT = JibrilExported.user_agent;
    private final String baseApi;
    private final String token;

    private JibrilAPI(OkHttpClient httpClient, String token, String baseApi, boolean trackCallSites) {
        super(httpClient, null, trackCallSites);
        this.token = token == null ? "" : token;
        this.baseApi = baseApi == null ? "http://localhost:8080" : baseApi;
    }

    @Override
    public void close() {
        getClient().dispatcher().executorService().shutdown();
    }

    /**
     * Returns the token given to the builder, needed for posting stats.
     *
     * @return The token.
     */
    @CheckReturnValue
    @Nonnull
    public String getToken() {
        return token;
    }

    /**
     * Posts a bot's server count. This endpoint requires the bot owner's token.
     *
     * @param shardTotal  The total amount shards.
     * @param guilds      The server count for the bot.
     * @param users       The user count for the bot.
     * @param musicAmount The music player count for the bot.
     * @param queueSize   The queue size for the bot.
     */
    @CheckReturnValue
    @Nonnull
    public PendingRequest<Void> postStats(long shardTotal, long guilds, long users, long musicAmount, long queueSize) {
        if (token == null) throw new IllegalStateException("Token is null");
        Request request = new Request.Builder()
            .url(baseApi + "/api/bots/stats")
            .header("Authorization", token)
            .header("User-Agent", USER_AGENT)
            .header("Accept-Encoding", "gzip")
            .post(
                RequestBody.create(
                    MediaType.parse("application/json"),
                    new JSONObject()
                        .put("shardTotal", shardTotal)
                        .put("guilds", guilds)
                        .put("users", users)
                        .put("musicAmount", musicAmount)
                        .put("queueSize", queueSize)
                        .toString()
                )
            )
            .build();
        return createRequest(request)
            .setStatusCodeValidator(StatusCodeValidator.ACCEPT_200)
            .build(r -> null, null);
    }
}
