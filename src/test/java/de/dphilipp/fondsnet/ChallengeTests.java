package de.dphilipp.fondsnet;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testcontainers.containers.BrowserWebDriverContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.File;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@DisplayName("Starts the docker container once before the tests are executed")
@Slf4j
class ChallengeTests {

    private static final File TARGET_DIRECTORY = new File("./target/");
    private static final ChromeOptions CHROME_OPTIONS = new ChromeOptions()
            // User agent is not strictly required, but some sites may refuse communication without
            .addArguments(String.format("user-agent=%s", "weathershopper-test"))
            .addArguments("--log-level=OFF", "--incognito");

    @Container
    private static final BrowserWebDriverContainer BROWSER_CONTAINER = new BrowserWebDriverContainer()
            .withCapabilities(CHROME_OPTIONS)
            //.withRecordingMode(BrowserWebDriverContainer.VncRecordingMode.SKIP, TARGET_DIRECTORY);
            //.withRecordingMode(BrowserWebDriverContainer.VncRecordingMode.RECORD_FAILING, TARGET_DIRECTORY);
            .withRecordingMode(BrowserWebDriverContainer.VncRecordingMode.RECORD_ALL, TARGET_DIRECTORY);


    private static WebDriver browser;

    @BeforeAll
    static void configureBrowser() {
        browser = BROWSER_CONTAINER.getWebDriver();
        browser.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        browser.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(30));
    }


    @Test
    @DisplayName("fondsnet qa challenge sanity check")
    // As long as this test works the prerequisites for executing this project are upheld and the test page is available
    void fondsnetSanityCheck() {
        browser.get("https://www.fondsnet.com");
        assertThat(browser.getTitle()).isEqualTo("FONDSNET - Service. Kompetenz. Innovation.");
    }

    @Test
    @DisplayName("fondsnet qa challenge task 1")
    /*
        ● Aufruf https://www.fondsnet.com/
        ● Im Portal einloggen: (redacted credentials)
        ● Finde über die Schnellsuche (rechts oben) „Maximiliane Mustermann“
        ● Prüfe, ob ein entsprechender Datensatz gefunden wird
        ● Kundendaten von Maximilian Mustermann aufrufen
        ● Prüfe, ob der richtige Kunde geöffnet wurde
        ● Überprüfe, dass folgender Text nicht angezeigt wird: „Fehler-ID“
    */
    void fondsnetTaskOne() {
        browser.get("https://www.fondsnet.com");

        var loginElement = browser.findElement(new By.ByClassName("login-widget__link"));
        loginElement.click();

        var oldBrowserHandles = new ArrayList<String>();
        oldBrowserHandles.add(browser.getWindowHandle());
        switchToNewTab(oldBrowserHandles);
        waitUntilPageLoad("Anmeldung");

        var usernameElement = browser.findElement(new By.ById("mat-input-0"));
        patientSendKeys(usernameElement, System.getenv("FONDSNET_USERNAME"));

        var passwordElement = browser.findElement(new By.ById("mat-input-1"));
        patientSendKeys(passwordElement, System.getenv("FONDSNET_PASSWORD"));

        var loginButtonElement = browser.findElement(new By.ByXPath("//button[contains(text(), 'Login')]"));
        loginButtonElement.click();

        waitUntilPageLoad("Dashboard ⋅ Evolution");

        var quickSearchElement = browser.findElement(new By.ById("clr-form-control-1"));
        patientSendKeys(quickSearchElement, "Maximiliane Mustermann");
        //could add a wait or deal with quick results here
        quickSearchElement.sendKeys(Keys.RETURN);

        waitUntilPageLoad("Schnellsuche ⋅ Evolution");

        // deviating from the task, as there is no customer with name Maximilian Mustermann,
        // but one with the name Maximiliane Mustermann, as in the search query
        var customerLinkElement = browser.findElement(new By.ByPartialLinkText("Mustermann, Maximiliane"));
        assertThat(customerLinkElement.getAttribute("href")).contains("/kunden/");

        customerLinkElement.click();

        oldBrowserHandles.add(browser.getWindowHandle());
        switchToNewTab(oldBrowserHandles);
        waitUntilPageLoad("Übersicht ⋅ Maximiliane Mustermann ⋅ Evolution");

        var personTextElement = browser.findElement(new By.ByXPath("//ancestor::div[contains(text(), 'Person')]"));
        var personNameElement = personTextElement.findElement(new By.ByXPath("parent::div"));
        assertThat(personNameElement.getText()).contains("Frau Maximiliane Mustermann");

        assertThat(browser.findElement(new By.ByXPath("/*")).getText()).doesNotContain("Fehler-ID");
    }

    @Test
    @DisplayName("fondsnet qa challenge task 2")
    /*
        ● Aufruf https://www.fondsnet.com/
        ● Im Portal einloggen: (redacted credentials)
        ● Öffne über „Investment“ die „Produktsuche“
        ● Filtere die Produktsuche mit folgenden Kriterien:
            o Das Feld Fondsname mit „hausinvest“
            o Das Dropdown Fondskategorie mit „Immobilienfonds“
        ● Suche nach dem Produkt
        ● Öffne das Produkt
        ● Überprüfe, dass der Text „Investmentfonds hausInvest“ angezeigt wird
    */
    void fondsnetTaskTwo() {
        /*
            Instructions were a little confusing for this task, as especially for the 2 sub bullet points I didn't
            find any logical place to filter both with category and fondsname at the same time.
            I followed an approach that should allow display of related skills to the best of my ability.
         */
        browser.get("https://www.fondsnet.com");

        var loginElement = browser.findElement(new By.ByClassName("login-widget__link"));
        loginElement.click();

        var oldBrowserHandles = new ArrayList<String>();
        oldBrowserHandles.add(browser.getWindowHandle());
        switchToNewTab(oldBrowserHandles);
        waitUntilPageLoad("Anmeldung");

        var usernameElement = browser.findElement(new By.ById("mat-input-0"));
        patientSendKeys(usernameElement, System.getenv("FONDSNET_USERNAME"));

        var passwordElement = browser.findElement(new By.ById("mat-input-1"));
        patientSendKeys(passwordElement, System.getenv("FONDSNET_PASSWORD"));

        var loginButtonElement = browser.findElement(new By.ByXPath("//button[contains(text(), 'Login')]"));
        loginButtonElement.click();

        waitUntilPageLoad("Dashboard ⋅ Evolution");

        var hamburgerMenuButton = browser.findElement(new By.ByClassName("header-hamburger-trigger"));
        hamburgerMenuButton.click();

        var insuranceLinkElement = browser.findElement(new By.ByLinkText("Investment"));
        insuranceLinkElement.click();

        waitUntilPageLoad("Investment");

        var hamburgerMenuCloseButton = browser.findElement(new By.ByClassName("clr-nav-close"));
        hamburgerMenuCloseButton.click();

        var threeDotMenuElement = browser.findElement(new By.ByClassName("header-overflow-trigger"));
        threeDotMenuElement.click();

        var fondToolsGroupElement = browser.findElement(new By.ByXPath("//clr-vertical-nav-group[@title='Fondstools']"));
        fondToolsGroupElement.click();

        var topPerformerElement = browser.findElement(new By.ByLinkText("Top-Performer"));
        topPerformerElement.click();
        // do it a second time to trigger a page reload that also closes the three dot menu
        topPerformerElement.click();
        waitUntilPageLoad("Top-Performer ⋅ Evolution");

        var spaLegacyIframe = browser.findElement(new By.ByXPath("//iframe[@name='spa-legacy-iframe']"));
        browser.switchTo().frame(spaLegacyIframe);

        var webDriverWait = new WebDriverWait(browser, Duration.ofSeconds(30));
        var fondsCategoryDropdrownElement = webDriverWait.until(ExpectedConditions.visibilityOfElementLocated(new By.ById("anlageklasse")));
        Select select = new Select(fondsCategoryDropdrownElement);
        select.selectByValue("/produkte/fonds_topperformer.php?anlageklasse=Immobilienfonds");

        var houseInvestProductLinkElement = browser.findElement(new By.ByLinkText("hausInvest"));
        houseInvestProductLinkElement.click();

        oldBrowserHandles.add(browser.getWindowHandle());
        switchToNewTab(oldBrowserHandles);
        waitUntilPageLoad("");

        // at the time of writing this test opening the product always ends in an error, therefor this can't be validated
        assertThat(browser.findElement(new By.ByXPath("/*")).getText()).contains("Investmentfonds hausInvest");
    }

    void switchToNewTab(List<String> oldBrowserHandles) {
        for(var windowHandle : browser.getWindowHandles()) {
            if(!oldBrowserHandles.contains(windowHandle)) {
                browser.switchTo().window(windowHandle);
                break;
            }
        }
    }

    @SneakyThrows
    void patientSendKeys(WebElement element, String keys) {
        for(Character key : keys.toCharArray()){
            element.sendKeys(key.toString());
            Thread.sleep(20);
        }
    }

    @SneakyThrows
    void waitUntilPageLoad(String title){

        var START_INSTANT = Instant.now();
        while(Instant.now().minusSeconds(15).isBefore(START_INSTANT)
        && !browser.getTitle().equals(title)){
            Thread.sleep(50);
            browser.switchTo().defaultContent();
        }
        if(!browser.getTitle().equals(title)) {
            log.error("expected '{}' but got '{}'", title, browser.getTitle());
            throw new TimeoutException();
        }
    }
}