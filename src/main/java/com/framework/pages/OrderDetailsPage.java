package com.framework.pages;

import com.framework.utils.LogUtils;
import com.framework.utils.Assertion;
import com.framework.utils.WaitUtil;

public class OrderDetailsPage extends BasePage {

    private final String current_page = ".breadcrumb > span";
    private final String Order_number = ".page-title";
    private final String regimen_details1 = "#taskCardsContainer span.task-title >> nth=0";
    private final String regimen_details2 = "#taskCardsContainer span.task-title >> nth=1";
    private final String regimen_details3 = "#taskCardsContainer span.task-title >> nth=2";
    private final String regimen_details4 = "#taskCardsContainer span.task-title >> nth=3";

    private final String pathway_view_detalis = "//div[@data-title='TOI- R-CVP']";// "div.view-details-btn[data-title='TOI-
                                                                                  // R-CVP']";
    private final String Add_Drugs = "#addDrug";
    private final String Regimen_details_popup = "#pathwayModal";
    private final String drugs_details = ".drug-card";

    private final String drugs1_dose = ".drug-card >> span.value-text >> nth=1";
    private final String drugs1_route = ".drug-card >> span.value-text >> nth=2";
    private final String drugs2_dose = ".drug-card >> span.value-text >> nth=5";
    private final String drugs2_route = ".drug-card >> span.value-text >> nth=6";
    private final String drugs3_dose = ".drug-card >> span.value-text >> nth=9";
    private final String drugs3_route = ".drug-card >> span.value-text >> nth=10";
    private final String drugs4_dose = ".drug-card >> span.value-text >> nth=13";
    private final String drugs4_route = ".drug-card >> span.value-text >> nth=14";

    private final String drugs1_name = ".drug-title >> nth=0";
    private final String drugs2_name = ".drug-title >> nth=1";
    private final String drugs3_name = ".drug-title >> nth=2";
    private final String drugs4_name = ".drug-title >> nth=3";

    private final String Drugs_Card1 = ".drug-card >> nth=0";
    private final String Drugs_Card2 = ".drug-card >> nth=1";
    private final String Drugs_Card3 = ".drug-card >> nth=2";
    private final String Drugs_Card4 = ".drug-card >> nth=3";

    private final String Table_Drugs1 = "#pathwayTableBody tr >> nth=0 >> td.drug-name";
    private final String Table_Drugs2 = "#pathwayTableBody tr >> nth=1 >> td.drug-name";
    private final String Table_Drugs3 = "#pathwayTableBody tr >> nth=2 >> td.drug-name";
    private final String Table_Drugs4 = "#pathwayTableBody tr >> nth=3 >> td.drug-name";

    private final String Use_pathway_button = "#pathwayModal button.btn-usePathway";
    private final String add_drugs_input = "#drugInput";
    private final String select_drugs_template = "div.searchoptions:has-text(\"%s\")";
    private final String comment = "#commentInput";
    private final String add_Drug_button = "#createDrugBtn";
    private final String newly_add_Drugs = "button.newly-added-btn";
    private final String Edit_drug_dose = "#pathwayTableBody td.actions svg >> nth=1";
    private final String Fill_doseamount = "#editdoseInput";
    private final String add_drug_submit = "#saveBtn";
    private final String delete_drug = "#pathwayTableBody tr >> nth=1 >> td.actions svg >> nth=0";
    private final String confirm_delete_drug = "#deleteDrug";
    private final String remove_action = "button.status-pill-removed";
    private final String move_to_supportive_drug = "#supportiveDrugBtn";

    private final String ViewDetails_cancle = "#pathwayModal button.secondary";
    private final String ViewDetails_usepathway = "#pathwayModal button.btn-usePathway";

    private final String restore_icon_grey = "#pathwayTableBody tr.row-removed td.actions";

    private static final String PATHWAY_ROW_DRUG_NAME_XPATH_TEMPLATE = "#pathwayTableBody tr >> nth=%d >> td.drug-name";
    private static final String PATHWAY_ROW_XPATH_TEMPLATE = "#pathwayTableBody tr >> nth=%d";
    private static final String PATHWAY_ROW_APPROVAL_STATUS_XPATH_TEMPLATE = "#pathwayTableBody tr >> nth=%d >> td.status span.status-pill.require";
    private static final String PATHWAY_ROW_STRIKETHROUGH_XPATH_TEMPLATE = "#pathwayTableBody tr.row-removed td.dose-unit >> nth=%d";
    private static final String PATHWAY_ROW_GREY_ROW_XPATH_TEMPLATE = "#pathwayTableBody tr.row-removed >> nth=%d";

    private static final String REGIMEN_CARD_INDEX_XPATH_TEMPLATE = "#taskCardsContainer > div >> nth=%d";

    public void check_ordernumber() {
        waitForElement(Order_number, 10);
        boolean is_ordernumber_visible = isVisible(Order_number);
        Assertion.assertTrue(is_ordernumber_visible, "Order Number is not visible");
    }



