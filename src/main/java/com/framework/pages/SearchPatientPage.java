package com.framework.pages;

import com.framework.utils.LogUtils;
import com.framework.utils.Assertion;
import com.framework.utils.WaitUtil;

public class SearchPatientPage extends BasePage {

    private static final String CELL_SELECTOR_TEMPLATE = "div.pp-cell:has-text(\"%s\")";

    private final String First_name = ''
    private final String MRN_Input = "#ps-mrn";
    private final String Search_button = "#btnSearch";
    private final String View_button = "button.pp-btn-outline";
    private final String search_order_button = "button.pp-btn-primary";
    private final String reset="//button[@id='btnReset']";
    private final String Email = "#ps-tbody tr >> nth=0 >> .pp-details-grid > div >> nth=0 >> .pp-value";
    private final String home_phone = "#ps-tbody tr >> nth=0 >> .pp-details-grid > div >> nth=1 >> .pp-value";
    private final String member_phone = "#ps-tbody tr >> nth=0 >> .pp-details-grid > div >> nth=2 >> .pp-value";
    private final String preferred_language = "#ps-tbody tr >> nth=0 >> .pp-details-grid > div >> nth=3 >> .pp-value";
    private final String address = "#ps-tbody tr >> nth=0 >> .pp-details-grid > div >> nth=4 >> .pp-value";
    private final String height = "#ps-tbody tr >> nth=0 >> .pp-details-grid > div >> nth=5 >> .pp-value";
    private final String weight = "#ps-tbody tr >> nth=0 >> .pp-details-grid > div >> nth=6 >> .pp-value";
    private final String pageCurrent="button.page-btn.active";
    private final String pageDissable="button.page-btn";
    private final String pageSavaronRightICon="button.page-btn.| //button[contains(text(),'>')]";
    private final String pageCount="button.page-btn.dots > button.page-btn ";
    public void enterMRN(String mrn) {
        LogUtils.info("Entering MRN: " + mrn);
        fill(MRN_Input, mrn);
    }
    public void enterDefultName(){
        LogUtils.info("Entering DefaultFirstName: " +);
        fill(MRN_Input, );
    }
    public void clickReset(){
        click(reset);
        WaitUtil.waitForLoadingScreen();
        WaitUtil.waitForNetworkIdle();
    }
    private void clickCurrentPage(){
        click(pageCurrent);
    }
    private void clickSavaronRightIcon(){
        if(isVisible(pageDissable)){
        LogUtils.info("No further action can be taken");
        }
        else if (isVisible(pageSavaronRightICon)){
        click(pageSavaronRightICon);
        }
    }
    private void clickSavaronLeftIcon(){
        if(isVisible(pageDissable)){
        LogUtils.info("No further action can be taken");
        }
        else if (isVisible(pageSavaronRightICon)){
        click(pageSavaronRightICon);
        }
    }
    // private void 

    public void clickSearchPatient() {
        LogUtils.info("Clicking on Search Patient button...");
        click(Search_button);
        WaitUtil.waitForNetworkIdle();
    }

    public String getCellXpath(String value) {
        return String.format(CELL_SELECTOR_TEMPLATE, value);
    }

    public String getPatientResultMRN(String mrn) {
        String searchMrnLocator = getCellXpath(mrn);
        return getText(searchMrnLocator);
    }

    public void highlightMRNInput() {
        highlightElement(MRN_Input);
    }

    public void highlightSearchBtn() {
        highlightElement(Search_button);
    }

    public void highlightResult(String mrn) {
        highlightElement(getCellXpath(mrn));
    }

    public boolean isViewDetailsButtonVisible() {
        return isVisible(View_button);
    }

    public boolean isCreateOrderButtonVisible() {
        return isVisible(search_order_button);
    }

    public void highlightActionButtons() {
        highlightElement(View_button);
        highlightElement(search_order_button);
    }

    public void clickViewDetails() {
        LogUtils.info("Clicking on View Details button...");
        click(View_button);
        WaitUtil.waitForNetworkIdle();
    }

    public void clickCreateOrderInResult() {
        LogUtils.info("Clicking on Create Order button in result list...");
        click(search_order_button);
        WaitUtil.waitForLoadingScreen();
        //current system behaviour 
        handleMicrosoftLoginBypass();
    }

    public void verifyPatientDetailFieldVisible1() {
        universalWait(Email);
        boolean isEmailVisible = isVisible(Email);
        boolean isHomePhoneVisible = isVisible(home_phone);
        boolean isMemberPhoneVisible = isVisible(member_phone);
        boolean isPreferredLanguageVisible = isVisible(preferred_language);
        boolean isAddressVisible = isVisible(address);
        boolean isHeightVisible = isVisible(height);
        boolean isWeightVisible = isVisible(weight);

        Assertion.assertTrue(isEmailVisible, "Email field should be visible");
        Assertion.assertTrue(isHomePhoneVisible, "Home Phone field should be visible");
        Assertion.assertTrue(isMemberPhoneVisible, "Member Phone field should be visible");
        Assertion.assertTrue(isPreferredLanguageVisible, "Preferred Language field should be visible");
        Assertion.assertTrue(isAddressVisible, "Address field should be visible");
        Assertion.assertTrue(isHeightVisible, "Height field should be visible");
        Assertion.assertTrue(isWeightVisible, "Weight field should be visible");
    }

    public void highlightPatientDetailsField() {
        highlightElement(Email);
        highlightElement(home_phone);
        highlightElement(member_phone);
        highlightElement(preferred_language);
        highlightElement(address);
        highlightElement(height);
        highlightElement(weight);
    }

    public void scrollToViewDetailsButton() {
        scrollIntoView(View_button);
    }

    public void scrollToCreateOrderButton() {
        scrollIntoView(search_order_button);
    }

    public void scrollToPatientDetailsField() {
        scrollIntoView(Email);
    }
}
