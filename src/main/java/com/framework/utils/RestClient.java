package com.framework.utils;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.util.Map;

import static com.framework.config.ConfigurationManager.config;

/**
 * Generic REST client built on RestAssured.
 *
 * <p>Provides convenience methods for the four main HTTP verbs.
 * Extend or compose this class to create application-specific API clients.
 *
 * <pre>
 *   RestClient client = new RestClient(config().apiBaseUrl(), token);
 *   Response r = client.get("/users/123");
 *   r.then().statusCode(200);
 * </pre>
 */
public class RestClient {

    private final String baseUrl;
    private final String bearerToken;

    public RestClient(String baseUrl, String bearerToken) {
        this.baseUrl      = baseUrl;
        this.bearerToken  = bearerToken;
        RestAssured.useRelaxedHTTPSValidation();
    }

    /** Creates a client using the configured {@code api.base.url}. */
    public RestClient(String bearerToken) {
        this(config().apiBaseUrl(), bearerToken);
    }

    // ── HTTP Verbs ────────────────────────────────────────────────────────

    public Response get(String path) {
        return spec().when().get(baseUrl + path);
    }

    public Response get(String path, Map<String, ?> queryParams) {
        return spec().queryParams(queryParams).when().get(baseUrl + path);
    }

    public Response post(String path, Object body) {
        return spec().body(body).when().post(baseUrl + path);
    }

    public Response put(String path, Object body) {
        return spec().body(body).when().put(baseUrl + path);
    }

    public Response patch(String path, Object body) {
        return spec().body(body).when().patch(baseUrl + path);
    }

    public Response delete(String path) {
        return spec().when().delete(baseUrl + path);
    }

    // ── Spec builder ──────────────────────────────────────────────────────

    private RequestSpecification spec() {
        RequestSpecification req = RestAssured.given()
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .log().ifValidationFails();
        if (bearerToken != null && !bearerToken.isBlank()) {
            req = req.auth().oauth2(bearerToken);
        }
        return req;
    }
}
