# ReqRes API Tests (Rest Assured + TestNG + Allure)

Small demo suite that automates a user-management workflow against **https://reqres.in/**.

> ⚠️ **Note on ReqRes behavior**: ReqRes is a mock API and does **not persist** created/updated resources.
> - `POST /users` returns an `id`, but `GET /users/{id}` for that id will not return the created user.
> - `PUT /users/{id}` echoes the update in the response, but a later `GET` will not reflect it.
> - `DELETE /users/{id}` returns `204`, and later `GET` commonly returns `404`.
>
> The tests below therefore verify updates using the **PUT response**, and they handle the `GET` steps defensively:
> - If `GET` returns `200`, we verify the fields.
> - If `GET` returns `404`, we skip the verification with a clear reason (expected behavior for ReqRes).

## Stack
- Java 17
- Maven
- Rest Assured
- TestNG
- Allure reporting

## How to run
```bash
# 1) Run tests
mvn clean test

# 2) Generate & open Allure report (requires Allure CLI installed)
# On macOS (brew) or Windows (scoop/choco) install Allure first, then:
mvn allure:report
# or launch a local web server:
allure serve target/allure-results
```

## Test Coverage
1. **Create User** – `POST /users` (capture `id`).
2. **Update User** – `PUT /users/{id}` (update `job`, assert response).
3. **Get User (Verify Update)** – `GET /users/{id}` (verify if 200; skip if 404 due to non-persistence).
4. **Delete User** – `DELETE /users/{id}` (expect 204).
5. **Get User (Verify Deletion)** – `GET /users/{id}` (expect 404).

## Project Structure
```
reqres-api-tests/
├─ pom.xml
├─ README.md
└─ src
   └─ test
      ├─ java
      │  └─ com
      │     └─ mostafa
      │        └─ reqres
      │           ├─ BaseTest.java
      │           └─ UserWorkflowTest.java
      └─ resources
         └─ testng.xml
```

## CI (optional)
You can add a simple GitHub Actions workflow:
```yaml
# .github/workflows/tests.yml
name: CI
on: [push, pull_request]
jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: 17
      - run: mvn -q -B -DskipTests=false clean test
      - name: Publish Allure Results
        uses: simple-elf/allure-report-action@v1.8
        if: always()
        with:
          allure_results: target/allure-results
          gh_pages: false
```
