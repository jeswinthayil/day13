package in.edu.kristujayanti.utils;

import io.vertx.core.Vertx;
import io.vertx.ext.auth.PubSecKeyOptions;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTAuthOptions;
import io.vertx.core.json.JsonObject;

public class JwtUtil {

    private final JWTAuth jwtAuth;

    public JwtUtil(Vertx vertx) {
        JWTAuthOptions config = new JWTAuthOptions()
                .addPubSecKey(new PubSecKeyOptions()
                        .setAlgorithm("HS256")
                        .setBuffer("super-secret-key"));

        this.jwtAuth = JWTAuth.create(vertx, config);
    }

    public String generateToken(String email) {
        JsonObject claims = new JsonObject()
                .put("email", email)
                .put("exp", (System.currentTimeMillis() / 1000) + 3600);
        return jwtAuth.generateToken(claims);
    }

    public JWTAuth getJwtAuth() {
        return jwtAuth;
    }
}
