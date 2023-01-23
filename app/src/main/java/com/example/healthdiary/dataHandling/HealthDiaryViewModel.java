package com.example.healthdiary.dataHandling;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.healthdiary.dataTypes.BloodPressureReading;
import com.example.healthdiary.dataTypes.BodyMassReading;
import com.example.healthdiary.dataTypes.HealthDiaryPatient;
import com.example.healthdiary.dataTypes.HealthDiaryLocation;

import org.hl7.fhir.r4.model.Address;
import org.hl7.fhir.r4.model.BooleanType;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Quantity;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.StringType;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import ca.uhn.fhir.context.FhirContext;

public class HealthDiaryViewModel extends ViewModel {
    private final MutableLiveData<HealthDiaryPatient> currentPatient = new MutableLiveData<>();
    private final MutableLiveData<HealthDiaryPatient> newPatient = new MutableLiveData<>();
    private final MutableLiveData<List<HealthDiaryPatient>> allPatients = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<BloodPressureReading>> allBpReadings = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<BodyMassReading>> allMReadings = new MutableLiveData<>(new ArrayList<>());

    private final MutableLiveData<HealthDiaryLocation> currentLocation = new MutableLiveData<>();
    private final MutableLiveData<State> viewState = new MutableLiveData<>(State.NONE);
    private final MutableLiveData<Boolean> cancellable = new MutableLiveData<>(false);
    private final MutableLiveData<String> medicationName = new MutableLiveData<>("");
    private final MutableLiveData<Integer[]> medicationTime = new MutableLiveData<>();
    private final MutableLiveData<String> fhirResource = new MutableLiveData<>("");
    private final FhirContext ctx;


    public HealthDiaryViewModel() {
        super();
        this.ctx = FhirContext.forR4();
    }

    public LiveData<HealthDiaryPatient> getCurrentPatient(){
        return currentPatient;
    }
    public void setCurrentPatient(HealthDiaryPatient newCurrentPatient){
        currentPatient.setValue(newCurrentPatient);
    }

    public LiveData<HealthDiaryPatient> getNewPatient(){
        return newPatient;
    }
    public void setNewPatient(HealthDiaryPatient newNewPatient){
        newPatient.setValue(newNewPatient);
    }

    public LiveData<List<HealthDiaryPatient>> getAllPatients(){
        return allPatients;
    }

    public HealthDiaryViewModel setAllPatients(List<HealthDiaryPatient> allPatients){
        this.allPatients.setValue(allPatients);
        return this;
    }

    public LiveData<List<BloodPressureReading>> getAllBpReadings() {
        return allBpReadings;
    }
    public void setAllBpReadings(List<BloodPressureReading> newReadings){allBpReadings.setValue(newReadings);}

    public LiveData<List<BodyMassReading>> getAllMReadings() {
        return allMReadings;
    }
    public void setAllMReadings(List<BodyMassReading> newReadings){allMReadings.setValue(newReadings);}

    public LiveData<HealthDiaryLocation> getLocation(){
        return currentLocation;
    }
    public void setLocation(HealthDiaryLocation newLocation){
        currentLocation.setValue(newLocation);
    }

    public LiveData<State> getState(){
        return viewState;
    }
    public void setState(State newState){
        viewState.setValue(newState);
    }

    public void setCancellable(boolean cancellable) {
        this.cancellable.setValue(cancellable);
    }
    public boolean getCancellable(){
        return Boolean.TRUE.equals(cancellable.getValue());
    }

    public LiveData<String> getMedicationName(){
        return medicationName;
    }
    public HealthDiaryViewModel setMedicationName(String newMedicationName){
        medicationName.setValue(newMedicationName);
        return this;
    }
    public LiveData<Integer[]> getMedicationTime(){
        return medicationTime;
    }
    public HealthDiaryViewModel setMedicationTime(int hourOfDay, int minute){
        medicationTime.setValue(new Integer[]{hourOfDay,minute});
        return this;
    }
    public HealthDiaryViewModel clearMedicationTime(){
        medicationTime.setValue(null);
        return this;
    }

    public LiveData<String> getFhirResource(){ return fhirResource; }

