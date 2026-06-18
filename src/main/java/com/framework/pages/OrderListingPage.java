package com.framework.pages;

import com.framework.utils.LogUtils;
import static com.framework.config.ConfigurationManager.config;
import com.framework.utils.Assertion;
import com.framework.utils.WaitUtil;
import com.microsoft.playwright.*;

public class OrderListingPage extends BasePage {

    private final String Create_Order = "#createorder";
    private final String Orderlist_mrn_number = "#gridTable .grid-row >> nth=0 >> div >> nth=0 >> a.mrn-link";
    private final String Orderlist_description = "#gridTable .grid-row >> nth=0 >> div >> nth=2";
    private final String Orderlist_Status = "#gridTable .grid-row >> nth=0 >> div >> nth=3";
    private final String Orderlist_Approval_status = "#gridTable .grid-row >> nth=0 >> div >> nth=4";
    private final String order_page = "#mainContent .main-container .page-topbar";
    private final String logout = "#logoutBtn";
    private final String loginbutton = "//button[@name='provider']";
     
    public OrderListingPage open() {
        LogUtils.info("Navigating to Order Listing page");
        navigateTo(config().baseUrl());
        handleMicrosoftLoginBypass();
        return this;
    }

    public void clickCreateOrder() {
        LogUtils.info("Clicking on Create Order button...");
        click(Create_Order);
        WaitUtil.waitForLoadingScreen();
        handleMicrosoftLoginBypass();
        WaitUtil.waitForLoadingScreen();
        WaitUtil.waitForNetworkIdle();
    }

    public void login() {
        if (isVisible(loginbutton)) {
            click(loginbutton);
            WaitUtil.waitForLoadingScreen();
            handleMicrosoftLoginBypass();
        } else {
            handleMicrosoftLoginBypass();
        }
    }

    public void verify_orderlist_page(String mrn, String description) {
        WaitUtil.waitForLoadingScreen();
        WaitUtil.waitForLoadingScreen();

        String actualmrn = getText(Orderlist_mrn_number);
        String actual_Description = getText(Orderlist_description);

        Assertion.assertContains(actualmrn, mrn, "MRN Number matched");
        Assertion.assertContains(actual_Description, description, "Description matched");
        highlightElement(Orderlist_mrn_number);
        highlightElement(Orderlist_description);
    }

    public void verify_redirection_to_order_listing() {
        // WaitUtil.waitForLoadingScreen();
        LogUtils.info("Waiting for redirection to Order Listing page...");
        boolean isVisible = waitForElement(order_page, 30);
        Assertion.assertTrue(isVisible, "Order Listing page (order_page) is visible");
        highlightElement(order_page);
    }

    public void verify_status() {
        String actualStatus = getText(Orderlist_Status);
        String actualApprovalStatus = getText(Orderlist_Approval_status);

        Assertion.assertContains(actualStatus, "Submitted", "Status matched");
        boolean isMatched = actualApprovalStatus.toLowerCase().contains("requires approval")
                || actualApprovalStatus.toLowerCase().contains("approved") ||
                actualApprovalStatus.toLowerCase().contains("require approval");
        Assertion.assertTrue(isMatched, "Approval status matched: " + actualApprovalStatus);
        Assertion.assertTrue(isClickable(Orderlist_mrn_number), "Mrn is Hyperlink");

        highlightElement(Orderlist_Status);
        highlightElement(Orderlist_Approval_status);
    }

    public void click_mrn_hyperlink() {
        waitForElement(Orderlist_mrn_number);
        click(Orderlist_mrn_number);
        waitForPageToLoad();
        WaitUtil.waitForNetworkIdle();
        // universalWait();
    }

    public void logout() {
        click(logout);
    }

    public void scrollToOrderlistStatus() {
        scrollIntoView(Orderlist_Status);
    }
}
