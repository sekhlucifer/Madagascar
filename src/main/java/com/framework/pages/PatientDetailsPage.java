package com.framework.pages;

import com.framework.utils.LogUtils;
import com.framework.utils.WaitUtil;

public class PatientDetailsPage extends BasePage {

    private final String Final_create_order = "#btnNext";
    private final String current_page = ".breadcrumb > span";

    public String getCurrentPageText() {
        return getText(current_page);
    }

    public void clickFinalCreateOrder() {
        LogUtils.info("Clicking on Create Order button on Patient Details page...");
        click(Final_create_order);
        WaitUtil.waitForNetworkIdle();
    }

    public void scrollToCurrentPageText() {
        scrollIntoView(current_page);
    }

    public void highlightCurrentpage() {
        highlightElement(current_page);
    }
}
