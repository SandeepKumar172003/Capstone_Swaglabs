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
public class EndToEndTest extends BaseTest {

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
	
	@DataProvider
	public Object[][] logindata() throws IOException {

		String excelpath = projectpath + "\\src\\test\\resources\\Testdata\\login.xlsx";
		return ExcelUtiles.getdata(excelpath, "Sheet1");

	}

	@Test(priority = 1,dataProvider = "logindata")
	public void tc_060_purchaseSingleProduct(String username,String password) throws InterruptedException, IOException {
		test = extent.createTest("End to End Purchase Single Product");
		loginPage.login(username, password);
		Thread.sleep(3000);

		Assert.assertTrue(productsPage.isPageDisplayed());

		productsPage.addBackpackToCart();
		productsPage.goToCart();

		Assert.assertTrue(cartPage.isProductInCart("Sauce Labs Backpack"));

		cartPage.clickCheckout();

		checkoutPage.enterFirstName("John");
		checkoutPage.enterLastName("Doe");
		checkoutPage.enterPostalCode("12345");
		checkoutPage.clickContinue();

		Assert.assertTrue(overviewPage.isProductDisplayed("Sauce Labs Backpack"));

		overviewPage.clickFinish();

//		Assert.assertTrue(completePage.isOrderComplete());
		
		if(completePage.isOrderComplete()) {
			test.pass("Single product Oredered successfully");
		}else {
			String screenpath = ScreenShots.Capture(driver, "Single product Ordered Failed");
			test.fail("Single product Ordered Failed")
					.addScreenCaptureFromPath(screenpath);
		}

		completePage.clickBackHome();

		productsPage.logout();

	}

    @Test(priority = 2,dataProvider = "logindata")
    public void tc_061_purchaseMultipleProducts(String username,String password) throws InterruptedException, IOException {
        test = extent.createTest("End to End Purchase Multiple Products");

        loginPage.login(username, password);
		Thread.sleep(3000);

        Assert.assertTrue(productsPage.isPageDisplayed());

        productsPage.addBackpackToCart();
        productsPage.addBikeLightToCart();
        productsPage.goToCart();

        Assert.assertTrue(cartPage.isProductInCart("Sauce Labs Backpack"));
        Assert.assertTrue(cartPage.isProductInCart("Sauce Labs Bike Light"));

        cartPage.clickCheckout();

        checkoutPage.enterFirstName("Jane");
        checkoutPage.enterLastName("Smith");
        checkoutPage.enterPostalCode("54321");
        checkoutPage.clickContinue();

        Assert.assertTrue(overviewPage.isProductDisplayed("Sauce Labs Backpack"));
        Assert.assertTrue(overviewPage.isProductDisplayed("Sauce Labs Bike Light"));

        overviewPage.clickFinish();

//        Assert.assertTrue(completePage.isOrderComplete());
        
        if(completePage.isOrderComplete()) {
			test.pass("Multiple product Oredered successfully");
		}else {
			String screenpath = ScreenShots.Capture(driver, "Multiple product Ordered Failed");
			test.fail("Multiple product Ordered Failed")
					.addScreenCaptureFromPath(screenpath);
		}

        completePage.clickBackHome();

        productsPage.logout();

        test.pass("Multiple product checkout flow completed successfully");
    }
    
