package com.framework.tests;

import com.framework.annotations.TestGroups;
import com.framework.config.TestDataManager;
import com.framework.pages.*;
import com.framework.utils.Assertion;
import com.framework.utils.LogUtils;
import com.framework.utils.WaitUtil;
import org.testng.annotations.Test;

public class TOI_UM_WorkFlow extends BaseTest {

        @Test(description = "Verify the entire end-to-end workflow (Test Cases 1 to 28)", groups = TestGroups.SANITY)
        public void ToI_UM_WorkFlow() {
                LogUtils.info("=== Running TestCaseID_29_TOI_PoC: Complete End-to-End Workflow ===");

                OrderListingPage orderListingPage = new OrderListingPage();
                SearchPatientPage searchPatientPage = new SearchPatientPage();
                PatientDetailsPage patientDetailsPage = new PatientDetailsPage();
                ClinicalInformationPage clinicalInformationPage = new ClinicalInformationPage();
                OrderDetailsPage orderDetailsPage = new OrderDetailsPage();
                SupportiveDrugsPage supportiveDrugsPage = new SupportiveDrugsPage();

                // 1. User is logged in on the UM portal
                LogUtils.info("[Test Case 1 / Step 1] Open UM portal and wait for network idle");
                orderListingPage.open();
                WaitUtil.waitForNetworkIdle();

                // orderListingPage.clickCreateOrder();

                // 2. Enter the MRN as per the test data
//                LogUtils.info("[Test Case 1 / Step 2] Enter the MRN from test data");
//                String mrnData = TestDataManager.testData().patientMrnAlternate();
                searchPatientPage.enterMRN(mrnData);
//                searchPatientPage.highlightMRNInput();
                String firstname=TestDataManager.testData().patientFirstnameDefault();
                searchPatientPage.enterfirstname(firstname);
                // 3. Click on the Search Patient button
                LogUtils.info("[Test Case 1 / Step 3] Click on the Search Patient button");
                searchPatientPage.clickSearchPatient();

                // Patient List should appear as per the entered MRN
                LogUtils.info("[Test Case 1 / Assertion 1] Verify patient result MRN matches entered MRN");
                String resultMrn = searchPatientPage.getPatientResultMRN(mrnData);
                searchPatientPage.highlightResult(mrnData);
                searchPatientPage.scrollIntoView(searchPatientPage.getCellXpath(mrnData));
                searchPatientPage.captureFullPageScreenshot("TC1_MRN_Search_Result");
                Assertion.assertEquals(resultMrn, mrnData, "TC 1 - Patient List should appear as per the entered MRN");

                // 1. Patient List should be appeared as per the entered MRN
                // 2. View Details and Create Order buttons should be visible
                LogUtils.info("[Test Case 2 / Assertion 1 & 2] Verify Action buttons are visible");
                searchPatientPage.highlightActionButtons();
                searchPatientPage.scrollToViewDetailsButton();
                searchPatientPage.captureFullPageScreenshot("TC2_Action_Buttons_Visible");

                boolean isViewDetailsVisible = searchPatientPage.isViewDetailsButtonVisible();
                Assertion.assertTrue(isViewDetailsVisible, "TC 2 - View Details button should be visible");

                boolean isCreateOrderVisible = searchPatientPage.isCreateOrderButtonVisible();
                Assertion.assertTrue(isCreateOrderVisible, "TC 2 - Create Order button should be visible");

                // 4. Click on the View Details button from the list
                LogUtils.info("[Test Case 3 / Step 4] Click on the View Details button from the list");
                searchPatientPage.clickViewDetails();
                WaitUtil.waitForNetworkIdle();

                // Following Patient details should be displayed by clicking on the view details
                // button
                LogUtils.info(
                                "[Test Case 3 / Assertion 1] Verify patient information details (Email, Phone, language, Address, Height, Weight) are visible");
                searchPatientPage.scrollToPatientDetailsField();
                searchPatientPage.highlightPatientDetailsField();
                searchPatientPage.captureFullPageScreenshot("TC3_Patient_Details");
                searchPatientPage.verifyPatientDetailFieldVisible1();

                // 5. Click on the Create Order button
                LogUtils.info("[Test Case 4 / Step 5] Click on the Create Order button in search result list");
                searchPatientPage.clickCreateOrderInResult();
                // orderListingPage.login();
                WaitUtil.waitForNetworkIdle();

                // The system should navigate to the 'Patient Details' page, where all the
                // details should be visible.
                LogUtils.info("[Test Case 4 / Assertion 1] Verify system has navigated to the 'Patient Details' page");
                patientDetailsPage.scrollToCurrentPageText();
                patientDetailsPage.highlightCurrentpage();
                patientDetailsPage.captureFullPageScreenshot("TC4_Patient_Details_Navigation");
                String currentPage = patientDetailsPage.getCurrentPageText();
                Assertion.assertEquals(currentPage, "Patient Details",
                                "TC 4 - System should navigate to the 'Patient Details' page");

                // 6. Click on the Create Order button from the Patient Details page
                LogUtils.info("[Test Case 5 / Step 6] Click on Create Order button from the Patient Details page");
                patientDetailsPage.clickFinalCreateOrder();
                WaitUtil.waitForNetworkIdle();

                // The Physician should be navigated to the clinical information page
                LogUtils.info("[Test Case 5 / Assertion 1] Verify system navigated to the Clinical Information page");
                WaitUtil.waitForLoadingScreen();
                clinicalInformationPage.scrollToCurrentPageText();
                clinicalInformationPage.highlightCurrentpage();
                clinicalInformationPage.captureFullPageScreenshot("TC5_Clinical_Information_Navigation");
                currentPage = clinicalInformationPage.getCurrentPageText();
                String actualText = currentPage.replaceAll("\\s+", " ").trim();
                Assertion.assertEquals(actualText, "Clinical Information",
                                "TC 5 - Physician should be navigated to the clinical information page");

                // 7. Fill the data in all fields as per the provided test data
                LogUtils.info("[Test Case 6 / Step 1] Fill the clinical information data as per test data");
                clinicalInformationPage.clinical_information_filling_order(
                                TestDataManager.testData().diseaseGroup(),
                                TestDataManager.testData().lineOfTherapy(),
                                TestDataManager.testData().performanceStatus(),
                                TestDataManager.testData().cancerStage(),
                                TestDataManager.testData().treatmentIntent(),
                                TestDataManager.testData().tnmStage());
                WaitUtil.waitForLoadingScreen();
                WaitUtil.waitForNetworkIdle();

                // Validate the Continue to pathway button is visible
                LogUtils.info("[Test Case 6 / Assertion 1] Verify the 'Continue to Pathway' button is visible");
                clinicalInformationPage.scrollToContinueToPathwayButton();
                clinicalInformationPage.highlightContinueToPathwayButton();
                clinicalInformationPage.captureFullPageScreenshot("TC6_Continue_To_Pathway_Button_Visible");
                boolean isContinueVisible = clinicalInformationPage.isContinueToPathwayButtonVisible();
                Assertion.assertTrue(isContinueVisible, "TC 6 - Continue to Pathway button should be visible");

                // Click on the Continue to pathway button
                LogUtils.info("[Test Case 7 / Step 2] Click on the 'Continue to Pathway' button");
                clinicalInformationPage.click_continue_to_pathway();
                WaitUtil.waitForLoadingScreen();
                WaitUtil.waitForNetworkIdle();

                // Order Number should be generated after navigating from the Clinical
                // Information page
                LogUtils.info("[Test Case 8 / Assertion 1] Verify Order Number is visible on the Order Details page");
                WaitUtil.waitForLoadingScreen();
                orderDetailsPage.scrollToOrderNumber();
                orderDetailsPage.captureFullPageScreenshot("TC8_Order_Number_Visible");
                orderDetailsPage.check_ordernumber();
                orderDetailsPage.highlightCurrentpage();

                // Validate the regimens for the entered clinical information are displayed
                LogUtils.info("[Test Case 9 / Assertion 1] Verify the correct regimens list is displayed on the page");
                WaitUtil.waitForLoadingScreen();
                orderDetailsPage.scrollToRegimenDetails();
                orderDetailsPage.captureFullPageScreenshot("TC9_Regimens_Displayed");
                orderDetailsPage.check_regimens_details(
                                TestDataManager.testData().regimen1(),
                                TestDataManager.testData().regimen2(),
                                TestDataManager.testData().regimen3(),
                                TestDataManager.testData().regimen4());

                // Click on the View Details action from the TOI- R-CVP regimen tile
                LogUtils.info("[Test Case 10 / Step 1] Click on the View Details action from the TOI- R-CVP regimen tile");
                WaitUtil.waitForLoadingScreen();
                orderDetailsPage.click_view_details();
                WaitUtil.waitForLoadingScreen();

                // Cancel & Use Pathway buttons should be visible on the popup
                LogUtils.info("[Test Case 11 / Assertion 1] Verify the Cancel and Use Pathway buttons are visible on popup");
                orderDetailsPage.scrollToRegimenPopup();
                orderDetailsPage.captureFullPageScreenshot("TC11_Cancel_Use_Pathway_Buttons_Visible");
                boolean areButtonsVisible = orderDetailsPage.areCancelAndUsePathwayButtonsVisible();
                Assertion.assertTrue(areButtonsVisible, "TC 11 - Cancel and Use Pathway buttons should be visible");

                // Validate the Drug Details for the TOI- R-CVP regimen
                LogUtils.info(
                                "[Test Case 12 / Assertion 1] Verify the Drugs details match the TOI- R-CVP regimen expectations");
                WaitUtil.waitForLoadingScreen();
                orderDetailsPage.scrollToRegimenPopup();
                orderDetailsPage.highlightDrugs_Card();
                orderDetailsPage.captureFullPageScreenshot("TC12_Drug_Details_Regimen");
                orderDetailsPage.validate_drugs_data(
                                TestDataManager.testData().drug1Name(), TestDataManager.testData().drug1DoseUnit(),
                                TestDataManager.testData().drug1Route(),
                                TestDataManager.testData().drug2Name(), TestDataManager.testData().drug2DoseUnit(),
                                TestDataManager.testData().drug2Route(),
                                TestDataManager.testData().drug3Name(), TestDataManager.testData().drug3DoseUnit(),
                                TestDataManager.testData().drug3Route(),
                                TestDataManager.testData().drug4Name(), TestDataManager.testData().drug4DoseUnit(),
                                TestDataManager.testData().drug4Route());

                // Click on the Use Pathway button
                LogUtils.info("[Test Case 13 / Step 2] Click on the Use Pathway button");
                WaitUtil.waitForLoadingScreen();
                orderDetailsPage.click_Use_Pathway();
                WaitUtil.waitForLoadingScreen();

                // Validate following Drug should appear in the Drug listing Table
                LogUtils.info(
                                "[Test Case 13 / Assertion 1] Verify the selected regimen drugs are displayed in the listing table");
                WaitUtil.waitForLoadingScreen();
                orderDetailsPage.scrollToTableDrugs();
                orderDetailsPage.highlight_table_drugs();
                orderDetailsPage.captureFullPageScreenshot("TC13_Regimen_Drugs_Table");
                orderDetailsPage.Verify_drugs(
                                TestDataManager.testData().drug1Name(),
                                TestDataManager.testData().drug2Name(),
                                TestDataManager.testData().drug3Name(),
                                TestDataManager.testData().drug4Name());

                // Click on the Add Drug button and search/select the required drug, then add
                // comments and click add
                LogUtils.info("[Test Case 14 / Step 3] Add a new drug J9271 - Pembrolizumab IV, 25 mg/ml solution");
                WaitUtil.waitForLoadingScreen();
                orderDetailsPage.add_Drugs(
                                TestDataManager.testData().addDrugs(),
                                TestDataManager.testData().comment());

                // Validate the added drug appears in the drug listing page
                LogUtils.info("[Test Case 14 / Assertion 1] Verify the newly added drug appears in the drug listing table");
                WaitUtil.waitForLoadingScreen();
                orderDetailsPage.scrollToTableDrugs();
                orderDetailsPage.verify_AddDrugs(TestDataManager.testData().expectedDrugs());
                orderDetailsPage.captureFullPageScreenshot("TC14_Added_Drug_Visible");

                // Validate the Change Action value should be appeared as "Newly Added"
                LogUtils.info(
                                "[Test Case 15 / Assertion 1] Verify the change action value for the newly added drug is 'Newly Added'");
                WaitUtil.waitForLoadingScreen();
                orderDetailsPage.scrollToTableDrugs();
                orderDetailsPage.newly_add_drugs();
                orderDetailsPage.captureFullPageScreenshot("TC15_Change_Action_Newly_Added");

                // Validate the Approval Status value should be appeared as "Requires Approval"
                LogUtils.info(
                                "[Test Case 16 / Assertion 1] Verify the approval status value for the newly added drug is 'Requires Approval'");
                WaitUtil.waitForLoadingScreen();
                orderDetailsPage.scrollToTableDrugs();
                orderDetailsPage.check_approval(TestDataManager.testData().approvalStatus());
                orderDetailsPage.captureFullPageScreenshot("TC16_Approval_Status_Requires_Approval");

                // Click on the edit action for the drug, update Dose/unit, click save
                LogUtils.info("[Test Case 17 / Step 1] Edit drug dose value (increase) and save");
                WaitUtil.waitForLoadingScreen();
                orderDetailsPage.edit_Drugs_dose(TestDataManager.testData().doseAmount());
                WaitUtil.waitForLoadingScreen();

                // Verify that updated details and approval status is "Requires Approval" (or
                // "Require Approval" as per data)
                LogUtils.info(
                                "[Test Case 17 / Assertion 1] Verify that increased dose changes the approval status to 'Require Approval'");
                WaitUtil.waitForLoadingScreen();
                orderDetailsPage.scrollToTableDrugs();
                orderDetailsPage.check_approval(TestDataManager.testData().approvalStatus());
                orderDetailsPage.captureFullPageScreenshot("TC17_Increased_Dose_Requires_Approval");

                // Click on the Delete action for the any drug, confirm delete
                LogUtils.info("[Test Case 18 / Step 1] Click on Delete action for the drug and confirm deletion");
                WaitUtil.waitForLoadingScreen();
                orderDetailsPage.delete_drug();

                // Validate the deleted details: change action column should be appeared as
                // Removed
                LogUtils.info("[Test Case 18 / Assertion 1] Verify that deleted drug shows status pill as Removed");
                WaitUtil.waitForLoadingScreen();
                orderDetailsPage.scrollToRemoveAction();
                orderDetailsPage.verify_remove_action();
                LogUtils.info("[Test Case 18 / Assertion 2] Verify that deleted drug shows strikethrough");
                orderDetailsPage.check_strikethrough_is_visible();
                LogUtils.info("[Test Case 18 / Assertion 3] Verify that deleted drug shows grey text");
                orderDetailsPage.check_grey_text_is_visible();
                LogUtils.info("[Test Case 18 / Assertion 4] Verify that deleted drug shows grey icon");
                orderDetailsPage.restore_icon();
                orderDetailsPage.captureFullPageScreenshot("TC18_Deleted_Drug_Status_Removed");

                // Click on "Continue to Supportive Drug" from the Order Details page
                LogUtils.info("[Test Case 19 / Step 1] Click on 'Continue to Supportive Drug' button");
                WaitUtil.waitForLoadingScreen();
                orderDetailsPage.move_to_supportive_drug_page();

                // Verify system navigates to Supportive Drugs page
                LogUtils.info("[Test Case 19 / Assertion 1] Verify navigation to the Supportive Drugs page");
                WaitUtil.waitForLoadingScreen();
                supportiveDrugsPage.scrollToSubmitSupportiveDrug();
                supportiveDrugsPage.verify_supportive_drug_page();
                supportiveDrugsPage.captureFullPageScreenshot("TC19_Supportive_Drugs_Navigation");

                // Click on the Add Drug button, Search / Select, add comments, click add
                LogUtils.info("[Test Case 20 / Step 1] Add a drug on the Supportive Drug page");
                WaitUtil.waitForLoadingScreen();
                supportiveDrugsPage.add_Drugs(
                                TestDataManager.testData().addDrugs(),
                                TestDataManager.testData().comment());

                // Validate newly added drug change action value is "Newly Added"
                LogUtils.info(
                                "[Test Case 21 / Assertion 1] Verify the change action value for the newly added supportive drug is 'Newly Added'");
                WaitUtil.waitForLoadingScreen();
                supportiveDrugsPage.scrollToSubmitSupportiveDrug();
                supportiveDrugsPage.newly_add_drugs();
                supportiveDrugsPage.captureFullPageScreenshot("TC21_Supportive_Drug_Newly_Added");

                // Validate newly added drug approval status is "Requires Approval"
                LogUtils.info(
                                "[Test Case 22 / Assertion 1] Verify the approval status for the newly added supportive drug is 'Requires Approval'");
                WaitUtil.waitForLoadingScreen();
                supportiveDrugsPage.scrollToSubmitSupportiveDrug();
                supportiveDrugsPage.check_approval1(TestDataManager.testData().approvalStatus());
                supportiveDrugsPage.captureFullPageScreenshot("TC22_Supportive_Drug_Requires_Approval");

                // Click on edit action, update Dose/unit, click Save
                LogUtils.info("[Test Case 23 / Step 1] Edit supportive drug dose value and click Save");
                WaitUtil.waitForLoadingScreen();
                supportiveDrugsPage.edit_Supportive_drug_dose(TestDataManager.testData().supoortiveDose());
                WaitUtil.waitForLoadingScreen();

                // Validate updated dose is displayed
                LogUtils.info(
                                "[Test Case 23 / Assertion 1 & 2] Verify updated actual dose and approval status in supportive drug table");
                WaitUtil.waitForLoadingScreen();
                supportiveDrugsPage.scrollToSubmitSupportiveDrug();
                supportiveDrugsPage.verify_supportive_drug_dose(TestDataManager.testData().supoortiveDose());
                supportiveDrugsPage.check_approval1(TestDataManager.testData().approvalStatus());
                supportiveDrugsPage.captureFullPageScreenshot("TC23_Supportive_Drug_Dose_Increase");

                // Click on Delete action for the supportive drug, confirm deletion
                LogUtils.info("[Test Case 24 / Step 1] Delete the supportive drug and confirm");
                WaitUtil.waitForLoadingScreen();
                supportiveDrugsPage.delete_supportive_drug();

                // Validate the deleted details (removed action badge visible)
                LogUtils.info("[Test Case 24 / Assertion 1] Verify that deleted supportive drug displays 'Removed' badge");
                WaitUtil.waitForLoadingScreen();
                supportiveDrugsPage.verify_supportive_remove_action();
                LogUtils.info("[Test Case 24 / Assertion 2] Verify that deleted drug shows strikethrough");
                supportiveDrugsPage.check_strikethrough_is_visible();
                LogUtils.info("[Test Case 24 / Assertion 3] Verify that deleted drug shows grey text");
                supportiveDrugsPage.check_grey_text_is_visible();
                LogUtils.info("[Test Case 24 / Assertion 4] Verify that deleted drug shows grey icon");
                // supportiveDrugsPage.restore_icon();

                supportiveDrugsPage.captureFullPageScreenshot("TC24_Supportive_Drug_Removed_Badge");

                // Click on the Submit Order button
                LogUtils.info("[Test Case 25 / Step 1] Click on the Submit Order button");
                WaitUtil.waitForLoadingScreen();
                supportiveDrugsPage.submit_order();
                LogUtils.info("[Test Case 25 / Step 2] verify the pop up is show");
                supportiveDrugsPage.check_submit_popup();
                supportiveDrugsPage.captureFullPageScreenshot("TC25_Submit_popup");

                // Validate redirection to the Order Listing page
                LogUtils.info("[Test Case 25 / Assertion 1] Verify system has redirected to the Order Listing page");
                WaitUtil.waitForLoadingScreen();

                orderListingPage.verify_redirection_to_order_listing();
                orderListingPage.captureFullPageScreenshot("TC25_Redirect_To_Order_Listing");
                        
                // Validate that newly created order appears in listing at the first position of
                // the order listing table
                LogUtils.info(
                                "[Test Case 26 / Assertion 1] Verify newly created order appears at first position of order listing table");
                WaitUtil.waitForLoadingScreen();
                orderListingPage.scrollToOrderlistStatus();
                orderListingPage.verify_orderlist_page(
                                TestDataManager.testData().patientMrnValid(),
                                TestDataManager.testData().orderDescription());
                orderListingPage.captureFullPageScreenshot("TC26_New_Order_At_First_Position");

                // Verify Order Status is "Submitted", Approval Status matches expectation, and
                // MRN is a hyperlink
                LogUtils.info(
                                "[Test Case 27 / Assertion 1] Verify Order Status is 'Submitted', Approval Status matches, and MRN is clickable");
                WaitUtil.waitForLoadingScreen();
                orderListingPage.scrollToOrderlistStatus();
                orderListingPage.verify_status();
                orderListingPage.captureFullPageScreenshot("TC27_Order_Status_Submitted");

                // Click on the MRN hyperlink
                LogUtils.info("[Test Case 28 / Step 3] Click on the MRN hyperlink from the order listing page");
                WaitUtil.waitForLoadingScreen();
                orderListingPage.click_mrn_hyperlink();

                // Verify system redirects to the Supportive Drug page
                LogUtils.info("[Test Case 28 / Assertion 1] Verify the system redirects to the Supportive Drug page");
                WaitUtil.waitForLoadingScreen();
                supportiveDrugsPage.scrollToSubmitSupportiveDrug();
                supportiveDrugsPage.verify_supportive_drug_page();
                supportiveDrugsPage.captureFullPageScreenshot("TC28_Redirect_Back_To_Supportive_Drugs");
                LogUtils.info("[Test Case 29 / Step 1] Logout from the application");
                orderListingPage.logout();
                WaitUtil.waitForLoadingScreen();
                orderListingPage.captureFullPageScreenshot("TC29_Logout");
                // orderListingPage.logout();

                LogUtils.info("=== TestCaseID_29_TOI_PoC completed successfully! ===");
        }
}
