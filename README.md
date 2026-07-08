# CaseKaro Automation

Automated UI tests for the CaseKaro shopping flow using Java 17, Maven, Playwright, Cucumber, and JUnit 5.

## Prerequisites

- Java 17
- Maven 3.9+
- A Chromium-based browser available for Playwright, or let Playwright install one on first run

## Project Structure

- `src/main/java` contains the page objects, models, and Playwright setup
- `src/test/java` contains the Cucumber step definitions and JUnit suite runner
- `src/test/resources/features` contains the feature files

## Run Tests

```bash
mvn test
```

The Cucumber suite is executed through `com.gocomet.CaseKaroTestRunner`.

## Reports

After a run, HTML and JSON reports are generated under:

- `target/cucumber-reports/cucumber-report.html`
- `target/cucumber-reports/cucumber.json`

## Notes

- The main scenario covers searching for iPhone 16 Pro cases, adding Hard/Soft/Glass variants to the cart, and validating the cart contents.
- If Playwright browsers are missing, install them before running the suite.