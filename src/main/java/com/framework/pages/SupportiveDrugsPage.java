package com.framework.pages;

import com.framework.utils.LogUtils;
import com.framework.utils.Assertion;
import com.framework.utils.WaitUtil;

public class SupportiveDrugsPage extends BasePage {

    private final String current_page = ".breadcrumb > span";
    private final String submit_supportive_drug = "#submitOrder";
    private final String newly_add_Drugs = "button.newly-added-btn";
    private final String confirm_delete_drug = "#deleteDrug";
    private final String add_drug_submit = "#saveBtn";
    private final String Fill_doseamount = "#editdoseInput";
    private final String submit_order_popup = "#successPopupOverlay .popup-box .successpopup-title";
    private final String supportive_remove_action = "#drugTableBody tr.row-removed td.change-action button.removed-badge";
    private final String strikethrough = "#drugTableBody tr.row-removed td.dose-unit >> nth=0";
    private final String restore_icon_grey_supp = "#drugTableBody tr.row-removed td.actions";
    private final String grey_text = "tr.row-removed";
    private final String add_drugs_input = "#drugInput";
    private final String select_drugs_template = "div.searchoptions:has-text(\"%s\")";
    private final String comment = "#commentInput";
    private final String add_Drug_button = "#createDrugBtn";
    
    private final String ADD_DRUG_BUTTON = "#addDrug";
    private final String drug_name_input = "#drugInput";
    private final String Supportive_drug_dropdown = "#optionBox div.searchoptions >> nth=0";

    private static final String DRUG_ROW_XPATH_TEMPLATE = "#drugTableBody tr >> nth=%d";
    private static final String DRUG_ROW_STATUS_XPATH_TEMPLATE = "#drugTableBody tr >> nth=%d >> td.status";
    private static final String DRUG_ROW_EDIT_DOSE_XPATH = "#drugTableBody td.actions svg >> nth=1";
    private static final String DRUG_ROW_DELETE_XPATH = "#drugTableBody td.actions svg >> nth=0";
    private static final String DRUG_ROW_ACTUAL_DOSE_XPATH_TEMPLATE = "#drugTableBody tr >> nth=%d >> td.actual-dose";
    private static final String DRUG_ROW_STRIKETHROUGH_XPATH_TEMPLATE = "#drugTableBody tr.row-removed td.dose-unit >> nth=%d";
    private static final String DRUG_ROW_GREY_ROW_XPATH_TEMPLATE = "#drugTableBody tr.row-removed >> nth=%d";

    public void verify_supportive_drug_page() {
        waitForElement(submit_supportive_drug);
        Assertion.assertTrue(isVisible(submit_supportive_drug), "Submit supportive drug button is visible");
        highlightElement(submit_supportive_drug);
        waitForLoadingSpinner();
        waitForPageToLoad();
        universalWait();
    }



    public void add_Drugs(String addDrugs, String commentText) {
        click(ADD_DRUG_BUTTON);
        WaitUtil.waitForNetworkIdle();
        click(add_drugs_input);
        String selectDrugsXpath = String.format(select_drugs_template, addDrugs);
        waitForElement(selectDrugsXpath, 10);
        click(selectDrugsXpath);
        fill(comment, commentText);
        WaitUtil.waitForNetworkIdle();
        click(add_Drug_button);
        WaitUtil.waitForNetworkIdle();
        universalWait(newly_add_Drugs);
    }

    public void add_suppotive_drug(String drugName) {
        click(ADD_DRUG_BUTTON);
        click(drug_name_input);
        fill(drug_name_input, drugName);
        click(Supportive_drug_dropdown);
        Assertion.assertTrue(isClickable(Supportive_drug_dropdown), "Supportive drug dropdown is visible");
        highlightElement(Supportive_drug_dropdown);
    }

    public void newly_add_drugs() {
        Assertion.assertTrue(isVisible(newly_add_Drugs), "Newly added badge is visible");
        scrollIntoView(newly_add_Drugs);
        highlightElement(newly_add_Drugs);
    }



