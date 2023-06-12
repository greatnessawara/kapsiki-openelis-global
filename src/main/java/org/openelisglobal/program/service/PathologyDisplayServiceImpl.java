package org.openelisglobal.program.service;

import java.util.Optional;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.hl7.fhir.r4.model.Questionnaire;
import org.hl7.fhir.r4.model.QuestionnaireResponse;
import org.openelisglobal.common.util.IdValuePair;
import org.openelisglobal.dataexchange.fhir.FhirUtil;
import org.openelisglobal.dictionary.service.DictionaryService;
import org.openelisglobal.patient.valueholder.Patient;
import org.openelisglobal.program.valueholder.pathology.PathologyCaseViewDisplayItem;
import org.openelisglobal.program.valueholder.pathology.PathologyConclusion;
import org.openelisglobal.program.valueholder.pathology.PathologyConclusion.ConclusionType;
import org.openelisglobal.program.valueholder.pathology.PathologyDisplayItem;
import org.openelisglobal.program.valueholder.pathology.PathologyRequest.RequestType;
import org.openelisglobal.program.valueholder.pathology.PathologySample;
import org.openelisglobal.program.valueholder.pathology.PathologyTechnique.TechniqueType;
import org.openelisglobal.sample.service.SampleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PathologyDisplayServiceImpl implements PathologyDisplayService {

    @Autowired
    private SampleService sampleService;
    @Autowired
    private PathologySampleService pathologySampleService;
    @Autowired
    private DictionaryService dictionaryService;
    @Autowired
    private FhirUtil fhirUtil;

    @Override
    @Transactional
    public PathologyDisplayItem convertToDisplayItem(Integer pathologySampleId) {
        PathologySample pathologySample = pathologySampleService.get(pathologySampleId);
        PathologyDisplayItem displayItem = new PathologyDisplayItem();
        displayItem.setStatus(pathologySample.getStatus());
        displayItem.setRequestDate(pathologySample.getSample().getEnteredDate());
        if (pathologySample.getPathologist() != null) {
            displayItem.setAssignedPathologist(pathologySample.getPathologist().getDisplayName());
        }
        if (pathologySample.getTechnician() != null) {
            displayItem.setAssignedTechnician(pathologySample.getTechnician().getDisplayName());
        }
        Patient patient = sampleService.getPatient(pathologySample.getSample());
        displayItem.setFirstName(patient.getPerson().getFirstName());
        displayItem.setLastName(patient.getPerson().getLastName());
        displayItem.setLabNumber(pathologySample.getSample().getAccessionNumber());
        displayItem.setPathologySampleId(pathologySample.getId());
        return displayItem;
    }

    @Override
    @Transactional
    public PathologyCaseViewDisplayItem convertToCaseDisplayItem(Integer pathologySampleId) {
        PathologySample pathologySample = pathologySampleService.get(pathologySampleId);
        PathologyCaseViewDisplayItem displayItem = new PathologyCaseViewDisplayItem();
        displayItem.setStatus(pathologySample.getStatus());
        displayItem.setRequestDate(pathologySample.getSample().getEnteredDate());
        if (pathologySample.getPathologist() != null) {
            displayItem.setAssignedPathologist(pathologySample.getPathologist().getDisplayName());
        }
        if (pathologySample.getTechnician() != null) {
            displayItem.setAssignedTechnician(pathologySample.getTechnician().getDisplayName());
        }
        Patient patient = sampleService.getPatient(pathologySample.getSample());
        displayItem.setFirstName(patient.getPerson().getFirstName());
        displayItem.setLastName(patient.getPerson().getLastName());
        displayItem.setLabNumber(pathologySample.getSample().getAccessionNumber());
        displayItem.setPathologySampleId(pathologySample.getId());
        displayItem.setProgramQuestionnaire(fhirUtil.getLocalFhirClient().read().resource(Questionnaire.class)
                .withId(pathologySample.getProgram().getQuestionnaireUUID().toString()).execute());
        displayItem.setProgramQuestionnaireResponse(fhirUtil.getLocalFhirClient().read()
                .resource(QuestionnaireResponse.class).withId(pathologySample.getQuestionnaireResponseUuid().toString())
                .execute());

        displayItem.setGrossExam(pathologySample.getGrossExam());
        displayItem.setMicroscopyExam(pathologySample.getMicroscopyExam());

        displayItem.setConclusions(
                pathologySample.getConclusions().stream().filter(e -> e.getType() == ConclusionType.DICTIONARY)
                        .map(e -> new IdValuePair(e.getValue(), dictionaryService.get(e.getValue()).getLocalizedName()))
                        .collect(Collectors.toList()));
        Optional<PathologyConclusion> conclusion = pathologySample.getConclusions().stream()
                .filter(e -> e.getType() == ConclusionType.TEXT).findFirst();
        if (conclusion.isPresent())
            displayItem.setConclusionText(conclusion.get().getValue());

        displayItem.setConclusions(
                pathologySample.getConclusions().stream().filter(e -> e.getType() == ConclusionType.DICTIONARY)
                        .map(e -> new IdValuePair(e.getValue(), dictionaryService.get(e.getValue()).getLocalizedName()))
                        .collect(Collectors.toList()));
        displayItem.setTechniques(
                pathologySample.getTechniques().stream().filter(e -> e.getType() == TechniqueType.DICTIONARY)
                        .map(e -> new IdValuePair(e.getValue(), dictionaryService.get(e.getValue()).getLocalizedName()))
                        .collect(Collectors.toList()));
        displayItem.setRequests(pathologySample.getRequests().stream()
                .filter(e -> e.getType() == RequestType.DICTIONARY)
                        .map(e -> new IdValuePair(e.getValue(), dictionaryService.get(e.getValue()).getLocalizedName()))
                        .collect(Collectors.toList()));
        return displayItem;
    }

}
