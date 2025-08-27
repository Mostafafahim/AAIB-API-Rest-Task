package com.mostafa.reqres;

import io.qameta.allure.*;
import io.restassured.response.Response;
import org.testng.SkipException;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@Epic("ReqRes")
@Feature("User Management Workflow")
@Severity(SeverityLevel.CRITICAL)
public class UserWorkflowTest extends BaseTest {

    private static String createdUserId;
    private static String name = "Mostafa Fahim";
    private static String initialJob = "QA Engineer";
    private static String updatedJob = "Senior QA Engineer";

    @Test(priority = 1, description = "Create user and capture id")
    @Story("Create User")
    public void testCreateUser() {
        Response res = baseRequest()
                .body("{\"name\":\"" + name + "\", \"job\":\"" + initialJob + "\"}")
                .when()
                .post("/users")
                .then()
                .statusCode(201)
                .body("name", equalTo(name))
                .body("job", equalTo(initialJob))
                .body("id", not(isEmptyOrNullString()))
                .extract().response();

        createdUserId = res.jsonPath().getString("id");
        Allure.addAttachment("Created User ID", createdUserId);
    }

    @Test(priority = 2, dependsOnMethods = "testCreateUser", description = "Update user's job using captured id")
    @Story("Update User")
    public void testUpdateUser() {
        Response res = baseRequest()
                .body("{\"name\":\"" + name + "\", \"job\":\"" + updatedJob + "\"}")
                .when()
                .put("/users/" + createdUserId)
                .then()
                .statusCode(200)
                .body("job", equalTo(updatedJob))
                .extract().response();

        Allure.addAttachment("PUT response", res.asPrettyString());
    }

    @Test(priority = 3, dependsOnMethods = "testUpdateUser", description = "GET user to verify updated job (if API returns 200)")
    @Story("Get User (Verify Update)")
    public void testGetUserVerifyUpdate() {
        Response res = baseRequest()
                .when()
                .get("/users/" + createdUserId)
                .andReturn();

        int status = res.getStatusCode();
        Allure.addAttachment("GET /users/{id} response", res.asPrettyString());

        if (status == 200) {
            // If ever ReqRes returns 200 with a body, verify job (unlikely for this mock API)
            res.then().body("data.job", equalTo(updatedJob));
        } else if (status == 404) {
            // Expected for ReqRes mock (no persistence)
            throw new SkipException("ReqRes does not persist created users. GET returned 404, skipping update verification.");
        }
        else {
            throw new AssertionError("Unexpected GET status: " + status + ". Body: " + res.asString());

    }
    }

    @Test(priority = 4, dependsOnMethods = "testUpdateUser", description = "Delete user")
    @Story("Delete User")
    public void testDeleteUser() {
        baseRequest()
                .when()
                .delete("/users/" + createdUserId)
                .then()
                .statusCode(204);
    }

    @Test(priority = 5, dependsOnMethods = "testDeleteUser", description = "Verify user not found after deletion")
    @Story("Get User (Verify Deletion)")
    public void testGetUserVerifyDeletion() {
        baseRequest()
                .when()
                .get("/users/" + createdUserId)
                .then()
                .statusCode(404);
    }
}
