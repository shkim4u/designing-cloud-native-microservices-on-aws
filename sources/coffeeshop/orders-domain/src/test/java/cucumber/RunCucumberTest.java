package cucumber;


import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
	plugin = {"pretty", "html:target/cucumber-report.html"},
	glue = "cucumber",
	publish = true)
// @CucumberOptions(plugin = "json:target/cucumber-report.json")
// @CucumberOptions(
        // plugin = "json:target/cucumber-report.json",
        // glue = "cucumber",
        // features = "src/test/resources/",
				// strict=false
				// )
public class RunCucumberTest {
}