    public HealthDiaryViewModel createFhirResource(){
        Bundle result = new Bundle();
        result.setType(Bundle.BundleType.COLLECTION);
        HealthDiaryPatient c = getCurrentPatient().getValue();
        if(c == null){
            fhirResource.setValue("{\"error\":0xabcdef0123456789}");
            return this;
        }

        // patient
        Patient fhirPat = new Patient();
        String patId = UUID.randomUUID().toString();
        fhirPat.setActive(true).setDeceased(new BooleanType(false)).setId(patId);
        fhirPat.addName().setUse(HumanName.NameUse.OFFICIAL).setFamily(c.getLastName()).addGiven(c.getFirstNames()).setText(c.getFirstNames()+" "+c.getLastName());
        fhirPat.addIdentifier().setType(new CodeableConcept().addCoding(new Coding().setSystem("urn:oid:2.16.840.1.113883.18.108").setCode("SS").setDisplay("Social Security number"))) //OID: HL7:identifierType
                .setUse(Identifier.IdentifierUse.USUAL).setSystem("urn:oid:1.2.40.0.10.1.4.3.1").setValue(c.getSsn()); //OID: Österreichische Sozialversicherung
        List<StringType> addressList = new ArrayList<>();
        addressList.add(new StringType(c.getAddressLine()));
        fhirPat.addAddress().setUse(Address.AddressUse.HOME).setLine(addressList).setPostalCode(c.getZip()).setCountry(c.getCountry());
        fhirPat.setBirthDate(c.getDob());
        result.addEntry(new Bundle.BundleEntryComponent().setResource(fhirPat));

        // blood pressure
        for(BloodPressureReading reading : Objects.requireNonNull(getAllBpReadings().getValue())){
            if(!reading.getUnit().equals("N/A")) {
                Observation pressure = new Observation();

                pressure.setId(UUID.randomUUID().toString());
                pressure.setStatus(Observation.ObservationStatus.FINAL).setId(UUID.randomUUID().toString());
                pressure.addCategory(new CodeableConcept(new Coding("urn:oid:2.16.840.1.113883.4.642.3.403","vital-signs","Vital Signs"))); // OID: HL7:ValueSet/observation-category
                pressure.setCode(new CodeableConcept(new Coding("urn:oid:2.16.840.1.113883.6.1", BloodPressureReading.loinc, "Blood pressure panel"))); // OID: LOINC

                Quantity sysQtty = new Quantity(reading.getSys());
                sysQtty.setSystem("urn:oid:2.16.840.1.113883.6.8").setCode("mm[Hg]").setUnit(reading.getUnit()); // OID: UCUM
                Observation.ObservationComponentComponent sysComp = new Observation.ObservationComponentComponent(new CodeableConcept(new Coding("urn:oid:2.16.840.1.113883.6.1", BloodPressureReading.loincSys, "Systolic blood pressure")));
                pressure.addComponent(sysComp.setValue(sysQtty));

                Quantity diaQtty = new Quantity(reading.getDia());
                diaQtty.setSystem("urn:oid:2.16.840.1.113883.6.8").setCode("mm[Hg]").setUnit(reading.getUnit()); // OID: UCUM
                Observation.ObservationComponentComponent diaComp = new Observation.ObservationComponentComponent(new CodeableConcept(new Coding("urn:oid:2.16.840.1.113883.6.1", BloodPressureReading.loincDia, "Diastolic blood pressure")));
                pressure.addComponent(diaComp.setValue(diaQtty));

                result.addEntry(new Bundle.BundleEntryComponent().setResource(pressure));
            }
        }

        // weight
        for(BodyMassReading reading : Objects.requireNonNull(getAllMReadings().getValue())){
            if(!reading.getUnit().equals("N/A")){
                Observation weight = new Observation();
                weight.setStatus(Observation.ObservationStatus.FINAL).setId(UUID.randomUUID().toString());
                weight.addCategory(new CodeableConcept().addCoding(new Coding("urn:oid:2.16.840.1.113883.4.642.3.403","vital-signs","Vital Signs"))); // OID: HL7:ValueSet/observation-category
                weight.setSubject(new Reference(patId)).setEffective(new DateTimeType(new Date(reading.getTimeStamp())));
                weight.setCode(new CodeableConcept(new Coding("urn:oid:2.16.840.1.113883.6.1", BodyMassReading.loinc,"Body weight"))); // OID: LOINC

                Quantity qty = new Quantity(reading.getMass());
                qty.setSystem("urn:oid:2.16.840.1.113883.6.8").setCode(reading.getUnit()).setUnit(reading.getUnit()); // OID: UCUM
                weight.setValue(qty).addPerformer(new Reference(patId));

                result.addEntry(new Bundle.BundleEntryComponent().setResource(weight));
            }
        }

        // output total
        fhirResource.setValue(ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(result));
        return this;
    }

    public enum State {
        NONE, CREATE_NEW, LOGIN, CANCELLED, NOT_AVAILABLE, ADD, DONE, RATIONALE_SEEN, PERMISSION_GRANTED, NO_LAST_LOCATION
    }
}
