
package com.unitn.bl_service;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the com.unitn.bl_service package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _RegisterNewUser_QNAME = new QName("http://bl_service.unitn.com/", "registerNewUser");
    private final static QName _SaveNewStepsResponse_QNAME = new QName("http://bl_service.unitn.com/", "saveNewStepsResponse");
    private final static QName _GetDescriptionResponse_QNAME = new QName("http://bl_service.unitn.com/", "getDescriptionResponse");
    private final static QName _RegisterNewUserResponse_QNAME = new QName("http://bl_service.unitn.com/", "registerNewUserResponse");
    private final static QName _NewStepResponse_QNAME = new QName("http://bl_service.unitn.com/", "newStepResponse");
    private final static QName _StatsResponse_QNAME = new QName("http://bl_service.unitn.com/", "statsResponse");
    private final static QName _Statistics_QNAME = new QName("http://bl_service.unitn.com/", "statistics");
    private final static QName _SaveNewSteps_QNAME = new QName("http://bl_service.unitn.com/", "saveNewSteps");
    private final static QName _StatisticsResponse_QNAME = new QName("http://bl_service.unitn.com/", "statisticsResponse");
    private final static QName _GetDescription_QNAME = new QName("http://bl_service.unitn.com/", "getDescription");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com.unitn.bl_service
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link RegisterNewUser }
     * 
     */
    public RegisterNewUser createRegisterNewUser() {
        return new RegisterNewUser();
    }

    /**
     * Create an instance of {@link SaveNewStepsResponse }
     * 
     */
    public SaveNewStepsResponse createSaveNewStepsResponse() {
        return new SaveNewStepsResponse();
    }

    /**
     * Create an instance of {@link GetDescriptionResponse }
     * 
     */
    public GetDescriptionResponse createGetDescriptionResponse() {
        return new GetDescriptionResponse();
    }

    /**
     * Create an instance of {@link NewStepResponse }
     * 
     */
    public NewStepResponse createNewStepResponse() {
        return new NewStepResponse();
    }

    /**
     * Create an instance of {@link StatsResponse }
     * 
     */
    public StatsResponse createStatsResponse() {
        return new StatsResponse();
    }

    /**
     * Create an instance of {@link RegisterNewUserResponse }
     * 
     */
    public RegisterNewUserResponse createRegisterNewUserResponse() {
        return new RegisterNewUserResponse();
    }

    /**
     * Create an instance of {@link SaveNewSteps }
     * 
     */
    public SaveNewSteps createSaveNewSteps() {
        return new SaveNewSteps();
    }

    /**
     * Create an instance of {@link StatisticsResponse }
     * 
     */
    public StatisticsResponse createStatisticsResponse() {
        return new StatisticsResponse();
    }

    /**
     * Create an instance of {@link GetDescription }
     * 
     */
    public GetDescription createGetDescription() {
        return new GetDescription();
    }

    /**
     * Create an instance of {@link Statistics }
     * 
     */
    public Statistics createStatistics() {
        return new Statistics();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link RegisterNewUser }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://bl_service.unitn.com/", name = "registerNewUser")
    public JAXBElement<RegisterNewUser> createRegisterNewUser(RegisterNewUser value) {
        return new JAXBElement<RegisterNewUser>(_RegisterNewUser_QNAME, RegisterNewUser.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SaveNewStepsResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://bl_service.unitn.com/", name = "saveNewStepsResponse")
    public JAXBElement<SaveNewStepsResponse> createSaveNewStepsResponse(SaveNewStepsResponse value) {
        return new JAXBElement<SaveNewStepsResponse>(_SaveNewStepsResponse_QNAME, SaveNewStepsResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetDescriptionResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://bl_service.unitn.com/", name = "getDescriptionResponse")
    public JAXBElement<GetDescriptionResponse> createGetDescriptionResponse(GetDescriptionResponse value) {
        return new JAXBElement<GetDescriptionResponse>(_GetDescriptionResponse_QNAME, GetDescriptionResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link RegisterNewUserResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://bl_service.unitn.com/", name = "registerNewUserResponse")
    public JAXBElement<RegisterNewUserResponse> createRegisterNewUserResponse(RegisterNewUserResponse value) {
        return new JAXBElement<RegisterNewUserResponse>(_RegisterNewUserResponse_QNAME, RegisterNewUserResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link NewStepResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://bl_service.unitn.com/", name = "newStepResponse")
    public JAXBElement<NewStepResponse> createNewStepResponse(NewStepResponse value) {
        return new JAXBElement<NewStepResponse>(_NewStepResponse_QNAME, NewStepResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StatsResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://bl_service.unitn.com/", name = "statsResponse")
    public JAXBElement<StatsResponse> createStatsResponse(StatsResponse value) {
        return new JAXBElement<StatsResponse>(_StatsResponse_QNAME, StatsResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Statistics }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://bl_service.unitn.com/", name = "statistics")
    public JAXBElement<Statistics> createStatistics(Statistics value) {
        return new JAXBElement<Statistics>(_Statistics_QNAME, Statistics.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SaveNewSteps }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://bl_service.unitn.com/", name = "saveNewSteps")
    public JAXBElement<SaveNewSteps> createSaveNewSteps(SaveNewSteps value) {
        return new JAXBElement<SaveNewSteps>(_SaveNewSteps_QNAME, SaveNewSteps.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StatisticsResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://bl_service.unitn.com/", name = "statisticsResponse")
    public JAXBElement<StatisticsResponse> createStatisticsResponse(StatisticsResponse value) {
        return new JAXBElement<StatisticsResponse>(_StatisticsResponse_QNAME, StatisticsResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetDescription }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://bl_service.unitn.com/", name = "getDescription")
    public JAXBElement<GetDescription> createGetDescription(GetDescription value) {
        return new JAXBElement<GetDescription>(_GetDescription_QNAME, GetDescription.class, null, value);
    }

}
