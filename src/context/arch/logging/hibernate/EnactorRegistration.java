package context.arch.logging.hibernate;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/** @author Hibernate CodeGenerator */
@Entity
public class EnactorRegistration implements Serializable {

	private static final long serialVersionUID = 9085104657008772388L;

    @Id 
    @GeneratedValue(strategy=GenerationType.IDENTITY)
	/** identifier field */
    private Integer enactorregistrationid;

    /** persistent field */
    private String enactorid;

    /** persistent field */
    private Date registrationtime;

	@OneToMany(fetch=FetchType.LAZY)
    /** persistent field */
    private Set<ComponentEvaluated> ComponentsEvaluated;

	@OneToMany(fetch=FetchType.LAZY)
    /** persistent field */
    private Set<ERParameter> ERParameters;

	@OneToMany(fetch=FetchType.LAZY)
    /** persistent field */
    private Set<ServiceExecution> ServiceExecutions;

	@OneToMany(fetch=FetchType.LAZY)
    /** persistent field */
    private Set<ParameterValueChanged> ParameterValuesChanged;

	@OneToMany(fetch=FetchType.LAZY)
    /** persistent field */
    private Set<ComponentAdded> ComponentsAdded;

	@OneToMany(fetch=FetchType.LAZY)
    /** persistent field */
    private Set<ERReference> ERReferences;

    /** full constructor */
    public EnactorRegistration(String enactorid, Date registrationtime, Set<ComponentEvaluated> ComponentsEvaluated, Set<ERParameter> ERParameters, Set<ServiceExecution> ServiceExecutions, Set<ParameterValueChanged> ParameterValuesChanged, Set<ComponentAdded> ComponentsAdded, Set<ERReference> ERReferences) {
        this.enactorid = enactorid;
        this.registrationtime = registrationtime;
        this.ComponentsEvaluated = ComponentsEvaluated;
        this.ERParameters = ERParameters;
        this.ServiceExecutions = ServiceExecutions;
        this.ParameterValuesChanged = ParameterValuesChanged;
        this.ComponentsAdded = ComponentsAdded;
        this.ERReferences = ERReferences;
    }

    /** default constructor */
    public EnactorRegistration() {
    }

    public Integer getEnactorregistrationid() {
        return this.enactorregistrationid;
    }

    public void setEnactorregistrationid(Integer enactorregistrationid) {
        this.enactorregistrationid = enactorregistrationid;
    }

    public String getEnactorid() {
        return this.enactorid;
    }

    public void setEnactorid(String enactorid) {
        this.enactorid = enactorid;
    }

    public Date getRegistrationtime() {
        return this.registrationtime;
    }

    public void setRegistrationtime(Date registrationtime) {
        this.registrationtime = registrationtime;
    }

    public Set<ComponentEvaluated> getComponentsEvaluated() {
        return this.ComponentsEvaluated;
    }

    public void setComponentsEvaluated(Set<ComponentEvaluated> ComponentsEvaluated) {
        this.ComponentsEvaluated = ComponentsEvaluated;
    }

    public Set<ERParameter> getERParameters() {
        return this.ERParameters;
    }

    public void setERParameters(Set<ERParameter> ERParameters) {
        this.ERParameters = ERParameters;
    }

    public Set<ServiceExecution> getServiceExecutions() {
        return this.ServiceExecutions;
    }

    public void setServiceExecutions(Set<ServiceExecution> ServiceExecutions) {
        this.ServiceExecutions = ServiceExecutions;
    }

    public Set<ParameterValueChanged> getParameterValuesChanged() {
        return this.ParameterValuesChanged;
    }

    public void setParameterValuesChanged(Set<ParameterValueChanged> ParameterValuesChanged) {
        this.ParameterValuesChanged = ParameterValuesChanged;
    }

    public Set<ComponentAdded> getComponentsAdded() {
        return this.ComponentsAdded;
    }

    public void setComponentsAdded(Set<ComponentAdded> ComponentsAdded) {
        this.ComponentsAdded = ComponentsAdded;
    }

    public Set<ERReference> getERReferences() {
        return this.ERReferences;
    }

    public void setERReferences(Set<ERReference> ERReferences) {
        this.ERReferences = ERReferences;
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("enactorregistrationid", getEnactorregistrationid())
            .toString();
    }

    public boolean equals(Object other) {
        if ( !(other instanceof EnactorRegistration) ) return false;
        EnactorRegistration castOther = (EnactorRegistration) other;
        return new EqualsBuilder()
            .append(this.getEnactorregistrationid(), castOther.getEnactorregistrationid())
            .isEquals();
    }

    public int hashCode() {
        return new HashCodeBuilder()
            .append(getEnactorregistrationid())
            .toHashCode();
    }

}