    @Test(priority = 3,dataProvider = "logindata")
    public void tc_sortAndCheckoutWithPriceValidation(String username,String password) throws InterruptedException, IOException {
        test = extent.createTest("TEnd-to-End Sort and Checkout with Price Validation");

        loginPage.login(username, password);
		Thread.sleep(3000);

        Assert.assertTrue(productsPage.isPageDisplayed());

        // Step 2: Sort products by Price (high to low)
        productsPage.sortByPriceHighToLow();

        // Step 3: Add top 3 products to cart
        String product1 = productsPage.getProductNameByIndex(0);
        String product2 = productsPage.getProductNameByIndex(1);
        String product3 = productsPage.getProductNameByIndex(2);

        productsPage.addProductToCart(product1);
        productsPage.addProductToCart(product2);
        productsPage.addProductToCart(product3);

        // Step 4: Open Cart and verify all 3 products are listed
        productsPage.goToCart();
        Assert.assertTrue(cartPage.isProductInCart(product1));
        Assert.assertTrue(cartPage.isProductInCart(product2));
        Assert.assertTrue(cartPage.isProductInCart(product3));

        // Step 5: Remove one product (let's remove the third one)
        cartPage.removeProduct(product3);
        Assert.assertTrue(cartPage.isProductInCart(product1));
        Assert.assertTrue(cartPage.isProductInCart(product2));
        Assert.assertFalse(cartPage.isProductInCart(product3));

        // Step 6: Checkout and enter user info
        cartPage.clickCheckout();

        checkoutPage.enterFirstName("John");
        checkoutPage.enterLastName("Doe");
        checkoutPage.enterPostalCode("12345");
        checkoutPage.clickContinue();

        // Step 7: Verify total price reflects the sum of the remaining 2 products

        double price1 = overviewPage.getProductPrice("Sauce Labs Fleece Jacket");
        double price2 = overviewPage.getProductPrice("Sauce Labs Backpack");
        double expectedSubtotal = price1 + price2;
        double actualSubtotal = overviewPage.getSubtotalPrice();

        Assert.assertEquals(actualSubtotal, expectedSubtotal, 0.01, "Subtotal mismatch");

        double expectedTax = Math.round(expectedSubtotal * 0.08 * 100.0) / 100.0;
        double actualTax = overviewPage.getTaxPrice();

        Assert.assertEquals(actualTax, expectedTax, 0.01, "Tax mismatch");

        double expectedTotal = expectedSubtotal + expectedTax;
        double actualTotal = overviewPage.getTotalPrice();

        Assert.assertEquals(actualTotal, expectedTotal, 0.01, "Total price mismatch");
        
        if (Math.abs(actualTotal - expectedTotal) < 0.01) {
        	test.pass("Sort and checkout with price validation completed successfully");
		}else {
			String screenpath = ScreenShots.Capture(driver, "Sort and checkout with price validation completed Failedy");
			test.fail("Sort and checkout with price validation completed Failed")
					.addScreenCaptureFromPath(screenpath);
		}


        // Step 8: Finish and confirm order
        overviewPage.clickFinish();

        Assert.assertTrue(completePage.isOrderComplete());
        completePage.clickBackHome();

        // Step 9: Logout
        productsPage.logout();

        
    }
    
    @Test(priority = 4,dataProvider = "logindata")
    public void tc_endToend_054_cartPersistenceAcrossLogoutAndLogin(String username,String password) throws InterruptedException, IOException {
        test = extent.createTest("TC_endToend_054 Cart Persistence Across Logout and Login");

        // Step 1 - Login
        loginPage.login(username, password);
		Thread.sleep(3000);
		
        Assert.assertTrue(productsPage.isPageDisplayed(), "Login failed");

        // Step 2 - Add product to cart
        productsPage.addProductToCart("Sauce Labs Backpack");

        // Step 3 - Logout
        productsPage.logout();

        // Step 4 - Login again
        loginPage.login("standard_user", "secret_sauce");
		Thread.sleep(3000);
        Assert.assertTrue(productsPage.isPageDisplayed(), "Login after logout failed");

        // Step 5 - Check product in cart
        productsPage.goToCart();
        Assert.assertTrue(cartPage.isProductInCart("Sauce Labs Backpack"), "Product not present in cart after relogin");
        if(cartPage.isProductInCart("Sauce Labs Backpack")) {
        	test.pass("Cart Persisted Across Logout and Login");
		}else {
			String screenpath = ScreenShots.Capture(driver, "Product not present in cart after relogin");
			test.fail("Cart not Persisted Across Logout and Login")
					.addScreenCaptureFromPath(screenpath);
		}
        
    }

    @Test(priority = 5,dataProvider = "logindata")
    public void tc_endToend_055_resetAppStateClearsCart(String username,String password) throws InterruptedException, IOException {
        test = extent.createTest("TC_endToend_055 Reset App State Clears Cart");

        // Step 1 - Login
        loginPage.login(username, password);
		Thread.sleep(3000);
        Assert.assertTrue(productsPage.isPageDisplayed(), "Login failed");

        // Step 2 - Add 4 products to the cart
        productsPage.addProductToCart("Sauce Labs Backpack");
        productsPage.addProductToCart("Sauce Labs Bike Light");
        productsPage.addProductToCart("Sauce Labs Bolt T-Shirt");
        productsPage.addProductToCart("Sauce Labs Fleece Jacket");

        // Verify products are added
        productsPage.goToCart();
        Thread.sleep(3000);
        Assert.assertEquals(cartPage.getCartItemCount(), 4, "Products not added to cart");

        // Step 3 - Reset app state from sidebar
        Thread.sleep(3000);
        productsPage.goToSidebar();
        Thread.sleep(2000);
        productsPage.clickResetAppState();

        // Step 4 - Refresh the page
        driver.navigate().refresh();
        Thread.sleep(3000);

        // Step 5 - Check that cart is empty
        productsPage.goToCart();	
        Thread.sleep(3000);
        Assert.assertEquals(cartPage.getCartItemCount(), 0, "Cart is not empty after resetting app state");
        if(cartPage.getCartItemCount()==0) {
        	test.pass("Reset app state cleared the cart successfully");
		}else {
			String screenpath = ScreenShots.Capture(driver, "Cart is not empty after resetting app state");
			test.fail("Reset app state cleared the cart Failed")
					.addScreenCaptureFromPath(screenpath);
		}

        
    }
    


}