    public void check_regimens_details(String r1, String r2, String r3, String r4) {
        waitForElement(regimen_details4, 15);
        String regimen1 = getText(regimen_details1);
        String regimen2 = getText(regimen_details2);
        String regimen3 = getText(regimen_details3);
        String regimen4 = getText(regimen_details4);
        WaitUtil.waitForNetworkIdle();
        highlightregimecard();
        Assertion.assertEquals(regimen1, r1, "Regimen 1 is matching");
        Assertion.assertEquals(regimen2, r2, "Regimen 2 is matching");
        Assertion.assertEquals(regimen3, r3, "Regimen 3 is matching");
        Assertion.assertEquals(regimen4, r4, "Regimen 4 is matching");
    }

    public void click_view_details() {
        universalWait(pathway_view_detalis);
        click(pathway_view_detalis);
        waitForElement(Regimen_details_popup, 10);
        highlightElement(Regimen_details_popup);
        highlightElement(Drugs_Card1);
    }

    public boolean areCancelAndUsePathwayButtonsVisible() {
        boolean result = isVisible(ViewDetails_cancle) && isVisible(ViewDetails_usepathway);
        highlightElement(ViewDetails_cancle);
        highlightElement(ViewDetails_usepathway);
        return result;
    }

    public void check_Cancel_Use_Pathway() {
        universalWait(pathway_view_detalis);
        click(pathway_view_detalis);
        waitForElement(ViewDetails_cancle, 10);
        boolean result = isVisible(ViewDetails_cancle) && isVisible(ViewDetails_usepathway);
        highlightElement(ViewDetails_cancle);
        highlightElement(ViewDetails_usepathway);
        Assertion.assertTrue(result, "Cancel and Use pathway button are not visible");
    }

    public void validate_drugs_data(
            String d1Name, String d1Dose, String d1Route,
            String d2Name, String d2Dose, String d2Route,
            String d3Name, String d3Dose, String d3Route,
            String d4Name, String d4Dose, String d4Route) {
        waitForElement(drugs1_name, 10);
        String Drug1_name = getText(drugs1_name);
        String Drug2_name = getText(drugs2_name);
        String Drug3_name = getText(drugs3_name);
        String Drug4_name = getText(drugs4_name);
        WaitUtil.waitForNetworkIdle();
        Assertion.assertEquals(Drug1_name, d1Name, "Drug 1 is matching");
        Assertion.assertEquals(Drug2_name, d2Name, "Drug 2 is matching");
        Assertion.assertEquals(Drug3_name, d3Name, "Drug 3 is matching");
        Assertion.assertEquals(Drug4_name, d4Name, "Drug 4 is matching");

        String Drug1_dose = getText(drugs1_dose);
        String Drug2_dose = getText(drugs2_dose);
        String Drug3_dose = getText(drugs3_dose);
        String Drug4_dose = getText(drugs4_dose);
        Assertion.assertEquals(Drug1_dose, d1Dose, "Drug 1 dose is matching");
        Assertion.assertEquals(Drug2_dose, d2Dose, "Drug 2 dose is matching");
        Assertion.assertEquals(Drug3_dose, d3Dose, "Drug 3 dose is matching");
        Assertion.assertEquals(Drug4_dose, d4Dose, "Drug 4 dose is matching");

        String Drug1_Route = getText(drugs1_route);
        String Drug2_Route = getText(drugs2_route);
        String Drug3_Route = getText(drugs3_route);
        String Drug4_Route = getText(drugs4_route);
        Assertion.assertEquals(Drug1_Route, d1Route, "Drug 1 route is matching");
        Assertion.assertEquals(Drug2_Route, d2Route, "Drug 2 route is matching");
        Assertion.assertEquals(Drug3_Route, d3Route, "Drug 3 route is matching");
        Assertion.assertEquals(Drug4_Route, d4Route, "Drug 4 route is matching");
    }

    public void click_Use_Pathway() {
        click(Use_pathway_button);
        WaitUtil.waitForLoadingScreen();
        WaitUtil.waitForNetworkIdle();
        waitForPageLoad();
    }

    public void Verify_drugs(String d1Name, String d2Name, String d3Name, String d4Name) {
        waitForElement(Table_Drugs1, 10);
        String table_drug1_name = getText(Table_Drugs1);
        String table_drug2_name = getText(Table_Drugs2);
        String table_drug3_name = getText(Table_Drugs3);
        String table_drug4_name = getText(Table_Drugs4);
        WaitUtil.waitForNetworkIdle();
        Assertion.assertEquals(table_drug1_name, d1Name, "Drug 1 is matching");
        Assertion.assertEquals(table_drug2_name, d2Name, "Drug 2 is matching");
        Assertion.assertEquals(table_drug3_name, d3Name, "Drug 3 is matching");
        Assertion.assertEquals(table_drug4_name, d4Name, "Drug 4 is matching");
    }

