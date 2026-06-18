package com.framework.config;

import org.aeonbits.owner.Config;
import org.aeonbits.owner.Config.Key;
import org.aeonbits.owner.Config.LoadPolicy;
import org.aeonbits.owner.Config.Sources;

@LoadPolicy(Config.LoadType.MERGE)
@Sources({
        "file:src/test/resources/testdata/testdata.properties"
})
public interface TestData extends Config {

//    @Key("patient.mrn.valid")
//    String patientMrnValid();

    @Key("dose.amount")
    String doseAmount();

    @Key("approval.status")
    String approvalStatus();
//
//    @Key("patient.mrn.alternate")
//    String patientMrnAlternate();

    @Key("patient.mrn.orderlist")
    String patientMrnOrderlist();

    @Key("patient.mrn.invalid")
    String patientMrnInvalid();

    @Key("patient.firstname.default")
    String patientFirstnameDefault();

    @Key("patient.firstname.alt1")
    String patientFirstnameAlt1();

    @Key("patient.firstname.alt2")
    String patientFirstnameAlt2();

    @Key("patient.lastname.default")
    String patientLastnameDefault();

    @Key("patient.lastname.alt")
    String patientLastnameAlt();

    @Key("patient.name.full")
    String patientNameFull();

    @Key("patient.name.partial")
    String patientNamePartial();

    @Key("patient.dob.valid")
    String patientDobValid();

    @Key("partial.tnm")
    String tnmStage();

    @Key("page.supportive")
    String supportivePage();

    @Key("regimen1")
    String regimen1();

    @Key("regimen2")
    String regimen2();

    @Key("regimen3")
    String regimen3();

    @Key("regimen4")
    String regimen4();

    @Key("drug1.name")
    String drug1Name();

    @Key("drug1.dose_unit")
    String drug1DoseUnit();

    @Key("drug1.route")
    String drug1Route();

    @Key("drug2.name")
    String drug2Name();

    @Key("drug2.dose_unit")
    String drug2DoseUnit();

    @Key("drug2.route")
    String drug2Route();

    @Key("drug3.name")
    String drug3Name();

    @Key("drug3.dose_unit")
    String drug3DoseUnit();

    @Key("drug3.route")
    String drug3Route();

    @Key("drug4.name")
    String drug4Name();

    @Key("drug4.dose_unit")
    String drug4DoseUnit();

    @Key("drug4.route")
    String drug4Route();

    @Key("addDrugs")
    String addDrugs();

    @Key("comment")
    String comment();

    @Key("expacteddrugs")
    String expectedDrugs();

    @Key("supoortive.dose")
    String supoortiveDose();

    @Key("order.description")
    String orderDescription();

    @Key("disease.group")
    String diseaseGroup();

    @Key("line.of.therapy")
    String lineOfTherapy();

    @Key("performance.status")
    String performanceStatus();

    @Key("cancer.stage")
    String cancerStage();

    @Key("treatment.intent")
    String treatmentIntent();
}