    public void check_approval1(String expectedStatus) {
        LogUtils.info("Checking approval status in the table...");
        boolean isFound = false;
        int i = 0;
        while (true) {
            String rowXpath = String.format(DRUG_ROW_XPATH_TEMPLATE, i);
            if (!isElementPresent(rowXpath)) {
                break;
            }
            String xpath = String.format(DRUG_ROW_STATUS_XPATH_TEMPLATE, i);
            if (isElementPresent(xpath)) {
                String actualStatus = getText(xpath);
                LogUtils.info("Row " + (i + 1) + " status text: " + actualStatus);
                if (actualStatus.toLowerCase().contains(expectedStatus.toLowerCase())) {
                    highlightElement(xpath);
                    isFound = true;
                    break;
                }
            }
            i++;
        }
        Assertion.assertTrue(isFound,
                "No approval status pill found containing '" + expectedStatus + "' in the table.");
    }



    public void edit_Supportive_drug_dose(String doseValue) {
        click(DRUG_ROW_EDIT_DOSE_XPATH);
        waitForElement(Fill_doseamount, 10);
        fill(Fill_doseamount, doseValue);
        click(add_drug_submit);
        waitForPageToLoad();
        WaitUtil.waitForNetworkIdle();
        universalWait(newly_add_Drugs);
    }



    public void verify_supportive_drug_dose(String expectedDose) {
        universalWait();
        LogUtils.info("Checking updated dose value in the drug table...");
        boolean isFound = false;
        int i = 0;
        while (true) {
            String rowXpath = String.format(DRUG_ROW_XPATH_TEMPLATE, i);
            if (!isElementPresent(rowXpath)) {
                break;
            }
            String xpath = String.format(DRUG_ROW_ACTUAL_DOSE_XPATH_TEMPLATE, i);
            if (isElementPresent(xpath)) {
                String actualDose = getText(xpath);
                LogUtils.info("Row " + (i + 1) + " dose text: " + actualDose);
                if (actualDose.toLowerCase().contains(expectedDose.toLowerCase())) {
                    highlightElement(xpath);
                    scrollIntoView(xpath);
                    isFound = true;
                    break;
                }
            }
            i++;
        }
        Assertion.assertTrue(isFound, "Updated dose " + expectedDose + " is not visible in the drug table.");
    }

    public void delete_supportive_drug() {
        click(DRUG_ROW_DELETE_XPATH);
        waitForPageToLoad();
        click(confirm_delete_drug);
        waitForPageToLoad();
        universalWait();
    }

    public void verify_supportive_remove_action() {
        waitForPageToLoad();
        waitForElement(supportive_remove_action, 10);
        Assertion.assertTrue(isVisible(supportive_remove_action), "Remove action is visible");
        highlightElement(supportive_remove_action);
    }

    public void check_strikethrough_is_visible() {
        waitForPageLoad();
        int maxAttempts = 10;
        boolean isFound = false;
        String visibleXpath = "";
        for (int i = 0; i < maxAttempts; i++) {
            String xpath = String.format(DRUG_ROW_STRIKETHROUGH_XPATH_TEMPLATE, i);
            if (isElementPresent(xpath) && isVisible(xpath)) {
                visibleXpath = xpath;
                isFound = true;
                break;
            }
        }
        Assertion.assertTrue(isFound, "Strikethrough is visible");
        if (isFound && !visibleXpath.isEmpty()) {
            scrollIntoView(visibleXpath);
            highlightElement(visibleXpath);
        }
    }

    public void check_grey_text_is_visible() {
        int maxAttempts = 10;
        boolean isFound = false;
        String visibleXpath = "";
        for (int i = 0; i < maxAttempts; i++) {
            String xpath = String.format(DRUG_ROW_GREY_ROW_XPATH_TEMPLATE, i);
            if (isElementPresent(xpath) && isVisible(xpath)) {
                visibleXpath = xpath;
                isFound = true;
                break;
            }
        }
        Assertion.assertTrue(isFound, "Grey text is visible");
        if (isFound && !visibleXpath.isEmpty()) {
            scrollIntoView(visibleXpath);
            highlightElement(visibleXpath);
        }
    }

    public void submit_order() {
        waitForElement(submit_supportive_drug);
        click(submit_supportive_drug);
        waitForPageToLoad();
        WaitUtil.waitForNetworkIdle();
        universalWait();
    }

    public void check_submit_popup() {
        WaitUtil.waitForLoadingScreen();
        waitForElement(submit_order_popup, 10);
        String actual_popup_title = getText(submit_order_popup);
        Assertion.assertEquals(actual_popup_title, "Order Successful", "Popup title is matching");
        highlightElement(submit_order_popup);
    }

    public void scrollToSubmitSupportiveDrug() {
        scrollIntoView(submit_supportive_drug);
    }

    public void highlightCurrentpage() {
        highlightElement(current_page);
    }
    
}