    public void highlight_table_drugs() {
        highlightElement(Table_Drugs1);
        highlightElement(Table_Drugs2);
        highlightElement(Table_Drugs3);
        highlightElement(Table_Drugs4);
    }

    public void highlightDrugs_Card() {
        highlightElement(Drugs_Card1);
        highlightElement(Drugs_Card2);
        highlightElement(Drugs_Card3);
        highlightElement(Drugs_Card4);
    }



    public void add_Drugs(String addDrugs, String commentText) {
        click(Add_Drugs);
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



    public void verify_AddDrugs(String addedDrugName) {
        LogUtils.info("Verifying added drug is visible in table: " + addedDrugName);
        boolean isFound = false;
        int i = 0;
        while (true) {
            String xpath = String.format(PATHWAY_ROW_DRUG_NAME_XPATH_TEMPLATE, i);
            if (!isElementPresent(xpath)) {
                break;
            }
            String drugName = getText(xpath);
            LogUtils.info("Row " + (i + 1) + " drug name: " + drugName);
            if (drugName.equals(addedDrugName) || drugName.contains(addedDrugName)) {
                isFound = true;
                highlightElement(xpath);
                break;
            }
            i++;
        }
        Assertion.assertTrue(isFound, "Added drug " + addedDrugName + " is not visible in the table.");
    }

    public void newly_add_drugs() {
        Assertion.assertTrue(isVisible(newly_add_Drugs), "Newly added badge is visible");
        scrollIntoView(newly_add_Drugs);
        highlightElement(newly_add_Drugs);
    }



    public void check_approval(String expectedStatus) {
        LogUtils.info("Checking approval status in the table...");
        boolean isFound = false;
        int i = 0;
        while (true) {
            String rowXpath = String.format(PATHWAY_ROW_XPATH_TEMPLATE, i);
            if (!isElementPresent(rowXpath)) {
                break;
            }
            String xpath = String.format(PATHWAY_ROW_APPROVAL_STATUS_XPATH_TEMPLATE, i);
            if (isElementPresent(xpath)) {
                String actualStatus = getText(xpath);
                LogUtils.info("Row " + (i + 1) + " status text: " + actualStatus);
                Assertion.assertEquals(actualStatus, expectedStatus, "Approval status mismatch at row " + (i + 1));
                highlightElement(xpath);
                scrollIntoView(xpath);
                isFound = true;
                break;
            }
            i++;
        }
        Assertion.assertTrue(isFound, "No approval status pill found in the table.");
    }



    public void edit_Drugs_dose(String newDose) {
        click(Edit_drug_dose);
        fill(Fill_doseamount, newDose);
        click(add_drug_submit);
        waitForPageLoad();
        WaitUtil.waitForNetworkIdle();
    }

    public void delete_drug() {
        click(delete_drug);
        waitForPageToLoad();
        click(confirm_delete_drug);
        waitForPageToLoad();
    }

    public void verify_remove_action() {
        waitForPageToLoad();
        waitForElement(remove_action, 10);
        Assertion.assertTrue(isVisible(remove_action), "Remove action is visible");
        highlightElement(remove_action);
    }

    public void move_to_supportive_drug_page() {
        waitForElement(move_to_supportive_drug, 10);
        click(move_to_supportive_drug);
        WaitUtil.waitForNetworkIdle();
        waitForPageLoad();
        highlightElement(current_page);
    }

    public void check_strikethrough_is_visible() {
        waitForPageLoad();
        int maxAttempts = 10;
        boolean isFound = false;
        String visibleXpath = "";
        for (int i = 0; i < maxAttempts; i++) {
            String xpath = String.format(PATHWAY_ROW_STRIKETHROUGH_XPATH_TEMPLATE, i);
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
            String xpath = String.format(PATHWAY_ROW_GREY_ROW_XPATH_TEMPLATE, i);
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

    public void restore_icon() {
        waitForElement(restore_icon_grey, 10);
        Assertion.assertTrue(isVisible(restore_icon_grey), "Restore icon is visible");
        highlightElement(restore_icon_grey);
    }

    public void scrollToOrderNumber() {
        scrollIntoView(Order_number);
    }

    public void scrollToRegimenDetails() {
        scrollIntoView(regimen_details1);
    }

    public void scrollToRegimenPopup() {
        scrollIntoView(Regimen_details_popup);
    }

    public void scrollToTableDrugs() {
        scrollIntoView(Table_Drugs1);
    }

    public void scrollToRemoveAction() {
        scrollIntoView(remove_action);
    }

    public void highlightCurrentpage() {
        highlightElement(current_page);
    }

    public void highlightregimecard() {
        for (int i = 0; i < 4; i++) {
            String xpath = String.format(REGIMEN_CARD_INDEX_XPATH_TEMPLATE, i);
            if (isElementPresent(xpath)) {
                LogUtils.info("Highlighting regimen card: " + (i + 1));
                highlightElement(xpath);
            }
        }
    }
}
