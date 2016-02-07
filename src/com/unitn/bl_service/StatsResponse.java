
package com.unitn.bl_service;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for statsResponse complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="statsResponse">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="chart" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="nrGoalsDone" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="nrTodoGoals" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "statsResponse", propOrder = {
    "chart",
    "nrGoalsDone",
    "nrTodoGoals"
})
public class StatsResponse {

    protected String chart;
    protected int nrGoalsDone;
    protected int nrTodoGoals;

    /**
     * Gets the value of the chart property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getChart() {
        return chart;
    }

    /**
     * Sets the value of the chart property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setChart(String value) {
        this.chart = value;
    }

    /**
     * Gets the value of the nrGoalsDone property.
     * 
     */
    public int getNrGoalsDone() {
        return nrGoalsDone;
    }

    /**
     * Sets the value of the nrGoalsDone property.
     * 
     */
    public void setNrGoalsDone(int value) {
        this.nrGoalsDone = value;
    }

    /**
     * Gets the value of the nrTodoGoals property.
     * 
     */
    public int getNrTodoGoals() {
        return nrTodoGoals;
    }

    /**
     * Sets the value of the nrTodoGoals property.
     * 
     */
    public void setNrTodoGoals(int value) {
        this.nrTodoGoals = value;
    }

}
