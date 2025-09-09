package com.swaglabs.test;

import java.io.IOException;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import com.swaglabs.base.BaseTest;
import com.swaglabs.listeners.TestListener;
import com.swaglabs.pages.CartPage;
import com.swaglabs.pages.CheckoutCompletePage;
import com.swaglabs.pages.CheckoutOverviewPage;
import com.swaglabs.pages.CheckoutPage;
import com.swaglabs.pages.LoginPage;
import com.swaglabs.pages.ProductPage;
import com.swaglabs.utilites.ExcelUtiles;
import com.swaglabs.utilites.ScreenShots;

@Listeners(TestListener.class)
public class End1 extends BaseTest {

    LoginPage loginPage;
    ProductPage productsPage;
    CartPage cartPage;
    CheckoutPage checkoutPage;
    CheckoutOverviewPage overviewPage;
    CheckoutCompletePage completePage;

    static String projectpath = System.getProperty("user.dir");

    @BeforeMethod
    public void init() {
        loginPage = new LoginPage(driver);
        productsPage = new ProductPage(driver);
        cartPage = new CartPage(driver);
        checkoutPage = new CheckoutPage(driver);
        overviewPage = new CheckoutOverviewPage(driver);
        completePage = new CheckoutCompletePage(driver);
    }

    // 🔹 Utility method to slow down execution
    public void pause(int seconds) {
        try {
            Thread.sleep(seconds * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @DataProvider
    public Object[][] logindata() throws IOException {
        String excelpath = projectpath + "\\src\\test\\resources\\Testdata\\login.xlsx";
        return ExcelUtiles.getdata(excelpath, "Sheet1");
    }

    @Test(priority = 1, dataProvider = "logindata")
    public void tc_060_purchaseSingleProduct(String username, String password) throws InterruptedException, IOException {
        test = extent.createTest("End to End Purchase Single Product");

        loginPage.login(username, password);
        pause(2);  // wait 2 sec

        Assert.assertTrue(productsPage.isPageDisplayed());
        pause(2);

        productsPage.addBackpackToCart();
        pause(2);

        productsPage.goToCart();
        pause(2);

        Assert.assertTrue(cartPage.isProductInCart("Sauce Labs Backpack"));
        pause(2);

        cartPage.clickCheckout();
        pause(2);

        checkoutPage.enterFirstName("John");
        pause(1);
        checkoutPage.enterLastName("Doe");
        pause(1);
        checkoutPage.enterPostalCode("12345");
        pause(1);
        checkoutPage.clickContinue();
        pause(2);

        Assert.assertTrue(overviewPage.isProductDisplayed("Sauce Labs Backpack"));
        pause(2);

        overviewPage.clickFinish();
        pause(2);

        if (completePage.isOrderComplete()) {
            test.pass("Single product Ordered successfully");
        } else {
            String screenpath = ScreenShots.Capture(driver, "Single product Ordered Failed");
            test.fail("Single product Ordered Failed")
                .addScreenCaptureFromPath(screenpath);
        }
        pause(2);

        completePage.clickBackHome();
        pause(2);

        productsPage.logout();
        pause(2);
    }
}
