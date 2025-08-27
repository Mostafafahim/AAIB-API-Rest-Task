package com.mostafa.reqres;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.*;
import io.restassured.response.Response;
import org.testng.SkipException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.hamcrest.Matchers.*;

@Epic("ReqRes")
@Feature("User Management Workflow")
@Severity(SeverityLevel.CRITICAL)
public class UserWorkflowTest extends BaseTest {

    private String createdUserId;
    private String name;
    private String updatedJob;

    @DataProvider(name = "userData")
    public Object[][] userDataProvider() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        List<User> users = mapper.readValue(new File("src/test/resources/users.json"), new TypeReference<List<User>>() {});
        Object[][] data = new Object[users.size()][1];
        for (int i = 0; i < users.size(); i++) {
            data[i][0] = users.get(i);
        }
        return data;
    }

    @Test(priority = 1, description = "Create user and capture id", dataProvider = "userData")
    @Story("Create User")
    public void testCreateUser(User user) {
        this.name = user.getName();
        this.updatedJob = user.getUpdatedJob();

        Response res = baseRequest()
                .body("{\"name\":\"" + user.getName() + "\", \"job\":\"" + user.getInitialJob() + "\"}")
                .when()
                .post("/users")
                .then()
                .statusCode(201)
                .body("name", equalTo(user.getName()))
                .body("job", equalTo(user.getInitialJob()))
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
            res.then().body("data.job", equalTo(updatedJob));
        } else if (status == 404) {
            throw new SkipException("ReqRes does not persist created users. GET returned 404, skipping update verification.");
        } else {
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
