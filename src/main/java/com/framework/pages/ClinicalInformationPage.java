package com.framework.pages;

import com.framework.utils.LogUtils;
import com.framework.utils.WaitUtil;

public class ClinicalInformationPage extends BasePage {

    private final String disease_group = "#diseaseGroup";
    private final String line_of_therapy = "#lineOfTherapy";
    private final String performance_status_dropdown = "#performanceStatus";
    private final String cancer_stage_dropdown = "#cancerStage";
    private final String treatment_intent_dropdown = "#treatmentIntent";
    private final String tnm_input = "#tnm";
    private final String continue_to_pathway = "#continueBtn";
    private final String current_page = ".breadcrumb > span";
    private final String ask="//*[contains(@class,'question') and .//span[contains(normalize-space(),'Cisplatin')]]";
    private final String radio_yes_button="//label[.//input[@value='yes'] and .//span[text()='Yes']]";
    private final String radio_no_button="//label[.//input[@value='no'] and .//span[text()='no']]";
    private static final String DROPDOWN_OPTION_TEMPLATE = "div.option:has-text(\"%1$s\")";

    public void clinical_information_filling_order(
            String diseaseGroup,
            String lineOfTherapy,
            String performanceStatus,
            String cancerStage,
            String treatmentIntent,
            String tnmStage) {
        click(disease_group);
        click(String.format(DROPDOWN_OPTION_TEMPLATE, diseaseGroup));
        // highlightElement(diseaseGroup);
        WaitUtil.waitForLoadingScreen();
        click(line_of_therapy);
        click(String.format(DROPDOWN_OPTION_TEMPLATE, lineOfTherapy));
        WaitUtil.waitForLoadingScreen();
        click(performance_status_dropdown);
        click(String.format(DROPDOWN_OPTION_TEMPLATE, performanceStatus));
        WaitUtil.waitForLoadingScreen();
        click(cancer_stage_dropdown);
        click(String.format(DROPDOWN_OPTION_TEMPLATE, cancerStage));
        WaitUtil.waitForLoadingScreen();
        click(treatment_intent_dropdown);
        click(String.format(DROPDOWN_OPTION_TEMPLATE, treatmentIntent));
        WaitUtil.waitForLoadingScreen();
        fill(tnm_input, tnmStage);
        waitForElement(continue_to_pathway, 15);
    }

    public void click_continue_to_pathway() {
        click(continue_to_pathway);
        WaitUtil.waitForNetworkIdle();
        waitForElement(current_page);
        highlightElement(current_page);
    }
    public void click_radio_option(){
         WaitUtil.waitForNetworkIdle();
         boolean radio= page().isVisible(ask);
         if(radio){
             page().isVisible(radio_yes_button);
             click(radio_yes_button);
             WaitUtil.waitForNetworkIdle();
             if(page().isVisible(continue_to_pathway)){
                 click(continue_to_pathway);
                 WaitUtil.waitForNetworkIdle();
             }
             else{
                 click(radio_no_button);
                 WaitUtil.waitForNetworkIdle();
                 if(page().isVisible(continue_to_pathway)){
                     click(continue_to_pathway);
                     WaitUtil.waitForNetworkIdle();
                 }
             }
         }
         page().pause();
    }

    public boolean isContinueToPathwayButtonVisible() {
        return isVisible(continue_to_pathway);
    }

    public void highlightContinueToPathwayButton() {
        highlightElement(continue_to_pathway);
    }

    public void scrollToContinueToPathwayButton() {
        scrollIntoView(continue_to_pathway);
    }

    public void highlightCurrentpage() {
        highlightElement(current_page);
    }

    public String getCurrentPageText() {
        return getText(current_page);
    }

    public void scrollToCurrentPageText() {
        scrollIntoView(current_page);
    }

}
