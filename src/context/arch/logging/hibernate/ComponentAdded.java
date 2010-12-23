package context.arch.logging.hibernate;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/** @author Hibernate CodeGenerator */
@Entity
public class ComponentAdded implements Serializable {

	private static final long serialVersionUID = -5407681335941617969L;

    @Id 
    @GeneratedValue(strategy=GenerationType.IDENTITY)
	/** identifier field */
    private Integer componentaddedid;

    /** persistent field */
    private String componentdescriptionid;

    /** persistent field */
    private Date componentaddedtime;

    @ManyToOne
	@JoinColumn(name = "erreferenceid")
    /** persistent field */
    private context.arch.logging.hibernate.ERReference ERReference;

    @ManyToOne
	@JoinColumn(name = "enactorregistrationid")
    /** persistent field */
    private context.arch.logging.hibernate.EnactorRegistration EnactorRegistration;

	@OneToMany(fetch=FetchType.LAZY)
    /** persistent field */
    private Set<CAParamAttribute> CAParamAttributes;

	@OneToMany(fetch=FetchType.LAZY)
    /** persistent field */
    private Set<ServiceExecution> ServiceExecutions;

    /** full constructor */
    public ComponentAdded(String componentdescriptionid, Date componentaddedtime, context.arch.logging.hibernate.ERReference ERReference, context.arch.logging.hibernate.EnactorRegistration EnactorRegistration, Set<CAParamAttribute> CAParamAttributes, Set<ServiceExecution> ServiceExecutions) {
        this.componentdescriptionid = componentdescriptionid;
        this.componentaddedtime = componentaddedtime;
        this.ERReference = ERReference;
        this.EnactorRegistration = EnactorRegistration;
        this.CAParamAttributes = CAParamAttributes;
        this.ServiceExecutions = ServiceExecutions;
    }

    /** default constructor */
    public ComponentAdded() {
    }

    public Integer getComponentaddedid() {
        return this.componentaddedid;
    }

    public void setComponentaddedid(Integer componentaddedid) {
        this.componentaddedid = componentaddedid;
    }

    public String getComponentdescriptionid() {
        return this.componentdescriptionid;
    }

    public void setComponentdescriptionid(String componentdescriptionid) {
        this.componentdescriptionid = componentdescriptionid;
    }

    public Date getComponentaddedtime() {
        return this.componentaddedtime;
    }

    public void setComponentaddedtime(Date componentaddedtime) {
        this.componentaddedtime = componentaddedtime;
    }

    public context.arch.logging.hibernate.ERReference getERReference() {
        return this.ERReference;
    }

    public void setERReference(context.arch.logging.hibernate.ERReference ERReference) {
        this.ERReference = ERReference;
    }

    public context.arch.logging.hibernate.EnactorRegistration getEnactorRegistration() {
        return this.EnactorRegistration;
    }

    public void setEnactorRegistration(context.arch.logging.hibernate.EnactorRegistration EnactorRegistration) {
        this.EnactorRegistration = EnactorRegistration;
    }

    public Set<CAParamAttribute> getCAParamAttributes() {
        return this.CAParamAttributes;
    }

    public void setCAParamAttributes(Set<CAParamAttribute> CAParamAttributes) {
        this.CAParamAttributes = CAParamAttributes;
    }

    public Set<ServiceExecution> getServiceExecutions() {
        return this.ServiceExecutions;
    }

    public void setServiceExecutions(Set<ServiceExecution> ServiceExecutions) {
        this.ServiceExecutions = ServiceExecutions;
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("componentaddedid", getComponentaddedid())
            .toString();
    }

    public boolean equals(Object other) {
        if ( !(other instanceof ComponentAdded) ) return false;
        ComponentAdded castOther = (ComponentAdded) other;
        return new EqualsBuilder()
            .append(this.getComponentaddedid(), castOther.getComponentaddedid())
            .isEquals();
    }

    public int hashCode() {
        return new HashCodeBuilder()
            .append(getComponentaddedid())
            .toHashCode();
    }

}
